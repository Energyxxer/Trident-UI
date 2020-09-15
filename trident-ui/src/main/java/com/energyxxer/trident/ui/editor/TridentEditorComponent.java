package com.energyxxer.trident.ui.editor;

import com.energyxxer.enxlex.lexical_analysis.LazyLexer;
import com.energyxxer.enxlex.lexical_analysis.summary.SummaryModule;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSection;
import com.energyxxer.enxlex.suggestions.SuggestionModule;
import com.energyxxer.nbtmapper.parser.NBTTMTokens;
import com.energyxxer.trident.compiler.lexer.TridentTokens;
import com.energyxxer.trident.compiler.lexer.syntaxlang.TDNMetaLexerProfile;
import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.global.Preferences;
import com.energyxxer.trident.global.Status;
import com.energyxxer.trident.global.temp.Lang;
import com.energyxxer.trident.global.temp.lang_defaults.presets.JSONLexerProfile;
import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.global.temp.projects.ProjectManager;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.trident.ui.editor.behavior.IndentationManager;
import com.energyxxer.trident.ui.editor.completion.SuggestionDialog;
import com.energyxxer.trident.ui.editor.inspector.Inspector;
import com.energyxxer.util.logger.Debug;
import org.jetbrains.annotations.Nullable;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.function.Function;

/**
 * Created by User on 1/1/2017.
 */
public class TridentEditorComponent extends AdvancedEditor implements KeyListener, CaretListener, ActionListener, FocusListener {

    private static final int MAX_HIGHLIGHTED_TOKENS_PER_LINE = 150;
    private TridentEditorModule parent;

    private StyledDocument sd;

    private Inspector inspector = null;
    private SuggestionDialog suggestionBox = new SuggestionDialog(this);

    private long lastEdit;

    private final Timer timer;
    private Thread highlightingThread = null;

    public static final Preferences.SettingPref<Integer> AUTOREPARSE_DELAY = new Preferences.SettingPref<>("settings.editor.auto_reparse_delay", 500, Integer::parseInt);
    public static final Preferences.SettingPref<Boolean> SHOW_SUGGESTIONS = new Preferences.SettingPref<>("settings.editor.show_suggestions", true, Boolean::parseBoolean);

