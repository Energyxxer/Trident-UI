package com.energyxxer.trident.ui.dialogs.settings;

import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.styledcomponents.StyledButton;
import com.energyxxer.trident.ui.styledcomponents.StyledList;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.ImageManager;
import com.energyxxer.xswing.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

public class Settings {

	private static JDialog dialog = new JDialog(TridentWindow.jframe);
	//static Theme t;

	private static ArrayList<Runnable> openEvents = new ArrayList<>();
	private static ArrayList<Runnable> applyEvents = new ArrayList<>();
	private static ArrayList<Runnable> cancelEvents = new ArrayList<>();
	private static ArrayList<Runnable> closeEvents = new ArrayList<>();

	private static JPanel currentSection;

	private static ThemeListenerManager tlm = new ThemeListenerManager();

	static {
		JPanel pane = new JPanel(new OverlayBorderLayout());
		pane.setPreferredSize(new ScalableDimension(900,600));
		tlm.addThemeChangeListener(t ->
				pane.setBackground(t.getColor(new Color(235, 235, 235), "Settings.background"))
		);

		JPanel contentPane = new JPanel(new BorderLayout());
		HashMap<String, JPanel> sectionPanes = new HashMap<>();

		{
			JPanel sidebar = new OverlayBorderPanel(new BorderLayout(), new Insets(0, 0, 0, ComponentResizer.DIST));

			ComponentResizer sidebarResizer = new ComponentResizer(sidebar);
			sidebar.setMinimumSize(new ScalableDimension(25, 1));
			sidebar.setMaximumSize(new ScalableDimension(400, 1));
			sidebarResizer.setResizable(false, false, false, true);

			String[] sections = new String[] { "Behavior", "Appearance", "Editor", "Snippets", "Keymap" };

			StyledList<String> navigator = new StyledList<>(sections, "Settings");
			sidebar.setBackground(navigator.getBackground());
			tlm.addThemeChangeListener(t ->
					sidebar.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, ComponentResizer.DIST), BorderFactory.createMatteBorder(0, 0, 0, Math.max(t.getInteger(1,"Settings.content.border.thickness"),0), t.getColor(new Color(200, 200, 200), "Settings.content.border.color"))))
			);
			sidebar.setOpaque(false);
			navigator.setPreferredSize(new ScalableDimension(200,500));

			navigator.addListSelectionListener(o -> {
				contentPane.remove(currentSection);
				currentSection = sectionPanes.get(sections[o.getFirstIndex()]);
				contentPane.add(currentSection, BorderLayout.CENTER);
				contentPane.repaint();
			});

			sidebar.add(navigator, BorderLayout.CENTER);

			pane.add(sidebar, BorderLayout.WEST);
		}

		tlm.addThemeChangeListener(t ->
				contentPane.setBackground(t.getColor(new Color(235, 235, 235), "Settings.content.background"))
		);
		pane.add(contentPane, BorderLayout.CENTER);

		SettingsBehavior contentBehavior = new SettingsBehavior();
		sectionPanes.put("Behavior", contentBehavior);
		sectionPanes.put("Appearance", new SettingsAppearance());
		sectionPanes.put("Editor", new SettingsEditor());
		sectionPanes.put("Snippets", new SettingsSnippets());
		sectionPanes.put("Keymap", new SettingsKeymap());

		contentPane.add(contentBehavior, BorderLayout.CENTER);
		currentSection = contentBehavior;

		{
			JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			buttons.setPreferredSize(new ScalableDimension(0,60));
			tlm.addThemeChangeListener(t -> buttons.setBackground(contentPane.getBackground()));

			{
				StyledButton okay = new StyledButton("OK", "Settings.okButton", tlm);
				tlm.addThemeChangeListener(t -> okay.setPreferredSize(new ScalableDimension(Math.max(t.getInteger(75,"Settings.okButton.width"),10), Math.max(t.getInteger(25,"Settings.okButton.height"),10))));
				buttons.add(okay);

				okay.addActionListener(e -> {
					dialog.setVisible(false);
					applyEvents.forEach(Runnable::run);
					closeEvents.forEach(Runnable::run);
				});
			}

			{
				StyledButton cancel = new StyledButton("Cancel", "Settings.cancelButton", tlm);
				tlm.addThemeChangeListener(t -> cancel.setPreferredSize(new ScalableDimension(Math.max(t.getInteger(75,"Settings.cancelButton.width"),10), Math.max(t.getInteger(25,"Settings.cancelButton.height"),10))));
				buttons.add(cancel);

				pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
				pane.getActionMap().put("cancel", new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cancel();
					}
				});

				cancel.addActionListener(e -> {cancel();});
			}

			{
				StyledButton apply = new StyledButton("Apply", "Settings.applyButton", tlm);
				tlm.addThemeChangeListener(t -> apply.setPreferredSize(new ScalableDimension(Math.max(t.getInteger(75,"Settings.applyButton.width"),10), Math.max(t.getInteger(25,"Settings.applyButton.height"),10))));
				buttons.add(apply);

				apply.addActionListener(e -> {
					applyEvents.forEach(Runnable::run);
				});
			}
			buttons.add(new Padding(25));

			contentPane.add(buttons, BorderLayout.SOUTH);
		}
		dialog.setContentPane(pane);
		dialog.pack();

		dialog.setTitle("Settings");
		dialog.setIconImage(ImageManager.load("/assets/icons/ui/settings.png").getScaledInstance(16, 16, Image.SCALE_SMOOTH));

		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		center.x -= dialog.getWidth()/2;
		center.y -= dialog.getHeight()/2;

		dialog.setLocation(center);

		dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
	}

	private static void cancel() {
		dialog.setVisible(false);
		cancelEvents.forEach(Runnable::run);
		closeEvents.forEach(Runnable::run);
	}

	public static void show() {
		openEvents.forEach(Runnable::run);

		dialog.setVisible(true);
	}

	static void addOpenEvent(Runnable r) {
		openEvents.add(r);
		r.run();
	}

	static void addApplyEvent(Runnable r) {
		applyEvents.add(r);
	}

    public static void addCancelEvent(Runnable r) {
		cancelEvents.add(r);
    }

	public static void addCloseEvent(Runnable r) {
		closeEvents.add(r);
	}
}
