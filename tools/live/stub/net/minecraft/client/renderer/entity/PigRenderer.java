package net.minecraft.client.renderer.entity;

import net.minecraft.entity.passive.PigEntity;

/**
 * Custom entity compile stub: shape-only 1.16.5 (MCP) vanilla API used
 * by {@code forge/} sources. Never runs (compile classpath only). The
 * single {@code (EntityRendererManager)} ctor is measured via javap on
 * the pinned notch client jar (notch {@code egd} declares exactly one
 * {@code (eet)} ctor, eet the renderer manager per joined.tsrg) — the
 * 1.7.10 multi-arg saddle ctor does not exist here, the saddle rides a
 * layer. What the client runtime wants this named is measured at live
 * time (server Reobf leaves client refs alone); the method ref is the
 * only honest path. A dedicated server never loads this (the referencing
 * method is client-stripped, hub decisions/SPAWN.md).
 */
public class PigRenderer extends EntityRenderer<PigEntity> {
    public PigRenderer(EntityRendererManager manager) {
    }
}
