package com.energyxxer.trident.ui.editor.completion.snippets;

import com.energyxxer.commodore.CommandUtils;
import com.energyxxer.enxlex.lexical_analysis.profiles.ScannerContextResponse;
import com.energyxxer.trident.global.Preferences;
import com.energyxxer.trident.global.temp.lang_defaults.presets.JSONLexerProfile;
import com.energyxxer.trident.ui.editor.completion.SnippetSuggestion;
import com.energyxxer.util.logger.Debug;

import java.util.ArrayList;

public final class SnippetManager {
    private static ArrayList<Snippet> snippets = new ArrayList<>();

    static {
        snippets.add(new Snippet("dee", "define entity $END$ {\n}", "Defines a custom entity").setContextEnabled(SnippetContext.ENTRY));
        snippets.add(new Snippet("deec", "define entity component $END$ {\n}", "Defines an entity component").setContextEnabled(SnippetContext.ENTRY));
        snippets.add(new Snippet("dei", "define item $END$ {\n}", "Defines a custom item").setContextEnabled(SnippetContext.ENTRY));
        snippets.add(new Snippet("deo", "define objective $END$", "Defines a scoreboard objective").setContextEnabled(SnippetContext.ENTRY));
        snippets.add(new Snippet("scoo", "scoreboard players operation $END$", "scoreboard players operation").setContextEnabled(SnippetContext.COMMAND));
        snippets.add(new Snippet("dheal", "default health $END$", "Sets a custom entity's default health").setContextEnabled(SnippetContext.ENTITY_BODY));
        snippets.add(new Snippet("dname", "default name $END$", "Sets a custom entity/item's default name").setContextEnabled(SnippetContext.ENTITY_BODY).setContextEnabled(SnippetContext.ITEM_BODY));
        snippets.add(new Snippet("dnbt", "default nbt {$END$}", "Sets a custom entity/item's default NBT").setContextEnabled(SnippetContext.ENTITY_BODY).setContextEnabled(SnippetContext.ITEM_BODY));
        snippets.add(new Snippet("dlore", "default lore [$END$]", "Sets a custom item's default lore").setContextEnabled(SnippetContext.ITEM_BODY));
        snippets.add(new Snippet("tickf", "ticking function {\n    $END$\n}", "Creates an entity ticking function").setContextEnabled(SnippetContext.ENTITY_BODY));

        snippets.add(new Snippet("asat", "as $END$ at @s", "as <entity> at @s").setContextEnabled(SnippetContext.MODIFIER));
    }


    /**
     * SnippetManager should not be instantiated.
     * */
    private SnippetManager() {

    }

    public static ArrayList<Snippet> getAll() {
        return snippets;
    }

    public static ArrayList<SnippetSuggestion> createSuggestionsForTag(String tag) {
        ArrayList<SnippetSuggestion> list = new ArrayList<>();
        for(Snippet snippet : snippets) {
            if(!snippet.expanderApplied && snippet.isContextEnabledForTag(tag)) {
                list.add(snippet.createSuggestion());
                snippet.expanderApplied = true;
            }
        }
        return list;
    }

    public static void load() {
        String saveData = Preferences.get("snippets", null);
        if(saveData == null) return;
        snippets.clear();

        while(saveData.length() > 0) {
            Snippet snippet = new Snippet();

            snippet.setEnabled(saveData.charAt(0) == 'e');
            ScannerContextResponse response;
            saveData = saveData.substring(1);

            response = JSONLexerProfile.STRING_LEXER_CONTEXT.analyze(saveData, null);
            saveData = saveData.substring(response.value.length());
            snippet.setShorthand(CommandUtils.parseQuotedString(response.value));

            response = JSONLexerProfile.STRING_LEXER_CONTEXT.analyze(saveData, null);
            saveData = saveData.substring(response.value.length());
            snippet.setDescription(CommandUtils.parseQuotedString(response.value));

            response = JSONLexerProfile.STRING_LEXER_CONTEXT.analyze(saveData, null);
            saveData = saveData.substring(response.value.length());
            snippet.setText(CommandUtils.parseQuotedString(response.value));

            while(saveData.charAt(0) != ';') {
                response = JSONLexerProfile.STRING_LEXER_CONTEXT.analyze(saveData, null);
                saveData = saveData.substring(response.value.length());
                snippet.setContextEnabled(SnippetContext.valueOf(CommandUtils.parseQuotedString(response.value)));
            }
            saveData = saveData.substring(1);

            snippets.add(snippet);
        }
    }

    public static void save() {
        StringBuilder sb = new StringBuilder();
        for(Snippet snippet : snippets) {
            sb.append(snippet.getSaveData());
        }
        Debug.log("Saving snippets: " + sb.toString());
        Preferences.put("snippets", sb.toString());
    }
}
