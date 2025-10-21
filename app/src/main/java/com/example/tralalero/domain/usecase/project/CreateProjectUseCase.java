package com.example.tralalero.domain.usecase.project;

import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IProjectRepository;

import java.util.regex.Pattern;
public class CreateProjectUseCase {

    private static final int MIN_KEY_LENGTH = 2;
    private static final int MAX_KEY_LENGTH = 10;
    private static final Pattern KEY_PATTERN = Pattern.compile("^[A-Z]{2,10}$");
    private static final String DEFAULT_BOARD_TYPE = "KANBAN";

    private final IProjectRepository repository;

    public CreateProjectUseCase(IProjectRepository repository) {
        this.repository = repository;
    }

    public void execute(String workspaceId, Project project, Callback<Project> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        if (workspaceId == null || workspaceId.trim().isEmpty()) {
            callback.onError("Workspace ID cannot be empty");
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

        String boardType = project.getBoardType();
        if (boardType == null || boardType.trim().isEmpty()) {
            boardType = DEFAULT_BOARD_TYPE;
        }

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


    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}