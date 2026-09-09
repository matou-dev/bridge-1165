package fr.iamacat.autoplay;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Autoplay companion (DEV ONLY, never ships): drives the scripted client
 * proof without a human at the keyboard. On the first client tick it joins
 * the pre-seeded flat world (run-client.sh preseeds saves/&lt;world&gt;,
 * refusing loudly when absent), then counts loaded ticks near spawn and
 * shuts the game down cleanly. World == pure union is judged afterwards
 * by hub tools/verify-client-save.sh — this mod never places a block, so
 * any foreign block fails loudly there, never here silently.
 *
 * Ticks: WAIT_TICKS overshoots the 4000-tick union on purpose (world load
 * and join cost wall ticks before the first world tick; overshoot only
 * re-lands the same deterministic cells — the union is a fixed point).
 * World name follows AUTOPLAY_WORLD (default matou), matching verify.
 */
@Mod(AutoplayMod.MODID)
public class AutoplayMod {
    public static final String MODID = "matouautoplay";
    static final int WAIT_TICKS = 5200;
    static final String WORLD = System.getenv().getOrDefault("AUTOPLAY_WORLD", "matou");

    int ticks = 0;
    boolean joined = false;
    boolean done = false;

    public AutoplayMod() {
        MinecraftForge.EVENT_BUS.register(this);
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
        if (ticks >= WAIT_TICKS && !done) {
            done = true;
            mc.shutdown();
        }
    }
}
