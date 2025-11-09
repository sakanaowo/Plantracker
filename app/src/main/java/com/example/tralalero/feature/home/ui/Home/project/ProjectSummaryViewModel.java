package com.example.tralalero.feature.home.ui.Home.project;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tralalero.App.App;
import com.example.tralalero.data.dto.TaskStatisticsDTO;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectSummaryViewModel extends ViewModel {
    private final TaskApiService taskApiService;
    
    private final MutableLiveData<TaskStatisticsDTO> statistics = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    
    public ProjectSummaryViewModel() {
        taskApiService = ApiClient.get(App.authManager).create(TaskApiService.class);
    }
    
    public LiveData<TaskStatisticsDTO> getStatistics() {
        return statistics;
    }
    
    public LiveData<Boolean> getLoading() {
        return loading;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    public void loadStatistics(String projectId) {
        loading.setValue(true);
        error.setValue(null);
        
        taskApiService.getProjectStatistics(projectId).enqueue(new Callback<TaskStatisticsDTO>() {
            @Override
            public void onResponse(Call<TaskStatisticsDTO> call, Response<TaskStatisticsDTO> response) {
                loading.setValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    statistics.setValue(response.body());
                } else {
                    error.setValue("Failed to load statistics: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<TaskStatisticsDTO> call, Throwable t) {
                loading.setValue(false);
                error.setValue("Network error: " + t.getMessage());
            }
        });
    }
}
