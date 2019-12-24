package com.energyxxer.trident.ui.editor.behavior.editmanager.edits;

import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.trident.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.trident.ui.editor.behavior.caret.Dot;
import com.energyxxer.trident.ui.editor.behavior.caret.EditorCaret;
import com.energyxxer.trident.ui.editor.behavior.editmanager.Edit;
import com.energyxxer.trident.ui.editor.folding.FoldableDocument;

import javax.swing.text.BadLocationException;

/**
 * Created by User on 1/26/2017.
 */
public class LineMoveEdit extends Edit {
    private CaretProfile previousProfile = new CaretProfile();
    private String previousText;
    private final int dir;

    public LineMoveEdit(AdvancedEditor editor, int dir) {
        previousProfile = editor.getCaret().getProfile();
        if(dir < 2 || dir > 3) throw new IllegalArgumentException("Invalid direction '" + dir + "'");
        this.dir = dir;
    }

    @Override
    public boolean redo(AdvancedEditor editor) {
        FoldableDocument doc = editor.getFoldableDocument();
        EditorCaret caret = editor.getCaret();

        boolean actionPerformed = false;

        try {
            String text = doc.getUnfoldedText(); //Result
            this.previousText = text;

            int characterDrift = 0;

            CaretProfile nextProfile = new CaretProfile();

            for (int i = (dir == Dot.UP) ? 0 : previousProfile.size()-2; (dir == Dot.UP) ? (i < previousProfile.size() - 1) : (i >= 0); i += (dir == Dot.UP) ? 2 : -2) {
                //Get bounds of the line to move
                int selectionStart = previousProfile.get(i) + characterDrift;
                int selectionEnd = previousProfile.get(i + 1) + characterDrift;

                int start = new Dot(Math.min(selectionStart,selectionEnd),editor).getRowStart();
                int end = new Dot(Math.max(selectionStart,selectionEnd),editor).getRowEnd()+1;

                //Check if invalid
                if((start == 0 && dir == Dot.UP) || (end == text.length()+1 && dir == Dot.DOWN)) {
                    nextProfile.add(new Dot(selectionStart,selectionEnd,editor));
                    continue;
                }

                //Get start of the line to move to

                int shiftTo;
                if(dir == Dot.UP) {
                    shiftTo = new Dot(start, editor).getPositionAbove();
                } else shiftTo = new Dot(end, editor).getRowEnd()+1 - (end-start);

                //Remove lines

                String value = text.substring(start,end-1) + "\n";

                if (end < text.length())
                    doc.remove(start, (end - start));
                else
                    doc.remove(start - 1, (end - start));

                //Add value back

                doc.insertString(shiftTo,value,null);
                actionPerformed = true;

                //Add new dot to profile

                nextProfile.add(new Dot(selectionStart + (shiftTo - start),selectionEnd + (shiftTo - start),editor));
            }

            caret.setProfile(nextProfile);

        } catch(BadLocationException x) {
            x.printStackTrace();
        }
        return actionPerformed;
    }

    @Override
    public boolean undo(AdvancedEditor editor) {
        FoldableDocument doc = editor.getFoldableDocument();
        EditorCaret caret = editor.getCaret();

        //Too complicated, just put back the text from before.

        doc.replace(this.previousText);
        caret.setProfile(previousProfile);
        return true;
    }
}
