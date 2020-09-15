package com.energyxxer.trident.global;

import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.global.temp.projects.ProjectManager;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.Tab;
import com.energyxxer.trident.ui.dialogs.OptionDialog;
import com.energyxxer.trident.ui.editor.TridentEditorModule;
import com.energyxxer.trident.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.trident.ui.modules.FileModuleToken;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.ui.styledcomponents.StyledMenuItem;
import com.energyxxer.trident.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.trident.ui.tablist.TabItem;
import com.energyxxer.trident.ui.tablist.TabListMaster;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Interface that allows communication between parts of the program and the tab
 * list.
 */
public class TabManager {
	@NotNull
	private final TabListMaster tabList;
	@NotNull
	private final ContentSwapper moduleComponent;
	private boolean changeWindowInfo = false;
	private String openTabSaveKey = null;

	public static Preferences.SettingPref<Boolean> SAVE_OPEN_TABS = new Preferences.SettingPref<>("settings.behavior.save_open_tabs", true, Boolean::parseBoolean);
	public static final Preferences.SettingPref<Integer> TAB_LIMIT = new Preferences.SettingPref<>("settings.behavior.tab_limit", 0, Integer::parseInt);

	public List<Tab> openTabs = Collections.synchronizedList(new ArrayList<>());

	private Tab selectedTab = null;
	
	private StyledPopupMenu menu;

	public TabManager(@NotNull TabListMaster tabList, @NotNull ContentSwapper moduleComponent) {
		this.tabList = tabList;
		this.moduleComponent = moduleComponent;
	}

	public void openTab(ModuleToken token, int index) {
		openTab(token);
		selectLocation(selectedTab, index, 0);
	}

	public void openTab(ModuleToken token, int index, int length) {
		openTab(token);
		selectLocation(selectedTab, index, length);
	}

	public void openTab(ModuleToken token) {
		for (int i = 0; i < openTabs.size(); i++) {
			if (openTabs.get(i).token.equals(token)) {
				setSelectedTab(openTabs.get(i));
				return;
			}
		}
		//Have to open a new one
		if(TAB_LIMIT.get() > 0 && openTabs.size() >= TAB_LIMIT.get()) {
			int toClose = (openTabs.size()+1) - TAB_LIMIT.get();
			openTabs.stream().filter(Tab::isSaved).sorted(Comparator.comparing(t -> t.openedTimeStamp)).limit(toClose).forEach(this::closeTab);
		}
		Tab nt = new Tab(token);
		openTabs.add(nt);
		tabList.addTab(new TabItem(this, nt));
		setSelectedTab(nt);
	}

	private void selectLocation(Tab tab, int index, int length) {
		if(tab.module instanceof TridentEditorModule) {
			((TridentEditorModule) tab.module).editorComponent.getCaret().setProfile(new CaretProfile(index + length, index));
		}
	}

	public void closeSelectedTab() {
		closeSelectedTab(false);
	}

	public void closeSelectedTab(boolean force) {
		closeTab(getSelectedTab(), force);
	}

	public void closeTab(Tab tab) {
		closeTab(tab, false);
	}

	public void closeTab(Tab tab, boolean force) {
		if(tab == null) return;
		if(!force) {
			if(!tab.isSaved()) {
				String confirmation = new OptionDialog("Unsaved changes", "'" + tab.getName() + "' has changes; do you want to save them?", new String[] {"Save", "Don't Save", "Cancel"}).result;
				if("Save".equals(confirmation)) {
					tab.save();
				}
				if(confirmation == null || "Cancel".equals(confirmation)) return;
			}
		}
		for (int i = 0; i < openTabs.size(); i++) {
			if (openTabs.get(i) == tab) {
				if (selectedTab == openTabs.get(i)) setSelectedTab(tabList.getFallbackTab(tab));

				tabList.removeTab(tab);
				openTabs.remove(i);
				tab.dispose();

				return;
			}
		}
	}

	public void closeAllTabs(boolean force) {
		while(openTabs.size() > 0) {
			closeTab(openTabs.get(0), force);
		}
	}

