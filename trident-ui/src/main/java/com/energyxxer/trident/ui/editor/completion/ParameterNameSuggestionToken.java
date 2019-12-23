package com.energyxxer.trident.ui.editor.completion;

public class ParameterNameSuggestionToken implements SuggestionToken {
    private final String parameterName;

    public ParameterNameSuggestionToken(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }
}
