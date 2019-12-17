package com.energyxxer.trident.ui.editor.completion.snippets;

import com.energyxxer.trident.compiler.lexer.TridentSuggestionTags;

public enum SnippetContext {
    EVERYWHERE(null),
    ENTRY(TridentSuggestionTags.CONTEXT_ENTRY),
    COMMAND(TridentSuggestionTags.CONTEXT_COMMAND),
    MODIFIER(TridentSuggestionTags.CONTEXT_MODIFIER),
    ENTITY_BODY(TridentSuggestionTags.CONTEXT_ENTITY_BODY),
    ITEM_BODY(TridentSuggestionTags.CONTEXT_ITEM_BODY),
    INTERPOLATION_VALUE(TridentSuggestionTags.CONTEXT_INTERPOLATION_VALUE);
    private final String tag;

    SnippetContext(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
