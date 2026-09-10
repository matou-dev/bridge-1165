package net.minecraft.entity.passive;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

/**
 * Spawn compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). The T1
 * victim/host is a vanilla pig until custom-entity registration lands
 * (hub decisions/SPAWN.md): the bridge lands one through this ctor
 * (measured via javap on the pinned notch bytes — public
 * {@code (EntityType,World)}, the 1.12 no-arg shape does not port; the
 * type arrives through {@code EntityType.PIG}). A {@code LivingEntity}
 * for the hp seam and the kill post.
 */
public class PigEntity extends LivingEntity {
    public PigEntity(EntityType<? extends PigEntity> type, World world) {
    }
}
