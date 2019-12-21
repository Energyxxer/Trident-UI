package com.energyxxer.trident.global.temp.projects;

import com.energyxxer.commodore.versioning.BedrockEditionVersion;
import com.energyxxer.crossbow.compiler.CrossbowCompiler;
import com.energyxxer.crossbow.compiler.lexer.CrossbowProductions;
import com.energyxxer.crossbow.compiler.out.BedrockModule;
import com.energyxxer.crossbow.compiler.util.CrossbowProjectSummary;
import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummary;
import com.energyxxer.enxlex.pattern_matching.ParsingSignature;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.LazyTokenPatternMatch;
import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.ui.commodoreresources.DefinitionPacks;
import com.energyxxer.util.Lazy;
import com.energyxxer.util.StringUtil;
import com.energyxxer.util.logger.Debug;
import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CrossbowProject implements Project {

    private File rootDirectory;

    private String name;

    public final Lazy<BedrockModule> module = new Lazy<>(() -> {
        try {
            return CrossbowCompiler.createModuleForProject(getName(), rootDirectory, DefinitionPacks.pickPacksForVersion(getTargetVersion()), DefinitionPacks.getAliasMap());
        } catch(IOException x) {
            Debug.log("Exception while creating module: " + x.toString(), Debug.MessageType.ERROR);
        }
        return Commons.getDefaultBedrockModule();
    });

    private final Lazy<CrossbowProductions> productions = new Lazy<>(() -> new CrossbowProductions(module.getValue()));

    private JsonObject config;
    private HashMap<String, ParsingSignature> resourceCache = new HashMap<>();

    private HashMap<String, ParsingSignature> sourceCache = new HashMap<>();

    private CrossbowProjectSummary summary = null;

    public CrossbowProject(String name) {
        Path rootPath = Paths.get(ProjectManager.getWorkspaceDir()).resolve(name);
        this.rootDirectory = rootPath.toFile();

        this.name = name;
        //this.prefix = StringUtil.getInitials(name).toLowerCase();

        Path outFolder = Paths.get(System.getProperty("user.home"), "Trident", "out", name);

        config = new JsonObject();
        config.addProperty("default-namespace", StringUtil.getInitials(name).toLowerCase());
        config.addProperty("world-output", outFolder.resolve(name).toString());
        config.addProperty("export-comments", true);
        config.addProperty("strict-text-components", false);
        JsonObject loggerObj = new JsonObject();
        loggerObj.addProperty("compact", false);
        loggerObj.addProperty("timestamp-enabled", true);
        loggerObj.addProperty("line-number-enabled", false);
        loggerObj.addProperty("pos-enabled", true);
        config.add("game-logger", loggerObj);
    }

    public CrossbowProject(File rootDirectory) {
        this.rootDirectory = rootDirectory;

        File config = new File(rootDirectory.getAbsolutePath() + File.separator + CrossbowCompiler.PROJECT_FILE_NAME);
        this.name = rootDirectory.getName();

        File resourceCacheFile = rootDirectory.toPath().resolve(".tdnui").resolve("resource_cache").toFile();
        if(resourceCacheFile.exists() && resourceCacheFile.isFile()) {
            try(FileReader fr = new FileReader(resourceCacheFile)) {
                JsonObject jsonObject = new Gson().fromJson(fr, JsonObject.class);
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
            try(FileReader fr = new FileReader(config)) {
                this.config = new Gson().fromJson(fr, JsonObject.class);
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
        return !Arrays.asList("resources","behaviors").contains(file.getName());
    }

    public void updateConfig() {
        File config = new File(rootDirectory.getAbsolutePath() + File.separator + CrossbowCompiler.PROJECT_FILE_NAME);
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
            this.rootDirectory.mkdirs();
            File config = new File(rootDirectory.getAbsolutePath() + File.separator + CrossbowCompiler.PROJECT_FILE_NAME);
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
        File cache = rootDirectory.toPath().resolve(".tdnui").resolve("resource_cache").toFile();

        JsonObject jsonObj = new JsonObject();
        for(Map.Entry<String, ParsingSignature> entry : resourceCache.entrySet()) {
            jsonObj.addProperty(entry.getKey(), entry.getValue().getHashCode());
        }

        try {
            cache.getParentFile().mkdirs();
            cache.createNewFile();
        } catch(IOException x) {
            Debug.log(x.getMessage());
        }

        try(PrintWriter writer = new PrintWriter(cache, "UTF-8")) {
            writer.print(new GsonBuilder().setPrettyPrinting().create().toJson(jsonObj));
        } catch (IOException x) {
            Debug.log(x.getMessage());
        }
    }

    @Override
    public BedrockEditionVersion getTargetVersion() {
        return new BedrockEditionVersion(1, 13, 0);
    }

    public void updateServerDataCache(HashMap<String, ParsingSignature> sourceCache) {
        this.sourceCache = sourceCache;
    }

    public void updateSummary(ProjectSummary summary) {
        this.summary = (CrossbowProjectSummary) summary;
    }

    public CrossbowProjectSummary getSummary() {
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
        return rootDirectory.toPath().resolve("behavior_packs").toFile();
    }

    public File getClientDataRoot() {
        return rootDirectory.toPath().resolve("resource_packs").toFile();
    }

    public String getName() {
        return name;
    }

    @Deprecated
    public String getPrefix() {
        return "";
    }

    @Deprecated
    public String getWorld() {
        return "";
    }

    @Override
    public String toString() {
        return "Project [" + name + "]";
    }

    public void setName(String name) {
        this.name = name;
    }
}
