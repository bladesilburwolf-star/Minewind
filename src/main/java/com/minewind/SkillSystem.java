package com.minewind;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

/**
 * Morrowind skill system with dynamic progression
 * Each skill ranges from 0-100 and improves through use
 */
public class SkillSystem {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SkillSystem.class);
    
    private static final float SKILL_IMPROVEMENT_RATE = 0.1f;
    private static final float MAJOR_SKILL_MULTIPLIER = 1.5f;
    private static final float MINOR_SKILL_MULTIPLIER = 1.0f;
    private static final float MISC_SKILL_MULTIPLIER = 0.5f;
    
    private final Map<MorrowindSystems.Skill, Float> skillValues;
    private final Map<MorrowindSystems.Skill, Float> skillProgress;
    private final Map<MorrowindSystems.Skill, SkillType> skillTypes;
    
    public SkillSystem() {
        this.skillValues = new EnumMap<>(MorrowindSystems.Skill.class);
        this.skillProgress = new EnumMap<>(MorrowindSystems.Skill.class);
        this.skillTypes = new EnumMap<>(MorrowindSystems.Skill.class);
        
        // Initialize all skills with default values
        for (MorrowindSystems.Skill skill : MorrowindSystems.Skill.values()) {
            skillValues.put(skill, 10.0f); // Starting at 10
            skillProgress.put(skill, 0.0f);
            skillTypes.put(skill, SkillType.MINOR);
        }
        
        // Set major skills (class-dependent, default for now)
        skillTypes.put(MorrowindSystems.Skill.BLADES, SkillType.MAJOR);
        skillTypes.put(MorrowindSystems.Skill.DESTRUCTION, SkillType.MAJOR);
        skillTypes.put(MorrowindSystems.Skill.HEAVY_ARMOR, SkillType.MAJOR);
        skillTypes.put(MorrowindSystems.Skill.BLOCK, SkillType.MAJOR);
        skillTypes.put(MorrowindSystems.Skill.RESTORATION, SkillType.MAJOR);
    }
    
    public void onClientStart() {
        LOGGER.info("Skill system started");
    }
    
    public void onClientStop() {
        LOGGER.info("Skill system stopped");
    }
    
    public void tick(ClientPlayerEntity player) {
        // Process skill improvements based on usage
        for (MorrowindSystems.Skill skill : MorrowindSystems.Skill.values()) {
            float progress = skillProgress.get(skill);
            if (progress >= 100.0f) {
                // Level up the skill
                float current = skillValues.get(skill);
                float newValue = Math.min(100.0f, current + 1.0f);
                skillValues.put(skill, newValue);
                skillProgress.put(skill, progress - 100.0f);
                LOGGER.info("Skill {} increased to {}", skill.name(), newValue);
            }
        }
    }
    
    /**
     * Increase skill progress based on usage
     */
    public void addSkillProgress(MorrowindSystems.Skill skill, float amount) {
        SkillType type = skillTypes.get(skill);
        float multiplier = getMultiplier(type);
        float adjustedAmount = amount * multiplier * SKILL_IMPROVEMENT_RATE;
        
        float currentProgress = skillProgress.get(skill);
        skillProgress.put(skill, currentProgress + adjustedAmount);
    }
    
    /**
     * Get current skill value (0-100)
     */
    public float getSkillValue(MorrowindSystems.Skill skill) {
        return skillValues.getOrDefault(skill, 10.0f);
    }
    
    /**
     * Get skill progress towards next level (0-100)
     */
    public float getSkillProgress(MorrowindSystems.Skill skill) {
        return skillProgress.getOrDefault(skill, 0.0f);
    }
    
    /**
     * Set skill value directly
     */
    public void setSkillValue(MorrowindSystems.Skill skill, float value) {
        skillValues.put(skill, Math.max(0.0f, Math.min(100.0f, value)));
    }
    
    /**
     * Set skill type (Major, Minor, Misc)
     */
    public void setSkillType(MorrowindSystems.Skill skill, SkillType type) {
        skillTypes.put(skill, type);
    }
    
    /**
     * Get skill type
     */
    public SkillType getSkillType(MorrowindSystems.Skill skill) {
        return skillTypes.get(skill);
    }
    
    /**
     * Save skill data to NBT
     */
    public NbtCompound saveToNbt() {
        NbtCompound nbt = new NbtCompound();
        
        NbtCompound values = new NbtCompound();
        for (MorrowindSystems.Skill skill : MorrowindSystems.Skill.values()) {
            values.putFloat(skill.name(), skillValues.get(skill));
        }
        nbt.put("values", values);
        
        NbtCompound progress = new NbtCompound();
        for (MorrowindSystems.Skill skill : MorrowindSystems.Skill.values()) {
            progress.putFloat(skill.name(), skillProgress.get(skill));
        }
        nbt.put("progress", progress);
        
        return nbt;
    }
    
    /**
     * Load skill data from NBT
     */
    public void loadFromNbt(NbtCompound nbt) {
        if (nbt.contains("values")) {
            NbtCompound values = nbt.getCompound("values");
            for (MorrowindSystems.Skill skill : MorrowindSystems.Skill.values()) {
                if (values.contains(skill.name())) {
                    skillValues.put(skill, values.getFloat(skill.name()));
                }
            }
        }
        
        if (nbt.contains("progress")) {
            NbtCompound progress = nbt.getCompound("progress");
            for (MorrowindSystems.Skill skill : MorrowindSystems.Skill.values()) {
                if (progress.contains(skill.name())) {
                    skillProgress.put(skill, progress.getFloat(skill.name()));
                }
            }
        }
    }
    
    private float getMultiplier(SkillType type) {
        return switch (type) {
            case MAJOR -> MAJOR_SKILL_MULTIPLIER;
            case MINOR -> MINOR_SKILL_MULTIPLIER;
            case MISC -> MISC_SKILL_MULTIPLIER;
        };
    }
    
    public enum SkillType {
        MAJOR, MINOR, MISC
    }
}
