package net.minecraftforge.fml.client.registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

/**
 * Custom entity compile stub: shape-only Forge 1.16.5-36.2.42 API
 * (universal jar, never obfuscated). Never runs (compile classpath
 * only). Measured with javap against the provisioned universal jar
 * (static {@code registerEntityRenderingHandler(EntityType,
 * IRenderFactory)} filling the map {@code loadEntityRenderers} drains
 * later — a setup-time put has no timing constraint past before-load).
 * Pinned by tools/run-live.sh — drift fails loudly.
 */
public class RenderingRegistry {
    public static <T extends Entity> void registerEntityRenderingHandler(
            EntityType<T> type, IRenderFactory<? super T> factory) {
    }
}
