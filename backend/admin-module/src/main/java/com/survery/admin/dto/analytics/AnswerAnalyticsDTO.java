package com.survery.admin.dto.analytics;

public class AnswerAnalyticsDTO {

    private String optionLabel;
    private long responseCount;
    private double percentage;

    //<editor-fold desc="Getters and Setters">
    public String getOptionLabel() {
        return optionLabel;
    }

    public void setOptionLabel(String optionLabel) {
        this.optionLabel = optionLabel;
    }

    public long getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(long responseCount) {
        this.responseCount = responseCount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
    //</editor-fold>
}
