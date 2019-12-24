package com.energyxxer.trident.ui.editor.folding;

import java.util.List;

public interface FoldableSection {
    int getSectionStartIndex();
    int getFoldedLength();
    default int getUnfoldedLength() {
        int length = getFoldedLength();
        for(FoldableSection section : getSubSections()) {
            length += section.getUnfoldedLength();
        }
        return length;
    }

    default String getUnfoldedText() {
        StringBuilder sb = new StringBuilder(getFoldedText());
        int carriedOffset = 0;
        for(FoldableSection section : getSubSections()) {
            sb.insert(section.getSectionStartIndex()+carriedOffset, section.getUnfoldedText()); //should be the same as unfolded
            carriedOffset += section.getUnfoldedLength();
        }
        return sb.toString();
    }
    String getFoldedText();

    List<FoldableSection> getSubSections();

    void unfold();
    void unfold(int sectionListIndex);
    default boolean intersectsRange(int start, int end) {
        return !(getSectionStartIndex() >= end || getSectionStartIndex()+getUnfoldedLength() <= start);
    }
    default boolean containsRange(int start, int end) {
        return intersectsRange(start, end) && start >= getSectionStartIndex() && end <= getSectionStartIndex()+getUnfoldedLength();
    }

    void offsetStartIndex(int off);
}
