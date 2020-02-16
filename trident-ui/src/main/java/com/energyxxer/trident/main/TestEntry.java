package com.energyxxer.trident.main;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

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

        System.out.println(new Gson().fromJson("", JsonElement.class));

    }
}
