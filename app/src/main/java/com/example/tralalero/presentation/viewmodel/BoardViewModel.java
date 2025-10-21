package com.example.tralalero.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.usecase.board.GetBoardByIdUseCase;
import com.example.tralalero.domain.usecase.board.GetBoardsByProjectUseCase;
import com.example.tralalero.domain.usecase.board.CreateBoardUseCase;
import com.example.tralalero.domain.usecase.board.UpdateBoardUseCase;
import com.example.tralalero.domain.usecase.board.DeleteBoardUseCase;
import com.example.tralalero.domain.usecase.board.ReorderBoardsUseCase;
import com.example.tralalero.domain.usecase.board.GetBoardTasksUseCase;

import java.util.List;

/**
 * ViewModel for managing Board-related UI state and business logic
 *
 * Features:
 * - CRUD operations for boards
 * - Load boards by project
 * - Reorder boards within a project
 * - Get tasks of a board
 *
 * @author Người 2
 * @date 14/10/2025
 */
public class BoardViewModel extends ViewModel {

    // ========== Dependencies (UseCases) ==========
    private final GetBoardByIdUseCase getBoardByIdUseCase;
    private final GetBoardsByProjectUseCase getBoardsByProjectUseCase;
    private final CreateBoardUseCase createBoardUseCase;
    private final UpdateBoardUseCase updateBoardUseCase;
    private final DeleteBoardUseCase deleteBoardUseCase;
    private final ReorderBoardsUseCase reorderBoardsUseCase;
    private final GetBoardTasksUseCase getBoardTasksUseCase;

    // ========== LiveData (UI State) ==========
    private final MutableLiveData<Board> selectedBoardLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Board>> projectBoardsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Task>> boardTasksLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> boardDeletedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> boardCreatedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> boardUpdatedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> boardsReorderedLiveData = new MutableLiveData<>(false);

    // ========== Constructor ==========
    /**
     * Constructor with dependency injection
     *
     * @param getBoardByIdUseCase UseCase to get board by ID
     * @param getBoardsByProjectUseCase UseCase to get boards by project
     * @param createBoardUseCase UseCase to create new board
     * @param updateBoardUseCase UseCase to update board
     * @param deleteBoardUseCase UseCase to delete board
     * @param reorderBoardsUseCase UseCase to reorder boards
     * @param getBoardTasksUseCase UseCase to get board tasks
     */
    public BoardViewModel(
            GetBoardByIdUseCase getBoardByIdUseCase,
            GetBoardsByProjectUseCase getBoardsByProjectUseCase,
            CreateBoardUseCase createBoardUseCase,
            UpdateBoardUseCase updateBoardUseCase,
            DeleteBoardUseCase deleteBoardUseCase,
            ReorderBoardsUseCase reorderBoardsUseCase,
            GetBoardTasksUseCase getBoardTasksUseCase
    ) {
        this.getBoardByIdUseCase = getBoardByIdUseCase;
        this.getBoardsByProjectUseCase = getBoardsByProjectUseCase;
        this.createBoardUseCase = createBoardUseCase;
        this.updateBoardUseCase = updateBoardUseCase;
        this.deleteBoardUseCase = deleteBoardUseCase;
        this.reorderBoardsUseCase = reorderBoardsUseCase;
        this.getBoardTasksUseCase = getBoardTasksUseCase;
    }

    // ========== Getters (Public API) ==========

    /**
     * Get the currently selected board
     * @return LiveData containing the selected board
     */
    public LiveData<Board> getSelectedBoard() {
        return selectedBoardLiveData;
    }

    /**
     * Get all boards for a project
     * @return LiveData containing list of boards
     */
    public LiveData<List<Board>> getProjectBoards() {
        return projectBoardsLiveData;
    }

    /**
     * Get tasks of the current board
     * @return LiveData containing list of tasks
     */
    public LiveData<List<Task>> getBoardTasks() {
        return boardTasksLiveData;
    }

