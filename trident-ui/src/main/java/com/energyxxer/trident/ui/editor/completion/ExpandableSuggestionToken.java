package com.energyxxer.trident.ui.editor.completion;

import com.energyxxer.enxlex.suggestions.Suggestion;
import com.energyxxer.trident.compiler.lexer.TridentSuggestionTags;
import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.ui.Tab;
import com.energyxxer.trident.ui.display.DisplayModule;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collection;

public class ExpandableSuggestionToken implements SuggestionToken, ModuleToken {

    protected SuggestionDialog parent;
    protected String preview;
    protected String text;
    protected String description;
    protected Suggestion suggestion;

    protected String iconKey;

    protected boolean enabled = true;
    protected boolean darkened;
    protected int backspaces = 0;

    protected ExpandableSuggestionToken() {

    }

    public ExpandableSuggestionToken(SuggestionDialog parent, String text, Suggestion suggestion) {
        this(parent, text, text, suggestion);
    }

    public ExpandableSuggestionToken(SuggestionDialog parent, String preview, String text, Suggestion suggestion) {
        this.parent = parent;
        this.suggestion = suggestion;
        this.preview = preview;
        this.text = text;

        iconKey = getIconKeyForTags(suggestion.getTags());

        if(suggestion instanceof SnippetSuggestion) {
            description = "  " + ((SnippetSuggestion) suggestion).getDescription();
        }
    }

    public String getIconKey() {
        return iconKey;
    }

    public void setIconKey(String iconKey) {
        this.iconKey = iconKey;
    }

    @Override
    public String getTitle(TokenContext context) {
        return preview;
    }

    @Override
    public String getSubTitle() {
        return description;
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
        parent.submit(StringUtil.repeat("\b", backspaces) + text, suggestion, true);
    }

    @Override
    public int getDefaultXOffset() {
        return -25;
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
        return other instanceof ExpandableSuggestionToken && ((ExpandableSuggestionToken) other).text.equals(this.text);
    }

    public void setEnabledFilter(String filter) {
        enabled = filter.isEmpty() || this.preview.startsWith(filter);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static String getIconKeyForTags(Collection<String> tags) {
        if(tags.contains(TridentSuggestionTags.TAG_OBJECTIVE)) {
            return "objective";
        } else if(tags.contains(TridentSuggestionTags.TAG_ENTITY_COMPONENT)) {
            return "feature";
        } else if(tags.contains(TridentSuggestionTags.TAG_CUSTOM_ENTITY)) {
            return "custom_entity";
        } else if(tags.contains(TridentSuggestionTags.TAG_CUSTOM_ITEM)) {
            return "custom_item";
        } else if(tags.contains(TridentSuggestionTags.TAG_ITEM)) {
            return "item";
        } else if(tags.contains(TridentSuggestionTags.TAG_ENTITY)) {
            return "entity";
        } else if(tags.contains(TridentSuggestionTags.TAG_COORDINATE)) {
            return "coordinates";
        } else if(tags.contains(TridentSuggestionTags.TAG_VARIABLE)) {
            return "variable";
        } else if(tags.contains(TridentSuggestionTags.TAG_COMMAND)) {
            return "command";
        } else if(tags.contains(TridentSuggestionTags.TAG_MODIFIER)) {
            return "modifier";
        } else if(tags.contains(TridentSuggestionTags.TAG_INSTRUCTION)) {
            return "instruction";
        } else if(tags.contains(SnippetSuggestion.TAG_SNIPPET)) {
            return "snippet";
        }
        return null;
    }

    public void setDarkened(boolean darkened) {
        this.darkened = darkened;
    }

    public boolean isDarkened() {
        return darkened;
    }

    public void setBackspaces(int backspaces) {
        this.backspaces = backspaces;
    }

    public int getBackspaces() {
        return backspaces;
    }

    @Override
    public String toString() {
        return preview;
    }
}
