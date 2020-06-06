package com.energyxxer.trident.ui.explorer;

import com.energyxxer.enxlex.report.StackTrace;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.explorer.base.ExplorerFlag;
import com.energyxxer.trident.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.trident.ui.modules.FileModuleToken;
import com.energyxxer.trident.ui.modules.ModuleToken;

import java.awt.*;
import java.awt.event.MouseEvent;

import static com.energyxxer.trident.ui.editor.behavior.AdvancedEditor.isPlatformControlDown;

public class NoticeStackTraceItem extends ExplorerElement {
    private NoticeItem parent;
    private StackTrace.StackTraceElement traceElement;

    private int x;
    private String message;
    private int lineCount;

    public NoticeStackTraceItem(NoticeItem parent, StackTrace.StackTraceElement traceElement) {
        super(parent.getMaster());
        this.parent = parent;
        this.traceElement = traceElement;

        this.x = (parent.x) + master.getIndentPerLevel();
        this.message = traceElement.toString();
        this.lineCount = message.split("\n",-1).length;
    }

    @Override
    public void render(Graphics g) {
        g.setFont(master.getFont());
        int y = master.getOffsetY();
        master.addToFlatList(this);

        int x = this.x + 23;

        g.setColor((this.rollover || parent.isRollover() || this.selected) ? master.getColors().get("item.rollover.background") : master.getColors().get("item.background"));
        g.fillRect(0, master.getOffsetY(), master.getWidth(), master.getRowHeight() * lineCount);
        if(this.selected) {
            g.setColor(master.getColors().get("item.selected.background"));

            switch(master.getSelectionStyle()) {
                case "FULL": {
                    g.fillRect(0, master.getOffsetY(), master.getWidth(), master.getRowHeight() * lineCount);
                    break;
                }
                case "LINE_LEFT": {
                    g.fillRect(0, master.getOffsetY(), master.getSelectionLineThickness(), master.getRowHeight() * lineCount);
                    break;
                }
                case "LINE_RIGHT": {
                    g.fillRect(master.getWidth() - master.getSelectionLineThickness(), master.getOffsetY(), master.getSelectionLineThickness(), master.getRowHeight() * lineCount);
                    break;
                }
                case "LINE_TOP": {
                    g.fillRect(0, master.getOffsetY(), master.getWidth(), master.getSelectionLineThickness());
                    break;
                }
                case "LINE_BOTTOM": {
                    g.fillRect(0, master.getOffsetY() + master.getRowHeight() * lineCount - master.getSelectionLineThickness(), master.getWidth(), master.getSelectionLineThickness());
                    break;
                }
            }
        }

        //Icon (blank)
        x += 25;

        //Message

        if(this.selected) {
            g.setColor(master.getColors().get("item.selected.foreground"));
        } else if(this.rollover) {
            g.setColor(master.getColors().get("item.rollover.foreground"));
        } else {
            g.setColor(master.getColors().get("item.foreground"));
        }
        FontMetrics metrics = g.getFontMetrics(g.getFont());

        int extraLength = 0;

        for(String line : message.split("\n")) {
            g.drawString(line, x, y + metrics.getAscent() + ((master.getRowHeight() - metrics.getHeight())/2));
            y += master.getRowHeight();

            extraLength = Math.max(extraLength, metrics.stringWidth(line));
        }
        x += extraLength;

        if(master.getFlag(ExplorerFlag.DEBUG_WIDTH)) {
            g.setColor(Color.YELLOW);
            g.fillRect(master.getContentWidth()-2, master.getOffsetY(), 2, master.getRowHeight());
            g.setColor(Color.GREEN);
            g.fillRect(x-2, master.getOffsetY(), 2, master.getRowHeight() * lineCount);
        }

        master.renderOffset(this.getHeight());

        master.setContentWidth(Math.max(master.getContentWidth(), x));

        for(ExplorerElement i : children) {
            i.render(g);
        }
    }

    @Override
    public ModuleToken getToken() {
        return null;
    }

    @Override
    public int getHeight() {
        return master.getRowHeight() * lineCount;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1 && !isPlatformControlDown(e) && e.getClickCount() % 2 == 0 && traceElement.getPattern().getFile() != null) {
            interact();
        }
    }

    @Override
    public void interact() {
        TridentWindow.tabManager.openTab(new FileModuleToken(traceElement.getPattern().getFile()), traceElement.getPattern().getStringLocation().index, traceElement.getPattern().getStringBounds().end.index - traceElement.getPattern().getStringBounds().start.index);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        master.setSelected(this, e);
    }
}
