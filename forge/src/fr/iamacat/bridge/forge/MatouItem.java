package fr.iamacat.bridge.forge;

import net.minecraft.item.Item;

/**
 * Generic item registered on behalf of example1.
 */
public final class MatouItem extends Item {
    public MatouItem(int stackSize) {
        super(new Item.Properties().maxStackSize(stackSize));
    }
}
