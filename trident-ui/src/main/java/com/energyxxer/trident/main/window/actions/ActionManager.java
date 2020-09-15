package com.energyxxer.trident.main.window.actions;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.global.Resources;
import com.energyxxer.trident.global.keystrokes.KeyMap;
import com.energyxxer.trident.global.keystrokes.SpecialMapping;
import com.energyxxer.trident.global.keystrokes.UserKeyBind;
import com.energyxxer.trident.global.keystrokes.UserMapping;
import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.global.temp.projects.TridentProject;
import com.energyxxer.trident.langinterface.ProjectType;
import com.energyxxer.trident.main.WorkspaceDialog;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.main.window.sections.AboutPane;
import com.energyxxer.trident.main.window.sections.quick_find.QuickFindDialog;
import com.energyxxer.trident.main.window.sections.search_path.SearchPathDialog;
import com.energyxxer.trident.main.window.sections.tools.ConsoleBoard;
import com.energyxxer.trident.ui.Tab;
import com.energyxxer.trident.ui.commodoreresources.DefinitionUpdateProcess;
import com.energyxxer.trident.ui.commodoreresources.TridentPlugins;
import com.energyxxer.trident.ui.common.ProgramUpdateProcess;
import com.energyxxer.trident.ui.dialogs.KeyStrokeDialog;
import com.energyxxer.trident.ui.dialogs.file_dialogs.ProjectDialog;
import com.energyxxer.trident.ui.dialogs.settings.Settings;
import com.energyxxer.util.logger.Debug;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static com.energyxxer.trident.global.keystrokes.KeyMap.identifierToStrokes;

public class ActionManager {
    private static LinkedHashMap<String, ProgramAction> actions = new LinkedHashMap<>();

