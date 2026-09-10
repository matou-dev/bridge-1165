package net.minecraft.util.math;

import net.minecraft.util.math.vector.Vector3i;

/**
 * E1 compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). Pinned
 * to the provisioned 1.16.5-36.2.42 jars by tools/run-live.sh (E3) —
 * drift fails loudly. Loot: extends {@link Vector3i} (measured in the
 * pinned joined.tsrg — coords read through the declaring type, hub
 * decisions/LOOT.md owner discipline).
 */
public class BlockPos extends Vector3i {
    public final int x;
    public final int y;
    public final int z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
