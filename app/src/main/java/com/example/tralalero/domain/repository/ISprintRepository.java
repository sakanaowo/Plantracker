package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.Sprint;
import com.example.tralalero.domain.model.Task;

import java.util.List;

public interface ISprintRepository {
    void getSprintsByProject(String projectId, RepositoryCallback<List<Sprint>> callback);

    void getSprintById(String sprintId, RepositoryCallback<Sprint> callback);

    void getActiveSprint(String projectId, RepositoryCallback<Sprint> callback);

    void createSprint(String projectId, Sprint sprint, RepositoryCallback<Sprint> callback);

    void updateSprint(String sprintId, Sprint sprint, RepositoryCallback<Sprint> callback);

    void deleteSprint(String sprintId, RepositoryCallback<Void> callback);

    void startSprint(String sprintId, RepositoryCallback<Sprint> callback);

    void completeSprint(String sprintId, RepositoryCallback<Sprint> callback);

    void addTaskToSprint(String sprintId, String taskId, RepositoryCallback<Void> callback);

    void removeTaskFromSprint(String taskId, RepositoryCallback<Void> callback);

    void getSprintTasks(String sprintId, RepositoryCallback<List<Task>> callback);

    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

