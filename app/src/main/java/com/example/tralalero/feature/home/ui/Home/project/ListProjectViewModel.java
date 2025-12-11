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

    }

    private void loadTasksFromBoard(String boardId, String status) {

    }
    private String mapStatusToBoardName(String status) {
        switch (status) {
            case "TO_DO":
                return "To Do";
            case "IN_PROGRESS":
                return "In Progress";
            case "DONE":
                return "Done";
            default:
                return "To Do"; 
        }
    }
    private Board findBoardByName(List<Board> boards, String boardName) {
        for (Board board : boards) {
            if (boardName.equalsIgnoreCase(board.getName())) {
                return board;
            }
        }
        return null;
    }
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
