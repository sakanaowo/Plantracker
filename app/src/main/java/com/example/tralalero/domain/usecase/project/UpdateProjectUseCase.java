package com.example.tralalero.domain.usecase.project;

import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IProjectRepository;

import java.util.regex.Pattern;


public class UpdateProjectUseCase {

    private static final int MIN_KEY_LENGTH = 2;
    private static final int MAX_KEY_LENGTH = 10;
    private static final Pattern KEY_PATTERN = Pattern.compile("^[A-Z]{2,10}$");

    private final IProjectRepository repository;

    public UpdateProjectUseCase(IProjectRepository repository) {
        this.repository = repository;
    }


    public void execute(String projectId, Project project, Callback<Project> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        if (projectId == null || projectId.trim().isEmpty()) {
            callback.onError("Project ID cannot be empty");
            return;
        }

        if (project == null) {
            callback.onError("Project cannot be null");
            return;
        }

        String name = project.getName();
        if (name == null || name.trim().isEmpty()) {
            callback.onError("Project name cannot be empty");
            return;
        }

        String key = project.getKey();
        if (key != null && !key.trim().isEmpty()) {  
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
                project.getBoardType() != null ? project.getBoardType().toUpperCase() : null,
                project.getType()  // Preserve existing type
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