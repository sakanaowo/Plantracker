package com.example.tralalero.domain.usecase.project;

import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IProjectRepository;

import java.util.regex.Pattern;

/**
 * UseCase: Create a new project in workspace
 *
 * Input: String workspaceId, Project project
 * Output: Project (created)
 *
 * Business Logic:
 * - Validate workspaceId is not null or empty
 * - Validate project name is not empty
 * - Validate project key format (2-10 uppercase letters, no special chars)
 * - Set default boardType to "KANBAN" if not specified
 * - Trim whitespace from name and description
 */
public class CreateProjectUseCase {

    private static final int MIN_KEY_LENGTH = 2;
    private static final int MAX_KEY_LENGTH = 10;
    private static final Pattern KEY_PATTERN = Pattern.compile("^[A-Z]{2,10}$");
    private static final String DEFAULT_BOARD_TYPE = "KANBAN";

    private final IProjectRepository repository;

    public CreateProjectUseCase(IProjectRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute the use case to create a new project
     *
     * @param workspaceId The workspace ID where project will be created
     * @param project The project object to create
     * @param callback Callback to receive the result
     */
    public void execute(String workspaceId, Project project, Callback<Project> callback) {
        // Validate callback
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate workspace ID
        if (workspaceId == null || workspaceId.trim().isEmpty()) {
            callback.onError("Workspace ID cannot be empty");
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

        // Validate project key
        String key = project.getKey();
        if (key == null || key.trim().isEmpty()) {
            callback.onError("Project key cannot be empty");
            return;
        }

        String trimmedKey = key.trim().toUpperCase();
        if (!KEY_PATTERN.matcher(trimmedKey).matches()) {
            callback.onError("Project key must be " + MIN_KEY_LENGTH + "-" + MAX_KEY_LENGTH +
                    " uppercase letters (A-Z) without special characters");
            return;
        }

        // Apply business rules
        String boardType = project.getBoardType();
        if (boardType == null || boardType.trim().isEmpty()) {
            boardType = DEFAULT_BOARD_TYPE;
        }

        // Create project with processed values
        Project processedProject = new Project(
                project.getId(),
                workspaceId,
                name.trim(),
                project.getDescription() != null ? project.getDescription().trim() : null,
                trimmedKey,
                boardType.toUpperCase()
        );

        repository.createProject(workspaceId, processedProject,
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