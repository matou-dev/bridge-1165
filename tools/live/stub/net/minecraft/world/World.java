package net.minecraft.world;

import net.minecraft.block.BlockState;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;

/**
 * E1 compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). Members
 * {@code OVERWORLD}, {@code getDimensionKey} and {@code setBlockState}
 * are reobfuscated MCP to SRG at E3 time (see tools/run-live.sh narrow
 * map) and pinned to the provisioned 1.16.5-36.2.42 jars — drift fails
 * loudly.
 */
public class World {
    public static final RegistryKey<World> OVERWORLD = null;

    public RegistryKey<World> getDimensionKey() {
        return null;
    }

    public boolean setBlockState(BlockPos pos, BlockState state) {
        return false;
    }
}
