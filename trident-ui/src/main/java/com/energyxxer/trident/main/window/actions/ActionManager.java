package com.energyxxer.trident.main.window.actions;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.global.Resources;
import com.energyxxer.trident.global.keystrokes.KeyMap;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.main.window.sections.search_path.SearchPathDialog;
import com.energyxxer.trident.ui.Tab;
import com.energyxxer.trident.ui.commodoreresources.DefinitionUpdateProcess;
import com.energyxxer.trident.ui.editor.TridentEditorModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

public class ActionManager {
    private static ArrayList<ProgramAction> actions = new ArrayList<>();

    static {
        actions.add(new ProgramAction(
                "Compile", "Compile the active project",
                KeyMap.COMPILE,
                Commons::compileActive)
        );
        actions.add(new ProgramAction(
                "Close Active Tab", "Close the tab currently visible",
                KeyMap.CLOSE_TAB,
                TridentWindow.tabManager::closeSelectedTab)
        );
        actions.add(new ProgramAction(
                "Save Active Tab", "Save the tab currently visible",
                KeyMap.TAB_SAVE,
                () -> {
                    Tab st = TridentWindow.tabManager.getSelectedTab();
                    if(st != null) st.save();
                })
        );
        actions.add(new ProgramAction(
                "Reload from Disk", "Reload the current file from disk",
                KeyMap.EDITOR_RELOAD,
                () -> {
                    Tab st = TridentWindow.tabManager.getSelectedTab();
                    if(st != null) {
                        if(st.module instanceof TridentEditorModule) {
                            ((TridentEditorModule) st.module).reloadFromDisk();
                        }
                    }
                })
        );
        actions.add(new ProgramAction(
                "Reload UI Resources", "Reload themes, definition packs and feature maps from disk",
                KeyMap.THEME_RELOAD,
                Resources::load)
        );
        actions.add(new ProgramAction(
                "Find in Path", "Find all occurrences of a query in a folder or project",
                KeyMap.FIND_IN_PATH,
                SearchPathDialog.INSTANCE::reveal)
        );
        actions.add(new ProgramAction(
                "Check for updates", "Check for definition updates",
                null,
                DefinitionUpdateProcess::tryUpdate)
        );
    }

    private static long ctrlWasDown = -1L;
    private static long altWasDown = -1L;
    private static boolean altGraphCaught = false;

    public static void setup(JPanel panel) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((e) -> {
            if(e.getID() == KeyEvent.KEY_PRESSED) {
                if(!altGraphCaught) {
                    if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
                        ctrlWasDown = e.getWhen();
                    } else if(e.getKeyCode() == KeyEvent.VK_ALT) {
                        altWasDown = e.getWhen();
                    }
                    if(e.isControlDown() && e.isAltDown() && altWasDown == e.getWhen() && altWasDown - ctrlWasDown >= 0 && altWasDown - ctrlWasDown <= 1 && ctrlWasDown > 0) {
                        altGraphCaught = true;
                    }
                }
            } else if(altGraphCaught && e.getID() == KeyEvent.KEY_RELEASED) {
                if(e.getKeyCode() == KeyEvent.VK_CONTROL || e.getKeyCode() == KeyEvent.VK_ALT) {
                    altGraphCaught = false;
                }
            }
            if(!altGraphCaught) {
                for(ProgramAction action : actions) {
                    if(action.getShortcut() != null) {
                        if(action.getShortcut().wasPerformedExact(e)) {
                            if(e.getID() == KeyEvent.KEY_PRESSED) {
                                action.perform();
                            }
                            e.consume();
                            return true;
                        }
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
