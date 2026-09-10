package net.minecraftforge.event.entity.living;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityEvent;

/**
 * Loot compile stub: 36.2.42 {@code LivingEvent} exposes the killed
 * entity through {@code getEntityLiving} (measured via javap against the
 * pinned 36.2.42 universal — private final {@code LivingEntity} field,
 * no public field; the 1.16.5 name of the 1.12 {@code EntityLivingBase}
 * slot). Never runs.
 */
public class LivingEvent extends EntityEvent {
    public LivingEvent(LivingEntity entity) {
        super(entity);
    }

    public LivingEntity getEntityLiving() {
        return null;
    }
}
