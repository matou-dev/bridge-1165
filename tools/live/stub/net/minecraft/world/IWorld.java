package net.minecraft.world;

/**
 * Loot compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). Marker
 * only: {@code BlockEvent.getWorld} returns this (measured via javap
 * against the pinned 36.2.42 universal — never assumed), and the forge
 * hook narrows to {@link World} before reading anything (the interface
 * itself carries no dim/remote members to read). Pinned by
 * tools/run-live.sh at live time — drift fails loudly.
 */
public interface IWorld {
}
