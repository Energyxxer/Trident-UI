package com.energyxxer.trident.main;

import com.energyxxer.trident.util.FileCommons;

import java.io.File;

public class TestEntry {

    private static int getJavaVersion() {
        String version = System.getProperty("java.version");
        StringBuilder sb = new StringBuilder();

        int major;
        for(major = 0; major < version.length(); ++major) {
            char c = version.charAt(major);
            if (!Character.isDigit(c)) {
                break;
            }

            sb.append(c);
        }

        major = Integer.parseInt(sb.toString());
        major = major == 1 ? 8 : major;
        return major;
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.version"));
        System.out.println(getJavaVersion() == 8);

        System.out.println(FileCommons.createCopyFileName(new File("C:\\Users\\PC\\AppData\\Local\\Packages\\Microsoft.MinecraftUWP_8wekyb3d8bbwe\\LocalState\\games\\com.mojang\\minecraftWorlds\\Project KS\\behavior_packs\\ksBP\\functions\\health_interface\\")));
    }
}
