package com.energyxxer.trident.global;

import com.energyxxer.crossbow.compiler.util.CrossbowProjectSummarizer;
import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummarizer;
import com.energyxxer.trident.compiler.TridentBuildConfiguration;
import com.energyxxer.trident.compiler.util.TridentProjectSummarizer;
import com.energyxxer.trident.global.temp.projects.CrossbowProject;
import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.global.temp.projects.TridentProject;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.util.processes.AbstractProcess;

import java.util.Objects;

public class IndexingProcess extends AbstractProcess {
    private Project project;
    private ProjectSummarizer summarizer;

    public IndexingProcess(Project project) {
        super("Indexing");
        this.project = project;
        if(project instanceof TridentProject) {
            TridentBuildConfiguration buildConfig = ((TridentProject) project).getBuildConfig();

            summarizer = new TridentProjectSummarizer(
                    project.getRootDirectory(),
                    buildConfig
            );
            summarizer.setSourceCache(project.getSourceCache());
            summarizer.addCompletionListener(() -> {
                Debug.log("Finished indexing project: " + project.getName());
                project.updateServerDataCache(summarizer.getSourceCache());
                project.updateSummary(summarizer.getSummary());
                this.updateStatus("");
                this.finalizeProcess(true);
            });
            initializeThread(this::startSummarizer);
        } else if(project instanceof CrossbowProject) {
            summarizer = new CrossbowProjectSummarizer(project.getRootDirectory());
            summarizer.setSourceCache(project.getSourceCache());
            summarizer.addCompletionListener(() -> {
                Debug.log("Finished indexing project: " + project.getName());
                project.updateServerDataCache(summarizer.getSourceCache());
                project.updateSummary(summarizer.getSummary());
                this.updateStatus("");
                this.finalizeProcess(true);
            });
            initializeThread(this::startSummarizer);
        }
    }

    private void startSummarizer() {
        summarizer.start();
        updateStatus("Updating indices [" + project.getRootDirectory().getName() + "]");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexingProcess that = (IndexingProcess) o;
        return Objects.equals(project, that.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(project);
    }
}
