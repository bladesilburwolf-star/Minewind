package com.minewind;

import java.util.HashMap;
import java.util.Map;

public class FactionSystem {
    private final Map<String, Integer> reputation = new HashMap<>();

    public void initialize() {
        reputation.clear();
        reputation.put("House Hlaalu", 0);
        reputation.put("House Redoran", 0);
        reputation.put("House Telvanni", 0);
        reputation.put("Imperial Cult", 0);
    }

    public int getReputation(String faction) {
        return reputation.getOrDefault(faction, 0);
    }

    public void setReputation(String faction, int value) {
        reputation.put(faction, value);
    }

    public void adjustReputation(String faction, int delta) {
        reputation.put(faction, getReputation(faction) + delta);
    }
}
