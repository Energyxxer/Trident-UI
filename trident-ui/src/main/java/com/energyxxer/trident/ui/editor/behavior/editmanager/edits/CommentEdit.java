package com.energyxxer.trident.ui.editor.behavior.editmanager.edits;

import com.energyxxer.trident.global.Status;
import com.energyxxer.trident.global.temp.Lang;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.editor.TridentEditorComponent;
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
    private final String commentMarker;
    private boolean uncomment = false;

    public CommentEdit(AdvancedEditor editor) {
        previousProfile = editor.getCaret().getProfile();
        if(editor instanceof TridentEditorComponent) {
            Lang language = ((TridentEditorComponent) editor).getParentModule().getLanguage();
            if(language == null) {
                TridentWindow.setStatus(new Status("Unknown language, don't know how to comment it out."));
                commentMarker = null;
            } else {
                commentMarker = language.getProperty("line_comment_marker");
                if(commentMarker == null) TridentWindow.setStatus(new Status("Language '" + language + "' has no comments"));
            }
        } else {
            commentMarker = null;
        }
    }

    @Override
    public boolean redo(AdvancedEditor editor) {
        if(commentMarker == null) return false;
        Document doc = editor.getDocument();
        EditorCaret caret = editor.getCaret();

        try {
            String text = doc.getText(0, doc.getLength()); //Result
            this.modifications.clear();
            this.uncomment = true;


            CaretProfile nextProfile = new CaretProfile(previousProfile);

            //First, make a list of all line starts and decide whether to comment or uncomment.

            EditUtils.fetchSelectedLines(previousProfile, editor, modifications, new EditUtils.Configuration() {
                {
                    fetchEnd = false;
                    lineHandler = (start, end, index) -> {
                        if(!text.startsWith(commentMarker, start)) uncomment = false;
                    };
                }
            });

            for(int i = 0; i < previousProfile.size()-1; i+= 2) {
                int selectionStart = previousProfile.get(i);
                int selectionEnd = previousProfile.get(i + 1);

                if(selectionStart == selectionEnd) {
                    int below = new Dot(selectionStart, editor).getPositionBelow();
                    nextProfile.set(i>>1, below);
                    nextProfile.set((i>>1)+1, below);
                }
            }

            //List done, start adding/removing comment markers

            int characterDrift = 0;
            for(int lineStart : modifications) {
                if(uncomment) {
                    doc.remove(lineStart+characterDrift, commentMarker.length());
                    nextProfile.pushFrom(lineStart+characterDrift+commentMarker.length(), -commentMarker.length());
                    characterDrift -= commentMarker.length();
                } else {
                    doc.insertString(lineStart+characterDrift, commentMarker, null);
                    nextProfile.pushFrom(lineStart+characterDrift, commentMarker.length());
                    characterDrift += commentMarker.length();
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
