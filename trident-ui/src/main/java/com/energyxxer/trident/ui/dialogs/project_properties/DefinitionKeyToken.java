package com.energyxxer.trident.ui.dialogs.project_properties;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.ui.commodoreresources.DefinitionPacks;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.ui.orderlist.OrderListAction;
import com.energyxxer.trident.ui.orderlist.OrderListToken;
import com.energyxxer.trident.ui.styledcomponents.StyledPopupMenu;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DefinitionKeyToken implements OrderListToken {

    enum DefinitionSource {
        UNKNOWN, IDE, PROJECT;
    }
    private String packName;

    @NotNull
    private DefinitionSource source;
    DefinitionKeyToken(String packName, @NotNull DefinitionSource source) {
        this.packName = packName;
        this.source = source;
    }

    @Override
    public @NotNull List<OrderListAction> getActions() {
        ArrayList<OrderListAction> list = new ArrayList<>();
        list.add(new OrderListAction() {
            @Override
            public Image getIcon() {
                return Commons.getIcon("clear").getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            }

            @Override
            public String getDescription() {
                return "Remove";
            }

            @Override
            public int perform() {
                return 0;
            }
        });
        list.add(new OrderListAction() {
            @Override
            public Image getIcon() {
                return Commons.getIcon("triangle_down").getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            }

            @Override
            public String getDescription() {
                return "Move Down";
            }

            @Override
            public int perform() {
                return 1;
            }
        });
        list.add(new OrderListAction() {
            @Override
            public Image getIcon() {
                return Commons.getIcon("triangle_up").getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            }

            @Override
            public String getDescription() {
                return "Move Up";
            }

            @Override
            public int perform() {
                return 2;
            }
        });
        if(packName != null && source != DefinitionSource.UNKNOWN) {
            list.add(new OrderListAction() {
                @Override
                public Image getIcon() {
                    return Commons.getIcon("explorer").getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                }

                @Override
                public String getDescription() {
                    return "Show in System Explorer";
                }

                @Override
                public int perform() {
                    String directory = source == DefinitionSource.IDE ? DefinitionPacks.DEF_PACK_DIR_PATH : ProjectProperties.project.getRootDirectory().getPath() + File.separator + "defpacks" + File.separator;
                    File fileToShow = new File(directory + packName);
                    if(!fileToShow.isDirectory()) {
                        fileToShow = new File(fileToShow.getPath() + ".zip");
                    }
                    Commons.showInExplorer(fileToShow.getPath());
                    return -1;
                }
            });
        }
        return list;
    }

    public String getPackName() {
        return packName;
    }

    public DefinitionSource getSource() {
        return source;
    }

    @Override
    public String getTitle() {
        return packName != null ? packName : "DEFAULT - provided by target version";
    }

    public String getIconName() {
        String iconName = "warn";
        if(packName == null) iconName = "blank";
        if(source == DefinitionSource.PROJECT) iconName = "project_content";
        if(source == DefinitionSource.IDE) iconName = "package";
        return iconName;
    }

    @Override
    public Image getIcon() {
        return Commons.getIcon(getIconName());
    }

    @Override
    public StyledPopupMenu generateMenu() {
        return null;
    }

    @Override
    public String getHint() {
        return source == DefinitionSource.UNKNOWN && packName != null ? "Not found" : null;
    }

    @Override
    public boolean equals(ModuleToken other) {
        return other instanceof DefinitionKeyToken;
    }
}
