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
        ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public static Task toDomain(TaskDTO dto) {
        if (dto == null) {
            return null;
        }
        
        android.util.Log.d("TaskMapper", "Converting task: " + dto.getTitle() + 
            ", taskLabels field: " + dto.getTaskLabels() + 
            ", taskAssignees field: " + dto.getTaskAssignees());
        android.util.Log.d("TaskMapper", "📅 Raw DTO dates - startAt: '" + dto.getStartAt() + 
            "', dueAt: '" + dto.getDueAt() + "'");
        
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
    
    /**
     * Convert Task to TaskDTO for UPDATE operation
     * Only includes fields that user can edit in CardDetailActivity.
     * 
     * EXCLUDES read-only/auto-managed fields:
     * - position (managed by move API)
     * - boardId, projectId (not editable in CardDetail)
     * - taskAssignees, taskLabels (managed separately via assignees/labels APIs)
     * - createdAt, updatedAt, deletedAt (auto-managed by backend)
     * - createdBy, updatedBy (auto-set by backend from auth)
     * - calendarEventId, lastSyncedAt (managed by calendar sync API)
     * - assigneeId, assigneeIds (legacy/managed separately)
     * 
     * INCLUDES editable fields in CardDetail:
     * - title, description
     * - dueAt, startAt
     * - priority, type, status
     * - calendarReminderEnabled, calendarReminderTime
     * - Advanced fields: issueKey, sprintId, epicId, parentTaskId, storyPoints, estimates
     */
    public static TaskDTO toDtoForUpdate(Task task) {
        if (task == null) {
            return null;
        }
        
        TaskDTO dto = new TaskDTO();
        
        // ✅ Always include ID for update
        dto.setId(task.getId());
        
        // ✅ Basic editable fields (CardDetail UI)
        if (task.getTitle() != null && !task.getTitle().isEmpty()) {
            dto.setTitle(task.getTitle());
        }
        if (task.getDescription() != null) {
            dto.setDescription(task.getDescription());
        }
        
        // ✅ Status/Priority (editable in CardDetail)
        if (task.getStatus() != null) {
            dto.setStatus(formatTaskStatus(task.getStatus()));
        }
        if (task.getPriority() != null) {
            dto.setPriority(formatTaskPriority(task.getPriority()));
        }
        
        // ✅ Dates (editable in CardDetail)
        if (task.getStartAt() != null) {
            dto.setStartAt(formatDate(task.getStartAt()));
        }
        if (task.getDueAt() != null) {
            dto.setDueAt(formatDate(task.getDueAt()));
        }
        
        // ✅ Calendar sync fields (editable in CardDetail)
        dto.setCalendarReminderEnabled(task.isCalendarSyncEnabled());
        if (task.getCalendarReminderMinutes() != null && !task.getCalendarReminderMinutes().isEmpty()) {
            dto.setCalendarReminderTime(task.getCalendarReminderMinutes().get(0));
        }
        
        // ✅ Advanced fields (if implemented in UI, currently not used but schema exists)
        if (task.getIssueKey() != null && !task.getIssueKey().isEmpty()) {
            dto.setIssueKey(task.getIssueKey());
        }
        if (task.getType() != null) {
            dto.setType(formatTaskType(task.getType()));
        }
        if (task.getSprintId() != null && !task.getSprintId().isEmpty()) {
            dto.setSprintId(task.getSprintId());
        }
        if (task.getEpicId() != null && !task.getEpicId().isEmpty()) {
            dto.setEpicId(task.getEpicId());
        }
        if (task.getParentTaskId() != null && !task.getParentTaskId().isEmpty()) {
            dto.setParentTaskId(task.getParentTaskId());
        }
        if (task.getStoryPoints() != null) {
            dto.setStoryPoints(task.getStoryPoints());
        }
        if (task.getOriginalEstimateSec() != null) {
            dto.setOriginalEstimateSec(task.getOriginalEstimateSec());
        }
        if (task.getRemainingEstimateSec() != null) {
            dto.setRemainingEstimateSec(task.getRemainingEstimateSec());
        }
        
        // ❌ DO NOT include:
        // - position (use move API)
        // - boardId, projectId (read-only)
        // - taskAssignees, taskLabels (use separate APIs)
        // - timestamps (auto-managed)
        // - calendarEventId, lastSyncedAt (auto-managed)
        
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
        android.util.Log.d("TaskMapper", "📅 parseDate input: " + dateString);
        
        if (dateString == null || dateString.isEmpty()) {
            android.util.Log.d("TaskMapper", "  ⚠️ Date is null/empty");
            return null;
        }
        
        try {
            Date parsed = ISO_DATE_FORMAT.parse(dateString);
            android.util.Log.d("TaskMapper", "  ✅ Parsed with ISO format: " + parsed);
            return parsed;
        } catch (ParseException e) {
            android.util.Log.d("TaskMapper", "  ⚠️ ISO format failed: " + e.getMessage());
            try {
                SimpleDateFormat altFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                Date parsed = altFormat.parse(dateString);
                android.util.Log.d("TaskMapper", "  ✅ Parsed with alt format: " + parsed);
                return parsed;
            } catch (ParseException ex) {
                android.util.Log.d("TaskMapper", "  ❌ All formats failed: " + ex.getMessage());
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
