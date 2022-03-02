package com.energyxxer.trident.guardian.dialogs.project_properties;

import com.energyxxer.commodore.versioning.JavaEditionVersion;
import com.energyxxer.guardian.ui.commodoreresources.DefinitionPacks;
import com.energyxxer.guardian.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.guardian.ui.styledcomponents.*;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.ScalableDimension;

import javax.swing.*;
import java.awt.*;

class ProjectPropertiesGeneral extends JPanel {

    private ThemeListenerManager tlm = new ThemeListenerManager();

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

            StyledLabel label = new StyledLabel("General", "ProjectProperties.content.header", tlm);
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

            //region Target Version
            {
                StyledLabel label = new StyledLabel("Target Version:", "ProjectProperties.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
                content.add(new StyledLabel("Determines which type definitions to use by default,", "ProjectProperties.content", tlm));
                content.add(new StyledLabel("as well as which commands are allowed.", "ProjectProperties.content", tlm));
            }
            {
                JavaEditionVersion[] knownVersions = DefinitionPacks.getKnownJavaVersions();

                StyledDropdownMenu<JavaEditionVersion> versionDropdown = new StyledDropdownMenu<>(knownVersions, "ProjectProperties");
                versionDropdown.setPopupFactory(StyledPopupMenu::new);
                versionDropdown.setPopupItemFactory(StyledMenuItem::new);
                ProjectProperties.addOpenEvent(p -> {
                    versionDropdown.setValue(p.getTargetVersion());
                });
                ProjectProperties.addApplyEvent(p -> {
                    JavaEditionVersion value = versionDropdown.getValue();
                    p.setTargetVersion(value);
                });
                content.add(versionDropdown);
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

            //region Language Level
            {
                StyledLabel label = new StyledLabel("Language Level:", "ProjectProperties.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
            }
            {
                StyledLabel label = new StyledLabel("The default level of abstraction allowed for this project.", "ProjectProperties.content", tlm);
                content.add(label);
            }
            {
                String[] levels = new String[] {
                        "1 - Basic pre-processing",
                        "2 - using, tag update",
                        "3 - Custom item events, game logger"
                };

                StyledDropdownMenu<String> levelDropdown = new StyledDropdownMenu<>(levels, "ProjectProperties");
                levelDropdown.setPopupFactory(StyledPopupMenu::new);
                levelDropdown.setPopupItemFactory(StyledMenuItem::new);
                ProjectProperties.addOpenEvent(p -> levelDropdown.setValueIndex(p.getLanguageLevel()-1));
                ProjectProperties.addApplyEvent(p -> {
                    p.setLanguageLevel(levelDropdown.getValueIndex()+1);
                });
                content.add(levelDropdown);
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

            //region Default Namespace
            {
                StyledLabel label = new StyledLabel("Default Namespace:", "ProjectProperties.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
            }
            {
                StyledLabel label = new StyledLabel("Used for Trident-generated functions.", "ProjectProperties.content", tlm);
                content.add(label);
            }
            {
                StyledTextField namespaceField = new StyledTextField("trident_temp_please_specify_default_namespace","ProjectProperties.content", tlm);
                namespaceField.setPreferredSize(new ScalableDimension(300,25));
                namespaceField.setMaximumSize(new ScalableDimension(200,25));
                namespaceField.setAlignmentX(Component.LEFT_ALIGNMENT);
                ProjectProperties.addOpenEvent(p -> namespaceField.setText(p.getDefaultNamespace()));
                ProjectProperties.addApplyEvent(p -> p.setDefaultNamespace(namespaceField.getText()));

                content.add(namespaceField);
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
                namespaceField.setPreferredSize(new ScalableDimension(300,25));
                namespaceField.setMaximumSize(new ScalableDimension(200,25));
                namespaceField.setAlignmentX(Component.LEFT_ALIGNMENT);
                ProjectProperties.addOpenEvent(p -> namespaceField.setText(p.getAnonymousFunctionName()));
                ProjectProperties.addApplyEvent(p -> p.setAnonymousFunctionName(namespaceField.getText()));

                content.add(namespaceField);
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


            //region Strict NBT
            {
                StyledCheckBox strictNBT = new StyledCheckBox("Strict NBT","ProjectProperties.content");
                strictNBT.setAlignmentX(Component.LEFT_ALIGNMENT);
                ProjectProperties.addOpenEvent(p -> strictNBT.setSelected(p.isStrictNBT()));
                ProjectProperties.addApplyEvent(p -> p.setStrictNBT(strictNBT.isSelected()));

                content.add(strictNBT);
                content.add(new StyledLabel("         If enabled, type errors in NBT tags will prevent compilation.", "ProjectProperties.content", tlm));
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

            //region Strict Text Components
            {
                StyledCheckBox strictTextComponents = new StyledCheckBox("Strict Text Components","ProjectProperties.content");
                strictTextComponents.setAlignmentX(Component.LEFT_ALIGNMENT);
                ProjectProperties.addOpenEvent(p -> strictTextComponents.setSelected(p.isStrictTextComponents()));
                ProjectProperties.addApplyEvent(p -> p.setStrictTextComponents(strictTextComponents.isSelected()));

                content.add(strictTextComponents);
                content.add(new StyledLabel("         If enabled, malformed text components will prevent compilation.", "ProjectProperties.content", tlm));
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
        }

    }

    ProjectPropertiesGeneral() {
        super(new BorderLayout());
    }
}
