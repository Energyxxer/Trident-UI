package com.energyxxer.trident.ui.dialogs.settings;

import com.energyxxer.trident.global.ContentSwapper;
import com.energyxxer.trident.main.window.sections.quick_find.StyledExplorerMaster;
import com.energyxxer.trident.ui.ToolbarButton;
import com.energyxxer.trident.ui.editor.completion.snippets.Snippet;
import com.energyxxer.trident.ui.editor.completion.snippets.SnippetContext;
import com.energyxxer.trident.ui.editor.completion.snippets.SnippetManager;
import com.energyxxer.trident.ui.explorer.base.ActionHostExplorerItem;
import com.energyxxer.trident.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.trident.ui.scrollbar.OverlayScrollPaneLayout;
import com.energyxxer.trident.ui.styledcomponents.Padding;
import com.energyxxer.trident.ui.styledcomponents.StyledLabel;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.Disposable;
import com.energyxxer.xswing.hints.Hint;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class SettingsSnippets extends JPanel implements ContentSwapper {

    ThemeListenerManager tlm = new ThemeListenerManager();

    private ArrayList<Snippet> snippets = new ArrayList<>();

    private final JPanel content;
    private final StyledExplorerMaster master;
    private JComponent currentSouthComponent = null;

    {
        {
            JPanel header = new JPanel(new BorderLayout());
            header.setPreferredSize(new Dimension(0,40));
            this.add(header, BorderLayout.NORTH);

            {
                JPanel padding = new JPanel();
                padding.setOpaque(false);
                padding.setPreferredSize(new Dimension(25,25));
                header.add(padding, BorderLayout.WEST);
            }

            StyledLabel label = new StyledLabel("Editor Snippets","Settings.content.header", tlm);
            header.add(label, BorderLayout.CENTER);

            tlm.addThemeChangeListener(t -> {
                setBackground(t.getColor(new Color(235, 235, 235), "Settings.content.background"));
                header.setBackground(t.getColor(new Color(235, 235, 235), "Settings.content.header.background"));
                header.setBorder(BorderFactory.createMatteBorder(0, 0, Math.max(t.getInteger(1,"Settings.content.header.border.thickness"),0), 0, t.getColor(new Color(200, 200, 200), "Settings.content.header.border.color")));
            });
        }

        {

            content = new JPanel(new BorderLayout());
            content.setOpaque(false);
            this.add(content, BorderLayout.CENTER);

            master = new StyledExplorerMaster();
            master.setMultipleSelectionsEnabled(false);

            JPanel listPane = new JPanel(new BorderLayout());
            listPane.setOpaque(false);

            JPanel controls = new JPanel(new GridLayout(0, 1, 6, 6));
            controls.setOpaque(false);

            controls.add(new ToolbarButton("add", tlm) {
                {
                    this.setHintText("Add");
                    this.setPreferredSize(new Dimension(24, 24));
                    this.setMinimumSize(new Dimension(24, 24));
                    this.setMaximumSize(new Dimension(24, 24));
                    this.setPreferredHintPos(Hint.LEFT);
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    Snippet newSnippet = new Snippet("<shorthand>", "", "");
                    newSnippet.setContextEnabled(SnippetContext.EVERYWHERE);
                    snippets.add(newSnippet);
                    ExplorerElement explorerItem = new ActionHostExplorerItem(master, new SnippetModuleToken(newSnippet, SettingsSnippets.this));
                    master.addElement(explorerItem);
                    master.setSelected(explorerItem, null);
                    master.repaint();
                }
            });
            controls.add(new ToolbarButton("toggle", tlm) {
                {
                    this.setHintText("Remove");
                    this.setPreferredSize(new Dimension(24, 24));
                    this.setMinimumSize(new Dimension(24, 24));
                    this.setMaximumSize(new Dimension(24, 24));
                    this.setPreferredHintPos(Hint.LEFT);
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    int selectedIndex = master.getFirstSelectedIndex();
                    if(selectedIndex < 0) return;
                    ExplorerElement explorerItem = master.getChildren().get(selectedIndex);
                    if(explorerItem.getToken() instanceof SnippetModuleToken) {
                        Snippet snippetToRemove = ((SnippetModuleToken) explorerItem.getToken()).getSnippet();
                        snippets.remove(snippetToRemove);
                        setContent(null);
                        ((SnippetModuleToken) explorerItem.getToken()).dispose();
                        master.removeElement(explorerItem);
                        master.repaint();
                    }
                }
            });
            controls.add(new ToolbarButton("copy", tlm) {
                {
                    this.setHintText("Duplicate");
                    this.setPreferredSize(new Dimension(24, 24));
                    this.setMinimumSize(new Dimension(24, 24));
                    this.setMaximumSize(new Dimension(24, 24));
                    this.setPreferredHintPos(Hint.LEFT);
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    int selectedIndex = master.getFirstSelectedIndex();
                    if(selectedIndex < 0) return;
                    ExplorerElement explorerItem = master.getChildren().get(selectedIndex);
                    if(explorerItem.getToken() instanceof SnippetModuleToken) {
                        Snippet original = ((SnippetModuleToken) explorerItem.getToken()).getSnippet();
                        Snippet duplicate = original.clone();
                        snippets.add(duplicate);
                        ExplorerElement newExplorerItem = new ActionHostExplorerItem(master, new SnippetModuleToken(duplicate, SettingsSnippets.this));
                        master.addElement(newExplorerItem);
                        master.setSelected(newExplorerItem, null);
                        master.repaint();
                    }
                }
            });
            listPane.add(new JPanel(new BorderLayout()) {
                {
                    this.add(new Padding(4), BorderLayout.WEST);
                    this.add(new Padding(4), BorderLayout.EAST);
                    this.add(new Padding(4), BorderLayout.NORTH);
                    this.add(new JPanel(new BorderLayout()) {
                        {
                            this.setOpaque(false);
                            this.add(controls, BorderLayout.NORTH);
                        }
                    });
                    this.setOpaque(false);
                }
            }, BorderLayout.EAST);

            content.add(listPane, BorderLayout.CENTER);

            JScrollPane sp = new JScrollPane(master);
            sp.setBorder(new EmptyBorder(0,0,0,0));
            sp.setLayout(new OverlayScrollPaneLayout(sp));

            listPane.add(sp, BorderLayout.CENTER);

            Settings.addOpenEvent(() -> {
                master.getChildren().forEach(e -> {
                    if(e.getToken() instanceof Disposable) {
                        ((Disposable) e.getToken()).dispose();
                    }
                });
                master.clear();
                snippets.clear();
                snippets.addAll(SnippetManager.getAll());
                snippets.replaceAll(Snippet::clone);
                setContent(null);

                for(Snippet snippet : snippets) {
                    ActionHostExplorerItem item = new ActionHostExplorerItem(master, new SnippetModuleToken(snippet, this));
                    item.setAutoUpdateTitle(true);
                    master.addElement(item);
                }
            });

            Settings.addApplyEvent(() -> {
                SnippetManager.getAll().clear();
                SnippetManager.getAll().addAll(snippets);
            });
        }
    }

    SettingsSnippets() {
        super(new BorderLayout());
    }

    @Override
    public void setContent(@Nullable JComponent newPanel) {
        if(currentSouthComponent != null) content.remove(currentSouthComponent);
        currentSouthComponent = newPanel;
        if(newPanel != null) {
            content.add(newPanel, BorderLayout.SOUTH);
        }
        content.revalidate();
        content.repaint();
    }
}