package com.energyxxer.trident.guardian.dialogs.project_properties;

import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.dialogs.PromptDialog;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collection;
import java.util.Locale;

public class AliasAddToken implements ModuleToken {
    private ProjectPropertiesAliases parent;

    public AliasAddToken(ProjectPropertiesAliases parent) {
        this.parent = parent;
    }

    @Override
    public String getTitle(TokenContext context) {
        return null;
    }

    @Override
    public Image getIcon() {
        return Commons.getIcon("add");
    }

    @Override
    public String getHint() {
        return "Add Category";
    }

    @Override
    public Collection<? extends ModuleToken> getSubTokens() {
        return null;
    }

    @Override
    public boolean isExpandable() {
        return false;
    }

    @Override
    public boolean isModuleSource() {
        return false;
    }

    @Override
    public DisplayModule createModule(Tab tab) {
        return null;
    }

    @Override
    public void onInteract() {
        String newCategory = new PromptDialog("Add alias category", "Insert the category name").result;
        if(newCategory != null && !newCategory.isEmpty()) {
            parent.openCategory(newCategory.toLowerCase(Locale.ENGLISH));
        }
    }

    @Override
    public StyledPopupMenu generateMenu(@NotNull ModuleToken.TokenContext context) {
        return null;
    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public boolean equals(ModuleToken other) {
        return other instanceof AliasAddToken;
    }

    @Override
    public boolean isTabCloseable() {
        return false;
    }
}
