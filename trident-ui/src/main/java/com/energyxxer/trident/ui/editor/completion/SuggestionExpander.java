package com.energyxxer.trident.ui.editor.completion;

import com.energyxxer.commodore.CommandUtils;
import com.energyxxer.commodore.types.defaults.BlockType;
import com.energyxxer.commodore.types.defaults.EntityType;
import com.energyxxer.commodore.types.defaults.FunctionReference;
import com.energyxxer.commodore.types.defaults.ItemType;
import com.energyxxer.crossbow.compiler.lexer.CrossbowSuggestionTags;
import com.energyxxer.crossbow.compiler.lexer.summaries.CrossbowSummaryModule;
import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummary;
import com.energyxxer.enxlex.suggestions.ComplexSuggestion;
import com.energyxxer.enxlex.suggestions.LiteralSuggestion;
import com.energyxxer.enxlex.suggestions.Suggestion;
import com.energyxxer.enxlex.suggestions.SuggestionModule;
import com.energyxxer.trident.compiler.TridentUtil;
import com.energyxxer.trident.compiler.lexer.TridentLexerProfile;
import com.energyxxer.trident.compiler.lexer.TridentSuggestionTags;
import com.energyxxer.trident.compiler.lexer.summaries.SummarySymbol;
import com.energyxxer.trident.compiler.lexer.summaries.TridentSummaryModule;
import com.energyxxer.trident.compiler.util.TridentProjectSummary;
import com.energyxxer.trident.ui.editor.completion.paths.ResourcePathExpander;
import com.energyxxer.trident.ui.editor.completion.paths.ResourcePathNode;
import com.energyxxer.trident.ui.editor.completion.snippets.SnippetManager;
import com.energyxxer.util.logger.Debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SuggestionExpander {
    public static Collection<SuggestionToken> expand(Suggestion suggestion, SuggestionDialog parent, SuggestionModule suggestionModule) {
        if(suggestion instanceof LiteralSuggestion) {
            return Collections.singletonList(new ExpandableSuggestionToken(parent, ((LiteralSuggestion) suggestion).getPreview(), ((LiteralSuggestion) suggestion).getLiteral(), suggestion));
        } else if(suggestion instanceof ComplexSuggestion) {
            ArrayList<SuggestionToken> tokens = new ArrayList<>();
            switch(((ComplexSuggestion) suggestion).getKey()) {
                case TridentSuggestionTags
                        .IDENTIFIER_EXISTING: {
                    if(parent.getSummary() != null) {
                        for(SummarySymbol sym : ((TridentSummaryModule) parent.getSummary()).getSymbolsVisibleAt(suggestionModule.getSuggestionIndex())) {
                            ExpandableSuggestionToken token = new ExpandableSuggestionToken(parent, sym.getName(), suggestion);
                            token.setIconKey(ExpandableSuggestionToken.getIconKeyForTags(sym.getSuggestionTags()));
                            tokens.add(0, token);
                            if(sym.getParentFileSummary() != parent.getSummary()) {
                                token.setDarkened(true);
                            }
                        }
                    }
                    break;
                }
                case CrossbowSuggestionTags
                        .IDENTIFIER_EXISTING: {
                    if(parent.getSummary() != null) {
                        for(com.energyxxer.crossbow.compiler.lexer.summaries.SummarySymbol sym : ((CrossbowSummaryModule) parent.getSummary()).getSymbolsVisibleAt(suggestionModule.getSuggestionIndex())) {
                            ExpandableSuggestionToken token = new ExpandableSuggestionToken(parent, sym.getName(), suggestion);
                            token.setIconKey(ExpandableSuggestionToken.getIconKeyForTags(sym.getSuggestionTags()));
                            tokens.add(0, token);
                            if(sym.getParentFileSummary() != parent.getSummary()) {
                                token.setDarkened(true);
                            }
                        }
                    }
                    break;
                }
                case TridentSuggestionTags
                        .IDENTIFIER_MEMBER: {
                    if(parent.getSummary() != null) {
                        Collection<SummarySymbol> membersVisible = ((TridentSummaryModule) parent.getSummary()).getSymbolsVisibleAtIndexForPath(suggestionModule.getSuggestionIndex(), suggestionModule.getLookingAtMemberPath());
                        ArrayList<ExpandableSuggestionToken> newTokens = new ArrayList<>(membersVisible.size());
                        for(SummarySymbol sym : membersVisible) {
                            String text = sym.getName();
                            int backspaces = 0;
                            if(!TridentLexerProfile.isValidIdentifier(text)) {
                                text = "[" + CommandUtils.quote(text) + "]";
                                backspaces = 1;
                            }
                            ExpandableSuggestionToken token = new ExpandableSuggestionToken(parent, sym.getName(), text, suggestion);
                            token.setBackspaces(backspaces);
                            token.setIconKey(ExpandableSuggestionToken.getIconKeyForTags(sym.getSuggestionTags()));
                            newTokens.add(token);
                        }
                        tokens.addAll(0, newTokens);
                    }
                    break;
                }
                case CrossbowSuggestionTags
                        .IDENTIFIER_MEMBER: {
                    if(parent.getSummary() != null) {
                        Collection<com.energyxxer.crossbow.compiler.lexer.summaries.SummarySymbol> membersVisible = ((CrossbowSummaryModule) parent.getSummary()).getSymbolsVisibleAtIndexForPath(suggestionModule.getSuggestionIndex(), suggestionModule.getLookingAtMemberPath());
                        ArrayList<ExpandableSuggestionToken> newTokens = new ArrayList<>(membersVisible.size());
                        for(com.energyxxer.crossbow.compiler.lexer.summaries.SummarySymbol sym : membersVisible) {
                            String text = sym.getName();
                            int backspaces = 0;
                            if(!TridentLexerProfile.isValidIdentifier(text)) {
                                text = "[" + CommandUtils.quote(text) + "]";
                                backspaces = 1;
                            }
                            ExpandableSuggestionToken token = new ExpandableSuggestionToken(parent, sym.getName(), text, suggestion);
                            token.setBackspaces(backspaces);
                            token.setIconKey(ExpandableSuggestionToken.getIconKeyForTags(sym.getSuggestionTags()));
                            newTokens.add(token);
                        }
                        tokens.addAll(0, newTokens);
                    }
                    break;
                }
                case TridentSuggestionTags
                        .OBJECTIVE_EXISTING: {
                    if(parent.getSummary() != null) {
                        for(String objective : ((TridentSummaryModule) parent.getSummary()).getAllObjectives()) {
                            ExpandableSuggestionToken token = new ExpandableSuggestionToken(parent, objective, suggestion);
                            token.setIconKey("objective");
                            tokens.add(0, token);
                        }
                    }
                    break;
                }
                case TridentSuggestionTags.TRIDENT_FUNCTION:
                case TridentSuggestionTags.FUNCTION:{
                    collectResourceLocationSuggestions(
                            parent, suggestion, tokens,
                            s -> ((TridentProjectSummary) s).getFunctionResources(((ComplexSuggestion) suggestion).getKey().equals(TridentSuggestionTags.FUNCTION)),
                            "function");
                    break;
                }
                case TridentSuggestionTags.SOUND_RESOURCE: {
                    collectResourceLocationSuggestions(
                            parent, suggestion, tokens,
                            s -> ((TridentProjectSummary) s).getSoundEvents(),
                            "sound");
                    break;
                }
                case TridentSuggestionTags.BLOCK: {
                    collectResourceLocationSuggestions(
                            parent, suggestion, tokens,
                            s -> ((TridentProjectSummary) s).getTypes().get(BlockType.CATEGORY),
                            "block", true);
                    break;
                }
                case TridentSuggestionTags.BLOCK_TAG: {
                    collectResourceLocationSuggestions(
                            parent, suggestion, tokens,
                            s -> ((TridentProjectSummary) s).getTags().get(BlockType.CATEGORY),
                            "block_tag");
                    break;
                }
                case TridentSuggestionTags.ITEM: {
                    if(parent.getSummary() != null) {
                        collectResourceLocationSuggestions(
                                parent, suggestion, tokens,
                                s -> ((TridentProjectSummary) s).getTypes().get(ItemType.CATEGORY),
                                "item", true);
                        for(SummarySymbol sym : ((TridentSummaryModule)parent.getSummary()).getSymbolsVisibleAt(suggestionModule.getSuggestionIndex())) {
                            if(sym.getSuggestionTags().contains(TridentSuggestionTags.TAG_CUSTOM_ITEM)) {
                                ExpandableSuggestionToken token = new ExpandableSuggestionToken(parent, "$" + sym.getName(), suggestion);
                                token.setIconKey(ExpandableSuggestionToken.getIconKeyForTags(sym.getSuggestionTags()));
                                tokens.add(0, token);
                                if(sym.getParentFileSummary() != parent.getSummary()) {
                                    token.setDarkened(true);
                                }
                            }
                        }
                    }
                    break;
                }
                case TridentSuggestionTags.ITEM_TAG: {
                    collectResourceLocationSuggestions(
                            parent, suggestion, tokens,
                            s -> ((TridentProjectSummary) s).getTags().get(ItemType.CATEGORY),
                            "item_tag");
                    break;
                }
                case TridentSuggestionTags.ENTITY_TYPE: {
                    if(parent.getSummary() != null) {
                        collectResourceLocationSuggestions(
                                parent, suggestion, tokens,
                                s -> ((TridentProjectSummary) s).getTypes().get(EntityType.CATEGORY),
                                "entity", true);
                        for(SummarySymbol sym : ((TridentSummaryModule)parent.getSummary()).getSymbolsVisibleAt(suggestionModule.getSuggestionIndex())) {
                            if(sym.getSuggestionTags().contains(TridentSuggestionTags.TAG_CUSTOM_ENTITY) && !sym.getSuggestionTags().contains(TridentSuggestionTags.TAG_ENTITY_COMPONENT)) {
                                ExpandableSuggestionToken token = new ExpandableSuggestionToken(parent, "$" + sym.getName(), suggestion);
                                token.setIconKey(ExpandableSuggestionToken.getIconKeyForTags(sym.getSuggestionTags()));
                                tokens.add(0, token);
                                if(sym.getParentFileSummary() != parent.getSummary()) {
                                    token.setDarkened(true);
                                }
                            }
                        }
                    }
                    break;
                }
                case TridentSuggestionTags.ENTITY_TYPE_TAG:{
                    collectResourceLocationSuggestions(
                            parent, suggestion, tokens,
                            s -> ((TridentProjectSummary) s).getTags().get(EntityType.CATEGORY),
                            "entity_type_tag");
                    break;
                }
                case TridentSuggestionTags.FUNCTION_TAG: {
                    collectResourceLocationSuggestions(
                            parent, suggestion, tokens,
                            s -> ((TridentProjectSummary) s).getTags().get(FunctionReference.CATEGORY),
                            "function");
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
                    if(((ComplexSuggestion) suggestion).getKey().startsWith("cspn:")) {
                        Debug.log("Found a parameter name suggestion: " + ((ComplexSuggestion) suggestion).getKey());
                        tokens.add(new ParameterNameSuggestionToken(((ComplexSuggestion) suggestion).getKey().substring("cspn:".length())));
                    } else {
                        Debug.log("Missing SuggestionExpander case for: " + ((ComplexSuggestion) suggestion).getKey());
                    }
                }
            }
            return tokens;
        }
        throw new IllegalArgumentException();
    }

    private static void collectResourceLocationSuggestions(SuggestionDialog parent, Suggestion suggestion, ArrayList<SuggestionToken> tokens, Function<ProjectSummary, Collection<TridentUtil.ResourceLocation>> picker, String leafIcon) {
        collectResourceLocationSuggestions(parent, suggestion, tokens, picker, leafIcon, false);
    }

    private static void collectResourceLocationSuggestions(SuggestionDialog parent, Suggestion suggestion, ArrayList<SuggestionToken> tokens, Function<ProjectSummary, Collection<TridentUtil.ResourceLocation>> picker, String leafIcon, boolean skipNamespace) {
        if(parent.getSummary() != null && parent.getSummary().getParentSummary() != null) {
            Collection<TridentUtil.ResourceLocation> locations = picker.apply(parent.getSummary().getParentSummary());
            if(locations == null) return;

            Collection<ResourcePathNode> nodes = ResourcePathExpander.expand(
                    locations.stream().map(TridentUtil.ResourceLocation::toString).collect(Collectors.toList()),
                    parent, suggestion, skipNamespace, skipNamespace);

            nodes.forEach(n -> {if(n.isLeaf()) n.setIconKey(leafIcon);});
            tokens.addAll(0, nodes);
        }
    }
}
