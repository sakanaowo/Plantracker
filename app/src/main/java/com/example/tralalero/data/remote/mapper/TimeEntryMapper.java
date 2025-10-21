package com.example.tralalero.data.remote.mapper;

import com.example.tralalero.data.remote.dto.task.TimeEntryDTO;
import com.example.tralalero.domain.model.TimeEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TimeEntryMapper {
    private static final SimpleDateFormat ISO_DATE_FORMAT;

    static {
        ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Converts TimeEntryDTO to TimeEntry domain model
     */
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

    /**
     * Converts TimeEntry domain model to TimeEntryDTO
     */
    public static TimeEntryDTO toDto(TimeEntry timeEntry) {
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

    /**
     * Converts list of TimeEntryDTO to list of TimeEntry domain models
     */
    public static List<TimeEntry> toDomainList(List<TimeEntryDTO> dtoList) {
        if (dtoList == null) {
            return new ArrayList<>();
        }

        List<TimeEntry> timeEntries = new ArrayList<>();
        for (TimeEntryDTO dto : dtoList) {
            TimeEntry timeEntry = toDomain(dto);
            if (timeEntry != null) {
                timeEntries.add(timeEntry);
            }
        }
        return timeEntries;
    }

    /**
     * Converts list of TimeEntry domain models to list of TimeEntryDTO
     */
    public static List<TimeEntryDTO> toDtoList(List<TimeEntry> timeEntries) {
        if (timeEntries == null) {
            return new ArrayList<>();
        }

        List<TimeEntryDTO> dtoList = new ArrayList<>();
        for (TimeEntry timeEntry : timeEntries) {
            TimeEntryDTO dto = toDto(timeEntry);
            if (dto != null) {
                dtoList.add(dto);
            }
        }
        return dtoList;
    }

    /**
     * Parses ISO 8601 date string to Date object
     * Returns null if parsing fails or input is null/empty
     */
    private static Date parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            return ISO_DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            // Try fallback parsing for different formats
            try {
                SimpleDateFormat fallbackFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                return fallbackFormat.parse(dateString);
            } catch (ParseException ex) {
                return null;
            }
        }
    }

    /**
     * Formats Date object to ISO 8601 string
     * Returns null if input is null
     */
    private static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return ISO_DATE_FORMAT.format(date);
    }
}
