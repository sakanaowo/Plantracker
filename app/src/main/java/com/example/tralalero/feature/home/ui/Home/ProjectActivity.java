package com.example.tralalero.feature.home.ui.Home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.App.App;
import com.example.tralalero.adapter.BoardAdapter;
import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.feature.home.ui.Home.project.TaskCreateEditBottomSheet;
import com.example.tralalero.presentation.viewmodel.BoardViewModel;
import com.example.tralalero.presentation.viewmodel.ProjectViewModel;
import com.example.tralalero.presentation.viewmodel.TaskViewModel;
import com.example.tralalero.presentation.viewmodel.TaskViewModelFactory;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.domain.usecase.task.*;
import com.example.tralalero.network.ApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectActivity extends AppCompatActivity implements BoardAdapter.OnTaskPositionChangeListener {
    private static final String TAG = "ProjectActivity";

    private ProjectViewModel projectViewModel;
    private BoardViewModel boardViewModel;
    private TaskViewModel taskViewModel;

    private RecyclerView boardsRecyclerView;
    private BoardAdapter boardAdapter;
    private ProgressBar progressBar;
    private TextView tvProjectName;
    private TextView tvWorkspaceName;
    private ImageButton backButton;

    private String projectId;
    private String projectName;
    private String workspaceId;
    private String workspaceName;
    private List<Board> boards = new ArrayList<>();
    private final Map<String, List<Task>> tasksPerBoard = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.project_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getIntentData();
        setupViewModels();
        initViews();
        setupRecyclerView();
        observeViewModels();
        loadBoards();
    }

    private void getIntentData() {
        projectId = getIntent().getStringExtra("project_id");
        projectName = getIntent().getStringExtra("project_name");
        workspaceId = getIntent().getStringExtra("workspace_id");
        workspaceName = getIntent().getStringExtra("workspace_name");

        Log.d(TAG, "=== ProjectActivity Started ===");
        Log.d(TAG, "Project ID: " + projectId);

        if (projectId == null || projectId.isEmpty()) {
            Log.e(TAG, "ERROR: No project_id provided!");
            Toast.makeText(this, "Error: No project ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupViewModels() {
        projectViewModel = new ViewModelProvider(this,
                ViewModelFactoryProvider.provideProjectViewModelFactory()
        ).get(ProjectViewModel.class);

        boardViewModel = new ViewModelProvider(this,
                ViewModelFactoryProvider.provideBoardViewModelFactory()
        ).get(BoardViewModel.class);

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

        TaskViewModelFactory taskFactory = new TaskViewModelFactory(
            getTaskByIdUseCase, getTasksByBoardUseCase, createTaskUseCase,
            updateTaskUseCase, deleteTaskUseCase, assignTaskUseCase,
            unassignTaskUseCase, moveTaskToBoardUseCase, updateTaskPositionUseCase,
            addCommentUseCase, getTaskCommentsUseCase, addAttachmentUseCase,
            getTaskAttachmentsUseCase, addChecklistUseCase, getTaskChecklistsUseCase
        );

        taskViewModel = new ViewModelProvider(this, taskFactory).get(TaskViewModel.class);
        Log.d(TAG, "ViewModels setup complete");
    }

    private void initViews() {
        boardsRecyclerView = findViewById(R.id.boardsRecyclerView);
        backButton = findViewById(R.id.btnClosePjrDetail);
        progressBar = findViewById(R.id.progressBar);
        tvProjectName = findViewById(R.id.tvProjectName);
        tvWorkspaceName = findViewById(R.id.tvWorkspaceName);

        if (projectName != null && !projectName.isEmpty()) {
            tvProjectName.setText(projectName);
        }
        if (workspaceName != null && !workspaceName.isEmpty()) {
            tvWorkspaceName.setText(workspaceName);
        }

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectActivity.this, WorkspaceActivity.class);
            intent.putExtra("WORKSPACE_ID", workspaceId);
            startActivity(intent);
            finish();
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        );
        boardsRecyclerView.setLayoutManager(layoutManager);

        boardAdapter = new BoardAdapter(this); // ✅ Pass 'this' as listener

        boardsRecyclerView.setAdapter(boardAdapter);
    }

    // ✅ Implement OnBoardActionListener methods
    public void onAddCardClick(Board board) {
        showCreateTaskDialog(board);
    }

    public void onBoardMenuClick(Board board) {
        Toast.makeText(this, "Board menu: " + board.getName(), Toast.LENGTH_SHORT).show();
    }

    public void onTaskClick(Task task, Board board) {
        showTaskDetailBottomSheet(task, board);
    }

    public List<Task> getTasksForBoard(String boardId) {
        return tasksPerBoard.get(boardId);
    }

    // ✅ Implement OnTaskPositionChangeListener
    public void onTaskPositionChanged(Task task, int newPosition, Board board) {
        Log.d(TAG, "🔄 Task '" + task.getTitle() + "' moved to position " + newPosition);

        // Calculate new position value (spacing of 1000 between items)
        double positionValue = newPosition * 1000.0;

        // Update task position in backend
        taskViewModel.updateTaskPosition(task.getId(), positionValue);

        Toast.makeText(this, "Position updated", Toast.LENGTH_SHORT).show();
    }

    private void observeViewModels() {
        boardViewModel.isLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        boardViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                boardViewModel.clearError();
            }
        });

        boardViewModel.getProjectBoards().observe(this, loadedBoards -> {
            if (loadedBoards != null && !loadedBoards.isEmpty()) {
                Log.d(TAG, "Loaded " + loadedBoards.size() + " boards");
                boards = loadedBoards;
                boardAdapter.setBoards(boards);

                for (Board board : boards) {
                    loadTasksForBoard(board.getId());
                }
            } else {
                Log.w(TAG, "⚠️ No boards found");
            }
        });
    }

    private void loadBoards() {
        Log.d(TAG, "Loading boards for project: " + projectId);
        if (projectId != null && !projectId.isEmpty()) {
            boardViewModel.loadBoardsByProject(projectId);
        }
    }

    private void loadTasksForBoard(String boardId) {
        // FIX: Remove old observer first to prevent multiple observers
        taskViewModel.getTasksForBoard(boardId).removeObservers(this);

        taskViewModel.getTasksForBoard(boardId).observe(this, tasks -> {
            if (tasks != null) {
                Log.d(TAG, "📋 Loaded " + tasks.size() + " tasks for board: " + boardId);
                tasksPerBoard.put(boardId, tasks);
                boardAdapter.notifyDataSetChanged();
            }
        });

        taskViewModel.loadTasksByBoard(boardId);
    }

    private void showCreateTaskDialog(Board board) {
        TaskCreateEditBottomSheet bottomSheet = TaskCreateEditBottomSheet.newInstanceForCreate(
            board.getId(), projectId
        );

        bottomSheet.setOnTaskActionListener(new TaskCreateEditBottomSheet.OnTaskActionListener() {
            @Override
            public void onTaskCreated(Task newTask) {
                Log.d(TAG, "Creating task: " + newTask.getTitle());
                taskViewModel.createTask(newTask);

                // FIX: Delay 1.5s and force reload
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Log.d(TAG, "🔄 Reloading tasks...");
                    loadTasksForBoard(board.getId());
                    Toast.makeText(ProjectActivity.this, "Task created!", Toast.LENGTH_SHORT).show();
                }, 1500);
            }

            @Override
            public void onTaskUpdated(Task task) {
            }

            @Override
            public void onTaskDeleted(String taskId) {
            }
        });

        bottomSheet.show(getSupportFragmentManager(), "CREATE_TASK");
    }

    private void showTaskDetailBottomSheet(Task task, Board board) {
        TaskCreateEditBottomSheet bottomSheet = TaskCreateEditBottomSheet.newInstanceForEdit(
            task, board.getId(), projectId
        );

        bottomSheet.setOnTaskActionListener(new TaskCreateEditBottomSheet.OnTaskActionListener() {
            @Override
            public void onTaskCreated(Task task) {
            }

            @Override
            public void onTaskUpdated(Task updatedTask) {
                Log.d(TAG, "Updating task: " + updatedTask.getId());
                taskViewModel.updateTask(updatedTask.getId(), updatedTask);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    loadTasksForBoard(board.getId());
                    Toast.makeText(ProjectActivity.this, "Task updated!", Toast.LENGTH_SHORT).show();
                }, 1500);
            }

            @Override
            public void onTaskDeleted(String taskId) {
                Log.d(TAG, "Deleting task: " + taskId);
                taskViewModel.deleteTask(taskId);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    loadTasksForBoard(board.getId());
                    Toast.makeText(ProjectActivity.this, "Task deleted!", Toast.LENGTH_SHORT).show();
                }, 1500);
            }
        });

        bottomSheet.show(getSupportFragmentManager(), "EDIT_TASK");
    }
}

