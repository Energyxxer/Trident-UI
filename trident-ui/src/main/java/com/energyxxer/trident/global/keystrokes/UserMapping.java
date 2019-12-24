package com.energyxxer.trident.global.keystrokes;

import javax.swing.*;
import java.awt.event.KeyEvent;

public interface UserMapping {
    boolean wasPerformed(KeyEvent e);
    boolean wasPerformedExact(KeyEvent e);
    String getHumanReadableName();
    void apply(InputMap inputMap, Object actionMapKey);
}
