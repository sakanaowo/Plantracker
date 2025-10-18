package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.notification.NotificationDTO;
import com.example.tralalero.domain.model.Notification;
import com.example.tralalero.domain.model.Notification.NotificationType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NotificationMapper {
    
    private static final SimpleDateFormat ISO_DATE_FORMAT;
    
    static {
        ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public static Notification toDomain(NotificationDTO dto) {
        if (dto == null) {
            return null;
        }
        
        NotificationType type = parseNotificationType(dto.getType());
        Date readAt = parseDate(dto.getReadAt());
        Date createdAt = parseDate(dto.getCreatedAt());
        
        return new Notification(
            dto.getId(),
            dto.getUserId(),
            type,
            dto.getTitle(),
            dto.getBody(),
            dto.getDeeplink(),
            readAt,
            createdAt
        );
    }
    
    public static NotificationDTO toDto(Notification notification) {
        if (notification == null) {
            return null;
        }
        
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setType(formatNotificationType(notification.getType()));
        dto.setTitle(notification.getTitle());
        dto.setBody(notification.getBody());
        dto.setDeeplink(notification.getDeeplink());
        dto.setReadAt(formatDate(notification.getReadAt()));
        dto.setCreatedAt(formatDate(notification.getCreatedAt()));
        
        return dto;
    }
    
    public static List<Notification> toDomainList(List<NotificationDTO> dtoList) {
        if (dtoList == null) {
            return null;
        }
        
        List<Notification> notifications = new ArrayList<>();
        for (NotificationDTO dto : dtoList) {
            notifications.add(toDomain(dto));
        }
        return notifications;
    }
    
    public static List<NotificationDTO> toDtoList(List<Notification> notifications) {
        if (notifications == null) {
            return null;
        }
        
        List<NotificationDTO> dtoList = new ArrayList<>();
        for (Notification notification : notifications) {
            dtoList.add(toDto(notification));
        }
        return dtoList;
    }
    
    private static NotificationType parseNotificationType(String typeString) {
        if (typeString == null || typeString.isEmpty()) {
            return NotificationType.SYSTEM;
        }
        
        try {
            return NotificationType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NotificationType.SYSTEM;
        }
    }
    
    private static String formatNotificationType(NotificationType type) {
        if (type == null) {
            return "SYSTEM";
        }
        
        return type.name();
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
