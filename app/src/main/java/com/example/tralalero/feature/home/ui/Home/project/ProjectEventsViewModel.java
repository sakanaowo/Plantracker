package com.example.tralalero.feature.home.ui.Home.project;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tralalero.App.App;
import com.example.tralalero.data.remote.api.EventApiService;
import com.example.tralalero.data.remote.dto.event.EventDTO;
import com.example.tralalero.domain.model.CreateEventRequest;
import com.example.tralalero.domain.model.ProjectEvent;
import com.example.tralalero.domain.model.UpdateEventRequest;
import com.example.tralalero.network.ApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
     * Create new event
     */
    public LiveData<Result<ProjectEvent>> createEvent(CreateEventRequest request) {
        MutableLiveData<Result<ProjectEvent>> resultLiveData = new MutableLiveData<>();
        loadingLiveData.setValue(true);
        error.setValue(null);
        
        EventDTO eventDTO = convertCreateRequestToDTO(request);
        
        eventApiService.createEvent(eventDTO).enqueue(new Callback<EventDTO>() {
            @Override
            public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                loadingLiveData.setValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ProjectEvent event = convertDTOToEvent(response.body());
                    resultLiveData.setValue(Result.success(event));
                    createEventSuccess.setValue(event);
                } else {
                    String errorMsg = "Không thể tạo sự kiện";
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
        
        // Parse dates from ISO strings
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            
            if (dto.getStartAt() != null) {
                Date startDate = isoFormat.parse(dto.getStartAt());
                event.setDate(startDate);
                
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                event.setTime(timeFormat.format(startDate));
            }
            
            if (dto.getEndAt() != null && dto.getStartAt() != null) {
                Date startDate = isoFormat.parse(dto.getStartAt());
                Date endDate = isoFormat.parse(dto.getEndAt());
                long durationMillis = endDate.getTime() - startDate.getTime();
                int durationMinutes = (int) (durationMillis / (1000 * 60));
                event.setDuration(durationMinutes);
            }
            
            if (dto.getCreatedAt() != null) {
                event.setCreatedAt(isoFormat.parse(dto.getCreatedAt()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Set defaults for fields not in DTO
        event.setType("MEETING");
        event.setRecurrence("NONE");
        event.setAttendeeCount(0);
        event.setCreateGoogleMeet(dto.getMeetLink() != null && !dto.getMeetLink().isEmpty());
        
        return event;
    }
    
    private EventDTO convertCreateRequestToDTO(CreateEventRequest request) {
        EventDTO dto = new EventDTO();
        dto.setProjectId(request.getProjectId());
        dto.setTitle(request.getTitle());
        
        // Convert date + time to ISO format for startAt
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date startDate = inputFormat.parse(request.getDate() + " " + request.getTime());
            
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            dto.setStartAt(isoFormat.format(startDate));
            
            // Calculate endAt based on duration
            if (request.getDuration() > 0) {
                long endTime = startDate.getTime() + (request.getDuration() * 60 * 1000);
                dto.setEndAt(isoFormat.format(new Date(endTime)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Set location from description for now
        dto.setLocation(request.getDescription());
        
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
