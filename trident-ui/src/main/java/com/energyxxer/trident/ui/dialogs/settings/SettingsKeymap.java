package com.energyxxer.trident.ui.dialogs.settings;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.main.window.sections.quick_find.StyledExplorerMaster;
import com.energyxxer.trident.ui.explorer.base.ActionHostExplorerItem;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.ui.orderlist.CompoundActionModuleToken;
import com.energyxxer.trident.ui.orderlist.ItemAction;
import com.energyxxer.trident.ui.orderlist.ItemButtonAction;
import com.energyxxer.trident.ui.styledcomponents.StyledLabel;
import com.energyxxer.trident.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.logger.Debug;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class SettingsKeymap extends JPanel {

    private ThemeListenerManager tlm = new ThemeListenerManager();

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

            StyledLabel label = new StyledLabel("Keymap","Settings.content.header");
            header.add(label, BorderLayout.CENTER);

            tlm.addThemeChangeListener(t -> {
                setBackground(t.getColor(new Color(235, 235, 235), "Settings.content.background"));
                header.setBackground(t.getColor(new Color(235, 235, 235), "Settings.content.header.background"));
                header.setBorder(BorderFactory.createMatteBorder(0, 0, Math.max(t.getInteger(1,"Settings.content.header.border.thickness"),0), 0, t.getColor(new Color(200, 200, 200), "Settings.content.header.border.color")));
            });
        }

        {
            JPanel padding_left = new JPanel();
            padding_left.setOpaque(false);
            padding_left.setPreferredSize(new Dimension(50,25));
            this.add(padding_left, BorderLayout.WEST);
        }
        {
            JPanel padding_right = new JPanel();
            padding_right.setOpaque(false);
            padding_right.setPreferredSize(new Dimension(50,25));
            this.add(padding_right, BorderLayout.EAST);
        }

        {

            JPanel content = new JPanel(new BorderLayout());
            //content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);
            this.add(content, BorderLayout.CENTER);

            StyledExplorerMaster master = new StyledExplorerMaster();
            content.add(master);
            /*master.addElement(new ActionHostExplorerItem(master, new CompoundActionModuleToken() {
                @Override
                public @NotNull List<ItemAction> getActions() {
                    return Collections.emptyList();
                }

                @Override
                public String getTitle() {
                    return "aaa";
                }

                @Override
                public Image getIcon() {
                    return null;
                }

                @Override
                public StyledPopupMenu generateMenu(@NotNull MenuContext context) {
                    return null;
                }

                @Override
                public boolean equals(ModuleToken other) {
                    return other == this;
                }
            }));*/
            master.addElement(new ActionHostExplorerItem(master, new CompoundActionModuleToken() {
                @Override
                public @NotNull List<ItemAction> getActions() {
                    return Collections.singletonList(new ItemButtonAction() {
                        @Override
                        public Image getIcon() {
                            return Commons.getIcon("explorer").getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                        }

                        @Override
                        public String getDescription() {
                            return "aaaa";
                        }

                        @Override
                        public void perform() {
                            Debug.log("perssed");
                        }
                    });
                }

                @Override
                public String getTitle() {
                    return "bbb";
                }

                @Override
                public Image getIcon() {
                    return Commons.getIcon("folder");
                }

                @Override
                public StyledPopupMenu generateMenu(@NotNull MenuContext context) {
                    return null;
                }

                @Override
                public boolean equals(ModuleToken other) {
                    return other == this;
                }
            }));
            master.addElement(new ActionHostExplorerItem(master, new CompoundActionModuleToken() {
                @Override
                public @NotNull List<ItemAction> getActions() {
                    return Collections.emptyList();
                }

                @Override
                public String getTitle() {
                    return "ccc";
                }

                @Override
                public Image getIcon() {
                    return null;
                }

                @Override
                public StyledPopupMenu generateMenu(@NotNull MenuContext context) {
                    return null;
                }

                @Override
                public boolean equals(ModuleToken other) {
                    return other == this;
                }
            }));

        }
    }

    SettingsKeymap() {
        super(new BorderLayout());
    }
}
