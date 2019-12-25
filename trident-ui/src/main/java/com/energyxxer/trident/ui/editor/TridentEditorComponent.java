package com.energyxxer.trident.ui.editor;

import com.energyxxer.crossbow.compiler.lexer.summaries.CrossbowSummaryModule;
import com.energyxxer.crossbow.compiler.util.CrossbowProjectSummary;
import com.energyxxer.enxlex.lexical_analysis.LazyLexer;
import com.energyxxer.enxlex.lexical_analysis.summary.SummaryModule;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSection;
import com.energyxxer.enxlex.suggestions.SuggestionModule;
import com.energyxxer.trident.compiler.lexer.summaries.TridentSummaryModule;
import com.energyxxer.trident.compiler.util.TridentProjectSummary;
import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.global.Preferences;
import com.energyxxer.trident.global.Status;
import com.energyxxer.trident.global.temp.Lang;
import com.energyxxer.trident.global.temp.projects.CrossbowProject;
import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.global.temp.projects.ProjectManager;
import com.energyxxer.trident.global.temp.projects.TridentProject;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.trident.ui.editor.completion.SuggestionDialog;
import com.energyxxer.trident.ui.editor.inspector.Inspector;
import com.energyxxer.util.logger.Debug;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by User on 1/1/2017.
 */
public class TridentEditorComponent extends AdvancedEditor implements KeyListener, CaretListener, ActionListener, FocusListener {

    private TridentEditorModule parent;

    private StyledDocument sd;

    private Inspector inspector = null;
    private SuggestionDialog suggestionBox = new SuggestionDialog(this);

    private long lastEdit;

    private final Timer timer;
    private Thread highlightingThread = null;

    public static final Preferences.SettingPref<Integer> AUTOREPARSE_DELAY = new Preferences.SettingPref<>("settings.editor.auto_reparse_delay", 500, Integer::new);
    public static final Preferences.SettingPref<Boolean> SHOW_SUGGESTIONS = new Preferences.SettingPref<>("settings.editor.show_suggestions", true, Boolean::new);

    TridentEditorComponent(TridentEditorModule parent) {
        this.parent = parent;

        this.setPaddingEnabled(true);
        sd = this.getStyledDocument();

        //if(Lang.getLangForFile(parent.associatedTab.path) != null) this.inspector = new Inspector(this);
        if(parent.file != null && parent.getLanguage() == Lang.TRIDENT) {
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
            char c = this.getFoldableDocument().getModelText(index-1, 1).charAt(0);
            if (!(Character.isJavaIdentifierPart(c) && c != '$') || --index <= 1)
                break;
        }
        return index;
    }

    public int getSoftCaretWordPosition() {
        int index = this.getCaretPosition();
        if(index <= 0) return 0;
        while (true) {
            char c = this.getFoldableDocument().getModelText(index-1, 1).charAt(0);
            if (!((Character.isJavaIdentifierPart(c) && c != '$') || "#:/.".contains(c+"")) || --index <= 1)
                break;
        }
        return index;
    }

    private Status errorStatus = new Status();

