package com.energyxxer.trident.ui.editor.completion.paths;

import com.energyxxer.enxlex.suggestions.Suggestion;
import com.energyxxer.trident.ui.editor.completion.SuggestionDialog;
import com.energyxxer.trident.ui.editor.completion.SuggestionToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.energyxxer.trident.ui.editor.completion.paths.ResourcePathExpander.DELIMITER_REGEX;

public class ResourcePathNode extends SuggestionToken {
    String parentPath;
    String name;
    ArrayList<ResourcePathNode> subPaths = new ArrayList<>();
    private HashMap<String, ResourcePathNode> tempNameMap = new HashMap<>();
    ArrayList<String> pathParts = new ArrayList<>();

    public ResourcePathNode() {
    }

    public ResourcePathNode(String parentPath, String name) {
        this.parentPath = parentPath;
        this.name = name;
    }

    public void insert(String rawPath) {
        String[] parts = rawPath.split(DELIMITER_REGEX, 2);
        String nextNodeName = parts[0];
        if(!tempNameMap.containsKey(nextNodeName)) {
            ResourcePathNode node = new ResourcePathNode(getFullPath(), nextNodeName);
            subPaths.add(node);
            tempNameMap.put(nextNodeName, node);
        }
        if(parts.length > 1) {
            tempNameMap.get(nextNodeName).insert(parts[1]);
        }
    }

    public String getFullPath() {
        if(name == null) return null;
        if(parentPath == null) return name;
        return parentPath + name;
    }

    @Override
    public String toString() {
        if(subPaths.isEmpty()) return name;
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if(!subPaths.isEmpty()) {
            sb.append('\n');
        }
        for(ResourcePathNode subNode : subPaths) {
            String subString = subNode.toString();
            for(String line : subString.split("\n")) {
                sb.append("    ");
                sb.append(line);
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    public void flattenAll() {
        ArrayList<ResourcePathNode> newList = new ArrayList<>();
        flattenAndCollectLeaves(newList);
        this.subPaths = newList;
    }

    private void flattenAndCollectLeaves(ArrayList<ResourcePathNode> list) {
        tempNameMap = null;
        if(this.subPaths.isEmpty()) {
            this.name = this.parentPath + this.name;
            this.parentPath = null;
            this.pathParts.clear();
            this.pathParts.add(name);
            list.add(this);
        } else {
            for(ResourcePathNode subNode : subPaths) {
                subNode.flattenAndCollectLeaves(list);
            }
        }
    }

    public void flattenEmptyMiddleNodes() {
        tempNameMap = null;
        for(ResourcePathNode subNode : subPaths) {
            subNode.flattenEmptyMiddleNodes();
        }
        if(this.name != null && subPaths.size() == 1) {
            ResourcePathNode next = subPaths.get(0);
            this.name += next.name;
            this.subPaths = next.subPaths;
        }
    }

    public void updatePathParts() {
        if(this.name != null) {
            this.pathParts.add(this.name);
            for(ResourcePathNode subNode : subPaths) {
                subNode.pathParts.addAll(0, this.pathParts);
            }
        }
        for(ResourcePathNode subNode : subPaths) {
            subNode.updatePathParts();
        }
    }

    public void collectSuggestionTokens(ArrayList<ResourcePathNode> list, SuggestionDialog parent, Suggestion suggestion) {
        this.parent = parent;
        this.text = parentPath != null ? parentPath + name : name;
        this.preview = this.text;
        this.suggestion = suggestion;

        this.setIconKey(!subPaths.isEmpty() ? (parentPath == null ? "package" : "folder") : "function");

        if(this.name != null) {
            list.add(this);
        }
        for(ResourcePathNode subNode : subPaths) {
            subNode.collectSuggestionTokens(list, parent, suggestion);
        }
    }

    private boolean skipNamespaces = false;

    public boolean isSkipNamespaces() {
        return skipNamespaces;
    }

    public void setSkipNamespaces(boolean skipNamespaces) {
        this.skipNamespaces = skipNamespaces;

        this.pathParts = new ArrayList<>(Arrays.asList(this.pathParts.get(0).split("(?<=[:])",2)));
    }

    @Override
    public void onInteract() {
        parent.submit(this.text, suggestion, subPaths.isEmpty());
    }

    @Override
    public void setEnabledFilter(String filter) {
        if(filter.isEmpty()) {
            this.enabled = parentPath == null;
        } else {
            this.enabled = true;
            int filterIndex = 0;
            for(int i = 0; i < pathParts.size(); i++) {
                String part = this.pathParts.get(i);
                if(i < pathParts.size()-1) {
                    if(!filter.startsWith(part, filterIndex) && !(i == 0 && skipNamespaces && part.startsWith(filter.substring(filterIndex)))) {
                        if(!(i == 0 && skipNamespaces)) {
                            enabled = false;
                            break;
                        }
                    } else {
                        filterIndex += part.length();
                    }
                } else {
                    if(!(skipNamespaces && filterIndex >= filter.length()) && (!part.startsWith(filter.substring(filterIndex)) || (!isLeaf() && part.equals(filter.substring(filterIndex))))) {
                        enabled = false;
                        break;
                    }
                    filterIndex += part.length();
                }
            }
        }
    }

    public boolean isLeaf() {
        return subPaths.isEmpty();
    }
}