    /**
     * Get loading state
     * @return LiveData containing loading state
     */
    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }

    /**
     * Get error message
     * @return LiveData containing error message
     */
    public LiveData<String> getError() {
        return errorLiveData;
    }

    /**
     * Get board deleted state
     * @return LiveData indicating if board was deleted
     */
    public LiveData<Boolean> isBoardDeleted() {
        return boardDeletedLiveData;
    }

    /**
     * Get board created state
     * @return LiveData indicating if board was created
     */
    public LiveData<Boolean> isBoardCreated() {
        return boardCreatedLiveData;
    }

    /**
     * Get board updated state
     * @return LiveData indicating if board was updated
     */
    public LiveData<Boolean> isBoardUpdated() {
        return boardUpdatedLiveData;
    }

    /**
     * Get boards reordered state
     * @return LiveData indicating if boards were reordered
     */
    public LiveData<Boolean> areBoardsReordered() {
        return boardsReorderedLiveData;
    }

    // ========== Public Methods ==========

    /**
     * Load a board by its ID
     * @param boardId The ID of the board to load
     */
    public void loadBoardById(String boardId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getBoardByIdUseCase.execute(boardId, new GetBoardByIdUseCase.Callback<Board>() {
            @Override
            public void onSuccess(Board result) {
                loadingLiveData.setValue(false);
                selectedBoardLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    /**
     * Load all boards for a project
     * @param projectId The project ID to load boards from
     */
    public void loadBoardsByProject(String projectId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getBoardsByProjectUseCase.execute(projectId, new GetBoardsByProjectUseCase.Callback<List<Board>>() {
            @Override
            public void onSuccess(List<Board> result) {
                loadingLiveData.setValue(false);
                projectBoardsLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    /**
     * Create a new board
     * @param projectId The project ID to create board in
     * @param board The board object to create
     */
    public void createBoard(String projectId, Board board) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        boardCreatedLiveData.setValue(false);

        createBoardUseCase.execute(projectId, board, new CreateBoardUseCase.Callback<Board>() {
            @Override
            public void onSuccess(Board result) {
                loadingLiveData.setValue(false);
                selectedBoardLiveData.setValue(result);
                boardCreatedLiveData.setValue(true);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                boardCreatedLiveData.setValue(false);
            }
        });
    }

    /**
     * Update an existing board
     * @param boardId The ID of the board to update
     * @param board The updated board object
     */
    public void updateBoard(String boardId, Board board) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        boardUpdatedLiveData.setValue(false);

        updateBoardUseCase.execute(boardId, board, new UpdateBoardUseCase.Callback<Board>() {
            @Override
            public void onSuccess(Board result) {
                loadingLiveData.setValue(false);
                selectedBoardLiveData.setValue(result);
                boardUpdatedLiveData.setValue(true);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                boardUpdatedLiveData.setValue(false);
            }
        });
    }

    /**
     * Delete a board
     * @param boardId The ID of the board to delete
     */
    public void deleteBoard(String boardId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        boardDeletedLiveData.setValue(false);

        deleteBoardUseCase.execute(boardId, new DeleteBoardUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                selectedBoardLiveData.setValue(null);
                boardDeletedLiveData.setValue(true);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                boardDeletedLiveData.setValue(false);
            }
        });
    }

    /**
     * Reorder boards within a project
     * @param projectId The project ID
     * @param boardIds List of board IDs in the new order
     */
    public void reorderBoards(String projectId, List<String> boardIds) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        boardsReorderedLiveData.setValue(false);

        reorderBoardsUseCase.execute(projectId, boardIds, new ReorderBoardsUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                boardsReorderedLiveData.setValue(true);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                boardsReorderedLiveData.setValue(false);
            }
        });
    }

    /**
     * Load all tasks of a board
     * @param boardId The ID of the board
     */
    public void loadBoardTasks(String boardId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getBoardTasksUseCase.execute(boardId, new GetBoardTasksUseCase.Callback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> result) {
                loadingLiveData.setValue(false);
                boardTasksLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    /**
     * Clear any error message
     */
    public void clearError() {
        errorLiveData.setValue(null);
    }

    /**
     * Reset all success flags
     */
    public void resetFlags() {
        boardCreatedLiveData.setValue(false);
        boardUpdatedLiveData.setValue(false);
        boardDeletedLiveData.setValue(false);
        boardsReorderedLiveData.setValue(false);
    }

    // ========== Lifecycle ==========
    @Override
    protected void onCleared() {
        super.onCleared();
        // Cleanup if needed
    }
}