package com.example.tralalero.domain.model;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;


public class Checklist {
    private final String id;
    private final String taskId;
    private final String title;
    private final Date createdAt;
    private final List<ChecklistItem> items; 

    public Checklist(String id, String taskId, String title, Date createdAt) {
        this(id, taskId, title, createdAt, new ArrayList<>());
    }

    public Checklist(String id, String taskId, String title, Date createdAt, List<ChecklistItem> items) {
        this.id = id;
        this.taskId = taskId;
        this.title = title;
        this.createdAt = createdAt;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    public String getId() { return id; }
    public String getTaskId() { return taskId; }
    public String getTitle() { return title; }
    public Date getCreatedAt() { return createdAt; }
    public List<ChecklistItem> getItems() { return new ArrayList<>(items); }

    public int getTotalItems() {
        return items.size();
    }

    public int getCompletedItems() {
        int count = 0;
        for (ChecklistItem item : items) {
            if (item.isDone()) count++;
        }
        return count;
    }

    public int getProgressPercentage() {
        if (items.isEmpty()) return 0;
        return (getCompletedItems() * 100) / getTotalItems();
    }

    public boolean isCompleted() {
        if (items.isEmpty()) return false;
        return getCompletedItems() == getTotalItems();
    }

    public String getProgressText() {
        return getCompletedItems() + "/" + getTotalItems();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Checklist checklist = (Checklist) o;
        return id != null && id.equals(checklist.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
