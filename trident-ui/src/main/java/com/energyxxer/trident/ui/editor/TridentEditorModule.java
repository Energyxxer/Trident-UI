package com.energyxxer.trident.ui.editor;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.global.temp.Lang;
import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.global.temp.projects.ProjectManager;
import com.energyxxer.trident.global.temp.projects.TridentProject;
import com.energyxxer.trident.main.TridentUI;
import com.energyxxer.trident.main.window.sections.editor_search.FindAndReplaceBar;
import com.energyxxer.trident.ui.Tab;
import com.energyxxer.trident.ui.display.DisplayModule;
import com.energyxxer.trident.ui.modules.FileModuleToken;
import com.energyxxer.trident.ui.scrollbar.OverlayScrollPaneLayout;
import com.energyxxer.trident.ui.theme.Theme;
import com.energyxxer.trident.ui.theme.ThemeManager;
import com.energyxxer.trident.ui.theme.change.ThemeChangeListener;
import com.energyxxer.trident.util.linenumber.TextLineNumber;
import com.energyxxer.util.Disposable;
import com.energyxxer.util.Lazy;
import com.energyxxer.util.logger.Debug;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Display module for the main text editor of the program.
 */
public class TridentEditorModule extends JPanel implements DisplayModule, UndoableEditListener, MouseListener, ThemeChangeListener, Disposable {

    JScrollPane scrollPane;

    File file;
    Tab associatedTab;

    public TridentEditorComponent editorComponent;
    private TextLineNumber tln;
    protected Theme syntax;

    private ArrayList<String> styles = new ArrayList<>();
    HashMap<String, String[]> parserStyles = new HashMap<>();

    private Lazy<FindAndReplaceBar> searchBar = new Lazy<>(() -> new FindAndReplaceBar(this));
    private boolean searchBarVisible = false;

    long highlightTime = 0;

    //public long lastToolTip = new Date().getTime();

