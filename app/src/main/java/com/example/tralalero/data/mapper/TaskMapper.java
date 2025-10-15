package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.task.TaskDTO;
import com.example.tralalero.domain.model.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TaskMapper {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "dd-MM-yyyy'T'HH:mm:ss.SSS'Z'",
            Locale.US
    );

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static Task toDomain(TaskDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Task(
                dto.getId(),
                dto.getProjectId(),
                dto.getBoardId(),
                dto.getTitle(),
                dto.getDescription(),
                dto.getIssueKey(),
                parseTaskType(dto.getType()),
                parseTaskStatus(dto.getStatus()),
                parseTaskPriority(dto.getPriority()),
                dto.getPosition(),
                dto.getAssigneeId(),
                dto.getCreatedBy(),
                dto.getSprintId(),
                dto.getEpicId(),
                dto.getParentTaskId(),
                parseDate(dto.getStartAt()),
                parseDate(dto.getDueAt()),
                dto.getStoryPoints(),
                dto.getOriginalEstimateSec(),
                dto.getRemainingEstimateSec(),
                parseDate(dto.getCreatedAt()),
                parseDate(dto.getUpdatedAt())
        );
    }

    public static TaskDTO toDTO(Task task) {
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
        dto.setType(task.getType() != null ? task.getType().name() : null);
        dto.setStatus(task.getStatus() != null ? task.getStatus().name() : null);
        dto.setPriority(task.getPriority() != null ? task.getPriority().name() : null);
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

        return dto;
    }

    public static List<Task> toDomainList(List<TaskDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<Task> tasks = new ArrayList<>();
        for (TaskDTO dto : dtos) {
            Task task = toDomain(dto);
            if (task != null) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    private static Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        try {
            return DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    private static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return DATE_FORMAT.format(date);
    }


    private static Task.TaskType parseTaskType(String type) {
        if (type == null || type.isEmpty()) {
            return Task.TaskType.TASK;
        }

        try {
            return Task.TaskType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Task.TaskType.TASK;
        }
    }


    private static Task.TaskStatus parseTaskStatus(String status) {
        if (status == null || status.isEmpty()) {
            return Task.TaskStatus.TO_DO;
        }

        try {
            return Task.TaskStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Task.TaskStatus.TO_DO;
        }
    }

    private static Task.TaskPriority parseTaskPriority(String priority) {
        if (priority == null || priority.isEmpty()) {
            return Task.TaskPriority.MEDIUM;
        }

        try {
            return Task.TaskPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Task.TaskPriority.MEDIUM;
        }
    }
}
