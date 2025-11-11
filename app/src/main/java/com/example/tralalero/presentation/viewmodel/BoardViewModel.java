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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BoardViewModel - Enhanced for MVVM Architecture
 * 
 * DEV 2 WORK - DAY 1-2: Complete BoardViewModel with optimistic updates
 * 
 * Features:
 * - Manages boards + tasks per board in single LiveData
 * - Optimistic updates for instant UI feedback
 * - Auto-loads tasks when boards load
 * - Zero manual reload needed in Activities
 */
public class BoardViewModel extends ViewModel {
    private final GetBoardByIdUseCase getBoardByIdUseCase;
    private final GetBoardsByProjectUseCase getBoardsByProjectUseCase;
    private final CreateBoardUseCase createBoardUseCase;
    private final UpdateBoardUseCase updateBoardUseCase;
    private final DeleteBoardUseCase deleteBoardUseCase;
    private final ReorderBoardsUseCase reorderBoardsUseCase;
    private final GetBoardTasksUseCase getBoardTasksUseCase;
    
    // ========== State Management ==========
    // Single source of truth for all board-related data
    private final MutableLiveData<Board> selectedBoardLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Board>> projectBoardsLiveData = new MutableLiveData<>();
    
    // ✅ NEW: Tasks per board mapping for efficient access
    private final Map<String, MutableLiveData<List<Task>>> tasksPerBoardMap = new HashMap<>();
    
    private final MutableLiveData<List<Task>> boardTasksLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    
    // ❌ DEPRECATED: These boolean flags are anti-pattern, will be removed
    private final MutableLiveData<Boolean> boardDeletedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> boardCreatedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> boardUpdatedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> boardsReorderedLiveData = new MutableLiveData<>(false);
    
    // ✅ NEW: Current project ID for context
    private String currentProjectId = null;

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

    public LiveData<Board> getSelectedBoard() {
        return selectedBoardLiveData;
    }

    public LiveData<List<Board>> getProjectBoards() {
        return projectBoardsLiveData;
    }

    public LiveData<List<Task>> getBoardTasks() {
        return boardTasksLiveData;
    }
    
    /**
     * ✅ NEW: Get tasks for specific board
     * @param boardId Board ID to get tasks for
     * @return LiveData of tasks for that board
     */
    public LiveData<List<Task>> getTasksForBoard(String boardId) {
        if (boardId == null || boardId.isEmpty()) {
            return boardTasksLiveData;
        }
        
        if (!tasksPerBoardMap.containsKey(boardId)) {
            tasksPerBoardMap.put(boardId, new MutableLiveData<>());
            // Auto-load tasks for this board
            loadBoardTasks(boardId);
        }
        return tasksPerBoardMap.get(boardId);
    }

    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<Boolean> isBoardDeleted() {
        return boardDeletedLiveData;
    }

    public LiveData<Boolean> isBoardCreated() {
        return boardCreatedLiveData;
    }

    public LiveData<Boolean> isBoardUpdated() {
        return boardUpdatedLiveData;
    }

