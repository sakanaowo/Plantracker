package com.example.tralalero.domain.usecase.board;

import com.example.tralalero.domain.repository.IBoardRepository;

/**
 * UseCase: Delete a board
 *
 * Matches Backend: DELETE /api/boards/:id
 *
 * Input: String boardId
 * Output: Void (success/failure)
 *
 * Business Logic:
 * - Delete board and all its tasks (cascade)
 * - Validate board ID
 * - Cannot delete if it's the last board in project
 */
public class DeleteBoardUseCase {

    private final IBoardRepository repository;

    public DeleteBoardUseCase(IBoardRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Delete board
     *
     * @param boardId Board ID to delete
     * @param callback Callback to receive result
     */
    public void execute(String boardId, Callback<Void> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate board ID
        if (boardId == null || boardId.trim().isEmpty()) {
            callback.onError("Board ID cannot be empty");
            return;
        }

        if (!isValidUUID(boardId)) {
            callback.onError("Invalid board ID format");
            return;
        }

        repository.deleteBoard(boardId, new IBoardRepository.RepositoryCallback<Void>() {
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

