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
    
    private static final SimpleDateFormat ISO_DATE_FORMAT;
    
    static {
        ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public static TaskComment toDomain(TaskCommentDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Date createdAt = parseDate(dto.getCreatedAt());
        
        return new TaskComment(
            dto.getId(),
            dto.getTaskId(),
            dto.getUserId(),
            dto.getBody(),
            createdAt
        );
    }
    
    public static TaskCommentDTO toDto(TaskComment comment) {
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
    
    public static List<TaskComment> toDomainList(List<TaskCommentDTO> dtoList) {
        if (dtoList == null) {
            return null;
        }
        
        List<TaskComment> comments = new ArrayList<>();
        for (TaskCommentDTO dto : dtoList) {
            comments.add(toDomain(dto));
        }
        return comments;
    }
    
    public static List<TaskCommentDTO> toDtoList(List<TaskComment> comments) {
        if (comments == null) {
            return null;
        }
        
        List<TaskCommentDTO> dtoList = new ArrayList<>();
        for (TaskComment comment : comments) {
            dtoList.add(toDto(comment));
        }
        return dtoList;
    }
    
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
