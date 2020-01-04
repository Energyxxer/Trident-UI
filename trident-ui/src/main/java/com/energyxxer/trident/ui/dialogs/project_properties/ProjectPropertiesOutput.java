package com.energyxxer.trident.ui.dialogs.project_properties;

import com.energyxxer.trident.ui.styledcomponents.StyledCheckBox;
import com.energyxxer.trident.ui.styledcomponents.StyledFileField;
import com.energyxxer.trident.ui.styledcomponents.StyledLabel;
import com.energyxxer.trident.ui.styledcomponents.StyledTextField;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.XFileField;

import javax.swing.*;
import java.awt.*;
import java.io.File;

class ProjectPropertiesOutput extends JPanel {

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

            StyledLabel label = new StyledLabel("Output", "ProjectProperties.content.header", tlm);
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

            //region Data Pack Output
            {
                StyledLabel label = new StyledLabel("Data Pack Output:", "ProjectProperties.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
                content.add(new StyledLabel("Where to store the data pack compilation result.", "ProjectProperties.content", tlm));
                content.add(new StyledLabel("May be a directory or a zip file.", "ProjectProperties.content", tlm));
            }
            {
                StyledFileField datapackOut = new StyledFileField(null, "ProjectProperties.content");
                datapackOut.setDialogTitle("Select Data Pack Output...");
                datapackOut.setOperation(XFileField.SAVE);
                datapackOut.setMaximumSize(new Dimension(datapackOut.getMaximumSize().width,25));
                datapackOut.setAlignmentX(Component.LEFT_ALIGNMENT);
                ProjectProperties.addOpenEvent(p -> {
                    File file = p.getDataOut();
                    if(file == null) file = new File(System.getProperty("user.home") + File.separator + "Trident" + File.separator + "out" + File.separator + p.getName());
                    datapackOut.setFile(file);
                });
                ProjectProperties.addApplyEvent(p -> {
                    p.setDataOut(datapackOut.getFile());
                });
                content.add(datapackOut);
            }

            {
                JPanel margin = new JPanel();
                margin.setMinimumSize(new Dimension(200,15));
                margin.setMaximumSize(new Dimension(200,15));
                margin.setOpaque(false);
                margin.setAlignmentX(Component.LEFT_ALIGNMENT);

                content.add(margin);
            }
            //endregion

            //region Resource Pack Output
            {
                StyledLabel label = new StyledLabel("Resource Pack Output:", "ProjectProperties.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
                content.add(new StyledLabel("Where to store the resource pack compilation result, if applicable.", "ProjectProperties.content", tlm));
                content.add(new StyledLabel("May be a directory or a zip file.", "ProjectProperties.content", tlm));
            }
            {
                StyledFileField resourcesOut = new StyledFileField(null, "ProjectProperties.content");
                resourcesOut.setDialogTitle("Select Resource Pack Output...");
                resourcesOut.setOperation(XFileField.SAVE);
                resourcesOut.setMaximumSize(new Dimension(resourcesOut.getMaximumSize().width,25));
                resourcesOut.setAlignmentX(Component.LEFT_ALIGNMENT);
                ProjectProperties.addOpenEvent(p -> {
                    File file = p.getResourcesOut();
                    if(file == null) file = new File(System.getProperty("user.home") + File.separator + "Trident" + File.separator + "out" + File.separator + p.getName() + "-resources.zip");
                    resourcesOut.setFile(file);
                });
                ProjectProperties.addApplyEvent(p -> {
                    p.setResourcesOut(resourcesOut.getFile());
                });
                content.add(resourcesOut);
            }

            {
                JPanel margin = new JPanel();
                margin.setMinimumSize(new Dimension(200,15));
                margin.setMaximumSize(new Dimension(200,15));
                margin.setOpaque(false);
                margin.setAlignmentX(Component.LEFT_ALIGNMENT);

                content.add(margin);
            }
            //endregion

            //region Export Comments
            {
                StyledCheckBox exportComments = new StyledCheckBox("Export comments","ProjectProperties.content");
                exportComments.setAlignmentX(Component.LEFT_ALIGNMENT);
                ProjectProperties.addOpenEvent(p -> exportComments.setSelected(p.isExportComments()));
                ProjectProperties.addApplyEvent(p -> p.setExportComments(exportComments.isSelected()));

                content.add(exportComments);
                content.add(new StyledLabel("         If enabled, comments in Trident functions will be exported.", "ProjectProperties.content", tlm));
            }

            {
                JPanel margin = new JPanel();
                margin.setMinimumSize(new Dimension(200,15));
                margin.setMaximumSize(new Dimension(200,15));
                margin.setOpaque(false);
                margin.setAlignmentX(Component.LEFT_ALIGNMENT);

                content.add(margin);
            }
            //endregion

            //region Anonymous Function Name
            {
                StyledLabel label = new StyledLabel("Anonymous Function Name:", "ProjectProperties.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
            }
            {
                StyledLabel label = new StyledLabel("Name used for anonymous functions. The number of the anonymous function will replace asterisks (*)", "ProjectProperties.content", tlm);
                content.add(label);
            }
            {
                StyledTextField namespaceField = new StyledTextField("You shouldn't be able to see this*","ProjectProperties.content", tlm);
                namespaceField.setPreferredSize(new Dimension(300,25));
                namespaceField.setMaximumSize(new Dimension(200,25));
                namespaceField.setAlignmentX(Component.LEFT_ALIGNMENT);
                ProjectProperties.addOpenEvent(p -> namespaceField.setText(p.getAnonymousFunctionName()));
                ProjectProperties.addApplyEvent(p -> p.setAnonymousFunctionName(namespaceField.getText()));

                content.add(namespaceField);
            }

            {
                JPanel margin = new JPanel();
                margin.setMinimumSize(new Dimension(200,15));
                margin.setMaximumSize(new Dimension(200,15));
                margin.setOpaque(false);
                margin.setAlignmentX(Component.LEFT_ALIGNMENT);

                content.add(margin);
            }
            //endregion

        }

    }

    ProjectPropertiesOutput() {
        super(new BorderLayout());
    }
}
