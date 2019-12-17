package com.energyxxer.trident.main.window.sections.editor_search;

import com.energyxxer.trident.global.Preferences;
import com.energyxxer.trident.global.keystrokes.KeyMap;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.ToolbarButton;
import com.energyxxer.trident.ui.ToolbarSeparator;
import com.energyxxer.trident.ui.editor.TridentEditorModule;
import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.trident.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.trident.ui.editor.behavior.editmanager.edits.InsertionEdit;
import com.energyxxer.trident.ui.scrollbar.OverlayScrollBarUI;
import com.energyxxer.trident.ui.scrollbar.OverlayScrollPaneLayout;
import com.energyxxer.trident.ui.styledcomponents.*;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.Disposable;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.xswing.TemporaryConfirmation;
import com.energyxxer.xswing.UnifiedDocumentListener;
import com.energyxxer.xswing.hints.TextHint;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.energyxxer.trident.ui.editor.behavior.AdvancedEditor.isPlatformControlDown;
import static java.awt.Image.SCALE_SMOOTH;

public class FindAndReplaceBar extends JPanel implements Disposable {

    private ThemeListenerManager tlm;

    private final TridentEditorModule editor;

    public TextHint hint = TridentWindow.hintManager.createTextHint("");

    private SearchHighlighter highlighter;
    ArrayList<Integer> regions = new ArrayList<>();
    ArrayList<Integer> excluded = new ArrayList<>();
    int selectedIndex = 0;

    private AdvancedEditor findField;
    private AdvancedEditor replaceField;
    private StyledLabel infoLabel;

    boolean matchCase;
    boolean wordsOnly;
    boolean regex;

