package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.sprint.SprintDTO;
import com.example.tralalero.domain.model.Sprint;
import com.example.tralalero.domain.model.SprintState;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SprintMapper {
    
    private static final SimpleDateFormat ISO_DATE_FORMAT;
    
    static {
        ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public static Sprint toDomain(SprintDTO dto) {
        if (dto == null) {
            return null;
        }
        
        SprintState state = parseSprintState(dto.getState());
        Date startAt = parseDate(dto.getStartAt());
        Date endAt = parseDate(dto.getEndAt());
        Date createdAt = parseDate(dto.getCreatedAt());
        
        return new Sprint(
            dto.getId(),
            dto.getProjectId(),
            dto.getName(),
            dto.getGoal(),
            startAt,
            endAt,
            state,
            createdAt
        );
    }
    
    public static SprintDTO toDto(Sprint sprint) {
        if (sprint == null) {
            return null;
        }
        
        SprintDTO dto = new SprintDTO();
        dto.setId(sprint.getId());
        dto.setProjectId(sprint.getProjectId());
        dto.setName(sprint.getName());
        dto.setGoal(sprint.getGoal());
        dto.setStartAt(formatDate(sprint.getStartAt()));
        dto.setEndAt(formatDate(sprint.getEndAt()));
        dto.setState(formatSprintState(sprint.getState()));
        dto.setCreatedAt(formatDate(sprint.getCreatedAt()));
        
        return dto;
    }
    
    public static List<Sprint> toDomainList(List<SprintDTO> dtoList) {
        if (dtoList == null) {
            return null;
        }
        
        List<Sprint> sprints = new ArrayList<>();
        for (SprintDTO dto : dtoList) {
            sprints.add(toDomain(dto));
        }
        return sprints;
    }
    
    public static List<SprintDTO> toDtoList(List<Sprint> sprints) {
        if (sprints == null) {
            return null;
        }
        
        List<SprintDTO> dtoList = new ArrayList<>();
        for (Sprint sprint : sprints) {
            dtoList.add(toDto(sprint));
        }
        return dtoList;
    }
    
    private static SprintState parseSprintState(String stateString) {
        if (stateString == null || stateString.isEmpty()) {
            return SprintState.PLANNED;
        }
        
        try {
            return SprintState.valueOf(stateString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SprintState.PLANNED;
        }
    }
    
    private static String formatSprintState(SprintState state) {
        if (state == null) {
            return "PLANNED";
        }
        
        return state.name();
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
