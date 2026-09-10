package net.minecraftforge.event.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraftforge.eventbus.api.Event;

/**
 * Custom-entity compile stub: shape-only Forge 1.16.5-36.2.42 API
 * (universal jar, never obfuscated). Never runs (compile classpath
 * only). The beast's attribute map registers here (measured via javap
 * against the pinned 36.2.42 universal: mod-bus event — the real class
 * also implements {@code IModBusEvent} — single call
 * {@code put(EntityType, AttributeModifierMap)}). Without this the
 * vanilla {@code LivingEntity} ctor NPEs on the first landing (fresh
 * types carry no attribute map — measured live, never assumed). Only
 * the member forge reads is stubbed. Pinned by tools/run-live.sh —
 * drift fails loudly.
 */
public class EntityAttributeCreationEvent extends Event {
    public void put(EntityType<? extends LivingEntity> type,
            AttributeModifierMap map) {
    }
}
