package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.Event;

import java.util.Date;
import java.util.List;

public interface IEventRepository {
    void getEventsByProject(String projectId, RepositoryCallback<List<Event>> callback);

    void getEventById(String eventId, RepositoryCallback<Event> callback);

    void getEventsByDateRange(String projectId, Date startDate, Date endDate, RepositoryCallback<List<Event>> callback);

    void getUpcomingEvents(String projectId, RepositoryCallback<List<Event>> callback);

    void createEvent(String projectId, Event event, RepositoryCallback<Event> callback);

    void updateEvent(String eventId, Event event, RepositoryCallback<Event> callback);

    void deleteEvent(String eventId, RepositoryCallback<Void> callback);

    void addParticipant(String eventId, String email, RepositoryCallback<Void> callback);

    void removeParticipant(String eventId, String participantId, RepositoryCallback<Void> callback);

    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

