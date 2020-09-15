package com.energyxxer.trident.util;

import com.energyxxer.trident.ui.dialogs.ConfirmDialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
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
                    confirmed = new ConfirmDialog(action.replaceFirst("\\s.+","") + " existing", "<html>" + (file.isDirectory() ? "Folder" : "File") + " '" + file.getName() + "' already exists in the destination. <br>Would you like to " + action.toLowerCase(Locale.ENGLISH) + "?</html>").result;
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
        return copyFiles(filesToCopy, destination, new HashSet<>());
    }

    public static boolean copyFiles(File[] filesToCopy, File destination, HashSet<File> copiedTo) {
        if(filesToCopy == null) return true;
        try {
            for(File file : filesToCopy) {
                if(copiedTo.contains(file)) continue;
                Path destinationFile = createCopyFileName(destination.toPath().resolve(file.getName()).toFile()).toPath();
                copiedTo.add(destinationFile.toFile());

                Files.copy(file.toPath(), destinationFile);

                if(file.isDirectory()) {
                    copyFiles(file.listFiles(), destinationFile.toFile(), copiedTo);
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

    /**
     * Returns whether or not the given string is a valid file name.
     */
    public static boolean validateFilename(String str) {
        return str.indexOf("\\") + str.indexOf("/") + str.indexOf(":") + str.indexOf("*") + str.indexOf("?")
                + str.indexOf("\"") + str.indexOf("<") + str.indexOf(">") + str.indexOf("|") == -9;
    }

    /**
     * Returns whether or not the given string is a valid file path.
     */
    public static boolean validatePath(String str) {
        return str.indexOf(":") + str.indexOf("*") + str.indexOf("?")
                + str.indexOf("\"") + str.indexOf("<") + str.indexOf(">") + str.indexOf("|") == -7;
    }

    public static String getRelativePath(File file, File root) {
        String result = (file.getAbsolutePath() + File.separator).replaceFirst(Pattern.quote(root.getAbsolutePath() + File.separator),"");
        if(result.endsWith(File.separator)) result = result.substring(0, result.length()-1);
        return result;
    }

    public static String stripExtension(String str) {

        if (str == null)
            return null;

        int pos = str.lastIndexOf(".");

        if (pos == -1)
            return str;

        return str.substring(0, pos);
    }

    public static Collection<File> listFilesOrdered(File directory) {
        if(!directory.isDirectory()) throw new IllegalArgumentException("Received non-directory file as parameter for FileCommons.listFilesOrdered(File)");
        ArrayList<File> subFilesOrdered = new ArrayList<>();
        int firstFileIndex = 0;
        File[] subFiles = directory.listFiles();
        if(subFiles != null) {
            for(File file : subFiles) {
                if(file.isDirectory()) {
                    subFilesOrdered.add(firstFileIndex, file);
                    firstFileIndex++;
                } else {
                    subFilesOrdered.add(file);
                }
            }
        }
        return subFilesOrdered;
    }
}
