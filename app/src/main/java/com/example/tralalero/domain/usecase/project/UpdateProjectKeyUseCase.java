package com.example.tralalero.domain.usecase.project;

import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IProjectRepository;

import java.util.regex.Pattern;


public class UpdateProjectKeyUseCase {

    private static final int MIN_KEY_LENGTH = 2;
    private static final int MAX_KEY_LENGTH = 10;
    private static final Pattern KEY_PATTERN = Pattern.compile("^[A-Z]{2,10}$");

    private final IProjectRepository repository;

    public UpdateProjectKeyUseCase(IProjectRepository repository) {
        this.repository = repository;
    }

    public void execute(String projectId, String newKey, Callback<Project> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        if (projectId == null || projectId.trim().isEmpty()) {
            callback.onError("Project ID cannot be empty");
            return;
        }

        if (newKey == null || newKey.trim().isEmpty()) {
            callback.onError("Project key cannot be empty");
            return;
        }

        String trimmedKey = newKey.trim().toUpperCase();

        if (!KEY_PATTERN.matcher(trimmedKey).matches()) {
            callback.onError("Project key must be " + MIN_KEY_LENGTH + "-" + MAX_KEY_LENGTH +
                    " uppercase letters (A-Z) without special characters");
            return;
        }

        repository.updateProjectKey(projectId, trimmedKey,
                new IProjectRepository.RepositoryCallback<Project>() {
                    @Override
                    public void onSuccess(Project result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                }
        );
    }


    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}