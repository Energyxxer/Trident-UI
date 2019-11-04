package com.energyxxer.trident.ui.orderlist;

import com.energyxxer.trident.ui.theme.change.ThemeChangeListener;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public abstract class OrderListElement implements MouseListener, MouseMotionListener, ThemeChangeListener {
    protected final OrderListMaster master;
    protected boolean selected;
    protected boolean rollover;

    protected int lastRecordedOffset = 0;

    public OrderListElement(OrderListMaster master) {
        this.master = master;
    }

    public abstract void render(Graphics g);
    public abstract int getHeight();
    public int getLastRecordedOffset() {
        return lastRecordedOffset;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isRollover() {
        return rollover;
    }

    public void setRollover(boolean rollover) {
        this.rollover = rollover;
    }

    public abstract boolean select(MouseEvent e);

    public abstract String getToolTipText();
    public abstract Point getToolTipLocation();
}
