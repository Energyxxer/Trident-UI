package com.energyxxer.trident.ui.editor.completion;

public interface SuggestionInterface {
    void dismiss(boolean force);
    void relocate();

    void lock();
}
