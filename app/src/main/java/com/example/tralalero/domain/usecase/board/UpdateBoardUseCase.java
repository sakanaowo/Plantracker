package com.example.tralalero.domain.usecase.board;

import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.repository.IBoardRepository;

/**
 * UseCase: Update board information
 *
 * Matches Backend: PUT /api/boards/:id
 * Backend DTO: UpdateBoardDto {
 *   name?: string
 *   order?: number
 * }
 *
 * Input: String boardId, Board board
 * Output: Board (updated)
 *
 * Business Logic:
 * - Validate board name if provided
 * - Validate board ID
 * - Only update fields that are provided
 */
public class UpdateBoardUseCase {

    private final IBoardRepository repository;

    public UpdateBoardUseCase(IBoardRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Update board
     *
     * @param boardId Board ID to update
     * @param board Board object with updated fields
     * @param callback Callback to receive result
     */
    public void execute(String boardId, Board board, Callback<Board> callback) {
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

        // Validate board object
        if (board == null) {
            callback.onError("Board cannot be null");
            return;
        }

        // Validate board name if provided
        if (board.getName() != null) {
            if (board.getName().trim().isEmpty()) {
                callback.onError("Board name cannot be empty");
                return;
            }

            if (board.getName().length() > 100) {
                callback.onError("Board name cannot exceed 100 characters");
                return;
            }
        }

        repository.updateBoard(boardId, board, new IBoardRepository.RepositoryCallback<Board>() {
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

