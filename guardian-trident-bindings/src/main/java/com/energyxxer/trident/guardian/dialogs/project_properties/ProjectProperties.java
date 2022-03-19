package com.energyxxer.trident.guardian.dialogs.project_properties;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.actions.ActionManager;
import com.energyxxer.guardian.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.guardian.ui.styledcomponents.Padding;
import com.energyxxer.guardian.ui.styledcomponents.StyledButton;
import com.energyxxer.guardian.ui.styledcomponents.StyledList;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.guardian.ui.user_configs.ConfigTab;
import com.energyxxer.guardian.ui.user_configs.ConfigTabDisplayModule;
import com.energyxxer.prismarine.util.JsonTraverser;
import com.energyxxer.trident.guardian.TridentProject;
import com.energyxxer.util.ImageManager;
import com.energyxxer.xswing.ComponentResizer;
import com.energyxxer.xswing.OverlayBorderLayout;
import com.energyxxer.xswing.OverlayBorderPanel;
import com.energyxxer.xswing.ScalableDimension;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ProjectProperties {

	public static final JDialog dialog = new JDialog(GuardianWindow.jframe);

	static TridentProject project;

	private static ArrayList<Consumer<TridentProject>> openEvents = new ArrayList<>();
	private static ArrayList<Consumer<TridentProject>> applyEvents = new ArrayList<>();
	private static ArrayList<Runnable> closeEvents = new ArrayList<>();

	private static StyledList<SettingsTab> navigator;
	private static ArrayList<SettingsTab> tabs = new ArrayList<>();
	private static JPanel contentPane;
	private static JPanel currentSection;

	private static ThemeListenerManager tlm = new ThemeListenerManager();

	static {

		JPanel contentGeneral = new ProjectPropertiesGeneral();

		tabs.add(new SettingsTab("General", contentGeneral));
		tabs.add(new SettingsTab("Definitions", new ProjectPropertiesDefinitions()));
		tabs.add(new SettingsTab("Type Aliases", new ProjectPropertiesAliases()));
		tabs.add(new SettingsTab("Dependencies", new ProjectPropertiesDependencies()));
		tabs.add(new SettingsTab("Game Logger", new ProjectPropertiesGameLogger()));
		tabs.add(new SettingsTab("Plugins", new ProjectPropertiesPlugins()));

		addOpenEvent(p -> {
			JsonTraverser traverser = new JsonTraverser(p.getProjectConfigJson());

			for(SettingsTab tab : tabs) {
				if(tab.component instanceof ConfigTabDisplayModule) {
					((ConfigTabDisplayModule) tab.component).open(traverser);
				}
			}
		});
		addApplyEvent(p -> {
			JsonTraverser traverser = new JsonTraverser(p.getProjectConfigJson()).createOnTraversal();

			for(SettingsTab tab : tabs) {
				if(tab.component instanceof ConfigTabDisplayModule) {
					((ConfigTabDisplayModule) tab.component).apply(traverser);
				}
			}
		});

		JPanel pane = new JPanel(new OverlayBorderLayout());
		//JButton okay = new JButton("OK");
		//JButton cancel = new JButton("Cancel");
		
		pane.setPreferredSize(new ScalableDimension(900,600));

		contentPane = new JPanel(new BorderLayout());

		{
			JPanel sidebar = new OverlayBorderPanel(new BorderLayout(), new Insets(0, 0, 0, ComponentResizer.DIST));

			ComponentResizer resizer = new ComponentResizer(sidebar);
			resizer.setResizable(false, false, false, true);

			navigator = new StyledList<>(tabs.toArray(new SettingsTab[0]), "ProjectProperties");
			sidebar.setBackground(navigator.getBackground());
			sidebar.setOpaque(false);

			navigator.addListSelectionListener(o -> {
				changeTab(((SettingsTab) o.getSource()));
			});

			OverlayScrollPane scrollPane = new OverlayScrollPane(tlm, navigator);
			sidebar.add(scrollPane, BorderLayout.CENTER);

			pane.add(sidebar, BorderLayout.WEST);

			tlm.addThemeChangeListener(t -> {
				sidebar.setMinimumSize(new ScalableDimension(25, 1));
				sidebar.setMaximumSize(new ScalableDimension(400, 1));
				sidebar.setPreferredSize(new ScalableDimension(200,500));
				pane.setBackground(t.getColor(new Color(235, 235, 235), "ProjectProperties.background"));
				contentPane.setBackground(t.getColor(new Color(235, 235, 235), "ProjectProperties.content.background"));
				sidebar.setBorder(new CompoundBorder(new EmptyBorder(0, 0, 0, ComponentResizer.DIST), BorderFactory.createMatteBorder(0, 0, 0, Math.max(t.getInteger(1,"ProjectProperties.content.border.thickness"),0), t.getColor(new Color(200, 200, 200), "ProjectProperties.content.border.color"))));
			});
		}

		pane.add(contentPane, BorderLayout.CENTER);

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

	private static void changeTab(SettingsTab tab) {
		contentPane.remove(currentSection);
		currentSection = tab.component;
		contentPane.add(currentSection, BorderLayout.CENTER);
		contentPane.revalidate();
		contentPane.repaint();
	}


	private static void cancel() {
		dialog.setVisible(false);
		closeEvents.forEach(Runnable::run);
	}

	private static void removeAllUserTabs() {
		while(tabs.size() > 6) {
			tabs.remove(tabs.size()-1);
		}
	}


	public static void show(TridentProject p) {
		project = p;
		removeAllUserTabs();

		for(ConfigTab tab : project.getProjectConfigTabs()) {
			tabs.add(new SettingsTab(tab.getTitle(), ((JPanel) tab.createModule(null))));
		}
		navigator.setOptions(tabs.toArray(new SettingsTab[0]));
		navigator.setSelectedOptionIndex(0);
		changeTab(tabs.get(0));

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

	private static class SettingsTab {
		public final String name;
		public final JPanel component;

		public SettingsTab(String name, JPanel component) {
			this.name = name;
			this.component = component;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
