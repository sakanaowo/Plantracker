package com.example.tralalero.feature.home.ui.Home.project;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tralalero.data.dto.project.ProjectSummaryResponse;
import com.example.tralalero.data.repository.ProjectRepositoryImpl;
import com.example.tralalero.domain.repository.IProjectRepository;

/**
 * ViewModel for Project Summary feature
 * Handles business logic for displaying project statistics and status overview
 */
public class ProjectSummaryViewModel extends AndroidViewModel {
    
    private static final String TAG = "ProjectSummaryVM";
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutes in milliseconds
    
    private final IProjectRepository repository;
    
    // LiveData for project summary
    private final MutableLiveData<ProjectSummaryResponse> summary = new MutableLiveData<>();
    
    // LiveData for loading state
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    // LiveData for error messages
    private final MutableLiveData<String> error = new MutableLiveData<>();
    
    // LiveData for refresh success
    private final MutableLiveData<Boolean> refreshSuccess = new MutableLiveData<>();
    
    // Cache management
    private long lastFetchTime = 0;
    private String cachedProjectId = null;
    
    public ProjectSummaryViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ProjectRepositoryImpl(application);
    }
    
    // Getters for LiveData
    public LiveData<ProjectSummaryResponse> getSummary() {
        return summary;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    public LiveData<Boolean> getRefreshSuccess() {
        return refreshSuccess;
    }
    
    /**
     * Load project summary statistics
     * @param projectId Project ID
     */
    public void loadSummary(String projectId) {
        loadSummary(projectId, false);
    }
    
    /**
     * Load project summary statistics
     * @param projectId Project ID
     * @param isRefresh Whether this is a manual refresh
     */
    public void loadSummary(String projectId, boolean isRefresh) {
        if (projectId == null || projectId.isEmpty()) {
            Log.e(TAG, "loadSummary - Project ID is required!");
            error.setValue("Project ID is required");
            return;
        }
        
        // Check cache validity (skip cache if manual refresh or different project)
        if (!isRefresh && projectId.equals(cachedProjectId) && isCacheValid()) {
            Log.d(TAG, "loadSummary - Using cached data for project: " + projectId);
            // Data is already in LiveData, no need to fetch
            return;
        }
        
        isLoading.setValue(true);
        error.setValue(null);
        if (isRefresh) {
            refreshSuccess.setValue(false);
        }
        
        Log.d(TAG, "loadSummary - API call for project: " + projectId + " (Refresh: " + isRefresh + 
            ", Cache: " + (isCacheValid() ? "valid" : "invalid") + ")");
        
        repository.getProjectSummary(projectId, new IProjectRepository.RepositoryCallback<ProjectSummaryResponse>() {
            @Override
            public void onSuccess(ProjectSummaryResponse data) {
                isLoading.postValue(false);
                summary.postValue(data);
                
                // Update cache
                cachedProjectId = projectId;
                lastFetchTime = System.currentTimeMillis();
                
                if (isRefresh) {
                    refreshSuccess.postValue(true);
                }
                Log.d(TAG, "✅ SUCCESS - Done: " + data.getDone() + 
                    ", Due: " + data.getDue() + 
                    ", Created: " + data.getCreated() + 
                    ", Updated: " + data.getUpdated());
                
                if (data.getStatusOverview() != null) {
                    Log.d(TAG, "Status Overview - Total: " + data.getStatusOverview().getTotal() + 
                        ", ToDo: " + data.getStatusOverview().getToDo() + 
                        ", InProgress: " + data.getStatusOverview().getInProgress() + 
                        ", Done: " + data.getStatusOverview().getDone());
                }
            }
            
            @Override
            public void onError(String errorMsg) {
                isLoading.postValue(false);
                error.postValue(errorMsg);
                if (isRefresh) {
                    refreshSuccess.postValue(false);
                }
                Log.e(TAG, "❌ ERROR: " + errorMsg);
            }
        });
    }
    
    /**
     * Check if cached data is still valid
     * @return true if cache is valid (within 5 minutes)
     */
    private boolean isCacheValid() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastFetch = currentTime - lastFetchTime;
        return timeSinceLastFetch < CACHE_DURATION && summary.getValue() != null;
    }
    
    /**
     * Clear cache and force refresh
     */
    public void clearCache() {
        lastFetchTime = 0;
        cachedProjectId = null;
        Log.d(TAG, "Cache cleared");
    }
    
    /**
     * Refresh project summary
     * @param projectId Project ID
     */
    public void refreshSummary(String projectId) {
        loadSummary(projectId, true);
    }
    
    /**
     * Get percentage for status overview
     */
    public int getStatusPercentage(ProjectSummaryResponse.StatusOverview overview, String status) {
        if (overview == null || overview.getTotal() == 0) {
            return 0;
        }
        
        int count;
        switch (status.toUpperCase()) {
            case "TODO":
                count = overview.getToDo();
                break;
            case "INPROGRESS":
                count = overview.getInProgress();
                break;
            case "INREVIEW":
                count = overview.getInReview();
                break;
            case "DONE":
                count = overview.getDone();
                break;
            default:
                return 0;
        }
        
        return (int) ((count * 100.0) / overview.getTotal());
    }
    
    /**
     * Clear error message
     */
    public void clearError() {
        error.setValue(null);
    }
}
