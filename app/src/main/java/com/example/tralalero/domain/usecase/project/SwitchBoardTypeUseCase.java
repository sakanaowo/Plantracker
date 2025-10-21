package com.example.tralalero.domain.usecase.project;

import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IProjectRepository;


public class SwitchBoardTypeUseCase {

    private static final String BOARD_TYPE_KANBAN = "KANBAN";
    private static final String BOARD_TYPE_SCRUM = "SCRUM";

    private final IProjectRepository repository;

    public SwitchBoardTypeUseCase(IProjectRepository repository) {
        this.repository = repository;
    }

    
    public void execute(String projectId, String boardType, Callback<Project> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        if (projectId == null || projectId.trim().isEmpty()) {
            callback.onError("Project ID cannot be empty");
            return;
        }

        if (boardType == null || boardType.trim().isEmpty()) {
            callback.onError("Board type cannot be empty");
            return;
        }

        String normalizedBoardType = boardType.trim().toUpperCase();

        if (!BOARD_TYPE_KANBAN.equals(normalizedBoardType) &&
                !BOARD_TYPE_SCRUM.equals(normalizedBoardType)) {
            callback.onError("Board type must be either KANBAN or SCRUM");
            return;
        }

        repository.updateBoardType(projectId, normalizedBoardType,
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