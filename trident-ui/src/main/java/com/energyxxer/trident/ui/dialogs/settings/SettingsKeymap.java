package com.energyxxer.trident.ui.dialogs.settings;

import com.energyxxer.trident.global.keystrokes.KeyMap;
import com.energyxxer.trident.global.keystrokes.UserKeyStroke;
import com.energyxxer.trident.main.window.sections.quick_find.StyledExplorerMaster;
import com.energyxxer.trident.ui.dialogs.KeyStrokeDialog;
import com.energyxxer.trident.ui.explorer.base.ActionHostExplorerItem;
import com.energyxxer.trident.ui.explorer.base.StyleProvider;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.ui.orderlist.CompoundActionModuleToken;
import com.energyxxer.trident.ui.orderlist.ItemAction;
import com.energyxxer.trident.ui.orderlist.ItemActionHost;
import com.energyxxer.trident.ui.scrollbar.OverlayScrollPaneLayout;
import com.energyxxer.trident.ui.styledcomponents.StyledLabel;
import com.energyxxer.trident.ui.styledcomponents.StyledMenuItem;
import com.energyxxer.trident.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.KeyInputUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

public class SettingsKeymap extends JPanel {

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private final StyledExplorerMaster master;

    {
        {
            JPanel header = new JPanel(new BorderLayout());
            header.setPreferredSize(new Dimension(0,40));
            this.add(header, BorderLayout.NORTH);

            {
                JPanel padding = new JPanel();
                padding.setOpaque(false);
                padding.setPreferredSize(new Dimension(25,25));
                header.add(padding, BorderLayout.WEST);
            }

            StyledLabel label = new StyledLabel("Keymap","Settings.content.header", tlm);
            header.add(label, BorderLayout.CENTER);

            tlm.addThemeChangeListener(t -> {
                setBackground(t.getColor(new Color(235, 235, 235), "Settings.content.background"));
                header.setBackground(t.getColor(new Color(235, 235, 235), "Settings.content.header.background"));
                header.setBorder(BorderFactory.createMatteBorder(0, 0, Math.max(t.getInteger(1,"Settings.content.header.border.thickness"),0), 0, t.getColor(new Color(200, 200, 200), "Settings.content.header.border.color")));
            });
        }

        {

            JPanel content = new JPanel(new BorderLayout());
            //content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);
            this.add(content, BorderLayout.CENTER);

            master = new StyledExplorerMaster();

            JScrollPane sp = new JScrollPane(master);
            sp.setBorder(new EmptyBorder(0,0,0,0));
            sp.setLayout(new OverlayScrollPaneLayout(sp));

            content.add(sp, BorderLayout.CENTER);

            for(UserKeyStroke ks : KeyMap.getAll()) {
                master.addElement(new ActionHostExplorerItem(master, new CompoundActionModuleToken() {
                    @Override
                    public @NotNull List<ItemAction> getActions() {
                        return Collections.singletonList(new KeyStrokeAction(ks));
                    }

                    @Override
                    public String getTitle() {
                        return ks.getName();
                    }

                    @Override
                    public Image getIcon() {
                        return null;
                    }

                    @Override
                    public StyledPopupMenu generateMenu(@NotNull MenuContext context) {
                        StyledPopupMenu menu = new StyledPopupMenu();
                        menu.add(new StyledMenuItem("Add Shortcut") {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                KeyStroke toAdd = new KeyStrokeDialog("Add Shortcut", "Action: " + ks.getName(), SettingsKeymap::checkCollisions).result;
                                if(toAdd != null) {
                                    List<KeyStroke> newList = ks.getNewStrokes();
                                    if(!newList.contains(toAdd)) {
                                        newList.add(toAdd);
                                        master.repaint();
                                    }
                                }
                            }
                        });
                        List<KeyStroke> allKeybinds = ks.getNewStrokes();
                        if(!allKeybinds.isEmpty()) menu.addSeparator();
                        for(KeyStroke keybind : allKeybinds) {
                            menu.add(new StyledMenuItem("Remove " + KeyInputUtils.getReadableKeyStroke(keybind)) {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    ks.getNewStrokes().remove(keybind);
                                    master.repaint();
                                }
                            });
                        }
                        if(!ks.newMatchesDefault()) {
                            menu.addSeparator();
                            menu.add(new StyledMenuItem("Reset Shortcuts") {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    ks.revertToDefault();
                                    master.repaint();
                                }
                            });
                        }
                        return menu;
                    }

                    @Override
                    public boolean equals(ModuleToken other) {
                        return other == this;
                    }
                }));
            }

        }
        Settings.addApplyEvent(() -> {
            for(UserKeyStroke uks : KeyMap.getAll()) {
                uks.applyChanges();
                uks.save();
            }
        });
        Settings.addCancelEvent(() -> {
            for(UserKeyStroke uks : KeyMap.getAll()) {
                uks.discardChanges();
            }
        });
    }

    private static String checkCollisions(KeyStroke stroke) {
        for(UserKeyStroke uks : KeyMap.getAll()) {
            for(KeyStroke ks : uks.getNewStrokes()) {
                if(stroke.equals(ks)) {
                    return "Already assigned to: " + uks.getName();
                }
            }
        }
        return null;
    }

    SettingsKeymap() {
        super(new BorderLayout());
    }

    private static class KeyStrokeAction implements ItemAction {
        private UserKeyStroke stroke;

        private int renderedWidth = 0;

        private int margin = 8;
        private int padding = 4;

        public KeyStrokeAction(UserKeyStroke stroke) {
            this.stroke = stroke;
        }

        @Override
        public void render(Graphics g, ItemActionHost host, int x, int y, int w, int h, int mouseState, boolean actionEnabled) {
            StyleProvider styleProvider = host.getStyleProvider();

            int buttonVGap = 4;
            int buttonVSize = h - 2*buttonVGap;

            renderedWidth = margin;
            x -= margin;
            FontMetrics fm = g.getFontMetrics();

            String styleVariant = stroke.newMatchesDefault() ? "default." : "";

            List<KeyStroke> allStrokes = this.stroke.getNewStrokes();
            for(int i = allStrokes.size()-1; i >= 0; i--) {
                KeyStroke stroke = allStrokes.get(i);
                int plateWidth = 2*padding;
                String readableName = KeyInputUtils.getReadableKeyStroke(stroke);
                plateWidth += fm.stringWidth(readableName);

                g.setColor(styleProvider.getColors().get("keybind." + styleVariant + "border.color"));
                g.fillRect(x - plateWidth - 1, y + buttonVGap - 1, plateWidth + 2, buttonVSize + 2);

                g.setColor(styleProvider.getColors().get("keybind." + styleVariant + "background"));
                g.fillRect(x - plateWidth, y + buttonVGap, plateWidth, buttonVSize);

                g.setColor(styleProvider.getColors().get("keybind." + styleVariant + "foreground"));
                g.drawString(readableName, x - plateWidth + padding, y + (h + fm.getAscent() - fm.getDescent())/2);
                renderedWidth += plateWidth;
                renderedWidth += margin;

                x -= plateWidth;
                x -= margin;
            }
        }

        @Override
        public boolean intersects(Point p, int w, int h) {
            return false;
        }

        @Override
        public int getRenderedWidth() {
            return renderedWidth;
        }

        @Override
        public boolean isLeftAligned() {
            return false;
        }

        @Override
        public String getHint() {
            return null;
        }

        @Override
        public int getHintOffset() {
            return 0;
        }
    }
}
