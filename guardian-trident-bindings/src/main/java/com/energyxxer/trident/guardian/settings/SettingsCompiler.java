package com.energyxxer.trident.guardian.settings;


import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.ui.dialogs.settings.Settings;
import com.energyxxer.guardian.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.guardian.ui.styledcomponents.StyledLabel;
import com.energyxxer.guardian.ui.styledcomponents.StyledTextField;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.trident.Trident;
import com.energyxxer.xswing.ScalableDimension;

import javax.swing.*;
import java.awt.*;

public class SettingsCompiler extends JPanel {

    private ThemeListenerManager tlm = new ThemeListenerManager();

    public static void load() {
        Trident.NUM_IO_THREADS = Integer.parseInt(Preferences.get("trident.compiler.ioThreads", "0"));
    }

    {
        {
            JPanel header = new JPanel(new BorderLayout());
            header.setPreferredSize(new ScalableDimension(0,40));
            this.add(header, BorderLayout.NORTH);

            {
                JPanel padding = new JPanel();
                padding.setOpaque(false);
                padding.setPreferredSize(new ScalableDimension(25,25));
                header.add(padding, BorderLayout.WEST);
            }

            StyledLabel label = new StyledLabel("Trident Compiler","Settings.content.header", tlm);
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
            padding_left.setPreferredSize(new ScalableDimension(50,25));
            this.add(padding_left, BorderLayout.WEST);
        }
        {
            JPanel padding_right = new JPanel();
            padding_right.setOpaque(false);
            padding_right.setPreferredSize(new ScalableDimension(50,25));
            this.add(padding_right, BorderLayout.EAST);
        }

        {

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);
            OverlayScrollPane scrollPane = new OverlayScrollPane(tlm, content);
            this.add(scrollPane, BorderLayout.CENTER);

            {
                JPanel padding = new JPanel();
                padding.setOpaque(false);
                padding.setMinimumSize(new ScalableDimension(1,20));
                padding.setMaximumSize(new ScalableDimension(1,20));
                content.add(padding);
            }


            {
                StyledLabel label = new StyledLabel("Number of I/O Threads:","Settings.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
            }
            {
                StyledLabel label = new StyledLabel("Controls how many parallel threads can be used when generating","Settings.content", tlm);
                label.setStyle(Font.ITALIC);
                content.add(label);
            }
            {
                StyledLabel label = new StyledLabel("behavior pack and resource pack output.","Settings.content", tlm);
                label.setStyle(Font.ITALIC);
                content.add(label);
            }
            {
                StyledLabel label = new StyledLabel("Set to 0 for no additional threads.","Settings.content", tlm);
                label.setStyle(Font.ITALIC);
                content.add(label);
            }
            {
                StyledTextField field = new StyledTextField("","Settings.content", tlm);
                field.setMaximumSize(new ScalableDimension(300,25));
                field.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> field.setText("" + Trident.NUM_IO_THREADS));
                Settings.addApplyEvent(() -> {
                    try {
                        int value = Integer.parseInt(field.getText());
                        Trident.NUM_IO_THREADS = value;
                        Preferences.put("trident.compiler.ioThreads", String.valueOf(value));
                    } catch(NumberFormatException ignore) {}
                });
                content.add(field);
            }
        }
    }

    public SettingsCompiler() {
        super(new BorderLayout());
    }
}
