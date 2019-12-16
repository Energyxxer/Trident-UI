package com.energyxxer.trident.ui.display;

import com.energyxxer.trident.ui.modules.ModuleToken;

/**
 * Created by User on 2/8/2017.
 */
public interface DisplayModule {
    void displayCaretInfo();
    Object getValue();
    boolean canSave();
    Object save();
    void focus();
    default void onSelect() {}
    default void performModuleAction(String key) {}

    boolean transform(ModuleToken newToken);
}
