package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.task.CheckListItemDTO;
import com.example.tralalero.domain.model.ChecklistItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Mapper for ChecklistItem entity
 */
public class ChecklistItemMapper {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
    );

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static ChecklistItem toDomain(CheckListItemDTO dto) {
        if (dto == null) {
            return null;
        }

        return new ChecklistItem(
                dto.getId(),
                dto.getChecklistId(),
                dto.getContent(),
                dto.isDone(),
                dto.getPosition(),
                parseDate(dto.getCreatedAt())
        );
    }

    public static CheckListItemDTO toDTO(ChecklistItem item) {
        if (item == null) {
            return null;
        }

        CheckListItemDTO dto = new CheckListItemDTO();
        dto.setId(item.getId());
        dto.setChecklistId(item.getChecklistId());
        dto.setContent(item.getContent());
        dto.setDone(item.isDone());
        dto.setPosition(item.getPosition());
        dto.setCreatedAt(formatDate(item.getCreatedAt()));

        return dto;
    }

    public static List<ChecklistItem> toDomainList(List<CheckListItemDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<ChecklistItem> items = new ArrayList<>();
        for (CheckListItemDTO dto : dtos) {
            ChecklistItem item = toDomain(dto);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    public static List<CheckListItemDTO> toDTOList(List<ChecklistItem> items) {
        if (items == null) {
            return new ArrayList<>();
        }

        List<CheckListItemDTO> dtos = new ArrayList<>();
        for (ChecklistItem item : items) {
            CheckListItemDTO dto = toDTO(item);
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

