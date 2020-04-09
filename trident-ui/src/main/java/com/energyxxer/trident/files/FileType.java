package com.energyxxer.trident.files;

import com.energyxxer.commodore.functionlogic.functions.Function;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.dialogs.file_dialogs.FileDialog;
import com.energyxxer.trident.ui.dialogs.file_dialogs.FolderDialog;
import com.energyxxer.trident.ui.dialogs.file_dialogs.FunctionDialog;
import com.energyxxer.trident.ui.dialogs.file_dialogs.ProjectDialog;
import com.energyxxer.trident.ui.styledcomponents.StyledMenuItem;

import java.io.File;
import java.nio.file.Paths;
import java.util.function.Predicate;

/**
 * Created by User on 2/9/2017.
 */
public enum FileType {
    TDN(0, "Trident Function", "trident_file", ".tdn", FunctionDialog::create, (pr, pth) -> pr != null && pth.startsWith(pr + "datapack" + File.separator), str -> str.matches(Function.ALLOWED_PATH_REGEX)),
    MODEL(0, "Model", "model", ".json", FileDialog::create, (pr, pth) -> pr != null && pth.startsWith(pr + "resources" + File.separator)),
    LANG(0, "Language File", "lang", ".json", FileDialog::create, (pr, pth) -> pr != null && pth.startsWith(pr + "resources" + File.separator)),
    FUNCTION(0, "Function", "function", ".mcfunction", FileDialog::create, (pr, pth) -> pr != null && pth.startsWith(pr + "datapack" + File.separator), str -> str.matches(Function.ALLOWED_PATH_REGEX)),
    META(1, "Meta File", "meta", ".mcmeta", FileDialog::create, (pr, pth) -> pr != null && pth.startsWith(pr + "resources" + File.separator)),
    JSON(1, "JSON File", "json", ".json", FileDialog::create, (pr, pth) -> true),
    FILE(2, "File", "file", "", FileDialog::create, (pr, pth) -> true),
    FOLDER(2, "Folder", "folder", null, FolderDialog::create, (pr, pth) -> true),
    PROJECT(3, "Project", "project", null, ProjectDialog::create, (pr, pth) -> true),
    DATA_ROOT(4, "Data Pack Root", "data", null, (type, dest) -> {
        Paths.get(dest).resolve("datapack").toFile().mkdirs();
        TridentWindow.projectExplorer.refresh();
    }, (pr, pth) -> pth.equals(pr) && !Paths.get(pr).resolve("datapack").toFile().exists()),
    RESOURCES_ROOT(4, "Resource Pack Root", "resources", null, (type, dest) -> {
        Paths.get(dest).resolve("resources").toFile().mkdirs();
        TridentWindow.projectExplorer.refresh();
    }, (pr, pth) -> pth.equals(pr) && !Paths.get(pr).resolve("resources").toFile().exists());

    public final int group;
    public final String name;
    public final String icon;
    public final String extension;
    public final FileTypeDialog dialog;
    public final DirectoryValidator validator;
    public final Predicate<String> fileNameValidator;

    FileType(int group, String name, String icon, String extension, FileTypeDialog dialog, DirectoryValidator validator) {
        this(group, name, icon, extension, dialog, validator, s -> true);
    }

    FileType(int group, String name, String icon, String extension, FileTypeDialog dialog, DirectoryValidator validator, Predicate<String> fileNameValidator) {
        this.group = group;
        this.name = name;
        this.icon = icon;
        this.extension = extension;
        this.dialog = dialog;
        this.validator = validator;
        this.fileNameValidator = fileNameValidator;
    }

    public void create(String destination) {
        this.dialog.create(this, destination);
    }

    public StyledMenuItem createMenuItem(String newPath) {
        StyledMenuItem item = new StyledMenuItem(name, icon);
        item.addActionListener(e -> create(newPath));
        return item;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean canCreate(String projectDir, String path) {
        return validator.canCreate(projectDir, path);
    }
}
