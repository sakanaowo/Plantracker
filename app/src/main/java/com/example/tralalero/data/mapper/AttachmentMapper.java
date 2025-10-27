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

public class AttachmentMapper {
    
    private static final SimpleDateFormat ISO_DATE_FORMAT;
    
    static {
        ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public static Attachment toDomain(AttachmentDTO dto) {
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
    
    public static AttachmentDTO toDto(Attachment attachment) {
        if (attachment == null) {
            return null;
        }
        
        AttachmentDTO dto = new AttachmentDTO();
        dto.id = attachment.getId();
        dto.taskId = attachment.getTaskId();
        dto.url = attachment.getUrl();
        dto.fileName = attachment.getFileName();
        dto.mimeType = attachment.getMimeType();
        dto.size = attachment.getSize();
        dto.uploadedBy = attachment.getUploadedBy();
        dto.createdAt = formatDate(attachment.getCreatedAt());
        
        return dto;
    }
    
    public static List<Attachment> toDomainList(List<AttachmentDTO> dtoList) {
        if (dtoList == null) {
            return null;
        }
        
        List<Attachment> attachments = new ArrayList<>();
        for (AttachmentDTO dto : dtoList) {
            attachments.add(toDomain(dto));
        }
        return attachments;
    }
    
    public static List<AttachmentDTO> toDtoList(List<Attachment> attachments) {
        if (attachments == null) {
            return null;
        }
        
        List<AttachmentDTO> dtoList = new ArrayList<>();
        for (Attachment attachment : attachments) {
            dtoList.add(toDto(attachment));
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
