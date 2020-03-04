package com.energyxxer.trident.ui.editor.behavior.editmanager.edits;

import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.trident.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.trident.ui.editor.behavior.caret.Dot;
import com.energyxxer.trident.ui.editor.behavior.caret.EditorCaret;
import com.energyxxer.trident.ui.editor.behavior.editmanager.Edit;
import com.energyxxer.util.StringUtil;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import java.util.ArrayList;

/**
 * Created by User on 1/10/2017.
 */
public class InsertionEdit extends Edit {
    private String value;
    private ArrayList<Integer> undoIndices = new ArrayList<>();
    private ArrayList<String> previousValues = new ArrayList<>();
    private ArrayList<String> writingValues = new ArrayList<>();
    private CaretProfile previousProfile;

    public InsertionEdit(String value, AdvancedEditor editor) {
        this.value = value;
        this.previousProfile = editor.getCaret().getProfile();
    }

    @Override
    public boolean redo(AdvancedEditor editor) {

        StyledDocument doc = editor.getStyledDocument();
        EditorCaret caret = editor.getCaret();
        try {
            String result = doc.getText(0, doc.getLength()); //Result

            int characterDrift = 0;

            previousValues.clear();
            undoIndices.clear();
            writingValues.clear();
            CaretProfile nextProfile = new CaretProfile();

            for (int i = 0; i < previousProfile.size() - 1; i += 2) {
                int start = previousProfile.get(i) + characterDrift;
                int end = previousProfile.get(i + 1) + characterDrift;
                if (end < start) {
                    int temp = start;
                    start = end;
                    end = temp;
                }

                String valueToWrite = value;
                if (Dot.SMART_KEYS_INDENT.get() && valueToWrite.length() == 1 && editor.getIndentationManager().isClosingBrace(valueToWrite) && start <= new Dot(start, start, editor).getRowContentStart()) {
                    int rowStart = new Dot(start, start, editor).getRowStart();
                    int whitespace = start - rowStart;
                    int properWhitespace = 4 * Math.max(editor.getIndentationLevelAt(start) - 1, 0);
                    int diff = properWhitespace - whitespace;
                    start += diff;
                    if(end < start) {
                        valueToWrite = StringUtil.repeat(" ", start - end) + valueToWrite;
                        start = end;
                    }
                }
                int caretOffset = 0;
                if(valueToWrite.length() == 1 && Dot.SMART_KEYS_BRACES.get() && editor.getIndentationManager().isOpeningBrace(valueToWrite) && editor.getIndentationManager().isBalanced()) {
                    valueToWrite += editor.getIndentationManager().getMatchingBraceChar(valueToWrite);
                    caretOffset--;
                } else if(valueToWrite.length() == 1 &&
                        ((
                                Dot.SMART_KEYS_BRACES.get() &&
                                editor.getIndentationManager().isClosingBrace(valueToWrite) &&
                                editor.getIndentationManager().isBalanced()
                        ) ||
                        (
                                Dot.SMART_KEYS_QUOTES.get() &&
                                "\"'".contains(valueToWrite) &&
                                !editor.getStyledDocument().getCharacterElement(start).getAttributes().containsAttributes(editor.getStyle(AdvancedEditor.STRING_ESCAPE_STYLE))
                        )
                        ) && result.startsWith(valueToWrite,start) ) {
                    valueToWrite = "";
                    caretOffset++;
                } else if(valueToWrite.length() == 1 && Dot.SMART_KEYS_QUOTES.get() && "\"'".contains(valueToWrite) && !editor.getStyledDocument().getCharacterElement(start).getAttributes().containsAttributes(editor.getStyle(AdvancedEditor.STRING_STYLE)) && !editor.getStyledDocument().getCharacterElement(start).getAttributes().containsAttributes(editor.getStyle(AdvancedEditor.STRING_ESCAPE_STYLE))) {
                    valueToWrite += "\"";
                    caretOffset--;
                }

                undoIndices.add(start);
                previousValues.add(result.substring(start, end));
                writingValues.add(valueToWrite);
                result = result.substring(0, start) + valueToWrite + result.substring(end);

                nextProfile.add(start + valueToWrite.length()+caretOffset, start + valueToWrite.length()+caretOffset);

                ((AbstractDocument) doc).replace(start, end - start, valueToWrite, null);

                characterDrift += valueToWrite.length() - (end - start);

                final int fstart = start;
                final int fend = end;
                final int flen = valueToWrite.length();

                editor.registerCharacterDrift(o -> (o >= fstart) ? ((o <= fend) ? fstart + flen : o + flen - (fend - fstart)) : o);
            }
            caret.setProfile(nextProfile);

        } catch(BadLocationException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean undo(AdvancedEditor editor) {

        Document doc = editor.getDocument();
        EditorCaret caret = editor.getCaret();
        try {
            int characterDrift = 0;
            for (int i = 0; i < undoIndices.size(); i++) {
                int start = undoIndices.get(i) + characterDrift;
                int resultEnd = start + writingValues.get(i).length();

                String previousValue = previousValues.get(i);

                ((AbstractDocument) doc).replace(start, resultEnd - start, previousValue, null);

                final int fstart = start;
                final int flen = resultEnd - start;
                final int fplen = previousValue.length();
                characterDrift += fplen-flen;

                editor.registerCharacterDrift(o -> (o >= fstart) ? ((o <= fstart + flen) ? fstart + fplen : o + (fplen - flen)): o);
            }

            caret.setProfile(previousProfile);

        } catch(BadLocationException e) {
            e.printStackTrace();
        }
        return true;
    }
}
