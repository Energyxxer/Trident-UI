package com.energyxxer.trident.ui.editor.behavior.editmanager.edits;

import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.trident.ui.editor.behavior.editmanager.Edit;
import com.energyxxer.util.Lazy;

import java.util.ArrayList;

/**
 * Created by User on 1/6/2017.
 */
public class CompoundEdit extends Edit {
    private ArrayList<Lazy<Edit>> edits = new ArrayList<>();

    public CompoundEdit() {
    }

    public CompoundEdit(ArrayList<Lazy<Edit>> edits) {
        this.edits = edits;
    }

    public void appendEdit(Lazy<Edit> edit) {
        edits.add(edit);
    }

    @Override
    public boolean redo(AdvancedEditor editor) {
        boolean actionPerformed = false;
        for(Lazy<Edit> e : edits) {
            if(e.getValue().redo(editor)) actionPerformed = true;
        }
        return actionPerformed;
    }

    @Override
    public boolean undo(AdvancedEditor editor) {
        boolean actionPerformed = false;
        for(int i = edits.size()-1; i >= 0; i--) {
            Lazy<Edit> e = edits.get(i);
            if(e.getValue().undo(editor)) actionPerformed = true;
        }
        return actionPerformed;
    }
}
