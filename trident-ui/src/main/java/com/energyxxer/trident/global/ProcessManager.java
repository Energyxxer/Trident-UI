package com.energyxxer.trident.global;

import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.util.processes.AbstractProcess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class ProcessManager {
    private static List<AbstractProcess> activeProcesses = Collections.synchronizedList(new ArrayList<>());

    public static void queueProcess(AbstractProcess process) {
        if(activeProcesses.contains(process)) return;
        process.addProgressListener((p) -> {
            updateStatusBar();
        });
        process.addCompletionListener((p, s) -> {
            Debug.log("Process completed: " + process);
            activeProcesses.remove(process);
            TridentWindow.processBoard.removeProcess(process);
            updateStatusBar();
        });
        activeProcesses.add(process);
        TridentWindow.processBoard.addProcess(process);

        process.start();
    }

    public static boolean any(Predicate<AbstractProcess> p) {
        for(AbstractProcess process : activeProcesses) {
            if(p.test(process)) return true;
        }
        return false;
    }

    private static void updateStatusBar() {
        if(!activeProcesses.isEmpty()) {
            if(activeProcesses.size() == 1) {
                AbstractProcess process = activeProcesses.get(0);
                TridentWindow.statusBar.setStatus(new Status(Status.INFO, process.getStatus(), process.getProgress()));
            } else {
                TridentWindow.statusBar.setStatus(new Status(Status.INFO, activeProcesses.size() + " processes running...", null));
            }
        } else {
            TridentWindow.statusBar.setProgress(null);
        }
        TridentWindow.processBoard.repaint();
    }

    public static int getCount() {
        return activeProcesses.size();
    }
}
