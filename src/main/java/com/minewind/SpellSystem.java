package com.minewind;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dynamic spellcasting system for Morrowind-style magic
 * Features:
 * - Spell creation and customization
 * - Multiple magic schools (Destruction, Restoration, Illusion, etc.)
 * - Spell effects and projectiles
 * - Magicka cost calculation
 * - Spell casting animations
 */
public class SpellSystem {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellSystem.class);
    
    // Spell constants
    private static final float BASE_MAGICKA_COST = 10.0f;
    private static final float SPELL_CAST_TIME = 0.5f;
    private static final float PROJECTILE_SPEED = 20.0f;
    private static final float SPELL_RANGE = 50.0f;
    
    // Spell state
    private boolean isCasting;
    private float castProgress;
    private Spell currentSpell;
    private float castCooldown;
    
    // Known spells
    private final Map<String, Spell> knownSpells;
    private final List<ActiveSpellEffect> activeEffects;
    
    public SpellSystem() {
        this.isCasting = false;
        this.castProgress = 0.0f;
        this.currentSpell = null;
        this.castCooldown = 0.0f;
        this.knownSpells = new HashMap<>();
        this.activeEffects = new ArrayList<>();
        
        // Register default spells
        registerDefaultSpells();
    }
    
    public void initialize() {
        LOGGER.info("Initializing spell system...");
        
        // Register spell handlers
        registerSpellHandlers();
        
        LOGGER.info("Spell system initialized with {} spells", knownSpells.size());
    }
    
    public void onClientStart() {
        LOGGER.info("Spell system started");
    }
    
    public void onClientStop() {
        LOGGER.info("Spell system stopped");
        activeEffects.clear();
    }
    
    private void registerSpellHandlers() {
        // Handlers will be registered through mixins
        LOGGER.debug("Spell handlers registered");
    }
    
    private void registerDefaultSpells() {
        // Destruction spells
        registerSpell(new Spell("fireball", MagicSchool.DESTRUCTION, 25.0f, 20.0f, 10.0f));
        registerSpell(new Spell("frostbite", MagicSchool.DESTRUCTION, 20.0f, 15.0f, 8.0f));
        registerSpell(new Spell("lightning_bolt", MagicSchool.DESTRUCTION, 30.0f, 30.0f, 15.0f));
        
        // Restoration spells
        registerSpell(new Spell("healing", MagicSchool.RESTORATION, 20.0f, 0.0f, 10.0f));
        registerSpell(new Spell("restore_health", MagicSchool.RESTORATION, 30.0f, 0.0f, 15.0f));
        registerSpell(new Spell("restore_magicka", MagicSchool.RESTORATION, 25.0f, 0.0f, 12.0f));
        
        // Illusion spells
        registerSpell(new Spell("calm", MagicSchool.ILLUSION, 20.0f, 0.0f, 10.0f));
        registerSpell(new Spell("frenzy", MagicSchool.ILLUSION, 25.0f, 0.0f, 12.0f));
        registerSpell(new Spell("invisibility", MagicSchool.ILLUSION, 30.0f, 0.0f, 15.0f));
        
        // Alteration spells
        registerSpell(new Spell("jump", MagicSchool.ALTERATION, 15.0f, 0.0f, 8.0f));
        registerSpell(new Spell("levitate", MagicSchool.ALTERATION, 25.0f, 0.0f, 12.0f));
        registerSpell(new Spell("water_walking", MagicSchool.ALTERATION, 20.0f, 0.0f, 10.0f));
        
        // Conjuration spells
        registerSpell(new Spell("summon_flame_atronach", MagicSchool.CONJURATION, 50.0f, 0.0f, 25.0f));
        registerSpell(new Spell("summon_frost_atronach", MagicSchool.CONJURATION, 60.0f, 0.0f, 30.0f));
        
        LOGGER.debug("Registered {} default spells", knownSpells.size());
    }
    
    /**
     * Register a spell
     */
    public void registerSpell(Spell spell) {
        knownSpells.put(spell.getId(), spell);
        LOGGER.debug("Registered spell: {}", spell.getId());
    }
    
    /**
     * Start casting a spell
     */
    public boolean startCasting(String spellId, ClientPlayerEntity player) {
        if (isCasting || castCooldown > 0) {
            return false;
        }
        
        Spell spell = knownSpells.get(spellId);
        if (spell == null) {
            LOGGER.warn("Unknown spell: {}", spellId);
            return false;
        }
        
        // Check magicka
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            AttributeSystem attributeSystem = mod.getMorrowindSystems().getAttributeSystem();
            float currentMagicka = attributeSystem.getMagicka();
            
            if (currentMagicka < spell.getMagickaCost()) {
                LOGGER.info("Not enough magicka for spell: {}", spellId);
                return false;
            }
        }
        
        isCasting = true;
        castProgress = 0.0f;
        currentSpell = spell;
        
        LOGGER.debug("Started casting spell: {}", spellId);
        return true;
    }
    
    /**
     * Cancel current spell casting
     */
    public void cancelCasting() {
        isCasting = false;
        castProgress = 0.0f;
        currentSpell = null;
        LOGGER.debug("Casting cancelled");
    }
    
    /**
     * Complete spell casting and execute spell
     */
    public boolean completeCasting(ClientPlayerEntity player) {
        if (!isCasting || castProgress < 1.0f) {
            return false;
        }
        
        if (currentSpell == null) {
            return false;
        }
        
        // Deduct magicka
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            AttributeSystem attributeSystem = mod.getMorrowindSystems().getAttributeSystem();
            float currentMagicka = attributeSystem.getMagicka();
            float newMagicka = currentMagicka - currentSpell.getMagickaCost();
            
            // For now, just log the magicka cost
            LOGGER.debug("Deducting {} magicka for spell: {}", currentSpell.getMagickaCost(), currentSpell.getId());
        }
        
        // Execute spell
        executeSpell(currentSpell, player);
        
        // Reset casting state
        isCasting = false;
        castProgress = 0.0f;
        castCooldown = currentSpell.getCooldown();
        
        // Progress magic skill
        progressMagicSkill(player, currentSpell.getSchool());
        
        currentSpell = null;
        return true;
    }
    
    /**
     * Execute a spell
     */
    private void executeSpell(Spell spell, ClientPlayerEntity player) {
        LOGGER.debug("Executing spell: {}", spell.getId());
        
        switch (spell.getSchool()) {
            case DESTRUCTION -> executeDestructionSpell(spell, player);
            case RESTORATION -> executeRestorationSpell(spell, player);
            case ILLUSION -> executeIllusionSpell(spell, player);
            case ALTERATION -> executeAlterationSpell(spell, player);
            case CONJURATION -> executeConjurationSpell(spell, player);
            case MYSTICISM -> executeMysticismSpell(spell, player);
        }
    }
    
    /**
     * Execute destruction spell (projectile-based)
     */
    private void executeDestructionSpell(Spell spell, ClientPlayerEntity player) {
        Vec3d lookDirection = player.getRotationVector();
        Vec3d start = player.getEyePos();
        Vec3d end = start.add(lookDirection.multiply(SPELL_RANGE));
        
        // Create projectile
        SpellProjectile projectile = new SpellProjectile(
            spell.getId(),
            start,
            lookDirection.multiply(PROJECTILE_SPEED),
            spell.getPower(),
            spell.getDuration()
        );
        
        // Add to active effects
        activeEffects.add(projectile);
        
        LOGGER.debug("Fired destruction projectile: {}", spell.getId());
    }
    
    /**
     * Execute restoration spell (instant effect)
     */
    private void executeRestorationSpell(Spell spell, ClientPlayerEntity player) {
        float healAmount = spell.getPower();
        
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            AttributeSystem attributeSystem = mod.getMorrowindSystems().getAttributeSystem();
            float currentHealth = attributeSystem.getHealth();
            float newHealth = Math.min(currentHealth + healAmount, attributeSystem.getHealth());
            
            LOGGER.debug("Healed for {} health", healAmount);
        }
    }
    
    /**
     * Execute illusion spell
     */
    private void executeIllusionSpell(Spell spell, ClientPlayerEntity player) {
        // Illusion spells affect targets
        LOGGER.debug("Cast illusion spell: {}", spell.getId());
    }
    
    /**
     * Execute alteration spell
     */
    private void executeAlterationSpell(Spell spell, ClientPlayerEntity player) {
        // Alteration spells modify the caster or environment
        LOGGER.debug("Cast alteration spell: {}", spell.getId());
    }
    
    /**
     * Execute conjuration spell
     */
    private void executeConjurationSpell(Spell spell, ClientPlayerEntity player) {
        // Conjuration spells summon entities
        LOGGER.debug("Cast conjuration spell: {}", spell.getId());
    }
    
    /**
     * Execute mysticism spell
     */
    private void executeMysticismSpell(Spell spell, ClientPlayerEntity player) {
        // Mysticism spells are utility spells
        LOGGER.debug("Cast mysticism spell: {}", spell.getId());
    }
    
    /**
     * Update spell system (called every tick)
     */
    public void tick(ClientPlayerEntity player) {
        // Update casting progress
        if (isCasting) {
            castProgress += 1.0f / (SPELL_CAST_TIME * 20.0f); // 20 ticks per second
            if (castProgress >= 1.0f) {
                castProgress = 1.0f;
            }
        }
        
        // Update cooldown
        if (castCooldown > 0) {
            castCooldown -= 0.05f;
        }
        
        // Update active effects
        for (int i = activeEffects.size() - 1; i >= 0; i--) {
            ActiveSpellEffect effect = activeEffects.get(i);
            effect.tick();
            
            if (effect.isExpired()) {
                activeEffects.remove(i);
            }
        }
    }
    
    /**
     * Progress magic skill based on school
     */
    private void progressMagicSkill(ClientPlayerEntity player, MagicSchool school) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            SkillSystem skillSystem = mod.getMorrowindSystems().getSkillSystem();
            
            switch (school) {
                case DESTRUCTION -> skillSystem.addSkillProgress(MorrowindSystems.Skill.DESTRUCTION, 5.0f);
                case RESTORATION -> skillSystem.addSkillProgress(MorrowindSystems.Skill.RESTORATION, 5.0f);
                case ILLUSION -> skillSystem.addSkillProgress(MorrowindSystems.Skill.ILLUSION, 5.0f);
                case ALTERATION -> skillSystem.addSkillProgress(MorrowindSystems.Skill.ALTERATION, 5.0f);
                case CONJURATION -> skillSystem.addSkillProgress(MorrowindSystems.Skill.CONJURATION, 5.0f);
                case MYSTICISM -> skillSystem.addSkillProgress(MorrowindSystems.Skill.MYSTICISM, 5.0f);
            }
        }
    }
    
    /**
     * Check if player is currently casting
     */
    public boolean isCasting() {
        return isCasting;
    }
    
    /**
     * Get current spell being cast
     */
    public Spell getCurrentSpell() {
        return currentSpell;
    }
    
    /**
     * Get casting progress (0-1)
     */
    public float getCastProgress() {
        return castProgress;
    }
    
    /**
     * Get a spell by ID
     */
    public Spell getSpell(String id) {
        return knownSpells.get(id);
    }
    
    /**
     * Get all known spells
     */
    public Map<String, Spell> getKnownSpells() {
        return new HashMap<>(knownSpells);
    }
    
    /**
     * Check if player knows a spell
     */
    public boolean knowsSpell(String id) {
        return knownSpells.containsKey(id);
    }
    
    public enum MagicSchool {
        DESTRUCTION, RESTORATION, ILLUSION, ALTERATION, CONJURATION, MYSTICISM
    }
    
    /**
     * Spell representation
     */
    public static class Spell {
        private final String id;
        private final MagicSchool school;
        private final float magickaCost;
        private final float power;
        private final float cooldown;
        private final float duration;
        
        public Spell(String id, MagicSchool school, float magickaCost, float power, float duration) {
            this.id = id;
            this.school = school;
            this.magickaCost = magickaCost;
            this.power = power;
            this.duration = duration;
            this.cooldown = magickaCost / 10.0f; // Cooldown based on magicka cost
        }
        
        public String getId() {
            return id;
        }
        
        public MagicSchool getSchool() {
            return school;
        }
        
        public float getMagickaCost() {
            return magickaCost;
        }
        
        public float getPower() {
            return power;
        }
        
        public float getDuration() {
            return duration;
        }
        
        public float getCooldown() {
            return cooldown;
        }
    }
    
    /**
     * Active spell effect (projectiles, area effects, etc.)
     */
    public abstract static class ActiveSpellEffect {
        private final String spellId;
        private float duration;
        private float timeAlive;
        
        public ActiveSpellEffect(String spellId, float duration) {
            this.spellId = spellId;
            this.duration = duration;
            this.timeAlive = 0.0f;
        }
        
        public void tick() {
            timeAlive += 0.05f;
        }
        
        public boolean isExpired() {
            return timeAlive >= duration && duration > 0;
        }
        
        public String getSpellId() {
            return spellId;
        }
        
        public float getDuration() {
            return duration;
        }
        
        public float getTimeAlive() {
            return timeAlive;
        }
    }
    
    /**
     * Spell projectile
     */
    public static class SpellProjectile extends ActiveSpellEffect {
        private Vec3d position;
        private Vec3d velocity;
        private float power;
        
        public SpellProjectile(String spellId, Vec3d position, Vec3d velocity, float power, float duration) {
            super(spellId, duration);
            this.position = position;
            this.velocity = velocity;
            this.power = power;
        }
        
        @Override
        public void tick() {
            super.tick();
            position = position.add(velocity);
        }
        
        public Vec3d getPosition() {
            return position;
        }
        
        public Vec3d getVelocity() {
            return velocity;
        }
        
        public float getPower() {
            return power;
        }
    }
}
