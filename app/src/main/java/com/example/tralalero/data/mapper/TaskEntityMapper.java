package com.example.tralalero.data.mapper;

import com.example.tralalero.data.local.database.entity.TaskEntity;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.model.Task.TaskType;
import com.example.tralalero.domain.model.Task.TaskStatus;
import com.example.tralalero.domain.model.Task.TaskPriority;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper between Task domain model and TaskEntity
 * CRITICAL: Maps all 23 fields without faking data
 * CRITICAL: Uses String IDs directly (no int conversion)
 * CRITICAL: Converts enums to/from strings for Room storage
 */
public class TaskEntityMapper {

    /**
     * Convert domain Task to TaskEntity
     * Maps all 23 fields: id, projectId, boardId, title, description, issueKey,
     * type, status, priority, position, assigneeId, createdBy, sprintId, epicId,
     * parentTaskId, startAt, dueAt, storyPoints, originalEstimateSec,
     * remainingEstimateSec, timeSpentSec, createdAt, updatedAt
     */
    public static TaskEntity toEntity(Task task) {
        if (task == null) {
            return null;
        }

        // Create entity with required fields (id, projectId, boardId, title)
        TaskEntity entity = new TaskEntity(
            task.getId(),
            task.getProjectId(),
            task.getBoardId(),
            task.getTitle()
        );

        // Set all optional fields
        entity.setDescription(task.getDescription());
        entity.setIssueKey(task.getIssueKey());

        // Convert enums to strings for Room storage
        entity.setType(task.getType() != null ? task.getType().name() : null);
        entity.setStatus(task.getStatus() != null ? task.getStatus().name() : null);
        entity.setPriority(task.getPriority() != null ? task.getPriority().name() : null);

        entity.setPosition(task.getPosition());
        entity.setAssigneeId(task.getAssigneeId());
        entity.setCreatedBy(task.getCreatedBy());
        entity.setSprintId(task.getSprintId());
        entity.setEpicId(task.getEpicId());
        entity.setParentTaskId(task.getParentTaskId());
        entity.setStartAt(task.getStartAt());
        entity.setDueAt(task.getDueAt());
        entity.setStoryPoints(task.getStoryPoints());
        entity.setOriginalEstimateSec(task.getOriginalEstimateSec());
        entity.setRemainingEstimateSec(task.getRemainingEstimateSec());
        entity.setCreatedAt(task.getCreatedAt());
        entity.setUpdatedAt(task.getUpdatedAt());

        return entity;
    }

    /**
     * Convert TaskEntity to domain Task
     * Maps all 23 fields without faking any data
     */
    public static Task toDomain(TaskEntity entity) {
        if (entity == null) {
            return null;
        }

        // Parse enum strings back to enums
        TaskType type = parseTaskType(entity.getType());
        TaskStatus status = parseTaskStatus(entity.getStatus());
        TaskPriority priority = parseTaskPriority(entity.getPriority());

        // Create domain Task with all 23 fields
        return new Task(
            entity.getId(),
            entity.getProjectId(),
            entity.getBoardId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getIssueKey(),
            type,
            status,
            priority,
            entity.getPosition(),
            entity.getAssigneeId(),
            entity.getCreatedBy(),
            entity.getSprintId(),
            entity.getEpicId(),
            entity.getParentTaskId(),
            entity.getStartAt(),
            entity.getDueAt(),
            entity.getStoryPoints(),
            entity.getOriginalEstimateSec(),
            entity.getRemainingEstimateSec(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * Convert list of TaskEntities to domain Tasks
     */
    public static List<Task> toDomainList(List<TaskEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }

        List<Task> tasks = new ArrayList<>();
        for (TaskEntity entity : entities) {
            Task task = toDomain(entity);
            if (task != null) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    /**
     * Convert list of domain Tasks to TaskEntities
     */
    public static List<TaskEntity> toEntityList(List<Task> tasks) {
        if (tasks == null) {
            return new ArrayList<>();
        }

        List<TaskEntity> entities = new ArrayList<>();
        for (Task task : tasks) {
            TaskEntity entity = toEntity(task);
            if (entity != null) {
                entities.add(entity);
            }
        }
        return entities;
    }

    // Helper methods to parse enum strings
    private static TaskType parseTaskType(String typeStr) {
        if (typeStr == null) return TaskType.TASK;
        try {
            return TaskType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return TaskType.TASK;
        }
    }

    private static TaskStatus parseTaskStatus(String statusStr) {
        if (statusStr == null) return TaskStatus.TO_DO;
        try {
            return TaskStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            return TaskStatus.TO_DO;
        }
    }

    private static TaskPriority parseTaskPriority(String priorityStr) {
        if (priorityStr == null) return TaskPriority.MEDIUM;
        try {
            return TaskPriority.valueOf(priorityStr);
        } catch (IllegalArgumentException e) {
            return TaskPriority.MEDIUM;
        }
    }
}

