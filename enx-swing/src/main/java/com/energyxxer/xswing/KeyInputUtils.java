package com.energyxxer.xswing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
                && lastThree.get(0).getWhen() >= ke.getWhen() - doublePressSpeed
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
        ArrayList<KeyEvent> events = timeline.get(ke.getKeyCode());
        if(events.isEmpty() || ke.getID() != events.get(events.size()-1).getID()) {
            events.add(ke);
        }
        removeOldEntries(ke);
    }

    private static void removeOldEntries(KeyEvent ke) {
        long current = ke.getWhen();
        for(ArrayList<KeyEvent> list : timeline.values()) {

            boolean foundFirstPressed = false;
            Iterator<KeyEvent> it = list.iterator();
            while(it.hasNext()) {
                KeyEvent k = it.next();

                if(!foundFirstPressed && k.getID() == KeyEvent.KEY_PRESSED) {
                    foundFirstPressed = true;
                } else if(!foundFirstPressed || k.getWhen() < current - doublePressSpeed) {
                    it.remove();
                }
            }
        }
    }

    public static String getReadableKeyStroke(KeyStroke stroke) {
        StringBuilder sb = new StringBuilder();
        if(isControl(stroke)) {
            sb.append(System.getProperty("os.name").contains("mac") ? "Command" : "Control");
            sb.append('+');
        }
        if(isAlt(stroke)) {
            sb.append("Alt+");
        }
        if(isShift(stroke)) {
            sb.append("Shift+");
        }
        sb.append(KeyEvent.getKeyText(stroke.getKeyCode()));
        return sb.toString();
    }

    public static int getPlatformControlMask() {
        return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    }

    public static boolean isControl(KeyStroke k) {
        return (k.getModifiers() & getPlatformControlMask()) != 0;
    }

    public static boolean isAlt(KeyStroke k) {
        return (k.getModifiers() & KeyEvent.ALT_DOWN_MASK) != 0;
    }

    public static boolean isShift(KeyStroke k) {
        return (k.getModifiers() & KeyEvent.SHIFT_DOWN_MASK) != 0;
    }
}
