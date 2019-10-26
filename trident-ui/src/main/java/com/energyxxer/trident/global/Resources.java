package com.energyxxer.trident.global;

import com.energyxxer.trident.files.FileDefaults;
import com.energyxxer.trident.ui.commodoreresources.DefinitionPacks;
import com.energyxxer.trident.ui.commodoreresources.TypeMaps;
import com.energyxxer.trident.ui.commodoreresources.VersionFeatureResources;
import com.energyxxer.trident.ui.theme.ThemeManager;
import com.energyxxer.trident.util.LineReader;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by User on 1/7/2017.
 */
public class Resources {
    public static final HashMap<String, ArrayList<String>> indexes = new HashMap<>();
    public static final ArrayList<String> tips = new ArrayList<>();
    public static JsonObject resources = new JsonObject();
    private static final File resourceInfoPath = new File(System.getProperty("user.home") + File.separator + "Trident" + File.separator + "resources.json");

    public static final Pattern ISO_8601_REGEX = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z");

    public static void load() {
        indexes.clear();
        try {
            ArrayList<String> lines = LineReader.read("/resources/indexes.txt");
            String key = null;
            ArrayList<String> currentValues = null;
            for(String line : lines) {
                if(line.endsWith(":")) {
                    if(key != null) indexes.put(key, currentValues);
                    currentValues = new ArrayList<>();

                    key = line.substring(0,line.lastIndexOf(":"));
                } else if(line.startsWith("-")) {
                    if(key != null) {
                        currentValues.add(line.substring(1).trim());
                    }
                }
            }
            if(key != null) indexes.put(key, currentValues);
        } catch(IOException x) {
            x.printStackTrace();
        }

        tips.clear();
        try {
            ArrayList<String> lines = LineReader.read("/resources/tips.txt");
            tips.addAll(lines);
        } catch(IOException x) {
            x.printStackTrace();
        }


        resourceInfoPath.getParentFile().mkdirs();
        try {
            if(resourceInfoPath.exists()) {
                JsonObject obj = new Gson().fromJson(new FileReader(resourceInfoPath), JsonObject.class);
                if(obj != null) {
                    JsonElement lastCheckedDefCommit = obj.get("last-checked-definition-commit");
                    if(lastCheckedDefCommit != null && lastCheckedDefCommit.isJsonPrimitive() && lastCheckedDefCommit.getAsJsonPrimitive().isString() && ISO_8601_REGEX.matcher(lastCheckedDefCommit.getAsString()).matches()) {
                        resources.addProperty("last-checked-definition-commit", lastCheckedDefCommit.getAsString());
                    }
                }
            }
        } catch (IOException x) {
            x.printStackTrace();
        }

        ThemeManager.loadAll();
        FileDefaults.loadAll();
        DefinitionPacks.loadAll();
        VersionFeatureResources.loadAll();
        TypeMaps.loadAll();
    }

    public static void saveAll() {
        try {
            Files.write(resourceInfoPath.toPath(), new Gson().toJson(resources).getBytes());
        } catch (IOException x) {
            x.printStackTrace();
        }
    }
}
