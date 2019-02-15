package com.energyxxer.trident.main.window.sections.tools.find;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.ui.modules.FileModuleToken;
import com.energyxxer.trident.ui.modules.ModuleToken;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class FindResults extends QueryResult {
    private HashMap<File, ProjectResult> projectResults = new HashMap<>();

    @Override
    public void insertResult(FileOccurrence occurrence) {
        super.insertResult(occurrence);
        File projectRoot = occurrence.getProjectRoot();
        if(!projectResults.containsKey(projectRoot)) {
            projectResults.put(projectRoot, new ProjectResult(projectRoot));
        }
        projectResults.get(projectRoot).insertResult(occurrence);
    }

    @Override
    public Collection<? extends QueryResult> getInnerOccurrences() {
        return projectResults.values();
    }

    @Override
    public String getTitle() {
        return "Found Occurrences";
    }

    @Override
    public Image getIcon() {
        return Commons.getIcon("search");
    }

    @Override
    public boolean isExpandable() {
        return true;
    }

    @Override
    public void collectFileOccurrences(List<FileOccurrence> target) {
        projectResults.values().forEach(r -> r.collectFileOccurrences(target));
    }

    public static class ProjectResult extends QueryResult {
        private File projectRoot;
        private HashMap<String, SubProjectResult> subResults = new HashMap<>();
        private ArrayList<FileOccurrence> loneResults = new ArrayList<>();

        public ProjectResult(File projectRoot) {
            this.projectRoot = projectRoot;
        }

        @Override
        public void insertResult(FileOccurrence occurrence) {
            super.insertResult(occurrence);
            String subProject = occurrence.getSubProjectRootName();
            if(subProject == null) {
                loneResults.add(occurrence);
            } else {
                if(!subResults.containsKey(subProject)) {
                    subResults.put(subProject, new SubProjectResult(subProject, occurrence.getSubProjectRoot()));
                }
                subResults.get(subProject).insertResult(occurrence);
            }
        }

        @Override
        public Collection<QueryResult> getInnerOccurrences() {
            ArrayList<QueryResult> inner = new ArrayList<>(subResults.values());
            inner.addAll(loneResults);
            return inner;
        }

        @Override
        public String getTitle() {
            return projectRoot.getName();
        }

        @Override
        public Image getIcon() {
            return Commons.getProjectIcon();
        }

        @Override
        public Collection<? extends ModuleToken> getSubTokens() {
            return getInnerOccurrences();
        }

        @Override
        public boolean isExpandable() {
            return true;
        }

        @Override
        public void collectFileOccurrences(List<FileOccurrence> target) {
            subResults.values().forEach(r -> r.collectFileOccurrences(target));
            loneResults.forEach(r -> r.collectFileOccurrences(target));
        }
    }

    public static class SubProjectResult extends QueryResult { //Data or resources
        private FileModuleToken subRootFile;
        private String rootName;

        private HashMap<String, PathResult> pathResults = new HashMap<>();

        public SubProjectResult(String rootName, File subRootFile) {
            this.rootName = rootName;
            this.subRootFile = new FileModuleToken(subRootFile);
        }

        @Override
        public Collection<PathResult> getInnerOccurrences() {
            return pathResults.values();
        }

        @Override
        public void insertResult(FileOccurrence occurrence) {
            super.insertResult(occurrence);
            String path = occurrence.getPath();
            if(!pathResults.containsKey(path)) {
                pathResults.put(path, new PathResult(path));
            }
            pathResults.get(path).insertResult(occurrence);
        }

        @Override
        public String getTitle() {
            return rootName;
        }

        @Override
        public Image getIcon() {
            return subRootFile.getIcon();
        }

        @Override
        public boolean isExpandable() {
            return true;
        }

        @Override
        public void collectFileOccurrences(List<FileOccurrence> target) {
            pathResults.values().forEach(r -> r.collectFileOccurrences(target));
        }
    }

    public static class PathResult extends QueryResult {
        private String path;
        private HashMap<File, FileResult> fileResults = new HashMap<>();

        public PathResult(String path) {
            this.path = path;
        }

        @Override
        public Collection<FileResult> getInnerOccurrences() {
            return fileResults.values();
        }

        @Override
        public void insertResult(FileOccurrence occurrence) {
            super.insertResult(occurrence);
            File file = occurrence.getFile();
            if(!fileResults.containsKey(file)) {
                fileResults.put(file, new FileResult(file));
            }
            fileResults.get(file).insertResult(occurrence);
        }

        @Override
        public String getTitle() {
            return path;
        }

        @Override
        public Image getIcon() {
            return Commons.getIcon("folder");
        }

        @Override
        public boolean isExpandable() {
            return true;
        }

        @Override
        public void collectFileOccurrences(List<FileOccurrence> target) {
            fileResults.values().forEach(r -> r.collectFileOccurrences(target));
        }
    }

    public static class FileResult extends QueryResult {
        private FileModuleToken file;
        private ArrayList<FileOccurrence> occurrences = new ArrayList<>();

        public FileResult(File file) {
            this.file = new FileModuleToken(file);
        }

        @Override
        public void insertResult(FileOccurrence occurrence) {
            super.insertResult(occurrence);
            occurrences.add(occurrence);
        }

        @Override
        public Collection<FileOccurrence> getInnerOccurrences() {
            return occurrences;
        }

        @Override
        public String getTitle() {
            return file.getTitle();
        }

        @Override
        public Image getIcon() {
            return file.getIcon();
        }

        @Override
        public boolean isExpandable() {
            return true;
        }

        @Override
        public void collectFileOccurrences(List<FileOccurrence> target) {
            occurrences.forEach(r -> r.collectFileOccurrences(target));
        }
    }
}
