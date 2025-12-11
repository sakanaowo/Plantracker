package com.example.tralalero.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for creating a new checklist item
 * POST /checklists/:id/items
 */
public class CreateChecklistItemDTO {
    @SerializedName("content")
    private String content;

    public CreateChecklistItemDTO() {
    }

    public CreateChecklistItemDTO(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
