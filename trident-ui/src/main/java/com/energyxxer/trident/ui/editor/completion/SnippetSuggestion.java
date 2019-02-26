package com.energyxxer.trident.ui.editor.completion;

import com.energyxxer.enxlex.suggestions.LiteralSuggestion;

public class SnippetSuggestion extends LiteralSuggestion {
    public static final String TAG_SNIPPET = "cst:snippet";

    private String description;

    public SnippetSuggestion(String preview, String literal, String description) {
        super(preview, literal);

        this.description = description;

        this.addTag(TAG_SNIPPET);
    }

    public String getDescription() {
        return description;
    }
}
