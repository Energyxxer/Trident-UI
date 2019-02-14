package com.energyxxer.trident.main.window.sections.search_path;

import com.energyxxer.trident.global.TabManager;
import com.energyxxer.trident.ui.explorer.base.ExplorerFlag;
import com.energyxxer.trident.ui.explorer.base.ExplorerMaster;
import com.energyxxer.trident.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.trident.ui.modules.FileModuleToken;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.util.ImageUtil;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

import static com.energyxxer.trident.ui.editor.behavior.AdvancedEditor.isPlatformControlDown;

public class SearchPathItem extends ExplorerElement {

    private FileModuleToken token;
    private String preview;

    private Image icon = null;

    private int x = 0;

    private final int substringOffset;
    private int start, length, line;

    public SearchPathItem(File file, String preview, int substringOffset, int start, int length, int line, ExplorerMaster master, ArrayList<Object> toOpen) {
        super(master);
        this.token = new FileModuleToken(file);

        int trimStart = 0;
        while(substringOffset > 0 && trimStart < preview.length() && Character.isWhitespace(preview.charAt(trimStart))) {
            trimStart++;
            substringOffset--;
        }
        preview = preview.substring(trimStart);

        this.preview = preview;

        this.substringOffset = substringOffset;
        this.start = start;
        this.length = length;
        this.line = line;

        this.icon = token.getIcon();
        if(this.icon != null) this.icon = ImageUtil.fitToSize(this.icon, 16, 16);
    }

    @Override
    public void render(Graphics g) {
        g.setFont(master.getFont());
        int y = master.getOffsetY();
        master.getFlatList().add(this);

        int x = master.getInitialIndent();

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

        //Expand/Collapse button
        if(token.isExpandable()){
            int margin = ((master.getRowHeight() - 16) / 2);
            if(expanded) {
                g.drawImage(master.getAssetMap().get("collapse"),x,y + margin,16, 16,new Color(0,0,0,0),null);
            } else {
                g.drawImage(master.getAssetMap().get("expand"),x,y + margin,16, 16,new Color(0,0,0,0),null);
            }
        }
        x += 23;

        //File Icon
        int margin = ((master.getRowHeight() - 16) / 2);

        //File Name
        FontMetrics metrics = g.getFontMetrics(g.getFont());

        g.setColor(Color.YELLOW.darker());
        g.setColor(master.getColorMap().get("item.find.background"));
        g.fillRect(x+metrics.stringWidth(preview.substring(0, substringOffset)), master.getOffsetY() + metrics.getAscent()/2 - 2, metrics.stringWidth(preview.substring(substringOffset, substringOffset+length)), metrics.getHeight());

        if(this.selected) {
            g.setColor(master.getColorMap().get("item.selected.foreground"));
        } else if(this.rollover) {
            g.setColor(master.getColorMap().get("item.rollover.foreground"));
        } else {
            g.setColor(master.getColorMap().get("item.foreground"));
        }

        Graphics2D g2d = (Graphics2D) g;
        Composite oldComposite = g2d.getComposite();

        g.drawString(preview, x, master.getOffsetY() + metrics.getAscent() + ((master.getRowHeight() - metrics.getHeight())/2));
        x += metrics.stringWidth(preview);

        g2d.setComposite(oldComposite);

        if(master.getFlag(ExplorerFlag.DEBUG_WIDTH)) {
            g.setColor(Color.YELLOW);
            g.fillRect(master.getContentWidth()-2, master.getOffsetY(), 2, master.getRowHeight());
            g.setColor(Color.GREEN);
            g.fillRect(x-2, master.getOffsetY(), 2, master.getRowHeight());
        }

        File file = token.getFile();
        String fileName = file.getName();
        if(!rollover || master.getWidth() - metrics.stringWidth(fileName) - 24 - 16 - 8 >= x) {
            if(this.icon != null) {
                int projectNameX = master.getWidth() - metrics.stringWidth(fileName) - 24;
                g.drawImage(this.icon, projectNameX - 16 - 8, y + margin + 8 - 8, null);
                g.drawString(fileName, projectNameX, master.getOffsetY() + metrics.getAscent() + ((master.getRowHeight() - metrics.getHeight())/2));
            }

            /*String subTitle = token.getSubTitle();
            if(subTitle != null) {
                g.setColor(new Color(g.getColor().getRed(), g.getColor().getGreen(), g.getColor().getBlue(), (int)(g.getColor().getAlpha() * 0.75)));
                x += 8;
                g.drawString(subTitle, x, master.getOffsetY() + metrics.getAscent() + ((master.getRowHeight() - metrics.getHeight())/2));
                x += metrics.stringWidth(subTitle);
            }*/
        }

        master.setOffsetY(master.getOffsetY() + master.getRowHeight());
        master.setContentWidth(Math.max(master.getContentWidth(), x));
        for(ExplorerElement i : children) {
            i.render(g);
        }
    }

    @Override
    public ModuleToken getToken() {
        return token;
    }

    @Override
    public int getHeight() {
        return master.getRowHeight();
    }

    private void open() {
        this.token.onInteract();
        TabManager.openTab(token, start, length);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1 && !isPlatformControlDown(e) && e.getClickCount() % 2 == 0 && (!token.isExpandable() || e.getX() < x || e.getX() > x + master.getRowHeight())) {
            this.open();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            master.setSelected(this, e);
            SearchPathDialog.INSTANCE.showEditor(token.getFile(), start, length);
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
