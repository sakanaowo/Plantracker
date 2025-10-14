package com.example.tralalero.domain.usecase.board;

import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.repository.IBoardRepository;

/**
 * UseCase: Get board by ID
 *
 * Matches Backend: GET /api/boards/:id
 *
 * Input: String boardId
 * Output: Board
 *
 * Business Logic:
 * - Retrieve single board detail
 * - Validate board ID format
 */
public class GetBoardByIdUseCase {

    private final IBoardRepository repository;

    public GetBoardByIdUseCase(IBoardRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Get board by ID
     *
     * @param boardId Board ID to retrieve
     * @param callback Callback to receive result
     */
    public void execute(String boardId, Callback<Board> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate board ID
        if (boardId == null || boardId.trim().isEmpty()) {
            callback.onError("Board ID cannot be empty");
            return;
        }

        // Validate UUID format
        if (!isValidUUID(boardId)) {
            callback.onError("Invalid board ID format");
            return;
        }

        repository.getBoardById(boardId, new IBoardRepository.RepositoryCallback<Board>() {
            @Override
            public void onSuccess(Board result) {
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

