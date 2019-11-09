package com.energyxxer.trident.util;

import com.energyxxer.trident.ui.dialogs.ConfirmDialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileCommons {
    public static boolean moveFiles(File[] filesToMove, File destination) {
        return moveFiles(filesToMove, destination, false);
    }

    public static boolean moveFiles(File[] filesToMove, File destination, boolean subFoldersConfirmed) {
        if(filesToMove == null) return true;
        boolean allMoved = true;
        try {
            for(File file : filesToMove) {
                Path destinationFile = destination.toPath().resolve(file.getName());
                boolean mergeDirectories = file.isDirectory() && destinationFile.toFile().isDirectory();
                boolean confirmed = true;
                if(destinationFile.toFile().exists() && !(subFoldersConfirmed && mergeDirectories)) {
                    String action = mergeDirectories ? "Merge them" : "Replace it";
                    confirmed = new ConfirmDialog(action.replaceFirst("\\s.+","") + " existing", "<html>" + (file.isDirectory() ? "Folder" : "File") + " '" + file.getName() + "' already exists in the destination. <br>Would you like to " + action.toLowerCase() + "?</html>").result;
                }
                if(confirmed) {
                    if(mergeDirectories) {
                        if(moveFiles(file.listFiles(), destinationFile.toFile(), true)) {
                            File[] subFiles = file.listFiles();
                            if(subFiles == null || subFiles.length == 0) {
                                file.delete();
                            }
                        }
                    } else {
                        Files.move(file.toPath(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                } else {
                    allMoved = false;
                }
            }
        } catch (IOException x) {
            x.printStackTrace();
            return false;
        }
        return allMoved;
    }

    public static boolean copyFiles(File[] filesToCopy, File destination) {
        if(filesToCopy == null) return true;
        try {
            for(File file : filesToCopy) {
                Path destinationFile = createCopyFileName(destination.toPath().resolve(file.getName()).toFile()).toPath();

                Files.copy(file.toPath(), destinationFile);

                if(file.isDirectory()) {
                    copyFiles(file.listFiles(), destinationFile.toFile());
                }
            }
        } catch (IOException x) {
            x.printStackTrace();
            return false;
        }
        return true;
    }

    private static Pattern copyFilenamePattern = Pattern.compile(".* copy( \\d+)?$");

    public static File createCopyFileName(File original) {
        File destination = original.getParentFile();
        String filename = original.getName();
        String extension = filename.substring(filename.contains(".") ? filename.lastIndexOf(".") : filename.length());
        filename = filename.substring(0, filename.length()-extension.length());
        while(true) {
            File file = destination.toPath().resolve(filename + extension).toFile();
            if(file.exists()) {
                Matcher matcher = copyFilenamePattern.matcher(filename);
                if(matcher.matches()) {
                    if(matcher.start(1) >= 0) {
                        int nextNumber = Integer.parseInt(filename.substring(matcher.start(1)+1))+1;
                        filename = filename.substring(0, matcher.start(1)) + " " + (nextNumber);
                    } else {
                        filename += " 2";
                    }
                } else {
                    filename += " copy";
                }
            } else {
                return file;
            }
        }
    }
}
