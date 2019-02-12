package com.energyxxer.xswing;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public interface UnifiedDocumentListener extends DocumentListener {
    void anyUpdate(DocumentEvent e);

    @Override
    default void insertUpdate(DocumentEvent e) {
        anyUpdate(e);
    }

    @Override
    default void removeUpdate(DocumentEvent e) {
        anyUpdate(e);
    }

    @Override
    default void changedUpdate(DocumentEvent e) {
        anyUpdate(e);
    }
}
