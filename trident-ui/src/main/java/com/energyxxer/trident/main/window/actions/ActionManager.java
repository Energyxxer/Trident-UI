package com.energyxxer.trident.main.window.actions;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.global.Resources;
import com.energyxxer.trident.global.TabManager;
import com.energyxxer.trident.ui.Tab;
import com.energyxxer.trident.ui.editor.TridentEditorModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

@SuppressWarnings("MagicConstant")
public class ActionManager {
    private static ArrayList<ProgramAction> actions = new ArrayList<>();

    static {
        actions.add(new ProgramAction(
                "Compile", "Compile the active project",
                KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
                Commons::compileActive)
        );
        actions.add(new ProgramAction(
                "Close Active Tab", "Close the tab currently visible",
                KeyStroke.getKeyStroke(KeyEvent.VK_W, getPlatformControlMask()),
                TabManager::closeSelectedTab)
        );
        actions.add(new ProgramAction(
                "Save Active Tab", "Save the tab currently visible",
                KeyStroke.getKeyStroke(KeyEvent.VK_S, getPlatformControlMask()),
                () -> {
                    Tab st = TabManager.getSelectedTab();
                    if(st != null) st.save();
                })
        );
        actions.add(new ProgramAction(
                "Reload from Disk", "Reload the current file from disk",
                KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
                () -> {
                    Tab st = TabManager.getSelectedTab();
                    if(st != null) {
                        if(st.module instanceof TridentEditorModule) {
                            ((TridentEditorModule) st.module).reloadFromDisk();
                        }
                    }
                })
        );
        actions.add(new ProgramAction(
                "Reload GUI Resources", "Reload GUI and syntax themes from disk",
                KeyStroke.getKeyStroke(KeyEvent.VK_T, getPlatformControlMask()),
                Resources::load)
        );
    }

    public static int getPlatformControlMask() {
        return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    }

    public static void setup(JPanel panel) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((e) -> {
            for(ProgramAction action : actions) {
                if(action.getShortcut() != null) {
                    if(
                            (e.getKeyCode() == action.getShortcut().getKeyCode() || //Check for matching character in keyPressed and keyReleased events
                                    Character.toLowerCase(e.getKeyChar()) == action.getShortcutChar())  //Check for matching character in keyTyped events
                                    &&
                            (((e.getModifiersEx() | e.getModifiers()) & action.getShortcut().getModifiers()) == action.getShortcut().getModifiers()) //Check for matching modifiers
                    ) {
                        if(e.getID() == KeyEvent.KEY_PRESSED) {
                            action.perform();
                        }
                        e.consume();
                        return true;
                    }
                }
            }
            return false;
        });
    }
}
