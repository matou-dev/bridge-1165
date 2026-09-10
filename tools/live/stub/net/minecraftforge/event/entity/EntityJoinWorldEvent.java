package net.minecraftforge.event.entity;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * Spawn compile stub: shape-only Forge 1.16.5-36.2.42 API (universal jar,
 * never obfuscated). Never runs (compile classpath only). The joined
 * entity lives on the {@code EntityEvent} base behind {@code getEntity},
 * the world on this subclass behind {@code getWorld} (measured via javap
 * against the pinned 36.2.42 universal — the 1.7.10 public-field shape
 * does not port). The event is {@code @Cancelable} (same measurement) —
 * the past-cap veto cancels here. Only the members forge reads are
 * stubbed. Pinned by tools/run-live.sh — drift fails loudly.
 */
public class EntityJoinWorldEvent extends EntityEvent {
    public EntityJoinWorldEvent(Entity entity, World world) {
        super(entity);
    }

    public World getWorld() {
        return null;
    }
}
