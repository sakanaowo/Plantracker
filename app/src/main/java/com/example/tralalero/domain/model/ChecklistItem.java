package com.example.tralalero.domain.model;

import java.util.Date;


public class ChecklistItem {
    private final String id;
    private final String checklistId;
    private final String content;
    private final boolean isDone;
    private final double position; 
    private final Date createdAt;

    public ChecklistItem(String id, String checklistId, String content,
                         boolean isDone, double position, Date createdAt) {
        this.id = id;
        this.checklistId = checklistId;
        this.content = content;
        this.isDone = isDone;
        this.position = position;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getChecklistId() { return checklistId; }
    public String getContent() { return content; }
    public boolean isDone() { return isDone; }
    public double getPosition() { return position; }
    public Date getCreatedAt() { return createdAt; }

    public boolean isEmpty() {
        return content == null || content.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChecklistItem that = (ChecklistItem) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
