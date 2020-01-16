package com.energyxxer.trident.ui.editor.behavior.editmanager.edits;

import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.trident.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.trident.ui.editor.behavior.caret.EditorCaret;
import com.energyxxer.trident.ui.editor.behavior.editmanager.Edit;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.ArrayList;

public class LineDuplicationEdit extends Edit {

    private CaretProfile previousProfile;
    private ArrayList<Integer> copiedLines = new ArrayList<>();

    public LineDuplicationEdit(AdvancedEditor editor) {
        previousProfile = editor.getCaret().getProfile();
    }

    @Override
    public boolean redo(AdvancedEditor editor) {
        Document doc = editor.getDocument();
        EditorCaret caret = editor.getCaret();

        try {
            String text = doc.getText(0, doc.getLength()); //Result
            this.copiedLines.clear();

            CaretProfile nextProfile = new CaretProfile(previousProfile);

            EditUtils.fetchSelectedLines(previousProfile, editor, copiedLines, new EditUtils.Configuration() {
                int carriedDrift = 0;
                {
                    includeNewline = true;
                    includeLastLine = true;
                    mergeLinesPerSelection = true;
                    lineHandler = ((start, end, selectionIndex) -> {
                        nextProfile.set(selectionIndex*2, nextProfile.get(selectionIndex*2) + carriedDrift + (end-start));
                        nextProfile.set(selectionIndex*2+1, nextProfile.get(selectionIndex*2+1) + carriedDrift + (end-start));
                        carriedDrift += end-start;
                        //Move down
                    });
                }
            });

            int characterDrift = 0;
            for(int i = 0; i < copiedLines.size(); i+=2) {
                int lineStart = copiedLines.get(i);
                int lineEnd = copiedLines.get(i+1);
                String toCopy = text.substring(lineStart, lineEnd);

                doc.insertString(lineEnd+characterDrift, toCopy, null);
                characterDrift += lineEnd-lineStart;
            }

            caret.setProfile(nextProfile);
        } catch(BadLocationException x) {
            x.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean undo(AdvancedEditor editor) {
        Document doc = editor.getDocument();
        EditorCaret caret = editor.getCaret();

        try {
            for(int i = 0; i < copiedLines.size(); i+=2) {
                doc.remove(copiedLines.get(i+1), copiedLines.get(i+1)-copiedLines.get(i));
            }
        } catch(BadLocationException x) {
            x.printStackTrace();
        }
        caret.setProfile(previousProfile);
        return true;
    }
}