    public LiveData<Boolean> areBoardsReordered() {
        return boardsReorderedLiveData;
    }

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
     * ✅ ENHANCED: Load boards and auto-load tasks for each board
     * This is the main method Activities should call
     */
    public void loadBoardsForProject(String projectId) {
        this.currentProjectId = projectId;
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getBoardsByProjectUseCase.execute(projectId, new GetBoardsByProjectUseCase.Callback<List<Board>>() {
            @Override
            public void onSuccess(List<Board> result) {
                projectBoardsLiveData.setValue(result);
                
                // ✅ Auto-load tasks for all boards
                if (result != null && !result.isEmpty()) {
                    loadTasksForAllBoards(result);
                } else {
                    loadingLiveData.setValue(false);
                }
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }
    
    /**
     * ✅ NEW: Load tasks for all boards in parallel
     */
    private void loadTasksForAllBoards(List<Board> boards) {
        final int[] loadedCount = {0};
        final int totalBoards = boards.size();
        
        for (Board board : boards) {
            getBoardTasksUseCase.execute(board.getId(), new GetBoardTasksUseCase.Callback<List<Task>>() {
                @Override
                public void onSuccess(List<Task> tasks) {
                    // Store tasks in map
                    if (!tasksPerBoardMap.containsKey(board.getId())) {
                        tasksPerBoardMap.put(board.getId(), new MutableLiveData<>());
                    }
                    MutableLiveData<List<Task>> boardTasks = tasksPerBoardMap.get(board.getId());
                    if (boardTasks != null) {
                        boardTasks.setValue(tasks != null ? tasks : new ArrayList<>());
                    }
                    
                    // Check if all boards loaded
                    loadedCount[0]++;
                    if (loadedCount[0] >= totalBoards) {
                        loadingLiveData.setValue(false);
                    }
                }

                @Override
                public void onError(String error) {
                    // Still count as loaded to prevent hanging
                    loadedCount[0]++;
                    if (loadedCount[0] >= totalBoards) {
                        loadingLiveData.setValue(false);
                    }
                }
            });
        }
    }
    
    @Deprecated
    public void loadBoardsByProject(String projectId) {
        // Use loadBoardsForProject instead
        loadBoardsForProject(projectId);
    }

    /**
     * ✅ ENHANCED: Create board with optimistic update
     * Instantly adds board to UI, then syncs with API
     */
    public void createBoard(String projectId, Board board) {
        errorLiveData.setValue(null);
        boardCreatedLiveData.setValue(false);
        
        // ✅ Optimistic update: Add board to UI immediately
        List<Board> currentBoards = projectBoardsLiveData.getValue();
        if (currentBoards == null) {
            currentBoards = new ArrayList<>();
        }
        
        // Create temporary board with temp ID
        Board tempBoard = new Board(
            "temp_" + System.currentTimeMillis(),
            projectId,
            board.getName(),
            board.getOrder()
        );
        
        List<Board> updatedBoards = new ArrayList<>(currentBoards);
        updatedBoards.add(tempBoard);
        projectBoardsLiveData.setValue(updatedBoards);
        
        // Initialize empty tasks for this board
        tasksPerBoardMap.put(tempBoard.getId(), new MutableLiveData<>(new ArrayList<>()));
        
        // Now call API
        loadingLiveData.setValue(true);
        createBoardUseCase.execute(projectId, board, new CreateBoardUseCase.Callback<Board>() {
            @Override
            public void onSuccess(Board result) {
                loadingLiveData.setValue(false);
                selectedBoardLiveData.setValue(result);
                boardCreatedLiveData.setValue(true);
                
                // ✅ Replace temp board with real board
                List<Board> boards = projectBoardsLiveData.getValue();
                if (boards != null) {
                    List<Board> updated = new ArrayList<>();
                    for (Board b : boards) {
                        if (b.getId().equals(tempBoard.getId())) {
                            updated.add(result); // Replace temp with real
                            // Move tasks map to real ID
                            MutableLiveData<List<Task>> tempTasks = tasksPerBoardMap.remove(tempBoard.getId());
                            if (tempTasks != null) {
                                tasksPerBoardMap.put(result.getId(), tempTasks);
                            }
                        } else {
                            updated.add(b);
                        }
                    }
                    projectBoardsLiveData.setValue(updated);
                }
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                boardCreatedLiveData.setValue(false);
                
                // ✅ Rollback: Remove temp board on error
                List<Board> boards = projectBoardsLiveData.getValue();
                if (boards != null) {
                    List<Board> updated = new ArrayList<>();
                    for (Board b : boards) {
                        if (!b.getId().equals(tempBoard.getId())) {
                            updated.add(b);
                        }
                    }
                    projectBoardsLiveData.setValue(updated);
                    tasksPerBoardMap.remove(tempBoard.getId());
                }
            }
        });
    }

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
     * ✅ ENHANCED: Delete board with optimistic update
     * Instantly removes board from UI, then syncs with API
     */
    public void deleteBoard(String boardId) {
        errorLiveData.setValue(null);
        boardDeletedLiveData.setValue(false);
        
        // ✅ Optimistic update: Remove board from UI immediately
        List<Board> currentBoards = projectBoardsLiveData.getValue();
        Board deletedBoard = null;
        
        if (currentBoards != null) {
            List<Board> updatedBoards = new ArrayList<>();
            for (Board board : currentBoards) {
                if (board.getId().equals(boardId)) {
                    deletedBoard = board; // Save for rollback
                } else {
                    updatedBoards.add(board);
                }
            }
            projectBoardsLiveData.setValue(updatedBoards);
            
            // Remove tasks for this board
            tasksPerBoardMap.remove(boardId);
        }
        
        final Board boardToRestore = deletedBoard;
        
        // Now call API
        loadingLiveData.setValue(true);
        deleteBoardUseCase.execute(boardId, new DeleteBoardUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                selectedBoardLiveData.setValue(null);
                boardDeletedLiveData.setValue(true);
                // Board already removed from UI, no need to update
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                boardDeletedLiveData.setValue(false);
                
                // ✅ Rollback: Restore board on error
                if (boardToRestore != null) {
                    List<Board> boards = projectBoardsLiveData.getValue();
                    if (boards != null) {
                        List<Board> updated = new ArrayList<>(boards);
                        // Insert at original position based on order field
                        int insertIndex = 0;
                        for (int i = 0; i < updated.size(); i++) {
                            if (updated.get(i).getOrder() > boardToRestore.getOrder()) {
                                insertIndex = i;
                                break;
                            }
                            insertIndex = i + 1;
                        }
                        updated.add(insertIndex, boardToRestore);
                        projectBoardsLiveData.setValue(updated);
                        
                        // Reload tasks for restored board
                        loadBoardTasks(boardId);
                    }
                }
            }
        });
    }

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
     * ✅ ENHANCED: Load tasks for specific board and update map
     */
    public void loadBoardTasks(String boardId) {
        errorLiveData.setValue(null);

        getBoardTasksUseCase.execute(boardId, new GetBoardTasksUseCase.Callback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> result) {
                // Update both general and specific LiveData
                boardTasksLiveData.setValue(result);
                
                // Update board-specific LiveData in map
                if (!tasksPerBoardMap.containsKey(boardId)) {
                    tasksPerBoardMap.put(boardId, new MutableLiveData<>());
                }
                MutableLiveData<List<Task>> boardTasks = tasksPerBoardMap.get(boardId);
                if (boardTasks != null) {
                    boardTasks.setValue(result != null ? result : new ArrayList<>());
                }
            }

            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    public void clearError() {
        errorLiveData.setValue(null);
    }

    public void resetFlags() {
        boardCreatedLiveData.setValue(false);
        boardUpdatedLiveData.setValue(false);
        boardDeletedLiveData.setValue(false);
        boardsReorderedLiveData.setValue(false);
    }
    @Override
    protected void onCleared() {
        super.onCleared();
    }
}