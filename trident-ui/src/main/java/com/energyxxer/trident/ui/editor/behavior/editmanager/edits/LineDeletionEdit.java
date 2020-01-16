package com.energyxxer.trident.ui.editor.behavior.editmanager.edits;

import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.trident.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.trident.ui.editor.behavior.caret.Dot;
import com.energyxxer.trident.ui.editor.behavior.caret.EditorCaret;
import com.energyxxer.trident.ui.editor.behavior.editmanager.Edit;
import com.energyxxer.util.logger.Debug;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.ArrayList;

public class LineDeletionEdit extends Edit {

    private CaretProfile previousProfile;
    private ArrayList<Integer> modificationLines = new ArrayList<>();
    private ArrayList<String> previousValues = new ArrayList<>();

    public LineDeletionEdit(AdvancedEditor editor) {
        previousProfile = editor.getCaret().getProfile();
    }

    @Override
    public boolean redo(AdvancedEditor editor) {
        Document doc = editor.getDocument();
        EditorCaret caret = editor.getCaret();

        try {
            String text = doc.getText(0, doc.getLength()); //Result
            this.modificationLines.clear();
            this.previousValues.clear();


            CaretProfile nextProfile = new CaretProfile(previousProfile);


            EditUtils.fetchSelectedLines(previousProfile, editor, modificationLines, new EditUtils.Configuration() {
                {
                    includeNewline = true;
                    includeLastLine = true;
                }
            });


            for(int i = 0; i < previousProfile.size()-1; i+= 2) {
                int selectionStart = previousProfile.get(i);

                nextProfile.set((i>>1)+1, selectionStart); //collapse to index
            }

            int characterDrift = 0;
            for(int i = 0; i < modificationLines.size(); i+=2) {
                int lineStart = modificationLines.get(i);
                int lineEnd = modificationLines.get(i+1);
                previousValues.add(text.substring(lineStart, lineEnd));
                for(int j = 0; j < nextProfile.size()-1; j+=2) {
                    int dot = nextProfile.get(j);
                    if(dot >= lineStart+characterDrift && dot < lineEnd+characterDrift ) {
                        dot = new Dot(dot, editor).getPositionBelow();
                        nextProfile.set(j,dot);
                        nextProfile.set(j+1,dot);
                    }
                    if(dot >= lineEnd+characterDrift ) {
                        dot -= lineEnd-lineStart;
                        nextProfile.set(j,dot);
                        nextProfile.set(j+1,dot);
                    }
                }
                doc.remove(lineStart + characterDrift, (lineEnd+characterDrift)-(lineStart+characterDrift));
                characterDrift -= lineEnd-lineStart;
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
            for(int i = 0; i < modificationLines.size(); i+=2) {
                int lineStart = modificationLines.get(i);
                doc.insertString(lineStart, previousValues.get(i>>1), null);
            }
        } catch(BadLocationException x) {
            x.printStackTrace();
        }
        caret.setProfile(previousProfile);
        return true;
    }
}