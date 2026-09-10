package net.minecraft.entity.ai.attributes;

/**
 * Spawn compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). The hp
 * seam lands the spec value through {@code setBaseValue} on the instance
 * {@code LivingEntity.getAttribute} returns (measured via joined.tsrg +
 * snapshot 20210309 + javap: {@code func_111128_a}, public
 * {@code (D)V} — the instance type is this concrete class on 1.16.5, the
 * 1.12 {@code IAttributeInstance} name does not port). Pinned by
 * tools/run-live.sh (narrow map) — drift fails loudly.
 */
public class ModifiableAttributeInstance {
    public void setBaseValue(double value) {
    }
}
