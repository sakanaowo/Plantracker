package com.example.tralalero.data.remote.dto.label;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for assigning a label to a task
 * Matches backend AssignLabelDto from assign-label.dto.ts
 */
public class AssignLabelRequest {
    @SerializedName("labelId")
    private String labelId;

    public AssignLabelRequest(String labelId) {
        this.labelId = labelId;
    }

    // Getters and Setters
    public String getLabelId() {
        return labelId;
    }

    public void setLabelId(String labelId) {
        this.labelId = labelId;
    }
}
