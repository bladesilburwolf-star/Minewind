package com.minewind;

import java.util.ArrayList;
import java.util.List;

public class DialogueSystem {
    private final List<String> dialogueLines = new ArrayList<>();
    private int currentIndex;

    public void initialize() {
        dialogueLines.clear();
        dialogueLines.add("Welcome, traveler.");
        dialogueLines.add("The ashlands are restless.");
        dialogueLines.add("The old gods still whisper in the stone.");
        currentIndex = 0;
    }

    public void addLine(String line) {
        dialogueLines.add(line);
    }

    public String getCurrentLine() {
        if (dialogueLines.isEmpty()) {
            return "";
        }
        return dialogueLines.get(currentIndex % dialogueLines.size());
    }

    public String nextLine() {
        if (dialogueLines.isEmpty()) {
            return "";
        }
        currentIndex = (currentIndex + 1) % dialogueLines.size();
        return getCurrentLine();
    }
}
