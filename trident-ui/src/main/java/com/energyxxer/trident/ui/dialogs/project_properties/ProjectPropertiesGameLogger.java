package com.energyxxer.trident.ui.dialogs.project_properties;

import com.energyxxer.trident.compiler.util.JsonTraverser;
import com.energyxxer.trident.global.Resources;
import com.energyxxer.trident.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.trident.ui.styledcomponents.StyledCheckBox;
import com.energyxxer.trident.ui.styledcomponents.StyledLabel;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.ScalableDimension;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;

class ProjectPropertiesGameLogger extends JPanel {

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private final StyledCheckBox doExport;
    private final StyledCheckBox compact;
    private final StyledCheckBox timestampEnabled;
    private final StyledCheckBox posEnabled;
    private final StyledCheckBox lineNumberEnabled;

    private final JEditorPane previewPane;

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

            StyledLabel label = new StyledLabel("Game Logger", "ProjectProperties.content.header", tlm);
            header.add(label, BorderLayout.CENTER);

            tlm.addThemeChangeListener(t -> {
                setBackground(t.getColor(new Color(235, 235, 235), "ProjectProperties.content.background"));
                header.setBackground(t.getColor(new Color(235, 235, 235), "ProjectProperties.content.header.background"));
                header.setBorder(BorderFactory.createMatteBorder(0, 0, Math.max(t.getInteger(1,"ProjectProperties.content.header.border.thickness"),0), 0, t.getColor(new Color(200, 200, 200), "ProjectProperties.content.header.border.color")));
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
            this.add(new OverlayScrollPane(tlm, content), BorderLayout.CENTER);

            {
                JPanel padding = new JPanel();
                padding.setOpaque(false);
                padding.setMinimumSize(new ScalableDimension(1,20));
                padding.setMaximumSize(new ScalableDimension(1,20));
                content.add(padding);
            }

            //region Export
            {
                doExport = new StyledCheckBox("Output Game Log Commands","ProjectProperties.content");
                doExport.setAlignmentX(Component.LEFT_ALIGNMENT);

                content.add(doExport);
                content.add(new StyledLabel("         If disabled, gamelog commands will not generate an output.", "ProjectProperties.content", tlm));
                content.add(new StyledLabel("         The gamelog command requires Language Level 3.", "ProjectProperties.content", tlm));
            }

            {
                JPanel margin = new JPanel();
                margin.setMinimumSize(new ScalableDimension(200,45));
                margin.setMaximumSize(new ScalableDimension(200,45));
                margin.setOpaque(false);
                margin.setAlignmentX(Component.LEFT_ALIGNMENT);

                content.add(margin);
            }
            //endregion

            //region Compact
            {
                compact = new StyledCheckBox("Compact","ProjectProperties.content");
                compact.setAlignmentX(Component.LEFT_ALIGNMENT);

                content.add(compact);
                content.add(new StyledLabel("         If enabled, game log messages are shortened, showing minimal information.", "ProjectProperties.content", tlm));
            }

            {
                JPanel margin = new JPanel();
                margin.setMinimumSize(new ScalableDimension(200,15));
                margin.setMaximumSize(new ScalableDimension(200,15));
                margin.setOpaque(false);
                margin.setAlignmentX(Component.LEFT_ALIGNMENT);

                content.add(margin);
            }
            //endregion

            //region Timestamp Enabled
            {
                timestampEnabled = new StyledCheckBox("Timestamp","ProjectProperties.content");
                timestampEnabled.setAlignmentX(Component.LEFT_ALIGNMENT);

                content.add(timestampEnabled);
                content.add(new StyledLabel("         If enabled, game log messages will show the time of execution, based on game time.", "ProjectProperties.content", tlm));
            }

            {
                JPanel margin = new JPanel();
                margin.setMinimumSize(new ScalableDimension(200,15));
                margin.setMaximumSize(new ScalableDimension(200,15));
                margin.setOpaque(false);
                margin.setAlignmentX(Component.LEFT_ALIGNMENT);

                content.add(margin);
            }
            //endregion

            //region Timestamp Enabled
            {
                posEnabled = new StyledCheckBox("Position","ProjectProperties.content");
                posEnabled.setAlignmentX(Component.LEFT_ALIGNMENT);

                content.add(posEnabled);
                content.add(new StyledLabel("         If enabled, game log messages will show the execution location.", "ProjectProperties.content", tlm));
            }

            {
                JPanel margin = new JPanel();
                margin.setMinimumSize(new ScalableDimension(200,15));
                margin.setMaximumSize(new ScalableDimension(200,15));
                margin.setOpaque(false);
                margin.setAlignmentX(Component.LEFT_ALIGNMENT);

                content.add(margin);
            }
            //endregion

            //region Line Number
            {
                lineNumberEnabled = new StyledCheckBox("Line Number","ProjectProperties.content");
                lineNumberEnabled.setAlignmentX(Component.LEFT_ALIGNMENT);

                content.add(lineNumberEnabled);
                content.add(new StyledLabel("         If enabled, game log messages will show the line number of the gamelog command.", "ProjectProperties.content", tlm));
            }

            {
                JPanel margin = new JPanel();
                margin.setMinimumSize(new ScalableDimension(200,15));
                margin.setMaximumSize(new ScalableDimension(200,15));
                margin.setOpaque(false);
                margin.setAlignmentX(Component.LEFT_ALIGNMENT);

                content.add(margin);
            }
            //endregion

            StyledLabel previewLabel = new StyledLabel("Preview:", "ProjectProperties.content", tlm);
            previewLabel.setStyle(Font.BOLD);
            content.add(previewLabel);

            previewPane = new JEditorPane("text/html", "");
            previewPane.setEditable(false);
            previewPane.setAlignmentX(LEFT_ALIGNMENT);
            previewPane.setFont(Resources.MINECRAFT_FONT);
            previewPane.repaint();
            content.add(previewPane);

            tlm.addThemeChangeListener(t -> {
                Color bgcolor = t.getColor(Color.WHITE, "ProjectProperties.textPreview.background");
                previewPane.setBackground(bgcolor);
                previewPane.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, bgcolor));
            });

