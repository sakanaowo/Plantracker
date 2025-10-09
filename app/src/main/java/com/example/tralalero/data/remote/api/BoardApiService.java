package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.board.BoardDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface BoardApiService {

    /**
     * Get all boards in a project
     * GET /boards?projectId={projectId}
     */
    @GET("boards")
    Call<List<BoardDTO>> getBoardsByProject(@Query("projectId") String projectId);

    /**
     * Get board by ID
     * GET /boards/{id}
     */
    @GET("boards/{id}")
    Call<BoardDTO> getBoardById(@Path("id") String boardId);

    /**
     * Create a new board
     * POST /boards
     */
    @POST("boards")
    Call<BoardDTO> createBoard(@Body BoardDTO board);

    /**
     * Update board
     * PATCH /boards/{id}
     */
    @PATCH("boards/{id}")
    Call<BoardDTO> updateBoard(
        @Path("id") String boardId,
        @Body BoardDTO board
    );

    /**
     * Delete board
     * DELETE /boards/{id}
     */
    @DELETE("boards/{id}")
    Call<Void> deleteBoard(@Path("id") String boardId);
}

