package com.example.tralalero.data.remote.dto.label;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for updating a label
 * Matches backend UpdateLabelDto from update-label.dto.ts
 * All fields are optional
 */
public class UpdateLabelRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("color")
    private String color;

    public UpdateLabelRequest() {
    }

    public UpdateLabelRequest(String name, String color) {
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