            compact.addActionListener(a -> updateTextPreview());
            timestampEnabled.addActionListener(a -> updateTextPreview());
            posEnabled.addActionListener(a -> updateTextPreview());
            lineNumberEnabled.addActionListener(a -> updateTextPreview());

            ProjectProperties.addOpenEvent(p -> {
                JsonObject config = p.getProjectConfigJson();
                doExport.setSelected(p.isExportGamelog());

                compact.setSelected(false);
                timestampEnabled.setSelected(true);
                posEnabled.setSelected(false);
                lineNumberEnabled.setSelected(false);

                if(config.has("game-logger") && config.get("game-logger").isJsonObject()) {
                    JsonObject loggerObj = config.getAsJsonObject("game-logger");

                    compact.setSelected(JsonTraverser.INSTANCE.reset(loggerObj).get("compact").asBoolean(false));
                    timestampEnabled.setSelected(JsonTraverser.INSTANCE.reset(loggerObj).get("timestamp-enabled").asBoolean(true));
                    posEnabled.setSelected(JsonTraverser.INSTANCE.reset(loggerObj).get("pos-enabled").asBoolean(false));
                    lineNumberEnabled.setSelected(JsonTraverser.INSTANCE.reset(loggerObj).get("line-number-enabled").asBoolean(false));
                }

                updateTextPreview();
            });

            ProjectProperties.addApplyEvent(p -> {
                p.setExportGamelog(doExport.isSelected());

                JsonObject config = p.getProjectConfigJson();
                JsonObject loggerObj = new JsonObject();
                config.add("game-logger", loggerObj);

                loggerObj.addProperty("compact", compact.isSelected());
                loggerObj.addProperty("timestamp-enabled", timestampEnabled.isSelected());
                loggerObj.addProperty("pos-enabled", posEnabled.isSelected());
                loggerObj.addProperty("line-number-enabled", lineNumberEnabled.isSelected());
            });
        }

    }

    ProjectPropertiesGameLogger() {
        super(new BorderLayout());
    }

    private void updateTextPreview() {
        StringBuilder sb = new StringBuilder("<html><body style=\"font-size:12px;\"><font face=\"Minecraft\">");

        String color = "#FF5555";
        String separator = "#AAAAAA";

        if(timestampEnabled.isSelected()) {
            sb.append("<font color=\"").append(color).append("\">");
            if(!compact.isSelected()) {
                sb.append("11/");
            }
            sb.append(compact.isSelected() ? 828 : 18);
            sb.append(":15:25.2 </font>");
        }
        sb.append("<font color=\"").append(separator).append("\">[</font>");
        if(!compact.isSelected()) {
            sb.append("<font color=\"").append(color).append("\">");
            sb.append(ProjectProperties.project.getName());
            sb.append("</font>");
            sb.append("<font color=\"").append(separator).append("\">/</font>");
        }
        sb.append("<font color=\"").append(color).append("\">ERROR</font>");
        sb.append("<font color=\"").append(separator).append("\">]</font>");
        if(!compact.isSelected()) {
            sb.append("<br></br>&emsp;");
        } else {
            sb.append(" ");
        }
        sb.append("<font color=\"").append(separator).append("\">In </font>");

        sb.append("<font color=\"").append(color).append("\">namespace:path/to/function</font>");
        if(lineNumberEnabled.isSelected()) {
            sb.append("<font color=\"").append(separator).append("\">:</font>");
            sb.append("<font color=\"").append(color).append("\">37</font>");
        }
        if(!compact.isSelected()) {
            sb.append("<br></br>&emsp;");
        } else {
            sb.append(" ");
        }
        if(posEnabled.isSelected()) {
            sb.append("<font color=\"").append(separator).append("\">at </font>");
            sb.append("<font color=\"").append(color).append("\">-11 63 35 </font>");
        }
        sb.append("<font color=\"").append(separator).append("\">as </font>");
        sb.append("<font color=\"").append(color).append("\">Villager</font>");
        sb.append("<font color=\"").append(separator).append("\">: </font>");
        sb.append("<font color=\"").append(color).append("\">Hello World!</font>");
        sb.append("</font></body></html>");

        previewPane.setText(sb.toString());
    }
}
