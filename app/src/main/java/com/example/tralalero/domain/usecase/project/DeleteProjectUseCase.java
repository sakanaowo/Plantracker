package com.example.tralalero.domain.usecase.project;

import com.example.tralalero.domain.repository.IProjectRepository;


public class DeleteProjectUseCase {

    private final IProjectRepository repository;

    public DeleteProjectUseCase(IProjectRepository repository) {
        this.repository = repository;
    }


    public void execute(String projectId, Callback<Void> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        if (projectId == null || projectId.trim().isEmpty()) {
            callback.onError("Project ID cannot be empty");
            return;
        }

        repository.deleteProject(projectId, new IProjectRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
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