    public FindAndReplaceBar(TridentEditorModule editor) {
        super(new BorderLayout());
        this.editor = editor;
        this.tlm = new ThemeListenerManager();
        this.highlighter = new SearchHighlighter(this, editor.editorComponent);
        infoLabel = new StyledLabel("", tlm);
        infoLabel.setStyle(Font.BOLD);
        //this.setPreferredSize(new Dimension(0, 60));

        matchCase = Preferences.get("editor.search.match_case","false").equals("true");
        wordsOnly = Preferences.get("editor.search.words","false").equals("true");
        regex = Preferences.get("editor.search.regex","false").equals("true");

        tlm.addThemeChangeListener(t -> {
            this.setBackground(t.getColor(new Color(235, 235, 235), "Editor.find.background"));
            this.setBorder(BorderFactory.createMatteBorder(0, 0, Math.max(t.getInteger(1,"Editor.find.border.thickness"),0), 0, t.getColor(new Color(200, 200, 200), "Editor.find.border.color")));

            this.highlighter.setHighlightColor(t.getColor(Color.GREEN, "Editor.find.highlight"));
            this.highlighter.setSelectedColor(t.getColor(Color.YELLOW, "Editor.find.selected", "Editor.find.highlight"));
            this.highlighter.setHighlightBorderColor(t.getColor(Color.YELLOW, "Editor.find.highlight.border"));
            this.highlighter.setSelectedBorderColor(t.getColor(Color.WHITE, "Editor.find.selected.border", "Editor.find.highlight.border"));
        });

        editor.editorComponent.addCharacterDriftListener(h -> {
            for(int i = 0; i < excluded.size(); i++) {
                excluded.set(i, h.apply(excluded.get(i)));
            }
        });

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        this.add(contentPanel);
        this.add(new Padding(3), BorderLayout.WEST);
        this.add(new Padding(4), BorderLayout.NORTH);
        this.add(new Padding(4), BorderLayout.SOUTH);
        this.add(new Padding(48), BorderLayout.EAST);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setOpaque(false);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        topPanel.setOpaque(false);
        topPanel.add(new StyledIcon("search_28", 23, 23, SCALE_SMOOTH));
        topPanel.add(createInput(false));
        inputPanel.add(topPanel, BorderLayout.NORTH);
        ToolbarButton prevButton = new ToolbarButton("triangle_up", tlm);
        prevButton.setHintText("Previous occurrence");
        prevButton.addActionListener(e -> onPreviousOccurrence());
        topPanel.add(prevButton);
        ToolbarButton nextButton = new ToolbarButton("triangle_down", tlm);
        nextButton.setHintText("Next occurrence");
        nextButton.addActionListener(e -> onNextOccurrence());
        topPanel.add(nextButton);
        ToolbarButton selectButton = new ToolbarButton("select", tlm);
        selectButton.setHintText("Select all occurrences");
        selectButton.addActionListener(e -> onSelectAll());
        topPanel.add(selectButton);

        topPanel.add(new ToolbarSeparator());


        {
            ToolbarButton configure = new ToolbarButton("cog_dropdown", tlm);
            configure.setHintText("Filter");

            configure.addActionListener(e -> {
                StyledPopupMenu menu = new StyledPopupMenu("What is supposed to go here?");

                {
                    StyledMenuItem item = new StyledMenuItem("Match Case", "checkmark");
                    item.setIconName(matchCase ? "checkmark" : "blank");

                    item.addActionListener(aa -> {
                        matchCase = !matchCase;
                        Preferences.put("editor.search.match_case",Boolean.toString(matchCase));
                        redoSearch();
                    });

                    menu.add(item);
                }
                {
                    StyledMenuItem item = new StyledMenuItem("Words", "checkmark");
                    item.setIconName(wordsOnly ? "checkmark" : "blank");

                    item.addActionListener(aa -> {
                        wordsOnly = !wordsOnly;
                        Preferences.put("editor.search.words",Boolean.toString(wordsOnly));
                        redoSearch();
                    });

                    menu.add(item);
                }
                {
                    StyledMenuItem item = new StyledMenuItem("Regex", "checkmark");
                    item.setIconName(regex ? "checkmark" : "blank");

                    item.addActionListener(aa -> {
                        regex = !regex;
                        Preferences.put("editor.search.regex",Boolean.toString(regex));
                        redoSearch();
                    });

                    menu.add(item);
                }

                menu.show(configure, configure.getWidth()/2, configure.getHeight());
            });

            topPanel.add(configure);
        }






        inputPanel.add(new Padding(4), BorderLayout.CENTER);


        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(new Padding(28));
        bottomPanel.add(createInput(true));
        inputPanel.add(bottomPanel, BorderLayout.SOUTH);

        ToolbarButton replaceButton = new ToolbarButton(null, tlm);
        replaceButton.setText("Replace");
        replaceButton.setHintText("Replace selected occurrence");
        replaceButton.addActionListener(e -> replaceSelected());
        bottomPanel.add(replaceButton);

        ToolbarButton replaceAllButton = new ToolbarButton(null, tlm);
        replaceAllButton.setText("Replace all");
        replaceAllButton.setHintText("Replace all occurrences");
        replaceAllButton.addActionListener(e -> replaceAll(false));
        bottomPanel.add(replaceAllButton);

        ToolbarButton replaceInSelectionButton = new ToolbarButton(null, tlm);
        replaceInSelectionButton.setText("Replace in selection");
        replaceInSelectionButton.setHintText("Replace all occurrences in the current selection");
        replaceInSelectionButton.addActionListener(e -> replaceAll(true));
        bottomPanel.add(replaceInSelectionButton);

        ToolbarButton excludeButton = new ToolbarButton(null, tlm);
        excludeButton.setText("Exclude");
        excludeButton.setHintText("Exclude selected occurrence from the Replace All operation");
        excludeButton.addActionListener(e -> excludeSelected());
        bottomPanel.add(excludeButton);

        contentPanel.add(inputPanel, BorderLayout.WEST);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlsPanel.setOpaque(false);
        controlsPanel.add(infoLabel);
        contentPanel.add(controlsPanel, BorderLayout.CENTER);

        try {
            editor.editorComponent.getHighlighter().addHighlight(0, 0, highlighter);
        } catch (BadLocationException e) {
            Debug.log(e.getMessage(), Debug.MessageType.ERROR);
        }
    }

