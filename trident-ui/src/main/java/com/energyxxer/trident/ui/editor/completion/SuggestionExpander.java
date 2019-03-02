package com.energyxxer.trident.ui.editor.completion;

import com.energyxxer.enxlex.suggestions.ComplexSuggestion;
import com.energyxxer.enxlex.suggestions.LiteralSuggestion;
import com.energyxxer.enxlex.suggestions.Suggestion;
import com.energyxxer.enxlex.suggestions.SuggestionModule;
import com.energyxxer.trident.compiler.TridentUtil;
import com.energyxxer.trident.compiler.lexer.TridentSuggestionTags;
import com.energyxxer.trident.compiler.lexer.summaries.SummarySymbol;
import com.energyxxer.trident.ui.editor.completion.paths.ResourcePathExpander;
import com.energyxxer.trident.ui.editor.completion.paths.ResourcePathNode;
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
                            if(sym.getParentFileSummary() != parent.getSummary()) {
                                token.setDarkened(true);
                            }
                        }
                    }
                    break;
                }
                case TridentSuggestionTags
                        .OBJECTIVE_EXISTING: {
                    if(parent.getSummary() != null) {
                        for(String objective : parent.getSummary().getAllObjectives()) {
                            SuggestionToken token = new SuggestionToken(parent, objective, suggestion);
                            tokens.add(0, token);
                        }
                    }
                    break;
                }
                case TridentSuggestionTags.TRIDENT_FUNCTION:
                case TridentSuggestionTags.FUNCTION:{
                    if(parent.getSummary() != null && parent.getSummary().getParentSummary() != null) {
                        ArrayList<String> test = new ArrayList<>();
                        for(TridentUtil.ResourceLocation loc : parent.getSummary().getParentSummary().getFunctionResources(((ComplexSuggestion) suggestion).getKey().equals(TridentSuggestionTags.FUNCTION))) {
                            test.add(loc.toString());
                        }
                        Collection<ResourcePathNode> nodes = ResourcePathExpander.expand(test, parent, suggestion);
                        for(ResourcePathNode node : nodes) {
                            if(node.isLeaf()) node.setIconKey("function");
                            tokens.add(0, node);
                        }
                    }
                    break;
                }
                case TridentSuggestionTags.SOUND_RESOURCE:{
                    if(parent.getSummary() != null && parent.getSummary().getParentSummary() != null) {
                        ArrayList<String> test = new ArrayList<>();
                        for(TridentUtil.ResourceLocation loc : parent.getSummary().getParentSummary().getSoundEvents()) {
                            test.add(loc.toString());
                        }
                        Collection<ResourcePathNode> nodes = ResourcePathExpander.expand(test, parent, suggestion);
                        for(ResourcePathNode node : nodes) {
                            if(node.isLeaf()) node.setIconKey("sound");
                            tokens.add(0, node);
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
                    Debug.log("Missing SuggestionExpander case for: " + ((ComplexSuggestion) suggestion).getKey());
                }
            }
            return tokens;
        }
        throw new IllegalArgumentException();
    }
}
