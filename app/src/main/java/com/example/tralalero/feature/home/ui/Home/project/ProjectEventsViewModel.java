package com.example.tralalero.feature.home.ui.Home.project;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tralalero.App.App;
import com.example.tralalero.data.remote.api.EventApiService;
import com.example.tralalero.data.remote.dto.event.EventDTO;
import com.example.tralalero.data.dto.event.CreateProjectEventRequest;
import com.example.tralalero.domain.model.CreateEventRequest;
import com.example.tralalero.domain.model.ProjectEvent;
import com.example.tralalero.domain.model.UpdateEventRequest;
import com.example.tralalero.network.ApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel for managing project events data
 */
public class ProjectEventsViewModel extends ViewModel {
    private final EventApiService eventApiService;
    private final MutableLiveData<Result<List<ProjectEvent>>> eventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result<ProjectEvent>> eventDetailLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<ProjectEvent> createEventSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    
    public enum EventFilter {
        UPCOMING, PAST, RECURRING, ALL
    }
    
    public ProjectEventsViewModel() {
        eventApiService = ApiClient.get(App.authManager).create(EventApiService.class);
    }
    
    /**
     * Load project events with filter
     */
    public LiveData<Result<List<ProjectEvent>>> loadProjectEvents(String projectId, EventFilter filter) {
        loadingLiveData.setValue(true);
        
        String filterStr = filter != null ? filter.name() : EventFilter.ALL.name();
        
        eventApiService.getEventsByProject(projectId, filterStr).enqueue(new Callback<List<EventDTO>>() {
            @Override
            public void onResponse(Call<List<EventDTO>> call, Response<List<EventDTO>> response) {
                loadingLiveData.setValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<ProjectEvent> events = convertDTOListToEvents(response.body());
                    eventsLiveData.setValue(Result.success(events));
                } else {
                    eventsLiveData.setValue(Result.error("Không thể tải danh sách sự kiện"));
                }
            }
            
            @Override
            public void onFailure(Call<List<EventDTO>> call, Throwable t) {
                loadingLiveData.setValue(false);
                eventsLiveData.setValue(Result.error("Lỗi kết nối: " + t.getMessage()));
            }
        });
        