	private void updateMenu() {
		menu = new StyledPopupMenu();
		if(openTabs.size() <= 0) {
			StyledMenuItem item = new StyledMenuItem("No tabs open!");
			item.setFont(item.getFont().deriveFont(Font.ITALIC));
			item.setIcon(new ImageIcon(Commons.getIcon("info").getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
			menu.add(item);
			return;
		}
		for(int i = 0; i < openTabs.size(); i++) {
			Tab tab = openTabs.get(i);
			StyledMenuItem item = new StyledMenuItem(((!tab.isSaved()) ? "*" : "") + tab.getName());
			item.setIcon(new ImageIcon(tab.getLinkedTabItem().getIcon()));
			if(!tab.visible) {
				item.setFont(item.getFont().deriveFont(Font.BOLD));
			}
			item.addActionListener(e -> setSelectedTab(tab));
			menu.add(item);
		}
	}

	public StyledPopupMenu getMenu() {
		updateMenu();
		return menu;
	}

	public void setSelectedTab(Tab tab) {
		tabList.selectTab(tab);
		if (selectedTab != null) {
			selectedTab = null;
		}
		if (tab != null) {
			selectedTab = tab;

			String linkedProject = null;
			if(tab.token.getAssociatedProjectRoot() != null) {
				linkedProject = tab.token.getAssociatedProjectRoot().getName();
			}
			if(changeWindowInfo) TridentWindow.setTitle(((linkedProject != null) ? linkedProject + " - " : "") + tab.getName());
			moduleComponent.setContent(tab.getModuleComponent());
			tab.onSelect();
		} else {
			if(changeWindowInfo) {
				TridentWindow.statusBar.setCaretInfo(Commons.DEFAULT_CARET_DISPLAY_TEXT);
				TridentWindow.statusBar.setSelectionInfo(" ");
				TridentWindow.clearTitle();
			}
            moduleComponent.setContent(null);
		}

		Commons.updateActiveProject();
		saveOpenTabs();
	}

	public Tab getSelectedTab() {
		return selectedTab;
	}

	public void saveOpenTabs() {
		if(openTabSaveKey == null) return;
		if(!SAVE_OPEN_TABS.get()) return;
		StringBuilder sb = new StringBuilder();
		for(Tab tab : openTabs) {
			if(selectedTab != tab) {
				sb.append(tab.token.getIdentifier());
				sb.append(File.pathSeparator);
			}
		}
		if(selectedTab != null) {
			sb.append(selectedTab.token.getIdentifier());
			sb.append(File.pathSeparator);
		}
		Preferences.put(openTabSaveKey, sb.toString());
	}

	public void openSavedTabs() {
		if(openTabSaveKey == null) return;
		if(!SAVE_OPEN_TABS.get()) return;
		String savedTabs = Preferences.get(openTabSaveKey,null);
		if(savedTabs != null) {
			String[] identifiers = savedTabs.split(Pattern.quote(File.pathSeparator));
			for(String identifier : identifiers) {
				ModuleToken created = ModuleToken.Static.createFromIdentifier(identifier);
				if(created != null) openTab(created);
			}
		}
	}

	public void setChangeWindowInfo(boolean changeWindowInfo) {
		this.changeWindowInfo = changeWindowInfo;
	}

	public TabListMaster getTabList() {
		return tabList;
	}

	public String getOpenTabSaveKey() {
		return openTabSaveKey;
	}

	public void setOpenTabSaveKey(String openTabSaveKey) {
		this.openTabSaveKey = openTabSaveKey;
	}

	public Tab getTabForToken(ModuleToken token) {
		for(Tab tab : openTabs) {
			if(tab.token.equals(token)) return tab;
		}
		return null;
	}

	public void checkForDeletion() {
		for (int i = 0; i < openTabs.size(); i++) {
			Tab tab = openTabs.get(i);
			if(tab.token instanceof FileModuleToken && !((FileModuleToken) tab.token).getFile().exists()) {
				closeTab(tab, true);
				i--;
			}
		}
	}

	public void closeAllTabsForProject(Project activeProject) {
		for (int i = 0; i < openTabs.size(); i++) {
			Tab tab = openTabs.get(i);
			if(tab.token instanceof FileModuleToken && ProjectManager.getAssociatedProject(((FileModuleToken) tab.token).getFile()) == activeProject) {
				closeTab(tab, false);
				i--;
			}
		}
	}

	public boolean confirmSaved() {
		for(Tab tab : openTabs) {
			if(!tab.isSaved()) {
			    setSelectedTab(tab);
				String confirmation = new OptionDialog("Unsaved changes", "'" + tab.getName() + "' has changes; do you want to save them?", new String[] {"Save", "Don't Save", "Cancel"}).result;
				if("Save".equals(confirmation)) {
					tab.save();
				} else if(!"Don't Save".equals(confirmation)) {
					return false;
				}
			}
		}
		return true;
	}
}
