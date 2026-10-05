package com.minewind;

public class MorrowindSystems {
    private final AttributeSystem attributeSystem = new AttributeSystem();
    private final CombatSystem combatSystem = new CombatSystem();
    private final DialogueSystem dialogueSystem = new DialogueSystem();
    private final FactionSystem factionSystem = new FactionSystem();
    private final HUDSystem hudSystem = new HUDSystem();
    private final InventorySystem inventorySystem = new InventorySystem();
    private final ModelSystem modelSystem = new ModelSystem();
    private final MovementSystem movementSystem = new MovementSystem();
    private final QuestSystem questSystem = new QuestSystem();
    private final SkillSystem skillSystem = new SkillSystem();
    private final SpellSystem spellSystem = new SpellSystem();

    public void initialize() {
        modelSystem.initialize();
        movementSystem.initialize();
        combatSystem.initialize();
        hudSystem.initialize();
        inventorySystem.initialize();
        questSystem.initialize();
        skillSystem.initialize();
        spellSystem.initialize();
        dialogueSystem.initialize();
        factionSystem.initialize();
        attributeSystem.initialize();
    }

    public AttributeSystem getAttributeSystem() {
        return attributeSystem;
    }

    public CombatSystem getCombatSystem() {
        return combatSystem;
    }

    public DialogueSystem getDialogueSystem() {
        return dialogueSystem;
    }

    public FactionSystem getFactionSystem() {
        return factionSystem;
    }

    public HUDSystem getHudSystem() {
        return hudSystem;
    }

    public InventorySystem getInventorySystem() {
        return inventorySystem;
    }

    public ModelSystem getModelSystem() {
        return modelSystem;
    }

    public MovementSystem getMovementSystem() {
        return movementSystem;
    }

    public QuestSystem getQuestSystem() {
        return questSystem;
    }

    public SkillSystem getSkillSystem() {
        return skillSystem;
    }

    public SpellSystem getSpellSystem() {
        return spellSystem;
    }
}
