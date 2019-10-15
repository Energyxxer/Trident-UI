package com.energyxxer.trident.ui.commodoreresources;

import com.energyxxer.commodore.versioning.compatibility.VersionFeatureManager;
import com.energyxxer.util.logger.Debug;

import java.io.File;
import java.io.FileReader;

public class VersionFeatureResources {
    public static void loadAll() {
        VersionFeatureManager.clearLoadedFeatures();

        String featMapDirPath = System.getProperty("user.home") + File.separator + "Trident" + File.separator + "resources" + File.separator + "featmaps" + File.separator;

        File featMapDir = new File(featMapDirPath);
        featMapDir.mkdirs();
        File[] files = featMapDir.listFiles();
        if(files != null) {
            for(File file : files) {
                if(file.isFile() && file.getName().endsWith(".json")) {
                    try (FileReader fr = new FileReader(file)) {
                        VersionFeatureManager.loadFeatureMap(fr);
                    } catch (Exception e) {
                        Debug.log(e.getMessage(), Debug.MessageType.ERROR);
                    }
                }
            }
        }
    }
}
