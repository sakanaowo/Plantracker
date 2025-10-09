package com.example.tralalero.domain.model;

public class Project {
    private final String id;
    private final String workspaceId;
    private final String name;
    private final String description;
    private final String key; // VD: "PLAN"
    private final String boardType; // KANBAN or SCRUM/ default is KANBAN

    public Project(String id, String workspaceId, String name, String description,
                   String key, String boardType) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.name = name;
        this.description = description;
        this.key = key;
        this.boardType = boardType;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getKey() {
        return key;
    }

    public String getBoardType() {
        return boardType;
    }

    // Business logic
    public boolean isKanban() {
        return "KANBAN".equalsIgnoreCase(boardType);
    }

    public boolean isScrum() {
        return "SCRUM".equalsIgnoreCase(boardType);
    }

    public String getBoardTypeDisplay() {
        return isKanban() ? "Kanban" : "Scrum";
    }

    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "Project{id='" + id + "', name='" + name + "', key='" + key + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return id != null && id.equals(project.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
