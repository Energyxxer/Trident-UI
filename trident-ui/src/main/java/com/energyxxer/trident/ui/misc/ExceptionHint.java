package com.energyxxer.trident.ui.misc;

import com.energyxxer.trident.main.window.actions.ActionManager;
import com.energyxxer.xswing.hints.Hint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class ExceptionHint extends Hint {
    private final JTextPane textPane;

    public ExceptionHint(JFrame owner) {
        super(owner);
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setOpaque(false);
        textPane.setBackground(new Color(0,0,0,0));
        textPane.setForeground(new Color(187, 187, 187));
        this.setContent(textPane);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        textPane.addMouseListener(this);
    }

    public ExceptionHint(JFrame owner, String text) {
        this(owner);
        setText(text);
    }

    public void setText(String text) {
        textPane.setText(text);
        textPane.invalidate();
        textPane.validate();
        this.update();
    }

    public String getText() {
        return textPane.getText();
    }

    public Color getForeground() {
        return textPane.getForeground();
    }

    public void setForeground(Color fg) {
        textPane.setForeground(fg);
    }

    public Font getFont() {
        return textPane.getFont();
    }

    public void setFont(Font font) {
        textPane.setFont(font);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        ActionManager.getAction("OPEN_CONSOLE").perform();
        updateCondition(() -> false);
        dismiss();
    }
}
