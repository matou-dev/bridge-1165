package net.minecraft.block;

import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * E1 compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). Member
 * {@code getDefaultState} is reobfuscated MCP to SRG at E3 time (see
 * tools/run-live.sh narrow map) and pinned to the provisioned
 * 1.16.5-36.2.42 jars — drift fails loudly. The
 * {@code IForgeRegistryEntry} bound mirrors the real class so registry
 * call sites erase to the runtime descriptor (found live in E3).
 */
public class Block implements IForgeRegistryEntry<Block> {
    public BlockState getDefaultState() {
        return null;
    }
}
