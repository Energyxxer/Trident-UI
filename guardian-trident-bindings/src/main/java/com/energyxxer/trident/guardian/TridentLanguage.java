package com.energyxxer.trident.guardian;

import com.energyxxer.commodore.types.defaults.BlockType;
import com.energyxxer.commodore.types.defaults.EntityType;
import com.energyxxer.commodore.types.defaults.FunctionReference;
import com.energyxxer.commodore.types.defaults.ItemType;
import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummary;
import com.energyxxer.enxlex.lexical_analysis.summary.SummaryModule;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.suggestions.ComplexSuggestion;
import com.energyxxer.enxlex.suggestions.Suggestion;
import com.energyxxer.enxlex.suggestions.SuggestionModule;
import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.global.temp.Lang;
import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.langinterface.ProjectType;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.caret.EditorCaret;
import com.energyxxer.guardian.ui.editor.completion.ExpandableSuggestionToken;
import com.energyxxer.guardian.ui.editor.completion.SnippetSuggestion;
import com.energyxxer.guardian.ui.editor.completion.SuggestionDialog;
import com.energyxxer.guardian.ui.editor.completion.SuggestionToken;
import com.energyxxer.guardian.ui.editor.completion.paths.ResourcePathExpander;
import com.energyxxer.guardian.ui.editor.completion.paths.ResourcePathNode;
import com.energyxxer.guardian.ui.editor.completion.snippets.Snippet;
import com.energyxxer.guardian.ui.editor.completion.snippets.SnippetContext;
import com.energyxxer.guardian.ui.editor.completion.snippets.SnippetManager;
import com.energyxxer.guardian.ui.editor.highlighters.AssociatedSymbolHighlighter;
import com.energyxxer.prismarine.summaries.PrismarineSummaryModule;
import com.energyxxer.prismarine.summaries.SummarySymbol;
import com.energyxxer.prismarine.summaries.SymbolSuggestion;
import com.energyxxer.trident.compiler.ResourceLocation;
import com.energyxxer.trident.compiler.lexer.TridentLexerProfile;
import com.energyxxer.trident.compiler.lexer.TridentSuggestionTags;
import com.energyxxer.trident.compiler.lexer.TridentTokens;
import com.energyxxer.trident.compiler.lexer.summaries.TridentProjectSummary;
import com.energyxxer.trident.compiler.lexer.summaries.TridentSummaryModule;

