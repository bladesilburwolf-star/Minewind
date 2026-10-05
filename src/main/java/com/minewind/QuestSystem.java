package com.minewind;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventorySystem {
    private static final Logger LOGGER = LoggerFactory.getLogger(InventorySystem.class);

    private static final int DEFAULT_SLOTS = 64;
    private static final float DEFAULT_WEIGHT_LIMIT = 100.0f;
    private static final int MAX_STACK_SIZE = 64;

    private final List<ItemStack> items;
    private final Map<EquipmentSlot, ItemStack> equipment;
    private float currentWeight;
    private float weightLimit;

    public InventorySystem() {
        this.items = new ArrayList<>();
        this.equipment = new HashMap<>();
        this.currentWeight = 0.0f;
        this.weightLimit = DEFAULT_WEIGHT_LIMIT;

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
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            AttributeSystem attributeSystem = mod.getMorrowindSystems().getAttributeSystem();
            weightLimit = attributeSystem.getCarryWeight();
        }
    }

    public boolean addItem(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return false;
        }

        float itemWeight = getItemWeight(item);

        if (currentWeight + itemWeight > weightLimit) {
            LOGGER.warn("Cannot add item: inventory over weight limit");
            return false;
        }

        for (ItemStack existing : items) {
            if (canStack(existing, item) && existing.getCount() < MAX_STACK_SIZE) {
                int space = MAX_STACK_SIZE - existing.getCount();
                int toAdd = Math.min(space, item.getCount());
                existing.increment(toAdd);
                currentWeight += itemWeight * toAdd / item.getCount();

                if (toAdd >= item.getCount()) {
                    return true;
                }

                ItemStack remaining = item.copy();
                remaining.setCount(item.getCount() - toAdd);
                item = remaining;
            }
        }

        if (items.size() < DEFAULT_SLOTS) {
            items.add(item.copy());
            currentWeight += itemWeight;
            return true;
        }

        LOGGER.warn("Cannot add item: inventory full");
        return false;
    }

    public boolean removeItem(ItemStack item, int count) {
        if (item == null || item.isEmpty()) {
            return false;
        }

        for (int i = 0; i < items.size(); i++) {
            ItemStack existing = items.get(i);
            if (existing.isOf(item.getItem()) && stacksEqual(existing, item)) {
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

    public boolean equipItem(EquipmentSlot slot, ItemStack item) {
        if (item == null || item.isEmpty()) {
            return false;
        }

        if (!canEquip(slot, item)) {
            return false;
        }

        ItemStack current = equipment.get(slot);
        if (!current.isEmpty()) {
            addItem(current);
        }

        equipment.put(slot, item.copy());
        item.setCount(0);
        LOGGER.debug("Equipped {} in slot {}", item.getName().getString(), slot.name());
        return true;
    }

    public ItemStack unequipItem(EquipmentSlot slot) {
        ItemStack item = equipment.get(slot);
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }

        equipment.put(slot, ItemStack.EMPTY);
        LOGGER.debug("Unequipped {} from slot {}", item.getName().getString(), slot.name());
        return item;
    }

    public ItemStack getEquipped(EquipmentSlot slot) {
        return equipment.getOrDefault(slot, ItemStack.EMPTY);
    }

    public List<ItemStack> getItems() {
        return new ArrayList<>(items);
    }

    public float getCurrentWeight() {
        return currentWeight;
    }

    public float getWeightLimit() {
        return weightLimit;
    }

    public float getWeightPercentage() {
        return currentWeight / weightLimit;
    }

    private boolean canStack(ItemStack a, ItemStack b) {
        return a.isOf(b.getItem()) && stacksEqual(a, b);
    }

    private boolean stacksEqual(ItemStack a, ItemStack b) {
        if (a.hasNbt() != b.hasNbt()) {
            return false;
        }
        if (a.hasNbt()) {
            return a.getNbt().equals(b.getNbt());
        }
        return true;
    }

    private boolean canEquip(EquipmentSlot slot, ItemStack item) {
        return true;
    }

    private float getItemWeight(ItemStack item) {
        return 1.0f;
    }

    public NbtCompound saveToNbt() {
        NbtCompound nbt = new NbtCompound();

        NbtList itemsList = new NbtList();
        for (ItemStack item : items) {
            NbtCompound itemNbt = new NbtCompound();
            itemNbt.putString("id", Registries.ITEM.getId(item.getItem()).toString());
            itemNbt.putInt("count", item.getCount());
            itemsList.add(itemNbt);
        }
        nbt.put("items", itemsList);

        NbtCompound equipmentNbt = new NbtCompound();
        for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                NbtCompound slotNbt = new NbtCompound();
                ItemStack value = entry.getValue();
                slotNbt.putString("id", Registries.ITEM.getId(value.getItem()).toString());
                slotNbt.putInt("count", value.getCount());
                equipmentNbt.put(entry.getKey().name(), slotNbt);
            }
        }
        nbt.put("equipment", equipmentNbt);

        nbt.putFloat("current_weight", currentWeight);
        return nbt;
    }

    public void loadFromNbt(NbtCompound nbt) {
        if (nbt.contains("items")) {
            NbtList itemsList = nbt.getList("items", 10);
            for (int i = 0; i < itemsList.size(); i++) {
                NbtCompound itemNbt = itemsList.getCompound(i);
                String itemId = itemNbt.getString("id");
                Item item = Registries.ITEM.get(Identifier.tryParse(itemId));
                if (item != null) {
                    int count = itemNbt.getInt("count");
                    items.add(new ItemStack(item, count));
                    currentWeight += getItemWeight(new ItemStack(item, count));
                }
            }
        }

        if (nbt.contains("equipment")) {
            NbtCompound equipmentNbt = nbt.getCompound("equipment");
            for (String key : equipmentNbt.getKeys()) {
                try {
                    EquipmentSlot slot = EquipmentSlot.valueOf(key);
                    NbtCompound slotNbt = equipmentNbt.getCompound(key);
                    String itemId = slotNbt.getString("id");
                    Item item = Registries.ITEM.get(Identifier.tryParse(itemId));
                    if (item != null) {
                        equipment.put(slot, new ItemStack(item, slotNbt.getInt("count")));
                    }
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Unknown equipment slot: {}", key);
                }
            }
        }

        if (nbt.contains("current_weight")) {
            currentWeight = nbt.getFloat("current_weight");
        }
    }

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
