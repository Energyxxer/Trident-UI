package com.energyxxer.trident.ui.commodoreresources;

import com.energyxxer.commodore.util.io.CompoundInput;
import com.energyxxer.commodore.util.io.DirectoryCompoundInput;
import com.energyxxer.commodore.util.io.ZipCompoundInput;
import com.energyxxer.commodore.versioning.JavaEditionVersion;
import com.energyxxer.util.logger.Debug;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeMaps {
    private static HashMap<String, String[]> loadedTypeMaps = new HashMap<>();
    private static final Pattern vanillaKey = Pattern.compile("j_(\\d+)_(\\d+)");

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
    }

    private static void loadFromCompound(CompoundInput input, String name) throws IOException {
        try {
            input.open();
            InputStream fileListIS = input.get("");
            if (fileListIS != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(fileListIS))) {
                    String innerFileName;
                    ArrayList<String> typeMapsInside = new ArrayList<>();
                    while ((innerFileName = br.readLine()) != null) {
                        InputStream is = input.get(innerFileName);
                        if (is != null) {
                            typeMapsInside.add(readAllText(is));
                        }
                    }
                    loadedTypeMaps.put(name, typeMapsInside.toArray(new String[0]));
                }
            }
        } finally {
            input.close();
        }
    }

    private static String readAllText(InputStream is) throws IOException {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            if(sb.length() > 0) sb.setLength(sb.length()-1);
            return sb.toString();
        }
    }

    public static String[] pickTypeMapsForVersion(JavaEditionVersion targetVersion) {
        if(targetVersion == null) return null;

        String key = "j_" + targetVersion.getMajor() + "_" + targetVersion.getMinor();
        String[] typemaps = loadedTypeMaps.get(key);
        if(typemaps != null) return typemaps;

        Map.Entry<JavaEditionVersion, String[]> latestMatch = null;

        for(Map.Entry<String, String[]> entry : loadedTypeMaps.entrySet()) {
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
