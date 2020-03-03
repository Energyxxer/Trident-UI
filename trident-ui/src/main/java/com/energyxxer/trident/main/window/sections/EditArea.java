package com.energyxxer.trident.main.window.sections;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.ToolbarButton;
import com.energyxxer.trident.ui.modules.FileModuleToken;
import com.energyxxer.trident.ui.scrollbar.InvisibleScrollPaneLayout;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.xswing.Padding;
import com.energyxxer.xswing.ScalableDimension;
import com.energyxxer.xswing.hints.Hint;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

/**
 * Created by User on 12/15/2016.
 */
public class EditArea extends JPanel {

    private JPanel tabList;
    
    private ThemeListenerManager tlm = new ThemeListenerManager();

    private JComponent content = null;

    public final TransferHandler dragToOpenFileHandler = new TransferHandler("filepath") {
        @Override
        public Image getDragImage() {
            return Commons.getIcon("file");
        }

        public boolean canImport(TransferSupport support) {
            return EditArea.this.canImport(support.getTransferable());
        }

        @SuppressWarnings("unchecked")
        public boolean importData(TransferSupport support) {
            return EditArea.this.importData(support.getTransferable());
        }
    };

    {
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new ScalableDimension(500, 500));
        tlm.addThemeChangeListener(t -> this.setBackground(t.getColor(new Color(215, 215, 215), "Editor.background")));

        JPanel tabListHolder = new JPanel(new BorderLayout());
        tlm.addThemeChangeListener(t -> {
            tabListHolder.setBackground(t.getColor(new Color(200, 202, 205), "TabList.background"));
            tabListHolder.setPreferredSize(new ScalableDimension(1, t.getInteger(30, "TabList.height")));
        });

        JPanel tabActionPanel = new JPanel(new GridBagLayout());
        tabActionPanel.setOpaque(false);
        tlm.addThemeChangeListener(t -> tabActionPanel.setBorder(BorderFactory.createMatteBorder(0, 0, Math.max(t.getInteger(1,"TabList.border.thickness"),0), 0, t.getColor(new Color(200, 200, 200), "TabList.border.color"))));

        {
            ToolbarButton more = new ToolbarButton("more", tlm);
            more.setHintText("View all tabs");
            more.setPreferredHintPos(Hint.LEFT);
            tabActionPanel.add(more);

            more.addActionListener(e -> TridentWindow.tabManager.getMenu().show(more, more.getWidth()/2, more.getHeight()));
        }
        tabActionPanel.add(new Padding(5));

        tabListHolder.add(tabActionPanel, BorderLayout.EAST);

        this.add(tabListHolder, BorderLayout.NORTH);

        tabList = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tlm.addThemeChangeListener(t -> {
            tabList.setBackground(t.getColor(new Color(200, 202, 205), "TabList.background"));
            tabList.setPreferredSize(new ScalableDimension(1, t.getInteger(30, "TabList.height")));
            tabList.setBorder(BorderFactory.createMatteBorder(0, 0, Math.max(t.getInteger(1,"TabList.border.thickness"),0), 0, t.getColor(new Color(200, 200, 200), "TabList.border.color")));
        });

        JScrollPane tabSP = new JScrollPane(TridentWindow.tabList);
        tabSP.setBorder(BorderFactory.createEmptyBorder());
        tabSP.setLayout(new InvisibleScrollPaneLayout(tabSP));
        tabSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        tabListHolder.add(tabSP, BorderLayout.CENTER);

        this.setContent(TridentWindow.welcomePane);

        this.setTransferHandler(dragToOpenFileHandler);

    }

    public void setContent(JComponent content) {
        if(this.content != null) {
            if(this.content == TridentWindow.welcomePane) {
                TridentWindow.welcomePane.tipScreen.pause();
            }
            this.remove(this.content);
        }
        if(content == null) content = TridentWindow.welcomePane;

        this.add(content, BorderLayout.CENTER);
        this.content = content;
        if(content == TridentWindow.welcomePane) {
            if(TridentWindow.isVisible()) TridentWindow.welcomePane.tipScreen.start();
        }

        this.revalidate();
        this.repaint();
    }

    public boolean canImport(Transferable support) {
        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    @SuppressWarnings("unchecked")
    public boolean importData(Transferable t) {
        if (!canImport(t)) {
            return false;
        }

        List<File> files;

        try {
            files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
        } catch (Exception e) {
            Debug.log(e, Debug.MessageType.ERROR);
            return false;
        }

        for(File file : files) {
            if(file.isFile() && file.exists()) {
                TridentWindow.tabManager.openTab(new FileModuleToken(file));
            }
        }

        return true;
    }
}
