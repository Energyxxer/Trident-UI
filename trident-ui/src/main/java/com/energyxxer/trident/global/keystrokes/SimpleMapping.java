package com.energyxxer.trident.global.keystrokes;

import com.energyxxer.xswing.KeyInputUtils;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Objects;

import static com.energyxxer.trident.ui.editor.behavior.AdvancedEditor.isPlatformControlDown;
import static com.energyxxer.xswing.KeyInputUtils.*;

public class SimpleMapping implements UserMapping {
    private KeyStroke stroke;

    public SimpleMapping(KeyStroke stroke) {
        this.stroke = stroke;
    }

    @Override
    public boolean wasPerformed(KeyEvent e) {
        return ((stroke.getKeyCode() == e.getKeyCode()) || (e.getID() == KeyEvent.KEY_TYPED && stroke.getKeyCode() == Character.toUpperCase(e.getKeyChar()))) &&
                (!isControl(stroke) || isPlatformControlDown(e)) &&
                (!isAlt(stroke) || e.isAltDown()) &&
                (!isShift(stroke) || e.isShiftDown());
    }

    @Override
    public boolean wasPerformedExact(KeyEvent e) {
        return ((stroke.getKeyCode() == e.getKeyCode()) || (e.getID() == KeyEvent.KEY_TYPED && stroke.getKeyCode() == Character.toUpperCase(e.getKeyChar()))) &&
                (isControl(stroke) == isPlatformControlDown(e)) &&
                (isAlt(stroke) == e.isAltDown()) &&
                (isShift(stroke) == e.isShiftDown());
    }

    @Override
    public String getHumanReadableName() {
        return KeyInputUtils.getReadableKeyStroke(stroke);
    }

    @Override
    public void apply(InputMap inputMap, Object actionMapKey) {
        inputMap.put(stroke, actionMapKey);
    }

    public KeyStroke getKeyStroke() {
        return stroke;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleMapping that = (SimpleMapping) o;
        return stroke.equals(that.stroke);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stroke);
    }
}
