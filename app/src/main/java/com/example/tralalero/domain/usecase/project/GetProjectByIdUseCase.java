package com.example.tralalero.domain.usecase.project;

import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IProjectRepository;


public class GetProjectByIdUseCase {

    private final IProjectRepository repository;

    public GetProjectByIdUseCase(IProjectRepository repository) {
        this.repository = repository;
    }

  
    public void execute(String projectId, Callback<Project> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        if (projectId == null || projectId.trim().isEmpty()) {
            callback.onError("Project ID cannot be empty");
            return;
        }

        repository.getProjectById(projectId, new IProjectRepository.RepositoryCallback<Project>() {
            @Override
            public void onSuccess(Project result) {
                if (result == null) {
                    callback.onError("Project not found");
                } else {
                    callback.onSuccess(result);
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }


    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}