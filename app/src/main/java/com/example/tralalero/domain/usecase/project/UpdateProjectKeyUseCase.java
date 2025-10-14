package com.example.tralalero.domain.usecase.project;

import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IProjectRepository;

import java.util.regex.Pattern;

/**
 * UseCase: Update project key
 *
 * Input: String projectId, String newKey
 * Output: Project (updated)
 *
 * Business Logic:
 * - Validate projectId is not null or empty
 * - Validate key format (2-10 uppercase letters)
 * - Convert key to uppercase
 * - Check for duplicate keys (if API supports it)
 */
public class UpdateProjectKeyUseCase {

    private static final int MIN_KEY_LENGTH = 2;
    private static final int MAX_KEY_LENGTH = 10;
    private static final Pattern KEY_PATTERN = Pattern.compile("^[A-Z]{2,10}$");

    private final IProjectRepository repository;

    public UpdateProjectKeyUseCase(IProjectRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute the use case to update project key
     *
     * @param projectId The project ID to update
     * @param newKey The new key value
     * @param callback Callback to receive the result
     */
    public void execute(String projectId, String newKey, Callback<Project> callback) {
        // Validate callback
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate project ID
        if (projectId == null || projectId.trim().isEmpty()) {
            callback.onError("Project ID cannot be empty");
            return;
        }

        // Validate new key
        if (newKey == null || newKey.trim().isEmpty()) {
            callback.onError("Project key cannot be empty");
            return;
        }

        String trimmedKey = newKey.trim().toUpperCase();

        // Validate key format
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

    /**
     * Callback interface for use case result
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}