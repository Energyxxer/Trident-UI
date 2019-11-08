package com.energyxxer.trident.ui.explorer.base;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.trident.ui.modules.FileModuleToken;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.ui.modules.NonStandardModuleToken;
import com.energyxxer.trident.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;

import static com.energyxxer.trident.ui.editor.behavior.AdvancedEditor.isPlatformControlDown;

public class StandardExplorerItem extends ExplorerElement {
    private ExplorerElement parent = null;

    private ModuleToken token = null;

    protected boolean expanded = false;

    private Image icon = null;

    protected int x = 0;

    private boolean detailed = false;
    private boolean translucent = false;

    private ArrayList<MouseListener> mouseListeners = new ArrayList<>();

    public StandardExplorerItem(ModuleToken token, StandardExplorerItem parent, ArrayList<String> toOpen) {
        this(parent, parent.getMaster(), token, toOpen);
    }

    public StandardExplorerItem(ModuleToken token, ExplorerMaster master, ArrayList<String> toOpen) {
        this(null, master, token, toOpen);
    }

    private StandardExplorerItem(StandardExplorerItem parent, ExplorerMaster master, ModuleToken token, ArrayList<String> toOpen) {
        super(master);
        this.parent = parent;
        if(parent != null) this.setDetailed(parent.detailed);
        this.token = token;

        this.translucent = ((token instanceof FileModuleToken) && ((FileModuleToken) token).getFile().getName().equals(".tdnproj") && FileModuleToken.isProjectRoot(((FileModuleToken) token).getFile().getParentFile()));

        this.icon = token.getIcon();
        if(this.icon != null) this.icon = ImageUtil.fitToSize(this.icon, 16, 16);

        if(toOpen.contains(this.token.getIdentifier())) {
            expand(toOpen);
        }
    }

    public void expand(ArrayList<String> toOpen) {
        for(ModuleToken subToken : token.getSubTokens()) {
            ExplorerElement inner;
            if(subToken instanceof NonStandardModuleToken) {
                inner = ((NonStandardModuleToken) subToken).createElement(this);
            } else {
                inner = new StandardExplorerItem(subToken, this, toOpen);
                ((StandardExplorerItem) inner).setDetailed(this.detailed);
            }
            this.children.add(inner);
        }
        expanded = true;
        master.getExpandedElements().add(this.token);
        master.repaint();
    }

    private void collapse() {
        this.propagateCollapse();
        this.children.clear();
        expanded = false;
        master.repaint();
    }

    private void propagateCollapse() {
        master.getExpandedElements().remove(this.token);
        for(ExplorerElement element : children) {
            if(element instanceof StandardExplorerItem) ((StandardExplorerItem) element).propagateCollapse();
        }
    }

    public void render(Graphics g) {
        g.setFont(master.getFont());
        int y = master.getOffsetY();
        master.addToFlatList(this);

        this.x = master.getIndentation() * master.getIndentPerLevel() + master.getInitialIndent();

        int x = this.x;

        g.setColor((this.rollover || this.selected) ? master.getColorMap().get("item.rollover.background") : master.getColorMap().get("item.background"));
        g.fillRect(0, master.getOffsetY(), master.getWidth(), master.getRowHeight());
        if(this.selected) {
            g.setColor(master.getColorMap().get("item.selected.background"));

            switch(master.getSelectionStyle()) {
                case "FULL": {
                    g.fillRect(0, master.getOffsetY(), master.getWidth(), master.getRowHeight());
                    break;
                }
                case "LINE_LEFT": {
                    g.fillRect(0, master.getOffsetY(), master.getSelectionLineThickness(), master.getRowHeight());
                    break;
                }
                case "LINE_RIGHT": {
                    g.fillRect(master.getWidth() - master.getSelectionLineThickness(), master.getOffsetY(), master.getSelectionLineThickness(), master.getRowHeight());
                    break;
                }
                case "LINE_TOP": {
                    g.fillRect(0, master.getOffsetY(), master.getWidth(), master.getSelectionLineThickness());
                    break;
                }
                case "LINE_BOTTOM": {
                    g.fillRect(0, master.getOffsetY() + master.getRowHeight() - master.getSelectionLineThickness(), master.getWidth(), master.getSelectionLineThickness());
                    break;
                }
            }
        }

        x += token.getDefaultXOffset();

        int margin = ((master.getRowHeight() - 16) / 2);
        //Expand/Collapse button
        if (token.isExpandable()) {
            if (expanded) {
                g.drawImage(master.getAssetMap().get("collapse"), x, y + margin, 16, 16, new Color(0, 0, 0, 0), null);
            } else {
                g.drawImage(master.getAssetMap().get("expand"), x, y + margin, 16, 16, new Color(0, 0, 0, 0), null);
            }
        }
        x += 23;

        //File Icon
        if (icon != null) {
            g.drawImage(this.icon, x + 8 - icon.getWidth(null) / 2, y + margin + 8 - icon.getHeight(null) / 2, null);
        }

        //File Name

        if(this.selected) {
            g.setColor(master.getColorMap().get("item.selected.foreground"));
        } else if(this.rollover) {
            g.setColor(master.getColorMap().get("item.rollover.foreground"));
        } else {
            g.setColor(master.getColorMap().get("item.foreground"));
        }
        FontMetrics metrics = g.getFontMetrics(g.getFont());

        Graphics2D g2d = (Graphics2D) g;
        Composite oldComposite = g2d.getComposite();

        if(translucent) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        }

