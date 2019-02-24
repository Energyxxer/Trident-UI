package com.energyxxer.trident.ui.editor.completion;

import com.energyxxer.enxlex.suggestions.ComplexSuggestion;
import com.energyxxer.enxlex.suggestions.Suggestion;
import com.energyxxer.trident.compiler.lexer.TridentSuggestionTags;
import com.energyxxer.trident.global.Commons;
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

    private String iconKey;

    public SuggestionToken(SuggestionDialog parent, String text, Suggestion suggestion) {
        this.parent = parent;
        this.suggestion = suggestion;
        this.text = text;

        if(suggestion instanceof ComplexSuggestion) {
            iconKey = getIconKeyForTags(((ComplexSuggestion) suggestion).getTags());
        }
    }

    public String getIconKey() {
        return iconKey;
    }

    public void setIconKey(String iconKey) {
        this.iconKey = iconKey;
    }

    @Override
    public String getTitle() {
        return text;
    }

    @Override
    public Image getIcon() {
        return iconKey != null ? Commons.getIcon(iconKey) : null;
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
        parent.submit(text, suggestion);
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

    public static String getIconKeyForTags(Collection<String> tags) {
        if(tags.contains(TridentSuggestionTags.TAG_OBJECTIVE)) {
            return "objective";
        } else if(tags.contains(TridentSuggestionTags.TAG_ENTITY_FEATURE)) {
            return "feature";
        } else if(tags.contains(TridentSuggestionTags.TAG_CUSTOM_ENTITY)) {
            return "entity";
        } else if(tags.contains(TridentSuggestionTags.TAG_CUSTOM_ITEM)) {
            return "item";
        } else if(tags.contains(TridentSuggestionTags.TAG_VARIABLE)) {
            return "variable";
        }
        return null;
    }
}
