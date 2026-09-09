package net.minecraftforge.event;

import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.LogicalSide;

/**
 * E1 compile stub: shape-only Forge 1.16.5-36.2.42 API (universal jar,
 * never obfuscated). Never runs (compile classpath only). Members
 * {@code side}, {@code phase}, {@code Phase.END} and
 * {@code WorldTickEvent.world} are pinned by tools/run-live.sh (E3) —
 * drift fails loudly. ClientTickEvent (no world field) serves the
 * dev-only autoplay companion (tools/autoplay/, never shipped).
 */
public class TickEvent extends Event {
    public final LogicalSide side = null;
    public final Phase phase = null;

    public enum Phase {
        START, END
    }

    public static class WorldTickEvent extends TickEvent {
        public final World world = null;
    }

    public static class ClientTickEvent extends TickEvent {
    }
}
