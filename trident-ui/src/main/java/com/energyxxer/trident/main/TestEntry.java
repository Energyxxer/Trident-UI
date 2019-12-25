package com.energyxxer.trident.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

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


        String javaPath = System.getProperty("java.home")
                + File.separator + "bin" + File.separator + "java";
        ProcessBuilder pb = new ProcessBuilder(javaPath, "-jar", Paths.get("C:\\Users\\PC\\Desktop\\temp\\Trident-UI u0_1_0c0_5_0-beta.jar").toString());
        try {
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
