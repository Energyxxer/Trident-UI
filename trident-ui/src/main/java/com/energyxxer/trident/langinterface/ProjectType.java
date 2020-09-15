package com.energyxxer.trident.langinterface;

import com.energyxxer.trident.global.temp.projects.Project;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public abstract class ProjectType {
    private static final ArrayList<ProjectType> registeredProjectTypes = new ArrayList<>();

    private final String code;
    private final String name;
    private final HashMap<String, String> properties = new HashMap<>();

    public ProjectType(String code, String name) {
        this.code = code;
        this.name = name;

        registeredProjectTypes.add(this);
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static Collection<ProjectType> values() {
        return registeredProjectTypes;
    }

    public static ProjectType getProjectTypeForRoot(File file) {
        for(ProjectType type : registeredProjectTypes) {
            if(type.isProjectRoot(file)) return type;
        }
        return null;
    }
    public static boolean isAnyProjectRoot(File file) {
        return getProjectTypeForRoot(file) != null;
    }

    public abstract boolean isProjectRoot(File file);

    public abstract Image getIconForRoot(File file);

    public abstract String getDefaultProjectIconName();

    public abstract Project createProjectFromRoot(File file);

    public abstract Project createNew(Path rootPath);

    public abstract void showProjectPropertiesDialog(Project project);
}
