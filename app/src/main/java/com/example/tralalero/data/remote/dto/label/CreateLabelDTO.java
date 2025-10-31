package com.example.tralalero.data.remote.dto.label;

import com.google.gson.annotations.SerializedName;

public class CreateLabelDTO {
    @SerializedName("name")
    private String name;

    @SerializedName("color")
    private String color;

    public CreateLabelDTO(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public String getName() { return name; }
    public String getColor() { return color; }
}
