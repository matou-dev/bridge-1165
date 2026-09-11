package net.minecraft.entity.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

/**
 * Loot compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). Only the
 * type is needed (the break-event harvester slot); forge reads nothing
 * through it at this tranche.
 *
 * <p>Combat tranche: the autoplay combat leg drives the genuine vanilla
 * attack path through {@code attackTargetEntityWithCurrentItem}
 * ({@code func_71059_n (Entity)V} declared on {@code PlayerEntity},
 * measured via javap against the pinned notch server jar — obf
 * {@code bfw/f}, not the same-named {@code ServerPlayerEntity} row).
 * Never runs.
 */
public class PlayerEntity extends LivingEntity {
    public void attackTargetEntityWithCurrentItem(Entity target) {
    }
}
