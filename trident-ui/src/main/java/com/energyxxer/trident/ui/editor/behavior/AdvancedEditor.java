package com.energyxxer.trident.ui.editor.behavior;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.global.Preferences;
import com.energyxxer.trident.global.keystrokes.KeyMap;
import com.energyxxer.trident.global.keystrokes.UserKeyBind;
import com.energyxxer.trident.main.window.sections.tools.ConsoleBoard;
import com.energyxxer.trident.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.trident.ui.editor.behavior.caret.Dot;
import com.energyxxer.trident.ui.editor.behavior.caret.EditorCaret;
import com.energyxxer.trident.ui.editor.behavior.editmanager.EditManager;
import com.energyxxer.trident.ui.editor.behavior.editmanager.edits.*;
import com.energyxxer.trident.ui.editor.completion.SuggestionInterface;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.trident.util.linepainter.LinePainter;
import com.energyxxer.util.Disposable;
import com.energyxxer.util.StringLocation;
import com.energyxxer.util.StringLocationCache;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.xswing.ScalableDimension;
import com.energyxxer.xswing.UnifiedDocumentListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by User on 1/5/2017.
 */
public class AdvancedEditor extends JTextPane implements KeyListener, CaretListener, FocusListener, Disposable {

    public static final String STRING_STYLE = "__STRING_STYLE";
    public static final String STRING_ESCAPE_STYLE = "__STRING_ESCAPE_STYLE";
    private static final float BIAS_POINT = 0.4f;

    private static final String WORD_DELIMITERS = "./\\()\"'-:,.;<>~!@#$%^&*|+=[]{}`~?";

    private final TransferHandler editorTransferHandler;

    private boolean enabled = true;

    public ThemeListenerManager tlm = new ThemeListenerManager();

    private EditorCaret caret;

    private EditManager editManager = new EditManager(this);
    private LinePainter linePainter;

    private final StringLocationCache viewLineCache = new StringLocationCache();
    private final IndentationManager indentationManager;

    private SuggestionInterface suggestionInterface;

    private int lineHeight = 17;

    private static UserKeyBind FOLD;
    private static UserKeyBind UNFOLD;

    static {
        if(Preferences.get("debug","false").equals("true")) {
            UNFOLD = KeyMap.requestMapping("test.unfold", KeyMap.identifierToStrokes("")).setName("Unfold").setGroupName("Testing - please do not use");
            FOLD = KeyMap.requestMapping("test.fold", KeyMap.identifierToStrokes("")).setName("Fold").setGroupName("Testing - please do not use");
        }
    }
    private Color selectionUnfocusedColor;
    private Color braceHighlightColor;

    public AdvancedEditor() {
        this.getStyledDocument().addStyle(STRING_STYLE, null);
        this.getStyledDocument().addStyle(STRING_ESCAPE_STYLE, null);

        linePainter = new LinePainter(this);
        this.setCaret(this.caret = new EditorCaret(this));
        this.addKeyListener(this);
        this.addFocusListener(this);

        indentationManager = new IndentationManager(this);

        this.getDocument().addDocumentListener((UnifiedDocumentListener) e -> {
            if(e.getType() == DocumentEvent.EventType.CHANGE) return;
            try {
                String text = getDocument().getText(0, getDocument().getLength());
                viewLineCache.textChanged(text, e.getOffset());
                indentationManager.textChanged(text);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }

            updateDefaultSize();
        });

        /*this.getDocument().addUndoableEditListener(e -> {
            if (e.getEdit().getPresentationName().equals("style change")) return;
            viewLineCache.setText(this.getText());
            updateDefaultSize();
        });*/

        this.setTransferHandler(this.editorTransferHandler = new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor) || support.isDataFlavorSupported(DataFlavor.getTextPlainUnicodeFlavor());
            }

            @Override
            public boolean importData(TransferSupport support) {
                Debug.log("Hey does this ever run?");
                return support.isDataFlavorSupported(DataFlavor.stringFlavor) || support.isDataFlavorSupported(DataFlavor.getTextPlainUnicodeFlavor());
            }

