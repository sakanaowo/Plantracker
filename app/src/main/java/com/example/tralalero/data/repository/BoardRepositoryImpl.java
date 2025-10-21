package com.example.tralalero.data.repository;

import android.util.Log;
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
    private static final String TAG = "BoardRepositoryImpl";
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
    public void getBoardsByProject(String projectId, RepositoryCallback<List<Board>> callback) {
        Log.d(TAG, "=== GET BOARDS BY PROJECT ===");
        Log.d(TAG, "Project ID: " + projectId);
        Log.d(TAG, "API Call: GET /boards?projectId=" + projectId);

        apiService.getBoardsByProject(projectId).enqueue(new Callback<List<BoardDTO>>() {
            @Override
            public void onResponse(Call<List<BoardDTO>> call, Response<List<BoardDTO>> response) {
                Log.d(TAG, "=== RESPONSE RECEIVED ===");
                Log.d(TAG, "Response Code: " + response.code());
                Log.d(TAG, "Response Success: " + response.isSuccessful());
                Log.d(TAG, "Response Body Null: " + (response.body() == null));

                if (response.isSuccessful() && response.body() != null) {
                    List<BoardDTO> boardDTOs = response.body();
                    Log.d(TAG, "✅ Received " + boardDTOs.size() + " boards from API");

                    for (int i = 0; i < boardDTOs.size(); i++) {
                        BoardDTO dto = boardDTOs.get(i);
                        Log.d(TAG, "Board " + (i + 1) + ": " +
                                "id=" + dto.getId() +
                                ", projectId=" + dto.getProjectId() +
                                ", name=" + dto.getName() +
                                ", order=" + dto.getOrder());
                    }

                    List<Board> boards = BoardMapper.toDomainList(boardDTOs);
                    Log.d(TAG, "✅ Mapped to " + boards.size() + " domain boards");
                    callback.onSuccess(boards);
                } else {
                    String errorMsg = "Failed to load boards: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            errorMsg += " - Could not read error body";
                        }
                    }
                    Log.e(TAG, "❌ " + errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<BoardDTO>> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, "❌ NETWORK FAILURE ===");
                Log.e(TAG, "Error: " + errorMsg, t);
                Log.e(TAG, "URL: " + call.request().url());
                callback.onError(errorMsg);
            }
        });
    }

    @Override
    public void createBoard(String projectId, Board board, RepositoryCallback<Board> callback) {
        Log.d(TAG, "=== CREATE BOARD DEBUG ===");
        Log.d(TAG, "Input projectId: " + projectId);
        Log.d(TAG, "Input Board - ID: " + board.getId() +
                   ", ProjectId: " + board.getProjectId() +
                   ", Name: " + board.getName() +
                   ", Order: " + board.getOrder());

        BoardDTO dto = BoardMapper.toDto(board);
        dto.setProjectId(projectId);

        Log.d(TAG, "DTO after mapping - ID: " + dto.getId() +
                   ", ProjectId: " + dto.getProjectId() +
                   ", Name: " + dto.getName() +
                   ", Order: " + dto.getOrder());
        Log.d(TAG, "DTO ProjectId is null? " + (dto.getProjectId() == null));
        Log.d(TAG, "DTO ProjectId is empty? " + (dto.getProjectId() != null && dto.getProjectId().isEmpty()));
        Log.d(TAG, "========================");

        apiService.createBoard(dto).enqueue(new Callback<BoardDTO>() {
            @Override
            public void onResponse(Call<BoardDTO> call, Response<BoardDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✅ Board created successfully: " + response.body().getId());
                    callback.onSuccess(BoardMapper.toDomain(response.body()));
                } else {
                    String errorMsg = "Failed to create board: " + response.code();
                    Log.e(TAG, "❌ " + errorMsg);
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<BoardDTO> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, "❌ " + errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    @Override
    public void updateBoard(String boardId, Board board, RepositoryCallback<Board> callback) {
        BoardDTO dto = BoardMapper.toDto(board);

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
