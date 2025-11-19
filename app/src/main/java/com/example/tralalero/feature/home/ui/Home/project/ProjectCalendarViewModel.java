package com.example.tralalero.feature.home.ui.Home.project;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tralalero.domain.model.CalendarEvent;
import com.example.tralalero.domain.repository.ICalendarRepository;
import com.example.tralalero.data.repository.CalendarRepositoryImpl;
import com.example.tralalero.App.App;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel for ProjectCalendarFragment
 * Handles business logic and API calls for calendar functionality
 */
public class ProjectCalendarViewModel extends ViewModel {
    private static final String TAG = "ProjectCalendarVM";
    
    private final ICalendarRepository calendarRepository;
    private final MutableLiveData<List<CalendarEvent>> allEventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<CalendarEvent>> filteredEventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> syncSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> calendarNotConnectedLiveData = new MutableLiveData<>();
    
    private List<CalendarEvent> cachedEvents = new ArrayList<>();
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    public ProjectCalendarViewModel() {
        this.calendarRepository = new CalendarRepositoryImpl(App.getInstance());
    }
    
    // LiveData getters
    public LiveData<List<CalendarEvent>> getAllEvents() {
        return allEventsLiveData;
    }
    
    public LiveData<List<CalendarEvent>> getFilteredEvents() {
        return filteredEventsLiveData;
    }
    
    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }
    
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public LiveData<Boolean> getSyncSuccess() {
        return syncSuccessLiveData;
    }
    
    public LiveData<Boolean> getCalendarNotConnected() {
        return calendarNotConnectedLiveData;
    }
    
    /**
     * Load calendar events for a specific project and month
     */
    public void loadProjectCalendarEvents(String projectId, int year, int month) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        
        // Calculate time range for the month
        Calendar startCal = Calendar.getInstance();
        startCal.set(year, month - 1, 1, 0, 0, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        
        Calendar endCal = Calendar.getInstance();
        endCal.set(year, month - 1, 1, 0, 0, 0);
        endCal.add(Calendar.MONTH, 1);
        endCal.add(Calendar.DAY_OF_MONTH, -1);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        
        String timeMin = formatDateTime(startCal.getTime());
        String timeMax = formatDateTime(endCal.getTime());
        
        Log.d(TAG, "Loading events for project: " + projectId + " from " + timeMin + " to " + timeMax);
        
        calendarRepository.syncEventsFromGoogle(projectId, timeMin, timeMax, 
            new ICalendarRepository.RepositoryCallback<List<CalendarEvent>>() {
                @Override
                public void onSuccess(List<CalendarEvent> events) {
                    loadingLiveData.postValue(false);
                    cachedEvents = events != null ? events : new ArrayList<>();
                    allEventsLiveData.postValue(cachedEvents);
                    filteredEventsLiveData.postValue(cachedEvents);
                    Log.d(TAG, "Loaded " + cachedEvents.size() + " events");
                }
                
                @Override
                public void onError(String error) {
                    loadingLiveData.postValue(false);
                    
                    // Check if it's "not connected" case
                    if ("CALENDAR_NOT_CONNECTED".equals(error)) {
                        Log.w(TAG, "Google Calendar not connected - prompting user");
                        calendarNotConnectedLiveData.postValue(true);
                    } else {
                        errorLiveData.postValue(error);
                        Log.e(TAG, "Error loading events: " + error);
                    }
                    
                    // Fallback to empty list
                    cachedEvents = new ArrayList<>();
                    allEventsLiveData.postValue(cachedEvents);
                    filteredEventsLiveData.postValue(cachedEvents);
                }
            });
    }
    
    /**
     * Filter events for a specific date
     */
    public void filterEventsByDate(int year, int month, int dayOfMonth) {
        Calendar targetCal = Calendar.getInstance();
        targetCal.set(year, month, dayOfMonth, 0, 0, 0);
        targetCal.set(Calendar.MILLISECOND, 0);
        
        String targetDate = dateFormatter.format(targetCal.getTime());
        
        Log.d(TAG, "Filtering events for date: " + targetDate + " (" + year + "-" + (month+1) + "-" + dayOfMonth + ")");
        
        List<CalendarEvent> filtered = new ArrayList<>();
        for (CalendarEvent event : cachedEvents) {
            if (event.getStartAt() != null) {
                // Extract date part (first 10 chars: "yyyy-MM-dd")
                String eventDate = event.getStartAt().length() >= 10 ? 
                    event.getStartAt().substring(0, 10) : event.getStartAt();
                
                Log.d(TAG, "  Checking event: " + event.getTitle() + " on " + eventDate + " vs " + targetDate);
                
                if (eventDate.equals(targetDate)) {
                    filtered.add(event);
                    Log.d(TAG, "    ✓ Match!");
                }
            }
        }
        
        filteredEventsLiveData.setValue(filtered);
        Log.d(TAG, "Filtered " + filtered.size() + " events for date: " + targetDate);
    }
    
    /**
     * Get events for a specific month
     */
    public void filterEventsByMonth(int year, int month) {
        Calendar startCal = Calendar.getInstance();
        startCal.set(year, month, 1, 0, 0, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        
        String monthPrefix = String.format(Locale.US, "%04d-%02d", year, month + 1);
        
        List<CalendarEvent> filtered = new ArrayList<>();
        for (CalendarEvent event : cachedEvents) {
            if (event.getStartAt() != null && event.getStartAt().startsWith(monthPrefix)) {
                filtered.add(event);
            }
        }
        
        filteredEventsLiveData.setValue(filtered);
        Log.d(TAG, "Filtered " + filtered.size() + " events for month: " + monthPrefix);
    }
    
    /**
     * Sync with Google Calendar manually
     */
    public void syncWithGoogleCalendar(String projectId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        syncSuccessLiveData.setValue(false);
        
        // Get current month range
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        
        // Reload events after sync
        loadProjectCalendarEvents(projectId, year, month);
        
        // For now, just indicate success after loading
        // In real implementation, you would call a specific sync endpoint
        syncSuccessLiveData.postValue(true);
    }
    
    /**
     * Check if there are events for a specific date
     */
    public boolean hasEventsOnDate(int year, int month, int dayOfMonth) {
        Calendar targetCal = Calendar.getInstance();
        targetCal.set(year, month, dayOfMonth, 0, 0, 0);
        targetCal.set(Calendar.MILLISECOND, 0);
        
        String targetDate = dateFormatter.format(targetCal.getTime());
        
        for (CalendarEvent event : cachedEvents) {
            if (event.getStartAt() != null && event.getStartAt().startsWith(targetDate)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Clear error message
     */
    public void clearError() {
        errorLiveData.setValue(null);
    }
    
    /**
     * Format date to ISO 8601 format for API
     */
    private String formatDateTime(Date date) {
        // Use RFC3339 format with timezone as required by Google Calendar API
        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
        return iso8601Format.format(date);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "ViewModel cleared");
    }
}
