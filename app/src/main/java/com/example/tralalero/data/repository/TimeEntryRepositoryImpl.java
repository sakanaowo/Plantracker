package com.example.tralalero.data.repository;

import com.example.tralalero.data.remote.mapper.TimeEntryMapper;
import com.example.tralalero.data.remote.api.TimerApiService;
import com.example.tralalero.data.remote.dto.task.TimeEntryDTO;
import com.example.tralalero.domain.model.TimeEntry;
import com.example.tralalero.domain.repository.ITimeEntryRepository;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimeEntryRepositoryImpl implements ITimeEntryRepository {
    private final TimerApiService apiService;

    public TimeEntryRepositoryImpl(TimerApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void getTimeEntriesByTask(String taskId, RepositoryCallback<List<TimeEntry>> callback) {
        apiService.getTimeEntriesByTask(taskId).enqueue(new Callback<List<TimeEntryDTO>>() {
            @Override
            public void onResponse(Call<List<TimeEntryDTO>> call, Response<List<TimeEntryDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(TimeEntryMapper.toDomainList(response.body()));
                } else {
                    callback.onError("Failed to fetch time entries: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<TimeEntryDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getTimeEntriesByUser(String userId, RepositoryCallback<List<TimeEntry>> callback) {
        apiService.getTimeEntriesByUser(userId).enqueue(new Callback<List<TimeEntryDTO>>() {
            @Override
            public void onResponse(Call<List<TimeEntryDTO>> call, Response<List<TimeEntryDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(TimeEntryMapper.toDomainList(response.body()));
                } else {
                    callback.onError("Failed to fetch time entries: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<TimeEntryDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getTimeEntriesByDateRange(String userId, Date startDate, Date endDate, RepositoryCallback<List<TimeEntry>> callback) {
        callback.onError("Get time entries by date range not yet implemented in API");
    }

    @Override
    public void getActiveTimeEntry(String userId, RepositoryCallback<TimeEntry> callback) {
        apiService.getActiveTimeEntry(userId).enqueue(new Callback<TimeEntryDTO>() {
            @Override
            public void onResponse(Call<TimeEntryDTO> call, Response<TimeEntryDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(TimeEntryMapper.toDomain(response.body()));
                } else if (response.code() == 404) {
                    callback.onSuccess(null); 
                } else {
                    callback.onError("Failed to fetch active time entry: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TimeEntryDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void startTimer(String taskId, RepositoryCallback<TimeEntry> callback) {
        apiService.startTimer(taskId).enqueue(new Callback<TimeEntryDTO>() {
            @Override
            public void onResponse(Call<TimeEntryDTO> call, Response<TimeEntryDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(TimeEntryMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to start timer: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TimeEntryDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void stopTimer(String timeEntryId, RepositoryCallback<TimeEntry> callback) {
        apiService.stopTimer(timeEntryId).enqueue(new Callback<TimeEntryDTO>() {
            @Override
            public void onResponse(Call<TimeEntryDTO> call, Response<TimeEntryDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(TimeEntryMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to stop timer: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TimeEntryDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void createTimeEntry(TimeEntry timeEntry, RepositoryCallback<TimeEntry> callback) {
        TimeEntryDTO dto = TimeEntryMapper.toDto(timeEntry);

        apiService.createTimeEntry(dto).enqueue(new Callback<TimeEntryDTO>() {
            @Override
            public void onResponse(Call<TimeEntryDTO> call, Response<TimeEntryDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(TimeEntryMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to create time entry: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TimeEntryDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void updateTimeEntry(String timeEntryId, TimeEntry timeEntry, RepositoryCallback<TimeEntry> callback) {
        TimeEntryDTO dto = TimeEntryMapper.toDto(timeEntry);

        apiService.updateTimeEntry(timeEntryId, dto).enqueue(new Callback<TimeEntryDTO>() {
            @Override
            public void onResponse(Call<TimeEntryDTO> call, Response<TimeEntryDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(TimeEntryMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to update time entry: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TimeEntryDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void deleteTimeEntry(String timeEntryId, RepositoryCallback<Void> callback) {
        apiService.deleteTimeEntry(timeEntryId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to delete time entry: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getTotalTimeByTask(String taskId, RepositoryCallback<Integer> callback) {
        callback.onError("Get total time by task not yet implemented in API");
    }

    @Override
    public void getTotalTimeByUser(String userId, Date startDate, Date endDate, RepositoryCallback<Integer> callback) {
        callback.onError("Get total time by user not yet implemented in API");
    }
}

