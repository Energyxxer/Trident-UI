package com.energyxxer.trident.ui.dialogs.settings;

import com.energyxxer.enxlex.lexical_analysis.profiles.LexerContext;
import com.energyxxer.enxlex.lexical_analysis.profiles.LexerProfile;
import com.energyxxer.enxlex.lexical_analysis.profiles.ScannerContextResponse;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class SnippetLexerProfile extends LexerProfile {

    private static final TokenType END_MARKER = new TokenType("END_MARKER"); // $END$

    public SnippetLexerProfile() {
        //$END$
        LexerContext endContext = new LexerContext() {

            @Override
            public ScannerContextResponse analyze(String str, LexerProfile profile) {
                if(str.startsWith("$END$")) return new ScannerContextResponse(true, "$END$", END_MARKER);
                else return new ScannerContextResponse(false);
            }

            @Override
            public Collection<TokenType> getHandledTypes() {
                return Collections.singletonList(END_MARKER);
            }
        };
        this.contexts = new ArrayList<>();
        contexts.add(endContext);
    }

    @Override
    public boolean canMerge(char ch0, char ch1) {
        return Character.isJavaIdentifierPart(ch0) && Character.isJavaIdentifierPart(ch1);
    }

    @Override
    public void putHeaderInfo(Token header) {
        header.attributes.put("TYPE","snippet");
        header.attributes.put("DESC","Trident UI Snippet Preview");
    }
}
