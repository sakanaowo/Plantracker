package com.example.tralalero.domain.usecase.board;

import com.example.tralalero.domain.repository.IBoardRepository;

import java.util.List;

/**
 * UseCase: Reorder boards in a project
 *
 * Matches Backend: PUT /api/projects/:projectId/boards/reorder
 * Backend DTO: ReorderBoardsDto {
 *   boardIds: string[] (array of board IDs in new order)
 * }
 *
 * Input: String projectId, List<String> boardIds
 * Output: Void (success/failure)
 *
 * Business Logic:
 * - Reorder boards by providing new order of board IDs
 * - Validate all board IDs are valid UUIDs
 * - Used for drag-and-drop board reordering
 */
public class ReorderBoardsUseCase {

    private final IBoardRepository repository;

    public ReorderBoardsUseCase(IBoardRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Reorder boards
     *
     * @param projectId Project ID containing the boards
     * @param boardIds List of board IDs in new order
     * @param callback Callback to receive result
     */
    public void execute(String projectId, List<String> boardIds, Callback<Void> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate project ID
        if (projectId == null || projectId.trim().isEmpty()) {
            callback.onError("Project ID cannot be empty");
            return;
        }

        if (!isValidUUID(projectId)) {
            callback.onError("Invalid project ID format");
            return;
        }

        // Validate board IDs list
        if (boardIds == null || boardIds.isEmpty()) {
            callback.onError("Board IDs list cannot be empty");
            return;
        }

        // Validate each board ID
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

    /**
     * Validate UUID format
     */
    private boolean isValidUUID(String uuid) {
        if (uuid == null) return false;
        String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return uuid.matches(uuidPattern);
    }

    /**
     * Callback interface for use case result
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

