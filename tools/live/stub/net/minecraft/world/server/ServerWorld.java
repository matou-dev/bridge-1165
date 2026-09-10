package net.minecraft.world.server;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * Loot compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). The
 * carrier sink is {@code addEntity} here — measured via joined.tsrg +
 * javap against the pinned 36.2.42 bytes ({@code func_217376_c},
 * {@code (Entity)Z}, public), never assumed: the same-SRG method the
 * 1.12 sink descends from ({@code func_72838_d}, there
 * {@code World.spawnEntity}) now lives on this class while its old
 * name survives only as the private {@code addEntity0} ({@code func_72838_d}
 * is private on 1.16.5 — calling it would die linking, so the public
 * {@code addEntity} is the sink and the live proof owns the semantics).
 * Pinned by tools/run-live.sh (narrow map) — drift fails loudly.
 */
public class ServerWorld extends World {
    public boolean addEntity(Entity entity) {
        return false;
    }
}