    private void redoSearch() {
        clear();
        String toFind = findField.getText();
        if(!toFind.isEmpty()) {
            try {
                String rawPattern = findField.getText();
                if(!regex) {
                    rawPattern = Pattern.quote(rawPattern);
                }
                if(wordsOnly) {
                    rawPattern = "\\b" + rawPattern + "\\b";
                }
                Pattern pattern = Pattern.compile(rawPattern, matchCase ? 0 : Pattern.CASE_INSENSITIVE);
                Matcher m = pattern.matcher(editor.getText());
                while (m.find()) {
                    addRegion(m.start(), m.end());
                }
                if(regions.isEmpty()) {
                    infoLabel.setText("No matches");
                } else if(regions.size() == 2) {
                    infoLabel.setText("1 match");
                } else {
                    infoLabel.setText(regions.size()/2 + " matches");
                }
            } catch(PatternSyntaxException x) {
                infoLabel.setText("Regex error: " + x.getMessage().split("\n")[0]);
            }
        } else {
            infoLabel.setText("");
        }

        editor.editorComponent.repaint();
    }

    private JComponent createInput(boolean replace) {
        AdvancedEditor field = new AdvancedEditor();
        if(!replace) {
            this.findField = field;
        } else {
            this.replaceField = field;
        }
        field.setDefaultSize(new Dimension(400, 23));
        field.setSelectedLineEnabled(false);
        if(!replace) {
            field.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {

                }

                @Override
                public void keyPressed(KeyEvent e) {
                    if (KeyMap.FIND_NEXT.wasPerformedExact(e)) {
                        onNextOccurrence();
                        e.consume();
                    } else if (KeyMap.FIND_PREVIOUS.wasPerformedExact(e)) {
                        onPreviousOccurrence();
                        e.consume();
                    } else if(e.getKeyCode() == KeyEvent.VK_TAB) {
                        replaceField.requestFocus();
                        e.consume();
                    } else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        editor.hideSearchBar();
                        e.consume();
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {

                }
            });
            DocumentListener eitherChange = (UnifiedDocumentListener) e -> redoSearch();
            field.getDocument().addDocumentListener(eitherChange);
            field.getDocument().addDocumentListener((UnifiedDocumentListener) e -> excluded.clear());
            editor.editorComponent.getDocument().addDocumentListener(eitherChange);
        } else {
            field.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {

                }

                @Override
                public void keyPressed(KeyEvent e) {
                    if ((e.getKeyCode() == KeyEvent.VK_ENTER && !isPlatformControlDown(e)) && !e.isShiftDown()) {
                        replaceSelected();
                        e.consume();
                    } else if(e.getKeyCode() == KeyEvent.VK_TAB) {
                        findField.requestFocus();
                        e.consume();
                    } else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        editor.hideSearchBar();
                        e.consume();
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {

                }
            });
        }
        JScrollPane scrollPane = new JScrollPane(field);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUI(new OverlayScrollBarUI(scrollPane));
        scrollPane.getHorizontalScrollBar().setUI(new OverlayScrollBarUI(scrollPane));
        scrollPane.setLayout(new OverlayScrollPaneLayout(scrollPane));

        field.addDefaultSizeListener(preferredSize -> {
            scrollPane.setPreferredSize(preferredSize);
            this.revalidate();
        });
        return scrollPane;
    }

    void onPreviousOccurrence() {
        if(regions.isEmpty()) return;
        selectedIndex -= 2;
        if(selectedIndex == -2) {
            try {
                Rectangle rect = editor.editorComponent.modelToView((regions.get(0) + regions.get(1)) / 2);
                rect.x += editor.editorComponent.getLocationOnScreen().x + 3;
                rect.y += editor.editorComponent.getLocationOnScreen().y + 12;
                hint.setText("First match reached");
                hint.show(new Point(rect.x, rect.y), () -> selectedIndex == -2);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        } else if(selectedIndex < -2) {
            selectedIndex = regions.size()-2;
        }

        if(selectedIndex != -2) {
            editor.scrollToCenter(regions.get(selectedIndex));
            editor.editorComponent.getCaret().setProfile(new CaretProfile(regions.get(selectedIndex+1), regions.get(selectedIndex)));
        }
        editor.editorComponent.repaint();
    }

    void onNextOccurrence() {
        if(regions.isEmpty()) return;
        selectedIndex += 2;
        if (selectedIndex >= regions.size()) {
            selectedIndex = -2;
            try {
                Rectangle rect = editor.editorComponent.modelToView((regions.get(regions.size() - 2) + regions.get(regions.size() - 1)) / 2);
                rect.x += editor.editorComponent.getLocationOnScreen().x + 3;
                rect.y += editor.editorComponent.getLocationOnScreen().y + 12;
                hint.setText("Last match reached");
                hint.show(new Point(rect.x, rect.y), () -> selectedIndex == -2);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }

        if(selectedIndex != -2) {
            editor.scrollToCenter(regions.get(selectedIndex));
            editor.editorComponent.getCaret().setProfile(new CaretProfile(regions.get(selectedIndex+1), regions.get(selectedIndex)));
        }
        editor.editorComponent.repaint();
    }

    private void onSelectAll() {
        editor.editorComponent.getCaret().setProfile(new CaretProfile(regions));
    }

    private void replaceSelected() {
        if(!regions.isEmpty()) {
            if(selectedIndex != -2) {
                editor.editorComponent.getCaret().setProfile(new CaretProfile(regions.get(selectedIndex+1), regions.get(selectedIndex)));
                editor.editorComponent.getEditManager().insertEdit(new InsertionEdit(replaceField.getText(), editor.editorComponent));
            } else {
                hint.setText("No match selected");
                hint.show(getLocationForReplacementError(), new TemporaryConfirmation());
            }
        } else {
            hint.setText("Nothing to replace");
            hint.show(getLocationForReplacementError(), new TemporaryConfirmation());
        }
    }

    private Point getLocationForReplacementError() {
        Point pt = replaceField.getLocationOnScreen();
        pt.x += replaceField.getWidth()/2;
        pt.y += replaceField.getHeight();
        return pt;
    }

    private void replaceAll(boolean inSelection) {
        ArrayList<Integer> toReplace = new ArrayList<>();
        for (int i = 0; i < regions.size() - 1; i += 2) {
            int first = regions.get(i);
            int second = regions.get(i+1);
            if (!excluded.contains(first) && (!inSelection || editor.editorComponent.getCaret().getProfile().contains(first, second))) {
                toReplace.add(first);
                toReplace.add(second);
            }
        }
        if(!toReplace.isEmpty()) {
            editor.editorComponent.getCaret().setProfile(new CaretProfile(toReplace));
            editor.editorComponent.getEditManager().insertEdit(new InsertionEdit(replaceField.getText(), editor.editorComponent));
        } else {
            hint.setText("Nothing to replace");
            hint.show(getLocationForReplacementError(), new TemporaryConfirmation());
        }
    }

    private void excludeSelected() {
        if(selectedIndex != -2 && !excluded.contains(selectedIndex)) {
            excluded.add(regions.get(selectedIndex));
            editor.editorComponent.repaint();
        }
    }

    public void clear() {
        regions.clear();
    }

    public void addRegion(int start, int end) {
        regions.add(start);
        regions.add(end);
    }

    public void focus() {
        findField.requestFocus();
        findField.getCaret().setProfile(new CaretProfile(findField.getText().length(), 0));
    }

    public void onReveal() {
        highlighter.setEnabled(true);
    }

    public void onDismiss() {
        highlighter.setEnabled(false);
    }

    public void dispose() {
        tlm.dispose();
    }
}
