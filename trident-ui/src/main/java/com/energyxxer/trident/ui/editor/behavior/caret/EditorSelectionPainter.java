package com.energyxxer.trident.ui.editor.behavior.caret;

import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.util.StringBounds;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

import static com.energyxxer.trident.ui.editor.behavior.caret.DragSelectMode.CHAR;
import static com.energyxxer.trident.ui.editor.behavior.caret.DragSelectMode.RECTANGLE;

/**
 * Created by User on 1/9/2017.
 */
public class EditorSelectionPainter implements Highlighter.HighlightPainter {

    private EditorCaret caret;

    public EditorSelectionPainter(EditorCaret caret) {
        this.caret = caret;
    }

    public static Collection<Rectangle> getRectanglesForBounds(AdvancedEditor editor, StringBounds bounds) throws BadLocationException {
        ArrayList<Rectangle> rects = new ArrayList<>();
        boolean midDrawn = false;

        for (int l = bounds.start.line; l <= bounds.end.line; l++) {
            Rectangle rectangle = null;
            if (l == bounds.start.line) {
                rectangle = editor.modelToView(bounds.start.index);
                if (bounds.start.line == bounds.end.line) {
                    rectangle.width = editor.modelToView(bounds.end.index).x - rectangle.x;
                } else {
                    rectangle.width = editor.getWidth() - rectangle.x;
                }
            } else if (l == bounds.end.line) {
                rectangle = editor.modelToView(bounds.end.index);
                rectangle.width = rectangle.x - editor.modelToView(0).x;
                rectangle.x = editor.modelToView(0).x; //0
            } else if(!midDrawn) {
                rectangle = editor.modelToView(bounds.start.index);
                rectangle.x = editor.modelToView(0).x; //0
                rectangle.y += rectangle.height;
                rectangle.height = editor.modelToView(bounds.end.index).y - rectangle.y;
                rectangle.width = editor.getWidth();
                midDrawn = true;
                l = bounds.end.line-1;
            }

            if(rectangle != null) {
                if(rectangle.width < 0) {
                    rectangle.x += rectangle.width;
                    rectangle.width *= -1;
                }
                rectangle.width = Math.abs(rectangle.width);
                rects.add(rectangle);
            }
        }
        return rects;
    }

    @Override
    public void paint(Graphics g, int p0, int p1, Shape graphicBounds, JTextComponent c) {
        AdvancedEditor editor = (AdvancedEditor) c;
        g.setColor(editor.hasFocus() ? editor.getSelectionColor() : editor.getSelectionUnfocusedColor());

        ArrayList<Dot> dots = caret.getDots();

        int dotIndex = 0;
        for(Dot dot : dots) {
            boolean shouldPaint = !(caret.dragSelectMode == RECTANGLE && dot == caret.bufferedDot) && !(caret.dragSelectMode == CHAR && dotIndex >= caret.rectangleDotsStartIndex);
            if(shouldPaint) try {
                StringBounds bounds = dot.getBounds();

                boolean midDrawn = false;

                for (int l = bounds.start.line; l <= bounds.end.line; l++) {
                    Rectangle rectangle = null;
                    if (l == bounds.start.line) {
                        rectangle = editor.modelToView(bounds.start.index);
                        if (bounds.start.line == bounds.end.line) {
                            rectangle.width = editor.modelToView(bounds.end.index).x - rectangle.x;
                        } else {
                            rectangle.width = editor.getWidth() - rectangle.x;
                        }
                    } else if (l == bounds.end.line) {
                        rectangle = editor.modelToView(bounds.end.index);
                        rectangle.width = rectangle.x - editor.modelToView(0).x;
                        rectangle.x = editor.modelToView(0).x; //0
                    } else if(!midDrawn) {
                        rectangle = editor.modelToView(bounds.start.index);
                        rectangle.x = editor.modelToView(0).x; //0
                        rectangle.y += rectangle.height;
                        rectangle.height = editor.modelToView(bounds.end.index).y - rectangle.y;
                        rectangle.width = editor.getWidth();
                        midDrawn = true;
                        l = bounds.end.line-1;
                    }

                    if(rectangle != null) {
                        if(rectangle.width < 0) {
                            rectangle.x += rectangle.width;
                            rectangle.width *= -1;
                        }
                        rectangle.width = Math.abs(rectangle.width);
                        g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
                    }
                }
                dotIndex++;
            } catch (BadLocationException e) {
                //Can't render
            }
        }
    }
}
