package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Loot compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). The
 * 1.16.5 name of the 1.12 {@code EntityItem} (measured in the pinned
 * joined.tsrg — no {@code EntityItem} class ships on 1.16.5). Ctor and
 * {@code getItem} measured via javap against the pinned 36.2.42 bytes
 * ({@code (World,DDDD,ItemStack)} public ctor, {@code getItem} is
 * {@code func_92059_d}). The diamond carrier. Pinned by
 * tools/run-live.sh at live time — drift fails loudly.
 */
public class ItemEntity extends Entity {
    public ItemEntity(World world, double x, double y, double z,
            ItemStack stack) {
    }

    public ItemStack getItem() {
        return null;
    }
}