    TridentEditorComponent(TridentEditorModule parent) {
        this.parent = parent;

        this.setPaddingEnabled(true);
        sd = this.getStyledDocument();

        //if(Lang.getLangForFile(parent.associatedTab.path) != null) this.inspector = new Inspector(this);
        if(parent.file != null && parent.getLanguage() != null && parent.getLanguage().getParserProduction() != null) {
            this.inspector = new Inspector(this);
        }

        this.addCaretListener(this);

        timer = new Timer(20, this);
        timer.start();

        //this.setTransferHandler(TridentWindow.editArea.dragToOpenFileHandler);
        this.setTransferHandler(new TransferHandler("string") {
            @Override
            public boolean importData(TransferSupport support) {
                Debug.log("called importData");
                if(support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    return TridentWindow.editArea.dragToOpenFileHandler.importData(support);
                } else {
                    return getEditorTransferHandler().importData(support);
                }
            }

            @Override
            public boolean canImport(TransferSupport support) {
                return getEditorTransferHandler().canImport(support) || TridentWindow.editArea.dragToOpenFileHandler.canImport(support);
            }

            @Nullable
            @Override
            protected Transferable createTransferable(JComponent c) {
                return TridentEditorComponent.this.createTransferable();
            }

            @Override
            public void exportAsDrag(JComponent comp, InputEvent e, int action) {
                super.exportAsDrag(comp, e, action);
            }

            @Override
            public int getSourceActions(JComponent c) {
                return TransferHandler.COPY_OR_MOVE | getEditorTransferHandler().getSourceActions(c);
            }
        });

        this.setSuggestionInterface(suggestionBox);

        //this.setOpaque(false);
        this.addFocusListener(this);

        Lang lang = parent.getLanguage();
        if(lang != null) {
            lang.onEditorAdd(this, this.getCaret());
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        super.caretUpdate(e);
        displayCaretInfo();
        parent.ensureVisible(getCaret().getDot());
    }

    public int getCaretWordPosition() {
        int index = this.getCaretPosition();
        if(index <= 0) return 0;
        while (true) {
            char c = 0;
            try {
                c = this.getDocument().getText(index-1, 1).charAt(0);
            } catch (BadLocationException x) {
                x.printStackTrace();
            }
            if (!(Character.isJavaIdentifierPart(c) && c != '$') || --index < 1)
                break;
        }
        return index;
    }

    public int getSoftCaretWordPosition() {
        int index = this.getCaretPosition();
        if(index <= 0) return 0;
        while (true) {
            char c = 0;
            try {
                c = this.getDocument().getText(index-1, 1).charAt(0);
            } catch (BadLocationException x) {
                x.printStackTrace();
            }
            if (!((Character.isJavaIdentifierPart(c) && c != '$') || "$#:/.-".contains(c+"")) || --index < 1)
                break;
        }
        return index;
    }

    private Status errorStatus = new Status();

    private void startSyntaxHighlighting() {
        if(parent.syntax == null) return;

        try {
            String text = getText();

            Lang lang = parent.getLanguage();
            if (lang == null) return;
            Project project = parent.file != null ? ProjectManager.getAssociatedProject(parent.file) : null;

            SuggestionModule suggestionModule = (SHOW_SUGGESTIONS.get() && project != null && lang.usesSuggestionModule()) ? new SuggestionModule(this.getCaretWordPosition(), this.getCaretPosition()) : null;
            SummaryModule summaryModule = project != null ? lang.createSummaryModule() : null;

            File file = parent.getFileForAnalyzer();
            Lang.LangAnalysisResponse analysis = file != null ? lang.analyze(file, text, suggestionModule, summaryModule) : null;
            if (analysis == null) return;

            SwingUtilities.invokeLater(() -> performTokenStyling(analysis, project, lang));
        } catch(Exception x) {
            x.printStackTrace();
            TridentWindow.showException(x);
        }
    }

    private void performTokenStyling(Lang.LangAnalysisResponse analysis, Project project, Lang lang) {
        try {
            Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

            if(analysis.response != null) suggestionBox.setSummary(analysis.lexer.getSummaryModule(), analysis.response.matched);
            if(analysis.lexer.getSuggestionModule() != null) {
                if(project != null && analysis.lexer.getSummaryModule() != null) {
                    lang.joinToProjectSummary(analysis.lexer.getSummaryModule(), parent.file, project);
                }
                suggestionBox.showSuggestions(analysis.lexer.getSuggestionModule());
            }

            Token prevToken = null;
            ArrayList<String> previousTokenStyles = new ArrayList<>();

            if(this.inspector != null) {
                this.inspector.inspect(analysis.lexer.getStream());
                this.inspector.insertNotices(analysis.notices);
            }

            if(analysis.response != null && !analysis.response.matched) {
                errorStatus.setMessage(analysis.response.getErrorMessage() + (analysis.response.faultyToken != null ? ". (line " + analysis.response.faultyToken.loc.line + " column " + analysis.response.faultyToken.loc.column + ")" : ""));
                TridentWindow.setStatus(errorStatus);
                if(analysis.response.faultyToken != null && analysis.response.faultyToken.value != null && analysis.response.faultyToken.loc != null) sd.setCharacterAttributes(analysis.response.faultyToken.loc.index, analysis.response.faultyToken.value.length(), TridentEditorComponent.this.getStyle("error"), true);
                if(analysis.lexer instanceof LazyLexer) return;
            }

            int tokensInLine = 0;

            for(Token token : analysis.lexer.getStream().tokens) {
                boolean shouldPaintStyles = true;
                if(prevToken != null && prevToken.loc.line != token.loc.line) tokensInLine = 0;
                tokensInLine++;
                if(tokensInLine > MAX_HIGHLIGHTED_TOKENS_PER_LINE) {
                    shouldPaintStyles = false;
                }
                Style style = TridentEditorComponent.this.getStyle(token.type.toString().toLowerCase(Locale.ENGLISH));

                int previousTokenStylesIndex = previousTokenStyles.size();

                int styleStart = token.loc.index;

                if(shouldPaintStyles) {
                    if(style != null)
                        sd.setCharacterAttributes(token.loc.index, token.value.length(), style, true);
                    else
                        sd.setCharacterAttributes(token.loc.index, token.value.length(), defaultStyle, true);

                    for(Map.Entry<String, Object> entry : token.attributes.entrySet()) {
                        if(!entry.getValue().equals(true)) continue;
                        Style attrStyle = TridentEditorComponent.this.getStyle("~" + entry.getKey().toLowerCase(Locale.ENGLISH));
                        if(attrStyle == null) continue;

                        if(prevToken != null && previousTokenStyles.contains(entry.getKey().toLowerCase(Locale.ENGLISH))) {
                            styleStart = prevToken.loc.index + prevToken.value.length();
                        }
                        previousTokenStyles.add(entry.getKey().toLowerCase(Locale.ENGLISH));

                        sd.setCharacterAttributes(styleStart, token.value.length() + (token.loc.index - styleStart), attrStyle, false);
                    }
                    for(Map.Entry<TokenSection, String> entry : token.subSections.entrySet()) {
                        TokenSection section = entry.getKey();
                        Style attrStyle = TridentEditorComponent.this.getStyle("~" + entry.getValue().toLowerCase(Locale.ENGLISH));
                        if(attrStyle == null) continue;

                        sd.setCharacterAttributes(token.loc.index + section.start, section.length, attrStyle, false);
                    }
                    for(String tag : token.tags) {
                        Style attrStyle = TridentEditorComponent.this.getStyle("$" + tag.toLowerCase(Locale.ENGLISH));
                        if(attrStyle == null) continue;

                        if(prevToken != null && previousTokenStyles.contains(tag.toLowerCase(Locale.ENGLISH))) {
                            styleStart = prevToken.loc.index + prevToken.value.length();
                        }
                        previousTokenStyles.add(tag.toLowerCase(Locale.ENGLISH));

                        sd.setCharacterAttributes(styleStart, token.value.length() + (token.loc.index - styleStart), attrStyle, false);
                    }

                    if(analysis.response != null) {
                        for (Map.Entry<String, String[]> entry : Collections.synchronizedSet(parent.parserStyles.entrySet())) {
                            String[] tagList = entry.getValue();
                            int startIndex = -1;
                            tgs:
                            do {
                                startIndex = indexOf(token.tags, tagList[0], startIndex + 1);
                                if (startIndex < 0) break;
                                for (int i = 0; i < tagList.length; i++) {
                                    if (startIndex + i >= token.tags.size() || !tagList[i].equalsIgnoreCase(token.tags.get(startIndex + i)))
                                        continue tgs;
                                }
                                Style attrStyle = TridentEditorComponent.this.getStyle(entry.getKey());
                                if (attrStyle == null) continue;
                                if (prevToken != null && previousTokenStyles.contains(entry.getKey())) {
                                    styleStart = prevToken.loc.index + prevToken.value.length();
                                }
                                previousTokenStyles.add(entry.getKey());
                                sd.setCharacterAttributes(styleStart, token.value.length() + (token.loc.index - styleStart), attrStyle, false);
                            } while (true);
                        }
                    }
                }
                while(previousTokenStylesIndex > 0) {
                    previousTokenStyles.remove(0);
                    previousTokenStylesIndex--;
                }

                if(getIndentationManager().getBraceMatcher().matcher(token.value).find() && !lang.isBraceToken(token)) {
                    sd.setCharacterAttributes(token.loc.index, token.value.length(), getStyle(IndentationManager.NULLIFY_BRACE_STYLE), false);
                }

                if(token.type == TridentTokens.STRING_LITERAL || token.type == JSONLexerProfile.STRING_LITERAL || token.type == NBTTMTokens.STRING_LITERAL || token.type == TDNMetaLexerProfile.STRING_LITERAL) {
                    sd.setCharacterAttributes(token.loc.index, token.value.length(), getStyle(AdvancedEditor.STRING_STYLE), false);

                    for(TokenSection section : token.subSections.keySet()) {
                        sd.setCharacterAttributes(token.loc.index + section.start, section.length, getStyle(AdvancedEditor.STRING_ESCAPE_STYLE), false);
                    }
                }


                prevToken = token;
            }
            previousTokenStyles.clear();

            if(analysis.response == null || analysis.response.matched) TridentWindow.dismissStatus(errorStatus);

        } catch(Exception x) {
            x.printStackTrace();
            TridentWindow.showException(x);
        }
    }

    private static <T> int indexOf(ArrayList<T> arr, T value, int fromIndex) {
        for(int i = fromIndex; i < arr.size(); i++) {
            if(Objects.equals(arr.get(i), value)) return i;
        }
        return -1;
    }

    void highlight() {
        parent.highlightTime = System.currentTimeMillis();
        lastEdit = new Date().getTime();
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (lastEdit > -1 && (new Date().getTime()) - lastEdit > AUTOREPARSE_DELAY.get() && (parent.associatedTab == null || parent.associatedTab.isActive())) {
            lastEdit = -1;
            if(highlightingThread != null) {
                highlightingThread.stop();
            }
            highlightingThread = new Thread(new SwingWorker<Object, Object>() {
                @Override
                protected Object doInBackground() {
                    startSyntaxHighlighting();
                    return null;
                }
            },"Text Highlighter");
            highlightingThread.setUncaughtExceptionHandler((t, e) -> {
                if(e instanceof ThreadDeath) return;
                e.printStackTrace();
                if(e instanceof Exception) {
                    TridentWindow.showException((Exception) e);
                } else {
                    TridentWindow.showException(e.getMessage());
                }
            });
            //highlightingThread = new Thread(this::startSyntaxHighlighting,"Text Highlighter");
            highlightingThread.start();

            Project project = ProjectManager.getAssociatedProject(parent.file);
            if(project != null && project.getSummary() == null) {
                Commons.index(project);
            }
        }
    }

    @Override
    public void registerCharacterDrift(Function<Integer, Integer> h) {
        super.registerCharacterDrift(h);

        if(this.inspector != null) this.inspector.registerCharacterDrift(h);
    }

    @Override
    public void repaint() {
        if(this.getParent() instanceof JViewport && this.getParent().getParent() instanceof JScrollPane) {
            this.getParent().getParent().repaint();
        } else super.repaint();
    }

    void displayCaretInfo() {
        TridentWindow.statusBar.setCaretInfo(getCaretInfo());
        TridentWindow.statusBar.setSelectionInfo(getSelectionInfo());
    }

    @Override
    public String getText() {
        try {
            return getDocument().getText(0, getDocument().getLength());
        } catch (BadLocationException x) {
            x.printStackTrace();
        }
        return null;
    }

    @Override
    public void dispose() {
        super.dispose();
        timer.stop();
        suggestionBox.dispose();
    }

    @Override
    public void focusGained(FocusEvent e) {
        TridentWindow.projectExplorer.clearSelected();
    }

    @Override
    public void focusLost(FocusEvent e) {

    }

    public TridentEditorModule getParentModule() {
        return parent;
    }
}
