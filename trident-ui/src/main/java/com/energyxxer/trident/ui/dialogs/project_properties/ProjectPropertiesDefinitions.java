package com.energyxxer.trident.ui.dialogs.project_properties;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.global.temp.projects.TridentProject;
import com.energyxxer.trident.ui.ToolbarButton;
import com.energyxxer.trident.ui.commodoreresources.DefinitionPacks;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectPropertiesDefinitions extends JPanel {

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private LinkedHashMap<String, ProjectResourceKeyToken> possiblePacks = new LinkedHashMap<>();

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

            StyledLabel label = new StyledLabel("Definitions", "ProjectProperties.content.header", tlm);
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
                JPanel preContent = new JPanel();
                preContent.setOpaque(false);
                preContent.setLayout(new BoxLayout(preContent, BoxLayout.Y_AXIS));
                content.add(preContent, BorderLayout.NORTH);

                preContent.add(new Padding(20));

                preContent.add(new StyledLabel("Definition packs determine what types (blocks/items/entities...) can be used by a project.", "ProjectProperties.content", tlm));
                preContent.add(new Padding(11));
                preContent.add(new StyledLabel("They can either be provided by the target version,", "ProjectProperties.content", tlm));
                preContent.add(new StyledLabel("the project directory (for project-specific definitions), or from a", "ProjectProperties.content", tlm));
                preContent.add(new StyledLabel("subdirectory in the IDE (for shared use by all projects in the workspace).", "ProjectProperties.content", tlm));

                preContent.add(new Padding(10));

                JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
                controls.setAlignmentX(LEFT_ALIGNMENT);
                controls.setOpaque(false);


                ToolbarButton addBtn = new ToolbarButton("add", tlm);
                addBtn.setText("Add");
                addBtn.setHintText("Add definition pack");

                addBtn.addActionListener(e -> {
                    StyledPopupMenu menu = new StyledPopupMenu("What is supposed to go here?");

                    for(ProjectResourceKeyToken token : getUnassignedPacks()) {
                        StyledMenuItem item = new StyledMenuItem(token.getTitle(), token.getIconName());

                        item.addActionListener(aa -> {
                            master.preAddItem(new StandardOrderListItem(master, token));
                            master.repaint();
                        });

                        menu.add(item);
                    }

                    menu.show(addBtn, 0, addBtn.getHeight());
                });

                controls.add(addBtn);

                controls.add(new Padding(50, 1));


                ToolbarButton openProjectBtn = new ToolbarButton("project_content", tlm);
                openProjectBtn.setText("Project definitions");
                openProjectBtn.setHintText("Open project definition pack directory");
                openProjectBtn.addActionListener(e -> {
                    File dir = new File(ProjectProperties.project.getRootDirectory().getPath() + File.separator + "defpacks" + File.separator);
                    dir.mkdir();
                    Commons.openInExplorer(dir.getPath());
                });
                controls.add(openProjectBtn);


                ToolbarButton openIdePacksBtn = new ToolbarButton("package", tlm);
                openIdePacksBtn.setText("IDE definitions");
                openIdePacksBtn.setHintText("Open Trident UI's definition pack directory");
                openIdePacksBtn.addActionListener(e -> {
                    File dir = new File(DefinitionPacks.DEF_PACK_DIR_PATH);
                    dir.mkdir();
                    Commons.openInExplorer(dir.getPath());
                });
                controls.add(openIdePacksBtn);



                preContent.add(controls);

            }

            ProjectProperties.addOpenEvent(p -> {
                master.removeAllElements();
                collectPossiblePacks(p);

                JsonObject config = p.getConfig();
                if(config.has("use-definitions") && config.get("use-definitions").isJsonArray()) {
                    JsonArray arr = config.getAsJsonArray("use-definitions");
                    if(arr.size() > 0) {

                        for(JsonElement elem : arr) {
                            if(elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) {
                                String asString = elem.getAsString();
                                if(asString.equals("DEFAULT")) {
                                    master.preAddItem(new StandardOrderListItem(master, possiblePacks.get("DEFAULT")));
                                } else if(!asString.isEmpty()) {
                                    master.preAddItem(new StandardOrderListItem(master, getTokenForPack(asString)));
                                }
                            }
                        }
                        return;
                    }
                }

                master.addItem(new StandardOrderListItem(master, possiblePacks.get("DEFAULT")));
            });

            ProjectProperties.addApplyEvent(p -> {
                List<String> entries = master.getAllElements().stream()
                        .map(e -> ((ProjectResourceKeyToken) ((StandardOrderListItem) e).getToken()))
                        .map(t -> t.getPackName() == null ? "DEFAULT" : t.getPackName())
                        .collect(Collectors.toList());

                JsonObject config = p.getConfig();

                if(entries.isEmpty() || (entries.size() == 1 && "DEFAULT".equals(entries.get(0)))) {
                    config.remove("use-definitions");
                } else {
                    JsonArray arr = new JsonArray(entries.size());
                    while(!entries.isEmpty()) {
                        arr.add(entries.remove(entries.size()-1));
                    }
                    config.add("use-definitions", arr);
                }
            });

            JScrollPane sp = new JScrollPane(master);
            sp.setBorder(new EmptyBorder(0,0,0,0));
            sp.setLayout(new OverlayScrollPaneLayout(sp, tlm));

            content.add(sp, BorderLayout.CENTER);
        }
    }

    private ProjectResourceKeyToken getTokenForPack(String packName) {
        ProjectResourceKeyToken known = possiblePacks.get(packName);
        if(known == null) known = new ProjectResourceKeyToken(ProjectResourceKeyToken.Type.DEFINITION_PACK, packName, ProjectResourceKeyToken.Source.UNKNOWN);
        return known;
    }

    private void collectPossiblePacks(TridentProject project) {
        possiblePacks.clear();

        possiblePacks.put("DEFAULT", new ProjectResourceKeyToken(ProjectResourceKeyToken.Type.DEFINITION_PACK, null, ProjectResourceKeyToken.Source.UNKNOWN));

        //Collect in project
        File inProjectDir = new File(project.getRootDirectory().getPath() + File.separator + "defpacks" + File.separator);
        if(inProjectDir.isDirectory()) {
            File[] files = inProjectDir.listFiles();
            if(files != null) {
                for(File packRoot : files) {
                    String packName = packRoot.getName();
                    if(packRoot.isFile() && packName.endsWith(".zip")) {
                        packName = packName.substring(0, packName.length() - ".zip".length());
                    }
                    ProjectResourceKeyToken token = new ProjectResourceKeyToken(ProjectResourceKeyToken.Type.DEFINITION_PACK, packName, ProjectResourceKeyToken.Source.PROJECT);
                    possiblePacks.putIfAbsent(packName, token);
                }
            }
        }

        //Collect from memory
        for(String packName : DefinitionPacks.getAliasMap().keySet()) {
            possiblePacks.putIfAbsent(packName, new ProjectResourceKeyToken(ProjectResourceKeyToken.Type.DEFINITION_PACK, packName, ProjectResourceKeyToken.Source.IDE));
        }
    }

    private Collection<ProjectResourceKeyToken> getUnassignedPacks() {
        ArrayList<ProjectResourceKeyToken> unassignedPacks = new ArrayList<>(possiblePacks.values());
        for(OrderListElement elem : master.getAllElements()) {
            unassignedPacks.removeIf(t -> t == ((StandardOrderListItem) elem).getToken());
        }
        return unassignedPacks;
    }

    public ProjectPropertiesDefinitions() {
        super(new BorderLayout());
    }
}
