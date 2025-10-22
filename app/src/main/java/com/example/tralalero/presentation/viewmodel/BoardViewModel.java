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

public class BoardViewModel extends ViewModel {
    private final GetBoardByIdUseCase getBoardByIdUseCase;
    private final GetBoardsByProjectUseCase getBoardsByProjectUseCase;
    private final CreateBoardUseCase createBoardUseCase;
    private final UpdateBoardUseCase updateBoardUseCase;
    private final DeleteBoardUseCase deleteBoardUseCase;
    private final ReorderBoardsUseCase reorderBoardsUseCase;
    private final GetBoardTasksUseCase getBoardTasksUseCase;
    private final MutableLiveData<Board> selectedBoardLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Board>> projectBoardsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Task>> boardTasksLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> boardDeletedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> boardCreatedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> boardUpdatedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> boardsReorderedLiveData = new MutableLiveData<>(false);

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