package com.energyxxer.trident.ui.styledcomponents;

import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.HintStylizer;
import com.energyxxer.util.Constant;
import com.energyxxer.xswing.hints.Hint;
import com.energyxxer.xswing.hints.TextHint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class ButtonHintHandler implements MouseMotionListener {
    private String hintText;
    private AbstractButton button;

    private Constant preferredHintPos = Hint.BELOW;

    public ButtonHintHandler(String hintText, AbstractButton button) {
        this(hintText, button, Hint.BELOW);
    }

    public ButtonHintHandler(String hintText, AbstractButton button, Constant preferredHintPos) {
        this.hintText = hintText;
        this.button = button;
        this.preferredHintPos = preferredHintPos;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        TextHint hint = TridentWindow.toolbar.hint;
        if(!hint.isShowing()) {
            hint.setText(hintText);
            hint.setPreferredPos(this.preferredHintPos);
            Point point = button.getLocationOnScreen();
            point.x += button.getWidth()/2;
            point.y += button.getHeight()/2;
            HintStylizer.style(hint);
            hint.show(point, () -> button.isShowing() && button.getModel().isRollover());
        }
    }
}
