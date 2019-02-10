package com.energyxxer.trident.main.window.sections;

import com.energyxxer.trident.global.Resources;
import com.energyxxer.trident.global.Status;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.styledcomponents.StyledLabel;
import com.energyxxer.trident.ui.theme.Theme;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by User on 12/15/2016.
 */
public class StatusBar extends JPanel implements MouseListener {

    private StyledLabel statusLabel;

    private Status currentStatus = null;

    private ProgressBar progressBar;

    private ExtendedStatusBar extension = new ExtendedStatusBar();

    private ThemeListenerManager tlm = new ThemeListenerManager();

    {
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(1, 25));

        tlm.addThemeChangeListener(t ->
            SwingUtilities.invokeLater(() -> {
                this.setBackground(t.getColor(new Color(235, 235, 235), "Status.background"));
                this.setBorder(
                    new CompoundBorder(
                        new MatteBorder(Math.max(t.getInteger("Status.border.thickness"),0), 0, 0, 0, t.getColor(new Color(200, 200, 200), "Status.border.color")),
                        new EmptyBorder(0,5,0,5)
                ));}
            )
        );

        JPanel progressWrapper = new JPanel(new BorderLayout());
        progressWrapper.setOpaque(false);

        statusLabel = new StyledLabel("");
        statusLabel.setIconName("info");
        this.add(statusLabel,BorderLayout.WEST);
        this.add(progressWrapper, BorderLayout.CENTER);

        progressBar = new ProgressBar();
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(200, 5));
        progressWrapper.add(progressBar, BorderLayout.EAST);

        this.add(extension,BorderLayout.EAST);

        this.addMouseListener(this);

        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "reloadResources");
        this.getActionMap().put("reloadResources", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Resources.load();
            }
        });
    }

    public void setStatus(String text) {
        setStatus(new Status(text));
    }

    public void setStatus(Status status) {

        Theme t = TridentWindow.getTheme();

        statusLabel.setForeground(t.getColor(Color.BLACK, "Status." + status.getType().toLowerCase(),"General.foreground"));
        statusLabel.setIconName(status.getType().toLowerCase());
        statusLabel.setText(status.getMessage());

        setProgress(status.getProgress());

        this.currentStatus = status;
    }

    public void setProgress(Float progress) {
        progressBar.setVisible(progress != null);
        if(progress != null) progressBar.setProgress(progress);
    }

    public void dismissStatus(Status status) {
        if(status == currentStatus) {
            statusLabel.setText("");
            statusLabel.setBackground(new Color(0,0,0,0));
            progressBar.setVisible(false);
        }
    }

    public void setCaretInfo(String text) {
        extension.setCaretInfo(text);
    }

    public void setSelectionInfo(String text) {
        extension.setSelectionInfo(text);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if((e.getClickCount() & 1) == 0) {
            TridentWindow.toolBoard.toggle();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
