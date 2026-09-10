package net.minecraft.world;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;

/**
 * E1 compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). Members
 * {@code OVERWORLD}, {@code getDimensionKey} and {@code setBlockState}
 * are reobfuscated MCP to SRG at E3 time (see tools/run-live.sh narrow
 * map) and pinned to the provisioned 1.16.5-36.2.42 jars — drift fails
 * loudly. Loot adds {@code isRemote} (same rule): MCP name measured, not
 * recalled ({@code isRemote} is {@code field_72995_K}, a public boolean
 * field per javap on the pinned bytes — the stub keeps it non-final so
 * no constant folds into prod bytes). {@code World} implements
 * {@code IWorld} (measured: the obfuscated super-interface on the pinned
 * bytes is the {@code IWorld} one — the break event hands an
 * {@code IWorld} that the hook narrows back to this).
 */
public class World implements IWorld {
    public static final RegistryKey<World> OVERWORLD = null;

    public boolean isRemote;

    public RegistryKey<World> getDimensionKey() {
        return null;
    }

    public boolean setBlockState(BlockPos pos, BlockState state) {
        return false;
    }
}
