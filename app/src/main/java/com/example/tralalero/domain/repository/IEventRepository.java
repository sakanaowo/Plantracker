package com.example.tralalero.domain.repository;

import com.example.tralalero.data.dto.event.CreateProjectEventRequest;
import com.example.tralalero.data.remote.dto.event.EventDTO;
import com.example.tralalero.domain.model.Event;

import java.util.Date;
import java.util.List;

public interface IEventRepository {
    void getEventsByProject(String projectId, RepositoryCallback<List<Event>> callback);
    
    void getEventsByProject(String projectId, String filter, RepositoryCallback<List<Event>> callback);

    void getEventById(String eventId, RepositoryCallback<Event> callback);

    void getEventsByDateRange(String projectId, Date startDate, Date endDate, RepositoryCallback<List<Event>> callback);

    void getUpcomingEvents(String projectId, RepositoryCallback<List<Event>> callback);

    void createEvent(String projectId, Event event, RepositoryCallback<Event> callback);

    void updateEvent(String eventId, Event event, RepositoryCallback<Event> callback);

    void deleteEvent(String eventId, RepositoryCallback<Void> callback);

    void addParticipant(String eventId, String email, RepositoryCallback<Void> callback);

    void removeParticipant(String eventId, String participantId, RepositoryCallback<Void> callback);
    
    // ==================== NEW PROJECT EVENT METHODS ====================
    
    /**
     * Get project events with filter (UPCOMING, PAST, RECURRING)
     */
    void getProjectEvents(String projectId, String filter, RepositoryCallback<List<EventDTO>> callback);
    
    /**
     * Create a project event with Google Meet option
     */
    void createProjectEvent(CreateProjectEventRequest request, RepositoryCallback<EventDTO> callback);
    
    /**
     * Update a project event
     */
    void updateProjectEvent(String eventId, CreateProjectEventRequest request, RepositoryCallback<EventDTO> callback);
    
    /**
     * Delete a project event
     */
    void deleteProjectEvent(String eventId, RepositoryCallback<Void> callback);

    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

