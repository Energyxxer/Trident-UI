package com.energyxxer.trident.ui.dialogs;

import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.ToolbarButton;
import com.energyxxer.trident.ui.styledcomponents.*;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.KeyInputUtils;
import com.energyxxer.xswing.Padding;
import com.energyxxer.xswing.hints.Hint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Function;

public class KeyStrokeDialog  {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 110;

    private static boolean visible = false;

    private final JDialog dialog;
    private final StyledTextField field;
    private final StyledButton okButton;
    private final Function<KeyStroke, String> warningFunction;
    private final StyledLabel warningLabel;

    public KeyStroke result = null;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    public KeyStrokeDialog(String title, String query) {
        this(title, query, null);
    }

    public KeyStrokeDialog(String title, String query, Function<KeyStroke, String> warningFunction) {
        dialog = new JDialog(TridentWindow.jframe);
        this.warningFunction = warningFunction;

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
            subContent.add(label);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);

            field = new StyledTextField();
            field.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    e.consume();
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    if(e.getModifiers() != 0 || (e.getKeyCode() != KeyEvent.VK_ESCAPE && e.getKeyCode() != KeyEvent.VK_ENTER)) {
                        setResult(KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers()));
                        e.consume();
                    }
                }
            });
            field.setPreferredSize(new Dimension(1,30));

            JPanel fieldPanel = new JPanel(new BorderLayout());
            fieldPanel.setMinimumSize(new Dimension(1,30));
            fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
            fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            fieldPanel.setOpaque(false);
            subContent.add(fieldPanel);

            fieldPanel.add(field);
            ToolbarButton extraBtn = new ToolbarButton("add", tlm);
            extraBtn.setPreferredSize(new Dimension(30, 30));
            extraBtn.setHintText("Set shortcut to");
            extraBtn.setPreferredHintPos(Hint.ABOVE);
            extraBtn.addActionListener(e -> {
                StyledPopupMenu menu = new StyledPopupMenu("What is supposed to go here?");
                menu.add(new StyledMenuItem("Set Enter") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setResult(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
                    }
                });
                menu.add(new StyledMenuItem("Set Escape") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setResult(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
                    }
                });
                menu.add(new StyledMenuItem("Set Tab") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setResult(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
                    }
                });
                menu.add(new StyledMenuItem("Set " + (System.getProperty("os.name").contains("mac") ? "Command" : "Ctrl") + "+Tab") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //noinspection MagicConstant
                        setResult(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyInputUtils.getPlatformControlMask()));
                    }
                });
                menu.add(new StyledMenuItem("Set " + (System.getProperty("os.name").contains("mac") ? "Command" : "Ctrl") + "+Shift+Tab") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //noinspection MagicConstant
                        setResult(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyInputUtils.getPlatformControlMask() | KeyEvent.SHIFT_MASK));
                    }
                });
                menu.add(new StyledMenuItem("Set Shift+Tab") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setResult(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK));
                    }
                });
                menu.show(extraBtn, 0, extraBtn.getHeight());
            });
            fieldPanel.add(extraBtn, BorderLayout.EAST);

            subContent.add(new Padding(10) {
                {
                    this.setAlignmentX(LEFT_ALIGNMENT);
                }
            });
            subContent.add(warningLabel = new StyledLabel(""));

            {
                JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
                buttons.setOpaque(false);
                buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

                okButton = new StyledButton("OK");
                okButton.addActionListener(e -> {
                    submit();
                });
                buttons.add(okButton);

                StyledButton cancelButton = new StyledButton("Cancel");
                cancelButton.addActionListener(e -> {
                    cancel();
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
                submit();
            }
        });
        pane.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
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
        visible = true;
        dialog.setVisible(true);
    }

    private void setResult(KeyStroke keyStroke) {
        if(keyStroke.getKeyCode() == KeyEvent.VK_SHIFT ||
                keyStroke.getKeyCode() == KeyEvent.VK_CONTROL ||
                keyStroke.getKeyCode() == KeyEvent.VK_META ||
                keyStroke.getKeyCode() == KeyEvent.VK_ALT) return;
        result = keyStroke;
        field.setText(KeyInputUtils.getReadableKeyStroke(result));

        if(warningFunction != null) {
            String warn = warningFunction.apply(result);
            if(warn != null) {
                warningLabel.setIconName("warn");
                warningLabel.setText(warn);
            } else {
                warningLabel.setIconName(null);
                warningLabel.setText("");
            }
            dialog.getContentPane().setPreferredSize(new Dimension(WIDTH, HEIGHT + warningLabel.getPreferredSize().height*2));
        }
        dialog.pack();
        dialog.repaint();
    }

    private void submit() {
        visible = false;
        dialog.setVisible(false);
        tlm.dispose();
        dialog.dispose();
    }

    private void cancel() {
        result = null;
        visible = false;
        dialog.setVisible(false);
        tlm.dispose();
        dialog.dispose();
    }

    public static boolean isVisible() {
        return visible;
    }
}
