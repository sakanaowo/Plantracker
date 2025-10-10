package com.example.tralalero.domain.usecase.project;

import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IProjectRepository;

import java.util.regex.Pattern;

/**
 * UseCase: Update existing project
 *
 * Input: String projectId, Project project
 * Output: Project (updated)
 *
 * Business Logic:
 * - Validate projectId is not null or empty
 * - Validate project name is not empty
 * - Validate project key format (if changed)
 * - Trim whitespace from name and description
 */
public class UpdateProjectUseCase {

    private static final int MIN_KEY_LENGTH = 2;
    private static final int MAX_KEY_LENGTH = 10;
    private static final Pattern KEY_PATTERN = Pattern.compile("^[A-Z]{2,10}$");

    private final IProjectRepository repository;

    public UpdateProjectUseCase(IProjectRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute the use case to update a project
     *
     * @param projectId The project ID to update
     * @param project The project object with updated values
     * @param callback Callback to receive the result
     */
    public void execute(String projectId, Project project, Callback<Project> callback) {
        // Validate callback
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate project ID
        if (projectId == null || projectId.trim().isEmpty()) {
            callback.onError("Project ID cannot be empty");
            return;
        }

        // Validate project object
        if (project == null) {
            callback.onError("Project cannot be null");
            return;
        }

        // Validate project name
        String name = project.getName();
        if (name == null || name.trim().isEmpty()) {
            callback.onError("Project name cannot be empty");
            return;
        }

        // Validate project key if present AND NOT EMPTY
        String key = project.getKey();
        if (key != null && !key.trim().isEmpty()) {  // ← QUAN TRỌNG: Chỉ validate nếu key có giá trị
            String trimmedKey = key.trim().toUpperCase();
            if (!KEY_PATTERN.matcher(trimmedKey).matches()) {
                callback.onError("Project key must be " + MIN_KEY_LENGTH + "-" + MAX_KEY_LENGTH +
                        " uppercase letters (A-Z) without special characters");
                return;
            }
            // Nếu key valid, gán lại trimmedKey vào project
            key = trimmedKey;
        }

        // Create project with trimmed values
        Project processedProject = new Project(
                project.getId(),
                project.getWorkspaceId(),
                name.trim(),
                project.getDescription() != null ? project.getDescription().trim() : null,
                key != null && !key.trim().isEmpty() ? key.trim().toUpperCase() : project.getKey(),
                project.getBoardType() != null ? project.getBoardType().toUpperCase() : null
        );

        repository.updateProject(projectId, processedProject,
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