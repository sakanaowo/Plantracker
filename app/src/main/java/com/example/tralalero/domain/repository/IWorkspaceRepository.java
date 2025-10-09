package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.model.Workspace;

import java.util.List;

public interface IWorkspaceRepository {
    void getWorkspaces(RepositoryCallback<List<Workspace>> callback);

    void getWorkspaceById(String workspaceId, RepositoryCallback<Workspace> callback);

    void createWorkspace(Workspace workspace, RepositoryCallback<Workspace> callback);

    void getProjects(String workspaceId, RepositoryCallback<List<Project>> callback);

    void getBoards(String projectId, RepositoryCallback<List<Board>> callback);

    interface RepositoryCallback<T> {
        void onSuccess(T result);

        void onError(String error);
    }
}
