package net.minecraft.block;

import net.minecraft.block.material.Material;

/**
 * E0 registration stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). Members
 * {@code Properties.create} and {@code hardnessAndResistance} are
 * reobfuscated MCP to SRG at live time (see tools/run-live.sh narrow
 * map) and pinned to the provisioned 1.16.5-36.2.42 jars — drift fails
 * loudly. MCP names measured, not recalled: {@code create} is
 * func_200945_a and single-arg {@code hardnessAndResistance} is
 * func_200943_b (bytecode-verified to delegate to the two-arg form with
 * matching resistance) per the pinned MCP snapshot 20210309. The class
 * is MCP {@code AbstractBlock} (no boolean opacity slot — see
 * {@code MatouBlock}).
 */
public abstract class AbstractBlock {
    protected AbstractBlock(Properties properties) {
    }

    /**
     * Loot declaring holder of {@code getBlock} (measured owner in the
     * pinned joined.tsrg — {@code func_177230_c} lives on this inner
     * class, not on {@code BlockState}, so forge reads through this
     * type and Reobf maps the exact bytecode owner — hub
     * decisions/LOOT.md owner discipline). Pinned by tools/run-live.sh
     * (narrow map) — drift fails loudly.
     */
    public abstract static class AbstractBlockState {
        public Block getBlock() {
            return null;
        }
    }

    public static class Properties {
        private Properties() {
        }

        public static Properties create(Material material) {
            return null;
        }

        public Properties hardnessAndResistance(float hardness) {
            return this;
        }
    }
}
