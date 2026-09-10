package net.minecraftforge.fml.client.registry;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;

/**
 * Custom entity compile stub: shape-only Forge 1.16.5-36.2.42 API
 * (universal jar, never obfuscated). Never runs (compile classpath
 * only). Measured with javap against the provisioned universal jar
 * ({@code createRenderFor(EntityRendererManager)} returning
 * {@code EntityRenderer<? super T>}). Pinned by tools/run-live.sh —
 * drift fails loudly.
 */
public interface IRenderFactory<T extends Entity> {
    EntityRenderer<? super T> createRenderFor(
            EntityRendererManager manager);
}
