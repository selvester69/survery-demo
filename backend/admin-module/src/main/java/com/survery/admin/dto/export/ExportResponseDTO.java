package com.survery.admin.dto.export;

import java.util.UUID;

public class ExportResponseDTO {

    private UUID jobId;
    private String message;

    public ExportResponseDTO(UUID jobId, String message) {
        this.jobId = jobId;
        this.message = message;
    }

    //<editor-fold desc="Getters and Setters">
    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    //</editor-fold>
}