        return eventsLiveData;
    }
    
    /**
     * Load event details by ID
     */
    public LiveData<Result<ProjectEvent>> loadEventDetails(String eventId) {
        loadingLiveData.setValue(true);
        
        eventApiService.getEventById(eventId).enqueue(new Callback<EventDTO>() {
            @Override
            public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                loadingLiveData.setValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ProjectEvent event = convertDTOToEvent(response.body());
                    eventDetailLiveData.setValue(Result.success(event));
                } else {
                    eventDetailLiveData.setValue(Result.error("Không thể tải chi tiết sự kiện"));
                }
            }
            
            @Override
            public void onFailure(Call<EventDTO> call, Throwable t) {
                loadingLiveData.setValue(false);
                eventDetailLiveData.setValue(Result.error("Lỗi kết nối: " + t.getMessage()));
            }
        });
        
        return eventDetailLiveData;
    }
    
    /**
     * Create new event using the correct POST /events/projects endpoint
     * This endpoint supports Google Calendar integration and participant notifications
     */
    public LiveData<Result<ProjectEvent>> createEvent(CreateEventRequest request) {
        MutableLiveData<Result<ProjectEvent>> resultLiveData = new MutableLiveData<>();
        loadingLiveData.setValue(true);
        error.setValue(null);
        
        // ✅ FIX: Convert to CreateProjectEventRequest for POST /events/projects endpoint
        CreateProjectEventRequest projectEventRequest = convertToProjectEventRequest(request);
        
        // ✅ FIX: Call createProjectEvent() instead of createEvent()
        eventApiService.createProjectEvent(projectEventRequest).enqueue(new Callback<EventDTO>() {
            @Override
            public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                loadingLiveData.setValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ProjectEvent event = convertDTOToEvent(response.body());
                    resultLiveData.setValue(Result.success(event));
                    createEventSuccess.setValue(event);
                } else {
                    String errorMsg = "Không thể tạo sự kiện";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            errorMsg += ": " + errorBody;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    resultLiveData.setValue(Result.error(errorMsg));
                    error.setValue(errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<EventDTO> call, Throwable t) {
                loadingLiveData.setValue(false);
                String errorMsg = "Lỗi kết nối: " + t.getMessage();
                resultLiveData.setValue(Result.error(errorMsg));
                error.setValue(errorMsg);
            }
        });
        
        return resultLiveData;
    }
    
    /**
     * Update event
     */
    public LiveData<Result<ProjectEvent>> updateEvent(String eventId, UpdateEventRequest request) {
        MutableLiveData<Result<ProjectEvent>> resultLiveData = new MutableLiveData<>();
        loadingLiveData.setValue(true);
        
        EventDTO eventDTO = convertUpdateRequestToDTO(request);
        
        eventApiService.updateEvent(eventId, eventDTO).enqueue(new Callback<EventDTO>() {
            @Override
            public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                loadingLiveData.setValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ProjectEvent event = convertDTOToEvent(response.body());
                    resultLiveData.setValue(Result.success(event));
                } else {
                    resultLiveData.setValue(Result.error("Không thể cập nhật sự kiện"));
                }
            }
            
            @Override
            public void onFailure(Call<EventDTO> call, Throwable t) {
                loadingLiveData.setValue(false);
                resultLiveData.setValue(Result.error("Lỗi kết nối: " + t.getMessage()));
            }
        });
        
        return resultLiveData;
    }
    
    /**
     * Delete event
     */
    public LiveData<Result<Void>> deleteEvent(String eventId) {
        MutableLiveData<Result<Void>> resultLiveData = new MutableLiveData<>();
        loadingLiveData.setValue(true);
        
        eventApiService.deleteEvent(eventId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                loadingLiveData.setValue(false);
                
                if (response.isSuccessful()) {
                    resultLiveData.setValue(Result.success(null));
                } else {
                    resultLiveData.setValue(Result.error("Không thể xóa sự kiện"));
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                loadingLiveData.setValue(false);
                resultLiveData.setValue(Result.error("Lỗi kết nối: " + t.getMessage()));
            }
        });
        
        return resultLiveData;
    }
    
    /**
     * Hard delete event (permanent)
     */
    public LiveData<Result<Void>> hardDeleteEvent(String eventId) {
        MutableLiveData<Result<Void>> resultLiveData = new MutableLiveData<>();
        loadingLiveData.setValue(true);
        
        // Get event first to find projectId
        eventApiService.getEventById(eventId).enqueue(new Callback<EventDTO>() {
            @Override
            public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String projectId = response.body().getProjectId();
                    
                    eventApiService.hardDeleteEvent(projectId, eventId).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            loadingLiveData.setValue(false);
                            
                            if (response.isSuccessful()) {
                                resultLiveData.setValue(Result.success(null));
                            } else {
                                resultLiveData.setValue(Result.error("Không thể xóa vĩnh viễn sự kiện"));
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            loadingLiveData.setValue(false);
                            resultLiveData.setValue(Result.error("Lỗi kết nối: " + t.getMessage()));
                        }
                    });
                } else {
                    loadingLiveData.setValue(false);
                    resultLiveData.setValue(Result.error("Không thể tìm thấy sự kiện"));
                }
            }
            
            @Override
            public void onFailure(Call<EventDTO> call, Throwable t) {
                loadingLiveData.setValue(false);
                resultLiveData.setValue(Result.error("Lỗi kết nối: " + t.getMessage()));
            }
        });
        
        return resultLiveData;
    }
    
    /**
     * Send reminder to attendees
     */
    public LiveData<Result<Void>> sendReminder(String eventId) {
        MutableLiveData<Result<Void>> resultLiveData = new MutableLiveData<>();
        loadingLiveData.setValue(true);
        
        eventApiService.sendReminder(eventId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                loadingLiveData.setValue(false);
                
                if (response.isSuccessful()) {
                    resultLiveData.setValue(Result.success(null));
                } else {
                    resultLiveData.setValue(Result.error("Không thể gửi nhắc nhở"));
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                loadingLiveData.setValue(false);
                resultLiveData.setValue(Result.error("Lỗi kết nối: " + t.getMessage()));
            }
        });
        
        return resultLiveData;
    }
    
    public LiveData<Boolean> getLoadingState() {
        return loadingLiveData;
    }
    
    public LiveData<ProjectEvent> getCreateEventSuccess() {
        return createEventSuccess;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    // Mapper methods
    private List<ProjectEvent> convertDTOListToEvents(List<EventDTO> dtoList) {
        List<ProjectEvent> events = new ArrayList<>();
        if (dtoList != null) {
            for (EventDTO dto : dtoList) {
                events.add(convertDTOToEvent(dto));
            }
        }
        return events;
    }
    
    private ProjectEvent convertDTOToEvent(EventDTO dto) {
        ProjectEvent event = new ProjectEvent();
        event.setId(dto.getId());
        event.setProjectId(dto.getProjectId());
        event.setTitle(dto.getTitle());
        event.setMeetLink(dto.getMeetLink());
        event.setCreatedBy(dto.getCreatedBy());
        
        // ✅ CRITICAL FIX: Map the raw ISO 8601 strings for sorting
        event.setStartAt(dto.getStartAt());
        event.setEndAt(dto.getEndAt());
        
        // Parse dates from ISO strings
        try {
            // ✅ FIX: Handle both UTC format (with .000Z) and local format (without Z)
            // Backend returns: "2025-12-05T17:00:00.000Z" (UTC)
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            // Fallback parser for local time format (without timezone)
            SimpleDateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            
            if (dto.getStartAt() != null) {
                Date startDate = null;
                try {
                    // Try UTC format first
                    startDate = isoFormat.parse(dto.getStartAt());
                } catch (Exception e) {
                    // Fallback to local format
                    startDate = localFormat.parse(dto.getStartAt());
                }
                event.setDate(startDate);
                
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                event.setTime(timeFormat.format(startDate));
            }
            
            if (dto.getEndAt() != null && dto.getStartAt() != null) {
                Date startDate = null;
                Date endDate = null;
                try {
                    // Try UTC format first
                    startDate = isoFormat.parse(dto.getStartAt());
                    endDate = isoFormat.parse(dto.getEndAt());
                } catch (Exception e) {
                    // Fallback to local format
                    startDate = localFormat.parse(dto.getStartAt());
                    endDate = localFormat.parse(dto.getEndAt());
                }
                long durationMillis = endDate.getTime() - startDate.getTime();
                int durationMinutes = (int) (durationMillis / (1000 * 60));
                event.setDuration(durationMinutes);
            }
            
            if (dto.getCreatedAt() != null) {
                Date createdDate = null;
                try {
                    createdDate = isoFormat.parse(dto.getCreatedAt());
                } catch (Exception e) {
                    createdDate = localFormat.parse(dto.getCreatedAt());
                }
                event.setCreatedAt(createdDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // ✅ FIX: Parse attendee count from participants array
        int attendeeCount = 0;
        if (dto.getParticipants() != null) {
            attendeeCount = dto.getParticipants().size();
        }
        
        // ✅ FIX: Map status field from DTO
        event.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        
        // Set defaults for fields not in DTO
        event.setType("MEETING");
        event.setRecurrence("NONE");
        event.setAttendeeCount(attendeeCount);
        event.setCreateGoogleMeet(dto.getMeetLink() != null && !dto.getMeetLink().isEmpty());
        
        return event;
    }
    
    /**
     * Convert CreateEventRequest to CreateProjectEventRequest for POST /events/projects
     * This method extracts date, time, and duration from ISO 8601 startAt/endAt fields
     */
    private CreateProjectEventRequest convertToProjectEventRequest(CreateEventRequest request) {
        CreateProjectEventRequest dto = new CreateProjectEventRequest();
        dto.setProjectId(request.getProjectId());
        dto.setTitle(request.getTitle());
        dto.setDescription(request.getDescription());
        dto.setType(request.getType() != null ? request.getType() : "MEETING");
        dto.setRecurrence(request.getRecurrence() != null ? request.getRecurrence() : "NONE");
        dto.setAttendeeIds(request.getAttendeeIds());
        dto.setCreateGoogleMeet(request.isCreateGoogleMeet());
        
        // ✅ SIMPLIFIED: Use date/time directly from request
        // FE sends: date="2025-12-07", time="19:18", duration=60
        // BE will combine: "2025-12-07T19:18:00+07:00"
        dto.setDate(request.getDate());  // yyyy-MM-dd
        dto.setTime(request.getTime());  // HH:mm
        dto.setDuration(request.getDuration());
        
        android.util.Log.d("ProjectEventsVM", "📦 CreateProjectEventRequest:");
        android.util.Log.d("ProjectEventsVM", "  date: " + dto.getDate());
        android.util.Log.d("ProjectEventsVM", "  time: " + dto.getTime());
        android.util.Log.d("ProjectEventsVM", "  duration: " + dto.getDuration());
        
        return dto;
    }
    
    /**
     * @deprecated Use convertToProjectEventRequest() instead
     * This method is kept for backward compatibility but should not be used
     */
    @Deprecated
    private EventDTO convertCreateRequestToDTO(CreateEventRequest request) {
        EventDTO dto = new EventDTO();
        dto.setProjectId(request.getProjectId());
        dto.setTitle(request.getTitle());
        
        // ✅ FIX: Use startAt/endAt from request directly (already ISO 8601 formatted)
        if (request.getStartAt() != null && request.getEndAt() != null) {
            dto.setStartAt(request.getStartAt());
            dto.setEndAt(request.getEndAt());
        } else {
            // Fallback: Convert old date/time format (for backward compatibility)
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                inputFormat.setTimeZone(TimeZone.getDefault());
                Date startDate = inputFormat.parse(request.getDate() + " " + request.getTime());
                
                // ✅ FIX: Format with 'Z' timezone suffix (ISO 8601 UTC)
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                dto.setStartAt(isoFormat.format(startDate));
                
                // Calculate endAt based on duration
                if (request.getDuration() > 0) {
                    long endTime = startDate.getTime() + (request.getDuration() * 60 * 1000);
                    dto.setEndAt(isoFormat.format(new Date(endTime)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // ✅ FIX: Set location and meetLink properly
        dto.setLocation(request.getLocation());
        dto.setMeetLink(request.getMeetingLink());
        
        return dto;
    }
    
    private EventDTO convertUpdateRequestToDTO(UpdateEventRequest request) {
        EventDTO dto = new EventDTO();
        dto.setTitle(request.getTitle());
        
        // Convert date + time to ISO format
        try {
            if (request.getDate() != null && request.getTime() != null) {
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date startDate = inputFormat.parse(request.getDate() + " " + request.getTime());
                
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                dto.setStartAt(isoFormat.format(startDate));
                
                if (request.getDuration() != null && request.getDuration() > 0) {
                    long endTime = startDate.getTime() + (request.getDuration() * 60 * 1000);
                    dto.setEndAt(isoFormat.format(new Date(endTime)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        dto.setLocation(request.getDescription());
        
        return dto;
    }
    
    /**
     * Result wrapper class
     */
    public static class Result<T> {
        private final T data;
        private final String errorMessage;
        private final boolean isSuccess;
        
        private Result(T data, String errorMessage, boolean isSuccess) {
            this.data = data;
            this.errorMessage = errorMessage;
            this.isSuccess = isSuccess;
        }
        
        public static <T> Result<T> success(T data) {
            return new Result<>(data, null, true);
        }
        
        public static <T> Result<T> error(String errorMessage) {
            return new Result<>(null, errorMessage, false);
        }
        
        public T getData() {
            return data;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public boolean isSuccess() {
            return isSuccess;
        }
    }
}
