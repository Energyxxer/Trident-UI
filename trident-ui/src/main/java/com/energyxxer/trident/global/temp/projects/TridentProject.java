package com.energyxxer.trident.global.temp.projects;

import com.energyxxer.commodore.module.Namespace;
import com.energyxxer.commodore.versioning.JavaEditionVersion;
import com.energyxxer.commodore.versioning.compatibility.VersionFeatureManager;
import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummary;
import com.energyxxer.enxlex.pattern_matching.ParsingSignature;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.LazyTokenPatternMatch;
import com.energyxxer.nbtmapper.NBTTypeMapPack;
import com.energyxxer.trident.compiler.TridentBuildConfiguration;
import com.energyxxer.trident.compiler.TridentCompiler;
import com.energyxxer.trident.compiler.TridentProjectWorker;
import com.energyxxer.trident.compiler.lexer.TridentProductions;
import com.energyxxer.trident.compiler.util.JsonTraverser;
import com.energyxxer.trident.compiler.util.TridentProjectSummary;
import com.energyxxer.trident.main.TridentUI;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.commodoreresources.DefinitionPacks;
import com.energyxxer.trident.ui.commodoreresources.TridentPlugins;
import com.energyxxer.trident.ui.commodoreresources.TypeMaps;
import com.energyxxer.trident.ui.dialogs.OptionDialog;
import com.energyxxer.util.Lazy;
import com.energyxxer.util.StringUtil;
import com.energyxxer.util.logger.Debug;
import com.google.gson.*;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TridentProject implements Project {

    private File rootDirectory;
    public final long instantiationTime;
    private File datapackRoot;
    private File resourceRoot;
    private final File resourceCacheFile;
    private String name;

    private final Lazy<TridentProductions> productions = new Lazy<>(() -> {
        try {
            TridentBuildConfiguration buildConfig = getBuildConfig();

            TridentProjectWorker worker = new TridentProjectWorker(rootDirectory);
            worker.setup.setupProductions = true;
            worker.setBuildConfig(buildConfig);
            worker.work();
            return worker.output.productions;
        } catch(IOException x) {
            Debug.log("Exception while creating module: " + x.toString(), Debug.MessageType.ERROR);
            x.printStackTrace();
            return null;
        }
    });

    private ArrayList<String> preActions;
    private ArrayList<String> postActions;

    private final Lazy<TridentBuildConfiguration> buildConfig = new Lazy<>(() -> {
        TridentBuildConfiguration buildConfig = new TridentBuildConfiguration();
        buildConfig.defaultDefinitionPacks = DefinitionPacks.pickPacksForVersion(this.getTargetVersion());
        buildConfig.definitionPackAliases = DefinitionPacks.getAliasMap();
        buildConfig.featureMap = VersionFeatureManager.getFeaturesForVersion(this.getTargetVersion());
        buildConfig.pluginAliases = TridentPlugins.getAliasMap();
        buildConfig.typeMapPacks = new NBTTypeMapPack[] {TypeMaps.pickTypeMapsForVersion(this.getTargetVersion())};

        JsonObject rawBuildConfig = null;
        if(ensureBuildDataExists()) {
            try {
                rawBuildConfig = buildConfig.populateFromProjectRoot(getRootDirectory());
            } catch (Exception x) {
                x.printStackTrace();
                TridentWindow.showException(x);
            }
        }

        JsonTraverser traverser = new JsonTraverser(rawBuildConfig);

        preActions = new ArrayList<>();
        for(JsonElement rawCommand : traverser.reset().get("trident-ui").get("actions").get("pre").iterateAsArray()) {
            if(rawCommand.isJsonPrimitive() && rawCommand.getAsJsonPrimitive().isString()) {
                String command = rawCommand.getAsString();
                if(!command.isEmpty()) {
                    preActions.add(command);
                }
            }
        }

        postActions = new ArrayList<>();
        for(JsonElement rawCommand : traverser.reset().get("trident-ui").get("actions").get("post").iterateAsArray()) {
            if(rawCommand.isJsonPrimitive() && rawCommand.getAsJsonPrimitive().isString()) {
                String command = rawCommand.getAsString();
                if(!command.isEmpty()) {
                    postActions.add(command);
                }
            }
        }

        return buildConfig;
    });

    private JsonObject config;
    private JavaEditionVersion targetVersion = null;
    private HashMap<String, ParsingSignature> resourceCache = new HashMap<>();

    private HashMap<String, ParsingSignature> sourceCache = new HashMap<>();

    private TridentProjectSummary summary = null;

    public TridentProject(String name) {
        instantiationTime = System.currentTimeMillis();
        Path rootPath = Paths.get(ProjectManager.getWorkspaceDir()).resolve(name);
        this.rootDirectory = rootPath.toFile();

        datapackRoot = rootPath.resolve("datapack").toFile();
        resourceRoot = rootPath.resolve("resources").toFile();
        resourceCacheFile = rootDirectory.toPath().resolve(".tdnui").resolve("resource_cache").toFile();

        this.name = name;
        //this.prefix = StringUtil.getInitials(name).toLowerCase();

        Path outFolder = rootPath.resolve("out");

        JavaEditionVersion latestVersion = DefinitionPacks.getLatestKnownJavaVersion();
        if(latestVersion == null) latestVersion = new JavaEditionVersion(1, 13, 0);
        JsonArray latestVersionArr = new JsonArray(3);
        latestVersionArr.add(latestVersion.getMajor());
        latestVersionArr.add(latestVersion.getMinor());
        latestVersionArr.add(latestVersion.getPatch());

        config = new JsonObject();
        config.add("target-version", latestVersionArr);
        config.addProperty("default-namespace", StringUtil.getInitials(name).toLowerCase());
        config.addProperty("language-level", 1);
        config.addProperty("datapack-output", outFolder.resolve(name).toString());
        config.addProperty("resources-output", outFolder.resolve(name + "-resources.zip").toString());
        config.addProperty("export-comments", true);
        config.addProperty("strict-nbt", false);
        config.addProperty("strict-text-components", false);
        config.addProperty("anonymous-function-name", "_anonymous*");
        config.addProperty("using-all-plugins", false);
        JsonObject loggerObj = new JsonObject();
        loggerObj.addProperty("compact", false);
        loggerObj.addProperty("timestamp-enabled", true);
        loggerObj.addProperty("line-number-enabled", false);
        loggerObj.addProperty("pos-enabled", true);
        config.add("game-logger", loggerObj);
    }

    public TridentProject(File rootDirectory) {
        instantiationTime = System.currentTimeMillis();
        this.rootDirectory = rootDirectory;

        datapackRoot = rootDirectory.toPath().resolve("datapack").toFile();
        resourceRoot = rootDirectory.toPath().resolve("resources").toFile();
        File config = new File(rootDirectory.getAbsolutePath() + File.separator + TridentCompiler.PROJECT_FILE_NAME);
        this.name = rootDirectory.getName();

        resourceCacheFile = rootDirectory.toPath().resolve(".tdnui").resolve("resource_cache").toFile();
        if(resourceCacheFile.exists() && resourceCacheFile.isFile()) {
            try(InputStreamReader isr = new InputStreamReader(new FileInputStream(resourceCacheFile), TridentUI.DEFAULT_CHARSET)) {
                JsonObject jsonObject = new Gson().fromJson(isr, JsonObject.class);
                if(jsonObject != null) {
                    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        try {
                            resourceCache.put(entry.getKey(), new ParsingSignature(entry.getValue().getAsInt()));
                        } catch (NumberFormatException | UnsupportedOperationException x) {
                            x.printStackTrace();
                        }
                    }
                }
            } catch (IOException | JsonParseException x) {
                x.printStackTrace();
            }
        }

        if(config.exists() && config.isFile()) {
            try(InputStreamReader isr = new InputStreamReader(new FileInputStream(config), TridentUI.DEFAULT_CHARSET)) {
                this.config = new Gson().fromJson(isr, JsonObject.class);

                if(this.config.has("target-version") && this.config.get("target-version").isJsonArray()) {
                    JsonArray arr = this.config.getAsJsonArray("target-version");

                    int major = 1;
                    int minor = 14;
                    int patch = 0;

                    if(arr.size() >= 1) {
                        JsonElement rawMajor = arr.get(0);
                        if(rawMajor.isJsonPrimitive() && rawMajor.getAsJsonPrimitive().isNumber()) {
                            major = rawMajor.getAsInt();
                        }

                        if(arr.size() >= 2) {
                            JsonElement rawMinor = arr.get(1);
                            if(rawMinor.isJsonPrimitive() && rawMinor.getAsJsonPrimitive().isNumber()) {
                                minor = rawMinor.getAsInt();
                            }

                            if(arr.size() >= 3) {
                                JsonElement rawPatch = arr.get(2);
                                if(rawPatch.isJsonPrimitive() && rawPatch.getAsJsonPrimitive().isNumber()) {
                                    patch = rawPatch.getAsInt();
                                }
                            }
                        }
                    }

                    targetVersion = new JavaEditionVersion(major, minor, patch);
                }

                return;
            } catch (IOException | JsonParseException x) {
                x.printStackTrace();
            }
        }
        this.rootDirectory = null;
        throw new RuntimeException("Invalid configuration file.");
    }

    public LazyTokenPatternMatch getFileStructure() {
        return productions.getValue().FILE;
    }

    public void rename(String name) throws IOException {
        File newFile = new File(ProjectManager.getWorkspaceDir() + File.separator + name);
        if(newFile.exists()) {
            throw new IOException("A project by that name already exists!");
        }
        this.name = name;
        updateConfig();
    }

    public boolean canFlatten(File file) {
        if(getRelativePath(file) == null) return true;
        if(!file.getParentFile().equals(this.getRootDirectory())) return true;
        return !Arrays.asList("src","resources","data").contains(file.getName());
    }

    public void updateConfig() {
        File config = new File(rootDirectory.getAbsolutePath() + File.separator + TridentCompiler.PROJECT_FILE_NAME);
        PrintWriter writer;
        try {
            writer = new PrintWriter(config, "UTF-8");
            writer.print(new GsonBuilder().setPrettyPrinting().create().toJson(this.config));
            writer.close();
        } catch (IOException x) {
            Debug.log(x.getMessage());
        }
    }

    private boolean exists() {
        return rootDirectory != null && rootDirectory.exists();
    }

    public void createNew() {
        if(!exists()) {
            this.datapackRoot.mkdirs();
            File config = new File(rootDirectory.getAbsolutePath() + File.separator + TridentCompiler.PROJECT_FILE_NAME);
            try {
                config.createNewFile();
                updateConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getRelativePath(File file) {
        if(!file.getAbsolutePath().startsWith((rootDirectory.getAbsolutePath()+File.separator))) return null;
        return file.getAbsolutePath().substring((rootDirectory.getAbsolutePath()+File.separator).length());
    }

    public void updateClientDataCache(HashMap<String, ParsingSignature> resourceCache) {
        this.resourceCache = resourceCache;

        JsonObject jsonObj = new JsonObject();
        for(Map.Entry<String, ParsingSignature> entry : resourceCache.entrySet()) {
            jsonObj.addProperty(entry.getKey(), entry.getValue().getHashCode());
        }

        try {
            resourceCacheFile.getParentFile().mkdirs();
            resourceCacheFile.createNewFile();
        } catch(IOException x) {
            Debug.log(x.getMessage());
        }

        try(PrintWriter writer = new PrintWriter(resourceCacheFile, "UTF-8")) {
            writer.print(new GsonBuilder().setPrettyPrinting().create().toJson(jsonObj));
        } catch (IOException x) {
            Debug.log(x.getMessage());
        }
    }

    public void clearClientDataCache() {
        updateClientDataCache(new HashMap<>());
        Debug.log("Client Data Cache for project '" + getName() + "' cleared.");
    }

    public void updateServerDataCache(HashMap<String, ParsingSignature> sourceCache) {
        this.sourceCache = sourceCache;
    }

    public void updateSummary(ProjectSummary summary) {
        this.summary = (TridentProjectSummary) summary;
    }

    public TridentProjectSummary getSummary() {
        return summary;
    }

    public HashMap<String, ParsingSignature> getSourceCache() {
        return sourceCache;
    }

    public HashMap<String, ParsingSignature> getResourceCache() {
        return resourceCache;
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public File getServerDataRoot() {
        return datapackRoot;
    }

    public File getClientDataRoot() {
        return resourceRoot;
    }

    public String getName() {
        return name;
    }

    @Override
    public JavaEditionVersion getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(@Nullable JavaEditionVersion version) {
        targetVersion = version;
        if(version != null) {
            JsonArray versionArr = new JsonArray(3);
            versionArr.add(version.getMajor());
            versionArr.add(version.getMinor());
            versionArr.add(version.getPatch());
            config.add("target-version", versionArr);
        } else {
            config.remove("target-version");
        }
    }

    public int getLanguageLevel() {
        if(config.has("language-level") && config.get("language-level").isJsonPrimitive() && config.get("language-level").getAsJsonPrimitive().isNumber()) {
            int level = Math.max(1, Math.min(3, config.get("language-level").getAsInt()));
            config.addProperty("language-level", level);
            return level;
        }
        config.addProperty("language-level", 1);
        return 1;
    }

    public void setLanguageLevel(int level) {
        config.addProperty("language-level", Math.max(1, Math.min(3, level)));
    }

    public String getDefaultNamespace() {
        if(config.has("default-namespace") && config.get("default-namespace").isJsonPrimitive() && config.get("default-namespace").getAsJsonPrimitive().isString()) {
            String namespace = config.get("default-namespace").getAsString();
            if(namespace.matches(Namespace.ALLOWED_NAMESPACE_REGEX)) {
                return namespace;
            }
        }
        String namespace = StringUtil.getInitials(this.getName()).toLowerCase();
        config.addProperty("default-namespace", namespace);
        return namespace;
    }

    public void setDefaultNamespace(String namespace) {
        if(namespace.matches(Namespace.ALLOWED_NAMESPACE_REGEX)) {
            config.addProperty("default-namespace", namespace);
        }
    }

    public String getAnonymousFunctionName() {
        if(config.has("anonymous-function-name") && config.get("anonymous-function-name").isJsonPrimitive() && config.get("anonymous-function-name").getAsJsonPrimitive().isString()) {
            return config.get("anonymous-function-name").getAsString();
        }
        String defaultValue = "_anonymous*";
        config.addProperty("anonymous-function-name", defaultValue);
        return defaultValue;
    }

    public void setAnonymousFunctionName(String value) {
        config.addProperty("anonymous-function-name", value);
    }

    public boolean isStrictNBT() {
        if(config.has("strict-nbt") && config.get("strict-nbt").isJsonPrimitive() && config.get("strict-nbt").getAsJsonPrimitive().isBoolean()) {
            return config.get("strict-nbt").getAsBoolean();
        }
        config.addProperty("strict-nbt", false);
        return false;
    }

    public void setStrictNBT(boolean strict) {
        config.addProperty("strict-nbt", strict);
    }

    public boolean isStrictTextComponents() {
        if(config.has("strict-text-components") && config.get("strict-text-components").isJsonPrimitive() && config.get("strict-text-components").getAsJsonPrimitive().isBoolean()) {
            return config.get("strict-text-components").getAsBoolean();
        }
        config.addProperty("strict-text-components", false);
        return false;
    }

    public void setStrictTextComponents(boolean strict) {
        config.addProperty("strict-text-components", strict);
    }

    public File getDataOut() {
        if(config.has("datapack-output") && config.get("datapack-output").isJsonPrimitive() && config.get("datapack-output").getAsJsonPrimitive().isString()) {
            String path = config.get("datapack-output").getAsString();
            return TridentCompiler.newFileObject(path, rootDirectory);
        }
        config.remove("datapack-output");
        return null;
    }

    public void setDataOut(File file) {
        if(file != null) {
            config.addProperty("datapack-output", file.getPath());
        } else {
            config.remove("datapack-output");
        }
    }

    public File getResourcesOut() {
        if(config.has("resources-output") && config.get("resources-output").isJsonPrimitive() && config.get("resources-output").getAsJsonPrimitive().isString()) {
            String path = config.get("resources-output").getAsString();
            return TridentCompiler.newFileObject(path, rootDirectory);
        }
        config.remove("resources-output");
        return null;
    }

    public void setResourcesOut(File file) {
        if(file != null) {
            config.addProperty("resources-output", file.getPath());
        } else {
            config.remove("resources-output");
        }
    }

    public boolean isExportComments() {
        if(config.has("export-comments") && config.get("export-comments").isJsonPrimitive() && config.get("export-comments").getAsJsonPrimitive().isBoolean()) {
            return config.get("export-comments").getAsBoolean();
        }
        config.addProperty("export-comments", true);
        return true;
    }

    public void setExportComments(boolean strict) {
        config.addProperty("export-comments", strict);
    }

    public boolean isClearData() {
        if(config.has("clear-datapack-output") && config.get("clear-datapack-output").isJsonPrimitive() && config.get("clear-datapack-output").getAsJsonPrimitive().isBoolean()) {
            return config.get("clear-datapack-output").getAsBoolean();
        }
        config.addProperty("clear-datapack-output", false);
        return false;
    }

    public void setClearData(boolean clear) {
        config.addProperty("clear-datapack-output", clear);
    }

    public boolean isClearResources() {
        if(config.has("clear-resources-output") && config.get("clear-resources-output").isJsonPrimitive() && config.get("clear-resources-output").getAsJsonPrimitive().isBoolean()) {
            return config.get("clear-resources-output").getAsBoolean();
        }
        config.addProperty("clear-resources-output", false);
        return false;
    }

    public void setClearResources(boolean clear) {
        config.addProperty("clear-resources-output", clear);
    }

    public JsonObject getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return "Project [" + name + "]";
    }

    public void setName(String name) {
        this.name = name;
    }

    private boolean ensureBuildDataExists() {
        File buildDataFile = rootDirectory.toPath().resolve(TridentCompiler.PROJECT_BUILD_FILE_NAME).toFile();
        if(!buildDataFile.exists()) {
            String confirmation = new OptionDialog("New Project Format", "Project \"" + name + "\" is using an outdated settings format. Update it now?", new String[] {"Update this project", "Update all projects", "Not now"}).result;
            if(confirmation == null) return false;
            switch(confirmation) {
                case "Update this project": {
                    createBuildDataFromTDNProj();
                    break;
                }
                case "Update all projects": {
                    for(Project project : ProjectManager.getLoadedProjects()) {
                        if(project instanceof TridentProject) {
                            ((TridentProject) project).createBuildDataFromTDNProj();
                        }
                    }
                    break;
                }
                default: {
                    return false;
                }
            }
        }
        return true;
    }

    private void createBuildDataFromTDNProj() {
        File buildDataFile = rootDirectory.toPath().resolve(TridentCompiler.PROJECT_BUILD_FILE_NAME).toFile();

        JsonObject root = new JsonObject();

        root.addProperty("input-resources", "(automatically selected by Trident-UI)");

        JsonObject outputObj = new JsonObject();
        root.add("output", outputObj);

        JsonObject directoriesObj = new JsonObject();
        outputObj.add("directories", directoriesObj);

        JsonObject cleanDirectoriesObj = new JsonObject();
        outputObj.add("clean-directories", cleanDirectoriesObj);

        directoriesObj.add("data-pack", config.get("datapack-output"));
        directoriesObj.add("resource-pack", config.get("resources-output"));
        cleanDirectoriesObj.add("data-pack", config.get("clear-datapack-output"));
        cleanDirectoriesObj.add("resource-pack", config.get("clear-resources-output"));

        outputObj.add("export-comments", config.get("export-comments"));
        outputObj.add("export-gamelog", config.get("export-gamelog"));

        try(PrintWriter writer = new PrintWriter(buildDataFile, "UTF-8")) {
            writer.print(new GsonBuilder().setPrettyPrinting().create().toJson(root));
            Debug.log("Created " + TridentCompiler.PROJECT_BUILD_FILE_NAME + " for \"" + name + "\"");
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    public TridentBuildConfiguration getBuildConfig() {
        return buildConfig.getValue();
    }

    public ArrayList<String> getPreActions() {
        return preActions;
    }

    public ArrayList<String> getPostActions() {
        return postActions;
    }
}
