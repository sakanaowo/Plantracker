package com.example.tralalero.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for creating a new checklist
 * Matches backend CreateChecklistDto
 */
public class CreateChecklistDTO {
    @SerializedName("title")
    private String title;

    public CreateChecklistDTO(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
