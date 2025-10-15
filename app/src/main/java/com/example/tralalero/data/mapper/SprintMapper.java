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

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
    );

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static Sprint toDomain(SprintDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Sprint(
                dto.getId(),
                dto.getProjectId(),
                dto.getName(),
                dto.getGoal(),
                parseDate(dto.getStartAt()),
                parseDate(dto.getEndAt()),
                parseSprintState(dto.getState()),
                parseDate(dto.getCreatedAt())
        );
    }

    public static SprintDTO toDTO(Sprint sprint) {
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

    public static List<Sprint> toDomainList(List<SprintDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<Sprint> sprints = new ArrayList<>();
        for (SprintDTO dto : dtos) {
            Sprint sprint = toDomain(dto);
            if (sprint != null) {
                sprints.add(sprint);
            }
        }
        return sprints;
    }

    public static List<SprintDTO> toDTOList(List<Sprint> sprints) {
        if (sprints == null) {
            return new ArrayList<>();
        }

        List<SprintDTO> dtos = new ArrayList<>();
        for (Sprint sprint : sprints) {
            SprintDTO dto = toDTO(sprint);
            if (dto != null) {
                dtos.add(dto);
            }
        }
        return dtos;
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

