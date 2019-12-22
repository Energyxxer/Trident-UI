package com.energyxxer.trident.main.window.sections.quick_find;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.global.Preferences;
import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.main.window.actions.ActionManager;
import com.energyxxer.trident.main.window.actions.ProgramAction;
import com.energyxxer.trident.ui.explorer.base.StandardExplorerItem;
import com.energyxxer.trident.ui.modules.FileModuleToken;
import com.energyxxer.trident.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.trident.ui.styledcomponents.StyledDropdownMenu;
import com.energyxxer.trident.ui.styledcomponents.StyledLabel;
import com.energyxxer.trident.ui.styledcomponents.StyledTextField;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.ComponentResizer;
import com.energyxxer.xswing.DragHandler;
import com.energyxxer.xswing.OverlayBorderPanel;
import com.energyxxer.xswing.UnifiedDocumentListener;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Date;

public class QuickFindDialog extends JDialog implements WindowFocusListener, ActionListener {

    private ThemeListenerManager tlm = new ThemeListenerManager();

    public static final QuickFindDialog INSTANCE = new QuickFindDialog();

    private JPanel contentPanel = new OverlayBorderPanel(new BorderLayout(), new Insets(8, 8, 8, 8));
    private StyledTextField field;
    private JScrollPane scrollPane;
    private StyledExplorerMaster explorer = new StyledExplorerMaster();
    private QuickFindCategoryItem recentFilesCategory = new QuickFindCategoryItem(explorer, "Recent Files");
    private QuickFindCategoryItem filesCategory = new QuickFindCategoryItem(explorer, "Files");
    private QuickFindCategoryItem actionsCategory = new QuickFindCategoryItem(explorer, "Actions");

    private long lastEdit;
    private Thread searchThread;

    private final String[] filterOptions = new String[] {"Entire Workspace", "Current Project", "Project Data", "Project Resources"};
    private StyledDropdownMenu<String> rootPicker;
    private Component lastFocusedComponent;

    private QuickFindDialog() {
        super(TridentWindow.jframe, false);
        setup();
    }

