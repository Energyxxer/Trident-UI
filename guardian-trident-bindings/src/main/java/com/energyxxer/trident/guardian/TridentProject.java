package com.energyxxer.trident.guardian;

import com.energyxxer.commodore.module.Namespace;
import com.energyxxer.commodore.versioning.JavaEditionVersion;
import com.energyxxer.commodore.versioning.compatibility.VersionFeatureManager;
import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummarizer;
import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummary;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.global.temp.projects.BuildConfiguration;
import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.global.temp.projects.ProjectManager;
import com.energyxxer.guardian.langinterface.ProjectType;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.commodoreresources.DefinitionPacks;
import com.energyxxer.guardian.ui.commodoreresources.Plugins;
import com.energyxxer.guardian.ui.commodoreresources.TypeMaps;
import com.energyxxer.guardian.ui.dialogs.OptionDialog;
import com.energyxxer.guardian.ui.dialogs.build_configs.BuildConfigTab;
import com.energyxxer.guardian.ui.dialogs.build_configs.BuildConfigTabDisplayModuleEntry;
import com.energyxxer.guardian.ui.dialogs.build_configs.JsonProperty;
import com.energyxxer.guardian.ui.modules.FileModuleToken;
import com.energyxxer.nbtmapper.packs.NBTTypeMapPack;
import com.energyxxer.prismarine.PrismarineCompiler;
import com.energyxxer.prismarine.PrismarineProductions;
import com.energyxxer.prismarine.in.ProjectReader;
import com.energyxxer.prismarine.summaries.PrismarineProjectSummarizer;
import com.energyxxer.prismarine.util.JsonTraverser;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;
import com.energyxxer.prismarine.worker.tasks.SetupProductionsTask;
import com.energyxxer.trident.Trident;
import com.energyxxer.trident.TridentFileUnitConfiguration;
import com.energyxxer.trident.TridentSuiteConfiguration;
import com.energyxxer.trident.compiler.TridentBuildConfiguration;
import com.energyxxer.trident.compiler.lexer.summaries.TridentProjectSummary;
import com.energyxxer.trident.guardian.dialogs.project_properties.ProjectProperties;
import com.energyxxer.trident.worker.tasks.*;
import com.energyxxer.util.Lazy;
import com.energyxxer.util.StringUtil;
import com.energyxxer.util.logger.Debug;
import com.google.gson.*;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import static com.energyxxer.prismarine.PrismarineCompiler.newFileObject;

public class TridentProject implements Project<TridentBuildConfiguration> {
    public static final ProjectType PROJECT_TYPE = new ProjectType("TRIDENT", "Trident Project") {
        @Override
        public boolean isProjectRoot(File file) {
            return file.toPath().resolve(Trident.PROJECT_FILE_NAME).toFile().exists();
        }

        @Override
        public boolean isProjectIdentity(File file) {
            return isProjectRoot(file.getParentFile()) && (file.getName().equals(Trident.PROJECT_FILE_NAME) || file.getName().equals(Trident.PROJECT_BUILD_FILE_NAME));
        }

        @Override
        public Image getIconForRoot(File file) {
            return Commons.getIcon("project_tdn");
        }

        @Override
        public String getDefaultProjectIconName() {return "project_tdn";}

        @Override
        public Project createProjectFromRoot(File file) throws Exception {
            return new TridentProject(file);
        }

        @Override
        public Project createNew(Path rootPath) {
            return new TridentProject(rootPath).createNew();
        }

        @Override
        public void showProjectPropertiesDialog(Project project) {
            ProjectProperties.show((TridentProject) project);
        }
    };

    private File rootDirectory;
    public final long instantiationTime;
    private File datapackRoot;
    private File resourceRoot;
    private final File resourceCacheFile;
    private String name;

