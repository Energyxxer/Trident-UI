package com.energyxxer.trident.ui.dialogs.project_properties;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.global.temp.projects.TridentProject;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.ui.orderlist.*;
import com.energyxxer.trident.ui.styledcomponents.StyledPopupMenu;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DependencyToken implements CompoundActionModuleToken {
    enum DependencySource {
        PROJECT, DIRECTORY
    }

    private File root;
    private String name;
    private String label;
    private DependencySource source;

    private String dependencyPath;

    private boolean export = true;
    private ItemCheckboxAction exportCheckbox;
    private boolean combine = false;
    private ItemDropdownAction<String> modeDropdown;

    public DependencyToken(TridentProject project) {
        root = project.getRootDirectory();
        name = project.getName();
        source = DependencySource.PROJECT;

        this.dependencyPath = "$PROJECT_DIR$" + File.separator + ".." + File.separator + project.getRootDirectory().getName();

        label = name;
    }

    public DependencyToken(File root) {
        this.root = root;
        this.name = root.getName();
        this.source = DependencySource.DIRECTORY;

        this.dependencyPath = root.getAbsolutePath();

        label = root.getAbsolutePath();

        if(label.length() > 64) {
            label = "..." + label.substring(label.length() - 61);
        }
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
        list.add(new ItemButtonAction() {
            @Override
            public Image getIcon() {
                return Commons.getIcon("explorer").getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            }

            @Override
            public String getDescription() {
                return "Open in System Explorer";
            }

            @Override
            public void perform() {
                Commons.openInExplorer(root.getPath());
            }
        });



        modeDropdown = new ItemDropdownAction<>(new String[] {"Precompile", "Combine"});
        list.add(modeDropdown);
        setCombine(combine);
        modeDropdown.addChoiceListener(c -> combine = c.equals("Combine"));


        exportCheckbox = new ItemCheckboxAction() {
            {
                this.leftAligned = true;
            }

            @Override
            public String getDescription() {
                return "Export";
            }

            @Override
            public void onChange(boolean newValue) {
                export = newValue;
            }
        };
        setExport(export);
        list.add(exportCheckbox);
        return list;
    }

    @Override
    public String getTitle() {
        return label;
    }

    @Override
    public boolean ellipsisFromLeft() {
        return true;
    }

    public String getIconName() {
        return source == DependencySource.PROJECT ? "project" : "folder";
    }

    @Override
    public Image getIcon() {
        return Commons.getIcon(getIconName());
    }

    @Override
    public StyledPopupMenu generateMenu(@NotNull MenuContext context) {
        return null;
    }

    @Override
    public String getHint() {
        return null;
    }

    @Override
    public void onInteract() {
    }

    @Override
    public boolean equals(ModuleToken other) {
        return other instanceof DependencyToken && ((DependencyToken) other).root.equals(this.root);
    }

    public void setExport(boolean export) {
        this.export = export;
        if(exportCheckbox != null) exportCheckbox.setValue(export);
    }

    public void setCombine(boolean combine) {
        this.combine = combine;
        if(modeDropdown != null) modeDropdown.setValueIndex(combine ? 1 : 0);
    }

    public JsonObject createDependencyObj() {
        JsonObject obj = new JsonObject();
        obj.addProperty("path", dependencyPath);
        obj.addProperty("export", export);
        obj.addProperty("mode", combine ? "combine" : "precompile");
        return obj;
    }
}
