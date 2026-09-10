package fr.iamacat.autoplay;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

/**
 * Autoplay companion (DEV ONLY, never ships): drives the scripted client
 * proof without a human at the keyboard. On the first client tick it joins
 * the pre-seeded flat world (run-client.sh preseeds saves/&lt;world&gt;,
 * refusing loudly when absent), then counts loaded ticks near spawn and
 * shuts the game down cleanly. World == pure union is judged afterwards
 * by hub tools/verify-client-save.sh -- this mod never places a block, so
 * any foreign block fails loudly there, never here silently.
 *
 * Ticks: WAIT_TICKS overshoots the 4000-tick union on purpose (world load
 * and join cost wall ticks before the first world tick; overshoot only
 * re-lands the same deterministic cells -- the union is a fixed point).
 * World name follows AUTOPLAY_WORLD (default matou), matching verify.
 *
 * <p>Spawn proof (SPAWN=1, DEV ONLY): the bridge itself lands budgeted
 * pigs (SPAWN=1 also arms {@code MatouBridgeMod.spawnTick} -- one flag
 * drives both sides, so union runs stay spawn-free). The companion never
 * spawns here: it polls the loaded pigs the bridge landed up to cap,
 * records the maximum seen (past cap fails loudly -- the veto owns that
 * bound), kills the first pig past SPAWN_KILL_TICK with a simulated
 * {@code LivingDropsEvent} post (loot honesty standard -- the kill pays
 * through the loot table, proving the spawn-to-loot chain), then polls
 * the diamond carrier at the kill spot. The first living pig's max health
 * is polled once against SPAWN_HP (hp tranche -- the bridge applies the
 * content hp per landing; a diverged read-back fails loudly here too).
 * T1 vanilla scope: the census IS pigs (the species narrows with
 * custom-entity registration). A missing pig, a breached cap, or a
 * missing carrier fails loudly (spawn FAILED) and shuts the game down
 * for post-mortem. Without SPAWN=1 nothing here runs and the proof is
 * byte-for-byte the proven union run.
 *
 * <p>1.16.5 native spelling (measured against the pinned 36.2.42 bytes,
 * never ported blind from 1122): the census poll is
 * {@code World.getEntitiesWithinAABB} (no {@code loadedEntityList} field
 * ships on 1.16.5); the living check is the {@code removed} field (the
 * 1.12 {@code isDead} name does not port); coords go through
 * {@code getPosX/Y/Z} (no {@code posX} fields); the id through
 * {@code getEntityId}; the simulated kill removes through
 * {@code remove()} (the 1.12 {@code setDead} name does not port); the
 * carrier content reads through {@code ItemEntity.getItem} /
 * {@code ItemStack.getItem} against {@code Items.DIAMOND}. Owner
 * discipline (hub decisions/LOOT.md): inherited vanilla members go
 * through the declaring stub type ({@code Entity},
 * {@code LivingEntity}), never the pig.
 */
@Mod(AutoplayMod.MODID)
public class AutoplayMod {
    public static final String MODID = "matouautoplay";
    static final int WAIT_TICKS = 5200;
    static final String WORLD = System.getenv().getOrDefault("AUTOPLAY_WORLD", "matou");
    static final boolean SPAWN = "1".equals(System.getenv("SPAWN"));
    /** Mirrors the effective cap (content {@code owned.matou mob my_beast
     * cap} default, operator {@code spawn.cap} wins -- transported by the
     * bridge spawn wire): the companion counts pigs, the effective
     * policy owns the bound -- a drift here fails the proof loudly
     * instead of asserting a stale cap silently. Override proofs set
     * {@code SPAWN_CAP} to the packs.cfg override (both sides name the
     * same bound, or the breach check is blind).
     */
    static final int SPAWN_CAP = spawnCapOfEnv();
    /** Mirrors the content hp ({@code owned.matou mob my_beast hp} via
     * {@code MatouBridgeMod} spawn wire): the companion polls the landed
     * max health, the bridge owns the value -- a drift here fails the
     * proof loudly instead of asserting a stale hp silently. */
    static final float SPAWN_HP = 20.0f;
    static final int SPAWN_KILL_TICK = 1000;
    static final int SPAWN_TIMEOUT = 600;
    /** Tranche-1 census window: same box the bridge reconciles (see
     * {@code MatouBridgeMod.CENSUS_BOX}, smaller here -- the companion
     * only watches the pads and the kill spot, both near spawn). */
    static final AxisAlignedBB CENSUS_BOX = new AxisAlignedBB(
            -64, 0, -64, 64, 256, 64);