    private final Lazy<PrismarineProductions> productions = new Lazy<>(() -> {
        try {
            PrismarineProjectWorker worker = new PrismarineProjectWorker(TridentSuiteConfiguration.INSTANCE, rootDirectory);
            worker.output.put(SetupBuildConfigTask.INSTANCE, getTridentBuildConfig());
            worker.setup.addTasks(
                    SetupModuleTask.INSTANCE,
                    SetupDependenciesTask.INSTANCE,
                    SetupPluginsTask.INSTANCE,
                    SetupPluginsTransitivelyTask.INSTANCE,
                    SetupProductionsTask.INSTANCE,
                    SetupPluginProductionsTask.INSTANCE
            );
            worker.work();
            return worker.output.get(SetupProductionsTask.INSTANCE).get(TridentFileUnitConfiguration.INSTANCE);
        } catch(Exception x) {
            Debug.log("Exception while creating module: " + x.toString(), Debug.MessageType.ERROR);
            x.printStackTrace();
            GuardianWindow.showException(x);
            return null;
        }
    });

    private ArrayList<BuildConfiguration<TridentBuildConfiguration>> buildConfigs;

    @Override
    public TridentBuildConfiguration parseBuildConfig(JsonObject root, File file) throws IOException {
        TridentBuildConfiguration buildConfig = new TridentBuildConfiguration();
        buildConfig.defaultDefinitionPacks = DefinitionPacks.pickPacksForVersion(this.getTargetVersion());
        buildConfig.definitionPackAliases = DefinitionPacks.getAliasMap();
        buildConfig.featureMap = VersionFeatureManager.getFeaturesForVersion(this.getTargetVersion());
        buildConfig.pluginAliases = Plugins.getAliasMap(TridentSuiteConfiguration.INSTANCE);
        buildConfig.typeMapPacks = new NBTTypeMapPack[] {TypeMaps.pickTypeMapsForVersion(this.getTargetVersion())};

        StringBuilder errorSB = null;
        if(buildConfig.defaultDefinitionPacks == null) {
            errorSB = new StringBuilder();
            errorSB.append("\n  * Definition Packs");
        }
        if(buildConfig.featureMap == null) {
            if(errorSB == null) errorSB = new StringBuilder();
            errorSB.append("\n  * Feature Maps");
        }
        if(buildConfig.typeMapPacks[0] == null) {
            if(errorSB == null) errorSB = new StringBuilder();
            errorSB.append("\n  * Type Maps");
            buildConfig.typeMapPacks = new NBTTypeMapPack[0];
        }

        if(errorSB != null) {
            errorSB.insert(0, "The following resources couldn't be obtained for " + this.getTargetVersion() + ":");
            errorSB.append("\nCompilation may not work as intended.\n\nCheck for definition updates.");
            Debug.log(errorSB.toString(), Debug.MessageType.ERROR);
            GuardianWindow.showError(errorSB.toString());
        }

        buildConfig.populateFromJson(root, file);

        return buildConfig;
    }

    private JsonObject projectConfigJson;
    private JavaEditionVersion targetVersion = null;
    private ProjectReader compilerCache = null;
    private ProjectReader summaryCache = null;

    private TridentProjectSummary summary = null;

    //NEW PROJECT
    public TridentProject(Path rootPath) {
        instantiationTime = System.currentTimeMillis();
        this.rootDirectory = rootPath.toFile();

        datapackRoot = rootPath.resolve("datapack").toFile();
        resourceRoot = rootPath.resolve("resources").toFile();
        resourceCacheFile = rootDirectory.toPath().resolve(".tdnui").resolve("resource_cache").toFile();

        this.name = rootPath.getFileName().toString();
        //this.prefix = StringUtil.getInitials(name).toLowerCase(Locale.ENGLISH);

        this.buildConfigs = new ArrayList<>();

        Path outFolder = rootPath.resolve("out");

        JavaEditionVersion latestVersion = DefinitionPacks.getLatestKnownJavaVersion();
        if(latestVersion == null) latestVersion = new JavaEditionVersion(1, 13, 0);
        JsonArray latestVersionArr = new JsonArray(3);
        latestVersionArr.add(latestVersion.getMajor());
        latestVersionArr.add(latestVersion.getMinor());
        latestVersionArr.add(latestVersion.getPatch());

        targetVersion = latestVersion;

        projectConfigJson = new JsonObject();
        projectConfigJson.add("target-version", latestVersionArr);
        projectConfigJson.addProperty("default-namespace", StringUtil.getInitials(name).toLowerCase(Locale.ENGLISH));
        projectConfigJson.addProperty("language-level", 1);

        //these belong in .tdnbuild, will be updated and removed later
        projectConfigJson.addProperty("datapack-output", outFolder.resolve(name).toString());
        projectConfigJson.addProperty("resources-output", outFolder.resolve(name + "-resources.zip").toString());
        projectConfigJson.addProperty("clear-datapack-output", false);
        projectConfigJson.addProperty("clear-resources-output", false);
        projectConfigJson.addProperty("export-comments", true);
        projectConfigJson.addProperty("export-gamelog", true);

        projectConfigJson.addProperty("strict-nbt", false);
        projectConfigJson.addProperty("strict-text-components", false);
        projectConfigJson.addProperty("anonymous-function-name", "_anonymous*");
        projectConfigJson.addProperty("using-all-plugins", false);
        JsonObject loggerObj = new JsonObject();
        loggerObj.addProperty("compact", false);
        loggerObj.addProperty("timestamp-enabled", true);
        loggerObj.addProperty("line-number-enabled", false);
        loggerObj.addProperty("pos-enabled", true);
        projectConfigJson.add("game-logger", loggerObj);

        createBuildDataFromTDNProj();

        migrateLegacyBuildConfigFile();
    }

