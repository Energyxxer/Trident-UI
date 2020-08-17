package com.energyxxer.trident.global.temp;

import com.energyxxer.commodore.standard.StandardDefinitionPacks;
import com.energyxxer.enxlex.lexical_analysis.EagerLexer;
import com.energyxxer.enxlex.lexical_analysis.LazyLexer;
import com.energyxxer.enxlex.lexical_analysis.Lexer;
import com.energyxxer.enxlex.lexical_analysis.profiles.LexerProfile;
import com.energyxxer.enxlex.lexical_analysis.summary.SummaryModule;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenStream;
import com.energyxxer.enxlex.pattern_matching.TokenMatchResponse;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.enxlex.report.NoticeType;
import com.energyxxer.enxlex.suggestions.ComplexSuggestion;
import com.energyxxer.enxlex.suggestions.SuggestionModule;
import com.energyxxer.nbtmapper.parser.NBTTMLexerProfile;
import com.energyxxer.nbtmapper.parser.NBTTMProductions;
import com.energyxxer.trident.compiler.TridentCompiler;
import com.energyxxer.trident.compiler.lexer.syntaxlang.TDNMetaLexerProfile;
import com.energyxxer.trident.compiler.lexer.syntaxlang.TDNMetaProductions;
import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.global.temp.lang_defaults.parsing.MCFunctionProductions;
import com.energyxxer.trident.global.temp.lang_defaults.presets.JSONLexerProfile;
import com.energyxxer.trident.global.temp.lang_defaults.presets.MCFunctionLexerProfile;
import com.energyxxer.trident.global.temp.lang_defaults.presets.PropertiesLexerProfile;
import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.ui.dialogs.settings.SnippetLexerProfile;
import com.energyxxer.trident.ui.editor.completion.SuggestionDialog;
import com.energyxxer.trident.ui.editor.completion.SuggestionToken;
import com.energyxxer.util.Factory;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;

/**
 * Created by User on 2/9/2017.
 */
public class Lang {
    private static final ArrayList<Lang> registeredLanguages = new ArrayList<>();

    public static final Lang JSON = new Lang("JSON",
            false,
            JSONLexerProfile::new,
            "json", "mcmeta", TridentCompiler.PROJECT_FILE_NAME.substring(1), TridentCompiler.PROJECT_BUILD_FILE_NAME.substring(1)
    ) {
        {
            setIconName("json");
        }
    };
    public static final Lang PROPERTIES = new Lang("PROPERTIES",
            false,
            PropertiesLexerProfile::new,
            "properties", "lang"
    ) {{this.putProperty("line_comment_marker","#");}};
    public static final Lang MCFUNCTION = new Lang("MCFUNCTION",
            true,
            MCFunctionLexerProfile::new,
            () -> MCFunctionProductions.FILE,
            "mcfunction"
    ) {
        {
            this.putProperty("line_comment_marker","#");
            this.setIconName("function");
        }
    };
    //public static final Lang TRIDENT = TridentLang.INSTANCE;
    public static final Lang TRIDENT_META = new Lang("TRIDENT_META",
            false,
            TDNMetaLexerProfile::new,
            () -> TDNMetaProductions.FILE,
            "tdnmeta"
    ) {{this.putProperty("line_comment_marker","//");}};
    public static final Lang NBTTM = new Lang("NBTTM",
            true,
            () -> new NBTTMLexerProfile(StandardDefinitionPacks.MINECRAFT_JAVA_LATEST_SNAPSHOT),
            () -> NBTTMProductions.FILE,
            "nbttm"
    ) {{this.putProperty("line_comment_marker","#");}};
    public static final Lang SNIPPET = new Lang("SNIPPET",
            false,
            SnippetLexerProfile::new);

    private final String name;
    private final boolean lazy;
    private final Factory<LexerProfile> lexerProfileFactory;
    private final Factory<TokenPatternMatch> parserProduction;
    private final List<String> extensions;
    private final HashMap<String, String> properties = new HashMap<>();
    private String iconName;

