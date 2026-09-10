package net.minecraft.entity;

import net.minecraft.entity.passive.PigEntity;

/**
 * Spawn compile stub (DEV-adjacent): shape-only 1.16.5 (MCP) vanilla API
 * used by {@code forge/} sources. Never runs (compile classpath only).
 * Only a type token: entity-type refs (the T1 pig type) go through this
 * (measured in the pinned joined.tsrg — {@code PIG} is
 * {@code field_200784_X} per snapshot 20210309). Non-final by design (a
 * final object would still be safe, but the Items.DIAMOND precedent keeps
 * registry refs non-final — hub decisions/REPOP_SPIKE.md no-stub-const
 * lesson). Pinned by tools/run-live.sh (narrow map) — drift fails loudly.
 */
public class EntityType<T extends Entity> {
    public static EntityType<PigEntity> PIG;
}
