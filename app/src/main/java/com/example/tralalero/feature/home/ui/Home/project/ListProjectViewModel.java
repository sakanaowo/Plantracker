package com.example.tralalero.feature.home.ui.Home.project;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tralalero.App.App;
import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.remote.api.WorkspaceApiService;
import com.example.tralalero.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListProjectViewModel extends ViewModel {
    private static final String TAG = "ListProjectViewModel";

    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final TaskApiService taskApiService;
    private final WorkspaceApiService workspaceApiService;

    public ListProjectViewModel() {
        taskApiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        workspaceApiService = ApiClient.get(App.authManager).create(WorkspaceApiService.class);
        isLoading.setValue(false);
    }

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    /**
     * Tạm thời vô hiệu hóa để test repository.
     * TODO: Kích hoạt lại và sửa lỗi logic này.
     */
    public void loadTasks(String projectId, String status) {
        Log.d(TAG, "loadTasks is temporarily disabled for testing.");
        tasks.postValue(new ArrayList<>());
        isLoading.postValue(false);
        // The original logic is commented out below
        /*
        isLoading.setValue(true);
        error.setValue(null);

        Log.d(TAG, "Starting loadTasks for projectId: " + projectId + ", status: " + status);

        // Bước 1: Load danh sách boards của project
        workspaceApiService.getBoards(projectId).enqueue(new Callback<List<Board>>() {
            @Override
            public void onResponse(Call<List<Board>> call, Response<List<Board>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Board> boards = response.body();
                    Log.d(TAG, "Boards loaded successfully: " + boards.size() + " boards");

                    // Bước 2: Tìm board theo status mapping
                    String targetBoardName = mapStatusToBoardName(status);
                    Board targetBoard = findBoardByName(boards, targetBoardName);

                    if (targetBoard != null) {
                        Log.d(TAG, "Found target board: " + targetBoard.getName() + " (ID: " + targetBoard.getId() + ")");
                        // Bước 3: Load tasks từ board đó
                        loadTasksFromBoard(targetBoard.getId(), status);
                    } else {
                        Log.e(TAG, "Board not found for status: " + status + " (looking for board name: " + targetBoardName + ")");
                        // Nếu không tìm thấy board, thử dùng board đầu tiên hoặc trả về danh sách rỗng
                        if (!boards.isEmpty()) {
                            Log.d(TAG, "Using first board as fallback: " + boards.get(0).getName());
                            loadTasksFromBoard(boards.get(0).getId(), status);
                        } else {
                            isLoading.setValue(false);
                            tasks.setValue(new ArrayList<>());
                            Log.d(TAG, "No boards found, returning empty task list");
                        }
                    }
                } else {
                    isLoading.setValue(false);
                    String errorMsg = "Failed to load boards: " + response.code();
                    error.setValue(errorMsg);
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<Board>> call, Throwable t) {
                isLoading.setValue(false);
                String errorMsg = "Error loading boards: " + t.getMessage();
                error.setValue(errorMsg);
                Log.e(TAG, errorMsg, t);
            }
        });
        */
    }

    /**
     * Load tasks từ một board cụ thể và filter theo status
     */
    private void loadTasksFromBoard(String boardId, String status) {
        // This method is disabled as it's called by the disabled loadTasks method.
        /*
        Log.d(TAG, "Loading tasks from boardId: " + boardId + " with status filter: " + status);

        taskApiService.getTasksByBoard(boardId).enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Task> allTasks = response.body();
                    Log.d(TAG, "Raw tasks loaded from board: " + allTasks.size() + " tasks");

                    // Filter tasks theo status (client-side filtering)
                    List<Task> filteredTasks = filterTasksByStatus(allTasks, status);
                    Log.d(TAG, "Filtered tasks for status " + status + ": " + filteredTasks.size() + " tasks");

                    tasks.setValue(filteredTasks);
                } else {
                    String errorMsg = "Failed to load tasks from board: " + response.code();
                    error.setValue(errorMsg);
                    Log.e(TAG, errorMsg + " for boardId: " + boardId);
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                isLoading.setValue(false);
                String errorMsg = "Error loading tasks from board: " + t.getMessage();
                error.setValue(errorMsg);
                Log.e(TAG, errorMsg + " for boardId: " + boardId, t);
            }
        });
        */
    }

    /**
     * Mapping từ status sang tên board
     */
    private String mapStatusToBoardName(String status) {
        switch (status) {
            case "TO_DO":
                return "To Do";
            case "IN_PROGRESS":
                return "In Progress";
            case "DONE":
                return "Done";
            default:
                return "To Do"; // Default fallback
        }
    }

    /**
     * Tìm board theo tên
     */
    private Board findBoardByName(List<Board> boards, String boardName) {
        for (Board board : boards) {
            if (boardName.equalsIgnoreCase(board.getName())) {
                return board;
            }
        }
        return null;
    }

    /**
     * Filter tasks theo status (client-side)
     */
    private List<Task> filterTasksByStatus(List<Task> allTasks, String status) {
        List<Task> filtered = new ArrayList<>();
        for (Task task : allTasks) {
            if (status.equals(task.getStatus())) {
                filtered.add(task);
            }
        }
        return filtered;
    }
}
