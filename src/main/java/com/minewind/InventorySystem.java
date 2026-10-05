package com.minewind;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom inventory system for Morrowind-style inventory management
 * Features:
 * - Weight-based inventory limits
 * - Equipment slots
 * - Stacking rules
 * - Custom items
 */
public class InventorySystem {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(InventorySystem.class);
    
    // Inventory constants
    private static final int DEFAULT_SLOTS = 64;
    private static final float DEFAULT_WEIGHT_LIMIT = 100.0f;
    private static final int MAX_STACK_SIZE = 64;
    
    // Inventory state
    private final List<ItemStack> items;
    private final Map<EquipmentSlot, ItemStack> equipment;
    private float currentWeight;
    private float weightLimit;
    
    public InventorySystem() {
        this.items = new ArrayList<>();
        this.equipment = new HashMap<>();
        this.currentWeight = 0.0f;
        this.weightLimit = DEFAULT_WEIGHT_LIMIT;
        
        // Initialize equipment slots
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            equipment.put(slot, ItemStack.EMPTY);
        }
    }
    
    public void onClientStart() {
        LOGGER.info("Inventory system started");
    }
    
    public void onClientStop() {
        LOGGER.info("Inventory system stopped");
        items.clear();
        equipment.clear();
    }
    
    public void tick(ClientPlayerEntity player) {
        // Update weight limit based on Strength
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            AttributeSystem attributeSystem = mod.getMorrowindSystems().getAttributeSystem();
            weightLimit = attributeSystem.getCarryWeight();
        }
    }
    
    /**
     * Add an item to inventory
     */
    public boolean addItem(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return false;
        }
        
        float itemWeight = getItemWeight(item);
        
        // Check weight limit
        if (currentWeight + itemWeight > weightLimit) {
            LOGGER.warn("Cannot add item: inventory over weight limit");
            return false;
        }
        
        // Try to stack with existing items
        for (ItemStack existing : items) {
            if (canStack(existing, item) && existing.getCount() < MAX_STACK_SIZE) {
                int space = MAX_STACK_SIZE - existing.getCount();
                int toAdd = Math.min(space, item.getCount());
                
                existing.increment(toAdd);
                currentWeight += itemWeight * toAdd / item.getCount();
                
                if (toAdd >= item.getCount()) {
                    return true;
                }
                
                // Create new stack for remaining items
                ItemStack remaining = item.copy();
                remaining.setCount(item.getCount() - toAdd);
                item = remaining;
            }
        }
        
        // Add as new item
        if (items.size() < DEFAULT_SLOTS) {
            items.add(item.copy());
            currentWeight += itemWeight;
            return true;
        }
        
        LOGGER.warn("Cannot add item: inventory full");
        return false;
    }
    
    /**
     * Remove an item from inventory
     */
    public boolean removeItem(ItemStack item, int count) {
        if (item == null || item.isEmpty()) {
            return false;
        }
        
        for (int i = 0; i < items.size(); i++) {
            ItemStack existing = items.get(i);
            if (existing.isOf(item.getItem()) && existing.getNbt() != null && existing.getNbt().equals(item.getNbt())) {
                if (existing.getCount() <= count) {
                    currentWeight -= getItemWeight(existing);
                    items.remove(i);
                    return true;
                } else {
                    existing.decrement(count);
                    currentWeight -= getItemWeight(existing) * count / existing.getCount();
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Equip an item
     */
    public boolean equipItem(EquipmentSlot slot, ItemStack item) {
        if (item == null || item.isEmpty()) {
            return false;
        }
        
        // Check if item can be equipped in this slot
        if (!canEquip(slot, item)) {
            return false;
        }
        
        // Unequip current item
        ItemStack current = equipment.get(slot);
        if (!current.isEmpty()) {
            addItem(current);
        }
        
        // Equip new item
        equipment.put(slot, item.copy());
        item.setCount(0);
        
        LOGGER.debug("Equipped {} in slot {}", item.getName().getString(), slot.name());
        return true;
    }
    
    /**
     * Unequip an item
     */
    public ItemStack unequipItem(EquipmentSlot slot) {
        ItemStack item = equipment.get(slot);
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        equipment.put(slot, ItemStack.EMPTY);
        LOGGER.debug("Unequipped {} from slot {}", item.getName().getString(), slot.name());
        return item;
    }
    
    /**
     * Get equipped item in a slot
     */
    public ItemStack getEquipped(EquipmentSlot slot) {
        return equipment.getOrDefault(slot, ItemStack.EMPTY);
    }
    
    /**
     * Get all items in inventory
     */
    public List<ItemStack> getItems() {
        return new ArrayList<>(items);
    }
    
    /**
     * Get current weight
     */
    public float getCurrentWeight() {
        return currentWeight;
    }
    
    /**
     * Get weight limit
     */
    public float getWeightLimit() {
        return weightLimit;
    }
    
    /**
     * Get weight percentage (0-1)
     */
    public float getWeightPercentage() {
        return currentWeight / weightLimit;
    }
    
    /**
     * Check if item can be stacked with another
     */
    private boolean canStack(ItemStack a, ItemStack b) {
        return a.isOf(b.getItem()) && 
               (a.getNbt() == null && b.getNbt() == null || 
                (a.getNbt() != null && a.getNbt().equals(b.getNbt())));
    }
    
    /**
     * Check if item can be equipped in a slot
     */
    private boolean canEquip(EquipmentSlot slot, ItemStack item) {
        // Implementation would check item type against slot type
        return true;
    }
    
    /**
     * Get weight of an item
     */
    private float getItemWeight(ItemStack item) {
        // Implementation would get weight from custom item data
        // For now, return 1.0 for all items
        return 1.0f;
    }
    
    /**
     * Save inventory to NBT
     */
    public NbtCompound saveToNbt() {
        NbtCompound nbt = new NbtCompound();
        
        // Save items
        NbtList itemsList = new NbtList();
        for (ItemStack item : items) {
            NbtCompound itemNbt = new NbtCompound();
            item.writeNbt(itemNbt);
            itemsList.add(itemNbt);
        }
        nbt.put("items", itemsList);
        
        // Save equipment
        NbtCompound equipmentNbt = new NbtCompound();
        for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                NbtCompound slotNbt = new NbtCompound();
                entry.getValue().writeNbt(slotNbt);
                equipmentNbt.put(entry.getKey().name(), slotNbt);
            }
        }
        nbt.put("equipment", equipmentNbt);
        
        nbt.putFloat("current_weight", currentWeight);
        
        return nbt;
    }
    
    /**
     * Load inventory from NBT
     */
    public void loadFromNbt(NbtCompound nbt) {
        // Load items
        if (nbt.contains("items")) {
            NbtList itemsList = nbt.getList("items");
            for (int i = 0; i < itemsList.size(); i++) {
                NbtCompound itemNbt = itemsList.getCompound(i);
                ItemStack item = ItemStack.fromNbt(itemNbt);
                if (!item.isEmpty()) {
                    items.add(item);
                    currentWeight += getItemWeight(item);
                }
            }
        }
        
        // Load equipment
        if (nbt.contains("equipment")) {
            NbtCompound equipmentNbt = nbt.getCompound("equipment");
            for (String key : equipmentNbt.getKeys()) {
                EquipmentSlot slot = EquipmentSlot.valueOf(key);
                NbtCompound slotNbt = equipmentNbt.getCompound(key);
                ItemStack item = ItemStack.fromNbt(slotNbt);
                if (!item.isEmpty()) {
                    equipment.put(slot, item);
                }
            }
        }
        
        if (nbt.contains("current_weight")) {
            currentWeight = nbt.getFloat("current_weight");
        }
    }
    
    /**
     * Equipment slots
     */
    public enum EquipmentSlot {
        HEAD,
        NECK,
        BODY,
        ROBE,
        RIGHT_HAND,
        LEFT_HAND,
        RIGHT_RING,
        LEFT_RING,
        AMULET
    }
}
