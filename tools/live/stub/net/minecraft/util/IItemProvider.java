package net.minecraft.util;

/**
 * Loot compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). The
 * {@code ItemStack} ctors take this, not {@code Item} (measured via
 * javap + joined.tsrg against the pinned 36.2.42 bytes: obf {@code brw}
 * maps to this interface, {@code blx}/{@code Item} implements it).
 * Ctors are never obfuscated, so no narrow-map row.
 */
public interface IItemProvider {
}
