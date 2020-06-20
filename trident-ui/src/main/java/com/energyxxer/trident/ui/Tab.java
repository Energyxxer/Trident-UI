package com.energyxxer.trident.ui;

import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.display.DisplayModule;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.ui.tablist.TabItem;
import com.energyxxer.util.Disposable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Date;

/**
 * Concept of an open tab in the interface. Contains a component that represents
 * the clickable tab element.
 */
public class Tab {
	//private Project linkedProject;
	public ModuleToken token;
	public DisplayModule module;
	private Object savedValue;
	public boolean visible = true;

	public long openedTimeStamp;
	private boolean saved = true;
	private String name;
	private TabItem tabItem;

	@Override
	public String toString() {
		return "Tab [title=" + getName() + ", token=" + token + ", visible=" + visible + "]";
	}

	public Tab(@NotNull ModuleToken token) {
		this.token = token;
		//this.linkedProject = ProjectManager.getAssociatedProject(new File(path));
		module = token.createModule(this);
		if(module == null) {
			throw new RuntimeException("File cannot be opened in a tab: " + token);
		}
		savedValue = module.getValue();
		openedTimeStamp = new Date().getTime();
		this.name = token.getTitle();
	}

	public void onSelect() {
		openedTimeStamp = new Date().getTime();
		module.focus();
		module.onSelect();
		module.displayCaretInfo();
	}

	public void onEdit() {
		this.setSaved(savedValue == null || savedValue.equals(module.getValue()));
	}

	public void updateName() {
		tabItem.updateName();
	}
	
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isActive() {
		return this.tabItem != null && this.tabItem.isSelected();
	}

	public void save() {
		if(!module.canSave()) return;

		Object val = module.save();
		if(val != null) {
			savedValue = val;
			setSaved(true);
		}
	}


	/*public Project getLinkedProject() {
		return linkedProject;
	}*/
	public JComponent getModuleComponent() {
		return (JComponent) module;
	}

	public boolean isSaved() {
		return saved;
	}

	private void setSaved(boolean saved) {
		if(this.saved != saved) {
			this.saved = saved;
			updateList();
		}
	}

	public void updateSavedValue() {
		if(module != null) {
            savedValue = module.getValue();
            setSaved(true);
        }
	}

	private void updateList() {
		TridentWindow.tabList.repaint();
	}

	public String getName() {
		return name;
	}

	public TabItem getLinkedTabItem() {
		return tabItem;
	}

	public void linkTabItem(TabItem tabItem) {
		this.tabItem = tabItem;
	}

	public void dispose() {
		if(module instanceof Disposable) {
			((Disposable) module).dispose();
		}
	}

	public boolean transform(ModuleToken newToken) {
		if(module != null) {
			if(!module.transform(newToken)) {
				return false;
			}
		}

		token = newToken;

		name = newToken.getTitle();
		tabItem.transform(newToken);

		return true;
	}
}
