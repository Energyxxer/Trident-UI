package com.energyxxer.trident.main.window.sections;

import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.langinterface.ProjectType;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.main.window.actions.ActionManager;
import com.energyxxer.trident.main.window.actions.ProgramAction;
import com.energyxxer.trident.ui.ToolbarButton;
import com.energyxxer.trident.ui.ToolbarSeparator;
import com.energyxxer.trident.ui.styledcomponents.StyledLabel;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.Padding;
import com.energyxxer.xswing.ScalableDimension;
import com.energyxxer.xswing.hints.TextHint;

import javax.swing.*;
import java.awt.*;

/**
 * Created by User on 12/15/2016.
 */
public class Toolbar extends JPanel {

    public TextHint hint = TridentWindow.hintManager.createTextHint("");
    private StyledLabel projectLabel;

    public ThemeListenerManager tlm;

    public void setActiveProject(Project project) {
        if(project != null) {
            projectLabel.setText(project.getName());
            projectLabel.setIconName(project.getProjectType().getDefaultProjectIconName());
        } else {
            projectLabel.setText("");
            projectLabel.setIconName(null);
        }
    }
    
    {
        this.tlm = new ThemeListenerManager();
        this.hint.setOutDelay(1);

        int defaultHeight = 29;

        this.setPreferredSize(new ScalableDimension(1, defaultHeight));
        this.setLayout(new BorderLayout());

        JPanel projectIndicator = new JPanel(new GridBagLayout());
        projectIndicator.setOpaque(false);

        this.add(projectIndicator, BorderLayout.WEST);

        projectIndicator.add(new Padding(10));

        projectLabel = new StyledLabel("", "Toolbar.projectIndicator", tlm);
        projectLabel.setTextThemeDriven(false);
        projectIndicator.add(projectLabel);

        JPanel buttonBar = new JPanel(new GridBagLayout());
        tlm.addThemeChangeListener(t -> {
            this.setBackground(t.getColor(new Color(235, 235, 235), "Toolbar.background"));
            this.setBorder(BorderFactory.createMatteBorder(0, 0, Math.max(t.getInteger(1,"Toolbar.border.thickness"),0), 0, t.getColor(new Color(200, 200, 200), "Toolbar.border.color")));

            int height = t.getInteger(29, "Toolbar.height");

            this.setPreferredSize(new ScalableDimension(1, height));
        });
        buttonBar.setOpaque(false);
        this.add(buttonBar, BorderLayout.EAST);

        {
            buttonBar.add(createButtonForAction("COMPILE"));
        }

        buttonBar.add(new ToolbarSeparator(tlm));

        {
            buttonBar.add(createButtonForAction("SAVE"));
        }

        {
            buttonBar.add(createButtonForAction("SAVE_ALL"));
        }

        buttonBar.add(new ToolbarSeparator(tlm));

        buttonBar.add(createButtonForAction("DOCUMENTATION"));

        buttonBar.add(new ToolbarSeparator(tlm));

        for(ProjectType type : ProjectType.values()) {
            buttonBar.add(createButtonForAction("NEW_PROJECT_" + type.getCode()));
        }

        {
            buttonBar.add(createButtonForAction("PROJECT_PROPERTIES"));
        }

        {
            buttonBar.add(createButtonForAction("SEARCH_EVERYWHERE"));
        }

        buttonBar.add(new Padding(10));
    }

    private ToolbarButton createButtonForAction(String actionKey) {
        ProgramAction action = ActionManager.getAction(actionKey);
        ToolbarButton button = new ToolbarButton(action.getIconKey(),tlm);
        String title = action.getTitle();
        if(action.getShortcut() != null && action.getShortcut().getFirstMapping() != null) {
            title += " (" + action.getShortcut().getFirstMapping().getHumanReadableName() + ")";
        }
        button.setHintText(title);
        button.addActionListener(e -> action.perform());
        return button;
    }
}
