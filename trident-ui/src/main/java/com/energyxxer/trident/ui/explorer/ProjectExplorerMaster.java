package com.energyxxer.trident.ui.explorer;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.global.Preferences;
import com.energyxxer.trident.global.temp.projects.ProjectManager;
import com.energyxxer.trident.main.window.sections.quick_find.StyledExplorerMaster;
import com.energyxxer.trident.ui.explorer.base.ExplorerFlag;
import com.energyxxer.trident.ui.explorer.base.StandardExplorerItem;
import com.energyxxer.trident.ui.explorer.base.elements.ExplorerSeparator;
import com.energyxxer.trident.ui.modules.DraggableExplorerModuleToken;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.ui.modules.WorkspaceRootModuleToken;
import com.energyxxer.util.logger.Debug;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by User on 5/16/2017.
 */
public class ProjectExplorerMaster extends StyledExplorerMaster {
    private ArrayList<ModuleToken> tokenSources = new ArrayList<>();

    private String filepath = "why is this";

    public static final ExplorerFlag
            FLATTEN_EMPTY_PACKAGES = new ExplorerFlag("Flatten Empty Packages"),
            SHOW_PROJECT_FILES = new ExplorerFlag("Show Project Files");

    public ProjectExplorerMaster() {
        filepath += Math.random();
        explorerFlags.put(FLATTEN_EMPTY_PACKAGES, Preferences.get("explorer.flatten_empty_packages","true").equals("true"));
        explorerFlags.put(SHOW_PROJECT_FILES, Preferences.get("explorer.show_project_files","false").equals("true"));
        explorerFlags.put(ExplorerFlag.DEBUG_WIDTH, Preferences.get("explorer.debug_width","false").equals("true"));

        this.tokenSources.add(new WorkspaceRootModuleToken());

        this.setTransferHandler(new TransferHandler("filepath") {
            @NotNull
            @Override
            protected Transferable createTransferable(JComponent c) {
                Collection<DraggableExplorerModuleToken> tokens = selectedItems.stream().filter(i -> i.getToken() instanceof DraggableExplorerModuleToken).map(i -> ((DraggableExplorerModuleToken) i.getToken())).collect(Collectors.toList());
                Object[] rawFlavors = tokens.stream().map(DraggableExplorerModuleToken::getDataFlavor).distinct().toArray();
                DataFlavor[] flavors = Arrays.copyOf(rawFlavors, rawFlavors.length, DataFlavor[].class);

                return new Transferable() {
                    @Override
                    public DataFlavor[] getTransferDataFlavors() {
                        return flavors;
                    }

                    @Override
                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return flavor == DataFlavor.javaFileListFlavor;
                    }

                    @NotNull
                    @Override
                    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                        return tokens.stream().filter(t -> t.getDataFlavor() == flavor).map(DraggableExplorerModuleToken::getTransferData).collect(Collectors.toList());
                    }
                };
            }
        });

        refresh();
    }

    @Override
    public void refresh() {
        ProjectManager.setWorkspaceDir(Preferences.get("workspace_dir", Preferences.DEFAULT_WORKSPACE_PATH));
        ProjectManager.loadWorkspace();

        clearSelected();
        refresh(new ArrayList<>(this.getExpandedElements().stream().map(ModuleToken::getIdentifier).collect(Collectors.toSet())));
    }

    private void refresh(ArrayList<String> toOpen) {
        children.clear();
        flatList.clear();
        this.getExpandedElements().clear();

        for(ModuleToken source : tokenSources) {
            for(ModuleToken token : source.getSubTokens()) {
                this.children.add(new StandardExplorerItem(token, this, toOpen));
            }
            this.children.add(new ExplorerSeparator(this));
        }

        repaint();
    }

    @Override
    protected void selectionUpdated() {
        super.selectionUpdated();
        Commons.updateActiveProject();
    }

    public void saveExplorerTree() {
        StringBuilder sb = new StringBuilder();
        Collection<ModuleToken> expandedElements = this.getExpandedElements();
        for(ModuleToken elem : expandedElements) {
            sb.append(elem.getIdentifier());
            sb.append(File.pathSeparator);
        }
        Debug.log("Saving: " + sb);
        Preferences.put("open_tree", sb.toString());
    }

    public void openExplorerTree() {
        String openTree = Preferences.get("open_tree",null);
        if(openTree != null) {
            Debug.log("Opening: " + openTree);
            refresh(new ArrayList<>(Arrays.asList(openTree.split(Pattern.quote(File.pathSeparator)))));
        }
    }

    public String getFilepath() {
        Debug.log("getFilepath called");
        return filepath;
    }
}
