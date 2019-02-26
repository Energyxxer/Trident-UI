package com.energyxxer.trident.ui.editor.completion.snippets;

import com.energyxxer.trident.ui.editor.completion.SnippetSuggestion;

import java.util.HashMap;
import java.util.Map;

public class Snippet {
    private String shorthand;
    private String text;
    private String description;
    private HashMap<SnippetContext, Boolean> contexts = new HashMap<>();

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

    public boolean isEnabledAnywhere() {
        return contexts.get(SnippetContext.ANYWHERE);
    }

    public boolean isContextEnabled(SnippetContext context) {
        return contexts.get(context);
    }

    public SnippetSuggestion createSuggestion() {
        return new SnippetSuggestion(shorthand, text, description);
    }

    public boolean isContextEnabledForTag(String tag) {
        if(isEnabledAnywhere()) return true;
        if(tag == null) return false;
        for(Map.Entry<SnippetContext, Boolean> entry : contexts.entrySet()) {
            if(entry.getValue() && entry.getKey().getTag().equals(tag)) return true;
        }
        return false;
    }
}
