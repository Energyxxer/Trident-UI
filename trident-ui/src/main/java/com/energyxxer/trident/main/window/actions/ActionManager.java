package com.energyxxer.trident.main.window.actions;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.global.Resources;
import com.energyxxer.trident.global.TabManager;
import com.energyxxer.trident.main.window.sections.search_path.SearchPathDialog;
import com.energyxxer.trident.ui.Tab;
import com.energyxxer.trident.ui.commodoreresources.DefinitionUpdateProcess;
import com.energyxxer.trident.ui.editor.TridentEditorModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

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
                "Reload UI Resources", "Reload themes, definition packs and feature maps from disk",
                KeyStroke.getKeyStroke(KeyEvent.VK_T, getPlatformControlMask()),
                Resources::load)
        );
        actions.add(new ProgramAction(
                "Find in Path", "Find all occurrences of a query in a folder or project",
                KeyStroke.getKeyStroke(KeyEvent.VK_H, getPlatformControlMask()),
                SearchPathDialog.INSTANCE::reveal)
        );
        actions.add(new ProgramAction(
                "Temporary Indexing", "Summarize active project",
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.ALT_DOWN_MASK),
                Commons::indexActive)
        );
        actions.add(new ProgramAction(
                "Check for updates", "Check for definition updates",
                KeyStroke.getKeyStroke(KeyEvent.VK_U, getPlatformControlMask()),
                DefinitionUpdateProcess::tryUpdate)
        );
    }

    public static int getPlatformControlMask() {
        return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    }

    private static long ctrlWasDown = -1L;
    private static long altWasDown = -1L;
    private static boolean altGraphCaught = false;

    public static void setup(JPanel panel) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((e) -> {
            if(e.getID() == KeyEvent.KEY_PRESSED) {
                if(altGraphCaught) {
                    return true;
                }
                if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    ctrlWasDown = e.getWhen();
                } else if(e.getKeyCode() == KeyEvent.VK_ALT) {
                    altWasDown = e.getWhen();
                }
                if(e.isControlDown() && e.isAltDown() && altWasDown == e.getWhen() && altWasDown - ctrlWasDown >= 0 && altWasDown - ctrlWasDown <= 1 && ctrlWasDown > 0) {
                    altGraphCaught = true;
                    return true;
                }
            } else if(altGraphCaught && e.getID() == KeyEvent.KEY_RELEASED) {
                if(e.getKeyCode() == KeyEvent.VK_CONTROL || e.getKeyCode() == KeyEvent.VK_ALT) {
                    altGraphCaught = false;
                }
            }
            for(ProgramAction action : actions) {
                if(action.getShortcut() != null) {
                    if(
                            (e.getKeyCode() == action.getShortcut().getKeyCode() || //Check for matching character in keyPressed and keyReleased events
                                    (Character.toUpperCase(e.getKeyChar()) == action.getShortcut().getKeyCode() && e.getID() == KeyEvent.KEY_TYPED))  //Check for matching character in keyTyped events
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

    public static Collection<ProgramAction> getAllActions() {
        return actions;
    }
}
