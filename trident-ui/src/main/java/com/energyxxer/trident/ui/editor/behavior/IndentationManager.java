package com.energyxxer.trident.ui.editor.behavior;

import java.util.ArrayList;
import java.util.Stack;

public class IndentationManager {
    public static final String NULLIFY_BRACE_STYLE = "__INDENTATION_CANCEL";
    protected final AdvancedEditor editor;
    protected boolean dirty = false;
    protected String text;
    private final String openingChars = "{[(";
    private final String closingChars = "}])";

    protected ArrayList<IndentationChange> indents = new ArrayList<>();
    private Stack<Integer> bracesSeen = new Stack<>();

    public IndentationManager(AdvancedEditor editor) {
        this.editor = editor;

        editor.getStyledDocument().addStyle(NULLIFY_BRACE_STYLE, null);
    }

    public void textChanged(String newText) {
        this.text = newText;
        dirty = true;
        indents.clear();
        bracesSeen.empty();
    }

    public int getSuggestedIndentationLevelAt(int index) {
        populate();

        int level = 0;
        for(IndentationChange indent : indents) {
            if(editor.getStyledDocument().getCharacterElement(indent.index).getAttributes().containsAttributes(editor.getStyle(NULLIFY_BRACE_STYLE))) continue;
            if(index <= indent.index) {
                return level;
            }
            level += indent.change;
        }
        return level;
    }

    private void populate() {
        if(!dirty) return;
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int openingIndex = openingChars.indexOf(c);
            int closingIndex = closingChars.indexOf(c);

            if(openingIndex >= 0) {
                bracesSeen.push(openingIndex);
                indents.add(new IndentationChange(i, +1));
            } else if(closingIndex >= 0) {
                int matchingBraceIndex = closingIndex;
                if(!bracesSeen.isEmpty()) {
                    matchingBraceIndex = bracesSeen.pop();
                }
                if(matchingBraceIndex == closingIndex) indents.add(new IndentationChange(i, -1));
            }
        }
        dirty = false;
    }

    public boolean isBalanced() {
        return getSuggestedIndentationLevelAt(text.length()) == 0;
    }

    public boolean match(char opening, char closing) {
        return isOpeningBrace(opening) && openingChars.indexOf(opening) == closingChars.indexOf(closing);
    }

    public boolean isOpeningBrace(String str) {
        return str.length() == 1 && isOpeningBrace(str.charAt(0));
    }

    public boolean isOpeningBrace(char ch) {
        return openingChars.indexOf(ch) >= 0;
    }

    public boolean isClosingBrace(String str) {
        return str.length() == 1 && isClosingBrace(str.charAt(0));
    }

    public boolean isClosingBrace(char ch) {
        return closingChars.indexOf(ch) >= 0;
    }

    public char getMatchingBraceChar(String str) {
        return getMatchingBraceChar(str.charAt(0));
    }

    public char getMatchingBraceChar(char ch) {
        if(isOpeningBrace(ch)) return closingChars.charAt(openingChars.indexOf(ch));
        return openingChars.charAt(closingChars.indexOf(ch));
    }

    public boolean isBrace(char ch) {
        return isOpeningBrace(ch) || isClosingBrace(ch);
    }

    private int binarySearchBraceIndex(int index) {
        int min = 0;
        int max = indents.size()-1;
        while(true) {

            if(min >= max) {
                if(!indents.isEmpty() && indents.get(min).index == index) return min;
                return -1;
            }

            int pivotIndex = (min + max) / 2;
            IndentationChange pivot = indents.get(pivotIndex);
            if(index == pivot.index) return pivotIndex;
            else if(index < pivot.index) {
                max = pivotIndex-1;
            } else {
                min = pivotIndex+1;
            }
        }
    }

    public int getMatchingBraceIndex(int braceCheckIndex) {
        populate();
        int indentIndex = binarySearchBraceIndex(braceCheckIndex);
        if(indentIndex == -1) return -1;
        IndentationChange brace = indents.get(indentIndex);
        int level = 1;
        for(int i = indentIndex + brace.change; i >= 0 && i < indents.size(); i += brace.change) {
            IndentationChange next = indents.get(i);
            if(editor.getStyledDocument().getCharacterElement(next.index).getAttributes().containsAttributes(editor.getStyle(NULLIFY_BRACE_STYLE))) continue;
            level += next.change * brace.change;
            if(level == 0) return next.index;
        }
        return -1;
    }

    private static class IndentationChange {
        int index;
        int change;

        public IndentationChange(int index, int change) {
            this.index = index;
            this.change = change;
        }

        @Override
        public String toString() {
            return "IndentationChange{" +
                    "index=" + index +
                    ", change=" + change +
                    '}';
        }
    }
}
