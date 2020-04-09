package com.energyxxer.trident.ui.commodoreresources;

import com.energyxxer.commodore.defpacks.DefinitionPack;
import com.energyxxer.commodore.util.io.DirectoryCompoundInput;
import com.energyxxer.commodore.util.io.ZipCompoundInput;
import com.energyxxer.commodore.versioning.BedrockEditionVersion;
import com.energyxxer.commodore.versioning.JavaEditionVersion;
import com.energyxxer.commodore.versioning.ThreeNumberVersion;
import com.energyxxer.commodore.versioning.Version;
import com.energyxxer.util.logger.Debug;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefinitionPacks {
    private static HashMap<String, DefinitionPack> loadedDefinitionPacks = new LinkedHashMap<>();
    private static final Pattern javaPackKey = Pattern.compile("minecraft_j_1_(\\d+)");
    private static final Pattern bedrockPackKey = Pattern.compile("minecraft_b_1_(\\d+)");

    private static JavaEditionVersion latestKnownJavaVersion = new JavaEditionVersion(1, 13, 0);
    private static BedrockEditionVersion latestKnownBedrockVersion = new BedrockEditionVersion(1, 13, 0);
    private static Version[] knownVersionList = null;
    public static final String DEF_PACK_DIR_PATH = System.getProperty("user.home") + File.separator + "Trident" + File.separator + "resources" + File.separator + "defpacks" + File.separator;

    public static void loadAll() {
        loadedDefinitionPacks.clear();

        File defPackDir = new File(DEF_PACK_DIR_PATH);
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

        Debug.log("Loaded definition packs");
    }

    private static void updateLatestKnownVersion(String packName) {
        Matcher match = javaPackKey.matcher(packName);
        if(match.matches()) {
            JavaEditionVersion thisVersion = new JavaEditionVersion(1, Integer.parseInt(match.group(1)), 0);
            if(latestKnownJavaVersion.compare(thisVersion) < 0) {
                latestKnownJavaVersion = thisVersion;
                knownVersionList = null;
            }
        }
        match = bedrockPackKey.matcher(packName);
        if(match.matches()) {
            BedrockEditionVersion thisVersion = new BedrockEditionVersion(1, Integer.parseInt(match.group(1)), 0);
            if(latestKnownBedrockVersion.compare(thisVersion) < 0) {
                latestKnownBedrockVersion = thisVersion;
                knownVersionList = null;
            }
        }
    }

    public static Version[] getKnownVersions() {
        if(knownVersionList != null) return knownVersionList;
        knownVersionList = new Version[Math.max(0, latestKnownJavaVersion.getMinor() - 13 + 1) + Math.max(0, latestKnownBedrockVersion.getMinor() - 13 + 1)];
        int j = 0;
        for(int i = 13; i <= latestKnownJavaVersion.getMinor(); i++) {
            knownVersionList[j++] = new JavaEditionVersion(1, i, 0);
        }
        for(int i = 13; i <= latestKnownBedrockVersion.getMinor(); i++) {
            knownVersionList[j++] = new BedrockEditionVersion(1, i, 0);
        }
        return knownVersionList;
    }

    public static JavaEditionVersion[] getKnownJavaVersions() {
        return Arrays.stream(getKnownVersions()).filter(v -> v instanceof JavaEditionVersion).map(v -> (JavaEditionVersion) v).toArray(JavaEditionVersion[]::new);
    }

    public static BedrockEditionVersion[] getKnownBedrockVersions() {
        return Arrays.stream(getKnownVersions()).filter(v -> v instanceof BedrockEditionVersion).map(v -> (BedrockEditionVersion) v).toArray(BedrockEditionVersion[]::new);
    }

    public static DefinitionPack[] pickPacksForVersion(ThreeNumberVersion targetVersion) {
        if(targetVersion == null) return null;

        String key = "minecraft_" + targetVersion.getEditionString().toLowerCase().charAt(0) + "_" + targetVersion.getMajor() + "_" + targetVersion.getMinor();
        Pattern vanillaKey = Pattern.compile("minecraft_" + targetVersion.getEditionString().toLowerCase().charAt(0) + "_1_(\\d+)");
        DefinitionPack pack = loadedDefinitionPacks.get(key);
        Debug.log("key: " + key);
        if(pack != null) return new DefinitionPack[] {pack};
        Debug.log("oh no pack is null");

        Map.Entry<JavaEditionVersion, DefinitionPack> latestMatch = null;

        targetVersion = new JavaEditionVersion(targetVersion.getMajor(), targetVersion.getMinor(), targetVersion.getPatch());

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

    public static JavaEditionVersion getLatestKnownJavaVersion() {
        return latestKnownJavaVersion;
    }

    public static BedrockEditionVersion getLatestKnownBedrockVersion() {
        return latestKnownBedrockVersion;
    }

    public static Map<String, DefinitionPack> getAliasMap() {
        return loadedDefinitionPacks;
    }
}
