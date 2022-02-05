package com.energyxxer.trident.guardian;

import com.energyxxer.commodore.versioning.ThreeNumberVersion;
import com.energyxxer.guardian.GuardianCore;
import com.energyxxer.guardian.ui.ToolbarButton;
import com.energyxxer.guardian.ui.common.ProgramUpdateProcess;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.trident.Trident;
import com.energyxxer.trident.global.Preferences;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.energyxxer.guardian.util.NetworkUtil.retrieveStreamForURLAuth;

public class TridentGuardianCore extends GuardianCore {

    public static final boolean IS_DEVELOPMENT_VERSION = true;
    public static ThreeNumberVersion UI_VERSION = new ThreeNumberVersion(1,6,0);
    public static final String MIXED_VERSION = "u" + UI_VERSION + "c" + Trident.TRIDENT_LANGUAGE_VERSION + (IS_DEVELOPMENT_VERSION ? "+DEV" : "");

    private static final Pattern MIXED_VERSION_REGEX = Pattern.compile("u(\\d+)\\.(\\d+)\\.(\\d+)c(\\d+)\\.(\\d+)\\.(\\d+)(-.+)?");

    public TridentGuardianCore() {
        super();
        programName = "Trident-UI";
        mainDirectory = new File(System.getProperty("user.home") + File.separator + "Trident").toPath();

        resourceRegistry = Preferences.class;
    }

    @Override
    public boolean usesJavaEditionDefinitions() {
        return true;
    }

    @Override
    public String getDisplayedVersion() {
        return MIXED_VERSION;
    }

    @Override
    public Collection<JComponent> createWelcomePaneButtons(ThemeListenerManager tlm) {
        ToolbarButton button = new ToolbarButton("trident_file", tlm);
        button.setText("Trident Website");
        button.setHintText("Go to the official Trident Website");
        button.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("http://energyxxer.com/trident"));
            } catch (IOException | URISyntaxException ex) {
                ex.printStackTrace();
            }
        });
        return Collections.singletonList(button);
    }

    private static final String LATEST_RELEASE_URL = "https://api.github.com/repos/Energyxxer/Trident-UI/releases/latest";

    @Override
    public ProgramUpdateProcess.ProgramVersionInfo checkForUpdates() throws IOException {
        JsonObject latestReleaseSummary = new Gson().fromJson(new InputStreamReader(retrieveStreamForURLAuth(LATEST_RELEASE_URL, false)), JsonObject.class);

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

        if(latestUIVersion.compare(UI_VERSION) > 0 || latestCompilerVersion.compare(Trident.TRIDENT_LANGUAGE_VERSION) > 0) {
            JsonArray assets = latestReleaseSummary.getAsJsonArray("assets");
            for(JsonElement elem : assets) {
                if(elem instanceof JsonObject && ((JsonObject) elem).get("name").getAsString().endsWith(".jar")) {
                    String jarName = "Trident-UI " + name + ".jar";
                    String downloadUrl = ((JsonObject) elem).get("browser_download_url").getAsString();
                    return new ProgramUpdateProcess.ProgramVersionInfo(name, jarName, htmlUrl, downloadUrl);
                }
            }
        }
        return null;
    }

    @Override
    public URI getDocumentationURI() throws URISyntaxException {
        return new URI("https://docs.google.com/document/d/1w_3ILt8-8s1VG-qv7cLLdIrTJTtbQvj2klh2xTnxQVw/edit?usp=sharing");
    }
}
