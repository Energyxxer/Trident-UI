package com.energyxxer.trident.ui.explorer.base.elements;

import com.energyxxer.trident.ui.explorer.base.ExplorerMaster;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.ui.theme.Theme;
import com.energyxxer.trident.ui.theme.change.ThemeChangeListener;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

/**
 * Created by User on 4/8/2017.
 */
public abstract class ExplorerElement implements MouseListener, MouseMotionListener, ThemeChangeListener {
    protected final ExplorerMaster master;
    protected boolean selected;
    protected boolean rollover;
    protected boolean expanded;

    protected int lastRecordedOffset = 0;

    public ExplorerElement(ExplorerMaster master) {
        this.master = master;
    }

    protected ArrayList<ExplorerElement> children = new ArrayList<>();

    public abstract void render(Graphics g);

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

    public abstract ModuleToken getToken();

    public abstract int getHeight();
    public int getRenderHeight() {
        return getHeight();
    }

    public ExplorerMaster getMaster() {
        return master;
    }

    public int getLastRecordedOffset() {
        return lastRecordedOffset;
    }




    public boolean select(MouseEvent e) {
        return false;
    }

    public String getToolTipText() {
        return null;
    }
    public Point getToolTipLocation() {
        return new Point(0, 0);
    }

    @Override
    public void themeChanged(Theme t) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