    private void setup() {
        this.setUndecorated(true);
        this.setBackground(new Color(0,0,0,1));

        this.setContentPane(contentPanel);
        contentPanel.setOpaque(false);
        contentPanel.setMinimumSize(new Dimension(400, 150));
        contentPanel.setPreferredSize(new Dimension(800, 600));
        JPanel header = new JPanel(new BorderLayout());

        JPanel titleBar = new JPanel(new BorderLayout());
        MouseAdapter dragAdapter = new DragHandler(this);
        titleBar.addMouseListener(dragAdapter);
        titleBar.addMouseMotionListener(dragAdapter);
        titleBar.add(new StyledLabel("    Quick Access", "QuickAccess.header", tlm), BorderLayout.WEST);
        JPanel controlsPanel = new JPanel();
        controlsPanel.setOpaque(false);
        controlsPanel.add(this.rootPicker = new StyledDropdownMenu<>(filterOptions, "QuickAccess"));
        this.rootPicker.setValue(filterOptions[1]);
        this.rootPicker.addChoiceListener(l -> updateLastEdit());
        titleBar.add(controlsPanel, BorderLayout.EAST);
        header.add(titleBar, BorderLayout.NORTH);
        titleBar.setPreferredSize(new Dimension(1, 35));








        this.field = new StyledTextField("", "QuickAccess");
        field.setPreferredSize(new Dimension(1, 28));
        this.field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dismiss();
                    e.consume();
                }
            }
        });
        this.field.getDocument().addDocumentListener((UnifiedDocumentListener) e -> updateLastEdit());
        header.add(this.field, BorderLayout.SOUTH);
        contentPanel.add(header, BorderLayout.NORTH);
        this.scrollPane = new OverlayScrollPane(this.explorer);
        contentPanel.add(this.scrollPane, BorderLayout.CENTER);

        tlm.addThemeChangeListener(t -> {
            titleBar.setBackground(t.getColor(new Color(230, 230, 230), "QuickAccess.header.background"));
            int thickness = Math.max(t.getInteger(1,"QuickAccess.border.thickness"),0);
            contentPanel.setBorder(new CompoundBorder(new EmptyBorder(ComponentResizer.DIST, ComponentResizer.DIST, ComponentResizer.DIST, ComponentResizer.DIST), BorderFactory.createMatteBorder(thickness, thickness, thickness, thickness, t.getColor(new Color(200, 200, 200), "QuickAccess.border.color"))));
            field.setBorder(BorderFactory.createMatteBorder(0, 28, 0, 0, new ImageIcon(Commons.getIcon("search_28"))));
        });

        //this.addMouseListener(this);
        //this.addMouseMotionListener(this);
        this.addWindowFocusListener(this);

        this.pack();
        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        Dimension size = this.getSize();
        center.x -= size.width/2;
        center.y -= size.height/2;
        this.setLocation(center);



        this.explorer.addElement(recentFilesCategory);
        this.explorer.addElement(filesCategory);
        this.explorer.addElement(actionsCategory);


        Timer timer = new Timer(20, this);
        timer.start();


        ComponentResizer resizer = new ComponentResizer(contentPanel, this);
        resizer.setResizable(true, true, true, true);
    }

    private void search() {
        recentFilesCategory.clear();
        filesCategory.clear();
        actionsCategory.clear();
        String query = this.field.getText().toLowerCase();
        if(query.isEmpty()) {
            for(File file : FileModuleToken.recentFiles) {
                StandardExplorerItem item = new StandardExplorerItem(new FileModuleToken(file), explorer, null);
                item.setDetailed(true);
                recentFilesCategory.addElement(item);
            }
        } else {
            File startFile = new File(Preferences.get("workspace_dir"));
            switch(rootPicker.getValueIndex()) {
                case 1: { //"Current Project"
                    Project project = Commons.getActiveProject();
                    if(project != null) startFile = project.getRootDirectory();
                    break;
                }
                case 2: { //"Project Server Data"
                    Project project = Commons.getActiveProject();
                    if(project != null) startFile = project.getServerDataRoot();
                    break;
                }
                case 3: { //"Project Client Data"
                    Project project = Commons.getActiveProject();
                    if(project != null) startFile = project.getClientDataRoot();
                    break;
                }
            }
            searchInFile(query, startFile);

            for(ProgramAction action : ActionManager.getAllActions().values()) {
                if(action.getDescription().toLowerCase().contains(query)
                || action.getDisplayName().toLowerCase().contains(query)) {
                    StandardExplorerItem item = new StandardExplorerItem(action, explorer, null);
                    item.setDetailed(true);
                    actionsCategory.addElement(item);
                }
            }
        }
        explorer.repaint();
    }

    private int temp = 0;

    private void searchInFile(String query, File file) {
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            if(files != null) {
                for(File child : files) {
                    if(child.getName().toLowerCase().contains(query)) {
                        addFileResult(child);
                    }
                    if(child.isDirectory()) searchInFile(query, child);
                }
            }
        } else throw new IllegalArgumentException("file");
    }

    private void addFileResult(File file) {
        StandardExplorerItem item = new StandardExplorerItem(new FileModuleToken(file), explorer, null);
        item.setDetailed(true);
        filesCategory.addElement(item);
        temp++;
        if(temp >= 20) {
            temp = 0;
            explorer.repaint();
        }
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {

    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        this.setVisible(false);
    }

    public void reveal() {
        if(this.isVisible()) {
            int index = rootPicker.getValueIndex();
            index++;
            if(index >= filterOptions.length) {
                index = 0;
            }
            rootPicker.setValueIndex(index);
        } else {
            lastFocusedComponent = TridentWindow.jframe.getFocusOwner();
            updateLastEdit();
        }
        this.setVisible(true);
        this.field.requestFocus();
        this.field.setSelectionStart(0);
        this.field.setSelectionEnd(this.field.getText().length());
    }

    public void dismiss() {
        this.setVisible(false);
        if(lastFocusedComponent != null) lastFocusedComponent.requestFocus();
    }

    void updateLastEdit() {
        lastEdit = new Date().getTime();
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (lastEdit > -1 && (new Date().getTime()) - lastEdit > 500 && this.isVisible()) {
            lastEdit = -1;
            if(searchThread != null) {
                searchThread.stop();
            }
            searchThread = new Thread(this::search,"Quick Find");
            searchThread.start();
        }
    }
}