            @NotNull
            @Override
            protected Transferable createTransferable(JComponent c) {
                return AdvancedEditor.this.createTransferable();
            }

            @Override
            public int getSourceActions(JComponent c) {
                return TransferHandler.COPY_OR_MOVE;
            }
        });

        tlm.addThemeChangeListener(t -> {
            this.setBackground(t.getColor(Color.WHITE, "Editor.background"));
            this.setBackground(this.getBackground());
            this.setForeground(t.getColor(Color.BLACK, "Editor.foreground","General.foreground"));
            this.setCaretColor(this.getForeground());
            this.setSelectionColor(t.getColor(new Color(50, 100, 175), "Editor.selection.background"));
            this.setSelectionUnfocusedColor(t.getColor(new Color(50, 100, 175), "Editor.selection.unfocused.background"));
            this.setCurrentLineColor(t.getColor(new Color(235, 235, 235), "Editor.currentLine.background"));
            this.setFont(new Font(t.getString("Editor.font","default:monospaced"), Font.PLAIN, Preferences.getModifiedEditorFontSize()));
            this.setBraceHighlightColor(t.getColor(Color.YELLOW, "Editor.braceHighlight.background"));
        });
    }

    public void setSelectedLineEnabled(boolean enabled) {
        linePainter.setEnabled(enabled);
    }

    public void setPaddingEnabled(boolean paddingEnabled) {
        if(paddingEnabled) {
            this.setMargin(new Insets(0, 5, 100, 100));
        } else {
            this.setMargin(new Insets(0, 0, 0, 0));
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {
        if(!enabled) return;
        if(e.isConsumed()) return;
        e.consume();
        if(!isPlatformControlDown(e) && !Commons.isSpecialCharacter(e.getKeyChar())) {
            editManager.insertEdit(new InsertionEdit("" + e.getKeyChar(), this));
            if(suggestionInterface != null) {
                suggestionInterface.setSafeToSuggest(true);
                suggestionInterface.lock();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(!enabled) return;
        if(e.isConsumed()) return;
        int keyCode = e.getKeyCode();
        if(KeyMap.UNDO.wasPerformedExact(e)) {
            editManager.undo();
            e.consume();
        } else if(KeyMap.REDO.wasPerformedExact(e)) {
            editManager.redo();
            e.consume();
        } else if(KeyMap.COMMENT.wasPerformedExact(e)) {
            e.consume();
            editManager.insertEdit(new CommentEdit(this));
        } else if(KeyMap.TEXT_DELETE_LINE.wasPerformedExact(e)) {
            e.consume();
            editManager.insertEdit(new LineDeletionEdit(this));
        } else if(KeyMap.TEXT_DUPLICATE_LINE.wasPerformedExact(e)) {
            e.consume();
            editManager.insertEdit(new LineDuplicationEdit(this));
        } else if(keyCode == KeyEvent.VK_TAB) {
            e.consume();

            CaretProfile profile = caret.getProfile();
            if(profile.getSelectedCharCount() == 0 && !e.isShiftDown()) {
                editManager.insertEdit(new TabInsertionEdit(this));
            } else {
                editManager.insertEdit(new IndentEdit(this, e.isShiftDown()));
            }
        } else if(keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
            e.consume();
            editManager.insertEdit(new DeletionEdit(this, isPlatformControlDown(e), keyCode == KeyEvent.VK_DELETE));
            if(suggestionInterface != null) {
                suggestionInterface.lock();
            }
        } else if(keyCode == KeyEvent.VK_ENTER) {
            e.consume();
            editManager.insertEdit(new NewlineEdit(this, !isPlatformControlDown(e)));
        } else if(KeyMap.COPY.wasPerformedExact(e) || KeyMap.CUT.wasPerformedExact(e)) {
            e.consume();
            this.copyOrCut(KeyMap.CUT.wasPerformedExact(e));
        } else if(KeyMap.PASTE.wasPerformed(e)) {
            e.consume();
            this.paste();
        } else if(KeyMap.TEXT_SELECT_ALL.wasPerformedExact(e)) {
            e.consume();
            caret.setProfile(new CaretProfile(0, getDocument().getLength()));
        } else if(KeyMap.TEXT_MOVE_LINE_UP.wasPerformedExact(e)) {
            e.consume();
            editManager.insertEdit(new LineMoveEdit(this, Dot.UP));
        } else if(KeyMap.TEXT_MOVE_LINE_DOWN.wasPerformedExact(e)) {
            e.consume();
            editManager.insertEdit(new LineMoveEdit(this, Dot.DOWN));
        } else if(keyCode == KeyEvent.VK_ESCAPE) {
            caret.deselect();
        }

        if(e.isConsumed() && suggestionInterface != null) {
            suggestionInterface.setSafeToSuggest(false);
        }
    }

    public EditManager getEditManager() {
        return editManager;
    }

    public StringLocation getLocationForOffset(int index) {
        return viewLineCache.getLocationForOffset(index);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void caretUpdate(CaretEvent e) {
    }

    @Override
    public int viewToModel(Point pt) {
        int superResult = super.viewToModel(pt);
        try {
            char ch = this.getDocument().getText(superResult,1).charAt(0);
            if(ch == '\n') return superResult;
            Rectangle backward = this.modelToView(superResult);
            Rectangle forward = this.modelToView(superResult+1);
            setLineHeight(backward.height);
            if(backward.x > forward.x) return superResult;

            float offset = (float) (pt.x - backward.x) / (forward.x - backward.x);
            if(offset < 0) {
                return (1+offset >= BIAS_POINT || (superResult > 0 && this.getDocument().getText(superResult-1,1).charAt(0) == '\n')) ? superResult : Math.max(superResult-1,0);
            }
            return (offset >= BIAS_POINT) ? superResult+1 : superResult;
        } catch(BadLocationException x) {
            Debug.log(x.getMessage(), Debug.MessageType.ERROR);
            return superResult;
        }
    }

    @Override
    public Rectangle modelToView(int pos) throws BadLocationException {
        return super.modelToView(pos);
    }

    protected String getCaretInfo() {
        return caret.getCaretInfo();
    }

    protected String getSelectionInfo() {
        return caret.getSelectionInfo();
    }

    @Override
    public EditorCaret getCaret() {
        return caret;
    }

    public void setCurrentLineColor(Color c) {
        linePainter.setColor(c);
    }

    public TransferHandler getEditorTransferHandler() {
        return editorTransferHandler;
    }

    protected Transferable createTransferable() {
        return new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] {DataFlavor.stringFlavor, DataFlavor.getTextPlainUnicodeFlavor()};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor == DataFlavor.stringFlavor || flavor.equals(DataFlavor.getTextPlainUnicodeFlavor());
            }

            @NotNull
            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                if(flavor == DataFlavor.stringFlavor || flavor.equals(DataFlavor.getTextPlainUnicodeFlavor())) {
                    return caret.getTransferData();
                }
                throw new UnsupportedFlavorException(flavor);
            }
        };
    }

    @Override
    public void cut() {
        copyOrCut(true);
    }

    @Override
    public void copy() {
        copyOrCut(false);
    }

    public static boolean richTextCopy = true;

    private void copyOrCut(boolean cut) {
        try {
            CaretProfile profile = caret.getProfile();
            if(profile.size() > 2 || profile.getSelectedCharCount() > 0) {

                String[] plainText = new String[profile.size() / 2];
                String[] htmlText = new String[profile.size() / 2];
                for (int i = 0; i < profile.size() - 1; i += 2) {
                    int start = profile.get(i);
                    int end = profile.get(i + 1);
                    if (start > end) {
                        int temp = start;
                        start = end;
                        end = temp;
                    }
                    int len = end - start;

                    String segment = this.getDocument().getText(start, len);
                    plainText[i/2] = segment;
                    if(richTextCopy) {
                        htmlText[i/2] = getRegionAsHTML(start, end);
                    } else {
                        htmlText[i/2] = segment;
                    }
                }

                Clipboard clipboard = this.getToolkit().getSystemClipboard();
                clipboard.setContents(new MultiStringSelection(plainText, htmlText), null);
            }

            if(cut) {
                editManager.insertEdit(new InsertionEdit("", this));
            }
        } catch(BadLocationException x) {
            Debug.log(x.getMessage(), Debug.MessageType.ERROR);
        }
    }

    @Override
    public void paste() {
        try {
            Clipboard clipboard = this.getToolkit().getSystemClipboard();
            if(this.getToolkit().getSystemClipboard().isDataFlavorAvailable(MultiStringSelection.multiStringFlavor)) {
                Object rawContents = clipboard.getData(MultiStringSelection.multiStringFlavor);

                if(rawContents == null) return;
                String[] contents = ((String[]) rawContents);
                editManager.insertEdit(new PasteEdit(contents, this));
            } else if(clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                Object rawContents = clipboard.getData(DataFlavor.stringFlavor);

                if(rawContents == null) return;
                String contents = ((String) rawContents).replace("\t", "    ").replace("\r","");
                editManager.insertEdit(new PasteEdit(contents, this));
            }
        } catch(Exception x) {
            x.printStackTrace();
        }
    }

    public void jumpToMatchingBrace() {
        try {
            int dot = this.getCaret().getDot();
            int afterIndex = 0;
            afterIndex = this.getNextNonWhitespace(dot);
            char afterChar = '\0';
            if(afterIndex < this.getDocument().getLength()) {
                afterChar = this.getDocument().getText(afterIndex, 1).charAt(0);
            }

            int braceCheckIndex = afterIndex;

            if(!this.getIndentationManager().isBrace(afterChar)) {
                int beforeIndex = this.getPreviousNonWhitespace(dot);
                char beforeChar = '\0';
                if(beforeIndex >= 0 && beforeIndex < this.getDocument().getLength()) {
                    beforeChar = this.getDocument().getText(beforeIndex, 1).charAt(0);
                }
                if(!this.getIndentationManager().isBrace(beforeChar)) return;
                braceCheckIndex = beforeIndex;
            }

            int matchingIndex = this.getIndentationManager().getMatchingBraceIndex(braceCheckIndex);

            if(matchingIndex == -1) return;
            if(matchingIndex > braceCheckIndex) matchingIndex++;

            caret.setPosition(matchingIndex);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void addCaretPaintListener(@NotNull Runnable runnable) {
        caret.addCaretPaintListener(runnable);
    }

    public Color getSelectionUnfocusedColor() {
        return selectionUnfocusedColor;
    }

    public void setSelectionUnfocusedColor(Color selectionUnfocusedColor) {
        this.selectionUnfocusedColor = selectionUnfocusedColor;
    }

    public IndentationManager getIndentationManager() {
        return indentationManager;
    }

    public Color getBraceHighlightColor() {
        return braceHighlightColor;
    }

    private void setBraceHighlightColor(Color color) {
        braceHighlightColor = color;
    }

    private enum CharType {
        ALPHA(true, 1), WHITESPACE(true, 0), SYMBOL(false), NULL(false);

        public final boolean pull;
        public final int pullPrecedence;

        CharType(boolean pull) {
            this(pull, -1);
        }

        CharType(boolean pull, int pullPrecedence) {
            this.pull = pull;
            this.pullPrecedence = pullPrecedence;
        }
    }

    private static CharType getCharType(char c) {
        if(c == '\n') return CharType.NULL;
        if(Character.isWhitespace(c)) return CharType.WHITESPACE;
        if(c == '_' || Character.isLetterOrDigit(c)) return CharType.ALPHA;
        return CharType.SYMBOL;
    }

    public int getWordStart(int offs) throws BadLocationException {
        if(offs <= 0) return 0;
        Document doc = this.getDocument();
        String text = doc.getText(0, doc.getLength());

        if(text.charAt(offs-1) == '\n') return offs;

        int index = offs-1;
        CharType outsideCharType = CharType.NULL;
        if(offs < text.length()) outsideCharType = getCharType(text.charAt(offs));

        CharType startCharType = getCharType(text.charAt(offs-1));

        CharType expecting = outsideCharType.pull && outsideCharType.pullPrecedence > startCharType.pullPrecedence
                                ? outsideCharType
                                : startCharType;

        while(index >= 0) {
            char ch = text.charAt(index);
            CharType curCharType = getCharType(ch);

            if(curCharType != expecting) {
                return index+1;
            }

            index--;
        }

        return 0;
    }

    public int getWordEnd(int offs) throws BadLocationException {
        Document doc = this.getDocument();
        String text = doc.getText(0, doc.getLength());
        int docLength = doc.getLength();
        if(offs >= docLength) return docLength;

        if(text.charAt(offs) == '\n') return offs;

        int index = offs;
        CharType outsideCharType = CharType.NULL;
        if(offs > 0) outsideCharType = getCharType(text.charAt(offs-1));

        CharType startCharType = getCharType(text.charAt(offs));

        CharType expecting = outsideCharType.pull && outsideCharType.pullPrecedence > startCharType.pullPrecedence
                ? outsideCharType
                : startCharType;

        while(index < docLength) {
            char ch = text.charAt(index);
            CharType curCharType = getCharType(ch);

            if(curCharType != expecting) {
                return index;
            }

            index++;
        }

        return docLength;
    }

    public int getPreviousWord(int offs) throws BadLocationException {
        Document doc = this.getDocument();
        String text = doc.getText(0, doc.getLength());

        int index = offs-1;
        char lastChar = '\000';
        boolean initialWhitespace = true;
        while(index >= 0) {
            char ch = text.charAt(index);
            if(((initialWhitespace || Character.isJavaIdentifierPart(lastChar) == Character.isJavaIdentifierPart(ch)) && ch != '\n') || index == offs-1) {
                if((index != offs-1 && initialWhitespace && !Character.isWhitespace(ch)) || ch == '\n') initialWhitespace = false;
                index--;
                lastChar = ch;
            } else break;
        }
        return index+1;
    }

    public int getNextWord(int offs) throws BadLocationException {
        Document doc = this.getDocument();
        String text = doc.getText(offs, doc.getLength()-offs);

        int index = 0;
        char lastChar = '\000';
        while(index < text.length()) {
            char ch = text.charAt(index);
            if((Character.isJavaIdentifierPart(lastChar) == Character.isJavaIdentifierPart(ch) && ch != '\n') || index == 0) {
                index++;
                lastChar = ch;
            } else break;
        }
        char ch;
        while(index < text.length()-offs && ((ch = text.charAt(index)) != '\n' && Character.isWhitespace(ch))) {
            index++;
        }
        return index+offs;
    }

    public int getNextNonWhitespace(int offs) throws BadLocationException {
        Document doc = this.getDocument();
        String text = doc.getText(offs, doc.getLength()-offs);

        int index;
        for(index = 0; index < text.length(); index++) {
            char c = text.charAt(index);
            if(c == '\n' || !Character.isWhitespace(c)) break;
        }
        return index+offs;
    }

    public int getPreviousNonWhitespace(int offs) throws BadLocationException {
        Document doc = this.getDocument();
        String text = doc.getText(0, offs);

        int index;
        for(index = offs-1; index >= 0; index--) {
            char c = text.charAt(index);
            if(c == '\n' || !Character.isWhitespace(c)) break;
        }
        return index;
    }

    private ArrayList<Consumer<Function<Integer, Integer>>> characterDriftListeners = new ArrayList<>();

    public void registerCharacterDrift(Function<Integer, Integer> h) {
        for(Consumer<Function<Integer, Integer>> listener : characterDriftListeners) {
            listener.accept(h);
        }
    }

    public void addCharacterDriftListener(Consumer<Function<Integer, Integer>> l) {
        characterDriftListeners.add(l);
    }

    public void removeCharacterDriftListener(Consumer<Function<Integer, Integer>> l) {
        characterDriftListeners.remove(l);
    }

    public static boolean isPlatformControlDown(InputEvent e) {
        return (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0;
    }

    private Dimension defaultSize = null;
    private ArrayList<Consumer<Dimension>> defaultSizeListeners = new ArrayList<>();

    public void addDefaultSizeListener(Consumer<Dimension> l) {
        defaultSizeListeners.add(l);
    }

    public void removeDefaultSizeListener(Consumer<Dimension> l) {
        defaultSizeListeners.remove(l);
    }

    public Dimension getDefaultSize() {
        return defaultSize;
    }

    public void setDefaultSize(Dimension defaultSize) {
        this.defaultSize = defaultSize;
        updateDefaultSize();
    }

    private void updateDefaultSize() {
        if(defaultSize != null) {
            int lineCount = getText().split("\n", -1).length;
            Dimension size = new ScalableDimension(defaultSize.width, (getLineHeight() * lineCount) + defaultSize.height - getLineHeight());
            this.setPreferredSize(size);
            for(Consumer<Dimension> consumer : defaultSizeListeners) {
                consumer.accept(size);
            }
        }
    }

    public boolean isEditable() {
        return enabled;
    }

    public void setEditable(boolean enabled) {
        this.enabled = enabled;
    }

    public SuggestionInterface getSuggestionInterface() {
        return suggestionInterface;
    }

    public void setSuggestionInterface(SuggestionInterface suggestionInterface) {
        this.suggestionInterface = suggestionInterface;
    }

    public int getIndentationLevelAt(int index) {
        return indentationManager.getSuggestedIndentationLevelAt(index);
    }

    public int getDocumentIndentationAt(int index) {
        try {
            String text = this.getDocument().getText(0, index);
            int lineStart = Math.max(0, text.lastIndexOf('\n', index - 1)+1);

            int spaces = 0;
            for(int j = lineStart; j < index; j++) {
                if(text.charAt(j) == ' ') spaces++;
                else break;
            }
            return spaces / 4;
        } catch (BadLocationException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getLineHeight() {
        return lineHeight;
    }

    void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    //Delegates and deprecated managers

    @Override
    public synchronized void addKeyListener(KeyListener l) {
        KeyListener[] oldListeners = this.getKeyListeners();
        for(KeyListener listener : oldListeners) {
            super.removeKeyListener(listener);
        }
        super.addKeyListener(l);
        for(KeyListener listener : oldListeners) {
            super.addKeyListener(listener);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        repaint();
    }

    @Override
    public void focusLost(FocusEvent e) {
        repaint();
    }

    @Override
    public void dispose() {
        tlm.dispose();
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        Component parent = getParent();
        ComponentUI ui = getUI();

        return parent == null || ui.getPreferredSize(this).width <= parent.getSize().width;
    }

    @Override
    public void setCaretPosition(int position) {
        caret.setPosition(position);
    }

    @Override
    public int getCaretPosition() {
        return super.getCaretPosition();
    }

    @Deprecated
    public void replaceSelection(String content) {
        super.replaceSelection(content);
    }

    @Deprecated
    public void moveCaretPosition(int pos) {
        super.moveCaretPosition(pos);
    }

    @Deprecated
    public String getSelectedText() {
        return super.getSelectedText();
    }

    @Deprecated
    public int getSelectionStart() {
        return super.getSelectionStart();
    }

    @Deprecated
    public void setSelectionStart(int selectionStart) {
        super.setSelectionStart(selectionStart);
    }

    @Deprecated
    public int getSelectionEnd() {
        return super.getSelectionEnd();
    }

    @Deprecated
    public void setSelectionEnd(int selectionEnd) {
        super.setSelectionEnd(selectionEnd);
    }

    @Deprecated
    public void select(int selectionStart, int selectionEnd) {
        super.select(selectionStart, selectionEnd);
    }

    @Deprecated
    public void selectAll() {
        super.selectAll();
    }

    public boolean isDocumentUpToDate() {
        return true;
    }

    public String getRegionAsHTML(int start, int end) {
        if(start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<pre style='font-family: \"Consolas\", monospace; font-size: 11pt'>");

            StyledDocument sd = getStyledDocument();

            String s = sd.getText(start, end - start);
            String[] as = s.replaceAll("\r?\n", "\n").split("(?!^)");
            Color[] colors = new Color[as.length];
            byte[] styles = new byte[as.length];

            for(int i = 0; i < as.length; i++) {
                AttributeSet attributes = sd.getCharacterElement(start+i).getAttributes();
                colors[i] = (Color) attributes.getAttribute(StyleConstants.ColorConstants.Foreground);
                if(colors[i] == null) {
                    colors[i] = getForeground();
                }

                String styleName = (String) attributes.getAttribute(StyleConstants.CharacterConstants.NameAttribute);
                Style namedStyle = sd.getStyle(styleName);
                byte style = 0;
                if(Boolean.TRUE.equals(namedStyle.getAttribute(StyleConstants.CharacterConstants.Bold))) {
                    style |= 0b01;
                }
                if(Boolean.TRUE.equals(namedStyle.getAttribute(StyleConstants.CharacterConstants.Italic))) {
                    style |= 0b10;
                }

                styles[i] = style;
            }

            Color pc = null;
            boolean pbold = false;
            boolean pitalic = false;
            StringBuilder styleOpens = new StringBuilder();
            for (int i = 0; i < as.length; i++) {
                styleOpens.setLength(0);
                if (colors[i] != pc) {
                    if (pc != null) {
                        sb.append("</span>");
                    }
                    if (colors[i] != null) {
                        styleOpens.append("<span style='color: ").append(String.format("#%02x%02x%02x", colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue())).append("'>");
                    }
                    pc = colors[i];
                }
                boolean bold = (styles[i] & 0b01) != 0;
                boolean italic = (styles[i] & 0b10) != 0;

                if(bold != pbold) {
                    if(bold) {
                        styleOpens.insert(0, "<b>");
                    } else {
                        sb.append("</b>");
                    }
                    pbold = bold;
                }
                if(italic != pitalic) {
                    if(italic) {
                        styleOpens.insert(0, "<em>");
                    } else {
                        sb.append("</em>");
                    }
                    pitalic = italic;
                }

                sb.append(styleOpens);
                sb.append(as[i].replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br> "));
            }
            if (pc != null) {
                sb.append("</span>");
            }
            if(pbold) {
                sb.append("</b>");
            }
            if(pitalic) {
                sb.append("</em>");
            }

            sb.append("</pre>");

            return sb.toString();
        }
        catch (BadLocationException ex) {
            ex.printStackTrace();
        }

        return "";
    }

    static {
        ConsoleBoard.registerCommandHandler("richcopy", new ConsoleBoard.CommandHandler() {
            @Override
            public String getDescription() {
                return "Toggles rich text copying from the editor (experimental)";
            }

            @Override
            public void printHelp() {
                Debug.log();
                Debug.log("RICHCOPY: Toggles rich text copying from the editor (experimental)");
            }

            @Override
            public void handle(String[] args) {
                richTextCopy = !richTextCopy;
                Debug.log("Rich text copying is now: " + richTextCopy);
            }
        });
    }

    private static class MyTransferable implements Transferable {
        private static ArrayList<DataFlavor> MyFlavors = new ArrayList<DataFlavor>();
        private String plain = null;
        private String html = null;

        static {
            try {
                for (String m : new String[]{"text/plain", "text/html"}) {
                    MyFlavors.add(new DataFlavor(m + ";class=java.lang.String"));
                    MyFlavors.add(new DataFlavor(m + ";class=java.io.Reader"));
                    MyFlavors.add(new DataFlavor(m + ";class=java.io.InputStream;charset=utf-8"));
                }
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public MyTransferable(String plain, String html) {
            this.plain = plain;
            this.html = html;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return MyFlavors.toArray(new DataFlavor[MyFlavors.size()]);
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return MyFlavors.contains(flavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            String s = null;
            if (flavor.getMimeType().contains("text/plain")) {
                s = plain;
            }
            else if (flavor.getMimeType().contains("text/html")) {
                s = html;
            }
            if (s != null) {
                if (String.class.equals(flavor.getRepresentationClass())) {
                    return s;
                }
                else if (Reader.class.equals(flavor.getRepresentationClass())) {
                    return new StringReader(s);
                }
                else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                    return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
                }
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
