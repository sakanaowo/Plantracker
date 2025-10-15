package com.example.tralalero.domain.model;

public class Label {
    private final String id;
    private final String workspaceId;
    private final String name;
    private final String color; 

    public Label(String id, String workspaceId, String name, String color) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.name = name;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public String getWorkspaceId() {
        return workspaceId;
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
