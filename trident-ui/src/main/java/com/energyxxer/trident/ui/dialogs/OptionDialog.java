package com.energyxxer.trident.ui.dialogs;

import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.styledcomponents.StyledButton;
import com.energyxxer.trident.ui.styledcomponents.StyledLabel;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.Padding;
import com.energyxxer.xswing.ScalableDimension;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by User on 2/11/2017.
 */
public class OptionDialog {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 110;

    public String result = null;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    public OptionDialog(String title, String query, String[] options) {
        JDialog dialog = new JDialog(TridentWindow.jframe);

        JPanel pane = new JPanel(new BorderLayout());
        //pane.setPreferredSize(new ScalableDimension(WIDTH, HEIGHT));
        tlm.addThemeChangeListener(t ->
                pane.setBackground(t.getColor(new Color(235, 235, 235), "OptionDialog.background"))
        );

        pane.add(new Padding(10), BorderLayout.NORTH);
        pane.add(new Padding(25), BorderLayout.WEST);
        pane.add(new Padding(25), BorderLayout.EAST);
        pane.add(new Padding(10), BorderLayout.SOUTH);

        {
            JPanel content = new JPanel(new BorderLayout());
            content.setOpaque(false);

            StyledLabel label = new StyledLabel(query, "OptionDialog", tlm);
            label.setStyle(Font.BOLD);

            JPanel labelWrapper = new JPanel(new BorderLayout());
            labelWrapper.setOpaque(false);
            labelWrapper.add(new Padding(25), BorderLayout.NORTH);
            labelWrapper.add(label, BorderLayout.CENTER);
            labelWrapper.add(new Padding(25), BorderLayout.SOUTH);
            content.add(labelWrapper, BorderLayout.CENTER);

            {
                JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttons.setOpaque(false);
                buttons.setMaximumSize(new ScalableDimension(Integer.MAX_VALUE, 30));

                for(String option : options) {
                    StyledButton button = new StyledButton(option,"OptionDialog", tlm);
                    button.addActionListener(e -> {
                        result = option;
                        tlm.dispose();
                        dialog.setVisible(false);
                    });
                    buttons.add(button);
                }

                content.add(buttons, BorderLayout.SOUTH);
            }

            pane.add(content, BorderLayout.CENTER);
        }

        pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit");
        pane.getActionMap().put("submit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = options[0];
                tlm.dispose();
                dialog.setVisible(false);
            }
        });

        dialog.setContentPane(pane);

        dialog.setTitle(title);

        dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
                center.x -= dialog.getWidth()/2;
                center.y -= dialog.getHeight()/2;

                dialog.setLocation(center);
            }
        });

        dialog.pack();
        dialog.setVisible(true);
    }
}
