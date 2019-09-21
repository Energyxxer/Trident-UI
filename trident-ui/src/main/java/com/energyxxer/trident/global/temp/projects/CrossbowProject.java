package com.energyxxer.trident.global.temp.projects;

import com.energyxxer.commodore.module.CommandModule;
import com.energyxxer.commodore.standard.StandardDefinitionPacks;
import com.energyxxer.crossbow.compiler.CrossbowCompiler;
import com.energyxxer.crossbow.compiler.lexer.CrossbowProductions;
import com.energyxxer.crossbow.compiler.util.CrossbowProjectSummary;
import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummary;
import com.energyxxer.enxlex.pattern_matching.ParsingSignature;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.LazyTokenPatternMatch;
import com.energyxxer.trident.global.Commons;
import com.energyxxer.util.Lazy;
import com.energyxxer.util.StringUtil;
import com.energyxxer.util.logger.Debug;
import com.google.gson.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CrossbowProject implements Project {

    private File rootDirectory;

    private File behaviorpackRoot;
    private File resourceRoot;
    private String name;

    public final Lazy<CommandModule> module = new Lazy<>(() -> {
        try {
            return CrossbowCompiler.createModuleForProject(getName(), rootDirectory, StandardDefinitionPacks.MINECRAFT_BEDROCK_LATEST_RELEASE);
        } catch(IOException x) {
            Debug.log("Exception while creating module: " + x.toString(), Debug.MessageType.ERROR);
        }
        return Commons.getDefaultModule();
    });

    private final Lazy<CrossbowProductions> productions = new Lazy<>(() -> new CrossbowProductions(module.getValue()));

    private JsonObject config;
    private HashMap<String, ParsingSignature> resourceCache = new HashMap<>();

    private HashMap<String, ParsingSignature> sourceCache = new HashMap<>();

    private CrossbowProjectSummary summary = null;

    public CrossbowProject(String name) {
        Path rootPath = Paths.get(ProjectManager.getWorkspaceDir()).resolve(name);
        this.rootDirectory = rootPath.toFile();

        behaviorpackRoot = rootPath.resolve("behaviors").toFile();
        resourceRoot = rootPath.resolve("resources").toFile();

        this.name = name;
        //this.prefix = StringUtil.getInitials(name).toLowerCase();

        Path outFolder = Paths.get(System.getProperty("user.home"), "Trident", "out");

        config = new JsonObject();
        config.addProperty("default-namespace", StringUtil.getInitials(name).toLowerCase());
        config.addProperty("language-level", 1);
        config.addProperty("behaviors-output", outFolder.resolve(name).toString());
        config.addProperty("resources-output", outFolder.resolve(name + "-resources.zip").toString());
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

        behaviorpackRoot = rootDirectory.toPath().resolve("behaviors").toFile();
        resourceRoot = rootDirectory.toPath().resolve("resources").toFile();
        File config = new File(rootDirectory.getAbsolutePath() + File.separator + CrossbowCompiler.PROJECT_FILE_NAME);
        this.name = rootDirectory.getName();

        File resourceCacheFile = rootDirectory.toPath().resolve(".tdnui").resolve("resource_cache").toFile();
        if(resourceCacheFile.exists() && resourceCacheFile.isFile()) {
            try {
                JsonObject jsonObject = new Gson().fromJson(new FileReader(resourceCacheFile), JsonObject.class);
                if(jsonObject != null) {
                    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        try {
                            resourceCache.put(entry.getKey(), new ParsingSignature(entry.getValue().getAsInt()));
                        } catch (NumberFormatException | UnsupportedOperationException x) {
                            x.printStackTrace();
                        }
                    }
                }
            } catch (FileNotFoundException | JsonParseException x) {
                x.printStackTrace();
            }
        }

        if(config.exists() && config.isFile()) {
            try {
                this.config = new Gson().fromJson(new FileReader(config), JsonObject.class);
                return;
            } catch (FileNotFoundException | JsonParseException x) {
                //I literally *just* checked if the file exists beforehand. Damn Java and its trust issues
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
            this.behaviorpackRoot.mkdirs();
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
        return behaviorpackRoot;
    }

    public File getClientDataRoot() {
        return resourceRoot;
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
