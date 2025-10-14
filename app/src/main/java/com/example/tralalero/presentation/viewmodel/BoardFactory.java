package com.example.tralalero.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tralalero.domain.usecase.board.GetBoardByIdUseCase;
import com.example.tralalero.domain.usecase.board.CreateBoardUseCase;
import com.example.tralalero.domain.usecase.board.UpdateBoardUseCase;
import com.example.tralalero.domain.usecase.board.DeleteBoardUseCase;
import com.example.tralalero.domain.usecase.board.ReorderBoardsUseCase;
import com.example.tralalero.domain.usecase.board.GetBoardTasksUseCase;

/**
 * Factory for creating BoardViewModel instances
 * Required by Android Architecture Components to inject dependencies
 *
 * @author Người 2
 * @date 14/10/2025
 */
public class BoardFactory implements ViewModelProvider.Factory {

    private final GetBoardByIdUseCase getBoardByIdUseCase;
    private final CreateBoardUseCase createBoardUseCase;
    private final UpdateBoardUseCase updateBoardUseCase;
    private final DeleteBoardUseCase deleteBoardUseCase;
    private final ReorderBoardsUseCase reorderBoardsUseCase;
    private final GetBoardTasksUseCase getBoardTasksUseCase;

    /**
     * Constructor with all required UseCases
     */
    public BoardFactory(
            GetBoardByIdUseCase getBoardByIdUseCase,
            CreateBoardUseCase createBoardUseCase,
            UpdateBoardUseCase updateBoardUseCase,
            DeleteBoardUseCase deleteBoardUseCase,
            ReorderBoardsUseCase reorderBoardsUseCase,
            GetBoardTasksUseCase getBoardTasksUseCase
    ) {
        this.getBoardByIdUseCase = getBoardByIdUseCase;
        this.createBoardUseCase = createBoardUseCase;
        this.updateBoardUseCase = updateBoardUseCase;
        this.deleteBoardUseCase = deleteBoardUseCase;
        this.reorderBoardsUseCase = reorderBoardsUseCase;
        this.getBoardTasksUseCase = getBoardTasksUseCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BoardViewModel.class)) {
            return (T) new BoardViewModel(
                    getBoardByIdUseCase,
                    createBoardUseCase,
                    updateBoardUseCase,
                    deleteBoardUseCase,
                    reorderBoardsUseCase,
                    getBoardTasksUseCase
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}