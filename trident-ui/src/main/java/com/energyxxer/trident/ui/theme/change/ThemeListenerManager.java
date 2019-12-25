package com.energyxxer.trident.ui.theme.change;

import com.energyxxer.trident.global.Status;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.main.window.sections.tools.ConsoleBoard;
import com.energyxxer.util.Disposable;
import com.energyxxer.util.logger.Debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ThemeListenerManager implements Disposable {
    private List<ThemeChangeListener> listeners = new ArrayList<>();
    private StackTraceElement[] stackTrace;

    public ThemeListenerManager() {
        this.stackTrace = Thread.currentThread().getStackTrace();
    }

    public void addThemeChangeListener(ThemeChangeListener l) {
        addThemeChangeListener(l, false);
    }
    public void addThemeChangeListener(ThemeChangeListener l, boolean priority) {
        ThemeChangeListener.addThemeChangeListener(l, priority);
        listeners.add(l);
    }

    @Override
    public void dispose() {
        if(listeners != null) {
            listeners.forEach(ThemeChangeListener::dispose);
            listeners.clear();
            listeners = null;
        } else {
            StackTraceElement call = stackTrace[2];
            String source = call.getClassName().substring(call.getClassName().lastIndexOf('.')+1) + "." + call.getMethodName() + "(" + call.getFileName() + ":" + call.getLineNumber() + ")";

            TridentWindow.statusBar.setStatus(new Status(Status.ERROR, "ERROR: Reused ThemeListenerManager '" + source + "'. Please report to Energyxxer. More details in the console."));

            StringBuilder extendedError = new StringBuilder("ERROR: Reused ThemeListenerManager. Please report to Energyxxer. Entire stack trace of the TLM instantiation:\n");
            for(StackTraceElement element : stackTrace) {
                extendedError.append("\tat ");
                extendedError.append(element.toString());
                extendedError.append('\n');
            }

            Debug.log(extendedError.toString(), Debug.MessageType.ERROR);
        }
    }

    private static Collection<ThemeChangeListener> seen = null;
    private static ArrayList<ThemeChangeListener> unseen = new ArrayList<>();

    static {
        ConsoleBoard.registerCommandHandler("tlm", new ConsoleBoard.CommandHandler() {

            @Override
            public String getDescription() {
                return "Provides an interface to keep track of active Theme Change Listeners in the GUI";
            }

            @Override
            public void printHelp() {
                Debug.log();
                Debug.log("TLM: Provides an interface to keep");
                Debug.log("track of active Theme Change Listeners in the GUI.");
                Debug.log();
                Debug.log("Available subcommands:");
                Debug.log("  > tlm count         (returns the number of active TCLs)");
                Debug.log("  > tlm list          (prints a list of all active TCL objects)");
                Debug.log("  > tlm list unseen   (prints a list of active TCL objects that weren't present when `tlm save` was last run)");
                Debug.log("  > tlm save          (saves a snapshot of the current active TCLs to exclude when using `tlm list unseen`)");
            }

            @Override
            public void handle(String[] args) {
                if (args.length >= 2) {
                    switch (args[1]) {
                        case "count": {
                            Debug.log("Number of active theme listeners: " + ThemeChangeListener.listeners.size());
                            return;
                        }
                        case "list": {
                            if (args.length >= 3 && args[2].equals("unseen")) {
                                unseen.addAll(ThemeChangeListener.listeners);
                                unseen.removeAll(seen);
                                Debug.log("Active theme listeners, excluding those marked as seen (" + unseen.size() + "/" + ThemeChangeListener.listeners.size() + ")");
                                Debug.log(unseen);
                                unseen.clear();
                            } else {
                                Debug.log("All active theme listeners (" + ThemeChangeListener.listeners.size() + ")");
                                Debug.log(ThemeChangeListener.listeners);
                            }
                            return;
                        }
                        case "save": {
                            if (seen == null) seen = new ArrayList<>();
                            seen.clear();
                            seen.addAll(ThemeChangeListener.listeners);
                            Debug.log("Marked " + seen.size() + " theme change listeners as seen");
                            return;
                        }
                    }
                }
                printHelp();
            }
        });
    }
}
