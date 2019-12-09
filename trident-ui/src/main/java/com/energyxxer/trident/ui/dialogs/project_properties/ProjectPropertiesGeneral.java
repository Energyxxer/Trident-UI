package com.energyxxer.trident.ui.dialogs.project_properties;

import com.energyxxer.commodore.versioning.JavaEditionVersion;
import com.energyxxer.commodore.versioning.Version;
import com.energyxxer.trident.ui.commodoreresources.DefinitionPacks;
import com.energyxxer.trident.ui.styledcomponents.*;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;

import javax.swing.*;
import java.awt.*;

class ProjectPropertiesGeneral extends JPanel {

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

            StyledLabel label = new StyledLabel("General", "ProjectProperties.content.header");
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

            //region Target Version
            {
                StyledLabel label = new StyledLabel("Target Version:", "ProjectProperties.content");
                label.setStyle(Font.BOLD);
                content.add(label);
                content.add(new StyledLabel("Determines which type definitions to use by default,", "ProjectProperties.content"));
                content.add(new StyledLabel("as well as which commands are allowed.", "ProjectProperties.content"));
            }
            {
                JavaEditionVersion[] knownVersions = DefinitionPacks.getKnownJavaVersions();
                JavaEditionVersion[] shownVersions = new JavaEditionVersion[knownVersions.length+1];
                shownVersions[0] = new UnsetVersion();
                System.arraycopy(knownVersions, 0, shownVersions, 1, knownVersions.length);

                StyledDropdownMenu<JavaEditionVersion> versionDropdown = new StyledDropdownMenu<>(shownVersions, "ProjectProperties");
                versionDropdown.setPopupFactory(StyledPopupMenu::new);
                versionDropdown.setPopupItemFactory(StyledMenuItem::new);
                ProjectProperties.addOpenEvent(p -> {
                    versionDropdown.setValue(p.getTargetVersion());
                });
                ProjectProperties.addApplyEvent(p -> {
                    JavaEditionVersion value = versionDropdown.getValue();
                    if(value instanceof UnsetVersion) {
                        p.setTargetVersion(null);
                    } else {
                        p.setTargetVersion(value);
                    }
                });
                content.add(versionDropdown);
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

            //region Language Level
            {
                StyledLabel label = new StyledLabel("Language Level:", "ProjectProperties.content");
                label.setStyle(Font.BOLD);
                content.add(label);
            }
            {
                StyledLabel label = new StyledLabel("The default level of abstraction allowed for this project.", "ProjectProperties.content");
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
                margin.setMinimumSize(new Dimension(200,15));
                margin.setMaximumSize(new Dimension(200,15));
                margin.setOpaque(false);
                margin.setAlignmentX(Component.LEFT_ALIGNMENT);

                content.add(margin);
            }
            //endregion

            //region Default Namespace
            {
                StyledLabel label = new StyledLabel("Default Namespace:", "ProjectProperties.content");
                label.setStyle(Font.BOLD);
                content.add(label);
            }
            {
                StyledLabel label = new StyledLabel("Used for Trident-generated functions.", "ProjectProperties.content");
                content.add(label);
            }
            {
                StyledTextField namespaceField = new StyledTextField("trident_temp_please_specify_default_namespace","ProjectProperties.content");
                namespaceField.setPreferredSize(new Dimension(300,25));
                namespaceField.setMaximumSize(new Dimension(200,25));
                namespaceField.setAlignmentX(Component.LEFT_ALIGNMENT);
                ProjectProperties.addOpenEvent(p -> namespaceField.setText(p.getDefaultNamespace()));
                ProjectProperties.addApplyEvent(p -> p.setDefaultNamespace(namespaceField.getText()));

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

            //region Strict NBT
            {
                StyledCheckBox strictNBT = new StyledCheckBox("Strict NBT","ProjectProperties.content");
                strictNBT.setAlignmentX(Component.LEFT_ALIGNMENT);
                ProjectProperties.addOpenEvent(p -> strictNBT.setSelected(p.isStrictNBT()));
                ProjectProperties.addApplyEvent(p -> p.setStrictNBT(strictNBT.isSelected()));

                content.add(strictNBT);
                content.add(new StyledLabel("         If enabled, type errors in NBT tags will prevent compilation.", "ProjectProperties.content"));
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

            //region Strict Text Components
            {
                StyledCheckBox strictTextComponents = new StyledCheckBox("Strict Text Components","ProjectProperties.content");
                strictTextComponents.setAlignmentX(Component.LEFT_ALIGNMENT);
                ProjectProperties.addOpenEvent(p -> strictTextComponents.setSelected(p.isStrictTextComponents()));
                ProjectProperties.addApplyEvent(p -> p.setStrictTextComponents(strictTextComponents.isSelected()));

                content.add(strictTextComponents);
                content.add(new StyledLabel("         If enabled, malformed text components will prevent compilation.", "ProjectProperties.content"));
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

    ProjectPropertiesGeneral() {
        super(new BorderLayout());
    }

    private static class UnsetVersion extends JavaEditionVersion {
        public UnsetVersion() {
            super(0, 0, 0);
        }

        @Override
        public boolean isComparableWith(Version other) {
            return false;
        }

        @Override
        public String toString() {
            return "Unset";
        }
    }
}
