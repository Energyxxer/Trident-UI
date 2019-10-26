package com.energyxxer.trident.ui.commodoreresources;

import com.energyxxer.trident.global.ProcessManager;
import com.energyxxer.trident.global.Resources;
import com.energyxxer.trident.global.temp.projects.ProjectManager;
import com.energyxxer.trident.ui.dialogs.OptionDialog;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.util.processes.AbstractProcess;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.bind.util.ISO8601Utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;

public class DefinitionUpdateProcess extends AbstractProcess {

    private final Gson gson = new Gson();

    private boolean promptedUpdate = false;
    private String lastCheckedDefCommit = null;
    private Date latestCommitDate = null;

    public DefinitionUpdateProcess() {
        super("Definition Update");
        initializeThread(this::checkForUpdates);
    }

    public static void tryUpdate() {
        ProcessManager.queueProcess(new DefinitionUpdateProcess());
    }

    public void checkForUpdates() {
        try {
            updateStatus("Checking for updates");
            JsonElement lastCheckedDefCommitElement = Resources.resources.get("last-checked-definition-commit");
            lastCheckedDefCommit = null;
            if(lastCheckedDefCommitElement != null) {
                lastCheckedDefCommit = lastCheckedDefCommitElement.getAsString();
            }

            String defPackCommitsURL = "https://api.github.com/repos/Energyxxer/Minecraft-Definitions/commits?path=defpacks/zipped/";
            if(lastCheckedDefCommit != null) defPackCommitsURL += "&since=" + lastCheckedDefCommit;

            String featMapCommitsURL = "https://api.github.com/repos/Energyxxer/Minecraft-Definitions/commits?path=featuremaps/";
            if(lastCheckedDefCommit != null) featMapCommitsURL += "&since=" + lastCheckedDefCommit;

            String typeMapCommitsURL = "https://api.github.com/repos/Energyxxer/Minecraft-Definitions/commits?path=typemaps/zipped/";
            if(lastCheckedDefCommit != null) typeMapCommitsURL += "&since=" + lastCheckedDefCommit;

            HashSet<String> defPackChanges = new HashSet<>();
            HashSet<String> featMapChanges = new HashSet<>();
            HashSet<String> typeMapChanges = new HashSet<>();

            scanCommitOverview(defPackCommitsURL, defPackChanges, "defpacks/zipped/", ".zip");
            scanCommitOverview(featMapCommitsURL, featMapChanges, "featuremaps/", ".json");
            scanCommitOverview(typeMapCommitsURL, typeMapChanges, "typemaps/zipped/", ".zip");

            updateStatus("Updating definition packs");
            syncChanges(defPackChanges, "defpacks/zipped/*.zip", "resources" + File.separator + "defpacks" + File.separator + "*.zip");
            updateStatus("Updating feature maps");
            syncChanges(featMapChanges, "featuremaps/*.json", "resources" + File.separator + "featmaps" + File.separator + "*.json");
            updateStatus("Updating type maps");
            syncChanges(typeMapChanges, "typemaps/zipped/*.zip", "resources" + File.separator + "typemaps" + File.separator + "*.zip");

            if(promptedUpdate) {
                DefinitionPacks.loadAll();
                VersionFeatureResources.loadAll();
                TypeMaps.loadAll();
                ProjectManager.loadWorkspace();

                Resources.resources.addProperty("last-checked-definition-commit", ISO8601Utils.format(latestCommitDate));
                Resources.saveAll();
            }

            updateStatus("All definitions up to date");
            this.finalizeProcess(true);
        } catch(UnknownHostException ignored) {
            Debug.log("Unable to check for updates: no internet connection");
        } catch(UpdateAbortException ignored) {
        } catch (Exception x) {
            x.printStackTrace();
        }
        updateStatus("");
        this.finalizeProcess(false);
    }

