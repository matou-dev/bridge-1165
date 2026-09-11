package net.minecraftforge.event.entity.living;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;

/**
 * Combat compile stub: 36.2.42 {@code LivingHurtEvent} carries the hurt
 * entity on the {@code LivingEvent} base behind {@code getEntityLiving},
 * the damage source and amount behind getters, the amount behind a
 * setter (all measured via javap against the pinned 36.2.42 universal —
 * ctor {@code (LivingEntity, DamageSource, float)}, Forge classes are
 * never obfuscated, presence is the pin). The bridge combat hook
 * resolves the struck bone through it; the autoplay combat leg drives it
 * through the genuine attack path, never by post. Never runs.
 */
public class LivingHurtEvent extends LivingEvent {
    public LivingHurtEvent(LivingEntity entity, DamageSource source,
            float amount) {
        super(entity);
    }

    public DamageSource getSource() {
        return null;
    }

    public float getAmount() {
        return 0.0f;
    }

    public void setAmount(float amount) {
    }
}
