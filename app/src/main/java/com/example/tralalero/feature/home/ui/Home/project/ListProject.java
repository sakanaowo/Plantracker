package com.example.tralalero.feature.home.ui.Home.project;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.tralalero.R;
import com.example.tralalero.adapter.TaskAdapter;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.presentation.viewmodel.TaskViewModel;
import com.example.tralalero.presentation.viewmodel.TaskViewModelFactory;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.App.App;
import com.example.tralalero.domain.usecase.task.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
public class ListProject extends Fragment {
    private static final String TAG = "ListProject";
    private static final String ARG_TYPE = "type";
    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_BOARD_ID = "board_id";
    private String type;
    private String projectId;
    private String boardId;
    private TaskViewModel taskViewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TaskAdapter taskAdapter;
    private FloatingActionButton fabAddTask;
    public static ListProject newInstance(String type, String projectId, String boardId) {
        ListProject fragment = new ListProject();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_PROJECT_ID, projectId);
        args.putString(ARG_BOARD_ID, boardId);
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_frm, container, false);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE);
            projectId = getArguments().getString(ARG_PROJECT_ID);
            boardId = getArguments().getString(ARG_BOARD_ID);
        }
        Log.d(TAG, "onCreateView - Type: " + type + ", BoardId: " + boardId);
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        observeViewModel();
        if (boardId != null && !boardId.isEmpty()) {
            loadTasksForBoard();
        } else {
            Log.w(TAG, "BoardId not available yet, waiting for boards to load...");
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
            }
        }
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (boardId == null || boardId.isEmpty()) {
            if (getActivity() != null) {
                Log.d(TAG, "onResume - Still waiting for boardId");
            }
        }
    }
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        fabAddTask = view.findViewById(R.id.fabAddTask);
        if (fabAddTask != null) {
            fabAddTask.setOnClickListener(v -> showCreateTaskDialog());
        }
    }
    private void setupViewModel() {
        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        ITaskRepository repository = new TaskRepositoryImpl(apiService);
        GetTaskByIdUseCase getTaskByIdUseCase = new GetTaskByIdUseCase(repository);
        GetTasksByBoardUseCase getTasksByBoardUseCase = new GetTasksByBoardUseCase(repository);
        CreateTaskUseCase createTaskUseCase = new CreateTaskUseCase(repository);
        UpdateTaskUseCase updateTaskUseCase = new UpdateTaskUseCase(repository);
        DeleteTaskUseCase deleteTaskUseCase = new DeleteTaskUseCase(repository);
        AssignTaskUseCase assignTaskUseCase = new AssignTaskUseCase(repository);
        UnassignTaskUseCase unassignTaskUseCase = new UnassignTaskUseCase(repository);
        MoveTaskToBoardUseCase moveTaskToBoardUseCase = new MoveTaskToBoardUseCase(repository);
        UpdateTaskPositionUseCase updateTaskPositionUseCase = new UpdateTaskPositionUseCase(repository);
        AddCommentUseCase addCommentUseCase = new AddCommentUseCase(repository);
        GetTaskCommentsUseCase getTaskCommentsUseCase = new GetTaskCommentsUseCase(repository);
        AddAttachmentUseCase addAttachmentUseCase = new AddAttachmentUseCase(repository);
        GetTaskAttachmentsUseCase getTaskAttachmentsUseCase = new GetTaskAttachmentsUseCase(repository);
        AddChecklistUseCase addChecklistUseCase = new AddChecklistUseCase(repository);
        GetTaskChecklistsUseCase getTaskChecklistsUseCase = new GetTaskChecklistsUseCase(repository);
        TaskViewModelFactory factory = new TaskViewModelFactory(
            getTaskByIdUseCase,
            getTasksByBoardUseCase,
            createTaskUseCase,
            updateTaskUseCase,
            deleteTaskUseCase,
            assignTaskUseCase,
            unassignTaskUseCase,
            moveTaskToBoardUseCase,
            updateTaskPositionUseCase,
            addCommentUseCase,
            getTaskCommentsUseCase,
            addAttachmentUseCase,
            getTaskAttachmentsUseCase,
            addChecklistUseCase,
            getTaskChecklistsUseCase
        );
        taskViewModel = new ViewModelProvider(requireActivity(), factory).get(TaskViewModel.class);
        Log.d(TAG, "TaskViewModel initialized");
    }
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(new ArrayList<>());
        taskAdapter.setOnTaskClickListener(task -> {
            showTaskDetailBottomSheet(task);
            Log.d(TAG, "Task clicked: " + task.getId());
        });
        recyclerView.setAdapter(taskAdapter);
    }
    private void observeViewModel() {
        if (boardId != null && !boardId.isEmpty()) {
            taskViewModel.getTasksForBoard(boardId).observe(getViewLifecycleOwner(), tasks -> {
                if (tasks != null && !tasks.isEmpty()) {
                    taskAdapter.updateTasks(tasks);
                    Log.d(TAG, "Loaded " + tasks.size() + " tasks for board: " + boardId);
                    recyclerView.setVisibility(View.VISIBLE);
                    if (emptyView != null) {
                        emptyView.setVisibility(View.GONE);
                    }
                } else {
                    recyclerView.setVisibility(View.GONE);
                    if (emptyView != null) {
                        emptyView.setVisibility(View.VISIBLE);
                        emptyView.setText(getEmptyMessage());
                    }
                    Log.d(TAG, "No tasks found for board: " + boardId);
                }
            });
        } else {
            Log.w(TAG, "Cannot observe tasks yet - boardId is null");
        }
        taskViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            Log.d(TAG, "Loading state for " + type + ": " + isLoading);
        });
        taskViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error loading tasks for " + type + ": " + error);
                taskViewModel.clearError();
            }
        });
    }
    private void loadTasksForBoard() {
        if (boardId == null || boardId.isEmpty()) {
            Log.e(TAG, "Cannot load tasks: boardId is null");
            return;
        }
        Log.d(TAG, "Loading tasks for board: " + boardId);
        taskViewModel.loadTasksByBoard(boardId);
    }
    private void showTaskDetailBottomSheet(Task task) {
        TaskCreateEditBottomSheet bottomSheet = TaskCreateEditBottomSheet.newInstanceForEdit(
            task, boardId, projectId
        );
        bottomSheet.setOnTaskActionListener(new TaskCreateEditBottomSheet.OnTaskActionListener() {
            @Override
            public void onTaskCreated(Task task) {
            }
            @Override
            public void onTaskUpdated(Task updatedTask) {
                Log.d(TAG, "Updating task: " + updatedTask.getId());
                taskViewModel.updateTask(updatedTask.getId(), updatedTask);
                Toast.makeText(getContext(), "Task updated successfully", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onTaskDeleted(String taskId) {
                Log.d(TAG, "Deleting task: " + taskId);
                taskViewModel.deleteTask(taskId);
                Toast.makeText(getContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show();
            }
        });
        bottomSheet.show(getParentFragmentManager(), "EDIT_TASK");
    }
    private void showCreateTaskDialog() {
        if (boardId == null || boardId.isEmpty()) {
            Toast.makeText(getContext(), "Board not ready yet, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (projectId == null || projectId.isEmpty()) {
            Toast.makeText(getContext(), "Project ID missing", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Opening create task dialog for board: " + boardId);
        TaskCreateEditBottomSheet bottomSheet = TaskCreateEditBottomSheet.newInstanceForCreate(
            boardId, projectId
        );
        bottomSheet.setOnTaskActionListener(new TaskCreateEditBottomSheet.OnTaskActionListener() {
            @Override
            public void onTaskCreated(Task newTask) {
                Log.d(TAG, "Creating new task: " + newTask.getTitle());
                taskViewModel.createTask(newTask);
                Toast.makeText(getContext(), "Task created successfully", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onTaskUpdated(Task task) {
            }
            @Override
            public void onTaskDeleted(String taskId) {
            }
        });
        bottomSheet.show(getParentFragmentManager(), "CREATE_TASK");
    }
    private String getEmptyMessage() {
        return "No tasks in " + type;
    }
    public void updateBoardIdAndReload(String newBoardId) {
        if (newBoardId != null && !newBoardId.isEmpty() && !newBoardId.equals(this.boardId)) {
            Log.d(TAG, "Updating boardId from " + this.boardId + " to " + newBoardId);
            this.boardId = newBoardId;
            if (getArguments() != null) {
                getArguments().putString(ARG_BOARD_ID, newBoardId);
            }
            taskViewModel.getTasksForBoard(newBoardId).observe(getViewLifecycleOwner(), tasks -> {
                if (tasks != null && !tasks.isEmpty()) {
                    taskAdapter.updateTasks(tasks);
                    Log.d(TAG, "Loaded " + tasks.size() + " tasks for updated board: " + newBoardId);
                    recyclerView.setVisibility(View.VISIBLE);
                    if (emptyView != null) {
                        emptyView.setVisibility(View.GONE);
                    }
                } else {
                    recyclerView.setVisibility(View.GONE);
                    if (emptyView != null) {
                        emptyView.setVisibility(View.VISIBLE);
                        emptyView.setText(getEmptyMessage());
                    }
                    Log.d(TAG, "No tasks found for updated board: " + newBoardId);
                }
            });
            loadTasksForBoard();
        }
    }
    public String getBoardId() {
        return boardId;
    }
}