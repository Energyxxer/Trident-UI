package com.energyxxer.trident.ui.editor.completion;

import com.energyxxer.enxlex.suggestions.ComplexSuggestion;
import com.energyxxer.enxlex.suggestions.LiteralSuggestion;
import com.energyxxer.enxlex.suggestions.Suggestion;
import com.energyxxer.enxlex.suggestions.SuggestionModule;
import com.energyxxer.trident.global.temp.Lang;
import com.energyxxer.util.logger.Debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class SuggestionExpander {
    private static final HashSet<String> missingCaseKeys = new HashSet<>();

    public static Collection<SuggestionToken> expand(Suggestion suggestion, SuggestionDialog dialog, SuggestionModule suggestionModule) {
        if(suggestion instanceof LiteralSuggestion) {
            return Collections.singletonList(new ExpandableSuggestionToken(dialog, ((LiteralSuggestion) suggestion).getPreview(), ((LiteralSuggestion) suggestion).getLiteral(), suggestion));
        } else if(suggestion instanceof ComplexSuggestion) {
            ArrayList<SuggestionToken> tokens = new ArrayList<>();
            Lang lang = Lang.getLangForFile(dialog.getEditor().getParentModule().getFileForAnalyzer().getPath());
            if(lang != null) {
                boolean expanded = lang.expandSuggestion((ComplexSuggestion) suggestion, tokens, dialog, suggestionModule);

                if(!expanded) {
                    if(((ComplexSuggestion) suggestion).getKey().startsWith("cspn:")) {
                        tokens.add(new ParameterNameSuggestionToken(((ComplexSuggestion) suggestion).getKey().substring("cspn:".length())));
                    } else if(missingCaseKeys.add(((ComplexSuggestion) suggestion).getKey())) {
                        Debug.log("Missing SuggestionExpander case for: " + ((ComplexSuggestion) suggestion).getKey());
                    }
                }
            }

            return tokens;
        }
        throw new IllegalArgumentException();
    }
}
