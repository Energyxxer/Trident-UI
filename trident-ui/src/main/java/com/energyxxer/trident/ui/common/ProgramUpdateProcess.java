package com.energyxxer.trident.ui.common;

import com.energyxxer.commodore.versioning.ThreeNumberVersion;
import com.energyxxer.trident.global.Preferences;
import com.energyxxer.trident.global.ProcessManager;
import com.energyxxer.trident.main.TridentUI;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.main.window.actions.ActionManager;
import com.energyxxer.trident.ui.commodoreresources.DefinitionUpdateProcess;
import com.energyxxer.trident.ui.dialogs.OptionDialog;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.util.processes.AbstractProcess;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.energyxxer.trident.ui.commodoreresources.DefinitionUpdateProcess.retrieveStreamForURL;

public class ProgramUpdateProcess extends AbstractProcess {
    public static Preferences.SettingPref<Boolean> CHECK_FOR_PROGRAM_UPDATES_STARTUP = new Preferences.SettingPref<>("settings.behavior.check_program_updates_startup", true, Boolean::parseBoolean);

    private final Gson gson = new Gson();
    private static final String LATEST_RELEASE_URL = "https://api.github.com/repos/Energyxxer/Trident-UI/releases/latest";
    private static final Pattern MIXED_VERSION_REGEX = Pattern.compile("u(\\d+)\\.(\\d+)\\.(\\d+)c(\\d+)\\.(\\d+)\\.(\\d+)(-.+)?");

    private static final ThreeNumberVersion thisUIVersion;
    private static final ThreeNumberVersion thisCompilerVersion;

    static {
        Matcher matcher = MIXED_VERSION_REGEX.matcher(TridentUI.MIXED_VERSION);
        matcher.lookingAt();
        thisUIVersion = new ThreeNumberVersion(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3))) {
            @Override
            public @NotNull String getEditionString() {
                return "u";
            }
        };
        thisCompilerVersion = new ThreeNumberVersion(Integer.parseInt(matcher.group(4)), Integer.parseInt(matcher.group(5)), Integer.parseInt(matcher.group(6))) {
            @Override
            public @NotNull String getEditionString() {
                return "c";
            }
        };
    }

    public ProgramUpdateProcess() {
        super("Program Update");
        initializeThread(this::checkForUpdates);
    }

    public static void tryUpdate() {
        ProcessManager.queueProcess(new ProgramUpdateProcess());
    }

    public void checkForUpdates() {
        try {
            updateStatus("Checking for program updates");
            JsonObject latestReleaseSummary = gson.fromJson(new InputStreamReader(retrieveStreamForURL(LATEST_RELEASE_URL, false)), JsonObject.class);

            String htmlUrl = latestReleaseSummary.get("html_url").getAsString();
            String name = latestReleaseSummary.get("name").getAsString();
            Matcher matcher = MIXED_VERSION_REGEX.matcher(name);
            matcher.lookingAt();
            ThreeNumberVersion latestUIVersion = new ThreeNumberVersion(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3))) {
                @Override
                public @NotNull String getEditionString() {
                    return "u";
                }
            };
            ThreeNumberVersion latestCompilerVersion = new ThreeNumberVersion(Integer.parseInt(matcher.group(4)), Integer.parseInt(matcher.group(5)), Integer.parseInt(matcher.group(6))) {
                @Override
                public @NotNull String getEditionString() {
                    return "c";
                }
            };

            boolean runningJar = TridentUI.RUNNING_PATH != null && TridentUI.RUNNING_PATH.isFile() && TridentUI.RUNNING_PATH.getName().endsWith(".jar");

            String[] options = runningJar ? new String[] {"View changelog", "Replace my .jar", "Not now"} : new String[] {"View changelog", "Not now"};

            boolean updateAvailable = latestUIVersion.compare(thisUIVersion) > 0 || latestCompilerVersion.compare(thisCompilerVersion) > 0;
            if(updateAvailable) {
                updateStatus("Newer version found: " + name);
                updateProgress(0);
                OptionDialog dialog = new OptionDialog("Program Update", "A new version of Trident-UI is available. Update now?", options);
                if("View changelog".equals(dialog.result)) {
                    try {
                        Desktop.getDesktop().browse(new URI(htmlUrl));
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                } else if("Replace my .jar".equals(dialog.result)) {
                    Debug.log(latestReleaseSummary);
                    JsonArray assets = latestReleaseSummary.getAsJsonArray("assets");
                    for(JsonElement elem : assets) {
                        if(elem instanceof JsonObject && ((JsonObject) elem).get("name").getAsString().endsWith(".jar")) {
                            Path newJar = TridentUI.RUNNING_PATH.getParentFile().toPath().resolve("Trident-UI " + name.replace('.','_') + ".jar");

                            Preferences.put("meta.delete_old_jar", TridentUI.RUNNING_PATH.getPath());

                            updateStatusAndProgress("Downloading new JAR", -1);
                            InputStream is = retrieveStreamForURL(((JsonObject) elem).get("browser_download_url").getAsString(), false);
                            Files.copy(is, newJar, StandardCopyOption.REPLACE_EXISTING);
                            is.close();

                            TridentWindow.setRestartingJar(newJar.toFile());
                            ActionManager.getAction("EXIT").perform();

                            break;
                        }
                    }
                }
            }
            this.finalizeProcess(true);
        } catch(UnknownHostException ignored) {
            Debug.log("Unable to check for updates: no internet connection");
        } catch(DefinitionUpdateProcess.UpdateAbortException ignored) {
        } catch (Exception x) {
            x.printStackTrace();
        }
        updateStatus("");
        this.finalizeProcess(false);
    }


}
