package net.minecraft.item;

import net.minecraft.util.IItemProvider;

/**
 * Loot compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). Ctors
 * measured via javap + joined.tsrg against the pinned 36.2.42 bytes
 * (public {@code (IItemProvider,int)} — obf {@code bmb(brw,int)}; the E0
 * stub said {@code (Item,int)} from the javap alone, and the first
 * SPAWN=1 live run died loud {@code NoSuchMethodError:
 * ItemStack.<init>(Item;I)V} at the first carrier drop); {@code getItem}
 * is {@code func_77973_b} per the pinned snapshot (kept for the
 * companion tranche, unused by forge/ at this tranche). Ctors are never
 * obfuscated, so no narrow-map row — drift fails live, loudly.
 */
public class ItemStack {
    public ItemStack(IItemProvider item, int size) {
    }

    public ItemStack(IItemProvider item) {
    }

    public Item getItem() {
        return null;
    }
}
