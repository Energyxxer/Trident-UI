package com.energyxxer.trident.ui.commodoreresources;

import com.energyxxer.commodore.util.io.DirectoryCompoundInput;
import com.energyxxer.commodore.util.io.ZipCompoundInput;
import com.energyxxer.trident.compiler.plugin.TridentPlugin;
import com.energyxxer.util.logger.Debug;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TridentPlugins {
    private static HashMap<String, TridentPlugin> loadedPlugins = new LinkedHashMap<>();

    public static final String PLUGIN_DIR_PATH = System.getProperty("user.home") + File.separator + "Trident" + File.separator + "resources" + File.separator + "plugins" + File.separator;

    public static void loadAll() {
        loadedPlugins.clear();

        File pluginsDir = new File(PLUGIN_DIR_PATH);
        pluginsDir.mkdirs();
        File[] files = pluginsDir.listFiles();
        if(files != null) {
            for(File file : files) {
                TridentPlugin plugin = null;
                if(file.isDirectory()) {
                    plugin = new TridentPlugin(new DirectoryCompoundInput(file), file);
                    String packName = file.getName();
                    loadedPlugins.put(packName, plugin);
                } else if(file.isFile() && file.getName().endsWith(".zip")) {
                    plugin = new TridentPlugin(new ZipCompoundInput(file), file);
                    String packName = file.getName().substring(0, file.getName().length() - ".zip".length());
                    loadedPlugins.put(packName, plugin);
                }
                try {
                    if(plugin != null) plugin.load();
                } catch(Exception e) {
                    Debug.log(e.getMessage(), Debug.MessageType.ERROR);
                }
            }
        }

        Debug.log("Loaded plugins");
    }

    public static Map<String, TridentPlugin> getAliasMap() {
        return loadedPlugins;
    }
}
