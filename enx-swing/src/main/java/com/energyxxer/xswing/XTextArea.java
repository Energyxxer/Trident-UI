package com.energyxxer.xswing;

import javax.swing.*;
import java.awt.*;

public class XTextArea extends JTextArea {

    private int borderThickness = 1;

    {
        setBackground(new Color(225,225,225));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(150,150,150)), BorderFactory.createEmptyBorder(0, 5, 0, 5)));
    }

    public XTextArea() {
        super();
    }

    public XTextArea(String text) {
        super(text);
    }

    public void setBorder(Color bc, int thickness) {
        this.borderThickness = thickness;
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bc, thickness), BorderFactory.createEmptyBorder(0, 5, 0, 5)));
    }

    public void setForeground(Color fg) {
        this.setCaretColor(fg);
        super.setForeground(fg);
    }

    public int getBorderThickness() {
        return borderThickness;
    }
}
