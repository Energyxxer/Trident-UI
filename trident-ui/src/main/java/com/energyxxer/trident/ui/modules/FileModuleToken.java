package com.energyxxer.trident.ui.modules;

import com.energyxxer.trident.compiler.TridentCompiler;
import com.energyxxer.trident.files.FileType;
import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.global.FileManager;
import com.energyxxer.trident.global.Preferences;
import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.global.temp.projects.ProjectManager;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.Tab;
import com.energyxxer.trident.ui.common.MenuItems;
import com.energyxxer.trident.ui.dialogs.PromptDialog;
import com.energyxxer.trident.ui.display.DisplayModule;
import com.energyxxer.trident.ui.editor.TridentEditorModule;
import com.energyxxer.trident.ui.explorer.ProjectExplorerMaster;
import com.energyxxer.trident.ui.imageviewer.ImageViewer;
import com.energyxxer.trident.ui.styledcomponents.StyledMenu;
import com.energyxxer.trident.ui.styledcomponents.StyledMenuItem;
import com.energyxxer.trident.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.util.FileUtil;
import com.energyxxer.util.logger.Debug;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileModuleToken implements ModuleToken, DraggableExplorerModuleToken, DropTargetExplorerModuleToken {
    public static ModuleTokenFactory<FileModuleToken> factory = str -> {
        if(!str.startsWith("file://")) return null;
        String path = str.substring("file://".length());
        File file = new File(path);
        return file.exists() ? new FileModuleToken(file) : null;
    };

    private static final int MAX_RECENT_FILES = 16;
    public static final ArrayList<File> recentFiles = new ArrayList<>(MAX_RECENT_FILES);

    public static final Preferences.SettingPref<Boolean> SHOW_EXTENSIONS_EXPLORER = new Preferences.SettingPref<>("settings.behavior.hide_extensions.explorer", true, Boolean::new);
    public static final Preferences.SettingPref<Boolean> SHOW_EXTENSIONS_TAB = new Preferences.SettingPref<>("settings.behavior.hide_extensions.tab", true, Boolean::new);
    public static final Preferences.SettingPref<Boolean> LOAD_PNGS = new Preferences.SettingPref<>("settings.behavior.load_pngs", true, Boolean::new);

    private final File file;
    private boolean isProjectRoot;
    private String overrideIconName = null;

    private String subTitle;
    private File associatedProjectRoot;

    public FileModuleToken(File file) {
        this.file = file;

        Project associatedProject = ProjectManager.getAssociatedProject(file);
        if(associatedProject != null) {
            this.associatedProjectRoot = associatedProject.getRootDirectory();
            if(file.equals(associatedProjectRoot)) {
                subTitle = "";
            } else {
                subTitle = associatedProjectRoot.toPath().relativize(file.getParentFile().toPath()).toString();
            }
            if(subTitle.isEmpty()) subTitle = null;
            else {
                subTitle = "(" + subTitle + ")";
            }
        } else {
            subTitle = "(" + file.getParentFile().toPath().toString() + ")";
        }

        this.isProjectRoot = isProjectRoot(file);
    }

    public static boolean isProjectRoot(File file) {
        if(!file.isDirectory()) return false;
        if(file.toPath().resolve(".tdnproj").toFile().exists()) return true;
        if(file.toPath().resolve(".cbwproj").toFile().exists()) return true;
        return false;
    }

    @Override
    public String getTitle(TokenContext context) {
        if((context == TokenContext.EXPLORER && SHOW_EXTENSIONS_EXPLORER.get()) || (context == TokenContext.TAB && SHOW_EXTENSIONS_TAB.get())) {
            return file.getName();
        } else {
            return getNameWithoutExtension();
        }
    }

    private String getNameWithoutExtension() {
        String name = file.getName();
        if(name.lastIndexOf('.') <= 0) return name;
        return name.substring(0, name.lastIndexOf('.'));
    }

    @Override
    public String getSubTitle() {
        return subTitle;
    }

    @Override
    public File getAssociatedProjectRoot() {
        return associatedProjectRoot;
    }

    @Override
    public Image getIcon() {
        if(overrideIconName != null) return Commons.getIcon(overrideIconName);
        if(file.isDirectory()) {
            if(isProjectRoot) {
                if(file.toPath().resolve(".cbwproj").toFile().exists()) {
                    return Commons.getIcon("project_cbw");
                } else {
                    return Commons.getIcon("project");
                }
            } else if(isProjectRoot(file.getParentFile())) {
                if(file.getName().equals("datapack")) return Commons.getIcon("data");
                if(file.getName().equals("resources")) return Commons.getIcon("resources");
            }
            return Commons.getIcon("folder");
        } else {
            String extension = "";
            if(file.getName().lastIndexOf(".") >= 0) {
                extension = file.getName().substring(file.getName().lastIndexOf("."));
            }
            if(extension.equals(".png") && LOAD_PNGS.get()) {
                try {
                    return ImageIO.read(file);
                } catch(IOException x) {
                    Debug.log("Couldn't load image from file '" + file + "'");
                    return Commons.getIcon("warn");
                }
            }
            switch(extension) {
                case ".tdn":
                    return Commons.getIcon("trident_file");
                case ".cbw":
                    return Commons.getIcon("crossbow_file");
                case ".mcfunction":
                    return Commons.getIcon("function");
                case ".mp3":
                case ".ogg":
                    return Commons.getIcon("audio");
                case ".json": {
                    if(file.getName().equals("sounds.json"))
                        return Commons.getIcon("sound_config");
                    else if(file.getParentFile().getName().equals("blockstates"))
                        return Commons.getIcon("blockstate");
                    else if(file.getParentFile().getName().equals("lang"))
                        return Commons.getIcon("lang");
                    else
                        return Commons.getIcon("json");
                }
                case ".mcmeta":
                case TridentCompiler.PROJECT_FILE_NAME:
                    return Commons.getIcon("meta");
                case ".nbt":
                    return Commons.getIcon("structure");
                case ".png":
                    return Commons.getIcon("image");
                default: return Commons.getIcon("file");
            }
        }
    }

    @Override
    public String getHint() {
        return file.getPath();
    }

    @Override
    public Collection<ModuleToken> getSubTokens() {
        ArrayList<ModuleToken> children = new ArrayList<>();
        int firstFileIndex = 0;
        File[] subFiles = file.listFiles();
        if(subFiles != null) {
            for (File subFile : subFiles) {
                FileModuleToken subToken = new FileModuleToken(subFile);
                if(this.isProjectRoot) {
                    //subToken.overrideIconName = subFile.getName().equals("datapack") ? "data" : subFile.getName().equals("resources") ? "resources" : null;
                }
                if (subFile.isDirectory()) {
                    if(!this.isProjectRoot || !subFile.getName().equals(".tdnui") || TridentWindow.projectExplorer.getFlag(ProjectExplorerMaster.SHOW_PROJECT_FILES)) {
                        children.add(firstFileIndex, subToken);
                        firstFileIndex++;
                    }
                }
                else {
                    if(!this.isProjectRoot || !subFile.getName().equals(".tdnproj") || TridentWindow.projectExplorer.getFlag(ProjectExplorerMaster.SHOW_PROJECT_FILES)) {
                        children.add(subToken);
                    }
                }
            }
        }
        return children;
    }

    @Override
    public boolean isExpandable() {
        return file.isDirectory();
    }

    @Override
    public DisplayModule createModule(Tab tab) {
        if(file.isFile()) {
            String name = file.getName();
            if(name.endsWith(".png")) {
                addRecentFile(file);
                return new ImageViewer(file);
            } else if(name.endsWith(".ogg") || name.endsWith(".mp3")) {

            } else {
                addRecentFile(file);
                return new TridentEditorModule(tab, file);
            }
        }
        return null;
    }

    @Override
    public boolean isModuleSource() {
        return file.isFile();
    }

    private static void addRecentFile(File file) {
        recentFiles.remove(file);
        recentFiles.add(0, file);
        while(recentFiles.size() >= MAX_RECENT_FILES) {
            recentFiles.remove(MAX_RECENT_FILES-1);
        }
    }

    @Override
    public void onInteract() {

    }

    @Override
    public StyledPopupMenu generateMenu(@NotNull ModuleToken.TokenContext context) {
        StyledPopupMenu menu = new StyledPopupMenu();

        String path = getPath();

        String newPath;
        if(file.isDirectory()) newPath = path;
        else newPath = file.getParent();

        if(context == TokenContext.EXPLORER) {
            StyledMenu newMenu = new StyledMenu("New");

            menu.add(newMenu);

            // --------------------------------------------------

            Project project = ProjectManager.getAssociatedProject(file);

            String projectDir = (project != null) ? project.getRootDirectory().getPath() + File.separator : null;

            int lastGroup = 0;

            for(FileType type : FileType.values()) {
                if(type.canCreate(projectDir, path + File.separator)) {
                    if(type.group != lastGroup) {
                        newMenu.addSeparator();
                        lastGroup = type.group;
                    }
                    newMenu.add(type.createMenuItem(newPath));
                }
            }
        }
        menu.addSeparator();


        if(context == TokenContext.EXPLORER) {
            List<ModuleToken> selectedTokens = TridentWindow.projectExplorer.getSelectedTokens();
            ArrayList<FileModuleToken> selectedFiles = new ArrayList<>();
            selectedFiles.add(this);
            for(ModuleToken token : selectedTokens) {
                if(token instanceof FileModuleToken && token != this) selectedFiles.add((FileModuleToken) token);
            }


            menu.add(MenuItems.fileItem(MenuItems.FileMenuItem.COPY));
            menu.add(MenuItems.fileItem(MenuItems.FileMenuItem.PASTE));


            menu.addSeparator();

            StyledMenuItem renameItem = MenuItems.fileItem(MenuItems.FileMenuItem.RENAME);
            renameItem.addActionListener(e -> {
                if(selectedFiles.size() != 1) return;

                String pathToRename = selectedFiles.get(0).getPath();
                String name = new File(pathToRename).getName();
                String rawName = name.substring(0, name.contains(".") ? name.lastIndexOf(".") : name.length());
                final String pathToParent = pathToRename.substring(0, pathToRename.lastIndexOf(name));

                String newName = new PromptDialog("Rename", "Enter a new name for the file:", name) {
                    @Override
                    protected boolean validate(String str) {
                        return str.trim().length() > 0 && FileUtil.validateFilename(str)
                                && !new File(pathToParent + str).exists();
                    }

                    @Override
                    protected int getSelectionEnd() {
                        return rawName.length();
                    }
                }.result;

                if (newName != null) {
                    if (ProjectManager.renameFile(new File(pathToRename), newName)) {
                        TridentWindow.projectExplorer.refresh();
                        TridentWindow.tabManager.openTabs.forEach(
                                tab -> {
                                    if(tab.token instanceof FileModuleToken && ((FileModuleToken) tab.token).getFile().equals(this.getFile())) {
                                        tab.transform(new FileModuleToken(new File(pathToParent + newName)));
                                    }
                                }
                        );
                        TridentWindow.tabManager.saveOpenTabs();
                        TridentWindow.tabList.repaint();
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "<html>The action can't be completed because the folder or file is open in another program.<br>Close the folder and try again.</html>",
                                "An error occurred.", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            menu.add(renameItem);



            StyledMenuItem deleteItem = MenuItems.fileItem(MenuItems.FileMenuItem.DELETE);
            deleteItem.setEnabled(selectedFiles.size() >= 1);
            ArrayList<String> selectedPaths = new ArrayList<>();
            for(FileModuleToken file : selectedFiles) {
                selectedPaths.add(file.getPath());
            }
            deleteItem.addActionListener(e -> FileManager.delete(selectedPaths));
            menu.add(deleteItem);

            menu.addSeparator();
        }


        StyledMenuItem openInSystemItem = new StyledMenuItem("Show in System Explorer", "explorer");
        openInSystemItem.addActionListener(e -> Commons.showInExplorer(path));

        menu.add(openInSystemItem);

        return menu;
    }

    public File getFile() {
        return file;
    }

    public String getPath() {
        return file.getPath();
    }

    @Override
    public String getIdentifier() {
        return "file://" + getPath();
    }

    @Override
    public boolean equals(ModuleToken other) {
        return other instanceof FileModuleToken && ((FileModuleToken) other).file.equals(this.file);
    }

    @Override
    public String toString() {
        return "[FileModuleToken: " + getPath() + "]";
    }

    public File getDragDestination() {
        File destination = getFile().isDirectory() ? getFile() : getFile().getParentFile();
        return destination.exists() ? destination : null;
    }

    @Override
    public boolean canAcceptMove(DraggableExplorerModuleToken[] draggables) {
        File destination = getDragDestination();
        if(destination == null) return false;
        for(DraggableExplorerModuleToken draggable : draggables) {
            if(!(draggable instanceof FileModuleToken)) return false;
            if(destination.equals(((FileModuleToken) draggable).file.getParentFile())) return false;
            if(destination.toPath().startsWith(((FileModuleToken) draggable).file.toPath())) return false;
        }
        return true;
    }

    @Override
    public boolean canAcceptCopy(DraggableExplorerModuleToken[] draggables) {
        File destination = getDragDestination();
        if(destination == null) return false;
        for(DraggableExplorerModuleToken draggable : draggables) {
            if(!(draggable instanceof FileModuleToken)) return false;
            if(destination.toPath().startsWith(((FileModuleToken) draggable).file.toPath())) return false;
        }
        return true;
    }

    @Override
    public DataFlavor getDataFlavor() {
        return DataFlavor.javaFileListFlavor;
    }

    @Override
    public File getTransferData() {
        return file;
    }
}