    static {
        actions.put("COMPILE",
                new ProgramAction(
                        "Compile", "Compile the active project", 
                        KeyMap.requestMapping("compile", identifierToStrokes("as+X")).setGroupName("Projects"),
                        Commons::compileActive
                ).setIconKey("export")
        );
        actions.put("CLOSE_TAB",
                new ProgramAction(
                    "Close Active Tab", "Close the tab currently visible",
                    KeyMap.requestMapping("tab.close", identifierToStrokes("c+W")).setGroupName("Tabs"),
                    TridentWindow.tabManager::closeSelectedTab
                )
        );
        actions.put("CLOSE_ALL_TABS",
                new ProgramAction(
                        "Close All Tabs", "Close all tabs",
                        KeyMap.requestMapping("tab.close_all", identifierToStrokes("cs+W")).setGroupName("Tabs"),
                        () -> TridentWindow.tabManager.closeAllTabs(false)
                )
        );
        actions.put("CLOSE_ALL_TABS_FOR_PROJECT",
                new ProgramAction(
                        "Close All Tabs For Project", "Close all tabs",
                        KeyMap.requestMapping("tab.close_project").setGroupName("Tabs"),
                        () -> TridentWindow.tabManager.closeAllTabsForProject(Commons.getActiveProject())
                )
        );
        actions.put("SAVE",
                new ProgramAction(
                "Save Active Tab", "Save the tab currently visible",
                    KeyMap.requestMapping("tab.save", identifierToStrokes("c+S")).setGroupName("Tabs"),
                    () -> {
                        Tab st = TridentWindow.tabManager.getSelectedTab();
                        if(st != null) st.save();
                    }
                ).setIconKey("save")
        );
        actions.put("SAVE_ALL",
                new ProgramAction(
                    "Save All Tabs", "Save all open tabs",
                    KeyMap.requestMapping("tab.save_all", identifierToStrokes("ca+S")).setGroupName("Tabs"),
                    () -> {
                        for(Tab st : TridentWindow.tabManager.openTabs) {
                            st.save();
                        }
                        Debug.log("All tabs saved");
                    }
                ).setIconKey("save_all")
        );
        actions.put("RELOAD_FROM_DISK",
                new ProgramAction(
                    "Reload from Disk", "Reload the current file from disk",
                    KeyMap.requestMapping("editor.reload").setGroupName("Editor"),
                    "editor.reload"
                ).setIconKey("reload")
        );
        actions.put("RELOAD_THEME",
                new ProgramAction(
                    "Reload GUI Resources", "Reload themes, definition packs and feature maps from disk",
                    KeyMap.requestMapping("theme.reload", identifierToStrokes("c+T")),
                    Resources::load
                ).setIconKey("reload")
        );
        actions.put("JUMP_TO_MATCHING_BRACE",
                new ProgramAction(
                        "Jump to Matching Brace", "Set caret position to the selected brace's match",
                        KeyMap.requestMapping("editor.jump_to_matching_brace", identifierToStrokes("cs+P")).setGroupName("Editor"),
                        "editor.jump_to_matching_brace"
                )
        );
        actions.put("EDITOR_FIND",
                new ProgramAction(
                    "Find in Editor", "Find all occurrences of a query in the current editor tab",
                    KeyMap.requestMapping("editor.find", identifierToStrokes("c+F")).setGroupName("Editor"),
                    "editor.find"
                ).setIconKey("search")
        );
        actions.put("FIND_IN_PATH",
                new ProgramAction(
                    "Find in Path", "Find all occurrences of a query in a folder or project",
                    KeyMap.requestMapping("find_in_path", identifierToStrokes("c+H")).setGroupName("Windows"),
                    SearchPathDialog.INSTANCE::reveal
                ).setIconKey("search")
        );
        actions.put("SEARCH_EVERYWHERE",
                new ProgramAction(
                    "Search Everywhere", "Search for files and actions",
                    KeyMap.requestMapping("quick_access", identifierToStrokes("cs+E;"+ UserKeyBind.Special.DOUBLE_SHIFT.getIdentifier())).setGroupName("Windows"),
                    QuickFindDialog.INSTANCE::reveal
                ).setIconKey("search")
        );
        actions.put("PROJECT_PROPERTIES",
                new ProgramAction(
                    "Project Properties", "Edit the current project",
                    KeyMap.requestMapping("open_project_properties", identifierToStrokes("sa+S")).setGroupName("Windows"),
                    () -> {
                        Project selectedProject = Commons.getActiveProject();
                        selectedProject.getProjectType().showProjectPropertiesDialog(selectedProject);
                    }
                ).setIconKey("project_properties")
        );
        actions.put("CHECK_FOR_UPDATES",
                new ProgramAction(
                        "Check for Definition Updates", "Check for definition updates",
                        KeyMap.requestMapping("update_check"),
                        DefinitionUpdateProcess::tryUpdate
                )
        );
        actions.put("CHECK_FOR_PROGRAM_UPDATES",
                new ProgramAction(
                        "Check for Program Updates", "Check for program and language updates",
                        KeyMap.requestMapping("program_update_check"),
                        ProgramUpdateProcess::tryUpdate
                )
        );
        actions.put("CHANGE_WORKSPACE",
                new ProgramAction(
                        "Change Workspace", "Select a directory to put projects in",
                        KeyMap.requestMapping("change_workspace").setGroupName("Projects"),
                        WorkspaceDialog::prompt
                ).setIconKey("folder")
        );
        actions.put("RELOAD_WORKSPACE",
                new ProgramAction(
                        "Reload Workspace", "Refresh the list of projects",
                        KeyMap.requestMapping("reload_workspace", identifierToStrokes("" + KeyEvent.VK_F5)).setGroupName("Projects"),
                        () -> {
                            TridentWindow.projectExplorer.refresh();
                            TridentPlugins.loadAll();
                        }
                ).setIconKey("reload")
        );
        actions.put("CLEAR_RESOURCE_CACHE",
                new ProgramAction(
                        "Clear Project Resource Cache", "Force the entire resource pack to be exported on next compilation",
                        KeyMap.requestMapping("clear_resource_cache").setGroupName("Projects"),
                        () -> {
                            Project project = Commons.getActiveProject();
                            if(project instanceof TridentProject) {
                                ((TridentProject) project).clearClientDataCache();
                            }
                        }
                ).setIconKey("reload")
        );
        actions.put("UNDO",
                new ProgramAction(
                    "Undo", "Undo the last change",
                    KeyMap.UNDO,
                    "undo"
                ).setGlobalUsage(false).setIconKey("undo")
        );
        actions.put("REDO",
                new ProgramAction(
                    "Redo", "Redo the last change undone",
                    KeyMap.REDO,
                    "redo"
                ).setGlobalUsage(false).setIconKey("redo")
        );
        actions.put("COPY",
                new ProgramAction(
                    "Copy", "Copy selected text",
                    KeyMap.COPY,
                    "copy"
                ).setGlobalUsage(false)
        );
        actions.put("CUT",
                new ProgramAction(
                    "Cut", "Cut selected text",
                    KeyMap.CUT,
                    "cut"
                ).setGlobalUsage(false)
        );
        actions.put("PASTE",
                new ProgramAction(
                    "Paste", "Paste text from clipboard",
                    KeyMap.PASTE,
                    "paste"
                ).setGlobalUsage(false)
        );
        actions.put("DELETE",
                new ProgramAction(
                    "Delete", "Delete selected text",
                    KeyMap.requestMapping("delete", identifierToStrokes(KeyEvent.VK_DELETE + ";" + KeyEvent.VK_BACK_SPACE)).setGroupName("Editor"),
                    "delete"
                ).setGlobalUsage(false)
        );
        for(ProjectType type : ProjectType.values()) {
            actions.put("NEW_PROJECT_" + type.getCode(),
                    new ProgramAction(
                            "New " + type.getName(), "Create new " + type.getName(),
                            KeyMap.requestMapping("new_project_" + type.getCode().toLowerCase(Locale.ENGLISH)).setGroupName("Projects"),
                            () -> ProjectDialog.create(type)
                    ).setIconKey(type.getDefaultProjectIconName())
            );
        }
        actions.put("SETTINGS",
                new ProgramAction(
                        "Settings", "Configure Trident UI",
                        KeyMap.requestMapping("open_settings", identifierToStrokes("csa+S")).setGroupName("Windows"),
                        Settings::show
                ).setIconKey("cog")
        );
        actions.put("DOCUMENTATION",
                new ProgramAction(
                        "Documentation", "Open the language documentation",
                        KeyMap.requestMapping("open_documentation", identifierToStrokes("" + KeyEvent.VK_F1)),
                        () -> {
                            try {
                                Desktop.getDesktop().browse(new URI("https://docs.google.com/document/d/1w_3ILt8-8s1VG-qv7cLLdIrTJTtbQvj2klh2xTnxQVw/edit?usp=sharing"));
                            } catch (IOException | URISyntaxException ex) {
                                ex.printStackTrace();
                            }
                        }
                ).setIconKey("documentation")
        );
        actions.put("TOGGLE_TOOL_BOARD",
                new ProgramAction(
                        "Toggle Tool Board", "Open/Close Tool Boards",
                        KeyMap.requestMapping("toggle_tool_board", identifierToStrokes("c+" + KeyEvent.VK_BACK_QUOTE + ";c+" + KeyEvent.VK_NUMPAD0)).setGroupName("Windows"),
                        () -> TridentWindow.toolBoard.toggle()
                )
        );
        actions.put("OPEN_TODO",
                new ProgramAction(
                        "Show TODO", "Open TODO Board",
                        KeyMap.requestMapping("open_tool_board_todo", identifierToStrokes("c+" + KeyEvent.VK_1 + ";c+" + KeyEvent.VK_NUMPAD1)).setGroupName("Windows"),
                        () -> TridentWindow.todoBoard.open()
                ).setIconKey("todo")
        );
        actions.put("OPEN_NOTICE_BOARD",
                new ProgramAction(
                        "Show Notices", "Open Notice Board",
                        KeyMap.requestMapping("open_tool_board_notices", identifierToStrokes("c+" + KeyEvent.VK_2 + ";c+" + KeyEvent.VK_NUMPAD2)).setGroupName("Windows"),
                        () -> TridentWindow.noticeBoard.open()
                ).setIconKey("notices")
        );
        actions.put("OPEN_CONSOLE",
                new ProgramAction(
                        "Show Console", "Open Console Board",
                        KeyMap.requestMapping("open_tool_board_console", identifierToStrokes("c+" + KeyEvent.VK_3 + ";c+" + KeyEvent.VK_NUMPAD3)).setGroupName("Windows"),
                        () -> {
                            TridentWindow.consoleBoard.open();
                            TridentWindow.consoleBoard.scrollToBottom();
                        }
                ).setIconKey("console")
        );
        actions.put("OPEN_SEARCH_RESULTS",
                new ProgramAction(
                        "Show Search Results", "Open Search Results Board",
                        KeyMap.requestMapping("open_tool_board_search_results", identifierToStrokes("c+" + KeyEvent.VK_4 + ";c+" + KeyEvent.VK_NUMPAD4)).setGroupName("Windows"),
                        () -> TridentWindow.findBoard.open()
                ).setIconKey("search")
        );
        actions.put("OPEN_PROCESSES",
                new ProgramAction(
                        "Show Processes", "Open Processes Board",
                        KeyMap.requestMapping("open_tool_board_processes", identifierToStrokes("c+" + KeyEvent.VK_5 + ";c+" + KeyEvent.VK_NUMPAD5)).setGroupName("Windows"),
                        () -> TridentWindow.processBoard.open()
                ).setIconKey("process")
        );
        actions.put("ABOUT",
                new ProgramAction(
                        "About", "About this program",
                        null,
                        () -> AboutPane.INSTANCE.setVisible(true)
                ).setIconKey("help")
        );
        actions.put("EXIT",
                new ProgramAction(
                        "Exit", "Close Trident UI",
                        null,
                        TridentWindow::close
                )
        );
    }

