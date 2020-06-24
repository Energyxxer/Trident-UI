package com.energyxxer.trident.global;

import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.dialogs.ConfirmDialog;
import com.energyxxer.trident.ui.modules.FileModuleToken;
import com.energyxxer.trident.util.FileCommons;
import com.energyxxer.trident.util.ProjectUtil;

import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

/**
 * Created by User on 2/10/2017.
 */
public class FileManager {

    public static void delete(List<String> files) {
        if(files.size() <= 0) return;
        StringBuilder subject = new StringBuilder();

        if(files.size() > 1) {

            HashMap<String, Integer> contents = new HashMap<>();
            contents.put("function", 0);
            contents.put("path", 0);
            contents.put("model", 0);
            contents.put("sound index file", 0);
            contents.put("meta file", 0);
            contents.put("language file", 0);
            contents.put("texture", 0);
            contents.put("sound", 0);
            contents.put("file", 0);
            contents.put("folder", 0);

            for (String path : files) {
                if (path.endsWith(".tdn") || path.endsWith(".mcfunction")) {
                    contents.put("function", contents.get("function") + 1);
                } else if (path.endsWith(".json")) {
                    if (path.endsWith(File.separator + "sounds.json")) {
                        contents.put("sound index file", contents.get("sound index file") + 1);
                    } else {
                        contents.put("model", contents.get("model") + 1);
                    }
                } else if (path.endsWith(".mcmeta")) {
                    contents.put("meta file", contents.get("meta file") + 1);
                } else if (path.endsWith(".lang")) {
                    contents.put("language file", contents.get("language file") + 1);
                } else if (path.endsWith(".png")) {
                    contents.put("texture", contents.get("texture") + 1);
                } else if (path.endsWith(".ogg")) {
                    contents.put("sound", contents.get("sound") + 1);
                } else if (new File(path).isDirectory()) {
                    contents.put("folder", contents.get("folder") + 1);
                } else {
                    contents.put("file", contents.get("file") + 1);
                }
            }

            for (String key : contents.keySet()) {
                int count = contents.get(key);
                if (count == 0) continue;
                StringBuilder enumeration = new StringBuilder();
                enumeration.append(count);
                enumeration.append(' ');
                enumeration.append(key);
                if (count != 1) enumeration.append('s');
                enumeration.append(", ");
                subject.append(enumeration.toString());
            }
            subject.setLength(subject.length() - 2);

            int andIndex = subject.lastIndexOf(",");
            if(andIndex >= 0) subject.replace(andIndex, andIndex + 1, " and");
        } else {
            File file = new File(files.get(0));
            if(file.isDirectory()) {
                subject.append('\'');
                String _package = ProjectUtil.getPackageInclusive(file);
                if(_package.equals("")) _package = file.getName();
                subject.append(_package);
            } else {
                if(file.getName().endsWith(".tdn") || file.getName().endsWith(".mcfunction")) {
                    subject.append("function ");
                    subject.append('\'');
                    subject.append(FileCommons.stripExtension(file.getName()));
                } else {
                    subject.append("file ");
                    subject.append('\'');
                    subject.append(file.getName());
                }
            }
            subject.append('\'');
        }

        String query = FileModuleToken.DELETE_MOVES_TO_TRASH.get() ? ("Move " + subject + " to trash?") : ( "Delete " + subject + "?");

        boolean confirmation = new ConfirmDialog(FileModuleToken.DELETE_MOVES_TO_TRASH.get() ? "Move to Trash" : "Permanently Delete", query).result;

        if(!confirmation) return;

        for(String path : files) {
            File file = new File(path);
            if(!file.exists()) continue;
            if(file.isDirectory()) {
                deleteFolder(file);
            } else {
                boolean success = deleteOrTrashFile(file);
                if(!success) {
                    TridentWindow.showError("Couldn't delete file '" + file.getName() + "'");
                }
            }
        }
        TridentWindow.projectExplorer.refresh();
        TridentWindow.tabManager.checkForDeletion();
    }

    public static boolean deleteOrTrashFile(File file) {
        if(!Files.isWritable(file.toPath())) return false;
        if(FileModuleToken.DELETE_MOVES_TO_TRASH.get()) {
            try {
                if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported((Desktop.Action) Desktop.Action.class.getField("MOVE_TO_TRASH").get(null))) {
                    return (boolean) Desktop.class.getMethod("moveToTrash", File.class).invoke(Desktop.getDesktop(), file);
                } else if(new ConfirmDialog("Move to Trash", "'Move to Trash' is not supported in your platform. Permanently delete '" + file + "'?").result) {
                    return file.delete();
                }
            } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                TridentWindow.showException(e);
                e.printStackTrace();
            }
            return false;
        } else {
            return file.delete();
        }
    }

    public static boolean deleteFolder(File folder) {
        if(!Files.isWritable(folder.toPath())) return false;
        if(FileModuleToken.DELETE_MOVES_TO_TRASH.get()) {
            try {
                if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported((Desktop.Action) Desktop.Action.class.getField("MOVE_TO_TRASH").get(null))) {
                    return (boolean) Desktop.class.getMethod("moveToTrash", File.class).invoke(Desktop.getDesktop(), folder);
                } else if(!new ConfirmDialog("Move to Trash", "'Move to Trash' is not supported in your platform. Permanently delete '" + folder + "'?").result) {
                    return false;
                }
            } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                TridentWindow.showException(e);
                e.printStackTrace();
            }
            return false;
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    boolean success = FileManager.deleteOrTrashFile(f);
                    if(!success) {
                        TridentWindow.showError("Couldn't delete file '" + f.getName() + "'");
                    }
                }
            }
        }
        return folder.delete();
    }
}
