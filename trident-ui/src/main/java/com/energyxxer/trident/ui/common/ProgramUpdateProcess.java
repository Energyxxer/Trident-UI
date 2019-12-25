package com.energyxxer.trident.ui.common;

import com.energyxxer.trident.global.Preferences;

public class ProgramUpdateProcess {
    public static Preferences.SettingPref<Boolean> CHECK_FOR_PROGRAM_UPDATES_STARTUP = new Preferences.SettingPref<>("settings.behavior.check_program_updates_startup", true, Boolean::new);
}
