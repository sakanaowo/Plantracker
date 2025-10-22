package com.example.tralalero.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tralalero.domain.usecase.board.GetBoardByIdUseCase;
import com.example.tralalero.domain.usecase.board.GetBoardsByProjectUseCase;
import com.example.tralalero.domain.usecase.board.CreateBoardUseCase;
import com.example.tralalero.domain.usecase.board.UpdateBoardUseCase;
import com.example.tralalero.domain.usecase.board.DeleteBoardUseCase;
import com.example.tralalero.domain.usecase.board.ReorderBoardsUseCase;
import com.example.tralalero.domain.usecase.board.GetBoardTasksUseCase;

public class BoardViewModelFactory implements ViewModelProvider.Factory {

    private final GetBoardByIdUseCase getBoardByIdUseCase;
    private final GetBoardsByProjectUseCase getBoardsByProjectUseCase;
    private final CreateBoardUseCase createBoardUseCase;
    private final UpdateBoardUseCase updateBoardUseCase;
    private final DeleteBoardUseCase deleteBoardUseCase;
    private final ReorderBoardsUseCase reorderBoardsUseCase;
    private final GetBoardTasksUseCase getBoardTasksUseCase;

    public BoardViewModelFactory(
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

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BoardViewModel.class)) {
            return (T) new BoardViewModel(
                    getBoardByIdUseCase,
                    getBoardsByProjectUseCase,
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