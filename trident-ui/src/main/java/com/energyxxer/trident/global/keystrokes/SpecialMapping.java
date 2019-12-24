package com.energyxxer.trident.global.keystrokes;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Objects;

public class SpecialMapping implements UserMapping {
    private UserKeyBind.Special special;

    public SpecialMapping(UserKeyBind.Special special) {
        this.special = special;
    }

    @Override
    public boolean wasPerformed(KeyEvent e) {
        return false;
    }

    @Override
    public boolean wasPerformedExact(KeyEvent e) {
        return false;
    }

    @Override
    public String getHumanReadableName() {
        return special.getHumanReadableKeystroke();
    }

    @Override
    public void apply(InputMap inputMap, Object actionMapKey) {
        throw new UnsupportedOperationException();
    }

    public UserKeyBind.Special getSpecial() {
        return special;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpecialMapping that = (SpecialMapping) o;
        return special == that.special;
    }

    @Override
    public int hashCode() {
        return Objects.hash(special);
    }
}
