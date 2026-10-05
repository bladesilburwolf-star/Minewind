package com.minewind;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventorySystem {
    private final List<ItemStack> items = new ArrayList<>();

    public void initialize() {
        items.clear();
    }

    public boolean addItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        items.add(stack.copy());
        return true;
    }

    public boolean removeItem(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return items.removeIf(entry -> entry.isOf(stack.getItem()));
    }

    public List<ItemStack> getItems() {
        return new ArrayList<>(items);
    }
}
