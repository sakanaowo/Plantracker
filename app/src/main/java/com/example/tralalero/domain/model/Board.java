package com.example.tralalero.domain.model;

public class Board {
    private final String id;
    private final String projectId;
    private final String name;
    private final int order;

    public Board(String id, String projectId, String name, int order) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.order = order;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return "Board{id='" + id + "', name='" + name + "', order=" + order + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return id != null && id.equals(board.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
