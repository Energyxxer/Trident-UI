package com.energyxxer.trident.global.keystrokes;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.energyxxer.xswing.KeyInputUtils.*;

public class KeyMap {

    public static final UserKeyStroke COPY = new UserKeyStroke("copy", identifierToStrokes("c+C"));
    public static final UserKeyStroke CUT = new UserKeyStroke("cut", identifierToStrokes("c+X"));
    public static final UserKeyStroke PASTE = new UserKeyStroke("paste", identifierToStrokes("c+V"));

    public static final UserKeyStroke UNDO = new UserKeyStroke("undo", identifierToStrokes("c+Z"));
    public static final UserKeyStroke REDO = new UserKeyStroke("redo", identifierToStrokes("c+Y"));

    public static final UserKeyStroke SUGGESTION_SELECT = new UserKeyStroke("Expand Editor Suggestion", "editor.suggestion.select", identifierToStrokes("" + KeyEvent.VK_TAB));

    public static final UserKeyStroke TEXT_SELECT_ALL = new UserKeyStroke("Select All Text", "text.select_all", identifierToStrokes("c+A"));
    public static final UserKeyStroke TEXT_MOVE_LINE_UP = new UserKeyStroke("Move Line Up", "text.move_line_up", identifierToStrokes("a+" + KeyEvent.VK_UP));
    public static final UserKeyStroke TEXT_MOVE_LINE_DOWN = new UserKeyStroke("Move Line Down", "text.move_line_down", identifierToStrokes("a+" + KeyEvent.VK_DOWN));

    public static final UserKeyStroke FIND_NEXT = new UserKeyStroke("find.next", identifierToStrokes(KeyEvent.VK_ENTER + ";" + KeyEvent.VK_F3));
    public static final UserKeyStroke FIND_PREVIOUS = new UserKeyStroke("find.previous", identifierToStrokes("s+" + KeyEvent.VK_ENTER + ";s+" + KeyEvent.VK_F3));

    private static final List<UserKeyStroke> allKeyStrokes;

    static {
        List<UserKeyStroke> keystrokes = new ArrayList<>();
        Field[] fields = KeyMap.class.getFields();
        for(Field field : fields) {
            try {
                if(field.getType() == UserKeyStroke.class) {
                    UserKeyStroke stroke = (UserKeyStroke) field.get(null);
                    keystrokes.add(stroke);
                    if(stroke.getName() == null) {
                        StringBuilder humanReadableName = new StringBuilder();
                        boolean uppercase = true;
                        for(char c : field.getName().toCharArray()) {
                            if(!uppercase) c = Character.toLowerCase(c);
                            uppercase = false;
                            if(c == '_') {
                                c = ' ';
                                uppercase = true;
                            }
                            humanReadableName.append(c);
                        }
                        stroke.setName(humanReadableName.toString());
                    }
                }
            } catch (IllegalAccessException x) {
                x.printStackTrace();
            }
        }
        keystrokes.sort(Comparator.comparing(UserKeyStroke::getName));
        allKeyStrokes = keystrokes;
    }


    public static String strokeToIdentifier(KeyStroke stroke) {
        StringBuilder sb = new StringBuilder();
        if(isControl(stroke)) {
            sb.append("c");
        }
        if(isAlt(stroke)) {
            sb.append("a");
        }
        if(isShift(stroke)) {
            sb.append("s");
        }
        sb.append("+");
        sb.append(stroke.getKeyCode());
        return sb.toString();
    }

    public static String strokesToIdentifier(KeyStroke[] strokes) {
        StringBuilder sb = new StringBuilder();
        for(KeyStroke stroke : strokes) {
            sb.append(strokeToIdentifier(stroke));
            sb.append(';');
        }
        if(strokes.length > 0) sb.setLength(sb.length()-1);
        return sb.toString();
    }

    public static KeyStroke identifierToStroke(String id) {
        int modifiers = 0;
        if(id.contains("c")) modifiers |= getPlatformControlMask();
        if(id.contains("a")) modifiers |= KeyEvent.ALT_DOWN_MASK;
        if(id.contains("s")) modifiers |= KeyEvent.SHIFT_DOWN_MASK;
        String keyCodeSegment = id.substring(id.indexOf("+")+1);
        int keyCode;
        if(keyCodeSegment.charAt(0) >= '1' && keyCodeSegment.charAt(0) <= '9') {
            keyCode = Integer.parseInt(keyCodeSegment);
        } else {
            keyCode = keyCodeSegment.toUpperCase().charAt(0);
        }
        //noinspection MagicConstant
        return KeyStroke.getKeyStroke(keyCode, modifiers);
    }

    public static KeyStroke[] identifierToStrokes(String ids) {
        if(ids.isEmpty()) return new KeyStroke[0];
        String[] splits = ids.split(";");
        KeyStroke[] strokes = new KeyStroke[splits.length];
        for(int i = 0; i < splits.length; i++) {
            strokes[i] = identifierToStroke(splits[i]);
        }
        return strokes;
    }

    public static List<UserKeyStroke> getAll() {
        return allKeyStrokes;
    }

    public static UserKeyStroke requestMapping(String key, KeyStroke... defaultStrokes) {
        UserKeyStroke stroke = new UserKeyStroke(key, defaultStrokes);
        allKeyStrokes.add(stroke);
        return stroke;
    }

    public static void sortMappings() {
        allKeyStrokes.sort(Comparator.comparing(UserKeyStroke::getName));
    }
}
