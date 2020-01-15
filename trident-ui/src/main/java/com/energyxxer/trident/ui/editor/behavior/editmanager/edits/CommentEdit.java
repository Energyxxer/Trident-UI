package com.energyxxer.trident.ui.editor.behavior.editmanager.edits;

import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.trident.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.trident.ui.editor.behavior.caret.Dot;
import com.energyxxer.trident.ui.editor.behavior.caret.EditorCaret;
import com.energyxxer.trident.ui.editor.behavior.editmanager.Edit;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.ArrayList;

public class CommentEdit extends Edit {


    private CaretProfile previousProfile;
    /**
     * ArrayList containing information about how to undo the edit.
     * Must contain even entries.
     * Every index contains the position in the document where comment markers were added/removed.
     * */
    private ArrayList<Integer> modifications = new ArrayList<>();
    private final String commentMarker = "#";
    private boolean uncomment = false;

    public CommentEdit(AdvancedEditor editor) {
        previousProfile = editor.getCaret().getProfile();
    }

    @Override
    public boolean redo(AdvancedEditor editor) {
        Document doc = editor.getDocument();
        EditorCaret caret = editor.getCaret();

        try {
            String text = doc.getText(0, doc.getLength()); //Result
            this.modifications.clear();
            this.uncomment = true;

            int characterDrift = 0;

            CaretProfile nextProfile = new CaretProfile(previousProfile);

            int previousL = -1;

            //First, make a list of all line starts and decide whether to comment or uncomment.

            for(int i = 0; i < previousProfile.size()-1; i+= 2) {
                int selectionStart = previousProfile.get(i) + characterDrift;
                int selectionEnd = previousProfile.get(i + 1) + characterDrift;

                int lineStart = new Dot(Math.min(selectionStart,selectionEnd),editor).getRowStart();
                int lineEnd = new Dot(Math.max(selectionStart,selectionEnd),editor).getRowEnd();

                if(selectionStart == selectionEnd) {
                    int below = new Dot(selectionStart, editor).getPositionBelow();
                    nextProfile.set(i>>1, below);
                    nextProfile.set((i>>1)+1, below);
                }

                boolean firstLineOfSelection = true;

                for(int l = lineStart; l < lineEnd; previousL = l, l = new Dot(l, editor).getPositionBelow()) {
                    if(l == previousL) {
                        if(firstLineOfSelection) {
                            firstLineOfSelection = false;
                            continue;
                        } else break;
                    }
                    firstLineOfSelection = false;
                    modifications.add(l);
                    if(uncomment && !text.startsWith(commentMarker, l)) uncomment = false;
                }

            }

            //List done, start adding/removing comment markers

            int drift = 0;
            for(int lineStart : modifications) {
                if(uncomment) {
                    doc.remove(lineStart+drift, commentMarker.length());
                    nextProfile.pushFrom(lineStart+drift+commentMarker.length(), -commentMarker.length());
                    drift -= commentMarker.length();
                } else {
                    doc.insertString(lineStart+drift, commentMarker, null);
                    nextProfile.pushFrom(lineStart+drift, commentMarker.length());
                    drift += commentMarker.length();
                }
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
            for(int lineStart : modifications) {
                if(!uncomment) {
                    doc.remove(lineStart, commentMarker.length());
                } else {
                    doc.insertString(lineStart, commentMarker, null);
                }
            }
        } catch(BadLocationException x) {
            x.printStackTrace();
        }
        caret.setProfile(previousProfile);
        return true;
    }
}
