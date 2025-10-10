package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.Board;

import java.util.List;

public interface IBoardRepository {
    // CRUD operations
    void getBoardById(String boardId, RepositoryCallback<Board> callback);

    void createBoard(String projectId, Board board, RepositoryCallback<Board> callback);

    void updateBoard(String boardId, Board board, RepositoryCallback<Board> callback);

    void deleteBoard(String boardId, RepositoryCallback<Void> callback);

    // Board-specific operations
    void reorderBoards(String projectId, List<String> boardIds, RepositoryCallback<Void> callback);

    void updateBoardOrder(String boardId, int newOrder, RepositoryCallback<Board> callback);

    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
