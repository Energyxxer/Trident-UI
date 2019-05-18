package com.energyxxer.trident.testing;

import com.energyxxer.util.logger.Debug;

import java.util.ArrayList;

public class LiveTestCase {
    private final String code;
    private final Class associatedClass;
    private final String title;
    private final String description;

    private ArrayList<LiveTestResult> results = new ArrayList<>();

    public LiveTestCase(String code, Class associatedClass, String title, String description) {
        this.code = code;
        this.associatedClass = associatedClass;
        this.title = title;
        this.description = description;
    }

    public void submitResult(LiveTestResult result) {
        results.add(result);
        Debug.log("Result for Live Test Case code '" + code + "' created: " + result);
    }

    public String getCode() {
        return code;
    }

    public Class getAssociatedClass() {
        return associatedClass;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
