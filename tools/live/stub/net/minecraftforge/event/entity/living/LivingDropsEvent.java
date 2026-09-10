package net.minecraftforge.event.entity.living;

import java.util.Collection;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.DamageSource;

/**
 * Loot compile stub: 36.2.42 {@code LivingDropsEvent} ctor is
 * {@code (LivingEntity, DamageSource, Collection<ItemEntity>, int,
 * boolean)} (measured via javap against the pinned 36.2.42 universal —
 * the 1.7.10 6-arg shape with specialDropValue does not port). Never
 * runs (Forge constructs the event; only the type plus
 * {@code getEntityLiving} are read by forge/).
 */
public class LivingDropsEvent extends LivingEvent {
    public LivingDropsEvent(LivingEntity entity, DamageSource source,
            Collection<ItemEntity> drops, int lootingLevel,
            boolean recentlyHit) {
        super(entity);
    }
}
