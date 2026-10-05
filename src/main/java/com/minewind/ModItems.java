package com.minewind;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

/** Morrowind weapons. Stats are rough approximations, tune freely. */
public final class ModItems {
    // Vanilla player entity reach is 3.0 blocks; "reach" below is added to that while the weapon is in the main hand.
    //                                         id                 material                damage speed   reach  durability rarity
    public static final Item IRON_DAGGER      = weapon("iron_dagger",      ToolMaterials.IRON,    1, -1.5f, -0.5, 250,  Rarity.COMMON);
    public static final Item STEEL_LONGSWORD  = weapon("steel_longsword",  ToolMaterials.IRON,    4, -2.4f,  0.0, 600,  Rarity.COMMON);
    public static final Item STEEL_CLAYMORE   = weapon("steel_claymore",   ToolMaterials.IRON,    6, -3.0f,  0.75, 700, Rarity.UNCOMMON);
    public static final Item EBONY_BROADSWORD = weapon("ebony_broadsword", ToolMaterials.DIAMOND, 6, -2.6f,  0.5, 1500, Rarity.EPIC);

    private static Item weapon(String id, ToolMaterial material, int damage, float speed, double reach, int durability, Rarity rarity) {
        AttributeModifiersComponent base = SwordItem.createAttributeModifiers(material, damage, speed);
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();
        for (AttributeModifiersComponent.Entry entry : base.modifiers()) {
            builder.add(entry.attribute(), entry.modifier(), entry.slot());
        }
        if (reach != 0.0) {
            builder.add(
                    EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE,
                    new EntityAttributeModifier(Identifier.of(MinewindMod.MOD_ID, id + "_reach"), reach, EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.MAINHAND);
        }
        Item.Settings settings = new Item.Settings()
                .maxDamage(durability)
                .rarity(rarity)
                .attributeModifiers(builder.build());
        return Registry.register(Registries.ITEM, Identifier.of(MinewindMod.MOD_ID, id), new SwordItem(material, settings));
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(IRON_DAGGER);
            entries.add(STEEL_LONGSWORD);
            entries.add(STEEL_CLAYMORE);
            entries.add(EBONY_BROADSWORD);
        });
    }

    private ModItems() {}
}
