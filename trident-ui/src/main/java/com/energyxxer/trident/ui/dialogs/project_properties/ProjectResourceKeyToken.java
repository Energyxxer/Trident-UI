package com.energyxxer.trident.ui.dialogs.project_properties;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.ui.commodoreresources.DefinitionPacks;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.ui.orderlist.ItemAction;
import com.energyxxer.trident.ui.orderlist.ItemButtonAction;
import com.energyxxer.trident.ui.orderlist.CompoundActionModuleToken;
import com.energyxxer.trident.ui.styledcomponents.StyledPopupMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectResourceKeyToken implements CompoundActionModuleToken {

    enum Source {
        UNKNOWN, IDE, PROJECT;
    }

    enum Type {
        DEFINITION_PACK("defpacks"), PLUGIN("plugins");

        final String subdirName;

        Type(String subdirName) {
            this.subdirName = subdirName;
        }
    }

    @NotNull
    private Type type;
    @Nullable
    private String packName;
    @NotNull
    private ProjectResourceKeyToken.Source source;

    ProjectResourceKeyToken(@NotNull Type type, @Nullable String packName, @NotNull ProjectResourceKeyToken.Source source) {
        this.type = type;
        this.packName = packName;
        this.source = source;
    }

    @Override
    public @NotNull List<ItemAction> getActions() {
        ArrayList<ItemAction> list = new ArrayList<>();
        list.add(new ItemButtonAction() {
            @Override
            public Image getIcon() {
                return Commons.getIcon("clear").getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            }

            @Override
            public String getDescription() {
                return "Remove";
            }

            @Override
            public int getActionCode() {
                return 0;
            }
        });
        list.add(new ItemButtonAction() {
            @Override
            public Image getIcon() {
                return Commons.getIcon("triangle_down").getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            }

            @Override
            public String getDescription() {
                return "Move Down";
            }

            @Override
            public int getActionCode() {
                return 1;
            }
        });
        list.add(new ItemButtonAction() {
            @Override
            public Image getIcon() {
                return Commons.getIcon("triangle_up").getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            }

            @Override
            public String getDescription() {
                return "Move Up";
            }

            @Override
            public int getActionCode() {
                return 2;
            }
        });
        if(packName != null && source != Source.UNKNOWN) {
            list.add(new ItemButtonAction() {
                @Override
                public Image getIcon() {
                    return Commons.getIcon("explorer").getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                }

                @Override
                public String getDescription() {
                    return "Show in System Explorer";
                }

                @Override
                public void perform() {
                    String directory = source == Source.IDE ? DefinitionPacks.DEF_PACK_DIR_PATH : ProjectProperties.project.getRootDirectory().getPath() + File.separator + type.subdirName + File.separator;
                    File fileToShow = new File(directory + packName);
                    if(!fileToShow.isDirectory()) {
                        fileToShow = new File(fileToShow.getPath() + ".zip");
                    }
                    Commons.showInExplorer(fileToShow.getPath());
                }
            });
        }

        return list;
    }

    public String getPackName() {
        return packName;
    }

    public Source getSource() {
        return source;
    }

    @Override
    public String getTitle(TokenContext context) {
        return packName != null ? packName : "DEFAULT - provided by target version";
    }

    public String getIconName() {
        String iconName = "warn";
        if(packName == null) iconName = "blank";
        if(source == Source.PROJECT) iconName = "project_content";
        if(source == Source.IDE) iconName = "package";
        return iconName;
    }

    @Override
    public Image getIcon() {
        return Commons.getIcon(getIconName());
    }

    @Override
    public StyledPopupMenu generateMenu(@NotNull ModuleToken.TokenContext context) {
        return null;
    }

    @Override
    public String getHint() {
        return source == Source.UNKNOWN && packName != null ? "Not found" : null;
    }

    @Override
    public boolean equals(ModuleToken other) {
        return other instanceof ProjectResourceKeyToken;
    }
}
