package com.energyxxer.trident.main.window.sections;

import com.energyxxer.trident.global.keystrokes.SimpleMapping;
import com.energyxxer.trident.main.window.actions.ActionManager;
import com.energyxxer.trident.main.window.actions.ProgramAction;
import com.energyxxer.trident.ui.common.MenuItems;
import com.energyxxer.trident.ui.styledcomponents.StyledMenu;
import com.energyxxer.trident.ui.styledcomponents.StyledMenuItem;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created by User on 12/15/2016.
 */
public class MenuBar extends JMenuBar {

    private ThemeListenerManager tlm = new ThemeListenerManager();

    {
        tlm.addThemeChangeListener(t -> {
            this.setBackground(t.getColor(new Color(215, 215, 215), "MenuBar.background"));
            this.setBorder(BorderFactory.createMatteBorder(0, 0, Math.max(t.getInteger(1,"MenuBar.border.thickness"),0), 0, t.getColor(new Color(150, 150, 150), "MenuBar.border.color")));
        });

        this.setPreferredSize(new Dimension(0, 20));

        {
            StyledMenu menu = new StyledMenu(" File ");

            menu.setMnemonic(KeyEvent.VK_F);

            // --------------------------------------------------

            menu.add(MenuItems.newMenu("New"));
            menu.add(createItemForAction("CHANGE_WORKSPACE"));
            menu.add(createItemForAction("RELOAD_WORKSPACE"));

            menu.addSeparator();

            menu.add(createItemForAction("SAVE"));
            menu.add(createItemForAction("SAVE_ALL"));

            menu.addSeparator();

            menu.add(createItemForAction("SETTINGS"));
            menu.add(createItemForAction("PROJECT_PROPERTIES"));

            menu.addSeparator();

            menu.add(createItemForAction("COMPILE"));

            menu.addSeparator();

            menu.add(createItemForAction("EXIT"));

            this.add(menu);
        }

        {
            StyledMenu menu = new StyledMenu(" Edit ");
            menu.setMnemonic(KeyEvent.VK_E);

            // --------------------------------------------------

            {
                menu.add(createItemForAction("UNDO"));
            }

            // --------------------------------------------------

            {
                menu.add(createItemForAction("REDO"));
            }

            // --------------------------------------------------

            menu.addSeparator();

            // --------------------------------------------------

            {
                menu.add(createItemForAction("COPY"));
            }

            // --------------------------------------------------

            {
                menu.add(createItemForAction("CUT"));
            }

            // --------------------------------------------------

            {
                menu.add(createItemForAction("PASTE"));
            }

            // --------------------------------------------------

            menu.addSeparator();

            // --------------------------------------------------

            {
                menu.add(createItemForAction("DELETE"));
            }

            // --------------------------------------------------

            this.add(menu);
        }/*

        {
            StyledMenu menu = new StyledMenu(" Project ");
            menu.setMnemonic(KeyEvent.VK_P);

            // --------------------------------------------------

            {
                menu.add(createItemForAction("COMPILE"));
            }

            // --------------------------------------------------

            menu.addSeparator();

            // --------------------------------------------------

            {
                menu.add(createItemForAction("PROJECT_PROPERTIES"));
            }

            // --------------------------------------------------

            this.add(menu);
        }

        {
            StyledMenu menu = new StyledMenu(" Debug ");
            menu.setMnemonic(KeyEvent.VK_D);

            // --------------------------------------------------

            {
                StyledMenuItem item = new StyledMenuItem("Reset Preferences", "warn");
                item.addActionListener(e -> {
                    int confirmation = JOptionPane.showConfirmDialog(null,
                            "        Are you sure you want to reset all saved settings?        ",
                            "Reset Preferences? ", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirmation == JOptionPane.YES_OPTION) {
                        Preferences.reset();
                    }
                });
                menu.add(item);
            }

            // --------------------------------------------------

            {
                StyledMenuItem item = new StyledMenuItem("Edit Electron Theme");
                item.addActionListener(e -> {
                    TridentWindow.tabManager.openTab(new FileModuleToken(new File(System.getProperty("user.home") + File.separator + "Trident" + File.separator + "resources" + File.separator + "themes" + File.separator + "gui" + File.separator + "Electron Dark.properties")));
                });
                menu.add(item);
            }

            // --------------------------------------------------

            {
                StyledMenuItem item = new StyledMenuItem("Refresh Native Library");
                item.addActionListener(e -> {
                    //Resources.nativeLib.refresh();
                });
                menu.add(item);
            }

            // --------------------------------------------------

            {
                StyledMenuItem item = new StyledMenuItem("Extract Native Library");
                item.addActionListener(e -> {
                    try {
                        URL url = TridentUI.class.getResource("/natives/");

                        File extractedNatives = new File(System.getProperty("user.home") + File.separator + "Trident" + File.separator + "natives" + File.separator);
                        FileCommons.deleteFolder(extractedNatives);
                        extractedNatives.mkdir();

                        String protocol = url.getProtocol();
                        if (protocol.equals("file")) {
                            File packagedNatives = new File(url.getFile());

                            Files.walkFileTree(packagedNatives.toPath(), new FileVisitor<Path>() {
                                @Override
                                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                                    return CONTINUE;
                                }

                                @Override
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                    File newFile = new File(System.getProperty("user.home") + File.separator + "Trident" + File.separator + "natives" + File.separator + (packagedNatives.toPath().relativize(file)));
                                    newFile.mkdirs();
                                    Files.copy(file, newFile.toPath(), REPLACE_EXISTING);
                                    TridentWindow.setStatus("Created file '" + newFile + "'");
                                    return CONTINUE;
                                }

                                @Override
                                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                                    return CONTINUE;
                                }

                                @Override
                                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                                    return CONTINUE;
                                }
                            });
                        } else if (protocol.equals("jar")) {
                            String file = url.getFile();
                            int bangIndex = file.indexOf('!');
                            file = new URL(file.substring(0, bangIndex)).getFile();
                            ZipFile zip = new ZipFile(file);
                            Enumeration<? extends ZipEntry> entries = zip.entries();
                            while(entries.hasMoreElements()) {
                                ZipEntry entry = entries.nextElement();
                                if(entry.getName().startsWith("natives/") && !entry.isDirectory()) {
                                    File newFile = new File(extractedNatives.getPath() + entry.getName().substring(7));
                                    newFile.mkdirs();
                                    Files.copy(zip.getInputStream(entry), newFile.toPath(), REPLACE_EXISTING);
                                    TridentWindow.setStatus("Created file '" + newFile + "'");
                                }
                            }
                        }
                        TridentWindow.setStatus("Native Library extraction completed successfully at '" + extractedNatives.getPath() + "'");
                    } catch (IOException x) {
                        TridentWindow.setStatus(new Status(Status.ERROR, "An error occurred during extraction: " + x.getMessage()));
                        x.printStackTrace();
                    }
                });
                menu.add(item);
            }

            // --------------------------------------------------

            this.add(menu);
        }*/

        {
            StyledMenu menu = new StyledMenu(" Navigate ");
            menu.setMnemonic(KeyEvent.VK_W);

            menu.add(createItemForAction("CLOSE_TAB"));
            menu.add(createItemForAction("CLOSE_ALL_TABS"));
            menu.add(createItemForAction("CLOSE_ALL_TABS_FOR_PROJECT"));
            menu.addSeparator();
            menu.add(createItemForAction("EDITOR_FIND"));
            menu.add(createItemForAction("FIND_IN_PATH"));
            menu.add(createItemForAction("SEARCH_EVERYWHERE"));
            menu.addSeparator();
            menu.add(createItemForAction("TOGGLE_TOOL_BOARD"));
            menu.add(createItemForAction("OPEN_TODO"));
            menu.add(createItemForAction("OPEN_NOTICE_BOARD"));
            menu.add(createItemForAction("OPEN_CONSOLE"));
            menu.add(createItemForAction("OPEN_SEARCH_RESULTS"));
            menu.add(createItemForAction("OPEN_PROCESSES"));

            this.add(menu);
        }

        {
            StyledMenu menu = new StyledMenu("Help");
            menu.setMnemonic(KeyEvent.VK_H);

            menu.add(createItemForAction("DOCUMENTATION"));
            menu.addSeparator();
            menu.add(createItemForAction("CHECK_FOR_UPDATES"));
            menu.add(createItemForAction("ABOUT"));

            this.add(menu);
        }
    }

    private static StyledMenuItem createItemForAction(String actionKey) {
        ProgramAction action = ActionManager.getAction(actionKey);
        StyledMenuItem item = new StyledMenuItem(action.getTitle(), action.getIconKey());
        if(action.getShortcut() != null && action.getShortcut().getFirstMapping() instanceof SimpleMapping) item.setAccelerator(((SimpleMapping) action.getShortcut().getFirstMapping()).getKeyStroke());
        item.addActionListener(e -> action.perform());
        return item;
    }
}
