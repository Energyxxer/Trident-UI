package com.energyxxer.trident.main.window.sections.tools.todo;

import com.energyxxer.crossbow.compiler.util.CrossbowProjectSummary;
import com.energyxxer.enxlex.lexical_analysis.summary.Todo;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSection;
import com.energyxxer.trident.compiler.util.TridentProjectSummary;
import com.energyxxer.trident.global.temp.projects.CrossbowProject;
import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.global.temp.projects.ProjectManager;
import com.energyxxer.trident.global.temp.projects.TridentProject;
import com.energyxxer.trident.main.window.sections.quick_find.StyledExplorerMaster;
import com.energyxxer.trident.main.window.sections.tools.ToolBoard;
import com.energyxxer.trident.main.window.sections.tools.ToolBoardMaster;
import com.energyxxer.trident.main.window.sections.tools.find.FileOccurrence;
import com.energyxxer.trident.main.window.sections.tools.find.FindExplorerFilter;
import com.energyxxer.trident.main.window.sections.tools.find.FindResultExplorerItem;
import com.energyxxer.trident.main.window.sections.tools.find.FindResults;
import com.energyxxer.trident.ui.ToolbarButton;
import com.energyxxer.trident.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.trident.ui.styledcomponents.ButtonHintHandler;
import com.energyxxer.trident.ui.styledcomponents.Padding;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.StringBounds;
import com.energyxxer.xswing.hints.Hint;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.Map;

public class TodoBoard extends ToolBoard {

    private static class TodoExplorer extends StyledExplorerMaster implements FindExplorerFilter {
        @Override
        public boolean groupByProject() {
            return true;
        }

        @Override
        public boolean groupBySubProject() {
            return false;
        }

        @Override
        public boolean groupByPath() {
            return false;
        }

        @Override
        public boolean groupByFile() {
            return false;
        }

        @Override
        public boolean highlightResult() {
            return false;
        }
    }

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private OverlayScrollPane scrollPane = new OverlayScrollPane(tlm);
    private StyledExplorerMaster explorer = new TodoExplorer();

    public TodoBoard(ToolBoardMaster parent) {
        super(parent);
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(10, 200));
        scrollPane.setViewportView(explorer);
        this.add(scrollPane);
        JPanel filterPanel = new JPanel(new BorderLayout());
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        filterPanel.add(wrapper, BorderLayout.NORTH);
        wrapper.add(new Padding(3), BorderLayout.NORTH);
        wrapper.add(new Padding(4), BorderLayout.WEST);
        wrapper.add(new Padding(4), BorderLayout.EAST);
        JPanel verticalPanel = new JPanel(new GridLayout(0, 1, 6, 6));
        verticalPanel.setOpaque(false);
        wrapper.add(verticalPanel, BorderLayout.CENTER);
        this.add(filterPanel, BorderLayout.WEST);

        ToolbarButton refreshButton = new ToolbarButton("reload", tlm);
        refreshButton.addMouseMotionListener(new ButtonHintHandler("Refresh", refreshButton, Hint.RIGHT));
        refreshButton.addActionListener(e -> refresh());
        verticalPanel.add(refreshButton);

        tlm.addThemeChangeListener(t -> {
            filterPanel.setBackground(t.getColor(Color.WHITE, "ToolBoard.header.background"));
        });
    }

    public void refresh() {
        explorer.clear();
        for(Project project : ProjectManager.getLoadedProjects()) {
            if(project.getSummary() != null) {
                Collection<Todo> todos = null;
                if(project instanceof TridentProject) todos = ((TridentProjectSummary) project.getSummary()).getTodos();
                else if(project instanceof CrossbowProject) todos = ((CrossbowProjectSummary) project.getSummary()).getTodos();

                if(todos == null || todos.isEmpty()) return;

                FindResults.ProjectResult projectResult = new FindResults.ProjectResult(project.getRootDirectory());

                for(Todo todo : todos) {
                    StringBounds bounds = todo.getToken().getStringBounds();
                    TokenSection section = null;
                    for(Map.Entry<TokenSection, String> entry : todo.getToken().getSubSections().entrySet()) {
                        if(entry.getValue().equals("comment.todo")) {
                            section = entry.getKey();
                            break;
                        }
                    }
                    FileOccurrence occurrence = new FileOccurrence(
                            new File(todo.getToken().file),
                            bounds.start.index + section.start,
                            section.length,
                            bounds.start.line,
                            todo.getText(),
                            0);
                    projectResult.insertResult(occurrence);
                }
                FindResultExplorerItem projectItem = new FindResultExplorerItem(projectResult, explorer, k -> true);
                explorer.addElement(projectItem);
            }
        }
    }

    @Override
    public String getName() {
        return "TODO";
    }

    @Override
    public String getIconName() {
        return "todo";
    }
}
