package com.energyxxer.trident.ui.dialogs.project_properties;

import com.energyxxer.trident.global.TabManager;
import com.energyxxer.trident.ui.Tab;
import com.energyxxer.trident.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.trident.ui.styledcomponents.StyledLabel;
import com.energyxxer.trident.ui.tablist.TabItem;
import com.energyxxer.trident.ui.tablist.TabListMaster;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.Padding;
import com.energyxxer.xswing.ScalableDimension;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;

class ProjectPropertiesAliases extends JPanel {

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private TabListMaster tabList;
    private TabManager tabManager;
    private JComponent currentView;

    private LinkedHashMap<String, AliasTabToken> tokens = new LinkedHashMap<>();

    private final TabItem addItem;

    {
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

            StyledLabel label = new StyledLabel("Type Aliases", "ProjectProperties.content.header", tlm);
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
            this.add(new OverlayScrollPane(tlm, content), BorderLayout.CENTER);

            {
                JPanel preContent = new JPanel();
                preContent.setOpaque(false);
                preContent.setLayout(new BoxLayout(preContent, BoxLayout.Y_AXIS));
                content.add(preContent, BorderLayout.NORTH);

                preContent.add(new Padding(20));

                preContent.add(new StyledLabel("Type key-value pairs, separated by equals (=) signs to create a type alias for the corresponding type category.", "ProjectProperties.content", tlm));
                preContent.add(new StyledLabel("Aliases will allow you to automatically replace appearances of an alias type with its real name.", "ProjectProperties.content", tlm));
                preContent.add(new Padding(11));
                preContent.add(new StyledLabel("Category names include (but are not limited to):", "ProjectProperties.content", tlm));
                preContent.add(new StyledLabel("block, item, entity, particle, enchantment, effect...", "ProjectProperties.content", tlm));

                preContent.add(new Padding(10));
            }

            JPanel switcher = new JPanel(new BorderLayout());
            //switcher.setOpaque(false);
            switcher.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(switcher);

            tabList = new TabListMaster();
            tabList.setMayRearrange(false);
            tabManager = new TabManager(tabList, c -> {
                if(currentView != null) {
                    switcher.remove(currentView);
                }
                currentView = c;
                if(c != null) {
                    switcher.add(currentView);
                }

                switcher.revalidate();
                switcher.repaint();
            });

            addItem = new TabItem(tabManager, new AliasAddToken(this));

            ProjectProperties.addOpenEvent(p -> {
                tabManager.closeAllTabs(true);
                tabList.removeAllTabs();
                tokens.clear();

                openCategory("block", false);
                openCategory("item", false);
                openCategory("entity", false);

                JsonElement aliasesElem = p.getConfig().get("aliases");
                if(aliasesElem != null && aliasesElem.isJsonObject()) {
                    for(String category : aliasesElem.getAsJsonObject().keySet()) {
                        openCategory(category);
                    }
                }

                tabManager.setSelectedTab(tabManager.openTabs.get(0));
            });

            switcher.add(tabList, BorderLayout.NORTH);
            currentView = new JPanel();
            switcher.add(currentView);


            ProjectProperties.addApplyEvent(p -> {
                for(Tab tab : tabManager.openTabs) {
                    tab.module.save();
                }
                JsonObject config = p.getConfig();
                if(config.has("aliases") && config.get("aliases").isJsonObject() && config.getAsJsonObject("aliases").keySet().isEmpty()) {
                    config.remove("aliases");
                }
            });

            ProjectProperties.addCloseEvent(() -> {
                tabManager.closeAllTabs(true);
                tabList.removeAllTabs();
                tokens.clear();
            });
        }

    }

    void openCategory(String category) {
        openCategory(category, true);
    }

    private void openCategory(String category, boolean switchToTab) {
        AliasTabToken token;
        if(!tokens.containsKey(category)) {
            tabList.removeTab(addItem);
            token = new AliasTabToken(category);
            tabManager.openTab(token);
            tokens.put(category, token);
            tabList.addTab(addItem);
        } else {
            token = tokens.get(category);
        }
        if(switchToTab) {
            Tab tabToSelect = tabManager.getTabForToken(token);
            if(tabToSelect != null) tabManager.setSelectedTab(tabToSelect);
        }
    }

    ProjectPropertiesAliases() {
        super(new BorderLayout());
    }
}
