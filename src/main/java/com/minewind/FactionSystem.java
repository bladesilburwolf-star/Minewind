package com.minewind;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Morrowind faction system
 * Manages player reputation with various factions
 */
public class FactionSystem {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FactionSystem.class);
    
    // Faction reputation range
    private static final int HATED = -100;
    private static final int DISLIKED = -50;
    private static final int NEUTRAL = 0;
    private static final int LIKED = 50;
    private static final int LOVED = 100;
    
    // Faction reputation change rates
    private static final int KILL_REPUTATION_CHANGE = -10;
    private static final int HELP_REPUTATION_CHANGE = 5;
    private static final int QUEST_REPUTATION_CHANGE = 20;
    
    private final Map<Faction, Integer> factionReputations;
    private final Map<Faction, Integer> factionRanks;
    
    public FactionSystem() {
        this.factionReputations = new EnumMap<>(Faction.class);
        this.factionRanks = new EnumMap<>(Faction.class);
        
        // Initialize all factions with neutral reputation
        for (Faction faction : Faction.values()) {
            factionReputations.put(faction, NEUTRAL);
            factionRanks.put(faction, 0);
        }
    }
    
    public void onClientStart() {
        LOGGER.info("Faction system started");
    }
    
    public void onClientStop() {
        LOGGER.info("Faction system stopped");
    }
    
    public void tick(ClientPlayerEntity player) {
        // Factions don't typically tick, but we can use this for temporary effects
    }
    
    /**
     * Get reputation with a faction
     */
    public int getReputation(Faction faction) {
        return factionReputations.getOrDefault(faction, NEUTRAL);
    }
    
    /**
     * Set reputation with a faction
     */
    public void setReputation(Faction faction, int reputation) {
        factionReputations.put(faction, Math.max(HATED, Math.min(LOVED, reputation)));
        updateRank(faction);
    }
    
    /**
     * Add reputation with a faction
     */
    public void addReputation(Faction faction, int amount) {
        int current = factionReputations.getOrDefault(faction, NEUTRAL);
        setReputation(faction, current + amount);
    }
    
    /**
     * Get rank in a faction
     */
    public int getRank(Faction faction) {
        return factionRanks.getOrDefault(faction, 0);
    }
    
    /**
     * Get reputation level as a string
     */
    public String getReputationLevel(Faction faction) {
        int reputation = getReputation(faction);
        
        if (reputation >= LOVED) {
            return "Loved";
        } else if (reputation >= LIKED) {
            return "Liked";
        } else if (reputation >= NEUTRAL) {
            return "Neutral";
        } else if (reputation >= DISLIKED) {
            return "Disliked";
        } else {
            return "Hated";
        }
    }
    
    /**
     * Check if player is liked by a faction
     */
    public boolean isLiked(Faction faction) {
        return getReputation(faction) >= LIKED;
    }
    
    /**
     * Check if player is hated by a faction
     */
    public boolean isHated(Faction faction) {
        return getReputation(faction) <= HATED;
    }
    
    /**
     * Check if player is neutral with a faction
     */
    public boolean isNeutral(Faction faction) {
        return getReputation(faction) >= DISLIKED && getReputation(faction) <= LIKED;
    }
    
    /**
     * Update rank based on reputation
     */
    private void updateRank(Faction faction) {
        int reputation = getReputation(faction);
        int rank = 0;
        
        // Simple rank calculation based on reputation
        if (reputation >= 80) {
            rank = 5; // Grandmaster
        } else if (reputation >= 60) {
            rank = 4; // Master
        } else if (reputation >= 40) {
            rank = 3; // Expert
        } else if (reputation >= 20) {
            rank = 2; // Journeyman
        } else if (reputation >= 10) {
            rank = 1; // Apprentice
        }
        
        factionRanks.put(faction, rank);
    }
    
    /**
     * Save faction data to NBT
     */
    public NbtCompound saveToNbt() {
        NbtCompound nbt = new NbtCompound();
        
        NbtCompound reputations = new NbtCompound();
        for (Faction faction : Faction.values()) {
            reputations.putInt(faction.name(), factionReputations.get(faction));
        }
        nbt.put("reputations", reputations);
        
        NbtCompound ranks = new NbtCompound();
        for (Faction faction : Faction.values()) {
            ranks.putInt(faction.name(), factionRanks.get(faction));
        }
        nbt.put("ranks", ranks);
        
        return nbt;
    }
    
    /**
     * Load faction data from NBT
     */
    public void loadFromNbt(NbtCompound nbt) {
        if (nbt.contains("reputations")) {
            NbtCompound reputations = nbt.getCompound("reputations");
            for (Faction faction : Faction.values()) {
                if (reputations.contains(faction.name())) {
                    factionReputations.put(faction, reputations.getInt(faction.name()));
                    updateRank(faction);
                }
            }
        }
        
        if (nbt.contains("ranks")) {
            NbtCompound ranks = nbt.getCompound("ranks");
            for (Faction faction : Faction.values()) {
                if (ranks.contains(faction.name())) {
                    factionRanks.put(faction, ranks.getInt(faction.name()));
                }
            }
        }
    }
    
    /**
     * Morrowind factions
     */
    public enum Faction {
        // Great Houses
        HOUSE_REDOAN,
        HOUSE_TELVANNI,
        HOUSE_DRES,
        HOUSE_INDORIL,
        HOUSE_HLAALU,
        
        // Guilds
        FIGHTERS_GUILD,
        MAGES_GUILD,
        THIEVES_GUILD,
        DARK_BROTHERHOOD,
        
        // Religious
        IMPERIAL_CULT,
        TRIBUNAL_TEMPLE,
        
        // Other
        IMPERIAL_LEGION,
        EAST_EMPANY,
        BLADES,
        VAMPIRE
    }
}
