package com.energyxxer.trident.main.window.actions;

import com.energyxxer.trident.global.keystrokes.UserKeyBind;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.main.window.sections.quick_find.QuickFindDialog;
import com.energyxxer.trident.ui.Tab;
import com.energyxxer.trident.ui.display.DisplayModule;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.ui.styledcomponents.StyledPopupMenu;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collection;
import java.util.Objects;

public class ProgramAction implements ModuleToken {
    private String displayName;
    private String description;
    private UserKeyBind shortcut;
    private Runnable action;
    private String iconKey = null;
    private boolean globalUsage = true;

    public ProgramAction(String displayName, String description, UserKeyBind shortcut, String moduleActionKey) {
        this(displayName, description, shortcut, () -> {
            Tab st = TridentWindow.tabManager.getSelectedTab();
            if(st != null) {
                st.module.performModuleAction(moduleActionKey);
            }
        });
    }

    public ProgramAction(String displayName, String description, UserKeyBind shortcut, Runnable action) {
        this.displayName = displayName;
        this.description = description;
        this.shortcut = shortcut;
        this.action = action;
        if(shortcut != null && shortcut.getName() == null) {
            shortcut.setName(displayName);
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public UserKeyBind getShortcut() {
        return shortcut;
    }

    public Runnable getAction() {
        return action;
    }

    public void perform() {
        if(action != null) {
            action.run();
        }
    }

    @Override
    public String getTitle(TokenContext context) {
        return displayName;
    }

    @Override
    public String getSubTitle() {
        return shortcut != null ? shortcut.getReadableKeyStroke() : null;
    }

    @Override
    public Image getIcon() {
        return null;
    }

    @Override
    public String getHint() {
        return description;
    }

    @Override
    public Collection<ModuleToken> getSubTokens() {
        return null;
    }

    @Override
    public boolean isExpandable() {
        return false;
    }

    @Override
    public DisplayModule createModule(Tab tab) {
        return null;
    }

    @Override
    public boolean isModuleSource() {
        return false;
    }

    @Override
    public void onInteract() {
        QuickFindDialog.INSTANCE.dismiss();
        perform();
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
        return other instanceof ProgramAction && other.equals(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgramAction that = (ProgramAction) o;
        return displayName.equals(that.displayName) &&
                Objects.equals(description, that.description) &&
                Objects.equals(shortcut, that.shortcut) &&
                Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, description, shortcut, action);
    }

    public boolean isGlobalUsage() {
        return globalUsage;
    }

    public ProgramAction setGlobalUsage(boolean globalUsage) {
        this.globalUsage = globalUsage;
        return this;
    }

    public String getIconKey() {
        return iconKey;
    }

    public ProgramAction setIconKey(String iconKey) {
        this.iconKey = iconKey;
        return this;
    }
}
