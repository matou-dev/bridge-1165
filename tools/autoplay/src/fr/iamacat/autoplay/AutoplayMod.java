package fr.iamacat.autoplay;

import java.util.ArrayList;
import java.util.List;
import fr.iamacat.bridge.forge.Example1Mod;
import fr.iamacat.bridge.forge.MatouEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

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
   * <p>Spike proof (SPIKE=1, DEV ONLY): at SPIKE_MINE_TICK server-world
  * ticks the companion places one stone at isolated coords outside the
  * union slices (20,10,8 — the verdict reads y=63..65 only and the vein
  * band is y=60..61, so the spike cell can never pollute world == pure
  * union; the loot legs already mine (8,10,8) and kill at (12,10,8), so
  * the spike takes another x at the same neutral y, inside the loaded
  * chunks), clears it, and posts that harvest as a {@code BreakEvent}
  * authored by the joined player — then polls the cell back to stone.
  * The harvest is simulated, honestly: placing + clearing plus a bus post
  * exercises the shipped hook ({@code onBreak} reads the IWorld/block
  * through the 36.2.42 getters) through the live seal
  * ({@code RepopSeal}), the pure {@code RepopJob} and the live sink —
  * that seam is what the spike owns. The post carries the real joined
  * player (the BreakEvent constructor itself reads it — null NPEs,
  * measured on the lead bridge); what is NOT re-proven is vanilla firing
  * the event on a genuine player harvest (Forge-owned, shape-pinned in
  * run-live.sh). A repop observed before the delay, or never, fails
  * loudly (spike FAILED) and shuts the game down for post-mortem — the
  * save keeps the air hole, the verifier and the y=10 anvil spot-check
  * refuse it. Without SPIKE=1 nothing here runs and the proof is
  * byte-for-byte the proven union run. 1.16.5 spelling is the loot
  * spelling (same WANT rows, no new members): stone resolves through
  * {@code ForgeRegistries.BLOCKS}, air probes through
  * {@code World.getBlockState} + {@code BlockStateBase.isAir}, place
  * through {@code World.setBlockState}, clear through
  * {@code World.removeBlock}, the player through
  * {@code ServerWorld.getPlayers}.
  *
  * <p>Loot proof (LOOT=1, DEV ONLY): at LOOT_HARVEST_TICK server-world
  * ticks the companion harvests the registered ore at an isolated coords
  * outside the union slices (8,10,8 — place + clear + a
  * {@code BreakEvent} post authored by the joined player, spike honesty
   * standard) and, five ticks later, kills the spawned registered beast at
  * (12,10,8) with a simulated {@code LivingDropsEvent} post
  * (single-table scope — hub decisions/LOOT.md) — then polls both spots for the
  * diamond carrier the bridge loot sink spawns per due drop. The posts
  * are simulated, honestly: place + clear + bus posts exercise the
  * shipped hooks ({@code onHarvest} reads the IWorld/block through the
  * 36.2.42 getters; {@code onKill} reads the entity only) through the
  * live seal ({@code LootSeal}), the pure {@code LootJob} and the live
  * sink — that seam is what loot owns. What is NOT re-proven is vanilla
  * firing the events on a genuine harvest/kill (Forge-owned, shape-pinned
  * in run-live.sh). A carrier observed late, or never, fails loudly
  * (loot FAILED) and shuts the game down for post-mortem. Without LOOT=1
  * nothing here runs and the proof is byte-for-byte the proven union run.
  *
  * <p>1.16.5 native spelling (measured against the pinned 36.2.42 bytes,
  * never ported blind from 1122): the ore resolves through
  * {@code ForgeRegistries.BLOCKS} (presence first — {@code getValue}
  * returns air for unknown names, never null); the state comes from
  * {@code Block.getDefaultState}; air probes go through
  * {@code World.getBlockState} + {@code BlockStateBase.isAir} (no
  * {@code isAirBlock} field shape); place goes through
  * {@code World.setBlockState}, clear through {@code World.removeBlock}
  * (the 1.12 {@code setBlockToAir} shape does not port); the harvest
  * post is a {@code world}-package {@code BreakEvent} authored by the
  * joined player from {@code ServerWorld.getPlayers} (no
  * {@code playerEntities} field ships on 1.16.5); the victim is a
  * {@code new MatouEntity(Example1Mod.beastType(), world)} (the T1
  * {@code new PigEntity(EntityType.PIG, world)} shape is retired with
  * the species — the type rides the bridge's registered beast entry); the spawn lands through {@code ServerWorld.addEntity} (the
  * loot sink, never the private {@code addEntity0}); removal goes
  * through {@code Entity.remove} (the 1.12 {@code setDead} name does
  * not port); carriers poll through {@code getEntitiesWithinAABB} over
  * one box per spot (the 1.12 {@code loadedEntityList} field shape does
  * not port).
  *
  * <p>Spawn proof (SPAWN=1, DEV ONLY): the bridge itself lands budgeted
  * beasts (SPAWN=1 also arms {@code MatouBridgeMod.spawnTick} -- one flag
  * drives both sides, so union runs stay spawn-free). The companion never
  * spawns here: it polls the loaded beasts the bridge landed up to cap,
  * records the maximum seen (past cap fails loudly -- the veto owns that
  * bound), kills the first beast past SPAWN_KILL_TICK with a simulated
  * {@code LivingDropsEvent} post (loot honesty standard -- the kill pays
  * through the loot table, proving the spawn-to-loot chain), then polls
  * the diamond carrier at the kill spot. The first living beast's max
  * health is polled once against SPAWN_HP (hp tranche -- the bridge
  * applies the content hp per landing; a diverged read-back fails loudly
  * here too). Custom-entity scope: the census IS the registered beast
  * (vanilla pigs are a different species — counting one would breach a
  * cap that is not its own). A missing beast, a breached cap, or a
  * missing carrier fails loudly (spawn FAILED) and shuts the game down
  * for post-mortem. Without SPAWN=1 nothing here runs and the proof is
  * byte-for-byte the proven union run.
  *
  * <p>Load order (measured on the lead bridge, hub decisions/SPAWN.md):
  * this companion frame-references the bridge's {@code MatouEntity}
  * ({@code new}/{@code instanceof}), so it loads after the bridge
  * (ordering AFTER on matoubridge in autoplay-mods.toml — the 1.16.5
  * shape of the lead's {@code required-after:matoubridge}): without it
  * the companion can construct before the bridge jar is sourced and die
  * on the verifier load. Load-bearing: removing it re-arms the crash.
  * Unproven on 36.2.42 until the live tranche (kept by construction:
  * the bridge is always present in our runs).
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
  * {@code LivingEntity}), never the beast.
  */
