package com.energyxxer.trident.ui.dialogs;

import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.styledcomponents.StyledButton;
import com.energyxxer.trident.ui.styledcomponents.StyledLabel;
import com.energyxxer.trident.ui.styledcomponents.StyledTextField;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.Padding;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class PromptDialog {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 110;

    private final StyledTextField field;
    public String result = null;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    public PromptDialog(String title, String query) {
        JDialog dialog = new JDialog(TridentWindow.jframe);

        JPanel pane = new JPanel(new BorderLayout());
        pane.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        tlm.addThemeChangeListener(t -> pane.setBackground(t.getColor(new Color(235, 235, 235), "PromptDialog.background")));

        pane.add(new Padding(10), BorderLayout.NORTH);
        pane.add(new Padding(25), BorderLayout.WEST);
        pane.add(new Padding(25), BorderLayout.EAST);
        pane.add(new Padding(10), BorderLayout.SOUTH);

        {
            JPanel content = new JPanel(new BorderLayout());
            content.setOpaque(false);

            JPanel subContent = new JPanel();
            subContent.setLayout(new BoxLayout(subContent, BoxLayout.PAGE_AXIS));
            subContent.setOpaque(false);
            content.add(subContent);

            StyledLabel label = new StyledLabel(query, "PromptDialog");
            subContent.add(label, BorderLayout.CENTER);

            field = new StyledTextField();
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));

            subContent.add(field);

            {
                JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttons.setOpaque(false);
                buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

                StyledButton okButton = new StyledButton("OK");
                okButton.addActionListener(e -> {
                    result = field.getText();
                    dialog.setVisible(false);
                });
                buttons.add(okButton);

                StyledButton cancelButton = new StyledButton("Cancel");
                cancelButton.addActionListener(e -> {
                    result = null;
                    dialog.setVisible(false);
                });

                buttons.add(cancelButton);
                content.add(buttons, BorderLayout.SOUTH);
            }

            pane.add(content, BorderLayout.CENTER);
        }

        pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit");
        pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        pane.getActionMap().put("submit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = field.getText();
                dialog.setVisible(false);
                tlm.dispose();
                dialog.dispose();
            }
        });
        pane.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = null;
                dialog.setVisible(false);
                tlm.dispose();
                dialog.dispose();
            }
        });

        dialog.setContentPane(pane);
        dialog.pack();

        dialog.setTitle(title);

        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        center.x -= dialog.getWidth()/2;
        center.y -= dialog.getHeight()/2;

        dialog.setLocation(center);

        dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);

        field.requestFocus();
        dialog.setVisible(true);
    }
}
