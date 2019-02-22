package com.energyxxer.xswing;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KeyInputUtils {
    private static int doublePressSpeed = 300;
    private static HashMap<Integer, ArrayList<KeyEvent>> timeline = new HashMap<>();

    public static boolean isDoublePress(KeyEvent ke) {
        addEntry(ke);
        List<KeyEvent> lastThree = timeline.get(ke.getKeyCode());
        if(lastThree.size() > 3) {
            lastThree = lastThree.subList(lastThree.size()-3, lastThree.size());
        } else if(lastThree.size() < 3) {
            return false;
        }
        boolean returnValue = lastThree.get(0).getID() == KeyEvent.KEY_PRESSED
                && lastThree.get(1).getID() == KeyEvent.KEY_RELEASED
                && lastThree.get(2).getID() == KeyEvent.KEY_PRESSED;
        if(returnValue) timeline.get(ke.getKeyCode()).clear();
        return returnValue;
    }

    public static void interruptDoublePresses() {
        for(ArrayList<KeyEvent> list : timeline.values()) {
            list.clear();
        }
    }

    public static void markRelease(KeyEvent ke) {
        addEntry(ke);
    }

    private static void addEntry(KeyEvent ke) {
        if(!timeline.containsKey(ke.getKeyCode())) timeline.put(ke.getKeyCode(), new ArrayList<>());
        timeline.get(ke.getKeyCode()).add(ke);
        removeOldEntries(ke);
    }

    private static void removeOldEntries(KeyEvent ke) {
        long current = ke.getWhen();
        for(ArrayList<KeyEvent> list : timeline.values()) {
            list.removeIf(k -> k.getWhen() < current - doublePressSpeed);
        }
    }

    public static String getReadableKeyStroke(KeyStroke keyStroke) {
        String modifiersText = KeyEvent.getKeyModifiersText(keyStroke.getModifiers());
        if(!modifiersText.isEmpty()) modifiersText += "+";
        return modifiersText + KeyEvent.getKeyText(keyStroke.getKeyCode());
    }
}
