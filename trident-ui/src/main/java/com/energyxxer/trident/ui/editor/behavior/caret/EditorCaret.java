package com.energyxxer.trident.ui.editor.behavior.caret;

import com.energyxxer.trident.compiler.util.Using;
import com.energyxxer.trident.testing.LiveTestCase;
import com.energyxxer.trident.testing.LiveTestManager;
import com.energyxxer.trident.testing.LiveTestResult;
import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.trident.ui.editor.behavior.editmanager.edits.CompoundEdit;
import com.energyxxer.trident.ui.editor.behavior.editmanager.edits.DeletionEdit;
import com.energyxxer.trident.ui.editor.behavior.editmanager.edits.DragInsertionEdit;
import com.energyxxer.trident.ui.editor.behavior.editmanager.edits.SetCaretProfileEdit;
import com.energyxxer.trident.util.Range;
import com.energyxxer.util.Lazy;
import com.energyxxer.util.StringLocation;
import com.energyxxer.util.logger.Debug;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Highlighter;
import javax.swing.text.Utilities;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import static com.energyxxer.trident.ui.editor.behavior.caret.DragSelectMode.CHAR;
import static com.energyxxer.trident.ui.editor.behavior.caret.DragSelectMode.RECTANGLE;

/**
 * Created by User on 1/3/2017.
 */
public class EditorCaret extends DefaultCaret implements DropTargetListener {

    static {
        LiveTestManager.registerTestCase(new LiveTestCase(
                "caret_duplication",
                EditorCaret.class,
                "Editor Caret Duplication",
                "Tests whether running the method EditorCaret#removeDuplicates is ever needed." +
                        " If this test never receives any positive results, that method will be removed."));
    }

    private ArrayList<Dot> dots = new ArrayList<>();
    private AdvancedEditor editor;

    private static Highlighter.HighlightPainter nullHighlighter = (g,p0,p1,bounds,c) -> {};

    private int dropLocation = -1;

