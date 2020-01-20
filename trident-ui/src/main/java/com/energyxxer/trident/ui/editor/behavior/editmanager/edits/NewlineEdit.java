package com.energyxxer.trident.ui.editor.behavior.editmanager.edits;

import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.trident.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.trident.ui.editor.behavior.caret.Dot;
import com.energyxxer.trident.ui.editor.behavior.caret.EditorCaret;
import com.energyxxer.trident.ui.editor.behavior.editmanager.Edit;
import com.energyxxer.util.StringUtil;
import com.energyxxer.util.logger.Debug;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.ArrayList;

/**
 * Created by User on 3/20/2017.
 */
public class NewlineEdit extends Edit {
    private CaretProfile previousProfile;
    private CaretProfile nextProfile;
    private ArrayList<Integer> modificationIndices = new ArrayList<>();
    private ArrayList<String> previousValues = new ArrayList<>();
    private ArrayList<String> nextValues = new ArrayList<>();

    private final boolean pushCaret;

    public NewlineEdit(AdvancedEditor editor) {
        this(editor, true);
    }

    public NewlineEdit(AdvancedEditor editor, boolean pushCaret) {
        this.previousProfile = editor.getCaret().getProfile();
        this.pushCaret = pushCaret;
    }

    @Override
    public boolean redo(AdvancedEditor editor) {
        Document doc = editor.getDocument();
        EditorCaret caret = editor.getCaret();

        modificationIndices.clear();
        previousValues.clear();
        nextValues.clear();

        boolean actionPerformed = false;

        nextProfile = (pushCaret) ? new CaretProfile() : new CaretProfile(previousProfile);

        try {
            String text = doc.getText(0, doc.getLength()); //Result

            int characterDrift = 0;

            for(int i = 0; i < previousProfile.size() - 1; i += 2) {
                int start = Math.min(previousProfile.get(i), previousProfile.get(i+1)) + characterDrift;
                int end = Math.max(previousProfile.get(i), previousProfile.get(i+1)) + characterDrift;

                String str = "\n";



                String beforeCaret = text.substring(0, start - characterDrift).trim();
                String afterCaret = text.substring(start - characterDrift);
                char beforeCaretChar = '\0';
                if(!beforeCaret.isEmpty()) {
                    beforeCaretChar = beforeCaret.charAt(beforeCaret.length()-1);
                }
                char afterCaretChar = '\0';
                for(char c : afterCaret.toCharArray()) {
                    if(!Character.isWhitespace(c) || c == '\n') {
                        afterCaretChar = c;
                        break;
                    }
                }
                String placeAfterCaret = "";
                int tabs = editor.getIndentationLevelAt(start - characterDrift);

                if(Dot.SMART_KEYS_INDENT.get() && !afterCaret.isEmpty()) {
                    if("}])".indexOf(afterCaretChar) == "{[(".indexOf(beforeCaretChar) && "}])".indexOf(afterCaretChar) >= 0) {
                        placeAfterCaret = "\n" + StringUtil.repeat("    ", Math.max(tabs-1, 0));
                    } else if("}])".indexOf(afterCaret.charAt(0)) >= 0) {
                        tabs--;
                    }
                } else {
                    tabs = 0;
                }

                Debug.log(beforeCaretChar + "|" + afterCaretChar);

                str += StringUtil.repeat("    ", tabs);
                str += placeAfterCaret;

                modificationIndices.add(start);
                previousValues.add(text.substring(start - characterDrift, end - characterDrift));
                nextValues.add(str);

                doc.remove(start, end-start);
                doc.insertString(start, str, null);
                actionPerformed = true;

                if(pushCaret) {
                    int dot = start + str.length() - placeAfterCaret.length();
                    nextProfile.add(dot, dot);
                } else nextProfile.pushFrom(start+1, str.length());
                characterDrift += (end - start) + (tabs * 4) + 1;

                int ftabs = tabs;

                editor.registerCharacterDrift(o -> (o >= start) ? ((o <= end) ? start + (ftabs * 4) + 1 : o + (ftabs * 4) + 1 - (end - start)) : o);

            }
        } catch(BadLocationException x) {
            x.printStackTrace();
        }

        caret.setProfile(nextProfile);
        return actionPerformed;
    }

    @Override
    public boolean undo(AdvancedEditor editor) {
        Document doc = editor.getDocument();
        EditorCaret caret = editor.getCaret();

        try {
            for(int i = modificationIndices.size()-1; i >= 0; i--) {
                int start = modificationIndices.get(i);
                doc.remove(start, nextValues.get(i).length());
                doc.insertString(start, previousValues.get(i), null);

                final int fnlen = nextValues.get(i).length();
                final int fplen = previousValues.get(i).length();

                editor.registerCharacterDrift(o -> (o >= start) ? ((o <= start + fnlen) ? start + fplen : o - fnlen + fplen) : o);
            }
        } catch(BadLocationException x) {
            x.printStackTrace();
        }

        caret.setProfile(previousProfile);
        return true;
    }
}
