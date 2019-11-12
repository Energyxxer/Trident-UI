package com.energyxxer.trident.global.keystrokes;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static com.energyxxer.xswing.KeyInputUtils.*;

public class KeyMap {

    public static final UserKeyStroke COPY = new UserKeyStroke("copy", identifierToStrokes("c+C"));
    public static final UserKeyStroke CUT = new UserKeyStroke("cut", identifierToStrokes("c+X"));
    public static final UserKeyStroke PASTE = new UserKeyStroke("paste", identifierToStrokes("c+V"));

    public static final UserKeyStroke UNDO = new UserKeyStroke("undo", identifierToStrokes("c+Z"));
    public static final UserKeyStroke REDO = new UserKeyStroke("redo", identifierToStrokes("c+Y"));

    public static final UserKeyStroke COMPILE = new UserKeyStroke("compile", identifierToStrokes("as+X"));
    public static final UserKeyStroke CLOSE_TAB = new UserKeyStroke("tab.close", identifierToStrokes("c+W"));
    public static final UserKeyStroke CLOSE_ALL_TABS = new UserKeyStroke("tab.close_all", identifierToStrokes("cs+W"));
    public static final UserKeyStroke TAB_SAVE = new UserKeyStroke("tab.save", identifierToStrokes("c+S"));
    public static final UserKeyStroke TAB_SAVE_AS = new UserKeyStroke("tab.save_as", identifierToStrokes("cs+S"));
    public static final UserKeyStroke TAB_SAVE_ALL = new UserKeyStroke("tab.save_all", identifierToStrokes("sa+S"));

    public static final UserKeyStroke EDITOR_FIND = new UserKeyStroke("editor.find", identifierToStrokes("c+F"));

    public static final UserKeyStroke EDITOR_RELOAD = new UserKeyStroke("editor.reload", identifierToStrokes("" + KeyEvent.VK_F5));

    public static final UserKeyStroke SUGGESTION_SELECT = new UserKeyStroke("editor.suggestion.select", identifierToStrokes("" + KeyEvent.VK_TAB));

    public static final UserKeyStroke THEME_RELOAD = new UserKeyStroke("theme.reload", identifierToStrokes("c+T"));
    public static final UserKeyStroke FIND_IN_PATH = new UserKeyStroke("find_in_path", identifierToStrokes("c+H"));

    public static final UserKeyStroke TEXT_SELECT_ALL = new UserKeyStroke("text.select_all", identifierToStrokes("c+A"));
    public static final UserKeyStroke TEXT_MOVE_LINE_UP = new UserKeyStroke("text.move_line_up", identifierToStrokes("a+" + KeyEvent.VK_UP));
    public static final UserKeyStroke TEXT_MOVE_LINE_DOWN = new UserKeyStroke("text.move_line_down", identifierToStrokes("a+" + KeyEvent.VK_DOWN));

    public static final UserKeyStroke FIND_NEXT = new UserKeyStroke("find.next", identifierToStrokes(KeyEvent.VK_ENTER + ";" + KeyEvent.VK_F3));
    public static final UserKeyStroke FIND_PREVIOUS = new UserKeyStroke("find.previous", identifierToStrokes("s+" + KeyEvent.VK_ENTER + ";s+" + KeyEvent.VK_F3));






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
        String[] splits = ids.split(";");
        KeyStroke[] strokes = new KeyStroke[splits.length];
        for(int i = 0; i < splits.length; i++) {
            strokes[i] = identifierToStroke(splits[i]);
        }
        return strokes;
    }
}
