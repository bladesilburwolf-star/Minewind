package com.minewind;

import java.util.HashMap;
import java.util.Map;

public class QuestSystem {
    private final Map<String, Quest> quests = new HashMap<>();

    public void initialize() {
        quests.clear();
    }

    public void addQuest(Quest quest) {
        if (quest != null) {
            quests.put(quest.getId(), quest);
        }
    }

    public Quest getQuest(String id) {
        return quests.get(id);
    }

    public static class Quest {
        private final String id;
        private final String title;

        public Quest(String id, String title) {
            this.id = id;
            this.title = title;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }
    }
}
