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
import com.example.tralalero.data.remote.dto.task.TaskCalendarSyncRequest;
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
public class    TaskCalendarSyncViewModel extends AndroidViewModel {
    
    private static final String TAG = "TaskCalendarSyncVM";
    
    private final TaskApiService taskApiService;
    
    private final MutableLiveData<TaskDTO> syncUpdatedTask = new MutableLiveData<>();
    private final MutableLiveData<String> syncError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUpdating = new MutableLiveData<>(false);
    
    private final MutableLiveData<List<TaskDTO>> calendarTasks = new MutableLiveData<>();
    private final MutableLiveData<String> fetchError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFetching = new MutableLiveData<>(false);
    
    public TaskCalendarSyncViewModel(@NonNull Application application) {
        super(application);
        taskApiService = ApiClient.get(App.authManager).create(TaskApiService.class);
    }
        
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
    
    public void updateCalendarSync(String taskId, boolean enabled, int reminderMinutes, String dueAt) {
        isUpdating.setValue(true);
        syncError.setValue(null);
        
        Log.d(TAG, "Updating calendar sync for task " + taskId + 
                   ": enabled=" + enabled + ", reminder=" + reminderMinutes + ", dueAt=" + dueAt);
        
        TaskCalendarSyncRequest syncData = new TaskCalendarSyncRequest();
        syncData.setCalendarReminderEnabled(enabled);
        syncData.setCalendarReminderTime(reminderMinutes);
        if (dueAt != null) {
            syncData.setDueAt(dueAt);
        }
        
        taskApiService.updateCalendarSync(taskId, syncData).enqueue(new Callback<>() {
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
}
