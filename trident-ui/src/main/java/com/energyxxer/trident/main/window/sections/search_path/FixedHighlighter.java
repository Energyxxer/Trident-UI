package com.energyxxer.trident.main.window.sections.search_path;

import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.util.StringBounds;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;

public class FixedHighlighter implements Highlighter.HighlightPainter {

    private final AdvancedEditor editor;

    private Color highlightColor;
    private Color highlightBorderColor;

    private ArrayList<Integer> regions = new ArrayList<>();

    private boolean enabled = true;

    public FixedHighlighter(AdvancedEditor editor) {
        this.editor = editor;
    }

    public void addRegion(int start, int end) {
        regions.add(start);
        regions.add(end);
    }

    public void clear() {
        regions.clear();
    }

    @Override
    public void paint(Graphics g, int p0, int p1, Shape graphicBounds, JTextComponent c) {
        if(!enabled) return;

        for(int i = 0; i < regions.size() - 1; i += 2) {
            int start = regions.get(i);
            int end = regions.get(i+1);

            try {
                StringBounds bounds = new StringBounds(editor.getLocationForOffset(start),editor.getLocationForOffset(end));

                for (int l = bounds.start.line; l <= bounds.end.line; l++) {
                    Rectangle rectangle;
                    if (l == bounds.start.line) {
                        rectangle = c.modelToView(bounds.start.index);
                        if (bounds.start.line == bounds.end.line) {
                            rectangle.width = c.modelToView(bounds.end.index).x - rectangle.x;
                        } else {
                            rectangle.width = c.getWidth() - rectangle.x;
                        }
                    } else if (l == bounds.end.line) {
                        rectangle = c.modelToView(bounds.end.index);
                        rectangle.width = rectangle.x - c.modelToView(0).x;
                        rectangle.x = c.modelToView(0).x; //0
                    } else {
                        rectangle = c.modelToView(bounds.start.index);
                        rectangle.x = c.modelToView(0).x; //0
                        rectangle.y += rectangle.height * (l - bounds.start.line);
                        rectangle.width = c.getWidth();
                    }

                    if(rectangle.width < 0) {
                        rectangle.x += rectangle.width;
                        rectangle.width *= -1;
                    }
                    rectangle.width = Math.abs(rectangle.width);

                    g.setColor(highlightColor);
                    g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);

                    g.setColor(highlightBorderColor);
                    g.fillRect(rectangle.x, rectangle.y, 1, rectangle.height-1);
                    g.fillRect(rectangle.x, rectangle.y + rectangle.height - 1, rectangle.width-1, 1);
                    g.fillRect(rectangle.x + rectangle.width - 1, rectangle.y+1, 1, rectangle.height-1);
                    g.fillRect(rectangle.x+1, rectangle.y, rectangle.width-1, 1);
                }
            } catch (BadLocationException e) {
                //Can't render
            }
        }
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }

    public Color getHighlightBorderColor() {
        return highlightBorderColor;
    }

    public void setHighlightBorderColor(Color highlightBorderColor) {
        this.highlightBorderColor = highlightBorderColor;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