    public Lang(String name, boolean lazy, Factory<LexerProfile> lexerProfileFactory, String... extensions) {
        this(name, lazy, lexerProfileFactory, null, extensions);
    }

    public Lang(String name, boolean lazy, Factory<LexerProfile> lexerProfileFactory, Factory<TokenPatternMatch> parserProduction, String... extensions) {
        this.name = name;
        this.lazy = lazy;
        this.lexerProfileFactory = lexerProfileFactory;
        this.parserProduction = parserProduction;
        this.extensions = new ArrayList<>();
        this.extensions.addAll(Arrays.asList(extensions));

        registeredLanguages.add(this);
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public void addExtension(String extension) {
        extensions.add(extension);
    }

    public LexerProfile createProfile() {
        return lexerProfileFactory.createInstance();
    }

    public Factory<TokenPatternMatch> getParserProduction() {
        return parserProduction;
    }

    public static Lang getLangForFile(String path) {
        if(path == null) return null;
        for(Lang lang : Lang.values()) {
            for(String extension : lang.extensions) {
                if(path.endsWith("." + extension)) {
                    return lang;
                }
            }
        }
        return null;
    }

    public String getIconName() {
        return iconName;
    }

    protected void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public LangAnalysisResponse analyze(File file, String text, SuggestionModule suggestionModule, SummaryModule summaryModule) {
        TokenPatternMatch patternMatch = (parserProduction != null) ? parserProduction.createInstance() : null;

        Lexer lexer = this.lazy ? new LazyLexer(new TokenStream(true), patternMatch) : new EagerLexer(new TokenStream(true));
        TokenMatchResponse response = null;
        ArrayList<Notice> notices = new ArrayList<>();
        ArrayList<Token> tokens;


        lexer.setSummaryModule(summaryModule);
        if(suggestionModule != null) {
            lexer.setSuggestionModule(suggestionModule);
        }
        lexer.start(file, text, createProfile());
        notices.addAll(lexer.getNotices());

        tokens = new ArrayList<>(lexer.getStream().tokens);
        tokens.remove(0);

        if(lexer instanceof LazyLexer) {
            response = ((LazyLexer) lexer).getMatchResponse();
        } else {
            tokens.removeIf(token -> !token.type.isSignificant());

            if(patternMatch != null) {
                response = patternMatch.match(0, lexer);

                if(response != null && !response.matched) {
                    notices.add(new Notice(NoticeType.ERROR, response.getErrorMessage(), response.faultyToken));
                }
            }
        }

        return new LangAnalysisResponse(lexer, response, lexer.getStream().tokens, notices);
    }

    @Override
    public String toString() {
        return name;
    }

    public static Collection<Lang> values() {
        return registeredLanguages;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void putProperty(String key, String value) {
        properties.put(key, value);
    }

    public boolean usesSuggestionModule() {
        return false;
    }

    public SummaryModule createSummaryModule() {
        return null;
    }

    public void joinToProjectSummary(SummaryModule summaryModule, File file, Project project) {
        throw new UnsupportedOperationException();
    }

    public boolean expandSuggestion(ComplexSuggestion suggestion, ArrayList<SuggestionToken> tokens, SuggestionDialog dialog, SuggestionModule suggestionModule) {
        return false;
    }

    public Image getIconForFile(File file) {
        return (iconName != null) ? Commons.getIcon(iconName) : null;
    }

    public static class LangAnalysisResponse {
        public Lexer lexer;
        public TokenMatchResponse response;
        public ArrayList<Token> tokens;
        public ArrayList<Notice> notices;

        public LangAnalysisResponse(Lexer lexer, TokenMatchResponse response, ArrayList<Token> tokens, ArrayList<Notice> notices) {
            this.lexer = lexer;
            this.response = response;
            this.tokens = tokens;
            this.notices = notices;
        }
    }
}