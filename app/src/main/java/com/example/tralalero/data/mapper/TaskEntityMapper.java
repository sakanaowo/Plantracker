package com.example.tralalero.data.mapper;

import com.example.tralalero.data.local.database.entity.TaskEntity;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.model.Task.TaskType;
import com.example.tralalero.domain.model.Task.TaskStatus;
import com.example.tralalero.domain.model.Task.TaskPriority;

import java.util.ArrayList;
import java.util.List;

public class TaskEntityMapper {
    
    public static TaskEntity toEntity(Task task) {
        if (task == null) {
            return null;
        }
        
        // Convert domain Task to Room entity
        // Map available fields, entity has simpler structure
        return new TaskEntity(
            parseId(task.getId()),
            task.getTitle(),
            task.getDescription(),
            task.getDueAt(), // dueDate -> dueAt
            priorityToInt(task.getPriority()),
            task.isDone(), // completed -> isDone()
            parseId(task.getProjectId()),
            task.getAssigneeId(), // userId -> assigneeId
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
    
    public static Task toDomain(TaskEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // Convert Room entity to domain Task (22 parameters!)
        // Use default values for fields not in entity
        return new Task(
            String.valueOf(entity.getId()),              // id
            String.valueOf(entity.getProjectId()),       // projectId
            null,                                         // boardId - not in entity
            entity.getTitle(),                            // title
            entity.getDescription(),                      // description
            null,                                         // issueKey - not in entity
            TaskType.TASK,                                // type - default TASK
            entity.isCompleted() ? TaskStatus.DONE : TaskStatus.TO_DO, // status
            intToPriority(entity.getPriority()),          // priority
            0.0,                                          // position - default 0
            entity.getUserId(),                           // assigneeId
            null,                                         // createdBy - not in entity
            null,                                         // sprintId - not in entity
            null,                                         // epicId - not in entity
            null,                                         // parentTaskId - not in entity
            null,                                         // startAt - not in entity
            entity.getDueDate(),                          // dueAt
            null,                                         // storyPoints - not in entity
            null,                                         // originalEstimateSec - not in entity
            null,                                         // remainingEstimateSec - not in entity
            entity.getCreatedAt(),                        // createdAt
            entity.getUpdatedAt()                         // updatedAt
        );
    }
    
    private static int parseId(String id) {
        if (id == null || id.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private static int priorityToInt(TaskPriority priority) {
        if (priority == null) return 1; // MEDIUM
        switch (priority) {
            case LOW: return 0;
            case MEDIUM: return 1;
            case HIGH: return 2;
            default: return 1;
        }
    }
    
    private static TaskPriority intToPriority(int priority) {
        switch (priority) {
            case 0: return TaskPriority.LOW;
            case 2: return TaskPriority.HIGH;
            default: return TaskPriority.MEDIUM;
        }
    }
    
    public static List<TaskEntity> toEntityList(List<Task> tasks) {
        if (tasks == null) {
            return null;
        }
        
        List<TaskEntity> entities = new ArrayList<>();
        for (Task task : tasks) {
            entities.add(toEntity(task));
        }
        return entities;
    }
    
    public static List<Task> toDomainList(List<TaskEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        List<Task> tasks = new ArrayList<>();
        for (TaskEntity entity : entities) {
            tasks.add(toDomain(entity));
        }
        return tasks;
    }
}
