package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.task.TaskDTO;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.model.Label;
import com.example.tralalero.domain.model.AssigneeInfo;
import com.example.tralalero.domain.model.Task.TaskType;
import com.example.tralalero.domain.model.Task.TaskStatus;
import com.example.tralalero.domain.model.Task.TaskPriority;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TaskMapper {
    
    private static final SimpleDateFormat ISO_DATE_FORMAT;
    
    static {
        ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public static Task toDomain(TaskDTO dto) {
        if (dto == null) {
            return null;
        }
        
        android.util.Log.d("TaskMapper", "Converting task: " + dto.getTitle() + 
            ", taskLabels field: " + dto.getTaskLabels() + 
            ", taskAssignees field: " + dto.getTaskAssignees());
        
        TaskType type = parseTaskType(dto.getType());
        TaskStatus status = parseTaskStatus(dto.getStatus());
        TaskPriority priority = parseTaskPriority(dto.getPriority());
        Date startAt = parseDate(dto.getStartAt());
        Date dueAt = parseDate(dto.getDueAt());
        Date createdAt = parseDate(dto.getCreatedAt());
        Date updatedAt = parseDate(dto.getUpdatedAt());
        Date calendarSyncedAt = parseDate(dto.getLastSyncedAt());
        
        // Parse calendar sync fields
        boolean calendarSyncEnabled = dto.getCalendarReminderEnabled() != null && dto.getCalendarReminderEnabled();
        List<Integer> calendarReminderMinutes = null;
        if (calendarSyncEnabled && dto.getCalendarReminderTime() != null) {
            calendarReminderMinutes = new ArrayList<>();
            calendarReminderMinutes.add(dto.getCalendarReminderTime());
        }
        
        // Parse labels from task_labels nested structure
        List<Label> labels = new ArrayList<>();
        if (dto.getTaskLabels() != null) {
            android.util.Log.d("TaskMapper", "Parsing labels, count: " + dto.getTaskLabels().size());
            for (TaskDTO.TaskLabelDTO taskLabel : dto.getTaskLabels()) {
                if (taskLabel.getLabels() != null) {
                    TaskDTO.LabelDTO labelDto = taskLabel.getLabels();
                    android.util.Log.d("TaskMapper", "Label: " + labelDto.getName() + ", color: " + labelDto.getColor());
                    labels.add(new Label(
                        labelDto.getId(),
                        dto.getProjectId(), // projectId
                        labelDto.getName(),
                        labelDto.getColor()
                    ));
                }
            }
        }
        
        // Parse assignees from task_assignees nested structure
        List<AssigneeInfo> assignees = new ArrayList<>();
        if (dto.getTaskAssignees() != null) {
            android.util.Log.d("TaskMapper", "Parsing assignees, count: " + dto.getTaskAssignees().size());
            for (TaskDTO.TaskAssigneeDTO taskAssignee : dto.getTaskAssignees()) {
                if (taskAssignee.getUsers() != null) {
                    TaskDTO.UserDTO userDto = taskAssignee.getUsers();
                    android.util.Log.d("TaskMapper", "Assignee: " + userDto.getName() + ", avatar: " + userDto.getAvatarUrl());
                    assignees.add(new AssigneeInfo(
                        userDto.getId(),
                        userDto.getName(),
                        userDto.getEmail(),
                        userDto.getAvatarUrl()
                    ));
                }
            }
        }
        
        return new Task(
            dto.getId(),
            dto.getProjectId(),
            dto.getBoardId(),
            dto.getTitle(),
            dto.getDescription(),
            dto.getIssueKey(),
            type,
            status,
            priority,
            dto.getPosition(),
            dto.getAssigneeId(),
            dto.getCreatedBy(),
            dto.getSprintId(),
            dto.getEpicId(),
            dto.getParentTaskId(),
            startAt,
            dueAt,
            dto.getStoryPoints(),
            dto.getOriginalEstimateSec(),
            dto.getRemainingEstimateSec(),
            createdAt,
            updatedAt,
            // Calendar sync fields
            calendarSyncEnabled,
            calendarReminderMinutes,
            dto.getCalendarEventId(),
            calendarSyncedAt,
            // Labels
            labels,
            // Assignees
            assignees
        );
    }
    
    public static TaskDTO toDto(Task task) {
        if (task == null) {
            return null;
        }
        
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setProjectId(task.getProjectId());
        dto.setBoardId(task.getBoardId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setIssueKey(task.getIssueKey());
        dto.setType(formatTaskType(task.getType()));
        dto.setStatus(formatTaskStatus(task.getStatus()));
        dto.setPriority(formatTaskPriority(task.getPriority()));
        dto.setPosition(task.getPosition());
        dto.setAssigneeId(task.getAssigneeId());
        dto.setCreatedBy(task.getCreatedBy());
        dto.setSprintId(task.getSprintId());
        dto.setEpicId(task.getEpicId());
        dto.setParentTaskId(task.getParentTaskId());
        dto.setStartAt(formatDate(task.getStartAt()));
        dto.setDueAt(formatDate(task.getDueAt()));
        dto.setStoryPoints(task.getStoryPoints());
        dto.setOriginalEstimateSec(task.getOriginalEstimateSec());
        dto.setRemainingEstimateSec(task.getRemainingEstimateSec());
        dto.setCreatedAt(formatDate(task.getCreatedAt()));
        dto.setUpdatedAt(formatDate(task.getUpdatedAt()));
        
        // Calendar sync fields
        dto.setCalendarReminderEnabled(task.isCalendarSyncEnabled());
        if (task.getCalendarReminderMinutes() != null && !task.getCalendarReminderMinutes().isEmpty()) {
            dto.setCalendarReminderTime(task.getCalendarReminderMinutes().get(0));
        }
        dto.setCalendarEventId(task.getCalendarEventId());
        dto.setLastSyncedAt(formatDate(task.getCalendarSyncedAt()));
        
        return dto;
    }
    
    /**
     * Convert Task to TaskDTO for CREATE operation
     * Only includes fields required by backend CreateTaskDto:
     * - projectId (required)
     * - boardId (required)
     * - title (required)
     * - assigneeId (optional)
     */
    public static TaskDTO toDtoForCreate(Task task) {
        if (task == null) {
            return null;
        }
        
        TaskDTO dto = new TaskDTO();
        // Only set REQUIRED fields for create
        dto.setProjectId(task.getProjectId());
        dto.setBoardId(task.getBoardId());
        dto.setTitle(task.getTitle());
        
        // Optional field - only set if not null
        if (task.getAssigneeId() != null && !task.getAssigneeId().isEmpty()) {
            dto.setAssigneeId(task.getAssigneeId());
        }
        
        return dto;
    }
    
    public static List<Task> toDomainList(List<TaskDTO> dtoList) {
        if (dtoList == null) {
            return null;
        }
        
        List<Task> tasks = new ArrayList<>();
        for (TaskDTO dto : dtoList) {
            tasks.add(toDomain(dto));
        }
        return tasks;
    }
    
    public static List<TaskDTO> toDtoList(List<Task> tasks) {
        if (tasks == null) {
            return null;
        }
        
        List<TaskDTO> dtoList = new ArrayList<>();
        for (Task task : tasks) {
            dtoList.add(toDto(task));
        }
        return dtoList;
    }
    
    // Enum converters
    private static TaskType parseTaskType(String typeString) {
        if (typeString == null || typeString.isEmpty()) {
            return TaskType.TASK;
        }
        
        try {
            return TaskType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TaskType.TASK;
        }
    }
    
    private static String formatTaskType(TaskType type) {
        if (type == null) {
            return "TASK";
        }
        return type.name();
    }
    
    private static TaskStatus parseTaskStatus(String statusString) {
        if (statusString == null || statusString.isEmpty()) {
            return TaskStatus.TO_DO;
        }
        
        try {
            return TaskStatus.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TaskStatus.TO_DO;
        }
    }
    
    private static String formatTaskStatus(TaskStatus status) {
        if (status == null) {
            return "TO_DO";
        }
        return status.name();
    }
    
    private static TaskPriority parseTaskPriority(String priorityString) {
        if (priorityString == null || priorityString.isEmpty()) {
            return TaskPriority.MEDIUM;
        }
        
        try {
            return TaskPriority.valueOf(priorityString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TaskPriority.MEDIUM;
        }
    }
    
    private static String formatTaskPriority(TaskPriority priority) {
        if (priority == null) {
            return "MEDIUM";
        }
        return priority.name();
    }
    
    // Date converters
    private static Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        
        try {
            return ISO_DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            try {
                SimpleDateFormat altFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                return altFormat.parse(dateString);
            } catch (ParseException ex) {
                return null;
            }
        }
    }
    
    private static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        
        return ISO_DATE_FORMAT.format(date);
    }
}
