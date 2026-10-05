package com.minewind;

import java.util.EnumMap;
import java.util.Map;

public class AttributeSystem {
    public enum Attribute {
        STRENGTH,
        AGILITY,
        ENDURANCE,
        INTELLIGENCE,
        WILLPOWER,
        LUCK,
        SPEED,
        HEALTH,
        MAGICKA
    }

    private final Map<Attribute, Integer> values = new EnumMap<>(Attribute.class);

    public void initialize() {
        for (Attribute attribute : Attribute.values()) {
            values.put(attribute, 10);
        }
        values.put(Attribute.HEALTH, 100);
        values.put(Attribute.MAGICKA, 100);
    }

    public int getAttribute(Attribute attribute) {
        return values.getOrDefault(attribute, 10);
    }

    public void setAttribute(Attribute attribute, int value) {
        values.put(attribute, value);
    }

    public int getHealth() {
        return getAttribute(Attribute.HEALTH);
    }

    public int getCarryWeight() {
        return 80 + getAttribute(Attribute.STRENGTH) * 8;
    }
}
