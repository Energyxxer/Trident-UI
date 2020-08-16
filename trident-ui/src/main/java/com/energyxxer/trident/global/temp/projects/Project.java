package com.energyxxer.trident.global.temp.projects;

import com.energyxxer.commodore.versioning.Version;
import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummarizer;
import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummary;
import com.energyxxer.enxlex.pattern_matching.ParsingSignature;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.LazyTokenPatternMatch;
import com.energyxxer.trident.langinterface.ProjectType;
import com.energyxxer.util.processes.AbstractProcess;

import java.awt.*;
import java.io.File;
import java.util.HashMap;

public interface Project {
	LazyTokenPatternMatch getFileStructure();
	ProjectSummary getSummary();
	File getRootDirectory();
	HashMap<String, ParsingSignature> getSourceCache();
	HashMap<String, ParsingSignature> getResourceCache();
	String getName();
	File getServerDataRoot();
	File getClientDataRoot();
	void updateServerDataCache(HashMap<String, ParsingSignature> sourceCache);
	void updateClientDataCache(HashMap<String, ParsingSignature> sourceCache);
	void updateSummary(ProjectSummary summary);
	void updateConfig();
	void createNew();
    Version getTargetVersion();
    Image getIconForFile(File file);

	Iterable<String> getPreActions();
	Iterable<String> getPostActions();

	ProjectType getProjectType();
	ProjectSummarizer createProjectSummarizer();

	AbstractProcess createBuildProcess();
}
