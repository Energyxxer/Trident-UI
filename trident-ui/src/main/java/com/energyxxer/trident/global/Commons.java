package com.energyxxer.trident.global;

import com.energyxxer.commodore.module.CommandModule;
import com.energyxxer.commodore.standard.StandardDefinitionPacks;
import com.energyxxer.crossbow.compiler.out.BedrockModule;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.LazyTokenPatternMatch;
import com.energyxxer.trident.global.temp.projects.CrossbowProject;
import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.global.temp.projects.ProjectManager;
import com.energyxxer.trident.global.temp.projects.TridentProject;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.Tab;
import com.energyxxer.trident.ui.modules.FileModuleToken;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.ui.theme.change.ThemeChangeListener;
import com.energyxxer.util.ImageManager;
import com.energyxxer.util.Lazy;
import com.energyxxer.util.logger.Debug;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Commons {

    public static String DEFAULT_CARET_DISPLAY_TEXT = "-:-";

    public static String themeAssetsPath = "light_theme/";

    private static Lazy<CommandModule> defaultModule = new Lazy<> (() -> {
        CommandModule defaultModule = new CommandModule("Default Module");
        try {
            StandardDefinitionPacks.MINECRAFT_JAVA_LATEST_SNAPSHOT.load();
            defaultModule.importDefinitions(StandardDefinitionPacks.MINECRAFT_JAVA_LATEST_SNAPSHOT);
        } catch(IOException x) {
            Debug.log(x.toString(), Debug.MessageType.ERROR);
        }
        return defaultModule;
    });

    private static Lazy<BedrockModule> defaultBedrockModule = new Lazy<> (() -> {
        BedrockModule defaultModule = new BedrockModule("Default Module");
        try {
            StandardDefinitionPacks.MINECRAFT_BEDROCK_LATEST_RELEASE.load();
            defaultModule.importDefinitions(StandardDefinitionPacks.MINECRAFT_BEDROCK_LATEST_RELEASE);
        } catch(IOException x) {
            Debug.log(x.toString(), Debug.MessageType.ERROR);
        }
        return defaultModule;
    });

    static {
        ThemeChangeListener.addThemeChangeListener(t -> {
            themeAssetsPath = t.getString("Assets.path","default:light_theme/");
        }, true);
    }

    public static boolean isSpecialCharacter(char ch) {
        return "\b\r\n\t\f\u007F\u001B".contains("" + ch);
    }

    public static void showInExplorer(String path) {
        try {
            if(System.getProperty("os.name").startsWith("Windows")) {
                Runtime.getRuntime().exec("Explorer.exe /select," + path);
            } else if(Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(new File(path).getParentFile());
            } else {
                Debug.log("Couldn't show file '" + path + "': Desktop is not supported", Debug.MessageType.ERROR);
            }
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    public static void openInExplorer(String path) {
        try {
            if(System.getProperty("os.name").startsWith("Windows")) {
                Runtime.getRuntime().exec("Explorer.exe \"" + path + "\""); //can't believe you don't have to escape it
            } else if(Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(new File(path));
            } else {
                Debug.log("Couldn't open file '" + path + "': Desktop is not supported", Debug.MessageType.ERROR);
            }
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    private static String getIconPath(String name) {
        return "/assets/icons/" + themeAssetsPath + name + ".png";
    }

    public static BufferedImage getIcon(String name) {
        return ImageManager.load(getIconPath(name));
    }

    public static void updateActiveProject() {
        if(TridentWindow.toolbar != null && TridentWindow.projectExplorer != null)
            TridentWindow.toolbar.setActiveProject(getActiveProject());
    }

    public static Project getActiveProject() {
        Project selected = null;

        Tab selectedTab = TridentWindow.tabManager.getSelectedTab();

        List<ModuleToken> selectedTokens = TridentWindow.projectExplorer.getSelectedTokens();
        ArrayList<FileModuleToken> selectedFiles = new ArrayList<>();
        for(ModuleToken token : selectedTokens) {
            if(token instanceof FileModuleToken) selectedFiles.add((FileModuleToken) token);
        }

        if(selectedTab != null && selectedTab.token instanceof FileModuleToken) {
            selected = ProjectManager.getAssociatedProject(((FileModuleToken) selectedTab.token).getFile());
        } else if(selectedFiles.size() > 0) {
            selected = ProjectManager.getAssociatedProject(selectedFiles.get(0).getFile());
        }
        return selected;
    }

    public static void compileActive() {
        compile(Commons.getActiveProject());
    }

    public static void compile(Project project) {
        if(project instanceof TridentProject) ProcessManager.queueProcess(new TridentCompilerWrapper((TridentProject) project));
        if(project instanceof CrossbowProject) ProcessManager.queueProcess(new CrossbowCompilerWrapper((CrossbowProject) project));
    }

    public static void indexActive() {
        index(Commons.getActiveProject());
    }

    public static void index(Project project) {
        if(project != null) ProcessManager.queueProcess(new IndexingProcess(project));
    }

    public static CommandModule getDefaultModule() {
        return defaultModule.getValue();
    }

    public static BedrockModule getDefaultBedrockModule() {
        return defaultBedrockModule.getValue();
    }

    public static LazyTokenPatternMatch getActiveTridentProductions() {
        Project activeProject = getActiveProject();
        if(activeProject != null) {
            return activeProject.getFileStructure();
        }
        return null;
    }

    public static LazyTokenPatternMatch getActiveCrossbowProductions() {
        Project activeProject = getActiveProject();
        if(activeProject != null) {
            return activeProject.getFileStructure();
        }
        return null;
    }

    public static Image getProjectIcon() {
        return Commons.getIcon("project").getScaledInstance(16, 16, Image.SCALE_SMOOTH);
    }
}
