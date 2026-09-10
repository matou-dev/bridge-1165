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
 */
public class Entity {
    public World world;

    public double getPosX() {
        return 0;
    }

    public double getPosY() {
        return 0;
    }

    public double getPosZ() {
        return 0;
    }
}
