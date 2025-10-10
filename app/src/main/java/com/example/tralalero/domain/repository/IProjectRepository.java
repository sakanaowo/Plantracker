package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.Project;

public interface IProjectRepository {
    // CRUD operations
    void getProjectById(String projectId, RepositoryCallback<Project> callback);

    void createProject(String workspaceId, Project project, RepositoryCallback<Project> callback);

    void updateProject(String projectId, Project project, RepositoryCallback<Project> callback);

    void deleteProject(String projectId, RepositoryCallback<Void> callback);

    // Project-specific operations
    void updateProjectKey(String projectId, String newKey, RepositoryCallback<Project> callback);

    void updateBoardType(String projectId, String boardType, RepositoryCallback<Project> callback);

    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
