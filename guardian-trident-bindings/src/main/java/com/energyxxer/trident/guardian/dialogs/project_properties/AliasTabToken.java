package com.energyxxer.trident.guardian.dialogs.project_properties;

import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.util.logger.Debug;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collection;

public class AliasTabToken implements ModuleToken {

    @NotNull
    private String category;

    public AliasTabToken(@NotNull String category) {
        this.category = category;
    }

    @Override
    public String getTitle(TokenContext context) {
        return category;
    }

    @Override
    public Image getIcon() {
        return null;
    }

    @Override
    public String getHint() {
        return "Category '" + category + "'";
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
        return true;
    }

    @Override
    public DisplayModule createModule(Tab tab) {
        return new AliasCategoryModule(category, ProjectProperties.project.getProjectConfigJson());
    }

    @Override
    public void onInteract() {
        Debug.log("Interact");
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
    public boolean isTabCloseable() {
        return false;
    }

    @Override
    public boolean equals(ModuleToken other) {
        return other instanceof AliasTabToken && ((AliasTabToken) other).category.equals(this.category);
    }
}
