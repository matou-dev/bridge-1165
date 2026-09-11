package net.minecraft.client.world;

import net.minecraft.entity.Entity;

/**
 * Shape-only compile stub for the 1.16.5 client world.
 * Never runs (compile classpath only). Declaring type of
 * {@code getAllEntities} (forge reads it through this type — hub
 * decisions/LOOT.md owner discipline). MCP name measured, not recalled,
 * against the pinned 36.2.42 bytes (joined.tsrg + snapshot 20210309 +
 * javap shape): {@code getAllEntities} is {@code func_217416_b}
 * (public {@code ()Ljava/util/List}, an {@code Iterable<Entity>} — the
 * 1.13+ shape, the 1.12 {@code loadedEntityList} field is gone; the
 * same-sounding {@code func_217369_A} is players-only and is not this).
 * Pinned by tools/run-live.sh (narrow map) — drift fails loudly.
 */
public class ClientWorld {
    public Iterable<Entity> getAllEntities() {
        return null;
    }
}
