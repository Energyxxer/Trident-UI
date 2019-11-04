package com.energyxxer.trident.main.window.sections.tools.find;

import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.global.temp.projects.ProjectManager;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.explorer.base.ExplorerMaster;
import com.energyxxer.trident.ui.explorer.base.StandardExplorerItem;
import com.energyxxer.trident.ui.modules.FileModuleToken;
import com.energyxxer.trident.ui.modules.NonStandardModuleToken;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class FileOccurrence extends QueryResult implements NonStandardModuleToken {
    private FileModuleToken fileToken;
    File file;
    int start;
    int length;
    int lineNum;
    String linePreview;
    int previewOffset;

    public FileOccurrence(File file, int start, int length, int lineNum, String linePreview, int previewOffset) {
        int trimStart = 0;
        while(previewOffset > 0 && trimStart < linePreview.length() && Character.isWhitespace(linePreview.charAt(trimStart))) {
            trimStart++;
            previewOffset--;
        }
        linePreview = linePreview.substring(trimStart);
        int trimEnd = linePreview.length();
        while(trimEnd > 0 && trimEnd > previewOffset+length+1 && Character.isWhitespace(linePreview.charAt(trimEnd-1))) {
            trimEnd--;
        }
        linePreview = linePreview.substring(0, trimEnd);

        this.file = file;
        this.fileToken = new FileModuleToken(file);
        this.start = start;
        this.length = length;
        this.lineNum = lineNum;
        this.linePreview = linePreview;
        this.previewOffset = previewOffset;
    }

    @Override
    public Collection<QueryResult> getInnerOccurrences() {
        return null;
    }

    public File getProjectRoot() {
        Project project = ProjectManager.getAssociatedProject(file);
        return project != null ? project.getRootDirectory() : null;
    }

    public String getSubProjectRootName() {
        Path relative = getProjectRoot().toPath().relativize(file.getParentFile().toPath());
        if(relative.getNameCount() > 0) {
            return relative.getName(0).toString();
        } else {
            return null;
        }
    }

    public File getSubProjectRoot() {
        Path projects = getProjectRoot().toPath();
        Path files = getFile().toPath();
        return files.getRoot().resolve(files.subpath(0, projects.getNameCount()+1)).toFile();
    }

    public String getPath() {
        Path relative = getProjectRoot().toPath().relativize(file.getParentFile().toPath());
        if(relative.getNameCount() > 1) {
            relative = relative.subpath(1, relative.getNameCount());
        }
        return relative.toString().replace(File.separator, "/");
    }

    public File getFile() {
        return file;
    }

    @Override
    public String getTitle() {
        return linePreview;
    }

    @Override
    public Image getIcon() {
        return fileToken.getIcon();
    }

    @Override
    public boolean isExpandable() {
        return false;
    }

    @Override
    public void onInteract() {
        TridentWindow.tabManager.openTab(new FileModuleToken(file), start, length);
    }

    @Override
    public FileOccurrenceExplorerItem createElement(StandardExplorerItem parent) {
        return new FileOccurrenceExplorerItem(this, parent);
    }

    @Override
    public FileOccurrenceExplorerItem createElement(ExplorerMaster parent) {
        return new FileOccurrenceExplorerItem(this, parent);
    }

    @Override
    public void collectFileOccurrences(List<FileOccurrence> target) {
        target.add(this);
    }

    public int getStart() {
        return start;
    }

    public int getLength() {
        return length;
    }

    public int getLineNum() {
        return lineNum;
    }

    public String getLinePreview() {
        return linePreview;
    }

    public int getPreviewOffset() {
        return previewOffset;
    }
}