    public TridentEditorModule(Tab tab, File file) {
        super(new BorderLayout());
        this.file = file;
        this.associatedTab = tab;
        this.scrollPane = new JScrollPane();

        editorComponent = new TridentEditorComponent(this);

        JPanel container = new JPanel(new BorderLayout());
        container.add(editorComponent);
        scrollPane.setViewportView(container);

        this.add(scrollPane, BorderLayout.CENTER);

        tln = new TextLineNumber(editorComponent, scrollPane);
        tln.setPadding(10);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        KeyStroke findKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        editorComponent.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(findKeystroke, "findKeystroke");

        editorComponent.getActionMap().put("findKeystroke", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showSearchBar();
                searchBar.getValue().focus();
            }
        });

        editorComponent.addMouseListener(this);

        scrollPane.setRowHeaderView(tln);

        scrollPane.setLayout(new OverlayScrollPaneLayout(scrollPane));

        scrollPane.getVerticalScrollBar().setUnitIncrement(17);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(17);
        scrollPane.getVerticalScrollBar().setValue(0);
        scrollPane.getHorizontalScrollBar().setValue(0);

        scrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                focus();
            }
        });
        scrollPane.getVerticalScrollBar().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                focus();
            }
        });
        scrollPane.getHorizontalScrollBar().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                focus();
            }
        });
        scrollPane.getViewport().addChangeListener(l -> editorComponent.getSuggestionInterface().relocate());

        addThemeChangeListener();

        reloadFromDisk();

        if(tab == null) {
            editorComponent.setEditable(false);
        }
    }

    public void showSearchBar() {
        if(!searchBarVisible) {
            this.add(searchBar.getValue(), BorderLayout.NORTH);
            searchBar.getValue().onReveal();
            revalidate();
            searchBarVisible = true;

            scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getValue() + searchBar.getValue().getHeight());
            repaint();
        }
    }

    public void hideSearchBar() {
        if(searchBarVisible) {
            scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getValue() - searchBar.getValue().getHeight());

            this.remove(searchBar.getValue());
            searchBar.getValue().onDismiss();
            revalidate();
            focus();
            searchBarVisible = false;
            repaint();
        }
    }

    public void reloadFromDisk() {
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(file.toPath());
            String s = new String(encoded, TridentUI.DEFAULT_CHARSET);
            setText(s);
            editorComponent.setCaretPosition(0);
            if(associatedTab != null) associatedTab.updateSavedValue();
            startEditListeners();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startEditListeners() {
        editorComponent.getDocument().addUndoableEditListener(this);
    }

    private void clearStyles() {
        for(String key : this.styles) {
            editorComponent.removeStyle(key);
        }
        for(String key : this.parserStyles.keySet()) {
            editorComponent.removeStyle(key);
        }
        this.styles.clear();
        this.parserStyles.clear();
    }

    private void setSyntax(Theme newSyntax) {
        if(newSyntax == null) {
            syntax = null;
            clearStyles();
            return;
        }
        if(newSyntax.getThemeType() != Theme.ThemeType.SYNTAX_THEME) {
            Debug.log("Theme \"" + newSyntax + "\" is not a syntax theme!", Debug.MessageType.ERROR);
            return;
        }

        this.syntax = newSyntax;
        clearStyles();
        for(String value : syntax.getValues().keySet()) {
            if(!value.contains(".")) continue;
            //if(sections.length > 2) continue;

            String name = value.substring(0,value.lastIndexOf("."));
            Style style = editorComponent.getStyle(name);
            if(style == null) {
                style = editorComponent.addStyle(name, null);
                this.styles.add(name);
                if(name.startsWith("$") && name.contains(".")) {
                    parserStyles.put(name, name.substring(1).toUpperCase().split("\\."));
                }
            }
            switch(value.substring(value.lastIndexOf(".")+1)) {
                case "foreground": {
                    StyleConstants.setForeground(style, syntax.getColor(value));
                    break;
                }
                case "background": {
                    StyleConstants.setBackground(style, syntax.getColor(value));
                    break;
                }
                case "italic": {
                    StyleConstants.setItalic(style, syntax.getBoolean(value));
                    break;
                }
                case "bold": {
                    StyleConstants.setBold(style, syntax.getBoolean(value));
                    break;
                }
            }
        }
    }

    public void setText(String text) {
        editorComponent.setText(text);

        editorComponent.highlight();
    }

    public String getText() {
        return editorComponent.getText();
    }

    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        if (!e.getEdit().getPresentationName().equals("style change")) {
            editorComponent.highlight();
            if(associatedTab != null) associatedTab.onEdit();
        }
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {

    }

    @Override
    public void mouseEntered(MouseEvent arg0) {

    }

    @Override
    public void mouseExited(MouseEvent arg0) {

    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {

    }

    public void ensureVisible(int index) {
        try {
            Rectangle view = scrollPane.getViewport().getViewRect();
            Rectangle rect = editorComponent.modelToView(index);
            if(rect == null) return;
            rect.width = 2;
            rect.x -= view.x;
            rect.y -= view.y;
            scrollPane.getViewport().scrollRectToVisible(rect);
        } catch (BadLocationException x) {
            x.printStackTrace();
        }
    }

    public void scrollToCenter(int index) {
        try {
            Rectangle view = scrollPane.getViewport().getViewRect();
            Rectangle rect = editorComponent.modelToView(index);
            if(rect == null) return;
            rect.width = view.width;
            rect.height = view.height;
            rect.x -= rect.width/2;
            rect.y -= rect.height/2;
            rect.x -= view.x;
            rect.y -= view.y;
            scrollPane.getViewport().scrollRectToVisible(rect);
        } catch (BadLocationException x) {
            x.printStackTrace();
        }
    }

    @Override
    public void themeChanged(Theme t) {
        tln.setBackground(t.getColor(new Color(235, 235, 235), "Editor.lineNumber.background"));
        tln.setForeground(t.getColor(new Color(150, 150, 150), "Editor.lineNumber.foreground"));
        //tln current line background
        tln.setCurrentLineForeground(t.getColor(tln.getForeground(), "TridentEditorModule.lineNumber.currentLine.foreground"));
        tln.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0,
                                0,
                                0,
                                Math.max(t.getInteger(1,"Editor.lineNumber.border.thickness"),0),
                                t.getColor(new Color(200, 200, 200), "Editor.lineNumber.border.color","General.line")
                        ),
                        BorderFactory.createEmptyBorder(
                                0,
                                0,
                                0,
                                15
                        )
                )
        );
        tln.setFont(new Font(t.getString("TridentEditorModule.lineNumber.font","default:monospaced"),0,12));

        Lang lang = Lang.getLangForFile(file.getPath());
        if(lang != null) {
            setSyntax(ThemeManager.getSyntaxForGUITheme(lang, t));
            editorComponent.highlight();
        }
    }

    @Override
    public void displayCaretInfo() {
        editorComponent.displayCaretInfo();
    }

    @Override
    public Object getValue() {
        return getText().intern().hashCode();
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public Object save() {
        PrintWriter writer;
        try {
            writer = new PrintWriter(file, "UTF-8");

            String text = getText();
            if(!text.endsWith("\n")) {
                text = text.concat("\n");
                try {
                    editorComponent.getDocument().insertString(text.length()-1,"\n",null);
                } catch(BadLocationException e) {
                    e.printStackTrace();
                }
            }
            writer.print(text);
            writer.close();

            if(new File(((FileModuleToken) associatedTab.token).getPath()).getName().equals(".tdnproj")) {
                ProjectManager.loadWorkspace();
            }

            Commons.index(ProjectManager.getAssociatedProject(file));

            return getValue();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void focus() {
        editorComponent.requestFocus();
    }

    @Override
    public void onSelect() {
        Project project = ProjectManager.getAssociatedProject(file);
        if(project instanceof TridentProject) {
            if(((TridentProject) project).instantiationTime >= highlightTime) {
                editorComponent.highlight();
            }
        }
    }

    @Override
    public void dispose() {
        editorComponent.dispose();
    }
}