    //EXISTING PROJECT
    public TridentProject(File rootDirectory) throws Exception {
        instantiationTime = System.currentTimeMillis();
        this.rootDirectory = rootDirectory;

        datapackRoot = rootDirectory.toPath().resolve("datapack").toFile();
        resourceRoot = rootDirectory.toPath().resolve("resources").toFile();
        File projectConfigFile = new File(rootDirectory.getAbsolutePath() + File.separator + Trident.PROJECT_FILE_NAME);
        this.name = rootDirectory.getName();

        this.buildConfigs = listAllConfigs();

        resourceCacheFile = rootDirectory.toPath().resolve(".tdnui").resolve("resource_cache").toFile();
        if(resourceCacheFile.exists() && resourceCacheFile.isFile()) {
            try(InputStreamReader isr = new InputStreamReader(new FileInputStream(resourceCacheFile), Guardian.DEFAULT_CHARSET)) {
                JsonObject jsonObject = new Gson().fromJson(isr, JsonObject.class);
                if(jsonObject != null) {
                    compilerCache = new ProjectReader();
                    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        try {
                            Path relativePath = Paths.get(entry.getKey().replace('/',File.separatorChar));
                            int hashCode = entry.getValue().getAsInt();
                            compilerCache.putResultHash(relativePath, hashCode);
                        } catch (NumberFormatException | UnsupportedOperationException | InvalidPathException x) {
                            x.printStackTrace();
                        }
                    }
                }
            }
        }

        if(projectConfigFile.exists() && projectConfigFile.isFile()) {
            try(InputStreamReader isr = new InputStreamReader(new FileInputStream(projectConfigFile), Guardian.DEFAULT_CHARSET)) {
                try {
                    this.projectConfigJson = new Gson().fromJson(isr, JsonObject.class);
                } catch (JsonIOException | JsonSyntaxException x) {
                    GuardianWindow.showError("JSON Error in " + projectConfigFile + ": " + x.getMessage());
                    this.projectConfigJson = new JsonObject();
                }

                if(this.projectConfigJson.has("target-version") && this.projectConfigJson.get("target-version").isJsonArray()) {
                    JsonArray arr = this.projectConfigJson.getAsJsonArray("target-version");

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
            }

            migrateLegacyBuildConfigFile();

            return;
        }
        this.rootDirectory = null;
        throw new RuntimeException("Invalid configuration file.");
    }

