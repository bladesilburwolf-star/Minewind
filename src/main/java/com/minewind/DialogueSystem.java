package com.minewind;

import net.minecraft.client.network.ClientPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Minimal dialogue state system used by the core Morrowind systems.
 * Dialogue UI and branching content can build on this without requiring
 * Minecraft's vanilla villager/trading dialogue systems.
 */
public class DialogueSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger(DialogueSystem.class);

    private final List<DialogueLine> currentConversation = new ArrayList<>();
    private int currentLine;
    private boolean active;

    public void onClientStart() {
        LOGGER.info("Dialogue system started");
    }

    public void onClientStop() {
        endDialogue();
        LOGGER.info("Dialogue system stopped");
    }

    public void tick(ClientPlayerEntity player) {
        // Dialogue is event-driven; keep this hook for future timed dialogue.
    }

    public void startDialogue(List<DialogueLine> lines) {
        currentConversation.clear();
        if (lines != null) {
            currentConversation.addAll(lines);
        }
        currentLine = 0;
        active = !currentConversation.isEmpty();
    }

    public void endDialogue() {
        currentConversation.clear();
        currentLine = 0;
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public DialogueLine getCurrentLine() {
        return active && currentLine < currentConversation.size()
            ? currentConversation.get(currentLine)
            : null;
    }

    public boolean advance() {
        if (!active) return false;
        currentLine++;
        if (currentLine >= currentConversation.size()) {
            endDialogue();
            return false;
        }
        return true;
    }

    public List<DialogueLine> getCurrentConversation() {
        return Collections.unmodifiableList(currentConversation);
    }

    public record DialogueLine(String speaker, String text) {
        public DialogueLine {
            speaker = speaker == null ? "" : speaker;
            text = text == null ? "" : text;
        }
    }
}
