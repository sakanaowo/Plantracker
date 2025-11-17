package com.example.tralalero.feature.home.ui.Home.task;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tralalero.App.App;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.remote.dto.task.TaskDTO;
import com.example.tralalero.network.ApiClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel for Task Calendar Sync feature
 * 
 * Handles:
 * - Update task calendar sync settings
 * - Fetch tasks with calendar sync enabled
 * - Sync status tracking
 */
public class TaskCalendarSyncViewModel extends AndroidViewModel {
    
    private static final String TAG = "TaskCalendarSyncVM";
    
    private final TaskApiService taskApiService;
    
    // LiveData for sync update result
    private final MutableLiveData<TaskDTO> syncUpdatedTask = new MutableLiveData<>();
    private final MutableLiveData<String> syncError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUpdating = new MutableLiveData<>(false);
    
    // LiveData for calendar tasks
    private final MutableLiveData<List<TaskDTO>> calendarTasks = new MutableLiveData<>();
    private final MutableLiveData<String> fetchError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFetching = new MutableLiveData<>(false);
    
    public TaskCalendarSyncViewModel(@NonNull Application application) {
        super(application);
        
        // Initialize API service with AuthManager
        taskApiService = ApiClient.get(App.authManager).create(TaskApiService.class);
    }
    
    // ==================== GETTERS ====================
    
    public LiveData<TaskDTO> getSyncUpdatedTask() {
        return syncUpdatedTask;
    }
    
    public LiveData<String> getSyncError() {
        return syncError;
    }
    
    public LiveData<Boolean> getIsUpdating() {
        return isUpdating;
    }
    
    public LiveData<List<TaskDTO>> getCalendarTasks() {
        return calendarTasks;
    }
    
    public LiveData<String> getFetchError() {
        return fetchError;
    }
    
    public LiveData<Boolean> getIsFetching() {
        return isFetching;
    }
    
    // ==================== METHODS ====================
    
    /**
     * Update calendar sync settings for a task
     * 
     * @param taskId Task ID
     * @param enabled Enable/disable calendar sync
     * @param reminderMinutes Minutes before due date to remind (15/30/60)
     * @param dueAt Due date in ISO format (nullable)
     */
    public void updateCalendarSync(String taskId, boolean enabled, int reminderMinutes, String dueAt) {
        isUpdating.setValue(true);
        syncError.setValue(null);
        
        Log.d(TAG, "Updating calendar sync for task " + taskId + 
                   ": enabled=" + enabled + ", reminder=" + reminderMinutes + ", dueAt=" + dueAt);
        
        // Prepare request body
        Map<String, Object> syncData = new HashMap<>();
        syncData.put("calendarReminderEnabled", enabled);
        syncData.put("calendarReminderTime", reminderMinutes);
        if (dueAt != null) {
            syncData.put("dueAt", dueAt);
        }
        
        // API call
        taskApiService.updateCalendarSync(taskId, syncData).enqueue(new Callback<TaskDTO>() {
            @Override
            public void onResponse(@NonNull Call<TaskDTO> call, @NonNull Response<TaskDTO> response) {
                isUpdating.setValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    TaskDTO updatedTask = response.body();
                    Log.d(TAG, "Calendar sync updated successfully. Event ID: " + 
                               updatedTask.getCalendarEventId());
                    syncUpdatedTask.setValue(updatedTask);
                } else {
                    String error = "Failed to update calendar sync: " + response.code();
                    Log.e(TAG, error);
                    syncError.setValue(error);
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<TaskDTO> call, @NonNull Throwable t) {
                isUpdating.setValue(false);
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                syncError.setValue(error);
            }
        });
    }
    
    /**
     * Fetch tasks with calendar sync in date range
     * 
     * @param startDate Start date (ISO format: "2025-11-10")
     * @param endDate End date (ISO format: "2025-11-17")
     */
    public void fetchCalendarTasks(String startDate, String endDate) {
        isFetching.setValue(true);
        fetchError.setValue(null);
        
        Log.d(TAG, "Fetching calendar tasks: " + startDate + " to " + endDate);
        
        taskApiService.getCalendarTasks(startDate, endDate).enqueue(new Callback<List<TaskDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<TaskDTO>> call, @NonNull Response<List<TaskDTO>> response) {
                isFetching.setValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<TaskDTO> tasks = response.body();
                    Log.d(TAG, "Fetched " + tasks.size() + " calendar tasks");
                    calendarTasks.setValue(tasks);
                } else {
                    String error = "Failed to fetch calendar tasks: " + response.code();
                    Log.e(TAG, error);
                    fetchError.setValue(error);
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<List<TaskDTO>> call, @NonNull Throwable t) {
                isFetching.setValue(false);
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                fetchError.setValue(error);
            }
        });
    }
    
    /**
     * Clear error messages
     */
    public void clearErrors() {
        syncError.setValue(null);
        fetchError.setValue(null);
    }
}
