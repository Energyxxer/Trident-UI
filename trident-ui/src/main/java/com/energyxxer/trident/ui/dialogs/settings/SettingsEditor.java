package com.energyxxer.trident.ui.dialogs.settings;

import com.energyxxer.trident.ui.editor.TridentEditorComponent;
import com.energyxxer.trident.ui.editor.behavior.caret.Dot;
import com.energyxxer.trident.ui.styledcomponents.StyledCheckBox;
import com.energyxxer.trident.ui.styledcomponents.StyledLabel;
import com.energyxxer.trident.ui.styledcomponents.StyledTextField;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.Padding;

import javax.swing.*;
import java.awt.*;

public class SettingsEditor extends JPanel {

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

            StyledLabel label = new StyledLabel("Editor","Settings.content.header", tlm);
            header.add(label, BorderLayout.CENTER);

            tlm.addThemeChangeListener(t -> {
                setBackground(t.getColor(new Color(235, 235, 235), "Settings.content.background"));
                header.setBackground(t.getColor(new Color(235, 235, 235), "Settings.content.header.background"));
                header.setBorder(BorderFactory.createMatteBorder(0, 0, Math.max(t.getInteger(1,"Settings.content.header.border.thickness"),0), 0, t.getColor(new Color(200, 200, 200), "Settings.content.header.border.color")));
            });
        }

        {
            JPanel paddingLeft = new JPanel();
            paddingLeft.setOpaque(false);
            paddingLeft.setPreferredSize(new Dimension(50,25));
            this.add(paddingLeft, BorderLayout.WEST);
        }
        {
            JPanel paddingRight = new JPanel();
            paddingRight.setOpaque(false);
            paddingRight.setPreferredSize(new Dimension(50,25));
            this.add(paddingRight, BorderLayout.EAST);
        }

        {

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);
            this.add(content, BorderLayout.CENTER);


            {
                content.add(new Padding(20));
            }


            {
                StyledLabel label = new StyledLabel("Auto-reparse delay (ms):","Settings.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
            }
            {
                StyledTextField autoreparseDelayField = new StyledTextField("","Settings.content", tlm);
                autoreparseDelayField.setMaximumSize(new Dimension(300,25));
                autoreparseDelayField.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> autoreparseDelayField.setText("" + TridentEditorComponent.AUTOREPARSE_DELAY.get()));
                Settings.addApplyEvent(() -> {
                    try {
                        int delay = Integer.parseInt(autoreparseDelayField.getText());
                        if(delay >= 0) {
                            TridentEditorComponent.AUTOREPARSE_DELAY.set(delay);
                        }
                    } catch(NumberFormatException ignore) {}
                });
                content.add(autoreparseDelayField);
            }

            {
                StyledCheckBox showSuggestions = new StyledCheckBox("Show suggestions as you type","Settings.content");
                showSuggestions.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> showSuggestions.setSelected(TridentEditorComponent.SHOW_SUGGESTIONS.get()));
                Settings.addApplyEvent(() -> TridentEditorComponent.SHOW_SUGGESTIONS.set(showSuggestions.isSelected()));

                content.add(showSuggestions);
            }
            {
                StyledLabel label = new StyledLabel("Suggestions are tied to the auto-reparse delay","Settings.content", tlm);
                label.setStyle(Font.ITALIC);
                content.add(label);
            }


            {
                content.add(new Padding(20));
            }

            {
                StyledLabel label = new StyledLabel("Smart Keys:","Settings.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
            }

            {
                StyledCheckBox smartKeysHome = new StyledCheckBox("Home","Settings.content");
                smartKeysHome.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> smartKeysHome.setSelected(Dot.SMART_KEYS_HOME.get()));
                Settings.addApplyEvent(() -> Dot.SMART_KEYS_HOME.set(smartKeysHome.isSelected()));

                content.add(smartKeysHome);
            }
            {
                StyledCheckBox smartKeysIndent = new StyledCheckBox("Smart Indent","Settings.content");
                smartKeysIndent.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> smartKeysIndent.setSelected(Dot.SMART_KEYS_INDENT.get()));
                Settings.addApplyEvent(() -> Dot.SMART_KEYS_INDENT.set(smartKeysIndent.isSelected()));

                content.add(smartKeysIndent);
            }
        }
    }

    public SettingsEditor() {
        super(new BorderLayout());
    }
}