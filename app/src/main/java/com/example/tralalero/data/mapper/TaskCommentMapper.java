package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.task.TaskCommentDTO;
import com.example.tralalero.domain.model.TaskComment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class TaskCommentMapper {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
    );

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static TaskComment toDomain(TaskCommentDTO dto) {
        if (dto == null) {
            return null;
        }

        return new TaskComment(
                dto.getId(),
                dto.getTaskId(),
                dto.getUserId(),
                dto.getBody(),
                parseDate(dto.getCreatedAt())
        );
    }

    public static TaskCommentDTO toDTO(TaskComment comment) {
        if (comment == null) {
            return null;
        }

        TaskCommentDTO dto = new TaskCommentDTO();
        dto.setId(comment.getId());
        dto.setTaskId(comment.getTaskId());
        dto.setUserId(comment.getUserId());
        dto.setBody(comment.getBody());
        dto.setCreatedAt(formatDate(comment.getCreatedAt()));

        return dto;
    }

    public static List<TaskComment> toDomainList(List<TaskCommentDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<TaskComment> comments = new ArrayList<>();
        for (TaskCommentDTO dto : dtos) {
            TaskComment comment = toDomain(dto);
            if (comment != null) {
                comments.add(comment);
            }
        }
        return comments;
    }

    public static List<TaskCommentDTO> toDTOList(List<TaskComment> comments) {
        if (comments == null) {
            return new ArrayList<>();
        }

        List<TaskCommentDTO> dtos = new ArrayList<>();
        for (TaskComment comment : comments) {
            TaskCommentDTO dto = toDTO(comment);
            if (dto != null) {
                dtos.add(dto);
            }
        }
        return dtos;
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
}

