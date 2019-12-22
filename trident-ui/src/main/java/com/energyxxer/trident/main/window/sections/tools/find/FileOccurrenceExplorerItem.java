package com.energyxxer.trident.main.window.sections.tools.find;

import com.energyxxer.trident.ui.explorer.base.ExplorerFlag;
import com.energyxxer.trident.ui.explorer.base.ExplorerMaster;
import com.energyxxer.trident.ui.explorer.base.StandardExplorerItem;
import com.energyxxer.trident.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.trident.util.ImageUtil;

import java.awt.*;
import java.io.File;

public class FileOccurrenceExplorerItem extends StandardExplorerItem {
    private FileOccurrence occurrence;
    private Image icon = null;

    public FileOccurrenceExplorerItem(FileOccurrence occurrence, StandardExplorerItem parent) {
        super(occurrence, parent, null);
        this.setDetailed(false);
        this.occurrence = occurrence;

        this.icon = occurrence.getIcon();
        if(this.icon != null) this.icon = ImageUtil.fitToSize(this.icon, 16, 16);
    }

    public FileOccurrenceExplorerItem(FileOccurrence occurrence, ExplorerMaster master) {
        super(occurrence, master, null);
        this.setDetailed(false);
        this.occurrence = occurrence;

        this.icon = occurrence.getIcon();
        if(this.icon != null) this.icon = ImageUtil.fitToSize(this.icon, 16, 16);
    }

    @Override
    public void render(Graphics g) {
        g.setFont(master.getFont());
        int y = master.getOffsetY();
        master.addToFlatList(this);

        this.x = (master.getIndentation() * master.getIndentPerLevel()) + master.getInitialIndent();
        int x = this.x;

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
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int margin = ((master.getRowHeight() - 16) / 2);

        if(!isDetailed()) {
            if (this.selected) {
                g.setColor(master.getColors().get("item.selected.foreground"));
            } else if (this.rollover) {
                g.setColor(master.getColors().get("item.rollover.foreground"));
            } else {
                g.setColor(master.getColors().get("item.foreground"));
            }

            String subTitle = occurrence.lineNum + "";
            g.setColor(new Color(g.getColor().getRed(), g.getColor().getGreen(), g.getColor().getBlue(), (int) (g.getColor().getAlpha() * 0.75)));
            g.drawString(subTitle, x, master.getOffsetY() + metrics.getAscent() + ((master.getRowHeight() - metrics.getHeight()) / 2));
            x += metrics.stringWidth(subTitle);
            x += 8;
        }

        //File Name

        if(!(master instanceof FindExplorerFilter) || ((FindExplorerFilter) master).highlightResult()) {
            g.setColor(master.getColors().get("item.find.background"));
            g.fillRect(x+metrics.stringWidth(occurrence.linePreview.substring(0, occurrence.previewOffset)), master.getOffsetY() + metrics.getAscent()/2 - 2, metrics.stringWidth(occurrence.linePreview.substring(occurrence.previewOffset, occurrence.previewOffset+occurrence.length)), metrics.getHeight());
        }

        if(this.selected) {
            g.setColor(master.getColors().get("item.selected.foreground"));
        } else if(this.rollover) {
            g.setColor(master.getColors().get("item.rollover.foreground"));
        } else {
            g.setColor(master.getColors().get("item.foreground"));
        }

        g.drawString(occurrence.linePreview, x, master.getOffsetY() + metrics.getAscent() + ((master.getRowHeight() - metrics.getHeight())/2));
        x += metrics.stringWidth(occurrence.linePreview);

        if(master.getFlag(ExplorerFlag.DEBUG_WIDTH)) {
            g.setColor(Color.YELLOW);
            g.fillRect(master.getContentWidth()-2, master.getOffsetY(), 2, master.getRowHeight());
            g.setColor(Color.GREEN);
            g.fillRect(x-2, master.getOffsetY(), 2, master.getRowHeight());
        }

        if(isDetailed() || (master instanceof FindExplorerFilter && !((FindExplorerFilter) master).groupByFile())) {
            File file = occurrence.file;
            String fileName = file.getName();
            fileName += " " + occurrence.lineNum;
            if (!rollover || master.getWidth() - metrics.stringWidth(fileName) - 24 - 16 - 8 >= x) {
                if (this.icon != null) {
                    int projectNameX = master.getWidth() - metrics.stringWidth(fileName) - 24;
                    g.drawImage(this.icon, projectNameX - 16 - 8, y + margin + 8 - 8, null);
                    g.drawString(fileName, projectNameX, master.getOffsetY() + metrics.getAscent() + ((master.getRowHeight() - metrics.getHeight()) / 2));
                }
            }
        }

        master.setContentWidth(Math.max(master.getContentWidth(), x));
        master.pushIndentation();
        master.renderOffset(this.getHeight());
        for(ExplorerElement i : children) {
            i.render(g);
        }
        master.popIndentation();
    }

    @Override
    public FileOccurrence getToken() {
        return occurrence;
    }

    @Override
    public int getHeight() {
        return master.getRowHeight();
    }
}
