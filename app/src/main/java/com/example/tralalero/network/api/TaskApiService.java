package com.example.tralalero.network.api;

import com.example.tralalero.model.Task;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TaskApiService {
    /**
     * Lấy danh sách tasks theo boardId từ backend endpoint
     * @param boardId ID của board (kanban board)
     * @return Danh sách các tasks trong board đó
     */
    @GET("tasks/by-board/{boardId}")
    Call<List<Task>> getTasksByBoard(@Path("boardId") String boardId);
}
