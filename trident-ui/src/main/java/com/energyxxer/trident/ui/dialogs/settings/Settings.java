package com.energyxxer.trident.ui.dialogs.settings;

import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.styledcomponents.StyledButton;
import com.energyxxer.trident.ui.styledcomponents.StyledList;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.ImageManager;
import com.energyxxer.xswing.ComponentResizer;
import com.energyxxer.xswing.OverlayBorderLayout;
import com.energyxxer.xswing.OverlayBorderPanel;
import com.energyxxer.xswing.Padding;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Settings {

	private static JDialog dialog = new JDialog(TridentWindow.jframe);
	//static Theme t;

	private static ArrayList<Runnable> openEvents = new ArrayList<>();
	private static ArrayList<Runnable> applyEvents = new ArrayList<>();

	private static JPanel currentSection;

	private static ThemeListenerManager tlm = new ThemeListenerManager();

	static {
		JPanel pane = new JPanel(new OverlayBorderLayout());
		pane.setPreferredSize(new Dimension(900,600));
		tlm.addThemeChangeListener(t ->
				pane.setBackground(t.getColor(new Color(235, 235, 235), "Settings.background"))
		);

		JPanel contentPane = new JPanel(new BorderLayout());
		HashMap<String, JPanel> sectionPanes = new HashMap<>();

		{
			JPanel sidebar = new OverlayBorderPanel(new BorderLayout(), new Insets(0, 0, 0, ComponentResizer.DIST));

			ComponentResizer sidebarResizer = new ComponentResizer(sidebar);
			sidebar.setMinimumSize(new Dimension(25, 1));
			sidebar.setMaximumSize(new Dimension(400, 1));
			sidebarResizer.setResizable(false, false, false, true);

			String[] sections = new String[] { "General", "Appearance", "Editor", "Resources", "Keymap" };

			StyledList<String> navigator = new StyledList<>(sections, "Settings");
			sidebar.setBackground(navigator.getBackground());
			tlm.addThemeChangeListener(t ->
					sidebar.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, ComponentResizer.DIST), BorderFactory.createMatteBorder(0, 0, 0, Math.max(t.getInteger(1,"Settings.content.border.thickness"),0), t.getColor(new Color(200, 200, 200), "Settings.content.border.color"))))
			);
			sidebar.setOpaque(false);
			navigator.setPreferredSize(new Dimension(200,500));

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

		SettingsGeneral contentGeneral = new SettingsGeneral();
		sectionPanes.put("General", contentGeneral);
		sectionPanes.put("Appearance", new SettingsAppearance());
		sectionPanes.put("Editor", new JPanel());
		sectionPanes.put("Resources", new JPanel());
		sectionPanes.put("Keymap", new SettingsKeymap());

		contentPane.add(contentGeneral, BorderLayout.CENTER);
		currentSection = contentGeneral;

		{
			JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
			buttons.setPreferredSize(new Dimension(0,60));
			tlm.addThemeChangeListener(t -> buttons.setBackground(contentPane.getBackground()));

			buttons.add(new Padding(25));

			{
				StyledButton okay = new StyledButton("OK", "Settings");
				tlm.addThemeChangeListener(t -> okay.setPreferredSize(new Dimension(Math.max(t.getInteger(75,"Settings.okButton.width"),10), Math.max(t.getInteger(25,"Settings.okButton.height"),10))));
				buttons.add(okay);

				okay.addActionListener(e -> {
					dialog.setVisible(false);
					dialog.dispose();
					applyEvents.forEach(Runnable::run);
				});
			}

			{
				StyledButton cancel = new StyledButton("Cancel", "Settings");
				tlm.addThemeChangeListener(t -> cancel.setPreferredSize(new Dimension(Math.max(t.getInteger(75,"Settings.cancelButton.width"),10), Math.max(t.getInteger(25,"Settings.cancelButton.height"),10))));
				buttons.add(cancel);

				cancel.addActionListener(e -> {
					dialog.setVisible(false);
					dialog.dispose();
				});
			}

			contentPane.add(buttons, BorderLayout.SOUTH);
		}
		dialog.setContentPane(pane);
		dialog.pack();

		dialog.setTitle("Settings");
		dialog.setIconImage(ImageManager.load("/assets/icons/ui/settings.png").getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));

		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		center.x -= dialog.getWidth()/2;
		center.y -= dialog.getHeight()/2;

		dialog.setLocation(center);

		dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
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
}
