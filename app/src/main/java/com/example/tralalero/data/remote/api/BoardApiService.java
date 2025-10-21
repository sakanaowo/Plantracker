package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.board.BoardDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface BoardApiService {


    @GET("boards")
    Call<List<BoardDTO>> getBoardsByProject(@Query("projectId") String projectId);


    @GET("boards/{id}")
    Call<BoardDTO> getBoardById(@Path("id") String boardId);


    @POST("boards")
    Call<BoardDTO> createBoard(@Body BoardDTO board);


    @PATCH("boards/{id}")
    Call<BoardDTO> updateBoard(
        @Path("id") String boardId,
        @Body BoardDTO board
    );


    @DELETE("boards/{id}")
    Call<Void> deleteBoard(@Path("id") String boardId);
}

