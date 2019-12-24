package com.energyxxer.trident.global.keystrokes;

import com.energyxxer.trident.global.Preferences;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;

public class UserKeyBind {
    public enum Special {
        DOUBLE_SHIFT("Shift+Shift","*ss");

        final String humanReadableKeystroke;
        final String identifier;

        Special(String humanReadableKeystroke, String identifier) {
            this.humanReadableKeystroke = humanReadableKeystroke;
            this.identifier = identifier;
        }

        public String getHumanReadableKeystroke() {
            return humanReadableKeystroke;
        }

        public String getIdentifier() {
            return identifier;
        }
    }

    private String name;
    private UserMapping[] defaultMappings;
    private UserMapping[] mappings;
    private ArrayList<UserMapping> newMappings = null;
    private final String key;
    private String groupName = "Miscellaneous";

    public UserKeyBind(String key, UserMapping... defaultMappings) {
        this(null, key, defaultMappings);
    }

    public UserKeyBind(String name, String key, UserMapping... defaultMappings) {
        this.key = key;
        this.defaultMappings = defaultMappings;
        this.mappings = defaultMappings;
        this.name = name;

        load();
    }

    private void load() {
        String savedValue = Preferences.get("keybind." + key);
        if(savedValue != null) {
            this.mappings = KeyMap.identifierToStrokes(savedValue);
        }
    }

    public void save() {
        Preferences.put("keybind." + key, KeyMap.strokesToIdentifier(mappings));
    }

    public boolean wasPerformed(KeyEvent e) {
        for(UserMapping stroke : mappings) {
            if(stroke.wasPerformed(e)) return true;
        }
        return false;
    }

    public boolean wasPerformedExact(KeyEvent e) {
        for(UserMapping stroke : mappings) {
            if(stroke.wasPerformedExact(e)) return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public UserKeyBind setName(String name) {
        this.name = name;
        return this;
    }

    public void apply(InputMap inputMap, Object actionMapKey) {
        for(UserMapping stroke : mappings) {
            stroke.apply(inputMap, actionMapKey);
        }
    }

    public String getReadableKeyStroke() {
        StringBuilder sb = new StringBuilder();
        for(UserMapping stroke : mappings) {
            sb.append(stroke.getHumanReadableName());
            sb.append(" OR ");
        }
        if(mappings.length >= 1) {
            sb.setLength(sb.length() - " OR ".length());
        }
        return sb.toString();
    }

    public UserMapping[] getAllMappings() {
        return mappings;
    }

    public ArrayList<UserMapping> getNewMappings() {
        if(newMappings == null) {
            newMappings = new ArrayList<>(Arrays.asList(mappings));
        }
        return newMappings;
    }

    public boolean newMatchesDefault() {
        if(defaultMappings.length == getNewMappings().size()) {
            for(int i = 0; i < defaultMappings.length; i++) {
                if(!getNewMappings().get(i).equals(defaultMappings[i])) return false;
            }
            return true;
        }
        return false;
    }

    public void revertToDefault() {
        newMappings = new ArrayList<>(Arrays.asList(defaultMappings));
    }

    public void applyChanges() {
        if(newMappings != null) {
            mappings = newMappings.toArray(new UserMapping[0]);
            newMappings = null;
        }
    }

    public void discardChanges() {
        newMappings = null;
    }

    public UserMapping getFirstMapping() {
        return mappings.length > 0 ? mappings[0] : null;
    }

    public String getGroupName() {
        return groupName;
    }

    public UserKeyBind setGroupName(String groupName) {
        this.groupName = groupName;
        return this;
    }
}
