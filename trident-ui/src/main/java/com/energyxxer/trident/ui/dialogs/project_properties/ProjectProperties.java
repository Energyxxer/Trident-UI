package com.energyxxer.trident.ui.dialogs.project_properties;

import com.energyxxer.trident.global.temp.projects.TridentProject;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.styledcomponents.StyledButton;
import com.energyxxer.trident.ui.styledcomponents.StyledList;
import com.energyxxer.trident.ui.theme.Theme;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.ImageManager;
import com.energyxxer.xswing.ComponentResizer;
import com.energyxxer.xswing.OverlayBorderLayout;
import com.energyxxer.xswing.OverlayBorderPanel;
import com.energyxxer.xswing.Padding;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class ProjectProperties {

	public static final JDialog dialog = new JDialog(TridentWindow.jframe);

	private static Theme t;
	static TridentProject project;

	private static ArrayList<Consumer<TridentProject>> openEvents = new ArrayList<>();
	private static ArrayList<Consumer<TridentProject>> applyEvents = new ArrayList<>();

	private static JPanel currentSection;

	private static ThemeListenerManager tlm = new ThemeListenerManager();

	static {

		tlm.addThemeChangeListener(th -> t = th);
		
		JPanel pane = new JPanel(new OverlayBorderLayout());
		//JButton okay = new JButton("OK");
		//JButton cancel = new JButton("Cancel");
		
		pane.setPreferredSize(new Dimension(900,600));
		pane.setBackground(t.getColor(new Color(235, 235, 235), "ProjectProperties.background"));

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBackground(t.getColor(new Color(235, 235, 235), "ProjectProperties.content.background"));
		HashMap<String, JPanel> sectionPanes = new HashMap<>();
		pane.add(contentPane, BorderLayout.CENTER);

		{
			JPanel sidebar = new OverlayBorderPanel(new BorderLayout(), new Insets(0, 0, 0, ComponentResizer.DIST));

			ComponentResizer resizer = new ComponentResizer(sidebar);
			sidebar.setMinimumSize(new Dimension(25, 1));
			sidebar.setMaximumSize(new Dimension(400, 1));
			resizer.setResizable(false, false, false, true);

			String[] sections = new String[] { "General", "Output", "Definitions", "Type Aliases", "Dependencies", "Game Logger" };

			StyledList<String> navigator = new StyledList<>(sections, "ProjectProperties");
			sidebar.setBackground(navigator.getBackground());
			sidebar.setOpaque(false);
			sidebar.setBorder(new CompoundBorder(new EmptyBorder(0, 0, 0, ComponentResizer.DIST), BorderFactory.createMatteBorder(0, 0, 0, Math.max(t.getInteger(1,"ProjectProperties.content.border.thickness"),0), t.getColor(new Color(200, 200, 200), "ProjectProperties.content.border.color"))));
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

		JPanel contentGeneral = new ProjectPropertiesGeneral();

		sectionPanes.put("General", contentGeneral);
		sectionPanes.put("Output", new ProjectPropertiesOutput());
		sectionPanes.put("Definitions", new ProjectPropertiesDefinitions());
		sectionPanes.put("Type Aliases", new ProjectPropertiesAliases());
		sectionPanes.put("Dependencies", new ProjectPropertiesDependencies());
		sectionPanes.put("Game Logger", new ProjectPropertiesGameLogger());

		contentPane.add(contentGeneral, BorderLayout.CENTER);
		currentSection = contentGeneral;

		{
			JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
			buttons.setPreferredSize(new Dimension(0,60));
			tlm.addThemeChangeListener(t -> buttons.setBackground(contentPane.getBackground()));

			buttons.add(new Padding(25));

			{
				StyledButton okay = new StyledButton("OK", "ProjectProperties");
				tlm.addThemeChangeListener(t -> okay.setPreferredSize(new Dimension(Math.max(t.getInteger(75,"ProjectProperties.okButton.width"),10), Math.max(t.getInteger(25,"ProjectProperties.okButton.height"),10))));
				buttons.add(okay);

				okay.addActionListener(e -> {
					dialog.setVisible(false);
					dialog.dispose();
					applyEvents.forEach(ae -> ae.accept(project));
					project.updateConfig();
				});
			}

			{
				StyledButton cancel = new StyledButton("Cancel", "ProjectProperties");
				tlm.addThemeChangeListener(t -> cancel.setPreferredSize(new Dimension(Math.max(t.getInteger(75,"ProjectProperties.cancelButton.width"),10), Math.max(t.getInteger(25,"ProjectProperties.cancelButton.height"),10))));
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
		//dialog.setResizable(false);

		addOpenEvent(p -> dialog.setTitle("Editing properties for project \"" + p.getName() + "\""));
		dialog.setIconImage(ImageManager.load("/assets/icons/ui/settings.png").getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));

		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		center.x -= dialog.getWidth()/2;
		center.y -= dialog.getHeight()/2;

		dialog.setLocation(center);

		dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
	}

	public static void show(TridentProject p) {
		project = p;
		openEvents.forEach(e -> e.accept(p));

		dialog.setVisible(true);
	}

	static void addOpenEvent(Consumer<TridentProject> r) {
		openEvents.add(r);
	}

	static void addApplyEvent(Consumer<TridentProject> r) {
		applyEvents.add(r);
	}
}
