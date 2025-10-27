package com.example.tralalero.domain.model;

import java.io.Serializable;

public class Label implements Serializable {
    private final String id;
    private final String projectId;  // Changed from workspaceId to projectId
    private final String name;
    private final String color; 

    public Label(String id, String projectId, String name, String color) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public String getProjectId() {  // Changed from getWorkspaceId to getProjectId
        return projectId;
    }

    // Keep getWorkspaceId for backwards compatibility (deprecated)
    @Deprecated
    public String getWorkspaceId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public boolean hasColor() {
        return color != null && color.matches("#[0-9A-Fa-f]{6}");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Label label = (Label) o;
        return id != null && id.equals(label.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
