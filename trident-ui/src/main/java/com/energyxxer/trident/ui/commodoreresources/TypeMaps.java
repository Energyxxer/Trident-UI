package com.energyxxer.trident.ui.commodoreresources;

import com.energyxxer.commodore.util.io.CompoundInput;
import com.energyxxer.commodore.util.io.DirectoryCompoundInput;
import com.energyxxer.commodore.util.io.ZipCompoundInput;
import com.energyxxer.commodore.versioning.JavaEditionVersion;
import com.energyxxer.commodore.versioning.ThreeNumberVersion;
import com.energyxxer.nbtmapper.NBTTypeMapPack;
import com.energyxxer.util.logger.Debug;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeMaps {
    private static HashMap<String, NBTTypeMapPack> loadedTypeMaps = new HashMap<>();

    public static void loadAll() {
        loadedTypeMaps.clear();

        String typeMapDirPath = System.getProperty("user.home") + File.separator + "Trident" + File.separator + "resources" + File.separator + "typemaps" + File.separator;

        File typeMapDir = new File(typeMapDirPath);
        typeMapDir.mkdirs();
        File[] files = typeMapDir.listFiles();
        if(files != null) {
            for(File file : files) {
                String name = null;
                CompoundInput input = null;
                if(file.isDirectory()) {
                    input = new DirectoryCompoundInput(file);
                    name = file.getName();
                } else if(file.isFile() && file.getName().endsWith(".zip")) {
                    input = new ZipCompoundInput(file);
                    name = file.getName().substring(0, file.getName().length() - ".zip".length());
                }

                try {
                    if(input != null) {
                        loadFromCompound(input, name);
                    }
                } catch(Exception e) {
                    Debug.log(e.getMessage(), Debug.MessageType.ERROR);
                }
            }
        }

        Debug.log("Loaded type maps");
    }

    private static void loadFromCompound(CompoundInput input, String name) throws IOException {
        loadedTypeMaps.put(name, NBTTypeMapPack.fromCompound(input));
    }

    public static NBTTypeMapPack pickTypeMapsForVersion(ThreeNumberVersion targetVersion) {
        if(targetVersion == null) return null;

        String key = targetVersion.getEditionString().toLowerCase().charAt(0) + "_" + targetVersion.getMajor() + "_" + targetVersion.getMinor();
        Pattern vanillaKey = Pattern.compile(targetVersion.getEditionString().toLowerCase().charAt(0) + "_(\\d+)_(\\d+)");
        NBTTypeMapPack typemaps = loadedTypeMaps.get(key);
        if(typemaps != null) return typemaps;

        Map.Entry<JavaEditionVersion, NBTTypeMapPack> latestMatch = null;

        for(Map.Entry<String, NBTTypeMapPack> entry : loadedTypeMaps.entrySet()) {
            Matcher match = vanillaKey.matcher(entry.getKey());
            if(match.matches()) {
                JavaEditionVersion version = new JavaEditionVersion(Integer.parseInt(match.group(1)), Integer.parseInt(match.group(2)), 0);
                if(version.compare(targetVersion) <= 0) {
                    if(latestMatch == null || version.compare(latestMatch.getKey()) > 0) {
                        latestMatch = new AbstractMap.SimpleEntry<>(version, entry.getValue());
                    }
                }
            }
        }

        return latestMatch != null ? latestMatch.getValue() : null;
    }
}
