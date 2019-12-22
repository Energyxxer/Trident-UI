package com.energyxxer.trident.global;

import com.energyxxer.trident.main.WorkspaceDialog;

import java.io.File;
import java.util.prefs.BackingStoreException;

public class Preferences {

    public static final String DEFAULT_WORKSPACE_PATH = System.getProperty("user.home") + File.separator + "Trident" + File.separator + "workspace";
    private static java.util.prefs.Preferences prefs = java.util.prefs.Preferences
            .userNodeForPackage(Preferences.class);
    public static final String LOG_FILE_PATH;

    private static int baseFontSize = 12;
    private static int editorFontSize = 12;

    static {
        LOG_FILE_PATH = System.getProperty("user.home") + File.separator + "Trident" + File.separator + "latest.log";

        if(prefs.get("theme",null) == null) prefs.put("theme", "Electron Dark");
        if(prefs.get("workspace_dir", null) == null) {
            promptWorkspace();
        }
        if(prefs.get("username",null) == null) prefs.put("username", "User");
        baseFontSize = Integer.parseInt(prefs.get("base_font_size","12"));
        if(prefs.get("base_font_size",null) == null) prefs.put("base_font_size", "12");
        editorFontSize = Integer.parseInt(prefs.get("editor_font_size","12"));
        if(prefs.get("editor_font_size",null) == null) prefs.put("editor_font_size", "12");
    }

    public static void promptWorkspace() {
        WorkspaceDialog.prompt();
    }

    public static void reset() {
        try {
            prefs.clear();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public static String get(String key, String def) {
        return prefs.get(key, def);
    }

    public static String get(String key) {
        return prefs.get(key, null);
    }

    public static void put(String key, String value) {
        prefs.put(key, value);
    }

    public static void remove(String key) {
        prefs.remove(key);
    }

    public static int getBaseFontSize() {
        return baseFontSize;
    }

    public static int getEditorFontSize() {
        return editorFontSize;
    }

    public static void setBaseFontSize(int fontSize) {
        if(fontSize > 0) {
            baseFontSize = fontSize;
            prefs.put("base_font_size", ""+fontSize);
        }
    }

    public static void setEditorFontSize(int fontSize) {
        if(fontSize > 0) {
            editorFontSize = fontSize;
            prefs.put("editor_font_size", ""+fontSize);
        }
    }
}
