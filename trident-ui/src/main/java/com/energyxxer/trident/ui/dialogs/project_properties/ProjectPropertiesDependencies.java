package com.energyxxer.trident.ui.dialogs.project_properties;

import com.energyxxer.trident.compiler.TridentCompiler;
import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.global.temp.projects.ProjectManager;
import com.energyxxer.trident.global.temp.projects.TridentProject;
import com.energyxxer.trident.ui.ToolbarButton;
import com.energyxxer.trident.ui.orderlist.OrderListElement;
import com.energyxxer.trident.ui.orderlist.OrderListMaster;
import com.energyxxer.trident.ui.orderlist.StandardOrderListItem;
import com.energyxxer.trident.ui.scrollbar.OverlayScrollPaneLayout;
import com.energyxxer.trident.ui.styledcomponents.StyledLabel;
import com.energyxxer.trident.ui.styledcomponents.StyledMenuItem;
import com.energyxxer.trident.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.Padding;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectPropertiesDependencies extends JPanel {

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private LinkedHashMap<File, DependencyToken> loadedProjects = new LinkedHashMap<>();

    private final OrderListMaster master;

    {
        master = new OrderListMaster();

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

            StyledLabel label = new StyledLabel("Dependencies", "ProjectProperties.content.header", tlm);
            header.add(label, BorderLayout.CENTER);

            tlm.addThemeChangeListener(t -> {
                setBackground(t.getColor(new Color(235, 235, 235), "ProjectProperties.content.background"));
                header.setBackground(t.getColor(new Color(235, 235, 235), "ProjectProperties.content.header.background"));
                header.setBorder(BorderFactory.createMatteBorder(0, 0, Math.max(t.getInteger(1,"ProjectProperties.content.header.border.thickness"),0), 0, t.getColor(new Color(200, 200, 200), "ProjectProperties.content.header.border.color")));
            });
        }

        {
            JPanel padding_left = new JPanel();
            padding_left.setOpaque(false);
            padding_left.setPreferredSize(new Dimension(50,25));
            this.add(padding_left, BorderLayout.WEST);
        }
        {
            JPanel padding_right = new JPanel();
            padding_right.setOpaque(false);
            padding_right.setPreferredSize(new Dimension(50,25));
            this.add(padding_right, BorderLayout.EAST);
        }


        {

            JPanel content = new JPanel(new BorderLayout());
            content.setOpaque(false);
            this.add(content, BorderLayout.CENTER);

            {
                JPanel aboveContent = new JPanel(new BorderLayout());
                aboveContent.setOpaque(false);
                content.add(aboveContent, BorderLayout.NORTH);

                JPanel preContent = new JPanel();
                preContent.setOpaque(false);
                preContent.setLayout(new BoxLayout(preContent, BoxLayout.Y_AXIS));
                aboveContent.add(preContent, BorderLayout.CENTER);

                preContent.add(new Padding(20));

                preContent.add(new StyledLabel("A project may utilize functions from another Trident project.", "ProjectProperties.content", tlm));
                preContent.add(new StyledLabel("Select a project from the workspace or the .tdnproj file of the project to depend on.", "ProjectProperties.content", tlm));
                preContent.add(new Padding(11));
                preContent.add(new StyledLabel("The projects below will be applied from bottom to top, each", "ProjectProperties.content", tlm));
                preContent.add(new StyledLabel("overwriting any repeated definitions from those below.", "ProjectProperties.content", tlm));

                preContent.add(new Padding(10));

                JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
                controls.setAlignmentX(LEFT_ALIGNMENT);
                controls.setOpaque(false);


                ToolbarButton addBtn = new ToolbarButton("add", tlm);
                addBtn.setText("Add");
                addBtn.setHintText("Add project dependency");

                addBtn.addActionListener(e -> {
                    StyledPopupMenu menu = new StyledPopupMenu("What is supposed to go here?");

                    for(DependencyToken token : getUnassignedProjects()) {
                        StyledMenuItem item = new StyledMenuItem(token.getTitle(), token.getIconName());

                        item.addActionListener(aa -> {
                            master.preAddItem(new StandardOrderListItem(master, token));
                            master.repaint();
                        });

                        menu.add(item);
                    }

                    {
                        StyledMenuItem item = new StyledMenuItem("Browse...");

                        item.addActionListener(aa -> {
                            FileDialog fd = new FileDialog(ProjectProperties.dialog, "Select .tdnproj file", FileDialog.LOAD);
                            fd.setFile("*.tdnproj");
                            //fd.setFilenameFilter((f, name) -> f.getName().equals(".tdnproj"));
                            fd.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
                            fd.setVisible(true);

                            File[] results = fd.getFiles();
                            if(results != null && results.length >= 1) {
                                File resultFile = results[0].getParentFile();
                                master.preAddItem(new StandardOrderListItem(master, new DependencyToken(resultFile)));
                                master.repaint();
                            }
                        });

                        menu.add(item);
                    }

                    menu.show(addBtn, 0, addBtn.getHeight());
                });

                controls.add(addBtn);

                preContent.add(controls);



                JPanel tableHead = new JPanel(new BorderLayout());
                tableHead.setAlignmentX(LEFT_ALIGNMENT);
                tableHead.setPreferredSize(new Dimension(1, 16));

                JPanel exportHead = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                exportHead.setOpaque(false);
                exportHead.setPreferredSize(new Dimension(38, 16));
                tableHead.add(exportHead, BorderLayout.WEST);

                StyledLabel exportHeadLabel = new StyledLabel("Export", "ProjectProperties.tablehead", tlm);
                exportHead.add(exportHeadLabel);


                JPanel rightHead = new JPanel(new BorderLayout());
                rightHead.setOpaque(false);
                rightHead.setPreferredSize(new Dimension(108+72, 16));
                tableHead.add(rightHead, BorderLayout.EAST);


                JPanel modeHead = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                modeHead.setOpaque(false);
                modeHead.setPreferredSize(new Dimension(72, 16));
                rightHead.add(modeHead, BorderLayout.WEST);

                StyledLabel modeHeadLabel = new StyledLabel("Mode", "ProjectProperties.tablehead", tlm);
                modeHead.add(modeHeadLabel);


                tlm.addThemeChangeListener(t -> {
                    tableHead.setBackground(t.getColor(Color.GRAY, "ProjectProperties.tablehead.background", "ProjectProperties.content.background"));
                    exportHead.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, t.getColor(Color.WHITE, "ProjectProperties.tablehead.separator")));
                    modeHead.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, t.getColor(Color.WHITE, "ProjectProperties.tablehead.separator")));
                    rightHead.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, t.getColor(Color.WHITE, "ProjectProperties.tablehead.separator")));
                });

                aboveContent.add(tableHead, BorderLayout.SOUTH);

            }

            ProjectProperties.addOpenEvent(p -> {
                master.removeAllElements();
                updateLoadedProjects(p);

                JsonObject config = p.getConfig();
                if(config.has("dependencies") && config.get("dependencies").isJsonArray()) {
                    JsonArray arr = config.getAsJsonArray("dependencies");
                    if(arr.size() > 0) {
                        for(JsonElement elem : arr) {
                            if(elem.isJsonObject()) {
                                JsonObject asObj = elem.getAsJsonObject();

                                loadDependencyJsonObj(asObj, p);
                            }
                        }
                    }
                }
            });

            ProjectProperties.addApplyEvent(p -> {
                List<DependencyToken> tokens = master.getAllElements().stream()
                        .filter(e -> e instanceof StandardOrderListItem)
                        .map(e -> ((DependencyToken) ((StandardOrderListItem) e).getToken()))
                        .collect(Collectors.toList());

                JsonObject config = p.getConfig();
                if(tokens.isEmpty()) {
                    config.remove("dependencies");
                } else {
                    JsonArray arr = new JsonArray(tokens.size());
                    config.add("dependencies", arr);
                    for(int i = tokens.size()-1; i >= 0; i--) {
                        JsonObject obj = tokens.get(i).createDependencyObj();
                        arr.add(obj);
                    }
                }
            });

            JScrollPane sp = new JScrollPane(master);
            sp.setBorder(new EmptyBorder(0,0,0,0));
            sp.setLayout(new OverlayScrollPaneLayout(sp));

            content.add(sp, BorderLayout.CENTER);
        }
    }

    private void loadDependencyJsonObj(JsonObject obj, TridentProject project) {
        if(obj.has("path") && obj.get("path").isJsonPrimitive() && obj.get("path").getAsJsonPrimitive().isString()) {
            String path = obj.get("path").getAsString();
            DependencyToken token = getTokenForPath(path, project);

            if(token != null) {
                if(obj.has("export") && obj.get("export").isJsonPrimitive() && obj.get("export").getAsJsonPrimitive().isBoolean()) {
                    token.setExport(obj.get("export").getAsBoolean());
                }
                if(obj.has("mode") && obj.get("mode").isJsonPrimitive() && obj.get("mode").getAsJsonPrimitive().isString()) {
                    token.setCombine(obj.get("mode").getAsString().equals("combine"));
                }

                master.preAddItem(new StandardOrderListItem(master, token));
            }
        }
    }

    private DependencyToken getTokenForPath(String path, TridentProject project) {
        try {
            File pointsTo = TridentCompiler.newFileObject(path, project.getRootDirectory());
            if(!loadedProjects.containsKey(pointsTo)) {
                loadedProjects.put(pointsTo, new DependencyToken(pointsTo));
            }
            return loadedProjects.get(pointsTo);
        } catch (InvalidPathException x) {
            x.printStackTrace();
            return null;
        }
    }

    private void updateLoadedProjects(TridentProject project) {
        loadedProjects.clear();

        for(Project p : ProjectManager.getLoadedProjects()) {
            if(!(p instanceof TridentProject)) continue;
            if(p != project) {
                loadedProjects.put(p.getRootDirectory(), new DependencyToken((TridentProject) p));
            }
        }
    }

    private Collection<DependencyToken> getUnassignedProjects() {
        ArrayList<DependencyToken> unassignedPacks = new ArrayList<>(loadedProjects.values());
        for(OrderListElement elem : master.getAllElements()) {
            unassignedPacks.removeIf(t -> t == ((StandardOrderListItem) elem).getToken());
        }
        return unassignedPacks;
    }

    public ProjectPropertiesDependencies() {
        super(new BorderLayout());
    }
}
