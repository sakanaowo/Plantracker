package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.task.AttachmentDTO;
import com.example.tralalero.domain.model.Attachment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Mapper for Attachment entity
 */
public class AttachmentMapper {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
    );

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static Attachment toDomain(AttachmentDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Attachment(
                dto.id,
                dto.taskId,
                dto.url,
                dto.mimeType,
                dto.size,
                dto.uploadedBy,
                parseDate(dto.createdAt)
        );
    }

    public static AttachmentDTO toDTO(Attachment attachment) {
        if (attachment == null) {
            return null;
        }

        AttachmentDTO dto = new AttachmentDTO();
        dto.id = attachment.getId();
        dto.taskId = attachment.getTaskId();
        dto.url = attachment.getUrl();
        dto.mimeType = attachment.getMimeType();
        dto.size = attachment.getSize();
        dto.uploadedBy = attachment.getUploadedBy();
        dto.createdAt = formatDate(attachment.getCreatedAt());

        return dto;
    }

    public static List<Attachment> toDomainList(List<AttachmentDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<Attachment> attachments = new ArrayList<>();
        for (AttachmentDTO dto : dtos) {
            Attachment attachment = toDomain(dto);
            if (attachment != null) {
                attachments.add(attachment);
            }
        }
        return attachments;
    }

    public static List<AttachmentDTO> toDTOList(List<Attachment> attachments) {
        if (attachments == null) {
            return new ArrayList<>();
        }

        List<AttachmentDTO> dtos = new ArrayList<>();
        for (Attachment attachment : attachments) {
            AttachmentDTO dto = toDTO(attachment);
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

