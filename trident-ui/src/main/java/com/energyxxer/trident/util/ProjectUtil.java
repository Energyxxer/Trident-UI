package com.energyxxer.trident.util;

import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.global.temp.projects.ProjectManager;

import java.io.File;

public class ProjectUtil {

    public static String getPackage(File file) {
        return getPackageInclusive(file.getParentFile());
    }

    public static String getPackageInclusive(File file) {
        Project associatedProject = ProjectManager.getAssociatedProject(file);
        return (
                (associatedProject != null) ?
                        FileCommons.stripExtension(
                                associatedProject.getRootDirectory().toPath().relativize(file.toPath()).toString()
                        ) : file.getName()
        ).replace(File.separator,"/");
    }

    /**
     * ProjectUtil should not be instantiated.
     * */
    private ProjectUtil() {
    }
}
