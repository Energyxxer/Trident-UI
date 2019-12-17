package com.energyxxer.trident.ui.editor.completion.snippets;

import com.energyxxer.commodore.CommandUtils;
import com.energyxxer.trident.ui.editor.completion.SnippetSuggestion;

import java.util.EnumMap;
import java.util.Map;

public class Snippet {
    private boolean enabled = true;
    private String shorthand;
    private String text;
    private String description;
    private EnumMap<SnippetContext, Boolean> contexts = new EnumMap<>(SnippetContext.class);
    public boolean expanderApplied = false;

    public Snippet() {
        this(null, null, null);
    }

    public Snippet(String shorthand, String text, String description) {
        this.shorthand = shorthand;
        this.text = text;
        this.description = description;

        for(SnippetContext ctx : SnippetContext.values()) {
            contexts.put(ctx, false);
        }
    }

    public Snippet setContextEnabled(SnippetContext context) {
        return setContextEnabled(context, true);
    }

    public Snippet setContextEnabled(SnippetContext context, boolean enabled) {
        contexts.put(context, enabled);
        return this;
    }

    public boolean isEnabledEverywhere() {
        return contexts.get(SnippetContext.EVERYWHERE);
    }

    public boolean isContextEnabled(SnippetContext context) {
        return contexts.get(context);
    }

    public SnippetSuggestion createSuggestion() {
        return new SnippetSuggestion(shorthand, text, description);
    }

    public boolean isContextEnabledForTag(String tag) {
        if(!enabled) return false;
        if(isEnabledEverywhere()) return true;
        if(tag == null) return false;
        for(Map.Entry<SnippetContext, Boolean> entry : contexts.entrySet()) {
            if(entry.getValue() && entry.getKey().getTag().equals(tag)) return true;
        }
        return false;
    }

    public String getShorthand() {
        return shorthand;
    }

    public String getText() {
        return text;
    }

    public String getDescription() {
        return description;
    }

    public EnumMap<SnippetContext, Boolean> getContexts() {
        return contexts;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Snippet clone() {
        Snippet cloned = new Snippet(shorthand, text, description);
        cloned.contexts = this.contexts.clone();
        cloned.enabled = this.enabled;
        return cloned;
    }

    public void setShorthand(String shorthand) {
        this.shorthand = shorthand;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSaveData() {
        StringBuilder sb = new StringBuilder();
        sb.append(enabled ? "e" : "d");
        sb.append(CommandUtils.quote(shorthand));
        sb.append(CommandUtils.quote(description));
        sb.append(CommandUtils.quote(text));
        for(Map.Entry<SnippetContext, Boolean> entry : contexts.entrySet()) {
            if(entry.getValue()) {
                sb.append(CommandUtils.quote(entry.getKey().name()));
            }
        }
        sb.append(';');
        return sb.toString();
    }
}
