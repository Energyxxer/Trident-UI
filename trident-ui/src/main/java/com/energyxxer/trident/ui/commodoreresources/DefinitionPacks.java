package com.energyxxer.trident.ui.commodoreresources;

import com.energyxxer.commodore.defpacks.DefinitionPack;
import com.energyxxer.commodore.util.io.DirectoryCompoundInput;
import com.energyxxer.commodore.util.io.ZipCompoundInput;
import com.energyxxer.commodore.versioning.JavaEditionVersion;
import com.energyxxer.util.logger.Debug;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefinitionPacks {
    private static HashMap<String, DefinitionPack> loadedDefinitionPacks = new HashMap<>();
    private static final Pattern vanillaKey = Pattern.compile("minecraft_j_1_(\\d+)");

    private static JavaEditionVersion latestKnownVersion = new JavaEditionVersion(1, 13, 0);
    private static JavaEditionVersion[] knownVersionList = null;

    public static void loadAll() {
        loadedDefinitionPacks.clear();

        String defPackDirPath = System.getProperty("user.home") + File.separator + "Trident" + File.separator + "resources" + File.separator + "defpacks" + File.separator;

        File defPackDir = new File(defPackDirPath);
        defPackDir.mkdirs();
        File[] files = defPackDir.listFiles();
        if(files != null) {
            for(File file : files) {
                DefinitionPack defPack = null;
                if(file.isDirectory()) {
                    defPack = new DefinitionPack(new DirectoryCompoundInput(file));
                    String packName = file.getName();
                    loadedDefinitionPacks.put(packName, defPack);
                    updateLatestKnownVersion(packName);
                } else if(file.isFile() && file.getName().endsWith(".zip")) {
                    defPack = new DefinitionPack(new ZipCompoundInput(file));
                    String packName = file.getName().substring(0, file.getName().length() - ".zip".length());
                    loadedDefinitionPacks.put(packName, defPack);
                    updateLatestKnownVersion(packName);
                }
                try {
                    if(defPack != null) defPack.load();
                } catch(Exception e) {
                    Debug.log(e.getMessage(), Debug.MessageType.ERROR);
                }
            }
        }
    }

    private static void updateLatestKnownVersion(String packName) {
        Matcher match = vanillaKey.matcher(packName);
        if(match.matches()) {
            JavaEditionVersion thisVersion = new JavaEditionVersion(1, Integer.parseInt(match.group(1)), 0);
            if(latestKnownVersion.compare(thisVersion) < 0) {
                latestKnownVersion = thisVersion;
                knownVersionList = null;
            }
        }
    }

    public static JavaEditionVersion[] getKnownVersions() {
        if(knownVersionList != null) return knownVersionList;
        knownVersionList = new JavaEditionVersion[Math.max(0, latestKnownVersion.getMinor() - 13 + 1)];
        for(int i = 13; i <= latestKnownVersion.getMinor(); i++) {
            knownVersionList[i-13] = new JavaEditionVersion(1, i, 0);
        }
        return knownVersionList;
    }

    public static DefinitionPack[] pickPacksForVersion(JavaEditionVersion targetVersion) {
        if(targetVersion == null) return null;

        String key = "minecraft_j_" + targetVersion.getMajor() + "_" + targetVersion.getMinor();
        DefinitionPack pack = loadedDefinitionPacks.get(key);
        Debug.log("key: " + key);
        if(pack != null) return new DefinitionPack[] {pack};
        Debug.log("oh no pack is null");

        Map.Entry<JavaEditionVersion, DefinitionPack> latestMatch = null;

        for(Map.Entry<String, DefinitionPack> entry : loadedDefinitionPacks.entrySet()) {
            Matcher match = vanillaKey.matcher(entry.getKey());
            if(match.matches()) {
                JavaEditionVersion version = new JavaEditionVersion(1, Integer.parseInt(match.group(1)), 0);
                if(version.compare(targetVersion) <= 0) {
                    if(latestMatch == null || version.compare(latestMatch.getKey()) > 0) {
                        latestMatch = new AbstractMap.SimpleEntry<>(version, entry.getValue());
                    }
                }
            }
        }

        Debug.log(loadedDefinitionPacks);
        Debug.log(latestMatch);

        return latestMatch != null ? new DefinitionPack[] {latestMatch.getValue()} : null;
    }

    public static Map<String, DefinitionPack> getAliasMap() {
        return loadedDefinitionPacks;
    }
}
