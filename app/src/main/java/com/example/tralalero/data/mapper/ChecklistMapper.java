package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.task.CheckListDTO;
import com.example.tralalero.domain.model.Checklist;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ChecklistMapper {
    
    private static final SimpleDateFormat ISO_DATE_FORMAT;
    
    static {
        ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public static Checklist toDomain(CheckListDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Date createdAt = parseDate(dto.getCreatedAt());
        
        return new Checklist(
            dto.getId(),
            dto.getTaskId(),
            dto.getTitle(),
            createdAt
        );
    }
    
    public static CheckListDTO toDto(Checklist checklist) {
        if (checklist == null) {
            return null;
        }
        
        CheckListDTO dto = new CheckListDTO();
        dto.setId(checklist.getId());
        dto.setTaskId(checklist.getTaskId());
        dto.setTitle(checklist.getTitle());
        dto.setCreatedAt(formatDate(checklist.getCreatedAt()));
        
        return dto;
    }
    
    public static List<Checklist> toDomainList(List<CheckListDTO> dtoList) {
        if (dtoList == null) {
            return null;
        }
        
        List<Checklist> checklists = new ArrayList<>();
        for (CheckListDTO dto : dtoList) {
            checklists.add(toDomain(dto));
        }
        return checklists;
    }
    
    public static List<CheckListDTO> toDtoList(List<Checklist> checklists) {
        if (checklists == null) {
            return null;
        }
        
        List<CheckListDTO> dtoList = new ArrayList<>();
        for (Checklist checklist : checklists) {
            dtoList.add(toDto(checklist));
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