    public EditorCaret(AdvancedEditor c) {
        this.editor = c;
        this.setBlinkRate(500);
        this.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        c.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleEvent(e);
            }
        });

        c.setDropTarget(new DropTarget(c, TransferHandler.COPY_OR_MOVE, this));

        bufferedDot = new Dot(0, 0, editor);
        addDot(bufferedDot);

        try {
            c.getHighlighter().addHighlight(0,0,new EditorSelectionPainter(this));
        } catch(BadLocationException x) {
            Debug.log(x.getMessage(), Debug.MessageType.ERROR);
        }
    }

    private void handleEvent(KeyEvent e) {
        if(e.isConsumed()) return;
        boolean actionPerformed = false;
        for(Dot dot : dots) {
            if(dot.handleEvent(e)) actionPerformed = true;
        }
        if(actionPerformed) {
            mergeDots();
            removeDuplicates();
            update();
        }
    }

    private void update() {
        Using.using(editor.getSuggestionInterface()).notIfNull().run(
                d -> d.dismiss(false)
        );
        editor.repaint();
        this.setVisible(true);
        readjustRect();
        this.fireStateChanged();
    }

    public void setPosition(int pos) {
        dots.clear();
        bufferedDot = new Dot(pos, editor);
        addDot(bufferedDot);
        update();
    }

    public void addDot(int... pos) {
        for(int dot : pos) {
            Dot newDot = new Dot(dot, editor);
            if(!dots.contains(newDot)) dots.add(newDot);
        }
    }

    public void addDot(Dot... newDots) {
        for(Dot dot : newDots) {
            dots.add(dot);
            mergeDots(dot);
            if(dot == bufferedDot) bufferedDotAdded = true;
        }
    }

    public void mergeDots(Dot newDot) {
        if(!dots.contains(newDot)) {
            Debug.log("DOTS DO NOT CONTAIN NEWDOT, DOT WAS NOT ADDED");
        }
        boolean hasMerged = true;
        while(hasMerged) {
            hasMerged = false;
            Iterator<Dot> it = dots.iterator();
            while(it.hasNext()) {
                Dot dot = it.next();
                if(dot != newDot && dot.intersects(newDot)) {
                    newDot.absorb(dot);
                    hasMerged = true;
                    it.remove();
                }
            }
        }
    }

    private void mergeDots() {
        for(int i = 0; i < dots.size(); i++) {
            Dot dot = dots.get(i);
            for(int j = i+1; j < dots.size(); j++) {
                Dot otherDot = dots.get(j);
                if(dot.intersects(otherDot)) {
                    dot.absorb(otherDot);
                    dots.remove(j);
                    j--;
                }
            }
        }
    }

    public void removeDuplicates() {
        boolean stateChanged = false;
        for(int i = 0; i < dots.size(); i++) {
            Dot d = dots.get(i);
            if(dots.indexOf(d) != i) {
                dots.remove(i);
                i--;
                stateChanged = true;
            }
        }
        if(stateChanged) {
            this.fireStateChanged();
            LiveTestManager.getTestCase("caret_duplication").submitResult(new LiveTestResult(LiveTestResult.ResultType.POSITIVE));
        }
    }

    @Override
    public void paint(Graphics g) {
        caretPaintListeners.forEach(Runnable::run);
        try {
            g.setColor(getComponent().getCaretColor());
            int paintWidth = 2;

            TextUI mapper = getComponent().getUI();

            ArrayList<Dot> allDots = new ArrayList<>(dots);

            int dotIndex = 0;
            for (Dot dot : allDots) {
                Rectangle r = mapper.modelToView(getComponent(), dot.index, getDotBias());

                boolean shouldPaint = !(dragSelectMode == RECTANGLE && dot == bufferedDot) && !(dragSelectMode == CHAR && dotIndex >= rectangleDotsStartIndex);

                if(isVisible() && shouldPaint) {
                    r.x -= paintWidth >> 1;
                    g.fillRect(r.x, r.y, paintWidth, r.height);
                }
                else {
                    getComponent().repaint(r);
                }
                dotIndex++;
            }

            if(dropLocation >= 0) {
                Rectangle r = mapper.modelToView(getComponent(), dropLocation, getDotBias());
                r.x -= paintWidth >> 1;
                g.fillRect(r.x, r.y, paintWidth, r.height);
            }
        } catch (BadLocationException x) {
            Debug.log(x.getMessage(), Debug.MessageType.ERROR);
        }
    }

    private void readjustRect() {
        try {
            ArrayList<Dot> allDots = new ArrayList<>(dots);

            Rectangle unionRect = null;

            for (Dot dot : allDots) {
                Rectangle r = editor.modelToView(dot.index);
                if(unionRect == null) unionRect = r; else unionRect = unionRect.union(r);
            }

            if(dropLocation >= 0) {
                Rectangle r = editor.modelToView(dropLocation);
                if(unionRect == null) unionRect = r; else unionRect = unionRect.union(r);
            }

            if(unionRect != null) {
                x = unionRect.x - 1;
                y = unionRect.y;
                width = unionRect.width + 3;
                height = unionRect.height;
            }
        } catch (BadLocationException x) {
            Debug.log(x.getMessage(), Debug.MessageType.ERROR);
        }
    }

    @Override
    protected synchronized void damage(Rectangle rect) {
        readjustRect();
        repaint();
    }

    public String getSelectionInfo() {
        StringBuilder s = new StringBuilder(" ");

        CaretProfile profile = this.getProfile();
        int selectedCharCount = profile.getSelectedCharCount();
        if(selectedCharCount > 0) {
            s.append(selectedCharCount).append(" chars");

            int selectedLineCount = profile.getSelectedLineCount(editor);
            if(selectedLineCount > 1) {
                s.append(", ").append(selectedLineCount).append(" lines");
            }
        }
        return s.toString();
    }

    public String getCaretInfo() {
        if(dots.size() > 1) {
            return dots.size() + " carets";
        } else {
            StringLocation loc = editor.getModelLocationForOffset(dots.get(0).index);
            return loc.line + ":" + loc.column;
        }
    }

    public void moveBy(int offset) {
        pushFrom(0, offset);
    }

    public void pushFrom(int pos, int offset) {
        int docLength = editor.getDocument().getLength();

        for(Dot dot : dots) {
            if(dot.index >= pos) {
                dot.index = Math.min(docLength, Math.max(0, dot.index + offset));
            }
            if(dot.mark >= pos) {
                dot.mark = Math.min(docLength, Math.max(0, dot.mark + offset));
            }
        }

        update();
    }

    public void deselect() {
        int newDotPos = getDot();
        dots.clear();
        addDot(newDotPos);
        dragSelectMode = CHAR;
        rectangleDotsStartIndex = 1;
        rectangleDotCursorIndex = 1;
        rectangleStartPoint = null;
        bufferedDot = null;
        bufferedDotAdded = false;
        update();
    }

    @Override
    public int getDot() {
        if(dragSelectMode == RECTANGLE) return dots.get(Math.min(rectangleDotCursorIndex, dots.size()-1)).index;
        int upperBound = dots.size()-1;
        if(dragSelectMode == CHAR) upperBound = rectangleDotsStartIndex -1;
        return dots.get(Math.min(upperBound, dots.size()-1)).index;
    }

    public ArrayList<Dot> getDots() {
        return new ArrayList<>(this.dots);
    }

    public CaretProfile getProfile() {
        if(bufferedDot != null && bufferedDotAdded) mergeDots(bufferedDot);
        CaretProfile profile = new CaretProfile();
        profile.addAllDots(dots);
        profile.sort();
        return profile;
    }

    @Override
    protected Highlighter.HighlightPainter getSelectionPainter() {
        return nullHighlighter;
    }

    public void setProfile(CaretProfile profile) {
        this.dots.clear();
        Range r = new Range(0,editor.getDocument().getLength());
        for(int i = 0; i < profile.size()-1; i += 2) {
            Dot newDot = new Dot(
                    editor.getFoldableDocument().modelIndexToView(r.clamp(profile.get(i))),
                    editor.getFoldableDocument().modelIndexToView(r.clamp(profile.get(i+1))),
                    editor
            );
            if(i == 0) bufferedDot = newDot;
            addDot(newDot);
        }
        removeDuplicates();
        update();
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    //Handle mouse selection

    Dot bufferedDot = null;
    private boolean bufferedDotAdded = false;
    DragSelectMode dragSelectMode = DragSelectMode.CHAR;
    int rectangleDotsStartIndex = 1;
    private int rectangleDotCursorIndex = 1;
    private Point rectangleStartPoint = null;

    private boolean clickStartInSelection = false;
    private boolean clickStartedMouse2 = false;

    private boolean isInSelection(int index) {
        for(Dot dot : dots) {
            if(dot.contains(index)) return true;
        }
        return false;
    }

    private Dot getSelectionAroundIndex(int index) {
        for(Dot dot : dots) {
            if(dot.contains(index)) return dot;
        }
        return null;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        editor.requestFocus();
        transferData = null;
        transferFromDot = null;
        int index = editor.viewToModel(e.getPoint());

        clickStartInSelection = isInSelection(index);

        bufferedDot = new Dot(index, index, editor);
        bufferedDotAdded = false;
        dragSelectMode = DragSelectMode.CHAR;
        clickStartedMouse2 = e.getButton() == MouseEvent.BUTTON2;

        if(e.getButton() == MouseEvent.BUTTON2 || (e.getButton() == MouseEvent.BUTTON1 && e.isAltDown() && !e.isShiftDown())) {
            Debug.log("Rectangle start");
            Debug.log(e.getButton() == MouseEvent.BUTTON2);
            clickStartInSelection = false;
            if(!e.isAltDown() || !e.isShiftDown()) {
                dots.clear();
            }

            dragSelectMode = RECTANGLE;
        } else if(e.getClickCount() == 2 && !e.isConsumed() && e.getButton() == MouseEvent.BUTTON1) {
            bufferedDot.mark = bufferedDot.getWordStart();
            bufferedDot.index = bufferedDot.getWordEnd();
            dragSelectMode = DragSelectMode.WORD;
        } else if(e.getClickCount() >= 3 && !e.isConsumed() && e.getButton() == MouseEvent.BUTTON1) {
            clickStartInSelection = false;

            bufferedDot.mark = bufferedDot.getRowStart();
            bufferedDot.index = bufferedDot.getRowEnd();
            dragSelectMode = DragSelectMode.LINE;
        }
        if(!clickStartInSelection) {
            if(!e.isAltDown() || !e.isShiftDown()) {
                dots.clear();
            }
            addDot(bufferedDot);
            bufferedDotAdded = true;
        } else if(dragSelectMode != DragSelectMode.CHAR) {
            dragSelectMode = DragSelectMode.CHAR;
            bufferedDot = null;
            bufferedDotAdded = false;
        }

        rectangleStartPoint = e.getPoint();
        rectangleDotsStartIndex = dots.size();
        rectangleDotCursorIndex = dots.size();

        e.consume();
        update();
    }

    private String transferData = null;
    private Dot transferFromDot = null;

    @Override
    public void mouseReleased(MouseEvent e) {
        if(dragSelectMode != RECTANGLE) {
            while(dots.size() > rectangleDotsStartIndex) {
                dots.remove(rectangleDotsStartIndex);
            }
        } else if(bufferedDot != null) {
            dots.remove(bufferedDot);
            rectangleDotCursorIndex--;
            mergeDots();
            bufferedDot = null;
            bufferedDotAdded = false;
        }

        if(bufferedDot != null) {
            if(bufferedDotAdded) mergeDots(bufferedDot);
            else if(clickStartInSelection && bufferedDot.isPoint() && transferData == null) {
                dots.clear();
                addDot(bufferedDot);
            }
            bufferedDot = null;
            bufferedDotAdded = false;
        }
        editor.repaint();
        this.setVisible(true);
        readjustRect();
        e.consume();
    }

    private void adjustFocus() {
        if ((editor != null) && editor.isEnabled() &&
                editor.isRequestFocusEnabled()) {
            editor.requestFocus();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(clickStartInSelection) { //DRAG TEXT
            if(editor.getTransferHandler() != null && transferData == null) {
                Dot selected = getSelectionAroundIndex(editor.viewToModel(e.getPoint()));
                if(selected != null) {
                    try {
                        editor.getTransferHandler().exportAsDrag(editor, e, TransferHandler.MOVE);
                        transferData = editor.getDocument().getText(selected.getMin(), selected.getMax() - selected.getMin());
                        transferFromDot = selected;
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } else if(bufferedDot != null) {
            bufferedDot.index = editor.viewToModel(e.getPoint());
            try {
                switch(dragSelectMode) {
                    case WORD: {
                        if(bufferedDot.index <= bufferedDot.mark) {
                            bufferedDot.index = bufferedDot.getWordStart();
                            bufferedDot.mark = editor.getWordEnd(bufferedDot.mark);
                        } else {
                            bufferedDot.index = bufferedDot.getWordEnd();
                            bufferedDot.mark = editor.getWordStart(bufferedDot.mark);
                        }
                        bufferedDot.updateX();
                        break;
                    }
                    case LINE: {
                        if(bufferedDot.index <= bufferedDot.mark) {
                            bufferedDot.index = bufferedDot.getRowStart();
                            bufferedDot.mark = Utilities.getRowEnd(editor, bufferedDot.mark);
                        } else {
                            bufferedDot.index = Utilities.getRowEnd(editor, bufferedDot.index);
                            bufferedDot.mark = Utilities.getRowStart(editor, bufferedDot.mark);
                        }
                        bufferedDot.updateX();
                        break;
                    }
                    case RECTANGLE: {
                        bufferedDotAdded = false;
                        rectangleDotCursorIndex = Math.max(0, rectangleDotsStartIndex -1);
                        while(dots.size() > rectangleDotsStartIndex) {
                            dots.remove(rectangleDotsStartIndex);
                        }
                        int rowHeight = editor.modelToView(bufferedDot.index).height;
                        int leftX = Math.min(e.getPoint().x, rectangleStartPoint.x);
                        int rightX = Math.max(e.getPoint().x, rectangleStartPoint.x);

                        int topY = (Math.min(e.getPoint().y, rectangleStartPoint.y) / rowHeight) * rowHeight;
                        int bottomY = (Math.max(e.getPoint().y, rectangleStartPoint.y) / rowHeight) * rowHeight;

                        boolean rtl = e.getPoint().x < rectangleStartPoint.x;
                        boolean topDown = e.getPoint().y >= rectangleStartPoint.y;

                        boolean hasUnselectedChars = false;
                        int selectedCharListIndex = -1;

                        for(int y = topY; y <= bottomY; y += rowHeight) {
                            int rowStartIndex = editor.viewToModel(new Point(leftX, y));
                            int rowEndIndex = editor.viewToModel(new Point(rightX, y));

                            if(rowStartIndex != rowEndIndex) {
                                if(selectedCharListIndex < 0) {
                                    selectedCharListIndex = dots.size();
                                }
                            } else if(selectedCharListIndex < 0) {
                                hasUnselectedChars = true;
                            }

                            if(selectedCharListIndex < 0 || rowStartIndex != rowEndIndex) dots.add(new Dot(rtl ? rowStartIndex : rowEndIndex, rtl ? rowEndIndex : rowStartIndex, editor));
                        }

                        if(hasUnselectedChars && selectedCharListIndex >= 0) {
                            for(int i = rectangleDotsStartIndex; i < selectedCharListIndex; i++) {
                                if(dots.get(i).isPoint()) {
                                    dots.remove(i);
                                    i--;
                                    selectedCharListIndex--;
                                }
                            }
                        }
                        if((e.isAltDown() == clickStartedMouse2) || e.isShiftDown()) {
                            dragSelectMode = DragSelectMode.CHAR;
                        }

                        if(topDown) {
                            rectangleDotCursorIndex = dots.size()-1;
                        } else {
                            rectangleDotCursorIndex = rectangleDotsStartIndex;
                        }
                        break;
                    }
                    case CHAR: {
                        bufferedDot.updateX();
                        if((e.isAltDown() != clickStartedMouse2) && !e.isShiftDown()) {
                            dragSelectMode = RECTANGLE;
                            rectangleDotCursorIndex = dots.size()-1;
                        }
                        break;
                    }
                }
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
        update();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
    }

    @Override
    public void dragEnter(DropTargetDragEvent e) {
        dragOver(e);
    }

    @Override
    public void dragOver(DropTargetDragEvent e) {
        if(e.getTransferable().isDataFlavorSupported(DataFlavor.stringFlavor) && editor.isEditable()) {
            e.acceptDrag(e.getDropAction());

            dropLocation = editor.viewToModel(e.getLocation());
            readjustRect();
            editor.repaint();
        } else {
            dropLocation = -1;
            if(editor.getTransferHandler() == null || !editor.getTransferHandler().canImport(new TransferHandler.TransferSupport(editor, e.getTransferable()))) {
                e.rejectDrag();
            }
        }
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent e) {

    }

    @Override
    public void dragExit(DropTargetEvent e) {
        dropLocation = -1;
    }

    @Override
    public void drop(DropTargetDropEvent e) {
        if(!editor.isEditable()) return;
        Debug.log(e);
        e.acceptDrop(TransferHandler.COPY_OR_MOVE);
        if(!e.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            Debug.log("Redirected to edit area");
            if(editor.getTransferHandler() != null) editor.getTransferHandler().importData(new TransferHandler.TransferSupport(editor, e.getTransferable()));
        } else {
            Debug.log("Is string");
            try {
                dropLocation = editor.viewToModel(e.getLocation());

                if(dropLocation >= 0) {
                    Transferable t = e.getTransferable();
                    String text = (String) t.getTransferData(DataFlavor.stringFlavor);

                    Dot dropDot = getSelectionAroundIndex(dropLocation);
                    if(dropDot == null) dropDot = new Dot(dropLocation, dropLocation, editor);

                    boolean isMoveOperation = e.getDropAction() == TransferHandler.MOVE && e.isLocalTransfer() && transferFromDot != null;

                    if (!isMoveOperation || transferFromDot.getMin() != dropDot.getMin() || transferFromDot.getMax() != dropDot.getMax()) {
                        CompoundEdit edit = new CompoundEdit();

                        if(isMoveOperation) { //Move operation
                            Debug.log(transferFromDot);
                            CaretProfile deletionProfile = new CaretProfile(transferFromDot.mark, transferFromDot.index);
                            edit.appendEdit(new Lazy<>(() -> new SetCaretProfileEdit(deletionProfile, editor)));
                            edit.appendEdit(new Lazy<>(() -> new DeletionEdit(editor)));
                            int transferFromLength = transferFromDot.getMax() - transferFromDot.getMin();
                            if(dropDot.mark > transferFromDot.getMin()) dropDot.mark = Math.max(transferFromDot.getMin(), dropDot.mark - transferFromLength);
                            if(dropDot.index > transferFromDot.getMin()) dropDot.index = Math.max(transferFromDot.getMin(), dropDot.index - transferFromLength);
                        }

                        CaretProfile newProfile = new CaretProfile(dropDot.mark, dropDot.index);

                        edit.appendEdit(new Lazy<>(() -> new SetCaretProfileEdit(newProfile, editor)));
                        edit.appendEdit(new Lazy<>(() -> new DragInsertionEdit(text, editor)));

                        editor.getEditManager().insertEdit(edit);
                    }
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                ex.printStackTrace();
            }
        }
        dropLocation = -1;
    }

    public String getTransferData() {
        return transferData;
    }

    private ArrayList<Runnable> caretPaintListeners = new ArrayList<>();

    public void addCaretPaintListener(@NotNull Runnable runnable) {
        caretPaintListeners.add(runnable);
    }
}