        if(token.getTitle() != null) {
            x += 25;
            g.drawString(token.getTitle(), x, master.getOffsetY() + metrics.getAscent() + ((master.getRowHeight() - metrics.getHeight())/2));
            x += metrics.stringWidth(token.getTitle());
        }

        if(detailed) {
            File projectRoot = token.getAssociatedProjectRoot();
            if(projectRoot != null) {
                String projectName = projectRoot.getName();
                int projectNameX = master.getWidth() - metrics.stringWidth(projectName) - 24;
                g.drawImage(Commons.getProjectIcon(), projectNameX - 16 - 8, y + margin + 8 - 8, null);
                g.drawString(projectName, projectNameX, master.getOffsetY() + metrics.getAscent() + ((master.getRowHeight() - metrics.getHeight())/2));
            }

            String subTitle = token.getSubTitle();
            if(subTitle != null) {
                g.setColor(new Color(g.getColor().getRed(), g.getColor().getGreen(), g.getColor().getBlue(), (int)(g.getColor().getAlpha() * 0.75)));
                x += 8;
                g.drawString(subTitle, x, master.getOffsetY() + metrics.getAscent() + ((master.getRowHeight() - metrics.getHeight())/2));
                x += metrics.stringWidth(subTitle);
            }
        }

        g2d.setComposite(oldComposite);

        if(master.getFlag(ExplorerFlag.DEBUG_WIDTH)) {
            g.setColor(Color.YELLOW);
            g.fillRect(master.getContentWidth()-2, master.getOffsetY(), 2, master.getRowHeight());
            g.setColor(Color.GREEN);
            g.fillRect(x-2, master.getOffsetY(), 2, master.getRowHeight());
        }

        master.setOffsetY(master.getOffsetY() + master.getRowHeight());
        master.setContentWidth(Math.max(master.getContentWidth(), x));
        master.pushIndentation();
        for(ExplorerElement i : children) {
            i.render(g);
        }
        master.popIndentation();
    }

    private void open() {
        this.token.onInteract();
        if(token.isExpandable()) {
            if(expanded) collapse();
            else expand(new ArrayList<>());
        } else if(token.isModuleSource()) {
            TridentWindow.tabManager.openTab(token);
        }
    }

    private void confirmActivationMenu(MouseEvent e) {
        if(e.isPopupTrigger()) {
            if(!this.selected) master.setSelected(this, new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), 0, e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), MouseEvent.BUTTON1));
            JPopupMenu menu = token.generateMenu(ModuleToken.MenuContext.EXPLORER);
            if(menu != null) menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public boolean isDetailed() {
        return detailed;
    }

    public void setDetailed(boolean detailed) {
        this.detailed = detailed;
    }

    @Override
    public int getHeight() {
        return master.getRowHeight();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1 && !isPlatformControlDown(e) && e.getClickCount() % 2 == 0 && (!token.isExpandable() || e.getX() < x || e.getX() > x + master.getRowHeight())) {
            this.open();
        }
        dispatchMouseEvent(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            //x = indentation * master.getIndentPerLevel() + master.getInitialIndent();
            if(token.isExpandable() && e.getX() >= x && e.getX() <= x + master.getRowHeight()) {
                if(expanded) collapse();
                else expand(new ArrayList<>());
            } else {
                master.setSelected(this, e);
            }
        }
        confirmActivationMenu(e);
        dispatchMouseEvent(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        confirmActivationMenu(e);
        dispatchMouseEvent(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        dispatchMouseEvent(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        dispatchMouseEvent(e);
    }

    @Override
    public ModuleToken getToken() {
        return token;
    }

    public void addMouseListener(MouseListener listener) {
        mouseListeners.add(listener);
    }

    public void removeMouseListener(MouseListener listener) {
        mouseListeners.remove(listener);
    }

    protected void dispatchMouseEvent(MouseEvent e) {
        for(MouseListener listener : mouseListeners) {
            switch(e.getID()) {
                case MouseEvent.MOUSE_CLICKED:
                    listener.mouseClicked(e);
                    break;
                case MouseEvent.MOUSE_PRESSED:
                    listener.mousePressed(e);
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    listener.mousePressed(e);
                    break;
                case MouseEvent.MOUSE_ENTERED:
                    listener.mouseEntered(e);
                    break;
                case MouseEvent.MOUSE_EXITED:
                    listener.mouseExited(e);
                    break;
            }
        }
    }
}
