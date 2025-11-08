package com.example.tralalero.feature.home.ui.Home.event;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tralalero.data.dto.event.CreateProjectEventRequest;
import com.example.tralalero.data.remote.dto.event.EventDTO;
import com.example.tralalero.data.repository.EventRepositoryImpl;
import com.example.tralalero.domain.model.EventType;
import com.example.tralalero.domain.model.RecurrenceType;
import com.example.tralalero.domain.repository.IEventRepository;
import com.example.tralalero.util.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel for Quick Event Creation feature
 * Handles business logic for creating, updating, and deleting project events
 */
public class QuickEventViewModel extends AndroidViewModel {
    
    private static final String TAG = "QuickEventVM";
    
    private final IEventRepository repository;
    
    // LiveData for events list
    private final MutableLiveData<List<EventDTO>> events = new MutableLiveData<>(new ArrayList<>());
    
    // LiveData for loading state
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    // LiveData for error messages
    private final MutableLiveData<String> error = new MutableLiveData<>();
    
    // LiveData for event creation success
    private final MutableLiveData<EventDTO> eventCreated = new MutableLiveData<>();
    
    // LiveData for event deletion success
    private final MutableLiveData<Boolean> eventDeleted = new MutableLiveData<>();
    
    public QuickEventViewModel(@NonNull Application application) {
        super(application);
        this.repository = new EventRepositoryImpl(application);
    }
    
    // Getters for LiveData
    public LiveData<List<EventDTO>> getEvents() {
        return events;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    public LiveData<EventDTO> getEventCreated() {
        return eventCreated;
    }
    
    public LiveData<Boolean> getEventDeleted() {
        return eventDeleted;
    }
    
    /**
     * Load events for a project with filter
     * @param projectId Project ID
     * @param filter UPCOMING, PAST, or RECURRING
     */
    public void loadProjectEvents(String projectId, String filter) {
        if (projectId == null || projectId.isEmpty()) {
            error.setValue("Project ID is required");
            return;
        }
        
        isLoading.setValue(true);
        error.setValue(null);
        
        Log.d(TAG, "Loading events for project: " + projectId + ", filter: " + filter);
        
        repository.getProjectEvents(projectId, filter, new IEventRepository.RepositoryCallback<List<EventDTO>>() {
            @Override
            public void onSuccess(List<EventDTO> data) {
                isLoading.postValue(false);
                events.postValue(data);
                Log.d(TAG, "Loaded " + data.size() + " events");
            }
            
            @Override
            public void onError(String errorMsg) {
                isLoading.postValue(false);
                error.postValue(errorMsg);
                events.postValue(new ArrayList<>());
            }
        });
    }
    
    /**
     * Create a quick event
     */
    public void createEvent(
        String projectId,
        String title,
        String description,
        Calendar date,
        int durationMinutes,
        String type,
        String recurrence,
        List<String> attendeeIds,
        boolean createGoogleMeet
    ) {
        // Validation
        if (projectId == null || projectId.isEmpty()) {
            error.setValue("Project ID is required");
            return;
        }
        
        if (title == null || title.trim().isEmpty()) {
            error.setValue("Event title is required");
            return;
        }
        
        if (date == null) {
            error.setValue("Event date is required");
            return;
        }
        
        isLoading.setValue(true);
        error.setValue(null);
        
        // Format date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
        
        String dateStr = dateFormat.format(date.getTime());
        String timeStr = timeFormat.format(date.getTime());
        
        CreateProjectEventRequest request = new CreateProjectEventRequest();
        request.setProjectId(projectId);
        request.setTitle(title);
        request.setDescription(description);
        request.setDate(dateStr);
        request.setTime(timeStr);
        request.setDuration(durationMinutes);
        
        // Use enums with fallback to string values
        EventType eventType = EventType.fromString(type);
        request.setType(eventType.name());
        
        RecurrenceType recurrenceType = RecurrenceType.fromString(recurrence);
        request.setRecurrence(recurrenceType.name());
        
        request.setAttendeeIds(attendeeIds != null ? attendeeIds : new ArrayList<>());
        request.setCreateGoogleMeet(createGoogleMeet);
        
        Log.d(TAG, "Creating event: " + title);
        
        repository.createProjectEvent(request, new IEventRepository.RepositoryCallback<EventDTO>() {
            @Override
            public void onSuccess(EventDTO data) {
                isLoading.postValue(false);
                eventCreated.postValue(data);
                Log.d(TAG, "Event created with ID: " + data.getId());
            }
            
            @Override
            public void onError(String errorMsg) {
                isLoading.postValue(false);
                error.postValue(errorMsg);
            }
        });
    }
    
    /**
     * Update an existing event
     */
    public void updateEvent(
        String eventId,
        String title,
        String description,
        Calendar date,
        int durationMinutes,
        List<String> attendeeIds
    ) {
        if (eventId == null || eventId.isEmpty()) {
            error.setValue("Event ID is required");
            return;
        }
        
        isLoading.setValue(true);
        error.setValue(null);
        
        // Format date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
        
        CreateProjectEventRequest request = new CreateProjectEventRequest();
        if (title != null) request.setTitle(title);
        if (description != null) request.setDescription(description);
        if (date != null) {
            request.setDate(dateFormat.format(date.getTime()));
            request.setTime(timeFormat.format(date.getTime()));
        }
        if (durationMinutes > 0) request.setDuration(durationMinutes);
        if (attendeeIds != null) request.setAttendeeIds(attendeeIds);
        
        Log.d(TAG, "Updating event: " + eventId);
        
        repository.updateProjectEvent(eventId, request, new IEventRepository.RepositoryCallback<EventDTO>() {
            @Override
            public void onSuccess(EventDTO data) {
                isLoading.postValue(false);
                eventCreated.postValue(data); // Reuse same LiveData for update
                Log.d(TAG, "Event updated: " + eventId);
            }
            
            @Override
            public void onError(String errorMsg) {
                isLoading.postValue(false);
                error.postValue(errorMsg);
            }
        });
    }
    
    /**
     * Delete an event
     */
    public void deleteEvent(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            error.setValue("Event ID is required");
            return;
        }
        
        isLoading.setValue(true);
        error.setValue(null);
        
        Log.d(TAG, "Deleting event: " + eventId);
        
        repository.deleteProjectEvent(eventId, new IEventRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                isLoading.postValue(false);
                eventDeleted.postValue(true);
                Log.d(TAG, "Event deleted: " + eventId);
            }
            
            @Override
            public void onError(String errorMsg) {
                isLoading.postValue(false);
                error.postValue(errorMsg);
                eventDeleted.postValue(false);
            }
        });
    }
    
    /**
     * Clear error message
     */
    public void clearError() {
        error.setValue(null);
    }
}
