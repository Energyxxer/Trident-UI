package com.energyxxer.trident.ui.dialogs.project_properties;

import com.energyxxer.trident.global.temp.projects.TridentProject;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.main.window.actions.ActionManager;
import com.energyxxer.trident.ui.styledcomponents.StyledButton;
import com.energyxxer.trident.ui.styledcomponents.StyledList;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.ImageManager;
import com.energyxxer.xswing.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class ProjectProperties {

	public static final JDialog dialog = new JDialog(TridentWindow.jframe);

	static TridentProject project;

	private static ArrayList<Consumer<TridentProject>> openEvents = new ArrayList<>();
	private static ArrayList<Consumer<TridentProject>> applyEvents = new ArrayList<>();
	private static ArrayList<Runnable> closeEvents = new ArrayList<>();

	private static JPanel currentSection;

	private static ThemeListenerManager tlm = new ThemeListenerManager();

	static {
		
		JPanel pane = new JPanel(new OverlayBorderLayout());
		//JButton okay = new JButton("OK");
		//JButton cancel = new JButton("Cancel");
		
		pane.setPreferredSize(new ScalableDimension(900,600));

		JPanel contentPane = new JPanel(new BorderLayout());
		HashMap<String, JPanel> sectionPanes = new HashMap<>();

		{
			JPanel sidebar = new OverlayBorderPanel(new BorderLayout(), new Insets(0, 0, 0, ComponentResizer.DIST));

			ComponentResizer resizer = new ComponentResizer(sidebar);
			resizer.setResizable(false, false, false, true);

			String[] sections = new String[] { "General", "Output", "Definitions", "Type Aliases", "Dependencies", "Game Logger", "Plugins" };

			StyledList<String> navigator = new StyledList<>(sections, "ProjectProperties");
			sidebar.setBackground(navigator.getBackground());
			sidebar.setOpaque(false);

			navigator.addListSelectionListener(o -> {
				contentPane.remove(currentSection);
				currentSection = sectionPanes.get(sections[o.getFirstIndex()]);
				contentPane.add(currentSection, BorderLayout.CENTER);
				contentPane.repaint();
			});

			sidebar.add(navigator, BorderLayout.CENTER);

			pane.add(sidebar, BorderLayout.WEST);

			tlm.addThemeChangeListener(t -> {
				sidebar.setMinimumSize(new ScalableDimension(25, 1));
				sidebar.setMaximumSize(new ScalableDimension(400, 1));
				navigator.setPreferredSize(new ScalableDimension(200,500));
				pane.setBackground(t.getColor(new Color(235, 235, 235), "ProjectProperties.background"));
				contentPane.setBackground(t.getColor(new Color(235, 235, 235), "ProjectProperties.content.background"));
				sidebar.setBorder(new CompoundBorder(new EmptyBorder(0, 0, 0, ComponentResizer.DIST), BorderFactory.createMatteBorder(0, 0, 0, Math.max(t.getInteger(1,"ProjectProperties.content.border.thickness"),0), t.getColor(new Color(200, 200, 200), "ProjectProperties.content.border.color"))));
			});
		}

		pane.add(contentPane, BorderLayout.CENTER);

		JPanel contentGeneral = new ProjectPropertiesGeneral();

		sectionPanes.put("General", contentGeneral);
		sectionPanes.put("Output", new ProjectPropertiesOutput());
		sectionPanes.put("Definitions", new ProjectPropertiesDefinitions());
		sectionPanes.put("Type Aliases", new ProjectPropertiesAliases());
		sectionPanes.put("Dependencies", new ProjectPropertiesDependencies());
		sectionPanes.put("Game Logger", new ProjectPropertiesGameLogger());
		sectionPanes.put("Plugins", new ProjectPropertiesPlugins());

		contentPane.add(contentGeneral, BorderLayout.CENTER);
		currentSection = contentGeneral;

		{
			JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			buttons.setPreferredSize(new ScalableDimension(0,60));
			tlm.addThemeChangeListener(t -> buttons.setBackground(contentPane.getBackground()));

			{
				StyledButton okay = new StyledButton("OK", "ProjectProperties.okButton", tlm);
				tlm.addThemeChangeListener(t -> okay.setPreferredSize(new ScalableDimension(Math.max(t.getInteger(75,"ProjectProperties.okButton.width"),10), Math.max(t.getInteger(25,"ProjectProperties.okButton.height"),10))));
				buttons.add(okay);

				okay.addActionListener(e -> {
					dialog.setVisible(false);
					applyEvents.forEach(ae -> ae.accept(project));
					project.updateConfig();
					closeEvents.forEach(Runnable::run);
					ActionManager.getAction("RELOAD_WORKSPACE").perform();
				});
			}

			{
				StyledButton cancel = new StyledButton("Cancel", "ProjectProperties.cancelButton", tlm);
				tlm.addThemeChangeListener(t -> cancel.setPreferredSize(new ScalableDimension(Math.max(t.getInteger(75,"ProjectProperties.cancelButton.width"),10), Math.max(t.getInteger(25,"ProjectProperties.cancelButton.height"),10))));
				buttons.add(cancel);


				pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
				pane.getActionMap().put("cancel", new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cancel();
					}
				});

				cancel.addActionListener(e -> cancel());
			}

			{
				StyledButton apply = new StyledButton("Apply", "ProjectProperties.applyButton", tlm);
				tlm.addThemeChangeListener(t -> apply.setPreferredSize(new ScalableDimension(Math.max(t.getInteger(75,"ProjectProperties.applyButton.width"),10), Math.max(t.getInteger(25,"ProjectProperties.applyButton.height"),10))));
				buttons.add(apply);

				apply.addActionListener(e -> {
					applyEvents.forEach(ae -> ae.accept(project));
					project.updateConfig();
					ActionManager.getAction("RELOAD_WORKSPACE").perform();
				});
			}

			buttons.add(new Padding(25));

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

	private static void cancel() {
		dialog.setVisible(false);
		closeEvents.forEach(Runnable::run);
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

    public static void addCloseEvent(Runnable r) {
		closeEvents.add(r);
    }
}