    volatile int worldTicks = 0;
    volatile boolean pigSeen = false;
    volatile boolean hpSeen = false;
    volatile int maxPigs = 0;
    volatile int firstPigTick = -1;
    volatile boolean pigKilled = false;
    volatile int killTick = -1;
    volatile int killX = 0;
    volatile int killY = 0;
    volatile int killZ = 0;
    volatile boolean carrierDropped = false;
    volatile boolean spawnFailed = false;
    volatile int carrierTick = -1;
    int ticks = 0;
    World world = null;
    boolean foreignNoted = false;
    boolean joined = false;
    boolean done = false;

    public AutoplayMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Effective cap want: {@code SPAWN_CAP} env wins, default 4 is the
     * content cap. Loud on garbage -- a defaulted bound blinds the
     * breach check silently otherwise. DEV-only.
     */
    private static int spawnCapOfEnv() {
        String raw = System.getenv("SPAWN_CAP");
        if (raw == null || raw.isEmpty()) {
            return 4;
        }
        try {
            int v = Integer.parseInt(raw);
            if (v <= 0) {
                throw new NumberFormatException("non-positive");
            }
            return v;
        } catch (RuntimeException bad) {
            throw new IllegalArgumentException(
                    "E_AUTOPLAY_SPAWN_CAP:bad <" + raw
                            + "> (want positive int, default 4)");
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side != LogicalSide.SERVER
                || event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!SPAWN || spawnFailed) {
            return;
        }
        if (!World.OVERWORLD.equals(event.world.getDimensionKey())) {
            if (!foreignNoted) {
                foreignNoted = true;
                System.out.println("[MatouAutoplay] note spawn-proof : "
                        + "ignoring non-overworld world ticks (the "
                        + "integrated server ticks every dim from boot)");
            }
            return;
        }
        if (world == null) {
            world = event.world;
            System.out.println("[MatouAutoplay] spawn armed <cap="
                    + SPAWN_CAP + "> killAt=" + SPAWN_KILL_TICK
                    + " (SPAWN=1)");
        }
        worldTicks++;
        spawnTick();
    }

    private void spawnFail(String what) {
        spawnFailed = true;
        System.out.println("[MatouAutoplay] FAIL spawn-proof : " + what);
    }

