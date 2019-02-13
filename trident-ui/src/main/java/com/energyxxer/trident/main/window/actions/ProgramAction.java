package com.energyxxer.trident.main.window.actions;

import javax.swing.*;

public class ProgramAction {
    private String displayName;
    private String description;
    private KeyStroke shortcut;
    private Runnable action;

    public ProgramAction(String displayName, String description, KeyStroke shortcut, Runnable action) {
        this.displayName = displayName;
        this.description = description;
        this.shortcut = shortcut;
        this.action = action;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public KeyStroke getShortcut() {
        return shortcut;
    }

    public Runnable getAction() {
        return action;
    }

    public void perform() {
        if(action != null) {
            action.run();
        }
    }

    public char getShortcutChar() {
        if(shortcut == null) return 0;
        return Character.toLowerCase((char) shortcut.getKeyCode());
    }
}
