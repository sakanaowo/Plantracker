package com.example.tralalero.data.remote.dto.label;

import com.google.gson.annotations.SerializedName;

public class AssignLabelDTO {
    @SerializedName("labelId")
    private String labelId;

    public AssignLabelDTO(String labelId) {
        this.labelId = labelId;
    }

    public String getLabelId() { return labelId; }
}
