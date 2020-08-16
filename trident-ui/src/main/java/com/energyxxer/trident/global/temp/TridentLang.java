package com.energyxxer.trident.global.temp;

import com.energyxxer.commodore.CommandUtils;
import com.energyxxer.commodore.types.defaults.BlockType;
import com.energyxxer.commodore.types.defaults.EntityType;
import com.energyxxer.commodore.types.defaults.FunctionReference;
import com.energyxxer.commodore.types.defaults.ItemType;
import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummary;
import com.energyxxer.enxlex.lexical_analysis.summary.SummaryModule;
import com.energyxxer.enxlex.suggestions.ComplexSuggestion;
import com.energyxxer.enxlex.suggestions.Suggestion;
import com.energyxxer.enxlex.suggestions.SuggestionModule;
import com.energyxxer.trident.compiler.TridentUtil;
import com.energyxxer.trident.compiler.lexer.TridentLexerProfile;
import com.energyxxer.trident.compiler.lexer.TridentSuggestionTags;
import com.energyxxer.trident.compiler.lexer.summaries.SummarySymbol;
import com.energyxxer.trident.compiler.lexer.summaries.TridentSummaryModule;
import com.energyxxer.trident.compiler.util.TridentProjectSummary;
import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.global.temp.projects.TridentProject;
import com.energyxxer.trident.langinterface.ProjectType;
import com.energyxxer.trident.ui.editor.completion.ExpandableSuggestionToken;
import com.energyxxer.trident.ui.editor.completion.SuggestionDialog;
import com.energyxxer.trident.ui.editor.completion.SuggestionToken;
import com.energyxxer.trident.ui.editor.completion.paths.ResourcePathExpander;
import com.energyxxer.trident.ui.editor.completion.paths.ResourcePathNode;
import com.energyxxer.trident.ui.editor.completion.snippets.SnippetManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TridentLang extends Lang {
    private static TridentLang INSTANCE;

    private TridentLang() {
        super("TRIDENT",
                TridentLexerProfile.INSTANCE::getValue,
                () -> {
                    Project activeProject = Commons.getActiveProject();
                    if(activeProject instanceof TridentProject) {
                        return activeProject.getFileStructure();
                    }
                    return null;
                },
                "tdn"
        );

        this.putProperty("line_comment_marker","#");
        setIconName("trident_file");
    }

    public static void load() {
        INSTANCE = new TridentLang();
        ProjectType type = TridentProject.PROJECT_TYPE;
    }

    @Override
    public boolean usesSuggestionModule() {
        return true;
    }

    @Override
    public SummaryModule createSummaryModule() {
        return new TridentSummaryModule();
    }

    @Override
    public void joinToProjectSummary(SummaryModule summaryModule, File file, Project project) {
        ((TridentSummaryModule) summaryModule).setParentSummary((TridentProjectSummary) project.getSummary());
        if(project.getSummary() != null) {
            ((TridentSummaryModule) summaryModule).setFileLocation(((TridentProject)project).getSummary().getLocationForFile(file));
        }
    }

    @Override
    public boolean expandSuggestion(ComplexSuggestion suggestion, ArrayList<SuggestionToken> tokens, SuggestionDialog dialog, SuggestionModule suggestionModule) {
        switch(suggestion.getKey()) {
            case TridentSuggestionTags
                    .IDENTIFIER_EXISTING: {
                if(dialog.getLastSuccessfulSummary() != null) {
                    for(SummarySymbol sym : ((TridentSummaryModule) dialog.getLastSuccessfulSummary()).getSymbolsVisibleAt(suggestionModule.getSuggestionIndex())) {
                        ExpandableSuggestionToken token = new ExpandableSuggestionToken(dialog, sym.getName(), suggestion);
                        token.setIconKey(ExpandableSuggestionToken.getIconKeyForTags(sym.getSuggestionTags()));
                        tokens.add(0, token);
                        if(sym.getParentFileSummary() != dialog.getLastSuccessfulSummary()) {
                            token.setDarkened(true);
                        }
                    }
                }
                break;
            }
            case TridentSuggestionTags
                    .IDENTIFIER_MEMBER: {
                if(dialog.getLastSuccessfulSummary() != null) {
                    Collection<SummarySymbol> membersVisible = ((TridentSummaryModule) dialog.getLastSuccessfulSummary()).getSymbolsVisibleAtIndexForPath(suggestionModule.getSuggestionIndex(), suggestionModule.getLookingAtMemberPath());
                    ArrayList<ExpandableSuggestionToken> newTokens = new ArrayList<>(membersVisible.size());
                    for(SummarySymbol sym : membersVisible) {
                        String text = sym.getName();
                        int backspaces = 0;
                        int endIndex = -1;
                        if(sym.getSuggestionTags().contains(TridentSuggestionTags.TAG_METHOD)) {
                            text += "()";
                            endIndex = text.length()-1;
                        } else {
                            if(!TridentLexerProfile.isValidIdentifier(text)) {
                                text = "[" + CommandUtils.quote(text) + "]";
                                backspaces = 1;
                            }
                        }
                        ExpandableSuggestionToken token = new ExpandableSuggestionToken(dialog, sym.getName(), text, suggestion);
                        token.setBackspaces(backspaces);
                        token.setEndIndex(endIndex);
                        token.setIconKey(ExpandableSuggestionToken.getIconKeyForTags(sym.getSuggestionTags()));
                        newTokens.add(token);
                    }
                    tokens.addAll(0, newTokens);
                }
                break;
            }
            case TridentSuggestionTags
                    .OBJECTIVE_EXISTING: {
                if(dialog.getLastSuccessfulSummary() != null) {
                    for(String objective : ((TridentSummaryModule) dialog.getLastSuccessfulSummary()).getAllObjectives()) {
                        ExpandableSuggestionToken token = new ExpandableSuggestionToken(dialog, objective, suggestion);
                        token.setIconKey("objective");
                        tokens.add(0, token);
                    }
                }
                break;
            }
            case TridentSuggestionTags.TRIDENT_FUNCTION:
            case TridentSuggestionTags.FUNCTION:{
                collectResourceLocationSuggestions(
                        dialog, suggestion, tokens,
                        s -> ((TridentProjectSummary) s).getFunctionResources(suggestion.getKey().equals(TridentSuggestionTags.FUNCTION)),
                        "function");
                break;
            }
            case TridentSuggestionTags.SOUND_RESOURCE: {
                collectResourceLocationSuggestions(
                        dialog, suggestion, tokens,
                        s -> ((TridentProjectSummary) s).getSoundEvents(),
                        "sound");
                break;
            }
            case TridentSuggestionTags.BLOCK: {
                collectResourceLocationSuggestions(
                        dialog, suggestion, tokens,
                        s -> ((TridentProjectSummary) s).getTypes().get(BlockType.CATEGORY),
                        "block", true);
                break;
            }
            case TridentSuggestionTags.BLOCK_TAG: {
                collectResourceLocationSuggestions(
                        dialog, suggestion, tokens,
                        s -> ((TridentProjectSummary) s).getTags().get(BlockType.CATEGORY),
                        "block_tag");
                break;
            }
            case TridentSuggestionTags.ITEM: {
                if(dialog.getSummary() != null) {
                    collectResourceLocationSuggestions(
                            dialog, suggestion, tokens,
                            s -> ((TridentProjectSummary) s).getTypes().get(ItemType.CATEGORY),
                            "item", true);
                    for(SummarySymbol sym : ((TridentSummaryModule) dialog.getSummary()).getSymbolsVisibleAt(suggestionModule.getSuggestionIndex())) {
                        if(sym.getSuggestionTags().contains(TridentSuggestionTags.TAG_CUSTOM_ITEM)) {
                            ExpandableSuggestionToken token = new ExpandableSuggestionToken(dialog, "$" + sym.getName(), suggestion);
                            token.setIconKey(ExpandableSuggestionToken.getIconKeyForTags(sym.getSuggestionTags()));
                            tokens.add(0, token);
                            if(sym.getParentFileSummary() != dialog.getSummary()) {
                                token.setDarkened(true);
                            }
                        }
                    }
                }
                break;
            }
            case TridentSuggestionTags.ITEM_TAG: {
                collectResourceLocationSuggestions(
                        dialog, suggestion, tokens,
                        s -> ((TridentProjectSummary) s).getTags().get(ItemType.CATEGORY),
                        "item_tag");
                break;
            }
            case TridentSuggestionTags.ENTITY_TYPE: {
                if(dialog.getSummary() != null) {
                    collectResourceLocationSuggestions(
                            dialog, suggestion, tokens,
                            s -> ((TridentProjectSummary) s).getTypes().get(EntityType.CATEGORY),
                            "entity", true);
                    for(SummarySymbol sym : ((TridentSummaryModule) dialog.getSummary()).getSymbolsVisibleAt(suggestionModule.getSuggestionIndex())) {
                        if(sym.getSuggestionTags().contains(TridentSuggestionTags.TAG_CUSTOM_ENTITY) && !sym.getSuggestionTags().contains(TridentSuggestionTags.TAG_ENTITY_COMPONENT)) {
                            ExpandableSuggestionToken token = new ExpandableSuggestionToken(dialog, "$" + sym.getName(), suggestion);
                            token.setIconKey(ExpandableSuggestionToken.getIconKeyForTags(sym.getSuggestionTags()));
                            tokens.add(0, token);
                            if(sym.getParentFileSummary() != dialog.getSummary()) {
                                token.setDarkened(true);
                            }
                        }
                    }
                }
                break;
            }
            case TridentSuggestionTags.ENTITY_TYPE_TAG:{
                collectResourceLocationSuggestions(
                        dialog, suggestion, tokens,
                        s -> ((TridentProjectSummary) s).getTags().get(EntityType.CATEGORY),
                        "entity_type_tag");
                break;
            }
            case TridentSuggestionTags.FUNCTION_TAG: {
                collectResourceLocationSuggestions(
                        dialog, suggestion, tokens,
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
                suggestionModule.getSuggestions().addAll(SnippetManager.createSuggestionsForTag(suggestion.getKey()));
                break;
            }
            case TridentSuggestionTags.BOOLEAN: {
                tokens.add(new ExpandableSuggestionToken(dialog, "true", suggestion));
                tokens.add(new ExpandableSuggestionToken(dialog, "false", suggestion));
                break;
            }
            default: {
                return false;
            }
        }
        return true;
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
