package com.example.tralalero.data.remote.dto.label;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for creating a new label
 * Matches backend CreateLabelDto from create-label.dto.ts
 * 
 * Validation rules (backend):
 * - name: required, max 50 characters
 * - color: required, hex format #RRGGBB
 */
public class CreateLabelRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("color")
    private String color;

    public CreateLabelRequest(String name, String color) {
        this.name = name;
        this.color = color;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
