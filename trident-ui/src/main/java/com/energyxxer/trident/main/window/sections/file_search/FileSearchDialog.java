package com.energyxxer.trident.main.window.sections.file_search;

import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.styledcomponents.StyledTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

public class FileSearchDialog extends JDialog implements WindowFocusListener {

    public static final FileSearchDialog INSTANCE = new FileSearchDialog();

    private JPanel contentPanel = new JPanel(new BorderLayout());
    private StyledTextField field;

    private FileSearchDialog() {
        super(TridentWindow.jframe, false);
        setup();
    }

    private void setup() {
        this.setUndecorated(true);

        this.setContentPane(contentPanel);

        contentPanel.setPreferredSize(new Dimension(400, 300));
        JPanel header = new JPanel(new BorderLayout());
        this.field = new StyledTextField();
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
        header.add(this.field, BorderLayout.SOUTH);
        contentPanel.add(header, BorderLayout.NORTH);

        //this.addMouseListener(this);
        //this.addMouseMotionListener(this);
        this.addWindowFocusListener(this);

        this.pack();
        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        Dimension size = this.getSize();
        center.x -= size.width/2;
        center.y -= size.height/2;
        this.setLocation(center);
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {

    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        this.setVisible(false);
    }

    public void reveal() {
        this.setVisible(true);
        this.field.requestFocus();
        this.field.setSelectionStart(0);
        this.field.setSelectionEnd(this.field.getText().length());
    }

    public void dismiss() {
        this.setVisible(false);
    }
}