    private void highlightSyntax() {
        if(parent.syntax == null) return;

        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        String text = getText();
        //Debug.log(text);

        Lang lang = parent.getLanguage();
        if(lang == null) return;
        Project project = parent.file != null ? ProjectManager.getAssociatedProject(parent.file) : null;

        SuggestionModule suggestionModule = SHOW_SUGGESTIONS.get() ? ((lang == Lang.TRIDENT || lang == Lang.CROSSBOW) && project != null ? new SuggestionModule(this.getCaretWordPosition(), this.getCaretPosition()) : null) : null;
        SummaryModule summaryModule = project != null ? (lang == Lang.TRIDENT ? new TridentSummaryModule() : (lang == Lang.CROSSBOW ? new CrossbowSummaryModule() : null)) : null;

        File file = parent.getFileForAnalyzer();
        Lang.LangAnalysisResponse analysis = file != null ? lang.analyze(file, text, suggestionModule, summaryModule) : null;
        if(analysis == null) return;

        suggestionBox.setSummary(analysis.lexer.getSummaryModule());
        if(analysis.lexer.getSuggestionModule() != null) {
            if(project != null) {
                if(project instanceof TridentProject && analysis.lexer.getSummaryModule() instanceof TridentSummaryModule) {
                    ((TridentSummaryModule) analysis.lexer.getSummaryModule()).setParentSummary((TridentProjectSummary) project.getSummary());
                    if(project.getSummary() != null) {
                        ((TridentSummaryModule) analysis.lexer.getSummaryModule()).setFileLocation(((TridentProject)project).getSummary().getLocationForFile(parent.file));
                    }
                } else if(project instanceof CrossbowProject && analysis.lexer.getSummaryModule() instanceof CrossbowSummaryModule) {
                    ((CrossbowSummaryModule) analysis.lexer.getSummaryModule()).setParentSummary((CrossbowProjectSummary) project.getSummary());
                    if(project.getSummary() != null) {
                        ((CrossbowSummaryModule) analysis.lexer.getSummaryModule()).setFileLocation(((CrossbowProject)project).getSummary().getLocationForFile(parent.file));
                    }
                }
            }
            suggestionBox.showSuggestions(analysis.lexer.getSuggestionModule());
        }

        Token prevToken = null;
        ArrayList<String> previousTokenStyles = new ArrayList<>();

        if(analysis.response != null && !analysis.response.matched) {
            errorStatus.setMessage(analysis.response.getErrorMessage() + (analysis.response.faultyToken != null ? ". (line " + analysis.response.faultyToken.loc.line + " column " + analysis.response.faultyToken.loc.column + ")" : ""));
            TridentWindow.setStatus(errorStatus);
            if(analysis.response.faultyToken != null && analysis.response.faultyToken.value != null && analysis.response.faultyToken.loc != null) sd.setCharacterAttributes(analysis.response.faultyToken.loc.index, analysis.response.faultyToken.value.length(), TridentEditorComponent.this.getStyle("error"), true);
            if(analysis.lexer instanceof LazyLexer) return;
        }

        for(Token token : analysis.lexer.getStream().tokens) {
            Style style = TridentEditorComponent.this.getStyle(token.type.toString().toLowerCase());

            int previousTokenStylesIndex = previousTokenStyles.size();

            int styleStart = token.loc.index;

            sd.setCharacterAttributes(token.loc.index, token.value.length(), defaultStyle, true);
            if(style != null)
                sd.setCharacterAttributes(token.loc.index, token.value.length(), style, false);

            for(Map.Entry<String, Object> entry : token.attributes.entrySet()) {
                if(!entry.getValue().equals(true)) continue;
                Style attrStyle = TridentEditorComponent.this.getStyle("~" + entry.getKey().toLowerCase());
                if(attrStyle == null) continue;

                if(prevToken != null && previousTokenStyles.contains(entry.getKey().toLowerCase())) {
                    styleStart = prevToken.loc.index + prevToken.value.length();
                }
                previousTokenStyles.add(entry.getKey().toLowerCase());

                sd.setCharacterAttributes(styleStart, token.value.length() + (token.loc.index - styleStart), attrStyle, false);
            }
            for(Map.Entry<TokenSection, String> entry : token.subSections.entrySet()) {
                TokenSection section = entry.getKey();
                Style attrStyle = TridentEditorComponent.this.getStyle("~" + entry.getValue().toLowerCase());
                if(attrStyle == null) continue;

                sd.setCharacterAttributes(token.loc.index + section.start, section.length, attrStyle, false);
            }
            for(String tag : token.tags) {
                Style attrStyle = TridentEditorComponent.this.getStyle("$" + tag.toLowerCase());
                if(attrStyle == null) continue;

                if(prevToken != null && previousTokenStyles.contains(tag.toLowerCase())) {
                    styleStart = prevToken.loc.index + prevToken.value.length();
                }
                previousTokenStyles.add(tag.toLowerCase());

                sd.setCharacterAttributes(styleStart, token.value.length() + (token.loc.index - styleStart), attrStyle, false);
            }

            if(analysis.response != null) {
                ps: for(Map.Entry<String, String[]> entry : parent.parserStyles.entrySet()) {
                    String[] tagList = entry.getValue();
                    int startIndex = token.tags.indexOf(tagList[0]);
                    if(startIndex < 0) continue;
                    for(int i = 0; i < tagList.length; i++) {
                        if(startIndex+i >= token.tags.size() || !tagList[i].equalsIgnoreCase(token.tags.get(startIndex+i))) continue ps;
                    }
                    Style attrStyle = TridentEditorComponent.this.getStyle(entry.getKey());
                    if(attrStyle == null) continue;
                    if(prevToken != null && previousTokenStyles.contains(entry.getKey())) {
                        styleStart = prevToken.loc.index + prevToken.value.length();
                    }
                    previousTokenStyles.add(entry.getKey());
                    sd.setCharacterAttributes(styleStart, token.value.length() + (token.loc.index - styleStart), attrStyle, false);
                }
            }
            while(previousTokenStylesIndex > 0) {
                previousTokenStyles.remove(0);
                previousTokenStylesIndex--;
            }
            prevToken = token;
        }
        previousTokenStyles.clear();

        if(this.inspector != null) {
            this.inspector.inspect(analysis.lexer.getStream());
            this.inspector.insertNotices(analysis.notices);
        }


        TridentWindow.dismissStatus(errorStatus);

        //sd.setCharacterAttributes(81, 119, getStyle("do_not_display"), true);
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
            highlightingThread = new Thread(this::highlightSyntax,"Text Highlighter");
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
        return getFoldableDocument().getUnfoldedText();
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
}