    /**
     * Spawn proof tick: count the bridge-landed pigs (cap bound owned by
     * the bridge veto -- past cap fails here), kill the first pig past
     * the kill tick through the loot seam, poll the carrier at the kill
     * spot. The companion never spawns: every pig here was decided by
     * the pure SpawnJob and landed by the bridge sink. T1 vanilla scope:
     * the census IS pigs (the species narrows with custom-entity
     * registration).
     */
    private void spawnTick() {
        List<PigEntity> found = world.getEntitiesWithinAABB(
                PigEntity.class, CENSUS_BOX, (PigEntity p) -> true);
        int pigs = 0;
        PigEntity first = null;
        for (PigEntity pig : found) {
            // Owner discipline (hub decisions/LOOT.md): inherited vanilla
            // members go through the declaring stub type, never the pig.
            // Dead pigs linger in the loaded set (measured on 1710: a
            // corpse counted past cap at worldTick 51) -- the census
            // counts the living only, like the bridge release on the
            // kill hook.
            Entity body = pig;
            if (body.removed) {
                continue;
            }
            pigs++;
            if (first == null) {
                first = pig;
            }
        }
        if (pigs > maxPigs) {
            maxPigs = pigs;
            System.out.println("[MatouAutoplay] spawn census <" + pigs
                    + "> at worldTick " + worldTicks);
        }
        if (!pigSeen && pigs > 0) {
            pigSeen = true;
            firstPigTick = worldTicks;
            System.out.println("[MatouAutoplay] spawn first pig at "
                    + "worldTick " + firstPigTick);
        }
        if (!hpSeen && first != null) {
            // Owner discipline (hub decisions/LOOT.md): inherited vanilla
            // members go through the declaring stub type, never the pig.
            LivingEntity living = first;
            float hp = living.getMaxHealth();
            if (hp != SPAWN_HP) {
                spawnFail("hp diverged <want=" + SPAWN_HP + " got=" + hp
                        + "> at worldTick " + worldTicks);
                return;
            }
            hpSeen = true;
            System.out.println("[MatouAutoplay] spawn hp <" + hp
                    + "> at worldTick " + worldTicks);
        }
        if (pigs > SPAWN_CAP) {
            spawnFail("cap breached <" + pigs + " > " + SPAWN_CAP
                    + "> at worldTick " + worldTicks);
            return;
        }
        if (!pigKilled && pigSeen && first != null
                && worldTicks >= SPAWN_KILL_TICK) {
            spawnKill(first);
        }
        if (pigKilled && !carrierDropped) {
            spawnPoll();
        }
        if (!pigSeen && worldTicks > SPAWN_KILL_TICK + SPAWN_TIMEOUT) {
            spawnFail("timeout (no bridge pig " + SPAWN_TIMEOUT
                    + " ticks after kill tick " + SPAWN_KILL_TICK + ")");
        } else if (pigKilled && !carrierDropped
                && worldTicks > killTick + SPAWN_TIMEOUT) {
            spawnFail("timeout (no carrier " + SPAWN_TIMEOUT
                    + " ticks after kill at worldTick " + killTick + ")");
        }
    }

    private void spawnKill(PigEntity pig) {
        // Owner discipline (hub decisions/LOOT.md): inherited vanilla
        // members go through the declaring stub type, never the pig.
        Entity body = pig;
        killX = (int) Math.floor(body.getPosX());
        killY = (int) Math.floor(body.getPosY());
        killZ = (int) Math.floor(body.getPosZ());
        MinecraftForge.EVENT_BUS.post(new LivingDropsEvent(pig, null,
                new ArrayList<ItemEntity>(), 0, true));
        body.remove();
        pigKilled = true;
        killTick = worldTicks;
        System.out.println("[MatouAutoplay] spawn pig killed <"
                + killX + "," + killY + "," + killZ + ":pig> at "
                + "worldTick " + killTick);
    }

    private void spawnPoll() {
        AxisAlignedBB box = new AxisAlignedBB(killX - 2.5, killY - 2.5,
                killZ - 2.5, killX + 3.5, killY + 3.5, killZ + 3.5);
        List<ItemEntity> found = world.getEntitiesWithinAABB(
                ItemEntity.class, box, (ItemEntity e) -> true);
        for (ItemEntity item : found) {
            ItemStack stack = item.getItem();
            if (stack == null || stack.getItem() != Items.DIAMOND) {
                continue;
            }
            Entity body = item;
            if (near(body, killX, killY, killZ)) {
                carrierDropped = true;
                carrierTick = worldTicks;
                System.out.println("[MatouAutoplay] spawn pig dropped "
                        + "<diamond> at worldTick " + carrierTick
                        + " (elapsed " + (carrierTick - killTick)
                        + ", want immediate)");
                return;
            }
        }
    }

    private static boolean near(Entity e, int x, int y, int z) {
        return Math.abs(e.getPosX() - (x + 0.5)) < 3.0
                && Math.abs(e.getPosY() - (y + 0.5)) < 3.0
                && Math.abs(e.getPosZ() - (z + 0.5)) < 3.0;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!joined) {
            joined = true;
            mc.loadWorld(WORLD);
            return;
        }
        ticks++;
        if (SPAWN && spawnFailed && !done) {
            done = true;
            System.out.println("[MatouAutoplay] spawn FAILED, shutting down");
            mc.shutdown();
            return;
        }
        if (ticks >= WAIT_TICKS
                && (!SPAWN || (pigSeen && carrierDropped))
                && !done) {
            done = true;
            mc.shutdown();
        }
    }
}
