package com.energyxxer.trident.main.window.sections.tools.find;

public interface FindExplorerFilter {
    boolean groupByProject();
    boolean groupBySubProject();
    boolean groupByPath();
    boolean groupByFile();

    default boolean highlightResult() {
        return true;
    }
}
