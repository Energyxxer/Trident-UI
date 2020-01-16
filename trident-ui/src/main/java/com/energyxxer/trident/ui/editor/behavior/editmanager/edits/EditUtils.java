package com.energyxxer.trident.ui.editor.behavior.editmanager.edits;

import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.trident.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.trident.ui.editor.behavior.caret.Dot;

import java.util.List;

public class EditUtils {
    public static class Configuration {
        public boolean fetchStart = true;
        public boolean fetchEnd = true;
        public boolean fetchContentStart = false;
        public boolean includeNewline = false;
        public boolean includeLastLine = false;
        public boolean mergeLinesPerSelection = false;
        public LineFetchHandler lineHandler = null;
    }

    public static void fetchSelectedLines(CaretProfile profile, AdvancedEditor editor, List<Integer> lines, Configuration config) {
        int previousL = -1;

        for(int i = 0; i < profile.size()-1; i+= 2) {
            int selectionStart = profile.get(i);
            int selectionEnd = profile.get(i + 1);

            int startLine = new Dot(Math.min(selectionStart,selectionEnd),editor).getRowStart();
            int endLine = new Dot(Math.max(selectionStart,selectionEnd),editor).getRowEnd();

            if(config.mergeLinesPerSelection) {
                if(config.fetchContentStart) {
                    startLine = new Dot(startLine, editor).getRowContentStart();
                }
                if(config.includeNewline) {
                    int newEndLine = new Dot(endLine, editor).getPositionAfter();
                    if(newEndLine == endLine) { // last line
                        if(config.includeLastLine && startLine == endLine) {
                            startLine = new Dot(startLine, editor).getPositionBefore();
                        }
                    }
                    endLine = newEndLine;
                }
                if(config.fetchStart) lines.add(startLine);
                if(config.fetchEnd) lines.add(endLine);
                if(config.lineHandler != null) config.lineHandler.accept(startLine, endLine, i>>1);
                continue;
            }

            boolean firstLineOfSelection = true;

            for(int l = startLine; l <= endLine; previousL = l, l = new Dot(l, editor).getPositionBelow()) {
                if(l == previousL) {
                    if(firstLineOfSelection) {
                        firstLineOfSelection = false;
                        continue;
                    } else break;
                }
                firstLineOfSelection = false;
                int lineStart = l;
                if(config.fetchContentStart) {
                    lineStart = new Dot(lineStart, editor).getRowContentStart();
                }
                int lineEnd = new Dot(l, editor).getRowEnd();
                if(config.includeNewline) {
                    int newLineEnd = new Dot(lineEnd, editor).getPositionAfter();
                    if(newLineEnd == lineEnd) { // last line
                        if(config.includeLastLine) {
                            lineStart = new Dot(lineStart, editor).getPositionBefore();
                        }
                    }
                    lineEnd = newLineEnd;
                }
                if(config.fetchStart) lines.add(lineStart);
                if(config.fetchEnd) lines.add(lineEnd);

                if(config.lineHandler != null) config.lineHandler.accept(lineStart, lineEnd, i>>1);
            }
        }
    }

    public interface LineFetchHandler {
        void accept(int start, int end, int selectionIndex);
    }
}
