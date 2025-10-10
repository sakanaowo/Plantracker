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

/**
 * Mapper for Checklist entity
 */
public class ChecklistMapper {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
    );

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static Checklist toDomain(CheckListDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Checklist(
                dto.getId(),
                dto.getTaskId(),
                dto.getTitle(),
                parseDate(dto.getCreatedAt())
        );
    }

    public static CheckListDTO toDTO(Checklist checklist) {
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

    public static List<Checklist> toDomainList(List<CheckListDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<Checklist> checklists = new ArrayList<>();
        for (CheckListDTO dto : dtos) {
            Checklist checklist = toDomain(dto);
            if (checklist != null) {
                checklists.add(checklist);
            }
        }
        return checklists;
    }

    public static List<CheckListDTO> toDTOList(List<Checklist> checklists) {
        if (checklists == null) {
            return new ArrayList<>();
        }

        List<CheckListDTO> dtos = new ArrayList<>();
        for (Checklist checklist : checklists) {
            CheckListDTO dto = toDTO(checklist);
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

