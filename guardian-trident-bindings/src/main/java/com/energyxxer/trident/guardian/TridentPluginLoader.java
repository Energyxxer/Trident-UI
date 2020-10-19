package com.energyxxer.trident.guardian;

import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.ui.commodoreresources.GuardianPluginLoader;
import com.energyxxer.prismarine.PrismarineSuiteConfiguration;
import com.energyxxer.trident.TridentSuiteConfiguration;

import java.io.File;

public class TridentPluginLoader implements GuardianPluginLoader {

    public static final TridentPluginLoader INSTANCE = new TridentPluginLoader();

    private TridentPluginLoader() {}

    @Override
    public PrismarineSuiteConfiguration getSuiteConfig() {
        return TridentSuiteConfiguration.INSTANCE;
    }

    @Override
    public File getPluginsDirectory() {
        return Guardian.core.getMainDirectory().resolve("resources").resolve("plugins").toFile();
    }
}
