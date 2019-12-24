package com.energyxxer.trident.ui.editor.folding;

import com.energyxxer.util.logger.Debug;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Segment;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FoldableDocument extends DefaultStyledDocument implements FoldableSection {
    private ArrayList<FoldableSection> subSections = new ArrayList<>();

    // Default Document get methods should reflect the VIEW
    // Default Document edit methods should reflect the MODEL

    @Override
    public int getSectionStartIndex() {
        return 0;
    }

    @Override
    public int getFoldedLength() {
        return getLength();
    }

    @Override
    public String getFoldedText() {
        try {
            return getText(0, getLength());
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<FoldableSection> getSubSections() {
        return subSections;
    }

    @Override
    public void unfold(int sectionListIndex) {
        FoldableSection section = subSections.get(sectionListIndex);
        subSections.remove(sectionListIndex);
        for(FoldableSection subSection : section.getSubSections()) {
            subSections.add(sectionListIndex++, subSection);
        }
        try {
            super.insertString(section.getSectionStartIndex(), section.getFoldedText(), null);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        while(sectionListIndex < subSections.size()) {
            subSections.get(sectionListIndex++).offsetStartIndex(section.getFoldedLength());
        }
    }

    @Override
    public void unfold() {
        int carriedOffset = 0;
        for(FoldableSection section : subSections) {
            section.offsetStartIndex(carriedOffset);
            section.unfold();

            try {
                super.insertString(section.getSectionStartIndex(), section.getFoldedText(), null); //should be the same as unfolded
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            carriedOffset += section.getFoldedLength();
            if(section.getFoldedLength() != section.getFoldedText().length()) {
                Debug.log("MISMATCHING FOLDED LENGTH");
            }
        }
        subSections.clear();
    }

    @Override
    public void offsetStartIndex(int off) {
        throw new UnsupportedOperationException("Cannot offset the root document's index");
    }

    public void fold(int start, int end) {
        try {
            FoldableSection newSection = new DefaultFoldedSection(start, getText(start, end-start));
            super.remove(start, end-start);
            for(FoldableSection section : subSections) {
                if(section.getSectionStartIndex() >= newSection.getSectionStartIndex()+newSection.getFoldedLength()) {
                    section.offsetStartIndex(-newSection.getFoldedLength());
                }
            }
            subSections.add(newSection);
            subSections.sort(Comparator.comparing(FoldableSection::getSectionStartIndex));
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    public void unfoldRange(int start, int end) {
        for(int i = 0; i < subSections.size(); i++) {
            FoldableSection section = subSections.get(i);
            if(section.intersectsRange(start, end)) {
                this.unfold(i);
                i--;
            }
        }
    }

    @Override
    public int getLength() {
        return super.getLength();
    }

    private void drift(int from, int offset) {
        for(FoldableSection section : subSections) {
            if(section.getSectionStartIndex() >= from) {
                section.offsetStartIndex(offset);
            }
        }
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException {
        unfoldRange(offs, offs+len);
        int newOffset = modelIndexToView(offs);
        int newLength = modelIndexToView(offs+len)-newOffset;
        super.remove(newOffset, newLength);
        drift(newOffset, -newLength);
    }

    public void replace(String text) {
        subSections.clear();
        try {
            super.replace(0, super.getLength(), text, null);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        remove(offset, length);
        insertString(offset, text, null);
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        unfoldRange(offs, offs);
        super.insertString(modelIndexToView(offs), str, a);
        drift(modelIndexToView(offs), str.length());
        drift(offs, str.length());
    }

    @Override
    public String getText(int offset, int length) throws BadLocationException {
        return super.getText(offset, length);
    }

    @Override
    public void getText(int offset, int length, Segment txt) throws BadLocationException {
        super.getText(offset, length, txt);
    }

    @Override
    public void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace) {
        int start = offset;
        int end = offset+length;
        for(FoldableSection section : subSections) {
            if(start >= section.getSectionStartIndex()) {
                if (start >= section.getSectionStartIndex() + section.getUnfoldedLength()) {
                    start -= section.getUnfoldedLength();
                } else {
                    start = section.getSectionStartIndex();
                }
            }
            if(end >= section.getSectionStartIndex()) {
                if (end >= section.getSectionStartIndex() + section.getUnfoldedLength()) {
                    end -= section.getUnfoldedLength();
                } else {
                    end = section.getSectionStartIndex();
                }
            }
            if(end <= start) return;
        }
        super.setCharacterAttributes(start, end-start, s, replace);
    }

    public int viewIndexToModel(int index) {
        int carriedOffset = 0;
        for(FoldableSection section : subSections) {
            if(index >= section.getSectionStartIndex()) {
                carriedOffset += section.getUnfoldedLength();
            }
        }
        return index + carriedOffset;
    }

    public int modelIndexToView(int index) {
        for(FoldableSection section : subSections) {
            if(index >= section.getSectionStartIndex()) {
                if (index >= section.getSectionStartIndex() + section.getUnfoldedLength()) {
                    index -= section.getUnfoldedLength();
                } else {
                    index = section.getSectionStartIndex();
                }
            }
        }
        return index;
    }

    public String getModelText(int offset, int length) {
        return getUnfoldedText().substring(offset, offset+length);
    }
}
