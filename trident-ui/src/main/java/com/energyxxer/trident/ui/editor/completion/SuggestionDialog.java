package com.energyxxer.trident.ui.editor.completion;

import com.energyxxer.enxlex.suggestions.LiteralSuggestion;
import com.energyxxer.enxlex.suggestions.Suggestion;
import com.energyxxer.enxlex.suggestions.SuggestionModule;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.main.window.sections.quick_find.StyledExplorerMaster;
import com.energyxxer.trident.ui.editor.TridentEditorComponent;
import com.energyxxer.trident.ui.editor.behavior.editmanager.edits.InsertionEdit;
import com.energyxxer.trident.ui.explorer.base.StandardExplorerItem;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.logger.Debug;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class SuggestionDialog extends JDialog implements KeyListener, FocusListener, SuggestionInterface {
    private TridentEditorComponent editor;

    private OverlayScrollPane scrollPane;
    private StyledExplorerMaster explorer;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    public SuggestionDialog(TridentEditorComponent editor) {
        super(TridentWindow.jframe, false);
        this.setUndecorated(true);
        this.editor = editor;

        this.explorer = new StyledExplorerMaster("EditorSuggestions");

        this.setContentPane(scrollPane = new OverlayScrollPane(explorer));

        tlm.addThemeChangeListener(t -> {
            //titleBar.setBackground(t.getColor(new Color(230, 230, 230), "FindInPath.header.background"));
            int thickness = Math.max(t.getInteger(1, "EditorSuggestions.border.thickness"), 0);
            scrollPane.setBorder(BorderFactory.createMatteBorder(thickness, thickness, thickness, thickness, t.getColor(new Color(200, 200, 200), "EditorSuggestions.border.color")));
        });

        editor.addKeyListener(this);
        editor.addFocusListener(this);
        this.addKeyListener(this);
    }

    public void showSuggestions(SuggestionModule results) {
        explorer.clear();

        boolean any = false;

        for(Suggestion suggestion : results.getSuggestions()) {
            if(suggestion instanceof LiteralSuggestion) {
                StandardExplorerItem item = new StandardExplorerItem(new SuggestionToken(this, suggestion), explorer, new ArrayList<>());
                explorer.addElement(item);
                if(!any) {
                    item.setSelected(true);
                    explorer.setSelected(item, null);
                }
                any = true;
            }
        }

        if(any) {
            this.setVisible(true);
            this.pack();
            this.setSize(new Dimension(300, 300));
            try {
                Rectangle rect = editor.modelToView(results.getFocusedIndex());
                Point loc = rect.getLocation();
                loc.y += rect.height;
                loc.translate(editor.getLocationOnScreen().x, editor.getLocationOnScreen().y);
                this.setLocation(loc);
            } catch (BadLocationException x) {
                x.printStackTrace();
            }
            editor.requestFocus();
        } else {
            this.setVisible(false);
        }
    }

    public void submit(Suggestion suggestion) {
        Debug.log("Submit suggestion '" + suggestion + "'");
        this.setVisible(false);
        if(suggestion instanceof LiteralSuggestion) {
            editor.getEditManager().insertEdit(new InsertionEdit(((LiteralSuggestion) suggestion).getLiteral(), editor));
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(!this.isVisible()) return;
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            this.setVisible(false);
            e.consume();
            return;
        }
        int selectedIndex = explorer.getFirstSelectedIndex();
        if(selectedIndex < 0) selectedIndex = 0;
        if(e.getKeyCode() == KeyEvent.VK_DOWN) {
            selectedIndex++;
            if(selectedIndex >= explorer.getCount()) {
                selectedIndex = 0;
            }
            explorer.setSelectedIndex(selectedIndex);
            e.consume();
        } else if(e.getKeyCode() == KeyEvent.VK_UP) {
            selectedIndex--;
            if(selectedIndex < 0) {
                selectedIndex = explorer.getCount()-1;
            }
            explorer.setSelectedIndex(selectedIndex);
            e.consume();
        } else if(e.getKeyCode() == KeyEvent.VK_TAB) {
            java.util.List<ModuleToken> tokens = explorer.getSelectedTokens();
            if(!tokens.isEmpty()) {
                tokens.get(0).onInteract();
            }
            e.consume();
        }

        Rectangle rect = explorer.getVisibleRect(selectedIndex);
        rect.y -= scrollPane.getViewport().getViewRect().y;

        scrollPane.getViewport().scrollRectToVisible(rect);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void dismiss() {
        this.setVisible(false);
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        if(e.getOppositeComponent() != this) {
            dismiss();
        }
    }
}
