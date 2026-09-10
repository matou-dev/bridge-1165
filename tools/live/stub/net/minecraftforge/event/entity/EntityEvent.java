package net.minecraftforge.event.entity;

import net.minecraft.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

/**
 * Loot compile stub: 36.2.42 {@code EntityEvent} exposes the entity
 * through {@code getEntity} (measured via javap against the pinned
 * 36.2.42 universal — private final field, no public field). Base of the
 * living events the loot hook reads. Never runs.
 */
public class EntityEvent extends Event {
    private final Entity entity;

    public EntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
