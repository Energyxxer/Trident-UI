package com.energyxxer.xswing;

import javax.swing.*;
import java.awt.*;

public class XToggleButton extends JCheckBox {

    protected Color borderColor = new Color(150,150,150);
    protected int borderThickness = 1;
    protected Color rolloverColor = new Color(240,240,240);
    protected Color pressedColor = new Color(175,175,175);

    protected Image toggleIcon = null;

    {
        setFocusPainted(false);
        setOpaque(false);
        setContentAreaFilled(false);
        setBackground(new Color(225,225,225));
        this.setBorderPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if(this.isSelected() || this.getModel().isRollover()) {
            g.setColor(this.getBorderColor());
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            if(this.isSelected() || this.getModel().isPressed()) {
                g.setColor(this.getPressedColor());
            } else if(this.getModel().isRollover()) {
                g.setColor(this.getRolloverColor());
            } else {
                g.setColor(this.getBackground());
            }
            g.fillRect(borderThickness, borderThickness, this.getWidth()-2* borderThickness, this.getHeight()-2* borderThickness);
        }
        Dimension iconSize = new Dimension(toggleIcon.getWidth(null), toggleIcon.getHeight(null));
        g.drawImage(toggleIcon, (this.getWidth()-iconSize.width)/2, (this.getHeight()-iconSize.height)/2, null);
    }

    public void setBorder(Color c, int thickness) {
        if(c != null) borderColor = c;
        this.borderThickness = thickness;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public int getBorderThickness() {
        return borderThickness;
    }

    public void setBorderThickness(int borderThickness) {
        this.borderThickness = borderThickness;
    }

    public Color getRolloverColor() {
        return rolloverColor;
    }

    public void setRolloverColor(Color rolloverColor) {
        this.rolloverColor = rolloverColor;
    }

    public Color getPressedColor() {
        return pressedColor;
    }

    public void setPressedColor(Color pressedColor) {
        this.pressedColor = pressedColor;
    }

    public Image getToggleIcon() {
        return toggleIcon;
    }

    public void setToggleIcon(Image toggleIcon) {
        this.toggleIcon = toggleIcon;
    }
}
