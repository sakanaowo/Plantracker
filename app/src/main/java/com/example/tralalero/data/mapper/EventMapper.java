package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.event.EventDTO;
import com.example.tralalero.domain.model.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class EventMapper {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
    );

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static Event toDomain(EventDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Event(
                dto.getId(),
                dto.getProjectId(),
                dto.getTitle(),
                parseDate(dto.getStartAt()),
                parseDate(dto.getEndAt()),
                dto.getLocation(),
                dto.getMeetLink(),
                dto.getCreatedBy(),
                parseDate(dto.getCreatedAt()),
                parseDate(dto.getUpdatedAt())
        );
    }

    public static EventDTO toDTO(Event event) {
        if (event == null) {
            return null;
        }

        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setProjectId(event.getProjectId());
        dto.setTitle(event.getTitle());
        dto.setStartAt(formatDate(event.getStartAt()));
        dto.setEndAt(formatDate(event.getEndAt()));
        dto.setLocation(event.getLocation());
        dto.setMeetLink(event.getMeetLink());
        dto.setCreatedBy(event.getCreatedBy());
        dto.setCreatedAt(formatDate(event.getCreatedAt()));
        dto.setUpdatedAt(formatDate(event.getUpdatedAt()));

        return dto;
    }

    public static List<Event> toDomainList(List<EventDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<Event> events = new ArrayList<>();
        for (EventDTO dto : dtos) {
            Event event = toDomain(dto);
            if (event != null) {
                events.add(event);
            }
        }
        return events;
    }

    public static List<EventDTO> toDTOList(List<Event> events) {
        if (events == null) {
            return new ArrayList<>();
        }

        List<EventDTO> dtos = new ArrayList<>();
        for (Event event : events) {
            EventDTO dto = toDTO(event);
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

