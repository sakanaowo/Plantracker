package com.example.tralalero.data.dto;

public class TaskStatisticsDTO {
    private int doneCount;
    private int updatedCount;
    private int createdCount;
    private int dueCount;
    private int totalCount;
    private int todoCount;
    private int inProgressCount;
    
    // Constructors
    public TaskStatisticsDTO() {}
    
    public TaskStatisticsDTO(int doneCount, int updatedCount, int createdCount, int dueCount,
                            int totalCount, int todoCount, int inProgressCount) {
        this.doneCount = doneCount;
        this.updatedCount = updatedCount;
        this.createdCount = createdCount;
        this.dueCount = dueCount;
        this.totalCount = totalCount;
        this.todoCount = todoCount;
        this.inProgressCount = inProgressCount;
    }
    
    // Getters and Setters
    public int getDoneCount() {
        return doneCount;
    }
    
    public void setDoneCount(int doneCount) {
        this.doneCount = doneCount;
    }
    
    public int getUpdatedCount() {
        return updatedCount;
    }
    
    public void setUpdatedCount(int updatedCount) {
        this.updatedCount = updatedCount;
    }
    
    public int getCreatedCount() {
        return createdCount;
    }
    
    public void setCreatedCount(int createdCount) {
        this.createdCount = createdCount;
    }
    
    public int getDueCount() {
        return dueCount;
    }
    
    public void setDueCount(int dueCount) {
        this.dueCount = dueCount;
    }
    
    public int getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    public int getTodoCount() {
        return todoCount;
    }
    
    public void setTodoCount(int todoCount) {
        this.todoCount = todoCount;
    }
    
    public int getInProgressCount() {
        return inProgressCount;
    }
    
    public void setInProgressCount(int inProgressCount) {
        this.inProgressCount = inProgressCount;
    }
}
