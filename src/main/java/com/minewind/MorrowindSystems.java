package com.minewind;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core Morrowind RPG mechanics system
 * Manages all Morrowind-specific gameplay systems:
 * - Skills and leveling
 * - Attributes (Strength, Intelligence, Willpower, etc.)
 * - Dynamic spellcasting
 * - Faction system
 * - Dialogue system
 * - Quest system
 */
public class MorrowindSystems {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MorrowindSystems.class);
    
    private final SkillSystem skillSystem;
    private final AttributeSystem attributeSystem;
    private final FactionSystem factionSystem;
    private final DialogueSystem dialogueSystem;
    private final QuestSystem questSystem;
    private final InventorySystem inventorySystem;
    
    public MorrowindSystems() {
        LOGGER.info("Initializing Morrowind RPG systems...");
        this.skillSystem = new SkillSystem();
        this.attributeSystem = new AttributeSystem();
        this.factionSystem = new FactionSystem();
        this.dialogueSystem = new DialogueSystem();
        this.questSystem = new QuestSystem();
        this.inventorySystem = new InventorySystem();
        LOGGER.info("Morrowind RPG systems initialized");
    }
    
    public void onClientStart() {
        LOGGER.info("Starting Morrowind systems...");
        skillSystem.onClientStart();
        attributeSystem.onClientStart();
        factionSystem.onClientStart();
        dialogueSystem.onClientStart();
        questSystem.onClientStart();
        inventorySystem.onClientStart();
    }
    
    public void onClientStop() {
        LOGGER.info("Stopping Morrowind systems...");
        skillSystem.onClientStop();
        attributeSystem.onClientStop();
        factionSystem.onClientStop();
        dialogueSystem.onClientStop();
        questSystem.onClientStop();
        inventorySystem.onClientStop();
    }
    
    public void onTick(MinecraftClient client) {
        if (client.player != null) {
            skillSystem.tick(client.player);
            attributeSystem.tick(client.player);
            factionSystem.tick(client.player);
            questSystem.tick(client.player);
        }
    }
    
    public SkillSystem getSkillSystem() {
        return skillSystem;
    }
    
    public AttributeSystem getAttributeSystem() {
        return attributeSystem;
    }
    
    public FactionSystem getFactionSystem() {
        return factionSystem;
    }
    
    public DialogueSystem getDialogueSystem() {
        return dialogueSystem;
    }
    
    public QuestSystem getQuestSystem() {
        return questSystem;
    }
    
    public InventorySystem getInventorySystem() {
        return inventorySystem;
    }
    
    // Skill definitions (Morrowind skills)
    public enum Skill {
        // Combat skills
        BLOCK, ARMORER, HEAVY_ARMOR, MEDIUM_ARMOR, LIGHT_ARMOR,
        BLADES, BLUNT, LONG_BLADE, AXE, SPEAR,
        ARCHERY, MARKSMAN, 
        
        // Magic skills
        DESTRUCTION, RESTORATION, ILLUSION, ALTERATION, CONJURATION, MYSTICISM,
        ALCHEMY, ENCHANT, 
        
        // Stealth skills
        SNEAK, SECURITY, PICKPOCKET, ACROBATICS,
        
        // Other skills
        MERCANTILE, SPEECHCRAFT, HAND_TO_HAND
    }
    
    // Attribute definitions (Morrowind attributes)
    public enum Attribute {
        STRENGTH, INTELLIGENCE, WILLPOWER,
        AGILITY, SPEED, ENDURANCE,
        PERSONALITY, LUCK
    }
}
