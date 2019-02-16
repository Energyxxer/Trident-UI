package com.energyxxer.trident.main.window.sections.tools.find;

import com.energyxxer.trident.global.Preferences;
import com.energyxxer.trident.main.window.sections.tools.ToolBoard;
import com.energyxxer.trident.main.window.sections.tools.ToolBoardMaster;
import com.energyxxer.trident.ui.explorer.base.StandardExplorerItem;
import com.energyxxer.trident.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.trident.ui.styledcomponents.ButtonHintHandler;
import com.energyxxer.trident.ui.styledcomponents.Padding;
import com.energyxxer.trident.ui.styledcomponents.StyledToggleButton;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.hints.Hint;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class FindBoard extends ToolBoard implements FindExplorerFilter {

    private final StyledToggleButton groupByProject;
    private final StyledToggleButton groupBySubProject;
    private final StyledToggleButton groupByPath;
    private final StyledToggleButton groupByFile;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private FindExplorerMaster explorer = new FindExplorerMaster();

    public FindBoard(ToolBoardMaster parent) {
        super(parent);
        this.setLayout(new BorderLayout());

        this.setPreferredSize(new Dimension(1, 300));

        this.add(new OverlayScrollPane(explorer), BorderLayout.CENTER);
        JPanel filterPanel = new JPanel(new BorderLayout());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel verticalPanel = new JPanel(new GridLayout(0, 1, 6, 6));
        verticalPanel.setOpaque(false);
        filterPanel.add(wrapper, BorderLayout.NORTH);
        wrapper.add(new Padding(3), BorderLayout.NORTH);
        wrapper.add(new Padding(4), BorderLayout.WEST);
        wrapper.add(new Padding(4), BorderLayout.EAST);
        wrapper.add(verticalPanel, BorderLayout.CENTER);
        this.add(filterPanel, BorderLayout.WEST);

        groupByProject = new StyledToggleButton("project_group", "ToolBoard.findBoard");
        groupByProject.addMouseMotionListener(new ButtonHintHandler("Group by Project", groupByProject, Hint.RIGHT));
        verticalPanel.add(groupByProject);

        groupBySubProject = new StyledToggleButton("subproject_group", "ToolBoard.findBoard");
        groupBySubProject.addMouseMotionListener(new ButtonHintHandler("Group by Sub Project", groupBySubProject, Hint.RIGHT));
        verticalPanel.add(groupBySubProject);

        groupByPath = new StyledToggleButton("folder_group", "ToolBoard.findBoard");
        groupByPath.addMouseMotionListener(new ButtonHintHandler("Group by Path", groupByPath, Hint.RIGHT));
        verticalPanel.add(groupByPath);

        groupByFile = new StyledToggleButton("file_group", "ToolBoard.findBoard");
        groupByFile.addMouseMotionListener(new ButtonHintHandler("Group by File", groupByFile, Hint.RIGHT));
        verticalPanel.add(groupByFile);

        tlm.addThemeChangeListener(t -> {
            filterPanel.setBackground(t.getColor(Color.WHITE, "ToolBoard.header.background"));
        });

        this.groupByProject.setSelected(Preferences.get("searchResults.groupBy.project","true").equals("true"));
        this.groupBySubProject.setSelected(Preferences.get("searchResults.groupBy.subProject","true").equals("true"));
        this.groupByPath.setSelected(Preferences.get("searchResults.groupBy.path","true").equals("true"));
        this.groupByFile.setSelected(Preferences.get("searchResults.groupBy.file","true").equals("true"));

        this.groupByProject.addActionListener(e -> {
            Preferences.put("searchResults.groupBy.project", String.valueOf(this.groupByProject.isSelected()));
            repaint();
        });
        this.groupBySubProject.addActionListener(e -> {
            Preferences.put("searchResults.groupBy.subProject", String.valueOf(this.groupBySubProject.isSelected()));
            repaint();
        });
        this.groupByPath.addActionListener(e -> {
            Preferences.put("searchResults.groupBy.path", String.valueOf(this.groupByPath.isSelected()));
            repaint();
        });
        this.groupByFile.addActionListener(e -> {
            Preferences.put("searchResults.groupBy.file", String.valueOf(this.groupByFile.isSelected()));
            repaint();
        });
    }

    @Override
    public String getName() {
        return "Search Results";
    }

    @Override
    public String getIconName() {
        return "search";
    }

    public void showResults(FindResults results) {
        explorer.clear();
        StandardExplorerItem item = new StandardExplorerItem(results, explorer, new ArrayList<>());
        item.setDetailed(true);
        explorer.addElement(item);
        this.repaint();
    }

    @Override
    public boolean groupByProject() {
        return groupByProject.isSelected();
    }

    @Override
    public boolean groupBySubProject() {
        return groupBySubProject.isSelected();
    }

    @Override
    public boolean groupByPath() {
        return groupByPath.isSelected();
    }

    @Override
    public boolean groupByFile() {
        return groupByFile.isSelected();
    }
}
