package com.energyxxer.trident.main.window.sections.search_path;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.global.Preferences;
import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.main.TridentUI;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.editor.TridentEditorModule;
import com.energyxxer.trident.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.trident.ui.styledcomponents.*;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.DragHandler;
import com.energyxxer.xswing.UnifiedDocumentListener;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchPathDialog extends JDialog implements WindowFocusListener, ActionListener {

    private static final int MAX_COUNT = 200;
    private ThemeListenerManager tlm = new ThemeListenerManager();

    public static final SearchPathDialog INSTANCE = new SearchPathDialog();

    private JPanel contentPanel = new JPanel(new BorderLayout());
    private StyledTextField field;
    private JScrollPane scrollPane;
    private SearchPathExplorerMaster explorer = new SearchPathExplorerMaster();

    private long lastEdit;
    private Thread searchThread;

    private final String[] filterOptions = new String[] {"Entire Workspace", "Current Project", "Project Data", "Project Resources"};
    private StyledDropdownMenu<String> rootPicker;
    private StyledCheckBox fileMaskEnabled;
    private StyledTextField fileMask;

    private JPanel previewPanel;
    private JLabel previewLabel;
    private TridentEditorModule editorModule;
    private FixedHighlighter highlighter;

    private ThemeListenerManager tlmHighlighter;

    private StyledCheckBox matchCase;
    private StyledCheckBox wordsOnly;
    private StyledCheckBox regex;

    private SearchPathDialog() {
        super(TridentWindow.jframe, false);
        setup();
    }


    private void setup() {
        this.setUndecorated(true);

        this.setContentPane(contentPanel);

        contentPanel.setPreferredSize(new Dimension(800, 600));
        JPanel header = new JPanel(new BorderLayout());

        JPanel titleBar = new JPanel(new BorderLayout());
        MouseAdapter dragAdapter = new DragHandler(this);
        titleBar.addMouseListener(dragAdapter);
        titleBar.addMouseMotionListener(dragAdapter);
        titleBar.add(new StyledLabel("    Find in Path", "FindInPath.header"), BorderLayout.WEST);
        JPanel controlsPanel = new JPanel();
        controlsPanel.setOpaque(false);

        controlsPanel.add(this.matchCase = new StyledCheckBox("Match case", "FindInPath.header"));
        controlsPanel.add(this.wordsOnly = new StyledCheckBox("Words", "FindInPath.header"));
        controlsPanel.add(this.regex = new StyledCheckBox("Regex", "FindInPath.header"));

        controlsPanel.add(new Padding(16));

        this.fileMaskEnabled = new StyledCheckBox("File Mask:", "FindInPath.header");
        controlsPanel.add(this.fileMaskEnabled);

        this.fileMask = new StyledTextField("", "FindInPath.header");
        this.fileMask.setPreferredSize(new Dimension(60, 24));
        this.fileMask.setText("*.tdn");
        controlsPanel.add(this.fileMask);

        controlsPanel.add(this.rootPicker = new StyledDropdownMenu<>(filterOptions, "FindInPath"));
        this.rootPicker.setValue(filterOptions[1]);
        this.rootPicker.addChoiceListener(l -> updateLastEdit());



        titleBar.add(controlsPanel, BorderLayout.EAST);
        header.add(titleBar, BorderLayout.NORTH);
        titleBar.setPreferredSize(new Dimension(1, 35));








        this.field = new StyledTextField("", "FindInPath");
        field.setPreferredSize(new Dimension(1, 28));
        this.field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dismiss();
                    TridentWindow.jframe.requestFocus();
                    e.consume();
                }
            }
        });
        this.field.getDocument().addDocumentListener((UnifiedDocumentListener) e -> updateLastEdit());
        header.add(this.field, BorderLayout.SOUTH);
        contentPanel.add(header, BorderLayout.NORTH);
        this.scrollPane = new OverlayScrollPane(this.explorer);
        contentPanel.add(this.scrollPane, BorderLayout.CENTER);

        //this.addMouseListener(this);
        //this.addMouseMotionListener(this);
        this.addWindowFocusListener(this);

        this.pack();
        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        Dimension size = this.getSize();
        center.x -= size.width/2;
        center.y -= size.height/2;
        this.setLocation(center);



        this.matchCase.setSelected(Preferences.get("findInPath.match_case","false").equals("true"));
        this.wordsOnly.setSelected(Preferences.get("findInPath.words","false").equals("true"));
        this.regex.setSelected(Preferences.get("findInPath.regex","false").equals("true"));
        this.fileMaskEnabled.setSelected(Preferences.get("findInPath.fileMaskEnabled","false").equals("true"));
        this.fileMask.setText(Preferences.get("findInPath.fileMask","*.tdn"));

        this.fileMask.setEnabled(this.fileMaskEnabled.isSelected());

        this.matchCase.addActionListener(e -> {
            Preferences.put("findInPath.match_case", String.valueOf(this.matchCase.isSelected()));
            updateLastEdit();
        });
        this.wordsOnly.addActionListener(e -> {
            Preferences.put("findInPath.words", String.valueOf(this.wordsOnly.isSelected()));
            updateLastEdit();
        });
        this.regex.addActionListener(e -> {
            Preferences.put("findInPath.regex", String.valueOf(this.regex.isSelected()));
            updateLastEdit();
        });
        this.fileMaskEnabled.addActionListener(e -> {
            Preferences.put("findInPath.fileMaskEnabled", String.valueOf(this.fileMaskEnabled.isSelected()));
            this.fileMask.setEnabled(this.fileMaskEnabled.isSelected());
            updateLastEdit();
        });
        this.fileMask.addActionListener(e -> {
            Preferences.put("findInPath.fileMask", this.fileMask.getText());
            updateLastEdit();
        });

        previewPanel = new JPanel(new BorderLayout());
        previewLabel = new StyledLabel("", "FindInPath.preview.header");
        previewLabel.setPreferredSize(new Dimension(1, 26));
        previewPanel.add(previewLabel, BorderLayout.NORTH);


        //this.explorer.addElement(recentFilesCategory);
        //this.explorer.addElement(filesCategory);
        //this.explorer.addElement(actionsCategory);


        tlm.addThemeChangeListener(t -> {
            titleBar.setBackground(t.getColor(new Color(230, 230, 230), "FindInPath.header.background"));
            int thickness = Math.max(t.getInteger(1,"FindInPath.border.thickness"),0);
            contentPanel.setBorder(BorderFactory.createMatteBorder(thickness, thickness, thickness, thickness, t.getColor(new Color(200, 200, 200), "FindInPath.border.color")));
            field.setBorder(BorderFactory.createMatteBorder(0, 28, 0, 0, new ImageIcon(Commons.getIcon("search_28"))));
            previewPanel.setBackground(t.getColor(new Color(230, 230, 230), "FindInPath.preview.header.background", "FindInPath.header.background"));
            thickness = Math.max(t.getInteger(1,"FindInPath.preview.header.border.thickness"),0);
            previewPanel.setBorder(BorderFactory.createMatteBorder(thickness, 0, thickness, 0, t.getColor(new Color(200, 200, 200), "FindInPath.preview.header.border.color", "FindInPath.border.color")));
        });


        Timer timer = new Timer(20, this);
        timer.start();
    }

    private void search() {
        explorer.clear();
        String rawPattern = field.getText();
        if(rawPattern.isEmpty()) return;
        if(!regex.isSelected()) {
            rawPattern = Pattern.quote(rawPattern);
        }
        if(wordsOnly.isSelected()) {
            rawPattern = "\\b" + rawPattern + "\\b";
        }
        Pattern pattern = Pattern.compile(rawPattern, matchCase.isSelected() ? 0 : Pattern.CASE_INSENSITIVE);
        File startFile = new File(Preferences.get("workspace_dir"));
        switch(rootPicker.getValueIndex()) {
            case 1: { //"Current Project"
                Project project = Commons.getActiveProject();
                if(project != null) startFile = project.getRootDirectory();
                break;
            }
            case 2: { //"Project Data"
                Project project = Commons.getActiveProject();
                if(project != null) startFile = project.getDataPackRoot();
                break;
            }
            case 3: { //"Project Resources"
                Project project = Commons.getActiveProject();
                if(project != null) startFile = project.getResourcePackRoot();
                break;
            }
        }
        searchInFile(pattern, startFile);
        explorer.repaint();
    }

    private int temp = 0;

    private void searchInFile(Pattern query, File file) {
        if(explorer.getCount() >= MAX_COUNT) return;
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            if(files != null) {
                for(File child : files) {
                    searchInFile(query, child);
                    if(explorer.getCount() >= MAX_COUNT) return;
                }
            }
        } else {
            if(shouldRead(file)) {
                try {
                    String content = new String(Files.readAllBytes(file.toPath()), TridentUI.DEFAULT_CHARSET);
                    Matcher matcher = query.matcher(content);
                    while(matcher.find()) {
                        int snippetStart = content.lastIndexOf('\n', matcher.start()-1);
                        if(snippetStart < 0) snippetStart = 0;
                        int snippetEnd = content.indexOf('\n', matcher.end());
                        if(snippetEnd > content.length()) snippetEnd = content.length();
                        int line = content.substring(0, snippetStart).split("\n",-1).length;
                        addFileResult(file, content.substring(snippetStart, snippetEnd), matcher.start() - snippetStart, matcher.start(), matcher.end() - matcher.start(), line);
                        if(explorer.getCount() >= MAX_COUNT) return;
                    }
                } catch (IOException x) {
                    x.printStackTrace();
                }
            }
        }
    }

    private static final String[] fileEndings = new String[] {".tdn", ".mcfunction", ".mcmeta", ".json", ".tdnproj", ".txt", ".md", ".gitignore"};

    private boolean shouldRead(File file) {
        if(fileMaskEnabled.isSelected()) {
            String nameRegex = fileMask.getText();
            nameRegex = Pattern.quote(nameRegex);
            nameRegex = nameRegex.replace("\\*", ".*");
            return file.getName().matches(nameRegex);
        } else {
            for(String extension : fileEndings) {
                if(file.getName().endsWith(extension)) return true;
            }
            return false;
        }
    }

    private void addFileResult(File file, String substring, int substringOffset, int start, int length, int line) {
        if(substring.length() > 128) {
            substring = substring.substring(substringOffset, substringOffset + length);
            substringOffset = 0;
        }
        SearchPathItem item = new SearchPathItem(file, substring, substringOffset, start, length, line, explorer, new ArrayList<>());
        explorer.addElement(item);
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
            updateLastEdit();
        }
        this.setVisible(true);
        this.field.requestFocus();
        this.field.setSelectionStart(0);
        this.field.setSelectionEnd(this.field.getText().length());
    }

    public void dismiss() {
        this.setVisible(false);
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

    public void showEditor(File file, int start, int length) {
        if(editorModule != null) {
            previewPanel.remove(editorModule);
            if(tlmHighlighter != null) tlmHighlighter.dispose();
            editorModule.dispose();
            editorModule = null;
            contentPanel.revalidate();
        }
        editorModule = new TridentEditorModule(null, file);
        editorModule.setPreferredSize(new Dimension(1, 300));
        highlighter = new FixedHighlighter(editorModule.editorComponent);
        tlmHighlighter = new ThemeListenerManager();
        tlm.addThemeChangeListener(t -> {
            this.highlighter.setHighlightColor(t.getColor(Color.GREEN, "FindInPath.highlight", "Editor.find.highlight"));
            this.highlighter.setHighlightBorderColor(t.getColor(Color.YELLOW, "FindInPath.highlight.border", "Editor.find.highlight.border"));
        });
        highlighter.addRegion(start, start+length);
        try {
            editorModule.editorComponent.getHighlighter().addHighlight(0, 0, highlighter);
        } catch (BadLocationException x) {
            x.printStackTrace(); //wtf exception
        }

        previewPanel.add(editorModule, BorderLayout.CENTER);
        previewLabel.setText("    " + file.getPath());
        contentPanel.add(previewPanel, BorderLayout.SOUTH);
        revalidate();
        repaint();
        editorModule.scrollToCenter(start);
    }
}
