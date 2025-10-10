package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.task.TimeEntryDTO;
import com.example.tralalero.domain.model.TimeEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Mapper for TimeEntry entity
 */
public class TimeEntryMapper {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
    );

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static TimeEntry toDomain(TimeEntryDTO dto) {
        if (dto == null) {
            return null;
        }

        return new TimeEntry(
                dto.getId(),
                dto.getTaskId(),
                dto.getUserId(),
                parseDate(dto.getStartAt()),
                parseDate(dto.getEndAt()),
                dto.getDurationSec(),
                dto.getNote(),
                parseDate(dto.getCreatedAt())
        );
    }

    public static TimeEntryDTO toDTO(TimeEntry timeEntry) {
        if (timeEntry == null) {
            return null;
        }

        TimeEntryDTO dto = new TimeEntryDTO();
        dto.setId(timeEntry.getId());
        dto.setTaskId(timeEntry.getTaskId());
        dto.setUserId(timeEntry.getUserId());
        dto.setStartAt(formatDate(timeEntry.getStartAt()));
        dto.setEndAt(formatDate(timeEntry.getEndAt()));
        dto.setDurationSec(timeEntry.getDurationSec());
        dto.setNote(timeEntry.getNote());
        dto.setCreatedAt(formatDate(timeEntry.getCreatedAt()));

        return dto;
    }

    public static List<TimeEntry> toDomainList(List<TimeEntryDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<TimeEntry> timeEntries = new ArrayList<>();
        for (TimeEntryDTO dto : dtos) {
            TimeEntry timeEntry = toDomain(dto);
            if (timeEntry != null) {
                timeEntries.add(timeEntry);
            }
        }
        return timeEntries;
    }

    public static List<TimeEntryDTO> toDTOList(List<TimeEntry> timeEntries) {
        if (timeEntries == null) {
            return new ArrayList<>();
        }

        List<TimeEntryDTO> dtos = new ArrayList<>();
        for (TimeEntry timeEntry : timeEntries) {
            TimeEntryDTO dto = toDTO(timeEntry);
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

