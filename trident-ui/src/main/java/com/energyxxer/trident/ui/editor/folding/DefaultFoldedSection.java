package com.energyxxer.trident.ui.editor.folding;

import java.util.ArrayList;
import java.util.List;

public class DefaultFoldedSection implements FoldableSection {
    private int sectionStartIndex;
    private String foldedText;
    private ArrayList<FoldableSection> subSections = new ArrayList<>();

    public DefaultFoldedSection(int sectionStartIndex, String foldedText) {
        this.sectionStartIndex = sectionStartIndex;
        this.foldedText = foldedText;
    }

    @Override
    public int getSectionStartIndex() {
        return sectionStartIndex;
    }

    @Override
    public int getFoldedLength() {
        return foldedText.length();
    }

    @Override
    public String getFoldedText() {
        return foldedText;
    }

    @Override
    public void unfold(int sectionListIndex) {
        FoldableSection section = subSections.get(sectionListIndex);
        subSections.remove(sectionListIndex);
        for(FoldableSection subSection : section.getSubSections()) {
            subSections.add(sectionListIndex++, subSection);
        }
        this.foldedText = foldedText.substring(0, section.getSectionStartIndex()) +
                section.getFoldedText() +
                foldedText.substring(section.getSectionStartIndex() + section.getFoldedLength());
        while(sectionListIndex < subSections.size()) {
            subSections.get(sectionListIndex++).offsetStartIndex(section.getFoldedLength());
        }
        subSections.clear();
    }

    @Override
    public List<FoldableSection> getSubSections() {
        return subSections;
    }

    @Override
    public void unfold() {
        StringBuilder sb = new StringBuilder(foldedText);
        int carriedOffset = 0;
        for(FoldableSection section : subSections) {
            section.offsetStartIndex(carriedOffset);
            section.unfold();

            sb.insert(section.getSectionStartIndex(), section.getFoldedText()); //should be the same as unfolded

            carriedOffset += section.getFoldedLength();
        }
        this.foldedText = sb.toString();
    }

    @Override
    public void offsetStartIndex(int off) {
        sectionStartIndex += off;
    }
}
