package com.minewind;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

/**
 * Morrowind attribute system
 * Attributes range from 0-100 and affect various gameplay aspects
 */
public class AttributeSystem {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AttributeSystem.class);
    
    private final Map<MorrowindSystems.Attribute, Integer> attributeValues;
    private final Map<MorrowindSystems.Attribute, Integer> attributeModifiers;
    
    public AttributeSystem() {
        this.attributeValues = new EnumMap<>(MorrowindSystems.Attribute.class);
        this.attributeModifiers = new EnumMap<>(MorrowindSystems.Attribute.class);
        
        // Initialize attributes with starting values
        for (MorrowindSystems.Attribute attribute : MorrowindSystems.Attribute.values()) {
            attributeValues.put(attribute, 30); // Starting at 30
            attributeModifiers.put(attribute, 0);
        }
    }
    
    public void onClientStart() {
        LOGGER.info("Attribute system started");
    }
    
    public void onClientStop() {
        LOGGER.info("Attribute system stopped");
    }
    
    public void tick(ClientPlayerEntity player) {
        // Attributes don't typically tick, but we can use this for temporary effects
    }
    
    /**
     * Get base attribute value (0-100)
     */
    public int getBaseAttribute(MorrowindSystems.Attribute attribute) {
        return attributeValues.getOrDefault(attribute, 30);
    }
    
    /**
     * Get modified attribute value (base + modifiers, clamped 0-100)
     */
    public int getAttribute(MorrowindSystems.Attribute attribute) {
        int base = attributeValues.getOrDefault(attribute, 30);
        int modifier = attributeModifiers.getOrDefault(attribute, 0);
        return Math.max(0, Math.min(100, base + modifier));
    }
    
    /**
     * Set base attribute value
     */
    public void setBaseAttribute(MorrowindSystems.Attribute attribute, int value) {
        attributeValues.put(attribute, Math.max(0, Math.min(100, value)));
    }
    
    /**
     * Add a temporary modifier to an attribute
     */
    public void addModifier(MorrowindSystems.Attribute attribute, int amount) {
        int current = attributeModifiers.getOrDefault(attribute, 0);
        attributeModifiers.put(attribute, current + amount);
    }
    
    /**
     * Remove a temporary modifier from an attribute
     */
    public void removeModifier(MorrowindSystems.Attribute attribute, int amount) {
        int current = attributeModifiers.getOrDefault(attribute, 0);
        attributeModifiers.put(attribute, current - amount);
    }
    
    /**
     * Get health based on Endurance
     */
    public float getHealth() {
        int endurance = getAttribute(MorrowindSystems.Attribute.ENDURANCE);
        return endurance * 2.0f; // Base health formula
    }
    
    /**
     * Get magicka based on Intelligence and Willpower
     */
    public float getMagicka() {
        int intelligence = getAttribute(MorrowindSystems.Attribute.INTELLIGENCE);
        int willpower = getAttribute(MorrowindSystems.Attribute.WILLPOWER);
        return (intelligence + willpower) * 1.0f;
    }
    
    /**
     * Get stamina based on Strength and Endurance
     */
    public float getStamina() {
        int strength = getAttribute(MorrowindSystems.Attribute.STRENGTH);
        int endurance = getAttribute(MorrowindSystems.Attribute.ENDURANCE);
        return (strength + endurance) * 1.5f;
    }
    
    /**
     * Get carry weight based on Strength
     */
    public float getCarryWeight() {
        int strength = getAttribute(MorrowindSystems.Attribute.STRENGTH);
        return strength * 5.0f;
    }
    
    /**
     * Save attribute data to NBT
     */
    public NbtCompound saveToNbt() {
        NbtCompound nbt = new NbtCompound();
        
        NbtCompound values = new NbtCompound();
        for (MorrowindSystems.Attribute attribute : MorrowindSystems.Attribute.values()) {
            values.putInt(attribute.name(), attributeValues.get(attribute));
        }
        nbt.put("values", values);
        
        return nbt;
    }
    
    /**
     * Load attribute data from NBT
     */
    public void loadFromNbt(NbtCompound nbt) {
        if (nbt.contains("values")) {
            NbtCompound values = nbt.getCompound("values");
            for (MorrowindSystems.Attribute attribute : MorrowindSystems.Attribute.values()) {
                if (values.contains(attribute.name())) {
                    attributeValues.put(attribute, values.getInt(attribute.name()));
                }
            }
        }
    }
}
