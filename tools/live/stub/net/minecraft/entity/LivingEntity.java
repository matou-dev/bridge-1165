package net.minecraft.entity;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;

/**
 * Loot compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). The
 * 1.16.5 name of the 1.12 {@code EntityLivingBase} (measured in the
 * pinned joined.tsrg — no {@code EntityLivingBase} class ships on
 * 1.16.5); {@code LivingEvent.getEntityLiving} returns this, forge
 * upcasts to {@link Entity} before reading (owner discipline).
 *
 * <p>Spawn shape (hub decisions/SPAWN.md, hp tranche): the content hp
 * lands through {@code getAttribute} ({@code func_110148_a} — the 1.12
 * {@code getEntityAttribute} name does not port) plus
 * {@code setHealth}/{@code getMaxHealth} (same SRG as 1.12,
 * {@code func_70606_j}/{@code func_110138_aP}). Measured in snapshot
 * 20210309 + joined.tsrg + javap, pinned by tools/run-live.sh (narrow
 * map) — drift fails loudly.
 */
public class LivingEntity extends Entity {
    public ModifiableAttributeInstance getAttribute(Attribute attribute) {
        return null;
    }

    public void setHealth(float health) {
    }

    public float getMaxHealth() {
        return 0;
    }
}
