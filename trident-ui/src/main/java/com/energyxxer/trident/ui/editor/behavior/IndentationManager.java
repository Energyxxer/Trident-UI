package com.energyxxer.trident.ui.editor.behavior;

import com.energyxxer.util.logger.Debug;

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

    public void textChanged(String newText, int offset) {
        this.text = newText;
        dirty = true;
        indents.clear();
        bracesSeen.empty();
    }

    public int getSuggestedIndentationLevelAt(int index) {
        populate();

        Debug.log(indents);

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

    public void disableRegion(int start, int end) {
        populate();
        for(IndentationChange indent : indents) {
            if(start <= indent.index && indent.index <= end) {
                indent.enabled = false;
            }
            if(indent.index > end) return;
        }
    }

    private static class IndentationChange {
        int index;
        int change = 0;
        boolean enabled = true;

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
