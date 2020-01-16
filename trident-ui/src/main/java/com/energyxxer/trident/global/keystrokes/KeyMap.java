package com.energyxxer.trident.global.keystrokes;

import com.energyxxer.util.logger.Debug;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.energyxxer.xswing.KeyInputUtils.*;

public class KeyMap {

    public static final UserKeyBind COPY = new UserKeyBind("copy", identifierToStrokes("c+C")).setGroupName("Editor");
    public static final UserKeyBind CUT = new UserKeyBind("cut", identifierToStrokes("c+X")).setGroupName("Editor");
    public static final UserKeyBind PASTE = new UserKeyBind("paste", identifierToStrokes("c+V")).setGroupName("Editor");

    public static final UserKeyBind UNDO = new UserKeyBind("undo", identifierToStrokes("c+Z")).setGroupName("Editor");
    public static final UserKeyBind REDO = new UserKeyBind("redo", identifierToStrokes("c+Y")).setGroupName("Editor");
    public static final UserKeyBind COMMENT = new UserKeyBind("comment", identifierToStrokes("c+"+KeyEvent.VK_SLASH)).setGroupName("Editor");

    public static final UserKeyBind SUGGESTION_SELECT = new UserKeyBind("Expand Suggestion", "editor.suggestion.select", identifierToStrokes("" + KeyEvent.VK_TAB)).setGroupName("Editor");

    public static final UserKeyBind TEXT_SELECT_ALL = new UserKeyBind("Select All Text", "text.select_all", identifierToStrokes("c+A")).setGroupName("Editor");
    public static final UserKeyBind TEXT_MOVE_LINE_UP = new UserKeyBind("Move Line Up", "text.move_line_up", identifierToStrokes("a+" + KeyEvent.VK_UP)).setGroupName("Editor");
    public static final UserKeyBind TEXT_MOVE_LINE_DOWN = new UserKeyBind("Move Line Down", "text.move_line_down", identifierToStrokes("a+" + KeyEvent.VK_DOWN)).setGroupName("Editor");
    public static final UserKeyBind TEXT_DELETE_LINE = new UserKeyBind("text.delete_line", identifierToStrokes("c+D")).setGroupName("Editor");
    public static final UserKeyBind TEXT_DUPLICATE_LINE = new UserKeyBind("text.duplicate_line", identifierToStrokes("ca+"+KeyEvent.VK_DOWN)).setGroupName("Editor");

    public static final UserKeyBind FIND_NEXT = new UserKeyBind("find.next", identifierToStrokes(KeyEvent.VK_ENTER + ";" + KeyEvent.VK_F3)).setGroupName("Editor");
    public static final UserKeyBind FIND_PREVIOUS = new UserKeyBind("find.previous", identifierToStrokes("s+" + KeyEvent.VK_ENTER + ";s+" + KeyEvent.VK_F3)).setGroupName("Editor");

    private static final List<UserKeyBind> allKeyBinds;

    static {
        List<UserKeyBind> keystrokes = new ArrayList<>();
        Field[] fields = KeyMap.class.getFields();
        for(Field field : fields) {
            try {
                if(field.getType() == UserKeyBind.class) {
                    UserKeyBind stroke = (UserKeyBind) field.get(null);
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
        //keystrokes.sort(Comparator.comparing(UserKeyBind::getName));
        allKeyBinds = keystrokes;
    }


    public static String strokeToIdentifier(UserMapping mapping) {
        if(mapping instanceof SpecialMapping) {
            return ((SpecialMapping) mapping).getSpecial().identifier;
        } else {
            KeyStroke stroke = ((SimpleMapping) mapping).getKeyStroke();
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
    }

    public static String strokesToIdentifier(UserMapping[] strokes) {
        StringBuilder sb = new StringBuilder();
        for(UserMapping stroke : strokes) {
            sb.append(strokeToIdentifier(stroke));
            sb.append(';');
        }
        if(strokes.length > 0) sb.setLength(sb.length()-1);
        return sb.toString();
    }

    public static UserMapping identifierToStroke(String id) {
        if(id.startsWith("*")) {
            for(UserKeyBind.Special special : UserKeyBind.Special.values()) {
                if(special.getIdentifier().equals(id)) return new SpecialMapping(special);
            }
            Debug.log("Couldn't find special mapping object for id '" + id + "'");
            return new SpecialMapping(UserKeyBind.Special.DOUBLE_SHIFT);
        } else {
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
            return new SimpleMapping(KeyStroke.getKeyStroke(keyCode, modifiers));
        }
    }

    public static UserMapping[] identifierToStrokes(String ids) {
        if(ids.isEmpty()) return new UserMapping[0];
        String[] splits = ids.split(";");
        UserMapping[] strokes = new UserMapping[splits.length];
        for(int i = 0; i < splits.length; i++) {
            strokes[i] = identifierToStroke(splits[i]);
        }
        return strokes;
    }

    public static List<UserKeyBind> getAll() {
        return allKeyBinds;
    }

    public static UserKeyBind getByKey(String key) {
        for(UserKeyBind kb : allKeyBinds) {
            if(key.equals(kb.getKey())) {
                return kb;
            }
        }
        return null;
    }

    public static UserKeyBind requestMapping(String key, UserMapping... defaultMappings) {
        UserKeyBind keyBind = new UserKeyBind(key, defaultMappings);
        allKeyBinds.add(keyBind);
        return keyBind;
    }

    public static void sortMappings() {
        allKeyBinds.sort(Comparator.comparing(UserKeyBind::getName));
    }
}
