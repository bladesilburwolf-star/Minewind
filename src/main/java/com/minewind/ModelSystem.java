package com.minewind;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestSystem.class);

    private final Map<String, Quest> activeQuests;
    private final List<String> completedQuests;
    private final List<String> failedQuests;
    private Quest currentQuest;

    public QuestSystem() {
        this.activeQuests = new HashMap<>();
        this.completedQuests = new ArrayList<>();
        this.failedQuests = new ArrayList<>();
        this.currentQuest = null;
    }

    public void onClientStart() {
        LOGGER.info("Quest system started");
    }

    public void onClientStop() {
        LOGGER.info("Quest system stopped");
    }

    public void tick(ClientPlayerEntity player) {
        for (Quest quest : activeQuests.values()) {
            quest.tick(player);
        }
    }

    public void startQuest(Quest quest) {
        if (quest == null) return;

        String questId = quest.getId();
        if (activeQuests.containsKey(questId)) {
            LOGGER.warn("Quest already active: {}", questId);
            return;
        }
        if (completedQuests.contains(questId)) {
            LOGGER.warn("Quest already completed: {}", questId);
            return;
        }

        activeQuests.put(questId, quest);
        currentQuest = quest;
        LOGGER.info("Started quest: {}", questId);
        quest.onStart();
    }

    public void completeQuest(String questId) {
        Quest quest = activeQuests.get(questId);
        if (quest == null) {
            LOGGER.warn("Cannot complete unknown quest: {}", questId);
            return;
        }

        quest.onComplete();
        activeQuests.remove(questId);
        completedQuests.add(questId);

        if (currentQuest != null && currentQuest.getId().equals(questId)) {
            currentQuest = null;
        }

        LOGGER.info("Completed quest: {}", questId);
    }

    public void failQuest(String questId) {
        Quest quest = activeQuests.get(questId);
        if (quest == null) {
            LOGGER.warn("Cannot fail unknown quest: {}", questId);
            return;
        }

        quest.onFail();
        activeQuests.remove(questId);
        failedQuests.add(questId);

        if (currentQuest != null && currentQuest.getId().equals(questId)) {
            currentQuest = null;
        }

        LOGGER.info("Failed quest: {}", questId);
    }

    public void abandonQuest(String questId) {
        Quest quest = activeQuests.get(questId);
        if (quest == null) {
            LOGGER.warn("Cannot abandon unknown quest: {}", questId);
            return;
        }

        quest.onAbandon();
        activeQuests.remove(questId);

        if (currentQuest != null && currentQuest.getId().equals(questId)) {
            currentQuest = null;
        }

        LOGGER.info("Abandoned quest: {}", questId);
    }

    public Quest getQuest(String questId) {
        return activeQuests.get(questId);
    }

    public Map<String, Quest> getActiveQuests() {
        return new HashMap<>(activeQuests);
    }

    public List<String> getCompletedQuests() {
        return new ArrayList<>(completedQuests);
    }

    public List<String> getFailedQuests() {
        return new ArrayList<>(failedQuests);
    }

    public Quest getCurrentQuest() {
        return currentQuest;
    }

    public void setCurrentQuest(String questId) {
        currentQuest = activeQuests.get(questId);
    }

    public boolean isQuestActive(String questId) {
        return activeQuests.containsKey(questId);
    }

    public boolean isQuestCompleted(String questId) {
        return completedQuests.contains(questId);
    }

    public boolean isQuestFailed(String questId) {
        return failedQuests.contains(questId);
    }

    public NbtCompound saveToNbt() {
        NbtCompound nbt = new NbtCompound();

        NbtList activeQuestsList = new NbtList();
        for (Quest quest : activeQuests.values()) {
            activeQuestsList.add(quest.saveToNbt());
        }
        nbt.put("active_quests", activeQuestsList);

        NbtList completedList = new NbtList();
        for (String questId : completedQuests) {
            completedList.add(NbtString.of(questId));
        }
        nbt.put("completed_quests", completedList);

        NbtList failedList = new NbtList();
        for (String questId : failedQuests) {
            failedList.add(NbtString.of(questId));
        }
        nbt.put("failed_quests", failedList);

        if (currentQuest != null) {
            nbt.putString("current_quest", currentQuest.getId());
        }

        return nbt;
    }

    public void loadFromNbt(NbtCompound nbt) {
        if (nbt.contains("completed_quests")) {
            NbtList completedList = nbt.getList("completed_quests", NbtElement.STRING_TYPE);
            for (int i = 0; i < completedList.size(); i++) {
                completedQuests.add(completedList.getString(i));
            }
        }

        if (nbt.contains("failed_quests")) {
            NbtList failedList = nbt.getList("failed_quests", NbtElement.STRING_TYPE);
            for (int i = 0; i < failedList.size(); i++) {
                failedQuests.add(failedList.getString(i));
            }
        }

        if (nbt.contains("current_quest")) {
            String currentQuestId = nbt.getString("current_quest");
            currentQuest = activeQuests.get(currentQuestId);
        }
    }

    public static class Quest {
        private final String id;
        private final String name;
        private final String description;
        private final String giver;
        private final QuestType type;
        private final List<QuestObjective> objectives;
        private int currentObjectiveIndex;
        private QuestStatus status;
        private final Map<String, Object> data;

        public Quest(String id, String name, String description, String giver, QuestType type) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.giver = giver;
            this.type = type;
            this.objectives = new ArrayList<>();
            this.currentObjectiveIndex = 0;
            this.status = QuestStatus.ACTIVE;
            this.data = new HashMap<>();
        }

        public void tick(ClientPlayerEntity player) {
            if (currentObjectiveIndex < objectives.size()) {
                QuestObjective objective = objectives.get(currentObjectiveIndex);
                if (objective.isComplete(player)) {
                    currentObjectiveIndex++;
                    objective.onComplete();

                    if (currentObjectiveIndex >= objectives.size()) {
                        status = QuestStatus.COMPLETE;
                    }
                }
            }
        }

        public void onStart() {
            LOGGER.debug("Quest started: {}", id);
        }

        public void onComplete() {
            LOGGER.debug("Quest completed: {}", id);
        }

        public void onFail() {
            LOGGER.debug("Quest failed: {}", id);
        }

        public void onAbandon() {
            LOGGER.debug("Quest abandoned: {}", id);
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getGiver() {
            return giver;
        }

        public QuestType getType() {
            return type;
        }

        public List<QuestObjective> getObjectives() {
            return new ArrayList<>(objectives);
        }

        public void addObjective(QuestObjective objective) {
            objectives.add(objective);
        }

        public QuestObjective getCurrentObjective() {
            if (currentObjectiveIndex < objectives.size()) {
                return objectives.get(currentObjectiveIndex);
            }
            return null;
        }

        public int getCurrentObjectiveIndex() {
            return currentObjectiveIndex;
        }

        public QuestStatus getStatus() {
            return status;
        }

        public void setStatus(QuestStatus status) {
            this.status = status;
        }

        public Object getData(String key) {
            return data.get(key);
        }

        public void setData(String key, Object value) {
            data.put(key, value);
        }

        public NbtCompound saveToNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("id", id);
            nbt.putString("name", name);
            nbt.putString("description", description);
            nbt.putString("giver", giver);
            nbt.putString("type", type.name());
            nbt.putInt("current_objective", currentObjectiveIndex);
            nbt.putString("status", status.name());

            NbtList objectivesList = new NbtList();
            for (QuestObjective objective : objectives) {
                objectivesList.add(objective.saveToNbt());
            }
            nbt.put("objectives", objectivesList);
            return nbt;
        }
    }

    public static class QuestObjective {
        private final String id;
        private final String description;
        private final ObjectiveType type;
        private boolean completed;
        private final Map<String, Object> data;

        public QuestObjective(String id, String description, ObjectiveType type) {
            this.id = id;
            this.description = description;
            this.type = type;
            this.completed = false;
            this.data = new HashMap<>();
        }

        public boolean isComplete(ClientPlayerEntity player) {
            return completed;
        }

        public void onComplete() {
            completed = true;
            LOGGER.debug("Objective completed: {}", id);
        }

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public ObjectiveType getType() {
            return type;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public Object getData(String key) {
            return data.get(key);
        }

        public void setData(String key, Object value) {
            data.put(key, value);
        }

        public NbtCompound saveToNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("id", id);
            nbt.putString("description", description);
            nbt.putString("type", type.name());
            nbt.putBoolean("completed", completed);
            return nbt;
        }
    }

    public enum QuestType {
        MAIN, SIDE, GUILD, FACTION, MISC
    }

    public enum QuestStatus {
        ACTIVE, COMPLETE, FAILED, ABANDONED
    }

    public enum ObjectiveType {
        KILL, COLLECT, TALK, GO_TO, USE, DELIVER, ESCORT, SURVIVE, DISCOVER
    }
}
