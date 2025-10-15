package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.TimeEntry;

import java.util.Date;
import java.util.List;

public interface ITimeEntryRepository {
    void getTimeEntriesByTask(String taskId, RepositoryCallback<List<TimeEntry>> callback);

    void getTimeEntriesByUser(String userId, RepositoryCallback<List<TimeEntry>> callback);

    void getTimeEntriesByDateRange(String userId, Date startDate, Date endDate, RepositoryCallback<List<TimeEntry>> callback);

    void getActiveTimeEntry(String userId, RepositoryCallback<TimeEntry> callback);

    void startTimer(String taskId, RepositoryCallback<TimeEntry> callback);

    void stopTimer(String timeEntryId, RepositoryCallback<TimeEntry> callback);

    void createTimeEntry(TimeEntry timeEntry, RepositoryCallback<TimeEntry> callback);

    void updateTimeEntry(String timeEntryId, TimeEntry timeEntry, RepositoryCallback<TimeEntry> callback);

    void deleteTimeEntry(String timeEntryId, RepositoryCallback<Void> callback);

    void getTotalTimeByTask(String taskId, RepositoryCallback<Integer> callback);

    void getTotalTimeByUser(String userId, Date startDate, Date endDate, RepositoryCallback<Integer> callback);

    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

