package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.notification.NotificationDTO;
import com.example.tralalero.domain.model.Notification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class NotificationMapper {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
    );

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static Notification toDomain(NotificationDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Notification(
                dto.getId(),
                dto.getUserId(),
                parseNotificationType(dto.getType()),
                dto.getTitle(),
                dto.getBody(),
                dto.getDeeplink(),
                parseDate(dto.getReadAt()),
                parseDate(dto.getCreatedAt())
        );
    }

    public static NotificationDTO toDTO(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setType(notification.getType() != null ? notification.getType().name() : null);
        dto.setTitle(notification.getTitle());
        dto.setBody(notification.getBody());
        dto.setDeeplink(notification.getDeeplink());
        dto.setReadAt(formatDate(notification.getReadAt()));
        dto.setCreatedAt(formatDate(notification.getCreatedAt()));

        return dto;
    }

    public static List<Notification> toDomainList(List<NotificationDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<Notification> notifications = new ArrayList<>();
        for (NotificationDTO dto : dtos) {
            Notification notification = toDomain(dto);
            if (notification != null) {
                notifications.add(notification);
            }
        }
        return notifications;
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

    private static Notification.NotificationType parseNotificationType(String type) {
        if (type == null || type.isEmpty()) {
            return Notification.NotificationType.SYSTEM;
        }

        try {
            return Notification.NotificationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Notification.NotificationType.SYSTEM;
        }
    }
}
