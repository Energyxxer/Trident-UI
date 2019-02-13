package com.energyxxer.trident.ui.editor.behavior;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.trident.ui.editor.behavior.caret.Dot;
import com.energyxxer.trident.ui.editor.behavior.caret.EditorCaret;
import com.energyxxer.trident.ui.editor.behavior.editmanager.CharacterDriftHandler;
import com.energyxxer.trident.ui.editor.behavior.editmanager.EditManager;
import com.energyxxer.trident.ui.editor.behavior.editmanager.edits.*;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.trident.util.linepainter.LinePainter;
import com.energyxxer.util.Disposable;
import com.energyxxer.util.StringLocation;
import com.energyxxer.util.StringLocationCache;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.xswing.UnifiedDocumentListener;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Created by User on 1/5/2017.
 */
public class AdvancedEditor extends JTextPane implements KeyListener, CaretListener, Disposable {

    private ThemeListenerManager tlm;

    private EditorCaret caret;

    private EditManager editManager = new EditManager(this);
    private LinePainter linePainter;

    private StringLocationCache lineCache = new StringLocationCache();

    private static final float BIAS_POINT = 0.4f;

    private static final String WORD_DELIMITERS = "./\\()\"'-:,.;<>~!@#$%^&*|+=[]{}`~?";

    {
        this.addKeyListener(this);

        linePainter = new LinePainter(this);
        this.setCaret(this.caret = new EditorCaret(this));

        //this.getInputMap().setParent(null);
        this.setInputMap(JComponent.WHEN_FOCUSED,new InputMap());

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),"undo");
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),"redo");

        this.getActionMap().put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editManager.undo();
            }
        });
        this.getActionMap().put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editManager.redo();
            }
        });

        this.getDocument().addUndoableEditListener(e -> {
            lineCache.setText(this.getText());
        });

        this.getDocument().addDocumentListener((UnifiedDocumentListener) e -> updateDefaultSize());

        tlm = new ThemeListenerManager();

        tlm.addThemeChangeListener(t -> {
            this.setBackground(t.getColor(Color.WHITE, "Editor.background"));
            this.setBackground(this.getBackground());
            this.setForeground(t.getColor(Color.BLACK, "Editor.foreground","General.foreground"));
            this.setCaretColor(this.getForeground());
            this.setSelectionColor(t.getColor(new Color(50, 100, 175), "Editor.selection.background"));
            this.setSelectedTextColor(t.getColor(this.getForeground(), "Editor.selection.foreground"));
            this.setCurrentLineColor(t.getColor(new Color(235, 235, 235), "Editor.currentLine.background"));
            this.setFont(new Font(t.getString("Editor.font","default:monospaced"), Font.PLAIN, 12));
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

    public AdvancedEditor() {
        this.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
    }

    public AdvancedEditor(StyledDocument doc) {
        super(doc);
        this.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if(e.isConsumed()) return;
        e.consume();
        if(!isPlatformControlDown(e) && !Commons.isSpecialCharacter(e.getKeyChar())) {
            editManager.insertEdit(new InsertionEdit("" + e.getKeyChar(), this));
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.isConsumed()) return;
        int keyCode = e.getKeyCode();
        if(keyCode == KeyEvent.VK_TAB) {
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
        } else if(keyCode == KeyEvent.VK_ENTER) {
            e.consume();
            editManager.insertEdit(new NewlineEdit(this, !isPlatformControlDown(e)));
        } else if((keyCode == KeyEvent.VK_C || keyCode == KeyEvent.VK_X) && isPlatformControlDown(e)) {
            e.consume();

            try {
                CaretProfile profile = caret.getProfile();
                if(profile.size() > 2 || profile.getSelectedCharCount() > 0) {

                    String[] toCopy = new String[profile.size() / 2];
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
                        toCopy[i/2] = segment;
                    }

                    Clipboard clipboard = this.getToolkit().getSystemClipboard();
                    clipboard.setContents(new MultiStringSelection(toCopy), null);
                }

                if(keyCode == KeyEvent.VK_X) {
                    editManager.insertEdit(new InsertionEdit("", this));
                }
            } catch(BadLocationException x) {
                Debug.log(x.getMessage(), Debug.MessageType.ERROR);
            }

        } else if(keyCode == KeyEvent.VK_V && isPlatformControlDown(e)) {
            e.consume();
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
        } else if(keyCode == KeyEvent.VK_A && isPlatformControlDown(e)) {
            e.consume();
            caret.setProfile(new CaretProfile(0, getDocument().getLength()));
        } else if(keyCode >= KeyEvent.VK_LEFT && keyCode <= KeyEvent.VK_DOWN && e.isAltDown()) {
            e.consume();
            if(keyCode == KeyEvent.VK_UP) {
                editManager.insertEdit(new LineMoveEdit(this, Dot.UP));
            }
            else if(keyCode == KeyEvent.VK_DOWN) {
                editManager.insertEdit(new LineMoveEdit(this, Dot.DOWN));
            }
        } else if(keyCode == KeyEvent.VK_ESCAPE) {
            int dotPos = caret.getDot();
            caret.setProfile(new CaretProfile(dotPos, dotPos));
        }
    }

    public EditManager getEditManager() {
        return editManager;
    }

    public StringLocation getLocationForOffset(int index) {
        return lineCache.getLocationForOffset(index);
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
        String text = doc.getText(0, doc.getLength());

        int index = offs;
        char lastChar = '\000';
        while(index < doc.getLength()) {
            char ch = text.charAt(index);
            if((Character.isJavaIdentifierPart(lastChar) == Character.isJavaIdentifierPart(ch) && ch != '\n') || index == offs) {
                index++;
                lastChar = ch;
            } else break;
        }
        char ch;
        while(index < text.length() && ((ch = text.charAt(index)) != '\n' && Character.isWhitespace(ch))) {
            index++;
        }
        return index;
    }

    private ArrayList<Consumer<CharacterDriftHandler>> characterDriftListeners = new ArrayList<>();

    public void registerCharacterDrift(CharacterDriftHandler h) {
        for(Consumer<CharacterDriftHandler> listener : characterDriftListeners) {
            listener.accept(h);
        }
    }

    public void addCharacterDriftListener(Consumer<CharacterDriftHandler> l) {
        characterDriftListeners.add(l);
    }

    public void removeCharacterDriftListener(Consumer<CharacterDriftHandler> l) {
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
            int lines = getText().split("\n", -1).length;
            Dimension size = new Dimension(defaultSize.width, (17 * lines) + defaultSize.height - 17);
            this.setPreferredSize(size);
            for(Consumer<Dimension> consumer : defaultSizeListeners) {
                consumer.accept(size);
            }
        }
    }

    //Delegates and deprecated managers

    @Override
    public synchronized void addKeyListener(KeyListener l) {
        if(l != this) {
            super.removeKeyListener(this);
            super.addKeyListener(l);
            super.addKeyListener(this);
        } else {
            super.addKeyListener(l);
        }
    }

    @Override
    public void dispose() {
        Debug.log("Disposing of editor");
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
}
