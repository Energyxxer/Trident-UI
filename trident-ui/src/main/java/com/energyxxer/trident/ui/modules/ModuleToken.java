package com.energyxxer.trident.ui.modules;

import com.energyxxer.trident.ui.Tab;
import com.energyxxer.trident.ui.display.DisplayModule;
import com.energyxxer.trident.ui.styledcomponents.StyledPopupMenu;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface ModuleToken {
    String getTitle();
    default boolean ellipsisFromLeft() {return false;}
    java.awt.Image getIcon();
    String getHint();
    Collection<? extends ModuleToken> getSubTokens();
    boolean isExpandable();
    boolean isModuleSource();
    DisplayModule createModule(Tab tab);
    void onInteract();
    StyledPopupMenu generateMenu();

    default boolean isTabCloseable() {
        return true;
    }

    default String getSearchTerms() { return null; }

    String getIdentifier();

    boolean equals(ModuleToken other);
    default String getSubTitle() {
        return null;
    }

    default File getAssociatedProjectRoot() {
        return null;
    }

    default int getDefaultXOffset() {
        return 0;
    }

    class Static {
        public static List<ModuleTokenFactory> tokenFactories = new ArrayList<>();

        static {
            tokenFactories.add(FileModuleToken.factory);
        }

        public static ModuleToken createFromIdentifier(String identifier) {
            for(ModuleTokenFactory factory : tokenFactories) {
                ModuleToken created = factory.createFromIdentifier(identifier);
                if(created != null) return created;
            }
            return null;
        }
    }
}
