package com.energyxxer.trident.ui.editor.completion;

import com.energyxxer.enxlex.suggestions.ComplexSuggestion;
import com.energyxxer.enxlex.suggestions.LiteralSuggestion;
import com.energyxxer.enxlex.suggestions.Suggestion;
import com.energyxxer.enxlex.suggestions.SuggestionModule;
import com.energyxxer.trident.compiler.lexer.TridentSuggestionTags;
import com.energyxxer.trident.compiler.lexer.summaries.SummarySymbol;
import com.energyxxer.trident.ui.editor.completion.snippets.SnippetManager;
import com.energyxxer.util.logger.Debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class SuggestionExpander {
    public static Collection<SuggestionToken> expand(Suggestion suggestion, SuggestionDialog parent, SuggestionModule suggestionModule) {
        if(suggestion instanceof LiteralSuggestion) {
            return Collections.singletonList(new SuggestionToken(parent, ((LiteralSuggestion) suggestion).getPreview(), ((LiteralSuggestion) suggestion).getLiteral(), suggestion));
        } else if(suggestion instanceof ComplexSuggestion) {
            ArrayList<SuggestionToken> tokens = new ArrayList<>();
            switch(((ComplexSuggestion) suggestion).getKey()) {
                case TridentSuggestionTags
                        .IDENTIFIER_EXISTING: {
                    if(parent.getSummary() != null) {
                        for(SummarySymbol sym : parent.getSummary().getSymbolsVisibleAt(suggestionModule.getSuggestionIndex())) {
                            SuggestionToken token = new SuggestionToken(parent, sym.getName(), suggestion);
                            token.setIconKey(SuggestionToken.getIconKeyForTags(sym.getSuggestionTags()));
                            tokens.add(0, token);
                        }
                    }
                    break;
                }
                case TridentSuggestionTags
                        .OBJECTIVE_EXISTING: {
                    if(parent.getSummary() != null) {
                        for(SummarySymbol sym : parent.getSummary().getObjectives()) {
                            SuggestionToken token = new SuggestionToken(parent, sym.getName(), suggestion);
                            token.setIconKey(SuggestionToken.getIconKeyForTags(sym.getSuggestionTags()));
                            tokens.add(0, token);
                        }
                    }
                    break;
                }
                case TridentSuggestionTags.CONTEXT_ENTRY:
                case TridentSuggestionTags.CONTEXT_COMMAND:
                case TridentSuggestionTags.CONTEXT_MODIFIER:
                case TridentSuggestionTags.CONTEXT_ENTITY_BODY:
                case TridentSuggestionTags.CONTEXT_ITEM_BODY:
                case TridentSuggestionTags.CONTEXT_INTERPOLATION_VALUE: {
                    suggestionModule.getSuggestions().addAll(SnippetManager.createSuggestionsForTag(((ComplexSuggestion) suggestion).getKey()));
                    break;
                }
                default: {
                    Debug.log(((ComplexSuggestion) suggestion).getKey());
                }
            }
            return tokens;
        }
        throw new IllegalArgumentException();
    }
}
