package com.example.tralalero.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for updating checklist item content
 * PATCH /checklist-items/:id
 */
public class UpdateChecklistItemDTO {
    @SerializedName("content")
    private String content;

    public UpdateChecklistItemDTO() {
    }

    public UpdateChecklistItemDTO(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
