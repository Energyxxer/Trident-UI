package com.energyxxer.trident.ui.editor.completion;

import com.energyxxer.enxlex.suggestions.LiteralSuggestion;
import com.energyxxer.enxlex.suggestions.Suggestion;
import com.energyxxer.trident.ui.Tab;
import com.energyxxer.trident.ui.display.DisplayModule;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.ui.styledcomponents.StyledPopupMenu;

import java.awt.*;
import java.util.Collection;

public class SuggestionToken implements ModuleToken {

    private SuggestionDialog parent;
    private String text;
    private Suggestion suggestion;

    public SuggestionToken(SuggestionDialog parent, Suggestion suggestion) {
        this.parent = parent;
        this.suggestion = suggestion;
        text = suggestion.toString();
        if(suggestion instanceof LiteralSuggestion) {
            text = ((LiteralSuggestion) suggestion).getLiteral();
        }
    }

    @Override
    public String getTitle() {
        return text;
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
        parent.submit(suggestion);
    }

    @Override
    public int getDefaultXOffset() {
        return -25;
    }

    @Override
    public StyledPopupMenu generateMenu() {
        return null;
    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public boolean equals(ModuleToken other) {
        return other instanceof SuggestionToken && ((SuggestionToken) other).text.equals(this.text);
    }
}
