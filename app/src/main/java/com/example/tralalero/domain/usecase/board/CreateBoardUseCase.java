package com.example.tralalero.domain.usecase.board;

import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.repository.IBoardRepository;

/**
 * UseCase: Create new board in project
 *
 * Matches Backend: POST /api/projects/:projectId/boards
 * Backend DTO: CreateBoardDto {
 *   name: string
 *   order?: number
 * }
 *
 * Input: String projectId, Board board
 * Output: Board (created with generated ID)
 *
 * Business Logic:
 * - Validate board name not empty
 * - Validate project ID
 * - Backend auto-assigns order if not provided
 */
public class CreateBoardUseCase {

    private final IBoardRepository repository;

    public CreateBoardUseCase(IBoardRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Create new board
     *
     * @param projectId Project ID to create board in
     * @param board Board object (must have name)
     * @param callback Callback to receive result
     */
    public void execute(String projectId, Board board, Callback<Board> callback) {
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

        // Validate board object
        if (board == null) {
            callback.onError("Board cannot be null");
            return;
        }

        // Validate board name
        if (board.getName() == null || board.getName().trim().isEmpty()) {
            callback.onError("Board name cannot be empty");
            return;
        }

        // Validate board name length
        if (board.getName().length() > 100) {
            callback.onError("Board name cannot exceed 100 characters");
            return;
        }

        repository.createBoard(projectId, board, new IBoardRepository.RepositoryCallback<Board>() {
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

