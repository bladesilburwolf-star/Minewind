package com.minewind;

import java.util.EnumMap;
import java.util.Map;

public class SkillSystem {
    public enum Skill {
        ATHLETICS,
        ACROBATICS,
        ILLUSION,
        DESTRUCTION,
        ALTERATION,
        RESTORATION,
        SPEAR,
        LONGSWORD,
        SHORTSWORD,
        MARKSMANSHIP
    }

    private final Map<Skill, Integer> levels = new EnumMap<>(Skill.class);

    public void initialize() {
        for (Skill skill : Skill.values()) {
            levels.put(skill, 1);
        }
    }

    public int getSkillLevel(Skill skill) {
        return levels.getOrDefault(skill, 1);
    }

    public void setSkillLevel(Skill skill, int level) {
        levels.put(skill, level);
    }
}
