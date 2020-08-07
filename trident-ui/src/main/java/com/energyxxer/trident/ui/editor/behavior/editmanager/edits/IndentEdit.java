package com.energyxxer.trident.ui.editor.behavior.editmanager.edits;

import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.trident.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.trident.ui.editor.behavior.caret.Dot;
import com.energyxxer.trident.ui.editor.behavior.caret.EditorCaret;
import com.energyxxer.trident.ui.editor.behavior.editmanager.Edit;
import com.energyxxer.util.StringUtil;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.ArrayList;

/**
 * Created by User on 1/27/2017.
 */
public class IndentEdit extends Edit {
    private CaretProfile previousProfile;
    /**
     * ArrayList containing information about how to undo the edit.
     * Must contain even entries.
     * Every even index (0, 2, 4...) contains the position in the document where spaces were added/removed. Doesn't account for indices drifted by adding or removing spaces.
     * Every odd index (1, 3, 5...) contains the amount of spaces added/removed at the position given by the index before it. (negatives for removal, positives for insertion)
     * */
    private ArrayList<Integer> modifications = new ArrayList<>();
    private final boolean reverse;

    public IndentEdit(AdvancedEditor editor) {
        this(editor,false);
    }

    public IndentEdit(AdvancedEditor editor, boolean reverse) {
        previousProfile = editor.getCaret().getProfile();
        this.reverse = reverse;
    }

    @Override
    public boolean redo(AdvancedEditor editor) {
        Document doc = editor.getDocument();
        EditorCaret caret = editor.getCaret();

        boolean actionPerformed = false;

        try {
            String text = doc.getText(0, doc.getLength()); //Result
            this.modifications.clear();

            CaretProfile nextProfile = new CaretProfile(previousProfile);

            int previousL = -1;

            for (int i = 0; i < previousProfile.size()-1; i += 2) {
                //Get bounds of the line to move
                int selectionStart = previousProfile.get(i);
                int selectionEnd = previousProfile.get(i + 1);

                int start = new Dot(Math.min(selectionStart,selectionEnd),editor).getRowStart();
                int end = new Dot(Math.max(selectionStart,selectionEnd),editor).getRowEnd();

                boolean firstLineOfSelection = true;

                for(int l = start; l < end; previousL = l, l = new Dot(l, editor).getRowEnd()+1) {
                    if(l == previousL) {
                        if(firstLineOfSelection) {
                            firstLineOfSelection = false;
                            continue;
                        } else {
                            break;
                        }
                    }
                    firstLineOfSelection = false;
                    int spaces = StringUtil.getSequenceCount(text," ", l);
                    if(!reverse) {
                        int spacesToAdd = 4 - (spaces % 4);
                        spacesToAdd = (spacesToAdd > 0) ? spacesToAdd : 4;
                        modifications.add(l);
                        modifications.add(spacesToAdd);
                        actionPerformed = true;
                    } else {
                        if(spaces == 0) {
                            continue;
                        }
                        int spacesToRemove = (spaces % 4 == 0) ? 4 : spaces % 4;
                        if(spacesToRemove != 0) {
                        }
                        actionPerformed = true;
                        modifications.add(l);
                        modifications.add(-spacesToRemove);
                    }
                }
            }

            if(actionPerformed) {
                for(int i = modifications.size()-2; i >= 0; i -= 2) {
                    int index = modifications.get(i);
                    int spaces = modifications.get(i+1);

                    if(spaces > 0) {
                        doc.insertString(index, StringUtil.repeat(" ", spaces), null);
                        nextProfile.pushFrom(index,spaces);
                    } else {
                        doc.remove(index, -spaces);
                        nextProfile.pushFrom(index,Math.min(0,spaces));
                    }

                    editor.registerCharacterDrift(o -> (o >= index) ? o + spaces : o);
                }

                caret.setProfile(nextProfile);
            }
        } catch(BadLocationException x) {
            x.printStackTrace();
        }
        return actionPerformed;
    }

    @Override
    public boolean undo(AdvancedEditor editor) {
        Document doc = editor.getDocument();
        EditorCaret caret = editor.getCaret();

        try {
            for(int i = 0; i < modifications.size(); i += 2) {
                int index = modifications.get(i);
                int spaces = modifications.get(i+1);

                if(spaces > 0) {
                    doc.remove(index, spaces);
                } else {
                    doc.insertString(index, StringUtil.repeat(" ", -spaces), null);
                }

                editor.registerCharacterDrift(o -> (o >= index) ? o - spaces : o);
            }
        } catch(BadLocationException x) {
            x.printStackTrace();
        }
        caret.setProfile(previousProfile);
        return true;
    }
}
