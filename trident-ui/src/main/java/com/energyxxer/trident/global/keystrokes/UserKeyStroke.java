package com.energyxxer.trident.global.keystrokes;

import com.energyxxer.trident.global.Preferences;
import com.energyxxer.xswing.KeyInputUtils;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;

import static com.energyxxer.trident.ui.editor.behavior.AdvancedEditor.isPlatformControlDown;
import static com.energyxxer.xswing.KeyInputUtils.*;

public class UserKeyStroke {
    private String name;
    private KeyStroke[] defaultStrokes;
    private KeyStroke[] strokes;
    private ArrayList<KeyStroke> newStrokes = null;
    private final String key;

    public UserKeyStroke(String key, KeyStroke... defaultStrokes) {
        this(null, key, defaultStrokes);
    }

    public UserKeyStroke(String name, String key, KeyStroke... defaultStrokes) {
        this.key = key;
        this.defaultStrokes = defaultStrokes;
        this.strokes = defaultStrokes;
        this.name = name;

        load();
    }

    private void load() {
        String savedValue = Preferences.get("keybind." + key);
        if(savedValue != null) {
            this.strokes = KeyMap.identifierToStrokes(savedValue);
        }
    }

    public void save() {
        Preferences.put("keybind." + key, KeyMap.strokesToIdentifier(strokes));
    }

    public boolean wasPerformed(KeyEvent e) {
        for(KeyStroke stroke : strokes) {
            if(
                    ((stroke.getKeyCode() == e.getKeyCode()) || (e.getID() == KeyEvent.KEY_TYPED && stroke.getKeyCode() == Character.toUpperCase(e.getKeyChar()))) &&
                            (!isControl(stroke) || isPlatformControlDown(e)) &&
                            (!isAlt(stroke) || e.isAltDown()) &&
                            (!isShift(stroke) || e.isShiftDown())
            ) return true;
        }
        return false;
    }

    public boolean wasPerformedExact(KeyEvent e) {
        for(KeyStroke stroke : strokes) {
            if(
                    ((stroke.getKeyCode() == e.getKeyCode()) || (e.getID() == KeyEvent.KEY_TYPED && stroke.getKeyCode() == Character.toUpperCase(e.getKeyChar()))) &&
                            (isControl(stroke) == isPlatformControlDown(e)) &&
                            (isAlt(stroke) == e.isAltDown()) &&
                            (isShift(stroke) == e.isShiftDown())
            ) return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void apply(InputMap inputMap, Object actionMapKey) {
        for(KeyStroke stroke : strokes) {
            inputMap.put(stroke, actionMapKey);
        }
    }

    public String getReadableKeyStroke() {
        StringBuilder sb = new StringBuilder();
        for(KeyStroke stroke : strokes) {
            sb.append(KeyInputUtils.getReadableKeyStroke(stroke));
            sb.append(" OR ");
        }
        if(strokes.length >= 1) {
            sb.setLength(sb.length() - " OR ".length());
        }
        return sb.toString();
    }

    public KeyStroke[] getAllStrokes() {
        return strokes;
    }

    public ArrayList<KeyStroke> getNewStrokes() {
        if(newStrokes == null) {
            newStrokes = new ArrayList<>(Arrays.asList(strokes));
        }
        return newStrokes;
    }

    public boolean newMatchesDefault() {
        if(defaultStrokes.length == getNewStrokes().size()) {
            for(int i = 0; i < defaultStrokes.length; i++) {
                if(!getNewStrokes().get(i).equals(defaultStrokes[i])) return false;
            }
            return true;
        }
        return false;
    }

    public void revertToDefault() {
        newStrokes = new ArrayList<>(Arrays.asList(defaultStrokes));
    }

    public void applyChanges() {
        strokes = newStrokes.toArray(new KeyStroke[0]);
        newStrokes = null;
    }

    public void discardChanges() {
        newStrokes = null;
    }

    public KeyStroke getFirstKeyStroke() {
        return strokes.length > 0 ? strokes[0] : null;
    }
}