@Mod(AutoplayMod.MODID)
public class AutoplayMod {
    public static final String MODID = "matouautoplay";
    static final int WAIT_TICKS = 5200;
    static final String WORLD = System.getenv().getOrDefault("AUTOPLAY_WORLD", "matou");
    static final boolean SPIKE = "1".equals(System.getenv("SPIKE"));
    static final int SPIKE_X = 20;
    static final int SPIKE_Y = 10;
    static final int SPIKE_Z = 8;
    static final String SPIKE_BLOCK = "minecraft:stone";
    static final int SPIKE_MINE_TICK = 1000;
    static final int SPIKE_TIMEOUT = 600;
    static final boolean LOOT = "1".equals(System.getenv("LOOT"));
    static final int LOOT_ORE_X = 8;
    static final int LOOT_ORE_Y = 10;
    static final int LOOT_ORE_Z = 8;
    static final String LOOT_ORE_BLOCK = "example1:my_ore";
    static final int LOOT_BEAST_X = 12;
    static final int LOOT_BEAST_Y = 10;
    static final int LOOT_BEAST_Z = 8;
    static final int LOOT_HARVEST_TICK = 1000;
    static final int LOOT_BEAST_DELAY = 5;
    static final int LOOT_TIMEOUT = 600;
    static final boolean SPAWN = "1".equals(System.getenv("SPAWN"));
    /** Mirrors the effective cap (content {@code owned.matou mob my_beast
     * cap} default, operator {@code spawn.cap} wins -- transported by the
     * bridge spawn wire): the companion counts beasts, the effective
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
    volatile boolean mined = false;
    volatile boolean repopped = false;
    volatile boolean spikeFailed = false;
    volatile int mineTick = -1;
    volatile int repopTick = -1;
    volatile int lootOreTick = -1;
    volatile int lootBeastTick = -1;
    volatile boolean oreDropped = false;
    volatile boolean beastDropped = false;
    volatile boolean lootFailed = false;
    volatile int oreDropTick = -1;
    volatile int beastDropTick = -1;
    volatile boolean beastSeen = false;
    volatile boolean hpSeen = false;
    volatile int maxBeasts = 0;
    volatile int firstBeastTick = -1;
    volatile boolean beastKilled = false;
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
    boolean playerNoted = false;
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
        if (!SPIKE && !LOOT && !SPAWN) {
            return;
        }
        if (!World.OVERWORLD.equals(event.world.getDimensionKey())) {
            if (!foreignNoted) {
                foreignNoted = true;
                System.out.println("[MatouAutoplay] note loot-proof : "
                        + "ignoring non-overworld world ticks (the "
                        + "integrated server ticks every dim from boot)");
            }
            return;
        }
        if (world == null) {
            world = event.world;
            if (SPIKE) {
                System.out.println("[MatouAutoplay] spike armed <"
                        + SPIKE_X + "," + SPIKE_Y + ","
                        + SPIKE_Z + ":" + SPIKE_BLOCK + "> mineAt="
                        + SPIKE_MINE_TICK + " (SPIKE=1)");
            }
            if (LOOT) {
                System.out.println("[MatouAutoplay] loot armed <ore "
                        + LOOT_ORE_X + "," + LOOT_ORE_Y + ","
                        + LOOT_ORE_Z + ":" + LOOT_ORE_BLOCK + " + beast "
                        + LOOT_BEAST_X + "," + LOOT_BEAST_Y + ","
                        + LOOT_BEAST_Z + "> harvestAt="
                        + LOOT_HARVEST_TICK + " (LOOT=1)");
            }
            if (SPAWN) {
                System.out.println("[MatouAutoplay] spawn armed <cap="
                        + SPAWN_CAP + "> killAt=" + SPAWN_KILL_TICK
                        + " (SPAWN=1)");
            }
        }
        worldTicks++;
        if (SPIKE) {
            if (!mined && !spikeFailed && worldTicks >= SPIKE_MINE_TICK) {
                mine();
            } else if (mined && !repopped && !spikeFailed) {
                poll();
            }
        }
        if (LOOT && !lootFailed) {
            lootTick();
        }
        if (SPAWN && !spawnFailed) {
            spawnTick();
        }
    }

    private void spikeFail(String what) {
        spikeFailed = true;
        System.out.println("[MatouAutoplay] FAIL spike-proof : " + what);
    }

    /**
     * Joined player or null (postponed, loudly once): the simulated spike
     * harvest is authored by the joined player — the break post carries
     * it like the loot harvest post, and an authorless harvest proves
     * nothing. Checked before touching the world. Same 1.16.5 shape as
     * {@link #lootPlayer} (no {@code playerEntities} field ships, so the
     * list comes from {@code ServerWorld.getPlayers}).
     */
    private PlayerEntity spikePlayer() {
        if (!(world instanceof ServerWorld)) {
            if (!playerNoted) {
                playerNoted = true;
                System.out.println("[MatouAutoplay] note spike-proof : "
                        + "non-server world at mine tick, postponing");
            }
            return null;
        }
        List<? extends PlayerEntity> players =
                ((ServerWorld) world).getPlayers();
        if (players == null || players.isEmpty()) {
            if (!playerNoted) {
                playerNoted = true;
                System.out.println("[MatouAutoplay] note spike-proof : "
                        + "player absent at mine tick, postponing");
            }
            return null;
        }
        return players.get(0);
    }

