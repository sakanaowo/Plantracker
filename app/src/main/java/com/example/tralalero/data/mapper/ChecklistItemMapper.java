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

public class ChecklistItemMapper {
    
    private static final SimpleDateFormat ISO_DATE_FORMAT;
    
    static {
        ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public static ChecklistItem toDomain(CheckListItemDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Date createdAt = parseDate(dto.getCreatedAt());
        
        return new ChecklistItem(
            dto.getId(),
            dto.getChecklistId(),
            dto.getContent(),
            dto.isDone(),
            dto.getPosition(),
            createdAt
        );
    }
    
    public static CheckListItemDTO toDto(ChecklistItem item) {
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
    
    public static List<ChecklistItem> toDomainList(List<CheckListItemDTO> dtoList) {
        if (dtoList == null) {
            return null;
        }
        
        List<ChecklistItem> items = new ArrayList<>();
        for (CheckListItemDTO dto : dtoList) {
            items.add(toDomain(dto));
        }
        return items;
    }
    
    public static List<CheckListItemDTO> toDtoList(List<ChecklistItem> items) {
        if (items == null) {
            return null;
        }
        
        List<CheckListItemDTO> dtoList = new ArrayList<>();
        for (ChecklistItem item : items) {
            dtoList.add(toDto(item));
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
