package com.energyxxer.trident.testing;

import java.util.HashMap;

public class LiveTestManager {
    private static HashMap<String, LiveTestCase> testCases = new HashMap<>();

    public static void registerTestCase(LiveTestCase testCase) {
        testCases.put(testCase.getCode(), testCase);
    }

    public static LiveTestCase getTestCase(String code) {
        return testCases.get(code);
    }
}