import javax.swing.text.BadLocationException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TridentLanguage extends Lang {
    private static TridentLanguage INSTANCE;

    public static SnippetContext ENTRY;
    public static SnippetContext COMMAND;
    public static SnippetContext MODIFIER;
    public static SnippetContext ENTITY_BODY;
    public static SnippetContext ITEM_BODY;
    public static SnippetContext CLASS_BODY;
    public static SnippetContext INTERPOLATION_VALUE;

    private TridentLanguage() {
        super("TRIDENT", "Trident",
                true,
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
        INSTANCE = new TridentLanguage();
        //load TridentProject class
        ProjectType type = TridentProject.PROJECT_TYPE;

        ENTRY =                  new SnippetContext(INSTANCE, "ENTRY", "Entry", TridentSuggestionTags.CONTEXT_ENTRY);
        COMMAND =                new SnippetContext(INSTANCE, "COMMAND", "Command", TridentSuggestionTags.CONTEXT_COMMAND);
        MODIFIER =               new SnippetContext(INSTANCE, "MODIFIER", "Modifier", TridentSuggestionTags.CONTEXT_MODIFIER);
        ENTITY_BODY =            new SnippetContext(INSTANCE, "ENTITY_BODY", "Entity Body", TridentSuggestionTags.CONTEXT_ENTITY_BODY);
        ITEM_BODY =              new SnippetContext(INSTANCE, "ITEM_BODY", "Item Body", TridentSuggestionTags.CONTEXT_ITEM_BODY);
        CLASS_BODY =             new SnippetContext(INSTANCE, "CLASS_BODY", "Class Body", TridentSuggestionTags.CONTEXT_CLASS_BODY);
        INTERPOLATION_VALUE =    new SnippetContext(INSTANCE, "INTERPOLATION_VALUE", "Interpolation Value", TridentSuggestionTags.CONTEXT_INTERPOLATION_VALUE);

        //Add aliases to old context codes for backwards compatibility
        SnippetContext.addAliasCode("ENTRY", ENTRY);
        SnippetContext.addAliasCode("COMMAND", COMMAND);
        SnippetContext.addAliasCode("MODIFIER", MODIFIER);
        SnippetContext.addAliasCode("ENTITY_BODY", ENTITY_BODY);
        SnippetContext.addAliasCode("ITEM_BODY", ITEM_BODY);
        SnippetContext.addAliasCode("CLASS_BODY", CLASS_BODY);
        SnippetContext.addAliasCode("INTERPOLATION_VALUE", INTERPOLATION_VALUE);
    }

    @Override
    public boolean usesSuggestionModule() {
        return true;
    }

    @Override
    public PrismarineSummaryModule createSummaryModule() {
        return new TridentSummaryModule();
    }

    @Override
    public void joinToProjectSummary(SummaryModule summaryModule, File file, Project project) {
        if(project.getSummary() != null) {
            project.getSummary().incrementGeneration();
        }
        ((TridentSummaryModule) summaryModule).setParentSummary(project.getSummary());
        if(project.getSummary() != null) {
            ((TridentSummaryModule) summaryModule).setFileLocation(((TridentProject)project).getSummary().getLocationForFile(file));
        }
    }

    @Override
    public boolean isBraceToken(Token token) {
        return token.type == TridentTokens.BRACE;
    }

    @Override
    public boolean isStringToken(Token token) {
        return token.type == TridentTokens.STRING_LITERAL;
    }

    private boolean shouldDarkenSymbol(SummarySymbol symbol, PrismarineSummaryModule lastSuccessfulSummary) {
        return symbol.getParentFileSummary() != null && symbol.getParentFileSummary().getFileLocation() != null && !symbol.getParentFileSummary().getFileLocation().equals(lastSuccessfulSummary.getFileLocation());
    }

    @Override
    public void expandSymbolSuggestion(SymbolSuggestion suggestion, ArrayList<SuggestionToken> tokens, SuggestionDialog dialog, SuggestionModule suggestionModule) {
        if(dialog.getLastSuccessfulSummary() != null) {
            PrismarineSummaryModule fs = dialog.getLastSuccessfulSummary();
            SummarySymbol sym = suggestion.getSymbol();

            String text = sym.getName();
            int endIndex = 0;
            String preview = text;

            if (sym.hasSuggestionTag(TridentSuggestionTags.TAG_METHOD)) {
                text += "()";
                endIndex = -1;
            }

            ExpandableSuggestionToken token = new ExpandableSuggestionToken(dialog, preview, text, suggestion);
            token.setEndIndex(text.length() + endIndex);

            if (sym.getType(fs) != null) {
                token.setDescription(sym.getType(fs).getName());
            }

            token.setIconKey(TridentLanguage.INSTANCE.getIconKeyForSuggestionTags(sym.getSuggestionTags()));

            tokens.add(0, token);
            if (shouldDarkenSymbol(sym, dialog.getLastSuccessfulSummary())) {
                token.setAlpha(0.6f);
            }
        }
    }

    @Override
    public void expandComplexSuggestion(ComplexSuggestion suggestion, ArrayList<SuggestionToken> tokens, SuggestionDialog dialog, SuggestionModule suggestionModule) {
        switch(suggestion.getKey()) {
            case TridentSuggestionTags
                    .IDENTIFIER_EXISTING: {
                if(dialog.getLastSuccessfulSummary() != null) {
                    for(SummarySymbol sym : dialog.getLastSuccessfulSummary().getSymbolsVisibleAt(suggestionModule.getSuggestionIndex())) {
                        expandSymbolSuggestion(new SymbolSuggestion(sym), tokens, dialog, suggestionModule);
                    }
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
                    for(SummarySymbol sym : dialog.getSummary().getSymbolsVisibleAt(suggestionModule.getSuggestionIndex())) {
                        if(sym.hasSuggestionTag(TridentSuggestionTags.TAG_CUSTOM_ITEM)) {
                            ExpandableSuggestionToken token = new ExpandableSuggestionToken(dialog, "$" + sym.getName(), suggestion);
                            token.setIconKey(TridentLanguage.INSTANCE.getIconKeyForSuggestionTags(sym.getSuggestionTags()));
                            tokens.add(0, token);
                            if(shouldDarkenSymbol(sym, dialog.getSummary())) {
                                token.setAlpha(0.6f);
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
                    for(SummarySymbol sym : dialog.getSummary().getSymbolsVisibleAt(suggestionModule.getSuggestionIndex())) {
                        if(sym.hasSuggestionTag(TridentSuggestionTags.TAG_CUSTOM_ENTITY) && !sym.hasSuggestionTag(TridentSuggestionTags.TAG_ENTITY_COMPONENT)) {
                            ExpandableSuggestionToken token = new ExpandableSuggestionToken(dialog, "$" + sym.getName(), suggestion);
                            token.setIconKey(TridentLanguage.INSTANCE.getIconKeyForSuggestionTags(sym.getSuggestionTags()));
                            tokens.add(0, token);
                            if(shouldDarkenSymbol(sym, dialog.getSummary())) {
                                token.setAlpha(0.6f);
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

            //SNIPPETS
            case TridentSuggestionTags.CONTEXT_ENTRY:
            case TridentSuggestionTags.CONTEXT_COMMAND:
            case TridentSuggestionTags.CONTEXT_MODIFIER:
            case TridentSuggestionTags.CONTEXT_ENTITY_BODY:
            case TridentSuggestionTags.CONTEXT_ITEM_BODY:
            case TridentSuggestionTags.CONTEXT_CLASS_BODY:
            case TridentSuggestionTags.CONTEXT_INTERPOLATION_VALUE: {
                SnippetManager.createSuggestionsForTag(suggestion.getKey(), suggestionModule.getSuggestions());
                break;
            }

            case TridentSuggestionTags.BOOLEAN: {
                tokens.add(new ExpandableSuggestionToken(dialog, "true", suggestion));
                tokens.add(new ExpandableSuggestionToken(dialog, "false", suggestion));
                break;
            }
        }
    }


    private static void collectResourceLocationSuggestions(SuggestionDialog parent, Suggestion suggestion, ArrayList<SuggestionToken> tokens, Function<ProjectSummary, Collection<ResourceLocation>> picker, String leafIcon) {
        collectResourceLocationSuggestions(parent, suggestion, tokens, picker, leafIcon, false);
    }

    private static void collectResourceLocationSuggestions(SuggestionDialog parent, Suggestion suggestion, ArrayList<SuggestionToken> tokens, Function<ProjectSummary, Collection<ResourceLocation>> picker, String leafIcon, boolean skipNamespace) {
        if(parent.getSummary() != null && parent.getSummary().getParentSummary() != null) {
            Collection<ResourceLocation> locations = picker.apply(parent.getSummary().getParentSummary());
            if(locations == null) return;

            Collection<ResourcePathNode> nodes = ResourcePathExpander.expand(
                    locations.stream().map(ResourceLocation::toString).collect(Collectors.toList()),
                    parent, suggestion, skipNamespace, skipNamespace);

            nodes.forEach(n -> {if(n.isLeaf()) n.setIconKey(leafIcon);});
            tokens.addAll(0, nodes);
        }
    }

    @Override
    public void onEditorAdd(AdvancedEditor editor, EditorCaret caret) {
        try {
            editor.getHighlighter().addHighlight(0, 0, new AssociatedSymbolHighlighter(editor, caret));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getIconKeyForSuggestionTags(Collection<String> tags) {
        if(tags == null) return null;
        if(tags.contains(TridentSuggestionTags.TAG_OBJECTIVE)) {
            return "objective";
        } else if(tags.contains(TridentSuggestionTags.TAG_ENTITY_COMPONENT)) {
            return "feature";
        } else if(tags.contains(TridentSuggestionTags.TAG_CUSTOM_ENTITY)) {
            return "custom_entity";
        } else if(tags.contains(TridentSuggestionTags.TAG_ENTITY_EVENT)) {
            return "entity_event";
        } else if(tags.contains(TridentSuggestionTags.TAG_CUSTOM_ITEM)) {
            return "custom_item";
        } else if(tags.contains(TridentSuggestionTags.TAG_ITEM)) {
            return "item";
        } else if(tags.contains(TridentSuggestionTags.TAG_ENTITY)) {
            return "entity";
        } else if(tags.contains(TridentSuggestionTags.TAG_COORDINATE)) {
            return "coordinates";
        } else if(tags.contains(TridentSuggestionTags.TAG_CLASS)) {
            return "class";
        } else if(tags.contains(TridentSuggestionTags.TAG_METHOD)) {
            return "method";
        } else if(tags.contains(TridentSuggestionTags.TAG_COMMAND)) {
            return "command";
        } else if(tags.contains(TridentSuggestionTags.TAG_MODIFIER)) {
            return "modifier";
        } else if(tags.contains(TridentSuggestionTags.TAG_INSTRUCTION)) {
            return "instruction";
        } else if(tags.contains(SnippetSuggestion.TAG_SNIPPET)) {
            return "snippet";
        } else if(tags.contains(TridentSuggestionTags.TAG_VARIABLE)) {
            return "variable";
        } else if(tags.contains(TridentSuggestionTags.TAG_FIELD)) {
            return "field";
        }
        return null;
    }

    @Override
    public void addDefaultSnippets(ArrayList<Snippet> snippets) {
        snippets.add(new Snippet("dee", "define entity $NAME$ $BASE$ {\n    $END$\n}", "Defines a custom entity").setContextEnabled(ENTRY));
        snippets.add(new Snippet("deec", "define entity component $NAME$ {\n    $END$\n}", "Defines an entity component").setContextEnabled(ENTRY));
        snippets.add(new Snippet("dei", "define item $NAME$ $BASE$ {\n    $END$\n}", "Defines a custom item").setContextEnabled(ENTRY));
        snippets.add(new Snippet("dec", "define class $NAME$ {\n    $END$\n}", "Defines a custom class").setContextEnabled(ENTRY));
        snippets.add(new Snippet("deo", "define objective $NAME$$END$", "Defines a scoreboard objective").setContextEnabled(ENTRY));
        snippets.add(new Snippet("scoo", "scoreboard players operation $END$", "scoreboard players operation").setContextEnabled(COMMAND));
        snippets.add(new Snippet("dheal", "default health $HEALTH$$END$", "Sets a custom entity's default health").setContextEnabled(ENTITY_BODY));
        snippets.add(new Snippet("dname", "default name $NAME$$END$", "Sets a custom entity/item's default name").setContextEnabled(ENTITY_BODY).setContextEnabled(ITEM_BODY));
        snippets.add(new Snippet("dnbt", "default nbt {$END$}", "Sets a custom entity/item's default NBT").setContextEnabled(ENTITY_BODY).setContextEnabled(ITEM_BODY));
        snippets.add(new Snippet("dlore", "default lore [$END$]", "Sets a custom item's default lore").setContextEnabled(ITEM_BODY));
        snippets.add(new Snippet("tickf", "ticking function {\n    $END$\n}", "Creates an entity ticking function").setContextEnabled(ENTITY_BODY));

        snippets.add(new Snippet("asat", "as $ENTITY$ at @s $END$", "as <entity> at @s").setContextEnabled(MODIFIER));
    }
}
