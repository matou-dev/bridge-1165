package net.minecraft.entity;

import net.minecraft.util.math.vector.Vector3d;
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

    /**
     * Renderer shape (hub decisions/GL_INSTANCING_ADAPTER.md): frame
     * interpolation rides the previous-tick origin plus the current
     * getters (1.16.5 keeps no {@code posX} fields — the getters are
     * already pinned, these fields were measured the same way):
     * {@code prevPosX/Y/Z} are {@code field_70169_q/70167_r/70166_s},
     * {@code rotationYaw/Pitch} are {@code field_70177_z/70125_A} (all
     * public, snapshot 20210309). Pinned by tools/run-live.sh (narrow
     * map) — drift fails loudly.
     */
    public double prevPosX;
    public double prevPosY;
    public double prevPosZ;
    public float rotationYaw;
    public float rotationPitch;
    /**
     * Animation compile stub: 36.2.42 {@code Entity} declares the age
     * counter the skinned renderer and posed hitboxes read for the clip
     * clock ({@code ticksExisted} is {@code field_70173_aa I}, measured
     * against the pinned notch server jar like every other Entity row
     * in {@code tools/live/want.tsv}). Never runs.
     */
    public int ticksExisted;

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

    /**
     * Combat compile stub: 36.2.42 {@code Entity} declares the attacker
     * eye/look surface the bridge combat hook reads through this
     * declaring type (owner discipline, hub decisions/LOOT.md) —
     * {@code getLookVec} is {@code func_70040_Z
     * ()->Vector3d} and {@code getEyeHeight} is {@code func_70047_e
     * ()F}, both measured via javap against the pinned notch server jar
     * (obf owners {@code aqa/bh} and {@code aqa/ce}). Never runs.
     */
    public Vector3d getLookVec() {
        return null;
    }

    public float getEyeHeight() {
        return 0.0f;
    }
}
