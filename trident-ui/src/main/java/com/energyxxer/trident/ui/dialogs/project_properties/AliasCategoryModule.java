package com.energyxxer.trident.ui.dialogs.project_properties;

import com.energyxxer.commodore.functionlogic.functions.Function;
import com.energyxxer.commodore.module.Namespace;
import com.energyxxer.trident.global.temp.Lang;
import com.energyxxer.trident.ui.display.DisplayModule;
import com.energyxxer.trident.ui.editor.TridentEditorModule;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.util.Disposable;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.regex.Pattern;

public class AliasCategoryModule extends JPanel implements DisplayModule, Disposable {
    private String category;
    private JsonObject config;
    private TridentEditorModule editorModule;

    private static final Pattern RESOURCE_LOCATION_REGEX = Pattern.compile("(" + Namespace.ALLOWED_NAMESPACE_REGEX + ":)?" + Function.ALLOWED_PATH_REGEX);

    public AliasCategoryModule(String category, JsonObject config) {
        super(new BorderLayout());
        this.config = config;
        this.category = category;
        this.editorModule = new TridentEditorModule(null, null);
        editorModule.setForcedLanguage(Lang.PROPERTIES);
        editorModule.updateSyntax();

        StringBuilder editorText = new StringBuilder();

        if(config.has("aliases") && config.get("aliases").isJsonObject()) {
            JsonObject aliases = config.getAsJsonObject("aliases");

            if(aliases.has(category) && aliases.get(category).isJsonObject()) {
                for(Map.Entry<String, JsonElement> entry : aliases.getAsJsonObject(category).entrySet()) {
                    if(entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString()) {
                        editorText.append(entry.getKey());
                        editorText.append(" = ");
                        editorText.append(entry.getValue().getAsString());
                        editorText.append('\n');
                    }
                }
            }
        }
        editorModule.setText(editorText.toString());

        //this.scrollPane = new OverlayScrollPane(editor);
        //this.add(this.scrollPane, BorderLayout.CENTER);
        this.add(editorModule);
    }

    @Override
    public void displayCaretInfo() {

    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public Object save() {
        JsonObject catObj = new JsonObject();
        boolean any = false;
        for(String line : editorModule.getText().split("\n")) {
            if(line.startsWith("#")) continue;
            if(!line.contains("=")) continue;
            String[] parts = line.split("=");
            if(parts.length == 2) {
                String left = parts[0].trim();
                String right = parts[1].trim();

                /*if(RESOURCE_LOCATION_REGEX.matcher(left).matches() && RESOURCE_LOCATION_REGEX.matcher(right).matches()) {
                    if(!left.contains(":")) left = "minecraft:" + left;
                    if(!right.contains(":")) right = "minecraft:" + right;
                }*/
                catObj.addProperty(left, right);
                any = true;
            }
        }
        if(any) {
            if (!config.has("aliases") || !config.get("aliases").isJsonObject()) {
                config.add("aliases", new JsonObject());
            }

            JsonObject aliases = config.getAsJsonObject("aliases");
            aliases.add(category, catObj);
        } else if(config.has("aliases") && config.get("aliases").isJsonObject()) {
            config.getAsJsonObject("aliases").remove(category);
        }
        return null;
    }

    @Override
    public void focus() {

    }

    @Override
    public boolean transform(ModuleToken newToken) {
        return false;
    }

    @Override
    public void dispose() {
        editorModule.dispose();
    }
}
