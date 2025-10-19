package com.example.tralalero.domain.usecase.board;

import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.repository.IBoardRepository;


public class GetBoardByIdUseCase {

    private final IBoardRepository repository;

    public GetBoardByIdUseCase(IBoardRepository repository) {
        this.repository = repository;
    }


    public void execute(String boardId, Callback<Board> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        if (boardId == null || boardId.trim().isEmpty()) {
            callback.onError("Board ID cannot be empty");
            return;
        }

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

 
    private boolean isValidUUID(String uuid) {
        if (uuid == null) return false;
        String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return uuid.matches(uuidPattern);
    }


    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

