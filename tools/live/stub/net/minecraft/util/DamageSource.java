package net.minecraft.util;

import net.minecraft.entity.Entity;

/**
 * Loot compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). Only the
 * type is needed (the kill-event damage slot, never constructed by
 * forge/); forge reads nothing through it at this tranche.
 *
 * <p>Combat tranche: the bridge combat hook resolves the attacker through
 * {@code getTrueSource} ({@code func_76346_g ()->Entity} on obf
 * {@code apk/k}, measured via javap against the pinned notch server jar —
 * the same-descriptor {@code func_76364_f} sibling is NOT the true
 * source, hence the SRG anchor in the narrow map). Never runs.
 */
public class DamageSource {
    public Entity getTrueSource() {
        return null;
    }
}
