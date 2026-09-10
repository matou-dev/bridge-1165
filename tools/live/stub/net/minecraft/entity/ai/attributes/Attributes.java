package net.minecraft.entity.ai.attributes;

/**
 * Spawn compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). The hp
 * seam addresses {@code MAX_HEALTH} through this (measured in snapshot
 * 20210309: {@code field_233818_a_} — the 1.12
 * {@code SharedMonsterAttributes} holder does not port on 1.16.5).
 * Non-final by design (registry-style ref, Items.DIAMOND precedent —
 * hub decisions/REPOP_SPIKE.md no-stub-const lesson). Pinned by
 * tools/run-live.sh (narrow map) — drift fails loudly.
 */
public class Attributes {
    public static Attribute MAX_HEALTH;
}
