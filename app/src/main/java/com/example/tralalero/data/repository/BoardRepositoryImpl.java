package com.example.tralalero.data.repository;

import com.example.tralalero.data.mapper.BoardMapper;
import com.example.tralalero.data.remote.api.BoardApiService;
import com.example.tralalero.data.remote.dto.board.BoardDTO;
import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.repository.IBoardRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoardRepositoryImpl implements IBoardRepository {
    private final BoardApiService apiService;

    public BoardRepositoryImpl(BoardApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void getBoardById(String boardId, RepositoryCallback<Board> callback) {
        apiService.getBoardById(boardId).enqueue(new Callback<BoardDTO>() {
            @Override
            public void onResponse(Call<BoardDTO> call, Response<BoardDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(BoardMapper.toDomain(response.body()));
                } else {
                    callback.onError("Board not found: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BoardDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void createBoard(String projectId, Board board, RepositoryCallback<Board> callback) {
        BoardDTO dto = BoardMapper.toDTO(board);
        dto.setProjectId(projectId);

        apiService.createBoard(dto).enqueue(new Callback<BoardDTO>() {
            @Override
            public void onResponse(Call<BoardDTO> call, Response<BoardDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(BoardMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to create board: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BoardDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void updateBoard(String boardId, Board board, RepositoryCallback<Board> callback) {
        BoardDTO dto = BoardMapper.toDTO(board);

        apiService.updateBoard(boardId, dto).enqueue(new Callback<BoardDTO>() {
            @Override
            public void onResponse(Call<BoardDTO> call, Response<BoardDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(BoardMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to update board: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BoardDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void deleteBoard(String boardId, RepositoryCallback<Void> callback) {
        apiService.deleteBoard(boardId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to delete board: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void reorderBoards(String projectId, List<String> boardIds, RepositoryCallback<Void> callback) {
        // This would need a specific API endpoint for reordering
        // For now, we'll update each board's order individually
        callback.onError("Reorder boards not yet implemented in API");
    }

    @Override
    public void updateBoardOrder(String boardId, int newOrder, RepositoryCallback<Board> callback) {
        BoardDTO dto = new BoardDTO();
        dto.setOrder(newOrder);

        apiService.updateBoard(boardId, dto).enqueue(new Callback<BoardDTO>() {
            @Override
            public void onResponse(Call<BoardDTO> call, Response<BoardDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(BoardMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to update board order: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BoardDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}