    private void scanCommitOverview(String commitListURL, HashSet<String> changeSet, String expectedPrefix, String expectedSuffix) throws ParseException, IOException {
        JsonArray defPackCommits = gson.fromJson(new InputStreamReader(retrieveStreamForURL(commitListURL, false)), JsonArray.class);

        for(JsonElement commitOverviewRaw : defPackCommits) {
            JsonObject commitOverview = commitOverviewRaw.getAsJsonObject();

            String commitDateRaw = commitOverview.getAsJsonObject("commit").getAsJsonObject("committer").get("date").getAsString();
            if(Objects.equals(commitDateRaw, lastCheckedDefCommit)) continue;
            Date commitDate = ISO8601Utils.parse(commitDateRaw, new ParsePosition(0));
            if(latestCommitDate == null) {
                latestCommitDate = commitDate;
            } else if(commitDate.after(latestCommitDate)) {
                latestCommitDate = commitDate;
            }

            String commitDetailsURL = commitOverview.get("url").getAsString();

            JsonObject commitDetails = gson.fromJson(new InputStreamReader(retrieveStreamForURL(commitDetailsURL, false)), JsonObject.class);
            JsonArray changedFiles = commitDetails.getAsJsonArray("files");
            for(JsonElement file : changedFiles) {
                boolean valid = false;
                String filename = file.getAsJsonObject().get("filename").getAsString();

                if(filename.contains("j_") && filename.startsWith(expectedPrefix) && filename.endsWith(expectedSuffix)) {
                    Debug.log("CHANGED:" + filename);
                    filename = filename.substring(expectedPrefix.length(), filename.length() - expectedSuffix.length());
                    changeSet.add(filename);
                    valid = true;
                }
                String status = file.getAsJsonObject().get("status").getAsString();
                if("renamed".equals(status)) {
                    String previousFilename = file.getAsJsonObject().get("previous_filename").getAsString();

                    if(previousFilename.contains("j_") && previousFilename.startsWith(expectedPrefix) && previousFilename.startsWith(expectedSuffix)) {
                        previousFilename = previousFilename.substring(expectedPrefix.length(), previousFilename.length() - expectedSuffix.length());
                        changeSet.add(previousFilename);
                        valid = true;
                    }
                }

                if(valid && !promptedUpdate) {
                    updateStatus("");
                    String confirmation = new OptionDialog("Minecraft Definitions", "There are updates to definitions available. Update them now?", new String[] {"Update", "Not now"}).result;
                    if(!"Update".equals(confirmation)) {
                        Debug.log("Aborting update process...");
                        throw new UpdateAbortException();
                    }
                    updateStatus("Retrieving updated files");
                    promptedUpdate = true;
                }
            }
        }
    }

    private void syncChanges(HashSet<String> changeSet, String repoPath, String destinationPath) throws IOException {
        for(String filename : changeSet) {
            InputStream is = retrieveStreamForURL("https://github.com/Energyxxer/Minecraft-Definitions/raw/master/" + repoPath.replace("*", filename), true);
            File targetFile = new File(System.getProperty("user.home") + File.separator + "Trident" + File.separator + destinationPath.replace("*", filename));
            if(is == null) {
                // File deleted
                Debug.log("DELETE FILE " + targetFile);
                //targetFile.delete();
            } else {
                Debug.log("CREATE FILE " + targetFile);
                targetFile.getParentFile().mkdirs();
                targetFile.createNewFile();
                Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                is.close();
            }
        }
    }

    private static InputStream retrieveStreamForURL(String link, boolean accept404Null) throws IOException {
        URL latestURL = new URL(link);
        HttpURLConnection connection = (HttpURLConnection) latestURL.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setInstanceFollowRedirects(true);

        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        connection.connect();
        int status = connection.getResponseCode();

        if(200 <= status && status <= 299) {
            // OK
            return connection.getInputStream();
        } else if(accept404Null && status == 404) {
            // 404
            return null;
        } else {
            // ERROR
            throw new IOException(inputStreamToString(connection.getErrorStream()));
        }
    }

    private static String inputStreamToString(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(is));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }

    public static void startUpdateCheckProcess() {
        ProcessManager.queueProcess(new DefinitionUpdateProcess());
    }

    private class UpdateAbortException extends RuntimeException {
    }
}
