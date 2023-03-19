package com.energyxxer.trident.guardian;

import com.energyxxer.commodore.functionlogic.functions.Function;
import com.energyxxer.guardian.GuardianBinding;
import com.energyxxer.guardian.files.FileType;
import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.global.temp.Lang;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.commodoreresources.Plugins;
import com.energyxxer.guardian.ui.dialogs.file_dialogs.FileDialog;
import com.energyxxer.guardian.ui.theme.ThemeManager;
import com.energyxxer.trident.Trident;
import com.energyxxer.trident.guardian.dialogs.FunctionDialog;
import com.energyxxer.trident.guardian.settings.SettingsCompiler;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;

public class GuardianTridentBindings implements GuardianBinding {

    public static FileType TRIDENT_FILE_TYPE;

    public static FileType MODEL_FILE_TYPE;
    public static FileType LANG_FILE_TYPE;
    public static FileType FUNCTION_FILE_TYPE;
    public static FileType META_FILE_TYPE;

    public static FileType DATA_ROOT_FILE_TYPE;
    public static FileType RESOURCES_ROOT_FILE_TYPE;

    public void setup() {
        TridentLanguage.load();
        NBTTMLanguage.load();
        Lang.JSON.addExtension(Trident.PROJECT_FILE_NAME.substring(1));
        Lang.JSON.addExtension(Trident.PROJECT_BUILD_FILE_NAME.substring(1));
        Lang.PRISMARINE_SYNTAX.addExtension("tdnmeta");

        ThemeManager.registerSyntaxThemeFromJar("Trident Syntax Dark");
        ThemeManager.registerSyntaxThemeFromJar("NBTTM Syntax Dark");

        Plugins.registerPluginLoader(TridentPluginLoader.INSTANCE);

        TRIDENT_FILE_TYPE = new FileType(
                0,
                "Trident Function",
                "trident_file",
                ".tdn",
                FunctionDialog::create,
                (pr, pth) -> pr != null && pth.startsWith(pr + "datapack" + File.separator),
                str -> str.matches(Function.ALLOWED_PATH_REGEX)
        );

        MODEL_FILE_TYPE = new FileType(
                0,
                "Model",
                "model",
                ".json",
                FileDialog::create,
                (pr, pth) -> pr != null && pth.startsWith(pr + "resources" + File.separator)
        );
        LANG_FILE_TYPE = new FileType(
                0,
                "Language File (.json)",
                "lang",
                ".json",
                FileDialog::create,
                (pr, pth) -> pr != null && pth.startsWith(pr + "resources" + File.separator)
        );
        FUNCTION_FILE_TYPE = new FileType(
                0,
                "Function",
                "function",
                ".mcfunction",
                FileDialog::create,
                (pr, pth) -> pr != null && pth.startsWith(pr + "datapack" + File.separator),
                str -> str.matches(Function.ALLOWED_PATH_REGEX)
        );
        META_FILE_TYPE = new FileType(
                10,
                "Meta File",
                "meta",
                ".mcmeta",
                FileDialog::create,
                (pr, pth) -> pr != null && pth.startsWith(pr + "resources" + File.separator)
        );

        DATA_ROOT_FILE_TYPE = new FileType(40, "Data Pack Root", "data", null, (type, dest) -> {
            Paths.get(dest).resolve("datapack").toFile().mkdirs();
            GuardianWindow.projectExplorer.refresh();
        }, (pr, pth) -> pth.equals(pr) && TridentProject.PROJECT_TYPE.isProjectRoot(new File(pr)) && !Paths.get(pr).resolve("datapack").toFile().exists());

        RESOURCES_ROOT_FILE_TYPE = new FileType(40, "Resource Pack Root", "resources", null, (type, dest) -> {
            Paths.get(dest).resolve("resources").toFile().mkdirs();
            GuardianWindow.projectExplorer.refresh();
        }, (pr, pth) -> pth.equals(pr) && TridentProject.PROJECT_TYPE.isProjectRoot(new File(pr)) && !Paths.get(pr).resolve("resources").toFile().exists());

        SettingsCompiler.load();
    }

    @Override
    public boolean usesJavaEditionDefinitions() {
        return true;
    }

    @Override
    public void setupSettingsSections(HashMap<String, JPanel> sectionPanes) {
        sectionPanes.put("Compiler", new SettingsCompiler());
    }

    @Override
    public Image getIconForFile(File file) {
        String extension = "";
        if(file.getName().lastIndexOf('.') >= 0) {
            extension = file.getName().substring(file.getName().lastIndexOf('.'));
        }
        switch(extension) {
            case ".json": {
                if(file.getName().equals("sounds.json"))
                    return Commons.getIcon("sound_config");
                else if(file.getParentFile().getName().equals("blockstates"))
                    return Commons.getIcon("blockstate");
                else if(file.getParentFile().getName().equals("lang"))
                    return Commons.getIcon("lang");
                break;
            }
            case ".mcmeta":
            case Trident.PROJECT_FILE_NAME:
            case Trident.PROJECT_BUILD_FILE_NAME:
                return Commons.getIcon("meta");
            case ".nbt":
                return Commons.getIcon("structure");
        }
        return null;
    }
}
