package com.v1.evaluation.model;

public enum EvaluationType {
    PRELIMINARY("Preliminary Examination"),
    TECHNICAL("Technical Evaluation"),
    FINANCIAL("Financial Evaluation"),
    FINAL("Final Evaluation");

    private final String displayName;

    EvaluationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
