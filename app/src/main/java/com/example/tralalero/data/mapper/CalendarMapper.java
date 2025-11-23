package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.calendar.CalendarConnectionStatusResponse;
import com.example.tralalero.data.remote.dto.calendar.CalendarEventDTO;
import com.example.tralalero.data.remote.dto.calendar.CalendarSyncResponse;
import com.example.tralalero.domain.model.CalendarConnection;
import com.example.tralalero.domain.model.CalendarEvent;
import com.example.tralalero.domain.model.CalendarSyncResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for converting between Calendar DTOs and Domain models
 */
public class CalendarMapper {

    /**
     * Convert CalendarConnectionStatusResponse to CalendarConnection domain model
     */
    public static CalendarConnection toDomain(CalendarConnectionStatusResponse dto) {
        if (dto == null) {
            return null;
        }

        return new CalendarConnection(
            dto.isConnected(),
            dto.getEmail(),
            dto.getLastSyncAt(),
            dto.getProvider()
        );
    }

    /**
     * Convert CalendarEventDTO to CalendarEvent domain model
     */
    public static CalendarEvent toDomain(CalendarEventDTO dto) {
        if (dto == null) {
            return null;
        }

        CalendarEvent event = new CalendarEvent();
        event.setId(dto.getId());
        event.setGoogleEventId(dto.getGoogleEventId());
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartAt(dto.getStartAt());
        event.setEndAt(dto.getEndAt());
        event.setLocation(dto.getLocation());
        event.setMeetLink(dto.getMeetLink());
        event.setAttendees(dto.getAttendees());
        event.setStatus(dto.getStatus());
        event.setCreatedAt(dto.getCreatedAt());
        event.setUpdatedAt(dto.getUpdatedAt());
        event.setProjectId(dto.getProjectId()); // ✅ Map projectId
        event.setEventType(dto.getEventType()); // ✅ Map eventType

        return event;
    }

    /**
     * Convert list of CalendarEventDTO to list of CalendarEvent
     */
    public static List<CalendarEvent> toDomainList(List<CalendarEventDTO> dtoList) {
        if (dtoList == null) {
            return null;
        }

        List<CalendarEvent> events = new ArrayList<>();
        for (CalendarEventDTO dto : dtoList) {
            events.add(toDomain(dto));
        }
        return events;
    }

    /**
     * Convert CalendarSyncResponse to CalendarSyncResult domain model
     */
    public static CalendarSyncResult toDomain(CalendarSyncResponse dto) {
        if (dto == null) {
            return null;
        }

        List<CalendarSyncResult.EventSyncResult> results = new ArrayList<>();
        if (dto.getResults() != null) {
            for (CalendarSyncResponse.SyncResult syncResult : dto.getResults()) {
                results.add(new CalendarSyncResult.EventSyncResult(
                    syncResult.getEventId(),
                    syncResult.isSuccess(),
                    syncResult.getGoogleEventId(),
                    syncResult.getError()
                ));
            }
        }

        return new CalendarSyncResult(
            dto.isSuccess(),
            dto.getSyncedCount(),
            dto.getFailedCount(),
            results
        );
    }

    /**
     * Convert CalendarEvent to CalendarEventDTO
     */
    public static CalendarEventDTO toDto(CalendarEvent event) {
        if (event == null) {
            return null;
        }

        CalendarEventDTO dto = new CalendarEventDTO();
        dto.setId(event.getId());
        dto.setGoogleEventId(event.getGoogleEventId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setStartAt(event.getStartAt());
        dto.setEndAt(event.getEndAt());
        dto.setLocation(event.getLocation());
        dto.setMeetLink(event.getMeetLink());
        dto.setAttendees(event.getAttendees());
        dto.setStatus(event.getStatus());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        dto.setProjectId(event.getProjectId()); // ✅ Map projectId
        dto.setEventType(event.getEventType()); // ✅ Map eventType

        return dto;
    }

    /**
     * Convert list of CalendarEvent to list of CalendarEventDTO
     */
    public static List<CalendarEventDTO> toDtoList(List<CalendarEvent> events) {
        if (events == null) {
            return null;
        }

        List<CalendarEventDTO> dtoList = new ArrayList<>();
        for (CalendarEvent event : events) {
            dtoList.add(toDto(event));
        }
        return dtoList;
    }
}
