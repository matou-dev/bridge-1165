package net.minecraft.item;

import net.minecraft.util.IItemProvider;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Loot and registration compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). Implements
 * {@code IItemProvider} like the runtime (measured: obf {@code blx}
 * implements {@code brw}) so an {@code Item} passes where the
 * {@code ItemStack} ctors take the provider.
 * Implements {@code IForgeRegistryEntry} per the Forge binary patch.
 */
public class Item implements IItemProvider, IForgeRegistryEntry<Item> {
    public static class Properties {
        public Properties maxStackSize(int maxStackSize) {
            return this;
        }
    }

    public Item(Item.Properties properties) {
    }

    public static int getIdFromItem(Item item) {
        return 0;
    }
}
