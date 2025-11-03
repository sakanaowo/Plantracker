package com.example.tralalero.domain.model;

public class Workspace {
    private final String id;
    private final String name;
    private final String type; 
    private final String ownerId;
    private final boolean isOwner;  // NEW: Flag to indicate if current user is owner

    public Workspace(String id, String name, String type, String ownerId) {
        this(id, name, type, ownerId, false);
    }

    public Workspace(String id, String name, String type, String ownerId, boolean isOwner) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.ownerId = ownerId;
        this.isOwner = isOwner;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public boolean isPersonal() {
        return "PERSONAL".equalsIgnoreCase(type);
    }

    public boolean isTeam() {
        return "TEAM".equalsIgnoreCase(type);
    }

    public String getTypeDisplay() {
        return isPersonal() ? "Personal" : "Team";
    }

    @Override
    public String toString() {
        return "Workspace{id='" + id + "', name='" + name + "', type='" + type + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Workspace workspace = (Workspace) o;
        return id != null && id.equals(workspace.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
