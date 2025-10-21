package com.example.tralalero.domain.usecase.board;

import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.repository.IBoardRepository;

import java.util.List;


public class GetBoardsByProjectUseCase {
    private final IBoardRepository boardRepository;

    public GetBoardsByProjectUseCase(IBoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    
    public void execute(String projectId, Callback<List<Board>> callback) {
        if (projectId == null || projectId.isEmpty()) {
            callback.onError("Project ID cannot be empty");
            return;
        }

        boardRepository.getBoardsByProject(projectId, new IBoardRepository.RepositoryCallback<List<Board>>() {
            @Override
            public void onSuccess(List<Board> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

