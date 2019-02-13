package com.energyxxer.trident.main.window.sections.editor_search;

import com.energyxxer.trident.ui.editor.TridentEditorComponent;
import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.util.StringBounds;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class SearchHighlighter implements Highlighter.HighlightPainter {

    private final FindAndReplaceBar finder;
    private final AdvancedEditor editor;


    private Color highlightColor;
    private Color selectedColor;
    private Color highlightBorderColor;
    private Color selectedBorderColor;
    private boolean enabled = true;

    public SearchHighlighter(FindAndReplaceBar finder, TridentEditorComponent editor) {
        this.finder = finder;
        this.editor = editor;
    }

    @Override
    public void paint(Graphics g, int p0, int p1, Shape graphicBounds, JTextComponent c) {
        if(!enabled) return;

        for(int i = 0; i < finder.regions.size() - 1; i += 2) {
            int start = finder.regions.get(i);
            int end = finder.regions.get(i+1);

            boolean selected = i == finder.selectedIndex;
            boolean excluded = finder.excluded.contains(start);

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

                    if(selected) {
                        g.setColor(selectedColor);
                    } else {
                        g.setColor(highlightColor);
                    }
                    if(!excluded) g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);

                    if(selected) {
                        g.setColor(selectedBorderColor);
                    } else {
                        g.setColor(highlightBorderColor);
                    }
                    if(selected || !excluded) {
                        g.fillRect(rectangle.x, rectangle.y, 1, rectangle.height-1);
                        g.fillRect(rectangle.x, rectangle.y + rectangle.height - 1, rectangle.width-1, 1);
                        g.fillRect(rectangle.x + rectangle.width - 1, rectangle.y+1, 1, rectangle.height-1);
                        g.fillRect(rectangle.x+1, rectangle.y, rectangle.width-1, 1);
                    }

                    if(excluded) {
                        g.setColor(editor.getForeground());
                        g.fillRect(rectangle.x, rectangle.y + rectangle.height/2 + 1, rectangle.width, 1);
                    }
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

    public Color getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(Color selectedColor) {
        this.selectedColor = selectedColor;
    }

    public Color getHighlightBorderColor() {
        return highlightBorderColor;
    }

    public void setHighlightBorderColor(Color highlightBorderColor) {
        this.highlightBorderColor = highlightBorderColor;
    }

    public Color getSelectedBorderColor() {
        return selectedBorderColor;
    }

    public void setSelectedBorderColor(Color selectedBorderColor) {
        this.selectedBorderColor = selectedBorderColor;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getEnabled() {
        return enabled;
    }
}
