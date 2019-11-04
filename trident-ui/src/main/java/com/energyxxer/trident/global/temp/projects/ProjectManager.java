package com.energyxxer.trident.global.temp.projects;

import com.energyxxer.crossbow.compiler.CrossbowCompiler;
import com.energyxxer.trident.compiler.TridentCompiler;
import com.energyxxer.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class ProjectManager {
	private static ArrayList<Project> loadedProjects = new ArrayList<>();
	private static String workspaceDir = null;

	public static void loadWorkspace() {
		if(workspaceDir == null) throw new IllegalStateException("Workspace directory not specified.");
		loadedProjects.clear();
		
		File workspace = new File(workspaceDir);

		File[] fileList = workspace.listFiles();
		if (fileList == null) {
			return;
		}

		for(File file : fileList) {
			if(file.isDirectory()) {
				if(file.toPath().resolve(TridentCompiler.PROJECT_FILE_NAME).toFile().exists()) {
					try {
						loadedProjects.add(new TridentProject(new File(file.getAbsolutePath())));
					} catch(RuntimeException x) {
						x.printStackTrace();
					}
				} else if(file.toPath().resolve(CrossbowCompiler.PROJECT_FILE_NAME).toFile().exists()) {
					try {
						loadedProjects.add(new CrossbowProject(new File(file.getAbsolutePath())));
					} catch(RuntimeException x) {
						x.printStackTrace();
					}
				}
			}
		}
	}
	
	public static Project getAssociatedProject(File file) {
		if(file == null) return null;
		for(Project project : loadedProjects) {
			if((file.getPath() + File.separator).startsWith((project.getRootDirectory().getPath() + File.separator))) {
				return project;
			}
		}
		return null;
	}
	
	public static String getIconFor(File file) {
		Project project = getAssociatedProject(file);
		String filename = file.getName();
		if(file.isFile()) {
			if(filename.endsWith(".json")) {
				if(filename.equals("sounds.json")) {
					return "sound_config";
				} else if(file.getParentFile().getName().equals("blockstates")) {
					return "blockstate";
				} else return "model";
			} else if(filename.endsWith(".lang")) {
				return "lang";
			} else if(filename.endsWith(".mcmeta") || filename.endsWith(TridentCompiler.PROJECT_FILE_NAME)) {
				return "meta";
			} else if(filename.endsWith(".ogg")) {
				return "audio";
			} else if(filename.endsWith(".nbt")) {
				return "structure";
			} else if(filename.endsWith(".mcfunction")) {
				return "function";
			} else if(filename.endsWith(".tdn")) {
				return "trident_file";
			}
			//TODO: Make this extension-to-icon mapping data-driven by the selected UI theme.

            /*
sounds.json = sound_config
blockstates/*.json = blockstate
*.json = model
*.lang = lang
*.mcmeta = meta
*.ogg = audio
*.nbt = structure
            */
		} else {
			//Check for file roots
			if(project != null) {
				if(file.getParentFile().equals(project.getRootDirectory())) {
					switch(filename) {
						case "src":
						case "resources":
						case "data":
							return filename;
					}
				}
			}
		}
		return null;
	}
	
	public static void create(String name) {
		Project p = new TridentProject(name);
		p.createNew();
		loadedProjects.add(p);
	}
	
	public static boolean renameFile(File file, String newName) {
		String path = file.getAbsolutePath();
		String name = file.getName();
		String rawName = FileUtil.stripExtension(name);
		String extension = name.replaceAll(rawName, "");
		String pathToParent = path.substring(0, path.lastIndexOf(name));
		
		File newFile = new File(pathToParent + newName + extension);
		
		boolean renamed = file.renameTo(newFile);

		return renamed;
	}

	public static String getWorkspaceDir() {
		return workspaceDir;
	}

	public static void setWorkspaceDir(String workspaceDir) {
		ProjectManager.workspaceDir = workspaceDir;
	}

	public static Collection<Project> getLoadedProjects() {
		return loadedProjects;
	}
}