    private void mine() {
        // The BreakEvent constructor reads the player (measured NPE on
        // null on the lead bridge), so the harvest is authored by the
        // joined player, not forged from null. Absent player (not joined
        // yet) postpones the mine, loudly once; a player that never shows
        // fails the proof instead of mining authorless. Checked before
        // touching the world: a postponed mine leaves no hole behind.
        PlayerEntity player = spikePlayer();
        if (player == null) {
            if (worldTicks > SPIKE_MINE_TICK + SPIKE_TIMEOUT) {
                spikeFail("player never joined (no harvest author)");
            }
            return;
        }
        if (!ForgeRegistries.BLOCKS.containsKey(
                new ResourceLocation(SPIKE_BLOCK))) {
            spikeFail("unknown <" + SPIKE_BLOCK + "> (want vanilla stone)");
            return;
        }
        Block stone = ForgeRegistries.BLOCKS.getValue(
                new ResourceLocation(SPIKE_BLOCK));
        if (stone == null) {
            spikeFail("unknown <" + SPIKE_BLOCK + "> (want vanilla stone)");
            return;
        }
        BlockState stoneState = stone.getDefaultState();
        BlockPos at = new BlockPos(SPIKE_X, SPIKE_Y, SPIKE_Z);
        // Owner discipline (hub decisions/LOOT.md): isAir lives on the
        // declaring AbstractBlockState type, never on BlockState — the
        // hierarchy walk only maps the exact bytecode owner, so the
        // read goes through an upcast local (same upcast as lootOre).
        AbstractBlock.AbstractBlockState before =
                world.getBlockState(at);
        if (!before.isAir()) {
            spikeFail("spike cell occupied before place (want air, "
                    + "proof needs isolated coords)");
            return;
        }
        if (!world.setBlockState(at, stoneState)) {
            spikeFail("place refused (setBlockState false at worldTick "
                    + worldTicks + ")");
            return;
        }
        AbstractBlock.AbstractBlockState placed =
                world.getBlockState(at);
        if (placed.isAir()) {
            spikeFail("place invisible (still air after setBlockState)");
            return;
        }
        if (!world.removeBlock(at, false)) {
            spikeFail("clear refused (removeBlock false)");
            return;
        }
        AbstractBlock.AbstractBlockState cleared =
                world.getBlockState(at);
        if (!cleared.isAir()) {
            spikeFail("clear invisible (not air after removeBlock)");
            return;
        }
        MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(
                world, at, stoneState, player));
        mined = true;
        mineTick = worldTicks;
        System.out.println("[MatouAutoplay] spike mined <" + SPIKE_X + ","
                + SPIKE_Y + "," + SPIKE_Z + ":" + SPIKE_BLOCK
                + "> at worldTick " + mineTick);
    }

    private void poll() {
        AbstractBlock.AbstractBlockState now =
                world.getBlockState(
                        new BlockPos(SPIKE_X, SPIKE_Y, SPIKE_Z));
        if (!now.isAir()) {
            repopped = true;
            repopTick = worldTicks;
            System.out.println("[MatouAutoplay] spike repopped <" + SPIKE_X
                    + "," + SPIKE_Y + "," + SPIKE_Z + ":" + SPIKE_BLOCK
                    + "> at worldTick " + repopTick + " (elapsed "
                    + (repopTick - mineTick) + ", want >= 200)");
        } else if (worldTicks > mineTick + SPIKE_TIMEOUT) {
            spikeFail("timeout (still air " + SPIKE_TIMEOUT
                    + " ticks after mine at worldTick " + mineTick + ")");
        }
    }

    private void lootFail(String what) {
        lootFailed = true;
        System.out.println("[MatouAutoplay] FAIL loot-proof : " + what);
    }

    private void lootTick() {
        if (lootOreTick < 0 && worldTicks >= LOOT_HARVEST_TICK) {
            lootOre();
        } else if (lootOreTick >= 0 && lootBeastTick < 0
                && worldTicks >= lootOreTick + LOOT_BEAST_DELAY) {
            lootBeast();
        }
        if ((lootOreTick >= 0 && !oreDropped)
                || (lootBeastTick >= 0 && !beastDropped)) {
            lootPoll();
        }
        if (!(oreDropped && beastDropped)
                && worldTicks > LOOT_HARVEST_TICK + LOOT_TIMEOUT) {
            lootFail("timeout (oreDropped=" + oreDropped + " beastDropped="
                    + beastDropped + " " + LOOT_TIMEOUT
                    + " ticks after harvest at worldTick "
                    + LOOT_HARVEST_TICK + ")");
        }
    }

    /**
     * Joined player or null (postponed, loudly once): both simulated
     * events are authored by the joined player — the break post carries
     * it like the spike harvest post, and an authorless kill proves
     * nothing. Checked before touching the world. 1.16.5 shape: no
     * {@code playerEntities} field ships, so the list comes from
     * {@code ServerWorld.getPlayers} (the tick world is always a server
     * world on this path — anything else postpones, never casts blind).
     */
    private PlayerEntity lootPlayer() {
        if (!(world instanceof ServerWorld)) {
            if (!playerNoted) {
                playerNoted = true;
                System.out.println("[MatouAutoplay] note loot-proof : "
                        + "non-server world at harvest tick, postponing");
            }
            return null;
        }
        List<? extends PlayerEntity> players =
                ((ServerWorld) world).getPlayers();
        if (players == null || players.isEmpty()) {
            if (!playerNoted) {
                playerNoted = true;
                System.out.println("[MatouAutoplay] note loot-proof : "
                        + "player absent at harvest tick, postponing");
            }
            return null;
        }
        return players.get(0);
    }

    private void lootOre() {
        PlayerEntity player = lootPlayer();
        if (player == null) {
            return;
        }
        if (!ForgeRegistries.BLOCKS.containsKey(
                new ResourceLocation(LOOT_ORE_BLOCK))) {
            lootFail("unknown <" + LOOT_ORE_BLOCK + "> (want registered ore)");
            return;
        }
        Block ore = ForgeRegistries.BLOCKS.getValue(
                new ResourceLocation(LOOT_ORE_BLOCK));
        if (ore == null) {
            lootFail("unknown <" + LOOT_ORE_BLOCK + "> (want registered ore)");
            return;
        }
        BlockState oreState = ore.getDefaultState();
        BlockPos at = new BlockPos(LOOT_ORE_X, LOOT_ORE_Y, LOOT_ORE_Z);
        // Owner discipline (hub decisions/LOOT.md): isAir lives on the
        // declaring AbstractBlockState type, never on BlockState — the
        // hierarchy walk only maps the exact bytecode owner, so the
        // read goes through an upcast local (same upcast as the 1201
        // Level/isClientSide fix).
        AbstractBlock.AbstractBlockState before =
                world.getBlockState(at);
        if (!before.isAir()) {
            lootFail("loot cell occupied before place (want air)");
            return;
        }
        if (!world.setBlockState(at, oreState)) {
            lootFail("place refused (setBlockState false at worldTick "
                    + worldTicks + ")");
            return;
        }
        AbstractBlock.AbstractBlockState placed =
                world.getBlockState(at);
        if (placed.isAir()) {
            lootFail("place invisible (still air after setBlockState)");
            return;
        }
        if (!world.removeBlock(at, false)) {
            lootFail("clear refused (removeBlock false)");
            return;
        }
        AbstractBlock.AbstractBlockState cleared =
                world.getBlockState(at);
        if (!cleared.isAir()) {
            lootFail("clear invisible (not air after removeBlock)");
            return;
        }
        MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(
                world, at, oreState, player));
        lootOreTick = worldTicks;
        System.out.println("[MatouAutoplay] loot ore harvested <"
                + LOOT_ORE_X + "," + LOOT_ORE_Y + "," + LOOT_ORE_Z + ":"
                + LOOT_ORE_BLOCK + "> at worldTick " + lootOreTick);
    }

    private void lootBeast() {
        if (lootPlayer() == null) {
            return;
        }
        // Custom entity (hub decisions/SPAWN.md): the loot kill lands on
        // the registered beast — every kill pays the single table entry
        // (per-mob filtering stays a re-opener). A wandering vanilla pig
        // would take the scripted kill dishonestly, so the species is
        // exact here, like the bridge census.
        EntityType<MatouEntity> type = Example1Mod.beastType();
        if (type == null) {
            lootFail("no beast type at worldTick " + worldTicks);
            return;
        }
        MatouEntity beast = new MatouEntity(type, world);
        // Owner discipline (hub decisions/LOOT.md): inherited vanilla
        // members go through the declaring stub type, never the beast.
        Entity body = beast;
        body.setPositionAndRotation(LOOT_BEAST_X + 0.5, LOOT_BEAST_Y,
                LOOT_BEAST_Z + 0.5, 0.0f, 0.0f);
        if (!(world instanceof ServerWorld)
                || !((ServerWorld) world).addEntity(beast)) {
            lootFail("beast spawn refused at worldTick " + worldTicks);
            return;
        }
        MinecraftForge.EVENT_BUS.post(new LivingDropsEvent(beast, null,
                new ArrayList<ItemEntity>(), 0, true));
        body.remove();
        lootBeastTick = worldTicks;
        System.out.println("[MatouAutoplay] loot beast killed <"
                + LOOT_BEAST_X + "," + LOOT_BEAST_Y + ","
                + LOOT_BEAST_Z + ":beast> at worldTick "
                + lootBeastTick);
    }

    private void lootPoll() {
        if (!oreDropped) {
            List<ItemEntity> found = world.getEntitiesWithinAABB(
                    ItemEntity.class, box(LOOT_ORE_X, LOOT_ORE_Y,
                            LOOT_ORE_Z),
                    (ItemEntity e) -> true);
            for (ItemEntity item : found) {
                ItemStack stack = item.getItem();
                if (stack == null || stack.getItem() != Items.DIAMOND) {
                    continue;
                }
                if (near(item, LOOT_ORE_X, LOOT_ORE_Y, LOOT_ORE_Z)) {
                    oreDropped = true;
                    oreDropTick = worldTicks;
                    System.out.println("[MatouAutoplay] loot ore dropped "
                            + "<diamond> at worldTick " + oreDropTick
                            + " (elapsed " + (oreDropTick - lootOreTick)
                            + ", want immediate)");
                    break;
                }
            }
        }
        if (!beastDropped && lootBeastTick >= 0) {
            List<ItemEntity> found = world.getEntitiesWithinAABB(
                    ItemEntity.class, box(LOOT_BEAST_X, LOOT_BEAST_Y,
                            LOOT_BEAST_Z),
                    (ItemEntity e) -> true);
            for (ItemEntity item : found) {
                ItemStack stack = item.getItem();
                if (stack == null || stack.getItem() != Items.DIAMOND) {
                    continue;
                }
                if (near(item, LOOT_BEAST_X, LOOT_BEAST_Y, LOOT_BEAST_Z)) {
                    beastDropped = true;
                    beastDropTick = worldTicks;
                    System.out.println("[MatouAutoplay] loot beast dropped "
                            + "<diamond> at worldTick " + beastDropTick
                            + " (elapsed " + (beastDropTick - lootBeastTick)
                            + ", want immediate)");
                    break;
                }
            }
        }
    }

    private static AxisAlignedBB box(int x, int y, int z) {
        return new AxisAlignedBB(x - 2.5, y - 2.5, z - 2.5,
                x + 3.5, y + 3.5, z + 3.5);
    }

    private void spawnFail(String what) {
        spawnFailed = true;
        System.out.println("[MatouAutoplay] FAIL spawn-proof : " + what);
    }

    /**
     * Spawn proof tick: count the bridge-landed beasts (cap bound owned by
     * the bridge veto -- past cap fails here), kill the first beast past
     * the kill tick through the loot seam, poll the carrier at the kill
     * spot. The companion never spawns: every beast here was decided by
     * the pure SpawnJob and landed by the bridge sink. Custom-entity
     * scope: the census IS the registered beast (vanilla pigs are a
     * different species — counting one would breach a cap that is not
     * its own).
     */
    private void spawnTick() {
        List<MatouEntity> found = world.getEntitiesWithinAABB(
                MatouEntity.class, CENSUS_BOX, (MatouEntity m) -> true);
        int beasts = 0;
        MatouEntity first = null;
        for (MatouEntity beast : found) {
            // Owner discipline (hub decisions/LOOT.md): inherited vanilla
            // members go through the declaring stub type, never the beast.
            // Dead beasts linger in the loaded set (measured on 1710: a
            // corpse counted past cap at worldTick 51) -- the census
            // counts the living only, like the bridge release on the
            // kill hook.
            Entity body = beast;
            if (body.removed) {
                continue;
            }
            beasts++;
            if (first == null) {
                first = beast;
            }
        }
        if (beasts > maxBeasts) {
            maxBeasts = beasts;
            System.out.println("[MatouAutoplay] spawn census <" + beasts
                    + "> at worldTick " + worldTicks);
        }
        if (!beastSeen && beasts > 0) {
            beastSeen = true;
            firstBeastTick = worldTicks;
            System.out.println("[MatouAutoplay] spawn first beast at "
                    + "worldTick " + firstBeastTick);
        }
        if (!hpSeen && first != null) {
            // Owner discipline (hub decisions/LOOT.md): inherited vanilla
            // members go through the declaring stub type, never the beast.
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
        if (beasts > SPAWN_CAP) {
            spawnFail("cap breached <" + beasts + " > " + SPAWN_CAP
                    + "> at worldTick " + worldTicks);
            return;
        }
        if (!beastKilled && beastSeen && first != null
                && worldTicks >= SPAWN_KILL_TICK) {
            spawnKill(first);
        }
        if (beastKilled && !carrierDropped) {
            spawnPoll();
        }
        if (!beastSeen && worldTicks > SPAWN_KILL_TICK + SPAWN_TIMEOUT) {
            spawnFail("timeout (no bridge beast " + SPAWN_TIMEOUT
                    + " ticks after kill tick " + SPAWN_KILL_TICK + ")");
        } else if (beastKilled && !carrierDropped
                && worldTicks > killTick + SPAWN_TIMEOUT) {
            spawnFail("timeout (no carrier " + SPAWN_TIMEOUT
                    + " ticks after kill at worldTick " + killTick + ")");
        }
    }

    private void spawnKill(MatouEntity beast) {
        // Owner discipline (hub decisions/LOOT.md): inherited vanilla
        // members go through the declaring stub type, never the beast.
        Entity body = beast;
        killX = (int) Math.floor(body.getPosX());
        killY = (int) Math.floor(body.getPosY());
        killZ = (int) Math.floor(body.getPosZ());
        MinecraftForge.EVENT_BUS.post(new LivingDropsEvent(beast, null,
                new ArrayList<ItemEntity>(), 0, true));
        body.remove();
        beastKilled = true;
        killTick = worldTicks;
        System.out.println("[MatouAutoplay] spawn beast killed <"
                + killX + "," + killY + "," + killZ + ":beast> at "
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
                System.out.println("[MatouAutoplay] spawn beast dropped "
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
        if (SPIKE && spikeFailed && !done) {
            done = true;
            System.out.println("[MatouAutoplay] spike FAILED, shutting down");
            mc.shutdown();
            return;
        }
        if (LOOT && lootFailed && !done) {
            done = true;
            System.out.println("[MatouAutoplay] loot FAILED, shutting down");
            mc.shutdown();
            return;
        }
        if (SPAWN && spawnFailed && !done) {
            done = true;
            System.out.println("[MatouAutoplay] spawn FAILED, shutting down");
            mc.shutdown();
            return;
        }
        if (ticks >= WAIT_TICKS
                && (!SPIKE || repopped)
                && (!LOOT || (oreDropped && beastDropped))
                && (!SPAWN || (beastSeen && carrierDropped))
                && !done) {
            done = true;
            mc.shutdown();
        }
    }
}
