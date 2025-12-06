package com.example.tralalero.data.repository;

import android.content.Context;

import com.example.tralalero.data.mapper.EventMapper;
import com.example.tralalero.data.remote.api.EventApiService;
import com.example.tralalero.data.remote.dto.event.EventDTO;
import com.example.tralalero.domain.model.Event;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.domain.repository.IEventRepository;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventRepositoryImpl implements IEventRepository {
    private final EventApiService apiService;

    public EventRepositoryImpl(EventApiService apiService) {
        this.apiService = apiService;
    }
    
    public EventRepositoryImpl(Context context) {
        this.apiService = ApiClient.get().create(EventApiService.class);
    }

    @Override
    public void getEventsByProject(String projectId, RepositoryCallback<List<Event>> callback) {
        // Default: no filter (get all events)
        getEventsByProject(projectId, null, callback);
    }
    
    @Override
    public void getEventsByProject(String projectId, String filter, RepositoryCallback<List<Event>> callback) {
        apiService.getEventsByProject(projectId, filter).enqueue(new Callback<List<EventDTO>>() {
            @Override
            public void onResponse(Call<List<EventDTO>> call, Response<List<EventDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(EventMapper.toDomainList(response.body()));
                } else {
                    callback.onError("Failed to fetch events: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<EventDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getEventById(String eventId, RepositoryCallback<Event> callback) {
        apiService.getEventById(eventId).enqueue(new Callback<EventDTO>() {
            @Override
            public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(EventMapper.toDomain(response.body()));
                } else {
                    callback.onError("Event not found: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<EventDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getEventsByDateRange(String projectId, Date startDate, Date endDate, RepositoryCallback<List<Event>> callback) {
        callback.onError("Get events by date range not yet implemented in API");
    }

    @Override
    public void getUpcomingEvents(String projectId, RepositoryCallback<List<Event>> callback) {
        callback.onError("Get upcoming events not yet implemented in API");
    }

    @Override
    public void createEvent(String projectId, Event event, RepositoryCallback<Event> callback) {
        EventDTO dto = EventMapper.toDto(event);
        dto.setProjectId(projectId);

        apiService.createEvent(dto).enqueue(new Callback<EventDTO>() {
            @Override
            public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(EventMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to create event: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<EventDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void updateEvent(String eventId, Event event, RepositoryCallback<Event> callback) {
        EventDTO dto = EventMapper.toDto(event);

        apiService.updateEvent(eventId, dto).enqueue(new Callback<EventDTO>() {
            @Override
            public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(EventMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to update event: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<EventDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void deleteEvent(String eventId, RepositoryCallback<Void> callback) {
        apiService.deleteEvent(eventId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to delete event: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void addParticipant(String eventId, String email, RepositoryCallback<Void> callback) {
        callback.onError("Add participant not yet implemented in API");
    }

    @Override
    public void removeParticipant(String eventId, String participantId, RepositoryCallback<Void> callback) {
        callback.onError("Remove participant not yet implemented in API");
    }
    
    // ==================== NEW PROJECT EVENT METHODS ====================
    
    @Override
    public void getProjectEvents(String projectId, String filter, RepositoryCallback<List<EventDTO>> callback) {
        apiService.getProjectEvents(projectId, filter).enqueue(new Callback<List<EventDTO>>() {
            @Override
            public void onResponse(Call<List<EventDTO>> call, Response<List<EventDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch project events: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<EventDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    @Override
    public void createProjectEvent(com.example.tralalero.data.dto.event.CreateProjectEventRequest request, 
                                   RepositoryCallback<EventDTO> callback) {
        apiService.createProjectEvent(request).enqueue(new Callback<EventDTO>() {
            @Override
            public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to create project event: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<EventDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    @Override
    public void updateProjectEvent(String eventId, 
                                  com.example.tralalero.data.dto.event.CreateProjectEventRequest request,
                                  RepositoryCallback<EventDTO> callback) {
        apiService.updateProjectEvent(eventId, request).enqueue(new Callback<EventDTO>() {
            @Override
            public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to update project event: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<EventDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    @Override
    public void deleteProjectEvent(String eventId, RepositoryCallback<Void> callback) {
        apiService.deleteProjectEvent(eventId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to delete project event: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    // ==================== CANCEL/RESTORE EVENT ====================
    
    @Override
    public void cancelEvent(String projectId, String eventId, String reason, 
                           RepositoryCallback<EventDTO> callback) {
        EventApiService.CancelEventRequest request = new EventApiService.CancelEventRequest(reason);
        apiService.cancelEvent(projectId, eventId, request).enqueue(new Callback<EventDTO>() {
            @Override
            public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to cancel event: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<EventDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    @Override
    public void restoreEvent(String projectId, String eventId, RepositoryCallback<EventDTO> callback) {
        apiService.restoreEvent(projectId, eventId).enqueue(new Callback<EventDTO>() {
            @Override
            public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to restore event: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<EventDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    @Override
    public void hardDeleteEvent(String projectId, String eventId, RepositoryCallback<Void> callback) {
        apiService.hardDeleteEvent(projectId, eventId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to permanently delete event: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}