    public TokenPatternMatch getFileStructure() {
        PrismarineProductions productions = this.productions.getValue();
        return productions != null ? productions.FILE : null;
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

    private void migrateLegacyBuildConfigFile() {
        getBuildDirectory().mkdirs();
        File legacyBuildConfigFile = rootDirectory.toPath().resolve(Trident.PROJECT_BUILD_FILE_NAME).toFile();
        if(legacyBuildConfigFile.exists() && legacyBuildConfigFile.isFile()) {
            try {
                Files.move(legacyBuildConfigFile.toPath(), getBuildDirectory().toPath().resolve(".build"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            refreshBuildConfigs();
        }
    }

    public void updateConfig() {
        this.datapackRoot.mkdirs();
        File projectConfigFile = new File(rootDirectory.getAbsolutePath() + File.separator + Trident.PROJECT_FILE_NAME);
        migrateLegacyBuildConfigFile();

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            try(PrintWriter writer = new PrintWriter(projectConfigFile, "UTF-8")) {
                writer.print(gson.toJson(this.projectConfigJson));
            } catch (FileNotFoundException | UnsupportedEncodingException x) {
                x.printStackTrace();
                GuardianWindow.showException(x);
            }

            if(!projectConfigFile.exists()) projectConfigFile.createNewFile();
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    private boolean exists() {
        return rootDirectory != null && rootDirectory.exists();
    }

    private TridentProject createNew() {
        Path defaultFunctionsDir = datapackRoot.toPath().resolve("data").resolve(getDefaultNamespace()).resolve("functions");

        defaultFunctionsDir.toFile().mkdirs();

        File mainFunctionPath = defaultFunctionsDir.resolve("main.tdn").toFile();
        try {
            mainFunctionPath.createNewFile();

            try(PrintWriter writer = new PrintWriter(mainFunctionPath, "UTF-8")) {
                writer.print("@ tag load\n\nsay Hello World!");
            } catch (IOException x) {
                Debug.log(x.getMessage());
                GuardianWindow.showException(x);
            }
        } catch (IOException x) {
            x.printStackTrace();
            GuardianWindow.showException(x);
        }

        GuardianWindow.tabManager.openTab(new FileModuleToken(mainFunctionPath));

        return this;
    }

    public String getRelativePath(File file) {
        if(!file.getAbsolutePath().startsWith((rootDirectory.getAbsolutePath()+File.separator))) return null;
        return file.getAbsolutePath().substring((rootDirectory.getAbsolutePath()+File.separator).length());
    }

    public void updateSummary(ProjectSummary summary) {
        this.summary = (TridentProjectSummary) summary;
    }

    public TridentProjectSummary getSummary() {
        return summary;
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
            projectConfigJson.add("target-version", versionArr);
        } else {
            projectConfigJson.remove("target-version");
        }
    }

    public int getLanguageLevel() {
        if(projectConfigJson.has("language-level") && projectConfigJson.get("language-level").isJsonPrimitive() && projectConfigJson.get("language-level").getAsJsonPrimitive().isNumber()) {
            int level = Math.max(1, Math.min(3, projectConfigJson.get("language-level").getAsInt()));
            projectConfigJson.addProperty("language-level", level);
            return level;
        }
        projectConfigJson.addProperty("language-level", 1);
        return 1;
    }

    public void setLanguageLevel(int level) {
        projectConfigJson.addProperty("language-level", Math.max(1, Math.min(3, level)));
    }

    public String getDefaultNamespace() {
        if(projectConfigJson.has("default-namespace") && projectConfigJson.get("default-namespace").isJsonPrimitive() && projectConfigJson.get("default-namespace").getAsJsonPrimitive().isString()) {
            String namespace = projectConfigJson.get("default-namespace").getAsString();
            if(namespace.matches(Namespace.ALLOWED_NAMESPACE_REGEX)) {
                return namespace;
            }
        }
        String namespace = StringUtil.getInitials(this.getName()).toLowerCase(Locale.ENGLISH);
        projectConfigJson.addProperty("default-namespace", namespace);
        return namespace;
    }

    public void setDefaultNamespace(String namespace) {
        if(namespace.matches(Namespace.ALLOWED_NAMESPACE_REGEX)) {
            projectConfigJson.addProperty("default-namespace", namespace);
        }
    }

    public String getAnonymousFunctionName() {
        if(projectConfigJson.has("anonymous-function-name") && projectConfigJson.get("anonymous-function-name").isJsonPrimitive() && projectConfigJson.get("anonymous-function-name").getAsJsonPrimitive().isString()) {
            return projectConfigJson.get("anonymous-function-name").getAsString();
        }
        String defaultValue = "_anonymous*";
        projectConfigJson.addProperty("anonymous-function-name", defaultValue);
        return defaultValue;
    }

    public void setAnonymousFunctionName(String value) {
        projectConfigJson.addProperty("anonymous-function-name", value);
    }

    public boolean isStrictNBT() {
        if(projectConfigJson.has("strict-nbt") && projectConfigJson.get("strict-nbt").isJsonPrimitive() && projectConfigJson.get("strict-nbt").getAsJsonPrimitive().isBoolean()) {
            return projectConfigJson.get("strict-nbt").getAsBoolean();
        }
        projectConfigJson.addProperty("strict-nbt", false);
        return false;
    }

    public void setStrictNBT(boolean strict) {
        projectConfigJson.addProperty("strict-nbt", strict);
    }

    public boolean isStrictTextComponents() {
        if(projectConfigJson.has("strict-text-components") && projectConfigJson.get("strict-text-components").isJsonPrimitive() && projectConfigJson.get("strict-text-components").getAsJsonPrimitive().isBoolean()) {
            return projectConfigJson.get("strict-text-components").getAsBoolean();
        }
        projectConfigJson.addProperty("strict-text-components", false);
        return false;
    }

    public void setStrictTextComponents(boolean strict) {
        projectConfigJson.addProperty("strict-text-components", strict);
    }

    public JsonObject getProjectConfigJson() {
        return projectConfigJson;
    }

    @Override
    public String toString() {
        return "Project [" + name + "]";
    }

    public void setName(String name) {
        this.name = name;
    }

    private boolean ensureBuildDataExists() {
        File buildDataFile = rootDirectory.toPath().resolve(Trident.PROJECT_BUILD_FILE_NAME).toFile();
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
        JsonObject buildConfigJson = new JsonObject();

        buildConfigJson.addProperty("input-resources", "(automatically selected by Trident-UI)");

        JsonObject outputObj = new JsonObject();
        buildConfigJson.add("output", outputObj);

        JsonObject directoriesObj = new JsonObject();
        outputObj.add("directories", directoriesObj);

        JsonObject cleanDirectoriesObj = new JsonObject();
        outputObj.add("clean-directories", cleanDirectoriesObj);

        directoriesObj.add("data-pack", projectConfigJson.get("datapack-output"));
        directoriesObj.add("resource-pack", projectConfigJson.get("resources-output"));
        cleanDirectoriesObj.add("data-pack", projectConfigJson.get("clear-datapack-output"));
        cleanDirectoriesObj.add("resource-pack", projectConfigJson.get("clear-resources-output"));

        outputObj.add("export-comments", projectConfigJson.get("export-comments"));
        outputObj.add("export-gamelog", projectConfigJson.get("export-gamelog"));

        JsonObject tridentUIObj = new JsonObject();
        buildConfigJson.add("trident-ui", tridentUIObj);

        JsonObject actionsObj = new JsonObject();
        tridentUIObj.add("actions", actionsObj);

        actionsObj.add("pre", new JsonArray());
        actionsObj.add("post", new JsonArray());

        projectConfigJson.remove("datapack-output");
        projectConfigJson.remove("resources-output");
        projectConfigJson.remove("export-comments");
        projectConfigJson.remove("export-gamelog");
        projectConfigJson.remove("clear-datapack-output");
        projectConfigJson.remove("clear-resources-output");

        updateConfig();
        Debug.log("Created " + Trident.PROJECT_BUILD_FILE_NAME + " for \"" + name + "\"");
    }

    public TridentBuildConfiguration getTridentBuildConfig() {
        return getBuildConfig().get(this);
    }

    @Override
    public ArrayList<BuildConfiguration<TridentBuildConfiguration>> getAllBuildConfigs() {
        return buildConfigs;
    }

    @Override
    public Iterable<? extends BuildConfigTab> getBuildConfigTabs() {
        ArrayList<BuildConfigTab> tabs = new ArrayList<>();
        {
            BuildConfigTab tab = new BuildConfigTab("Output");
            tab.addEntry(new BuildConfigTabDisplayModuleEntry.FileField<JsonTraverser>(
                            "Data Pack Output",
                            "Where to store the data pack compilation result.\nMay be a directory or a zip file.",
                            "Select Data Pack Output..."
                    )
                    .setProperty(new JsonProperty.JsonStringProperty(
                            "output.directories.data-pack",
                            getRootDirectory().toPath().resolve("out").resolve("datapack").toString()
                    ))
            );
            tab.addEntry(new BuildConfigTabDisplayModuleEntry.CheckboxField<JsonTraverser>(
                            "Delete data pack output before compiling",
                            null
                    )
                    .setProperty(new JsonProperty.JsonBooleanProperty(
                            "output.clean-directories.data-pack",
                            false
                    ))
            );
            tab.addEntry(new BuildConfigTabDisplayModuleEntry.Spacing(15));
            tab.addEntry(new BuildConfigTabDisplayModuleEntry.FileField<JsonTraverser>(
                            "Resource Pack Output",
                            "Where to store the resource pack compilation result, if applicable.\nMay be a directory or a zip file.",
                            "Select Resource Pack Output..."
                    )
                    .setProperty(new JsonProperty.JsonStringProperty(
                            "output.directories.resource-pack",
                            getRootDirectory().toPath().resolve("out").resolve("resources.zip").toString()
                    ))
            );
            tab.addEntry(
                    new BuildConfigTabDisplayModuleEntry.CheckboxField<JsonTraverser>(
                            "Delete resource pack output before compiling",
                            null
                    )
                    .setProperty(new JsonProperty.JsonBooleanProperty(
                            "output.clean-directories.resource-pack",
                            false
                    ))
            );
            tab.addEntry(new BuildConfigTabDisplayModuleEntry.Spacing(15));
            tab.addEntry(
                    new BuildConfigTabDisplayModuleEntry.CheckboxField<JsonTraverser>(
                            "Export comments",
                            "If enabled, comments in Trident functions will be exported."
                    )
                    .setProperty(new JsonProperty.JsonBooleanProperty(
                            "output.export-comments",
                            false
                    ))
            );
            tab.addEntry(new BuildConfigTabDisplayModuleEntry.Spacing(15));
            tab.addEntry(
                    new BuildConfigTabDisplayModuleEntry.CheckboxField<JsonTraverser>(
                            "Output Game Log Commands",
                            "If disabled, gamelog commands will not generate an output.\nThe gamelog command requires Language Level 3."
                    )
                    .setProperty(new JsonProperty.JsonBooleanProperty(
                            "output.export-gamelog",
                            false
                    ))
            );
            tabs.add(tab);
        }
        parseUserBuildConfigTabs(tabs, true);
        return tabs;
    }

    @Override
    public void refreshBuildConfigs() {
        buildConfigs.clear();
        buildConfigs = listAllConfigs();
    }

    private final BuildConfiguration<TridentBuildConfiguration> defaultBuildConfig = new BuildConfiguration<>();

    @Override
    public BuildConfiguration<TridentBuildConfiguration> getDefaultBuildConfig() {
        return defaultBuildConfig;
    }

    public ProjectType getProjectType() {
        return TridentProject.PROJECT_TYPE;
    }

    @Override
    public ProjectSummarizer createProjectSummarizer() {
        PrismarineProjectSummarizer summarizer = new PrismarineProjectSummarizer(
                TridentSuiteConfiguration.INSTANCE,
                this.getRootDirectory()
        );
        summarizer.getWorker().output.put(SetupBuildConfigTask.INSTANCE, this.getTridentBuildConfig());
        summarizer.getWorker().output.put(SetupPropertiesTask.INSTANCE, this.getProjectConfigJson());
        if(summaryCache != null) summaryCache.startCache();
        summarizer.setCachedReader(summaryCache);

        summarizer.addCompletionListener(() -> {
            if(summarizer.getWalker() != null) summaryCache = summarizer.getWalker().getReader();
        });

        return summarizer;
    }

    @Override
    public Image getIconForFile(File file) {
        if(file.isDirectory()) {
            if(PROJECT_TYPE.isProjectRoot(file.getParentFile())) {
                if(file.getName().equals("datapack")) return Commons.getIcon("data");
                if(file.getName().equals("resources")) return Commons.getIcon("resources");
            }
        } else {
            String extension = "";
            if(file.getName().lastIndexOf(".") >= 0) {
                extension = file.getName().substring(file.getName().lastIndexOf("."));
            }
            switch(extension) {
                case ".json": {
                    if(file.getName().equals("sounds.json"))
                        return Commons.getIcon("sound_config");
                    else if(file.getParentFile().getName().equals("blockstates"))
                        return Commons.getIcon("blockstate");
                    else if(file.getParentFile().getName().equals("lang"))
                        return Commons.getIcon("lang");
                    break;
                }
                case ".mcmeta":
                case Trident.PROJECT_FILE_NAME:
                case Trident.PROJECT_BUILD_FILE_NAME:
                    return Commons.getIcon("meta");
                case ".nbt":
                    return Commons.getIcon("structure");
            }
        }
        return null;
    }

    @Override
    public PrismarineCompiler createProjectCompiler() {
        BuildConfiguration<TridentBuildConfiguration> buildConfig = this.getBuildConfig();
        if(buildConfig.isFallback()) {
            throw new IllegalStateException("No Build Configuration selected");
        }
        PrismarineCompiler compiler = new PrismarineProjectWorker(TridentSuiteConfiguration.INSTANCE, this.getRootDirectory()).createCompiler();

        compiler.getWorker().output.put(SetupBuildConfigTask.INSTANCE, buildConfig.get(this));
        if(compilerCache != null) compilerCache.startCache();
        compiler.setCachedReader(compilerCache);

        compiler.addCompletionListener((p, success) -> {
            if(success) {
                this.updateCache(((PrismarineCompiler) p).getProjectReader());
            }
        });

        return compiler;
    }

    @Override
    public long getInstantiationTime() {
        return instantiationTime;
    }

    @Override
    public void clearPersistentCache() {
        compilerCache = null;
        Debug.log("Persistent cache for project '" + getName() + "' cleared.");
    }

    public ProjectReader getCompilerCache() {
        return compilerCache;
    }

    public void updateCache(ProjectReader projectReader) {
        compilerCache = projectReader;

        JsonObject jsonObj = new JsonObject();
        for(ProjectReader.Result results : compilerCache.getResults()) {
            jsonObj.addProperty(results.getRelativePath().toString().replace(File.separatorChar,'/'), results.getHashCode());
        }

        try {
            resourceCacheFile.getParentFile().mkdirs();
            resourceCacheFile.createNewFile();
        } catch(IOException x) {
            Debug.log(x.getMessage());
            GuardianWindow.showException(x);
        }

        try(PrintWriter writer = new PrintWriter(resourceCacheFile, "UTF-8")) {
            writer.print(new GsonBuilder().setPrettyPrinting().create().toJson(jsonObj));
        } catch (IOException x) {
            Debug.log(x.getMessage());
            GuardianWindow.showException(x);
        }
    }

    @Override
    public ArrayList<Project> getLoadedDependencies(ArrayList<Project> list, boolean recursively) {
        if(list.contains(this)) return list;
        if(projectConfigJson.has("dependencies") && projectConfigJson.get("dependencies").isJsonArray()) {
            JsonArray arr = projectConfigJson.getAsJsonArray("dependencies");
            if(arr.size() > 0) {
                for(JsonElement elem : arr) {
                    if(elem.isJsonObject()) {
                        JsonObject obj = elem.getAsJsonObject();

                        //region Load from dependency
                        if(obj.has("path") && obj.get("path").isJsonPrimitive() && obj.get("path").getAsJsonPrimitive().isString()) {

                            if(obj.has("mode") && obj.get("mode").isJsonPrimitive() && obj.get("mode").getAsJsonPrimitive().isString()) {
                                if(!obj.get("mode").getAsString().equals("combine")) continue;
                            }

                            String path = obj.get("path").getAsString();
                            File dependencyRoot = newFileObject(path, getRootDirectory());
                            if(ProjectManager.isLoadedProjectRoot(dependencyRoot)) {
                                Project dependencyProject = ProjectManager.getAssociatedProject(dependencyRoot);
                                list.add(dependencyProject);
                                if(recursively) {
                                    dependencyProject.getLoadedDependencies(list, true);
                                }
                            }
                        }
                        //endregion
                    }
                }
            }
        }
        return list;
    }
}
