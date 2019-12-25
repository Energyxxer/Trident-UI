package com.energyxxer.trident.ui.dialogs.settings;

import com.energyxxer.trident.global.Preferences;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.styledcomponents.*;
import com.energyxxer.trident.ui.theme.Theme;
import com.energyxxer.trident.ui.theme.ThemeManager;
import com.energyxxer.trident.ui.theme.change.ThemeChangeListener;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;

import javax.swing.*;
import java.awt.*;

class SettingsAppearance extends JPanel {

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

            StyledLabel label = new StyledLabel("Appearance","Settings.content.header", tlm);
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

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);
            this.add(content, BorderLayout.CENTER);

            {
                JPanel padding = new JPanel();
                padding.setOpaque(false);
                padding.setMinimumSize(new Dimension(1,20));
                padding.setMaximumSize(new Dimension(1,20));
                content.add(padding);
            }

            {
                StyledLabel label = new StyledLabel("Theme:","Settings.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
            }
            {
                StyledDropdownMenu<Theme> themeDropdown = new StyledDropdownMenu<>(ThemeManager.getGUIThemesAsArray(), "Settings");
                themeDropdown.setPopupFactory(StyledPopupMenu::new);
                themeDropdown.setPopupItemFactory(StyledMenuItem::new);
                themeDropdown.setValue(TridentWindow.getTheme());
                Settings.addOpenEvent(ThemeManager::loadAll);
                Settings.addApplyEvent(() -> ThemeManager.setGUITheme(themeDropdown.getValue().getName()));
                content.add(themeDropdown);
            }

            {
                JPanel padding = new JPanel();
                padding.setOpaque(false);
                padding.setMinimumSize(new Dimension(1,20));
                padding.setMaximumSize(new Dimension(1,20));
                content.add(padding);
            }

            {
                StyledLabel label = new StyledLabel("Base Font Size:","Settings.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
            }
            {
                StyledTextField baseFontSizeField = new StyledTextField("","Settings.content", tlm);
                baseFontSizeField.setMaximumSize(new Dimension(300,25));
                baseFontSizeField.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> baseFontSizeField.setText("" + Preferences.getBaseFontSize()));
                Settings.addApplyEvent(() -> {
                    try {
                        int fontSize = Integer.parseInt(baseFontSizeField.getText());
                        Preferences.setBaseFontSize(fontSize);
                        ThemeChangeListener.dispatchThemeChange(TridentWindow.getTheme());
                    } catch(NumberFormatException ignore) {}
                });
                content.add(baseFontSizeField);
            }

            {
                StyledLabel label = new StyledLabel("Editor Font Scale:","Settings.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
            }
            {
                StyledTextField editorFontSizeField = new StyledTextField("","Settings.content", tlm);
                editorFontSizeField.setMaximumSize(new Dimension(300,25));
                editorFontSizeField.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> editorFontSizeField.setText("" + Preferences.getEditorFontSize()));
                Settings.addApplyEvent(() -> {
                    try {
                        int fontSize = Integer.parseInt(editorFontSizeField.getText());
                        Preferences.setEditorFontSize(fontSize);
                        ThemeChangeListener.dispatchThemeChange(TridentWindow.getTheme());
                    } catch(NumberFormatException ignore) {}
                });
                content.add(editorFontSizeField);
            }
        }
    }

    SettingsAppearance() {
        super(new BorderLayout());
    }
}
