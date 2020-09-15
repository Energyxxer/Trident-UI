package com.energyxxer.trident.ui.explorer;

import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.trident.ui.explorer.base.ExplorerFlag;
import com.energyxxer.trident.ui.explorer.base.ExplorerMaster;
import com.energyxxer.trident.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.trident.ui.modules.FileModuleToken;
import com.energyxxer.trident.ui.modules.ModuleToken;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import static com.energyxxer.trident.ui.editor.behavior.AdvancedEditor.isPlatformControlDown;

/**
 * Created by User on 5/16/2017.
 */
public class NoticeGroupElement extends ExplorerElement {
    private String label;
    private List<Notice> notices;

    private int x;

    private Image icon = null;
    public int indentation = 0;

    public NoticeGroupElement(ExplorerMaster master, String label, List<Notice> notices) {
        super(master);
        this.label = label;
        this.notices = notices;

        if(notices.size() > 0 && notices.get(0).getFilePath() != null) {
            this.icon = new FileModuleToken(new File(notices.get(0).getFilePath())).getIcon();
        }

        this.x = master.getInitialIndent() + (indentation * master.getIndentPerLevel());
    }

    @Override
    public void render(Graphics g) {
        int y = master.getOffsetY();
        master.addToFlatList(this);

        int x = master.getInitialIndent();

        g.setColor((this.rollover || this.selected) ? master.getColors().get("item.rollover.background") : master.getColors().get("item.background"));
        g.fillRect(0, master.getOffsetY(), master.getWidth(), master.getRowHeight());
        if(this.selected) {
            g.setColor(master.getColors().get("item.selected.background"));

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

        //Expand/Collapse button
        {
            int margin = ((master.getRowHeight() - 16) / 2);
            if(expanded) {
                g.drawImage(master.getAssetMap().get("collapse"),x,y + margin,16, 16,null);
            } else {
                g.drawImage(master.getAssetMap().get("expand"),x,y + margin,16, 16,null);
            }
        }
        x += 23;

        //File Icon
        {
            int margin = ((master.getRowHeight() - 16) / 2);
            g.drawImage(this.icon,x,y + margin,16, 16,null);
        }
        x += 25;

        //File Name

        if(this.selected) {
            g.setColor(master.getColors().get("item.selected.foreground"));
        } else if(this.rollover) {
            g.setColor(master.getColors().get("item.rollover.foreground"));
        } else {
            g.setColor(master.getColors().get("item.foreground"));
        }

        Font originalFont = g.getFont();

        g.setFont(g.getFont().deriveFont(Font.BOLD));

        FontMetrics metrics = g.getFontMetrics(g.getFont());

        g.drawString(label, x, master.getOffsetY() + metrics.getAscent() + ((master.getRowHeight() - metrics.getHeight())/2));
        x += metrics.stringWidth(label);

        if(master.getFlag(ExplorerFlag.DEBUG_WIDTH)) {
            g.setColor(Color.YELLOW);
            g.fillRect(master.getContentWidth()-2, master.getOffsetY(), 2, master.getRowHeight());
            g.setColor(Color.GREEN);
            g.fillRect(x-2, master.getOffsetY(), 2, master.getRowHeight());
        }

        g.setFont(originalFont);

        master.setContentWidth(Math.max(master.getContentWidth(), x));
        master.renderOffset(this.getHeight());
        for(ExplorerElement i : children) {
            i.render(g);
        }
    }

    private void expand() {
        if(expanded) return;
        for(Notice n : notices) {
            children.add(new NoticeItem(this, n));
        }
        expanded = true;

        ///master.getExpandedElements().add(this.label);
        master.repaint();
    }

    @Override
    public void interact() {
        if(expanded) collapse();
        else expand();
    }

    private void collapse() {
        this.propagateCollapse();
        this.children.clear();
        expanded = false;
        master.repaint();
    }

    private void propagateCollapse() {
        master.getExpandedElements().remove(this.getToken());
        for(ExplorerElement element : children) {
            if(element instanceof NoticeGroupElement) ((NoticeGroupElement) element).propagateCollapse();
        }
    }

    @Override
    public ModuleToken getToken() {
        return null;
    }

    @Override
    public int getHeight() {
        return master.getRowHeight();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1 && !isPlatformControlDown(e) && e.getClickCount() % 2 == 0 && (e.getX() < x || e.getX() > x + master.getRowHeight())) {
            if(expanded) collapse();
            else expand();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1 && e.getX() >= x && e.getX() <= x + 20) {
            if(expanded) collapse();
            else expand();
        } else {
            master.setSelected(this, e);
        }
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
}
