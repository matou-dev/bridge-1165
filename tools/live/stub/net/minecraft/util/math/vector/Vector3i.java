package net.minecraft.util.math.vector;

/**
 * Loot compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). Declaring
 * type of {@code getX/getY/getZ} (measured owner in the pinned
 * joined.tsrg — {@code func_177958_n/177956_o/177952_p} live on this
 * class, and {@code BlockPos} extends it, so forge reads coords through
 * this type, never through {@code BlockPos}, and Reobf maps the exact
 * bytecode owner — hub decisions/LOOT.md owner discipline). There is no
 * {@code Vec3i} on 1.16.5 (measured absent from the tsrg — the 1.12
 * spelling does not port). Pinned by tools/run-live.sh (narrow map) —
 * drift fails loudly.
 */
public class Vector3i {
    public int getX() {
        return 0;
    }

    public int getY() {
        return 0;
    }

    public int getZ() {
        return 0;
    }
}
