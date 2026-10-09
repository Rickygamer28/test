package com.example.skyblocks;

import java.util.function.Supplier;
import net.minecraft.world.item.Item;

/** Item supplier for our SkyblockItemTypes constants (resolved lazily, after items are registered). */
public record SkyItemSupplier(int index) implements Supplier<Item> {
    @Override
    public Item get() {
        return ModItems.SKY_ITEMS.get(index).get();
    }
}
