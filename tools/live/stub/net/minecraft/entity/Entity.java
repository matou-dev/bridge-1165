package net.minecraft.entity;

import net.minecraft.world.World;

/**
 * Loot compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). Declaring
 * type of {@code world}/{@code getPosX/Y/Z} (forge reads inherited
 * vanilla members through this type — hub decisions/LOOT.md owner
 * discipline). MCP names measured, not recalled, against the pinned
 * 36.2.42 bytes (joined.tsrg + snapshot 20210309 + javap shape):
 * {@code world} is {@code field_70170_p} (public {@code World} field),
 * {@code getPosX/Y/Z} are {@code func_226277_ct_/226278_cu_/226281_cx_}
 * (public final {@code ()D} methods — 1.16.5 has no {@code posX} fields
 * left, the 1.12 field shape does not port). Pinned by tools/run-live.sh
 * (narrow map) — drift fails loudly.
 *
 * <p>Spawn shape (hub decisions/SPAWN.md, T1 vanilla host): the census id
 * goes through {@code getEntityId} ({@code func_145782_y}, public
 * {@code ()I} — the 1122 memory-anchored {@code func_82145_z} guess is
 * not repeated, the anchor is measured), the living check through the
 * {@code removed} field ({@code field_70128_L}, the 1.12 {@code isDead}
 * name does not port — measured in snapshot 20210309, non-final so no
 * constant folds into prod bytes), landings position through
 * {@code setPositionAndRotation} ({@code func_70080_a}). Same pin rule.
 */
public class Entity {
    public World world;

    public boolean removed;

    public double getPosX() {
        return 0;
    }

    public double getPosY() {
        return 0;
    }

    public double getPosZ() {
        return 0;
    }

    public int getEntityId() {
        return 0;
    }

    public void setPositionAndRotation(double x, double y, double z,
            float yaw, float pitch) {
    }

    /**
     * Companion shape (spawn proof, DEV ONLY): the simulated kill removes
     * the victim through this ({@code func_70106_y} — the 1.12
     * {@code setDead} name does not port, measured in snapshot 20210309).
     * Pinned by the AUTOPLAY derive in hub tools/run-client.sh (want.txt).
     */
    public void remove() {
    }
}
