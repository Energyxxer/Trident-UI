package com.energyxxer.trident.guardian.dialogs.project_properties;

import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.ui.ToolbarButton;
import com.energyxxer.guardian.ui.commodoreresources.Plugins;
import com.energyxxer.guardian.ui.orderlist.OrderListElement;
import com.energyxxer.guardian.ui.orderlist.OrderListMaster;
import com.energyxxer.guardian.ui.orderlist.StandardOrderListItem;
import com.energyxxer.guardian.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.guardian.ui.scrollbar.OverlayScrollPaneLayout;
import com.energyxxer.guardian.ui.styledcomponents.StyledLabel;
import com.energyxxer.guardian.ui.styledcomponents.StyledMenuItem;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.trident.TridentSuiteConfiguration;
import com.energyxxer.trident.guardian.TridentPluginLoader;
import com.energyxxer.trident.guardian.TridentProject;
import com.energyxxer.xswing.Padding;
import com.energyxxer.xswing.ScalableDimension;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;

public class ProjectPropertiesPlugins extends JPanel {

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private LinkedHashMap<String, ProjectResourceKeyToken> possiblePacks = new LinkedHashMap<>();

    private final OrderListMaster master;

    {
        master = new OrderListMaster();

        {
            JPanel header = new JPanel(new BorderLayout());
            header.setPreferredSize(new ScalableDimension(0,40));
            this.add(header, BorderLayout.NORTH);

            {
                JPanel padding = new JPanel();
                padding.setOpaque(false);
                padding.setPreferredSize(new ScalableDimension(25,25));
                header.add(padding, BorderLayout.WEST);
            }

            StyledLabel label = new StyledLabel("Plugins", "ProjectProperties.content.header", tlm);
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
            padding_left.setPreferredSize(new ScalableDimension(50,25));
            this.add(padding_left, BorderLayout.WEST);
        }
        {
            JPanel padding_right = new JPanel();
            padding_right.setOpaque(false);
            padding_right.setPreferredSize(new ScalableDimension(50,25));
            this.add(padding_right, BorderLayout.EAST);
        }


        {

            JPanel content = new JPanel(new BorderLayout());
            content.setOpaque(false);
            OverlayScrollPane scrollPane = new OverlayScrollPane(tlm, content);
            scrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
            this.add(scrollPane, BorderLayout.CENTER);

            this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    master.setPreferredSize(null);
                    master.revalidate();
                }
            });

            {
                JPanel preContent = new JPanel();
                preContent.setOpaque(false);
                preContent.setLayout(new BoxLayout(preContent, BoxLayout.Y_AXIS));
                content.add(preContent, BorderLayout.NORTH);

                preContent.add(new Padding(20));

                preContent.add(new StyledLabel("Plugins may add language features such as custom commands, which may be used by projects.", "ProjectProperties.content", tlm));
                preContent.add(new Padding(11));
                preContent.add(new StyledLabel("They can either be provided by", "ProjectProperties.content", tlm));
                preContent.add(new StyledLabel("the project directory (for project-specific plugins), or from a", "ProjectProperties.content", tlm));
                preContent.add(new StyledLabel("subdirectory in the IDE (for shared use by all projects in the workspace).", "ProjectProperties.content", tlm));

                preContent.add(new Padding(10));

                JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
                controls.setAlignmentX(LEFT_ALIGNMENT);
                controls.setOpaque(false);


                ToolbarButton addBtn = new ToolbarButton("add", tlm);
                addBtn.setText("Add");
                addBtn.setHintText("Add plugin");

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
                openProjectBtn.setText("Project plugins");
                openProjectBtn.setHintText("Open project plugins directory");
                openProjectBtn.addActionListener(e -> {
                    File dir = new File(ProjectProperties.project.getRootDirectory().getPath() + File.separator + "plugins" + File.separator);
                    dir.mkdir();
                    Commons.openInExplorer(dir.getPath());
                });
                controls.add(openProjectBtn);


                ToolbarButton openIdePacksBtn = new ToolbarButton("package", tlm);
                openIdePacksBtn.setText("IDE plugins");
                openIdePacksBtn.setHintText("Open Trident UI's plugins directory");
                openIdePacksBtn.addActionListener(e -> {
                    File dir = TridentPluginLoader.INSTANCE.getPluginsDirectory();
                    dir.mkdir();
                    Commons.openInExplorer(dir.getPath());
                });
                controls.add(openIdePacksBtn);



                preContent.add(controls);

            }

            ProjectProperties.addOpenEvent(p -> {
                master.removeAllElements();
                collectPossiblePacks(p);

                JsonObject config = p.getProjectConfigJson();
                if(config.has("use-plugins") && config.get("use-plugins").isJsonArray()) {
                    JsonArray arr = config.getAsJsonArray("use-plugins");
                    if(arr.size() > 0) {
                        for(JsonElement elem : arr) {
                            if(elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) {
                                String asString = elem.getAsString();
                                if(!asString.isEmpty()) {
                                    master.preAddItem(new StandardOrderListItem(master, getTokenForPack(asString)));
                                }
                            }
                        }
                    }
                }
            });

            ProjectProperties.addApplyEvent(p -> {
                List<String> entries = master.getAllElements().stream()
                        .map(e -> ((ProjectResourceKeyToken) ((StandardOrderListItem) e).getToken()))
                        .map(ProjectResourceKeyToken::getPackName)
                        .collect(Collectors.toList());

                JsonObject config = p.getProjectConfigJson();

                if(entries.isEmpty()) {
                    config.remove("use-plugins");
                } else {
                    JsonArray arr = new JsonArray(entries.size());
                    while(!entries.isEmpty()) {
                        arr.add(entries.remove(entries.size()-1));
                    }
                    config.add("use-plugins", arr);
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
        if(known == null) known = new ProjectResourceKeyToken(ProjectResourceKeyToken.Type.PLUGIN, packName, ProjectResourceKeyToken.Source.UNKNOWN);
        return known;
    }

    private void collectPossiblePacks(TridentProject project) {
        possiblePacks.clear();

        //Collect in project
        File inProjectDir = new File(project.getRootDirectory().getPath() + File.separator + "plugins" + File.separator);
        if(inProjectDir.isDirectory()) {
            File[] files = inProjectDir.listFiles();
            if(files != null) {
                for(File packRoot : files) {
                    String packName = packRoot.getName();
                    if(packRoot.isFile() && packName.endsWith(".zip")) {
                        packName = packName.substring(0, packName.length() - ".zip".length());
                    }
                    ProjectResourceKeyToken token = new ProjectResourceKeyToken(ProjectResourceKeyToken.Type.PLUGIN, packName, ProjectResourceKeyToken.Source.PROJECT);
                    possiblePacks.putIfAbsent(packName, token);
                }
            }
        }

        //Collect from memory
        for(String packName : Plugins.getAliasMap(TridentSuiteConfiguration.INSTANCE).keySet()) {
            possiblePacks.putIfAbsent(packName, new ProjectResourceKeyToken(ProjectResourceKeyToken.Type.PLUGIN, packName, ProjectResourceKeyToken.Source.IDE));
        }
    }

    private Collection<ProjectResourceKeyToken> getUnassignedPacks() {
        ArrayList<ProjectResourceKeyToken> unassignedPacks = new ArrayList<>(possiblePacks.values());
        for(OrderListElement elem : master.getAllElements()) {
            unassignedPacks.removeIf(t -> t == ((StandardOrderListItem) elem).getToken());
        }
        return unassignedPacks;
    }

    public ProjectPropertiesPlugins() {
        super(new BorderLayout());
    }
}
