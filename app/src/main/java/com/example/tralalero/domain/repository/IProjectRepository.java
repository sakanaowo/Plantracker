package com.example.tralalero.domain.repository;

import com.example.tralalero.data.dto.project.ProjectSummaryResponse;
import com.example.tralalero.domain.model.Project;

public interface IProjectRepository {
    void getProjectById(String projectId, RepositoryCallback<Project> callback);

    void createProject(String workspaceId, Project project, RepositoryCallback<Project> callback);

    void updateProject(String projectId, Project project, RepositoryCallback<Project> callback);

    void deleteProject(String projectId, RepositoryCallback<Void> callback);

    void updateProjectKey(String projectId, String newKey, RepositoryCallback<Project> callback);

    void updateBoardType(String projectId, String boardType, RepositoryCallback<Project> callback);
    
    /**
     * Get project summary statistics
     */
    void getProjectSummary(String projectId, RepositoryCallback<ProjectSummaryResponse> callback);

    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
