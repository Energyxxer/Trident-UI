package com.energyxxer.trident.ui.explorer.base.elements;

import com.energyxxer.trident.ui.explorer.base.ExplorerMaster;
import com.energyxxer.trident.ui.modules.ModuleToken;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

/**
 * Created by User on 4/8/2017.
 */
public abstract class ExplorerElement implements MouseListener, MouseMotionListener {
    protected final ExplorerMaster master;
    protected boolean selected;
    protected boolean rollover;
    protected boolean expanded;

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

    public ExplorerMaster getMaster() {
        return master;
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