    private static long ctrlWasDown = -1L;
    private static long altWasDown = -1L;
    private static boolean altGraphCaught = false;

    public static void setup() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((e) -> {
            if(KeyStrokeDialog.isVisible()) return false;
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
                for(ProgramAction action : actions.values()) {
                    if(action.getShortcut() != null && action.isGlobalUsage()) {
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

    public static Map<String, ProgramAction> getAllActions() {
        return actions;
    }

    public static ProgramAction getAction(String key) {
        return actions.get(key);
    }

    public static void performActionForSpecial(UserKeyBind.Special special) {
        for(ProgramAction action : actions.values()) {
            if(action.getShortcut() == null) continue;
            boolean performed = false;
            for(UserMapping mapping : action.getShortcut().getAllMappings()) {
                if(mapping instanceof SpecialMapping && ((SpecialMapping) mapping).getSpecial() == special) {
                    performed = true;
                    break;
                }
            }
            if(performed) action.perform();
        }
    }

    static {
        ConsoleBoard.registerCommandHandler("run", new ConsoleBoard.CommandHandler() {
            @Override
            public String getDescription() {
                return "Performs a program action with the given key";
            }

            @Override
            public void printHelp() {
                Debug.log();
                Debug.log("RUN: Performs a program action with the given key");
                Debug.log("Valid keys: " + actions.keySet());
            }

            @Override
            public void handle(String[] args) {
                if(args.length <= 1) {
                    printHelp();
                } else {
                    String key = args[1].toUpperCase(Locale.ENGLISH);
                    ProgramAction action = actions.get(key);
                    if(action == null) {
                        Debug.log("Error: Unknown action '" + key + "'");
                    } else {
                        Debug.log("Performing action '" + key + "'...");
                        action.perform();
                    }
                }
            }
        });
    }
}
