package com.energyxxer.trident.global;

import com.energyxxer.trident.compiler.TridentCompiler;
import com.energyxxer.trident.global.temp.projects.TridentProject;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.util.out.Console;
import com.energyxxer.util.processes.AbstractProcess;
import com.energyxxer.util.processes.CompletionListener;
import com.energyxxer.util.processes.ProgressListener;

public class TridentCompilerWrapper extends AbstractProcess {
    private TridentProject project;
    private TridentCompiler compiler;

    public TridentCompilerWrapper(TridentProject project) {
        super("Trident-Compiler[" + project.getRootDirectory().getName() + "]");
        this.project = project;

        compiler = new TridentCompiler(project.getRootDirectory());

        compiler.setBuildConfig(project.getBuildConfig());

        compiler.setSourceCache(project.getSourceCache());
        compiler.setInResourceCache(project.getResourceCache());

        compiler.addStartListener(p -> TridentWindow.consoleBoard.batchSubmitCommand(project.getPreActions()));

        compiler.addCompletionListener((process, success) -> {
            TridentWindow.noticeExplorer.setNotices(compiler.getReport().group());
            if (compiler.getReport().getTotal() > 0) TridentWindow.noticeBoard.open();
            compiler.getReport().getWarnings().forEach(Console.warn::println);
            compiler.getReport().getErrors().forEach(Console.err::println);
            project.updateServerDataCache(compiler.getSourceCache());
            project.updateClientDataCache(compiler.getOutResourceCache());

            TridentWindow.consoleBoard.batchSubmitCommand(project.getPostActions());
        });
    }

    @Override
    public boolean isRunning() {
        return compiler.isRunning();
    }

    @Override
    public void start() {
        compiler.start();
    }

    @Override
    public void terminate() {
        compiler.terminate();
    }

    @Override
    public void addProgressListener(ProgressListener listener) {
        compiler.addProgressListener(listener);
    }

    @Override
    public void removeProgressListener(ProgressListener listener) {
        compiler.removeProgressListener(listener);
    }

    @Override
    public void addCompletionListener(CompletionListener listener) {
        compiler.addCompletionListener(listener);
    }

    @Override
    public void removeCompletionListener(CompletionListener listener) {
        compiler.removeCompletionListener(listener);
    }

    @Override
    public String getName() {
        return compiler.getName();
    }

    @Override
    public String getStatus() {
        return compiler.getStatus();
    }

    @Override
    public float getProgress() {
        return compiler.getProgress();
    }

    @Override
    public Thread getThread() {
        return compiler.getThread();
    }
}
