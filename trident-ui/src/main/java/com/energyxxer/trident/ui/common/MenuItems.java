package com.energyxxer.trident.ui.common;

import com.energyxxer.trident.files.FileType;
import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.modules.FileModuleToken;
import com.energyxxer.trident.ui.styledcomponents.StyledMenu;
import com.energyxxer.trident.ui.styledcomponents.StyledMenuItem;

import java.io.File;

/**
 * Provides managers that create menu components for file and project management.
 */
public class MenuItems {
	public static StyledMenu newMenu(String title) {
		StyledMenu newMenu = new StyledMenu(title);

		// --------------------------------------------------

		{
			StyledMenuItem item = new StyledMenuItem(FileType.PROJECT.name, FileType.PROJECT.icon);
			item.addActionListener(e -> {
				FileType.PROJECT.create(null);
			});
			newMenu.add(item);
		}

		// --------------------------------------------------

		newMenu.addSeparator();

		// --------------------------------------------------

		newMenu.add(createNewFileItem(FileType.FOLDER));

		// --------------------------------------------------

		int prevGroup = -1;
		for(FileType type : FileType.values()) {
			if(type == FileType.FOLDER || type == FileType.PROJECT) continue;

			if(type.group != prevGroup) {
				newMenu.addSeparator();
				prevGroup = type.group;
			}

			newMenu.add(createNewFileItem(type));
		}

		return newMenu;
	}

	private static StyledMenuItem createNewFileItem(FileType type) {
		StyledMenuItem item = new StyledMenuItem(type.name, type.icon);
		item.addActionListener(e -> {
			File activeFile = Commons.getActiveFile();
			if(activeFile != null) {
				if(activeFile.isFile()) activeFile = activeFile.getParentFile();
				type.create(activeFile.getPath());
			}
		});
		return item;
	}

	public enum FileMenuItem {
		COPY, PASTE, DELETE, RENAME, MOVE
	}

	public static StyledMenuItem fileItem(FileMenuItem type) {
		StyledMenuItem item = null;
		switch (type) {
		case COPY:
			item = new StyledMenuItem("Copy");
			break;
		case DELETE:
			item = new StyledMenuItem(FileModuleToken.DELETE_MOVES_TO_TRASH.get() ? "Move to Trash" : "Delete");
			/*item.setEnabled(false);
			item.setEnabled(ExplorerMaster.selectedLabels.size() > 0);
			item.addActionListener(e -> {
				ArrayList<File> files = new ArrayList<>();
				String fileType = null;
				for(int i = 0; i < ExplorerMaster.selectedLabels.size(); i++) {
					File file = new File(ExplorerMaster.selectedLabels.get(i).parent.path);
					if(file.isFile() && fileType == null) {
						fileType = "file";
					} else if(file.isDirectory() && fileType == null) {
						fileType = "folder";
					} else if(file.isDirectory() && "file".equals(fileType)) {
						fileType = "item";
					} else if(file.isFile() && "folder".equals(fileType)) {
						fileType = "item";
					}
					files.add(file);
				}

				String subject = ((ExplorerMaster.selectedLabels.size() == 1) ? "this" : "these") + " " + ((ExplorerMaster.selectedLabels.size() == 1) ? "" : "" + ExplorerMaster.selectedLabels.size() + " ") + fileType + ((ExplorerMaster.selectedLabels.size() == 1) ? "" : "s");

				int confirmation = JOptionPane.showConfirmDialog(null,
						"        Are you sure you want to delete " + subject + "?        ",
						"Delete " + fileType, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (confirmation == JOptionPane.YES_OPTION) {
					for(File file : files) FileCommons.deleteFolder(file);
					TridentWindow.projectExplorer.refresh();
				}
			});*/
			break;
		case MOVE:
			item = new StyledMenuItem("Move");
			item.setEnabled(TridentWindow.projectExplorer.getSelectedTokens().size() > 0);
			break;
		case PASTE:
			item = new StyledMenuItem("Paste");
			break;
		case RENAME:
			item = new StyledMenuItem("Rename", "rename");
			item.setEnabled(TridentWindow.projectExplorer.getSelectedTokens().size() == 1);
			break;
		default:
			break;
		}
		return item;
	}

	public static StyledMenu refactorMenu(String title) {
		StyledMenu newMenu = new StyledMenu(title);

		newMenu.add(fileItem(FileMenuItem.RENAME));
		newMenu.add(fileItem(FileMenuItem.MOVE));

		return newMenu;

	}
}
