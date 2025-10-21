package com.example.tralalero.domain.usecase.board;

import com.example.tralalero.domain.repository.IBoardRepository;

import java.util.List;


public class ReorderBoardsUseCase {

    private final IBoardRepository repository;

    public ReorderBoardsUseCase(IBoardRepository repository) {
        this.repository = repository;
    }


    public void execute(String projectId, List<String> boardIds, Callback<Void> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        if (projectId == null || projectId.trim().isEmpty()) {
            callback.onError("Project ID cannot be empty");
            return;
        }

        if (!isValidUUID(projectId)) {
            callback.onError("Invalid project ID format");
            return;
        }

        if (boardIds == null || boardIds.isEmpty()) {
            callback.onError("Board IDs list cannot be empty");
            return;
        }

        for (String boardId : boardIds) {
            if (boardId == null || boardId.trim().isEmpty()) {
                callback.onError("Board ID cannot be empty");
                return;
            }

            if (!isValidUUID(boardId)) {
                callback.onError("Invalid board ID format: " + boardId);
                return;
            }
        }

        repository.reorderBoards(projectId, boardIds, new IBoardRepository.RepositoryCallback<Void>() {
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


    private boolean isValidUUID(String uuid) {
        if (uuid == null) return false;
        String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return uuid.matches(uuidPattern);
    }


    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

