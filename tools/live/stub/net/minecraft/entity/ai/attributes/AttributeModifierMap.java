package net.minecraft.entity.ai.attributes;

/**
 * Custom-entity compile stub: shape-only 1.16.5 (MCP) vanilla API used
 * by {@code forge/} sources. Never runs (compile classpath only). The
 * beast reuses the vanilla pig map wholesale
 * ({@code PigEntity.func_234215_eI_().create()} — pig-identical
 * attributes, never hand-copied values), so only the finishing
 * {@code create} call is MCP-named here (measured in snapshot
 * 20210309: {@code func_233813_a_} — the pig builder itself ships with
 * no MCP name in this snapshot and stays SRG-direct, passthrough, no
 * narrow row). Pinned by tools/run-live.sh (narrow map) — drift fails
 * loudly.
 */
public class AttributeModifierMap {
    public static class MutableAttribute {
        public AttributeModifierMap create() {
            return null;
        }
    }
}
