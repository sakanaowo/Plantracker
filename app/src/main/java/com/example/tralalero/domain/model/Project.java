package com.example.tralalero.domain.model;

public class Project {
    private final String id;
    private final String workspaceId;
    private final String name;
    private final String description;
    private final String key;
    private final String boardType;
    private final String type;  // PERSONAL or TEAM
    private String workspaceName; // Name of the workspace (for display)

    public Project(String id, String workspaceId, String name, String description,
                   String key, String boardType, String type) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.name = name;
        this.description = description;
        this.key = key;
        this.boardType = boardType;
        this.type = type;
    }
    
    public Project(String id, String workspaceId, String name, String description,
                   String key, String boardType, String type, String workspaceName) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.name = name;
        this.description = description;
        this.key = key;
        this.boardType = boardType;
        this.type = type;
        this.workspaceName = workspaceName;
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

    public String getDescription() {
        return description;
    }

    public String getKey() {
        return key;
    }

    public String getBoardType() {
        return boardType;
    }

    public String getType() {
        return type;
    }

    public boolean isTeamProject() {
        return "TEAM".equalsIgnoreCase(type);
    }

    public boolean isPersonalProject() {
        return "PERSONAL".equalsIgnoreCase(type);
    }
    
    public String getWorkspaceName() {
        return workspaceName;
    }
    
    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

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
