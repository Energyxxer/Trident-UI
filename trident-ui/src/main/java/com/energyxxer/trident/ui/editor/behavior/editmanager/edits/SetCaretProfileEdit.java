package com.energyxxer.trident.ui.editor.behavior.editmanager.edits;

import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.trident.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.trident.ui.editor.behavior.editmanager.Edit;

import java.util.ArrayList;

public class SetCaretProfileEdit extends Edit {
    private CaretProfile nextProfile;
    private ArrayList<String> previousValues = new ArrayList<>();
    private CaretProfile previousProfile;

    public SetCaretProfileEdit(CaretProfile nextProfile, AdvancedEditor editor) {
        this.nextProfile = nextProfile;
        this.previousProfile = editor.getCaret().getProfile();
    }

    @Override
    public boolean redo(AdvancedEditor editor) {
        editor.getCaret().setProfile(nextProfile);
        return true;
    }

    @Override
    public boolean undo(AdvancedEditor editor) {
        editor.getCaret().setProfile(previousProfile);
        return true;
    }
}
