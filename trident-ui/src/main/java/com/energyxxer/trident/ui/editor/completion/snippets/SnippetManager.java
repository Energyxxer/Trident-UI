package com.energyxxer.trident.ui.editor.completion.snippets;

import com.energyxxer.trident.ui.editor.completion.SnippetSuggestion;

import java.util.ArrayList;

public final class SnippetManager {
    private static ArrayList<Snippet> snippets = new ArrayList<>();

    static {
        snippets.add(new Snippet("dee", "define entity $END$ {\n}", "Defines a custom entity").setContextEnabled(SnippetContext.ENTRY));
        snippets.add(new Snippet("deef", "define entity feature $END$ {\n}", "Defines an entity feature").setContextEnabled(SnippetContext.ENTRY));
        snippets.add(new Snippet("dei", "define item $END$ {\n}", "Defines a custom item").setContextEnabled(SnippetContext.ENTRY));
        snippets.add(new Snippet("deo", "define objective $END$", "Defines a scoreboard objective").setContextEnabled(SnippetContext.ENTRY));
        snippets.add(new Snippet("scoo", "scoreboard players operation $END$", "scoreboard players operation").setContextEnabled(SnippetContext.COMMAND));
        snippets.add(new Snippet("dheal", "default health $END$", "Sets a custom entity's default health").setContextEnabled(SnippetContext.ENTITY_BODY));
        snippets.add(new Snippet("dname", "default name $END$", "Sets a custom entity/item's default name").setContextEnabled(SnippetContext.ENTITY_BODY).setContextEnabled(SnippetContext.ITEM_BODY));
        snippets.add(new Snippet("dnbt", "default nbt $END$", "Sets a custom entity/item's default NBT").setContextEnabled(SnippetContext.ENTITY_BODY).setContextEnabled(SnippetContext.ITEM_BODY));
        snippets.add(new Snippet("dlore", "default lore $END$", "Sets a custom item's default lore").setContextEnabled(SnippetContext.ITEM_BODY));
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
            if(snippet.isContextEnabledForTag(tag)) list.add(snippet.createSuggestion());
        }
        return list;
    }
}
