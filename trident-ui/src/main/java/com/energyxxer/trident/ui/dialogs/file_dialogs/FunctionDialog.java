package com.energyxxer.trident.ui.dialogs.file_dialogs;

import com.energyxxer.trident.compiler.TridentUtil;
import com.energyxxer.trident.files.FileType;
import com.energyxxer.trident.global.temp.projects.ProjectManager;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.modules.FileModuleToken;
import com.energyxxer.trident.ui.styledcomponents.StyledButton;
import com.energyxxer.trident.ui.styledcomponents.StyledIcon;
import com.energyxxer.trident.ui.styledcomponents.StyledLabel;
import com.energyxxer.trident.ui.styledcomponents.StyledTextField;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.xswing.Padding;
import com.energyxxer.xswing.ScalableDimension;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FunctionDialog {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 140;
    private static final int HEIGHT_ERR = 175;

    private static JDialog dialog = new JDialog(TridentWindow.jframe);
    private static JPanel pane;
    private static StyledIcon icon;
    private static StyledLabel nameLabel;

    private static StyledTextField nameField;

    private static JPanel errorPanel;
    private static StyledLabel errorLabel;
    private static StyledLabel warningLabel;
    private static StyledLabel locationLabel;

    private static StyledButton okButton;

    private static boolean valid = false;
    private static TridentUtil.ResourceLocation resourceLocation = null;

    private static String destination;

    private static ThemeListenerManager tlm = new ThemeListenerManager();

    static {
        pane = new JPanel(new BorderLayout());
        pane.setPreferredSize(new ScalableDimension(WIDTH,HEIGHT));
        tlm.addThemeChangeListener(t ->
                pane.setBackground(t.getColor(new Color(235, 235, 235), "NewFileDialog.background"))
        );

        //<editor-fold desc="Icon">
        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new ScalableDimension(73, 48));
        iconPanel.add(new Padding(25), BorderLayout.WEST);
        iconPanel.setBorder(new EmptyBorder(0, 0, 0, 2));
        iconPanel.add(icon = new StyledIcon("file", 48, 48, Image.SCALE_SMOOTH, tlm));
        pane.add(iconPanel, BorderLayout.WEST);
        //</editor-fold>

        //<editor-fold desc="Inner Margin">
        pane.add(new Padding(15), BorderLayout.NORTH);
        pane.add(new Padding(25), BorderLayout.EAST);
        //</editor-fold>

        //<editor-fold desc="Content Components">
        JPanel content = new JPanel();
        content.setOpaque(false);

        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        {

            JPanel entry = new JPanel(new BorderLayout());
            entry.setOpaque(false);
            entry.setMaximumSize(new ScalableDimension(Integer.MAX_VALUE, 25));

            nameLabel = new StyledLabel("Enter new function name:", "NewFileDialog", tlm);
            nameLabel.setStyle(Font.PLAIN);
            nameLabel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            nameLabel.setHorizontalTextPosition(JLabel.LEFT);
            entry.add(nameLabel, BorderLayout.CENTER);

            content.add(entry);
        }

        {

            JPanel entry = new JPanel(new BorderLayout());
            entry.setOpaque(false);
            entry.setMaximumSize(new ScalableDimension(Integer.MAX_VALUE, 25));

            locationLabel = new StyledLabel("", "NewFileDialog.info", tlm);
            locationLabel.setStyle(Font.BOLD);
            locationLabel.setMaximumSize(new ScalableDimension(Integer.MAX_VALUE, locationLabel.getPreferredSize().height));
            entry.add(locationLabel);

            content.add(entry);
        }

        {
            JPanel entry = new JPanel(new BorderLayout());
            entry.setOpaque(false);
            entry.setMaximumSize(new ScalableDimension(Integer.MAX_VALUE, 25));

            nameField = new StyledTextField("", "NewFileDialog", tlm);
            nameField.getDocument().addUndoableEditListener(e -> validateInput());

            entry.add(nameField, BorderLayout.CENTER);

            content.add(entry);
        }

        {
            errorPanel = new JPanel();
            errorPanel.setOpaque(false);
            errorPanel.setMaximumSize(new ScalableDimension(Integer.MAX_VALUE, 0));

            errorLabel = new StyledLabel("", "NewFileDialog.error", tlm);
            errorLabel.setStyle(Font.BOLD);
            errorLabel.setMaximumSize(new ScalableDimension(Integer.MAX_VALUE, errorLabel.getPreferredSize().height));
            errorPanel.add(errorLabel);

            warningLabel = new StyledLabel("", "NewFileDialog.warning", tlm);
            warningLabel.setStyle(Font.BOLD);
            warningLabel.setMaximumSize(new ScalableDimension(Integer.MAX_VALUE, warningLabel.getPreferredSize().height));
            errorPanel.add(warningLabel);

            content.add(errorPanel);
        }

        content.add(new Padding(5));

        {
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttons.setOpaque(false);
            buttons.setMaximumSize(new ScalableDimension(Integer.MAX_VALUE, 30));

            okButton = new StyledButton("OK", tlm);
            okButton.addActionListener(e -> submit());
            buttons.add(okButton);
            StyledButton cancelButton = new StyledButton("Cancel", tlm);
            cancelButton.addActionListener(e -> {
                cancel();
            });

            buttons.add(cancelButton);
            content.add(buttons);
        }

        pane.add(content, BorderLayout.CENTER);
        //</editor-fold>

        //<editor-fold desc="Enter key event">
        pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit");
        pane.getActionMap().put("submit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submit();
            }
        });
        pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        pane.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
        //</editor-fold>

        dialog.setContentPane(pane);
        displayLocation(null);
        displayError(null);
        displayWarning(null);
        dialog.pack();
        dialog.setResizable(true);

        dialog.setTitle("Create New File");

        dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
    }

    private static void submit() {
        if(!valid) return;

        File newFile = convertToPath(nameField.getText().trim()).toFile();
        try {
            boolean successful = (newFile.getParentFile().exists() || newFile.getParentFile().mkdirs()) && newFile.createNewFile();

            if (!successful) {
                Debug.log("File creation unsuccessful: " + newFile, Debug.MessageType.WARN);
                return;
            }

            if(newFile.exists()) TridentWindow.tabManager.openTab(new FileModuleToken(newFile), 0);
            TridentWindow.projectExplorer.refresh();
        } catch (IOException x) {
            x.printStackTrace();
        }
        dialog.setVisible(false);
    }

    public static void create(FileType type, String destination) {
        FunctionDialog.destination = destination;
        nameField.setText("");
        icon.setIconName(type.icon);
        dialog.setTitle("Create New " + type.name);
        nameLabel.setText("Enter new " + type.name + " name:");

        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        center.x -= dialog.getWidth()/2;
        center.y -= dialog.getHeight()/2;

        dialog.setLocation(center);

        dialog.setVisible(true);
        displayLocation(null);
        displayError(null);
        displayWarning(null);
    }

    private static void cancel() {
        dialog.setVisible(false);
    }

    private static Path convertToPath(String in) {
        if(!in.endsWith(FileType.TDN.extension)) in += FileType.TDN.extension;
        boolean isResourceLocation = in.contains(":");

        Path out;
        if(isResourceLocation) {
            resourceLocation = TridentUtil.ResourceLocation.createStrict(in);
            if(resourceLocation == null) throw new InvalidFunctionException("Illegal characters in function name");
            while(resourceLocation.body.contains("//")) {
                resourceLocation.body = resourceLocation.body.replace("//", "/");
            }
            out = ProjectManager.getAssociatedProject(new File(destination)).getServerDataRoot().toPath().resolve("data").resolve(resourceLocation.namespace).resolve(Paths.get("functions", resourceLocation.body.split("/")));
            resourceLocation.body = resourceLocation.body.substring(0, resourceLocation.body.length()-FileType.TDN.extension.length());
        } else {
            out = Paths.get(destination).resolve(in.replace("/", File.separator));
            Path relToDatapack = ProjectManager.getAssociatedProject(new File(destination)).getServerDataRoot().toPath().relativize(out);
            if(relToDatapack.getNameCount() <= 3 || !relToDatapack.getName(0).toString().equals("data") || !relToDatapack.getName(2).toString().equals("functions")) throw new InvalidFunctionException("Not inside a namespace/functions directory");
            resourceLocation = TridentUtil.ResourceLocation.createStrict(relToDatapack.getName(1) + ":" + relToDatapack.subpath(3, relToDatapack.getNameCount()).toString().replace(File.separatorChar, '/'));
            if(resourceLocation == null) throw new InvalidFunctionException("Illegal characters in function name");
            resourceLocation.body = resourceLocation.body.substring(0, resourceLocation.body.length()-FileType.TDN.extension.length());
        }
        if(resourceLocation.body.isEmpty() || resourceLocation.body.endsWith("/")) throw new InvalidPathException(in, "Empty name");
        return out;
    }

    private static void validateInput() {
        String str = nameField.getText().trim();

        if(str.length() <= 0) {
            valid = false;
            okButton.setEnabled(false);
            displayLocation(null);
            displayError(null);
            displayWarning(null);
            return;
        }

        Path path = null;
        try {
            path = convertToPath(str);
            valid = true;
        } catch(IllegalArgumentException x) {
            valid = false;
            displayError("Error: Not a valid file path");
        } catch(InvalidFunctionException x) {
            valid = false;
            displayError("Error: " + x.getMessage());
        }

        //Check if file exists
        if(valid && path != null) {
            valid = !path.toFile().exists();
            if(!valid) displayError("Error: File already exists at the destination");
        }

        if(valid) {
            displayError(null);
            displayLocation(resourceLocation);
        } else {
            displayLocation(null);
        }
        okButton.setEnabled(valid);
    }

    private static void displayError(String message) {
        if(message == null) {
            pane.setPreferredSize(new ScalableDimension(WIDTH, HEIGHT));
            errorPanel.setMaximumSize(new ScalableDimension(Integer.MAX_VALUE, 0));
            errorLabel.setText("");
            dialog.pack();
        } else {
            pane.setPreferredSize(new ScalableDimension(WIDTH, HEIGHT_ERR));
            errorPanel.setMaximumSize(new ScalableDimension(Integer.MAX_VALUE, 30));
            errorLabel.setText(message);
            dialog.pack();
        }
    }

    private static void displayWarning(String message) {
        if(message == null) {
            pane.setPreferredSize(new ScalableDimension(WIDTH, HEIGHT));
            errorPanel.setMaximumSize(new ScalableDimension(Integer.MAX_VALUE, 0));
            warningLabel.setText("");
            dialog.pack();
        } else {
            pane.setPreferredSize(new ScalableDimension(WIDTH, HEIGHT_ERR));
            errorPanel.setMaximumSize(new ScalableDimension(Integer.MAX_VALUE, 30));
            warningLabel.setText(message);
            dialog.pack();
        }
    }

    private static void displayLocation(TridentUtil.ResourceLocation message) {
        if(message == null) {
            locationLabel.setText(" ");
            dialog.pack();
        } else {
            locationLabel.setText("Creating: " + message.toString());
            dialog.pack();
        }
    }

    private static class InvalidFunctionException extends RuntimeException {
        public InvalidFunctionException(String message) {
            super(message);
        }
    }
}
