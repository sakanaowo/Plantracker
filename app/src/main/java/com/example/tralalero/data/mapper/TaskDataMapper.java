package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.task.AttachmentDTO;
import com.example.tralalero.data.remote.dto.task.TaskCommentDTO;
import com.example.tralalero.domain.model.Attachment;
import com.example.tralalero.domain.model.Comment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Mapper for Attachment and Comment DTOs to Domain models
 */
public class TaskDataMapper {

    private static final SimpleDateFormat ISO_DATE_FORMAT;

    static {
        ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // ==================== ATTACHMENT MAPPING ====================

    /**
     * Convert AttachmentDTO to Attachment domain model
     */
    public static Attachment attachmentDtoToDomain(AttachmentDTO dto) {
        if (dto == null) {
            return null;
        }

        Date createdAt = parseDate(dto.createdAt);

        return new Attachment(
            dto.id,
            dto.taskId,
            dto.url,
            dto.fileName,
            dto.mimeType,
            dto.size,
            dto.uploadedBy,
            createdAt
        );
    }

    /**
     * Convert list of AttachmentDTO to list of Attachment domain models
     */
    public static List<Attachment> attachmentDtoListToDomain(List<AttachmentDTO> dtoList) {
        if (dtoList == null) {
            return new ArrayList<>();
        }

        List<Attachment> attachments = new ArrayList<>();
        for (AttachmentDTO dto : dtoList) {
            Attachment attachment = attachmentDtoToDomain(dto);
            if (attachment != null) {
                attachments.add(attachment);
            }
        }
        return attachments;
    }

    // ==================== COMMENT MAPPING ====================

    /**
     * Convert TaskCommentDTO to Comment domain model
     */
    public static Comment commentDtoToDomain(TaskCommentDTO dto) {
        if (dto == null) {
            return null;
        }

        Date createdAt = parseDate(dto.getCreatedAt());
        Comment.User user = new Comment.User(
            dto.getUserId(),
            "User " + dto.getUserId(), // Placeholder - backend should return user name
            null // Placeholder - backend should return avatar URL
        );

        return new Comment(
            dto.getId(),
            dto.getTaskId(),
            dto.getUserId(),
            dto.getBody(),
            createdAt,
            user
        );
    }

    /**
     * Convert list of TaskCommentDTO to list of Comment domain models
     */
    public static List<Comment> commentDtoListToDomain(List<TaskCommentDTO> dtoList) {
        if (dtoList == null) {
            return new ArrayList<>();
        }

        List<Comment> comments = new ArrayList<>();
        for (TaskCommentDTO dto : dtoList) {
            Comment comment = commentDtoToDomain(dto);
            if (comment != null) {
                comments.add(comment);
            }
        }
        return comments;
    }

    // ==================== DTO CREATION ====================

    /**
     * Create AttachmentDTO for upload request
     */
    public static AttachmentDTO createAttachmentDto(String taskId, String url, String fileName, 
                                                   String mimeType, Integer size) {
        AttachmentDTO dto = new AttachmentDTO();
        dto.taskId = taskId;
        dto.url = url;
        dto.fileName = fileName;
        dto.mimeType = mimeType;
        dto.size = size;
        return dto;
    }

    /**
     * Create TaskCommentDTO for create/update request
     */
    public static TaskCommentDTO createCommentDto(String taskId, String body) {
        TaskCommentDTO dto = new TaskCommentDTO();
        dto.setTaskId(taskId);
        dto.setBody(body);
        return dto;
    }

    // ==================== HELPER METHODS ====================

    private static Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        try {
            return ISO_DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            // Try alternative format without milliseconds
            try {
                SimpleDateFormat altFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                altFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                return altFormat.parse(dateString);
            } catch (ParseException ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }
}
