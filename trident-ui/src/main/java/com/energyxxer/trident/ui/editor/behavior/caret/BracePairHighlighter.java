package com.energyxxer.trident.ui.editor.behavior.caret;

import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class BracePairHighlighter implements Highlighter.HighlightPainter {

    private final AdvancedEditor editor;

    private boolean shouldRender = false;
    private Rectangle nearRectangle = null;
    private Rectangle farRectangle = null;

    private int prevSeenDot = -1;

    public BracePairHighlighter(AdvancedEditor editor, EditorCaret caret) {
        this.editor = editor;

        caret.addCaretPaintListener(() -> {
            int dot = editor.getCaret().getDot();
            if(prevSeenDot != dot) {
                prevSeenDot = dot;
                updateRectangles();
                editor.repaint();
            }
        });
    }

    private void updateRectangles() {
        shouldRender = false;
        if(editor.getCaret().getDots().size() != 1 || !editor.getCaret().getDots().get(0).isPoint()) {
            prevSeenDot = -1;
            return;
        }
        try {
            int dot = editor.getCaret().getDot();
            int afterIndex = editor.getNextNonWhitespace(dot);
            char afterChar = '\0';
            if(afterIndex < editor.getDocument().getLength()) {
                afterChar = editor.getDocument().getText(afterIndex, 1).charAt(0);
            }

            int braceCheckIndex = afterIndex;

            if(!editor.getIndentationManager().isBrace(afterChar)) {
                int beforeIndex = editor.getPreviousNonWhitespace(dot);
                char beforeChar = '\0';
                if(beforeIndex >= 0 && beforeIndex < editor.getDocument().getLength()) {
                    beforeChar = editor.getDocument().getText(beforeIndex, 1).charAt(0);
                }
                if(!editor.getIndentationManager().isBrace(beforeChar)) return;
                braceCheckIndex = beforeIndex;
            }

            int matchingIndex = editor.getIndentationManager().getMatchingBraceIndex(braceCheckIndex);

            if(matchingIndex == -1) return;

            nearRectangle = editor.modelToView(braceCheckIndex);
            if(nearRectangle == null) return;
            nearRectangle.width = editor.modelToView(braceCheckIndex+1).x-nearRectangle.x;

            farRectangle = editor.modelToView(matchingIndex);
            farRectangle.width = editor.modelToView(matchingIndex+1).x-farRectangle.x;
            shouldRender = true;

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
        if(!shouldRender) return;
        if(c instanceof AdvancedEditor) {
            g.setColor(editor.getBraceHighlightColor());

            g.fillRect(nearRectangle.x, nearRectangle.y, nearRectangle.width, nearRectangle.height);
            g.fillRect(farRectangle.x, farRectangle.y, farRectangle.width, farRectangle.height);
        }
    }
}
