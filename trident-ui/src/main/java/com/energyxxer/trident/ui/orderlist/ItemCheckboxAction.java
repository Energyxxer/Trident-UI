package com.energyxxer.trident.ui.orderlist;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.ui.explorer.base.StyleProvider;

import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class ItemCheckboxAction implements ItemAction {
    protected boolean leftAligned = false;
    private boolean checked = false;

    public abstract String getDescription();
    public abstract void onChange(boolean newValue);

    private static final int buttonHGap = 6;
    private static final int buttonSize = 16;
    private static final int iconMargin = (buttonSize - 16) / 2;

    @Override
    public boolean isLeftAligned() {
        return leftAligned;
    }

    @Override
    public void render(Graphics g, ItemActionHost host, int x, int y, int w, int h, int mouseState, boolean actionEnabled) {
        StyleProvider styleProvider = host.getStyleProvider();

        int buttonVGap = (h - buttonSize) / 2;

        String styleKeyVariation = mouseState == 2 ? ".pressed" : mouseState == 1 ? ".rollover" : "";

        if(!isLeftAligned()) x -= buttonSize;

        if(actionEnabled) {
            int borderThickness = styleProvider.getStyleNumbers().get("checkbox" + styleKeyVariation + ".border.thickness");
            g.setColor(styleProvider.getColors().get("checkbox" + styleKeyVariation + ".border.color"));
            g.fillRect(x - borderThickness, y + buttonVGap - borderThickness, buttonSize + 2*borderThickness, buttonSize + 2*borderThickness);

            g.setColor(styleProvider.getColors().get("checkbox" + styleKeyVariation + ".background"));
            g.fillRect(x, y + buttonVGap, buttonSize, buttonSize);
        }
        if(checked) g.drawImage(Commons.getIcon("checkmark").getScaledInstance(16, 16, Image.SCALE_SMOOTH), x + iconMargin, y + buttonVGap + iconMargin, null);
    }

    @Override
    public int getRenderedWidth() {
        return buttonHGap + buttonSize;
    }

    @Override
    public int getHintOffset() {
        return (buttonSize / 2) + (isLeftAligned() ? 0 : buttonHGap);
    }

    @Override
    public String getHint() {
        return getDescription();
    }

    @Override
    public boolean intersects(Point p, int w, int h) {
        int buttonVGap = (h - buttonSize) / 2;

        return (p.getY() >= buttonVGap && p.getY() < buttonVGap + buttonSize) &&
                (p.getX() >= 0 && p.getX() < buttonSize);
    }

    @Override
    public void mouseReleased(MouseEvent e, ItemActionHost host) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            e.consume();
            checked = !checked;
            onChange(checked);
        }
    }

    public boolean getValue() {
        return checked;
    }

    public void setValue(boolean value) {
        checked = value;
    }
}
