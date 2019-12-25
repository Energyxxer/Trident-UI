package com.energyxxer.trident.main.window.sections.tools.process;

import com.energyxxer.trident.ui.Tab;
import com.energyxxer.trident.ui.display.DisplayModule;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.ui.styledcomponents.StyledMenuItem;
import com.energyxxer.trident.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.util.processes.AbstractProcess;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collection;
import java.util.Objects;

public class ProcessToken implements ModuleToken {
    private AbstractProcess process;

    public ProcessToken(AbstractProcess process) {
        this.process = process;
    }

    public AbstractProcess getProcess() {
        return process;
    }

    @Override
    public String getTitle(TokenContext context) {
        return process.getName();
    }

    @Override
    public String getSubTitle() {
        return process.getStatus() + (process.getProgress() >= 0 ? "| " + (100*process.getProgress()) + "%" : "");
    }

    @Override
    public Image getIcon() {
        return null;
    }

    @Override
    public String getHint() {
        return null;
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

    }

    @Override
    public StyledPopupMenu generateMenu(@NotNull ModuleToken.TokenContext context) {
        StyledPopupMenu menu = new StyledPopupMenu();
        StyledMenuItem stopItem = new StyledMenuItem("Kill Process");
        stopItem.addActionListener(e -> process.terminate());
        menu.add(stopItem);
        return menu;
    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public boolean equals(ModuleToken other) {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessToken that = (ProcessToken) o;
        return Objects.equals(process, that.process);
    }

    @Override
    public int hashCode() {
        return Objects.hash(process);
    }
}
