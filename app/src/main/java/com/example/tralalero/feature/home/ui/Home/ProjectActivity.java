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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.tralalero.feature.home.ui.Home.project.CardDetailActivity;
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

public class ProjectActivity extends AppCompatActivity implements BoardAdapter.OnBoardActionListener, BoardAdapter.OnTaskPositionChangeListener {
    private static final String TAG = "ProjectActivity";
    
    // Request codes for CardDetailActivity
    private static final int REQUEST_CODE_CREATE_TASK = 2001;
    private static final int REQUEST_CODE_EDIT_TASK = 2002;

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
    
    // � Activity Result Launcher for InboxActivity
    private ActivityResultLauncher<Intent> inboxActivityLauncher;

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
        setupActivityResultLauncher();  // 📥 Setup result launcher
        setupViewModels();
        initViews();
        setupRecyclerView();
        observeViewModels();
        loadBoards();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // No cleanup needed for ActivityResultLauncher
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 🔄 Reload all boards when returning from InboxActivity
        // This ensures tasks created in InboxActivity appear here automatically
        Log.d(TAG, "onResume: Reloading all boards to sync with InboxActivity changes");
        
        // Reload tasks for all currently loaded boards
        for (Board board : boards) {
            loadTasksForBoard(board.getId());
        }
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
    
    /**
     * 📥 Setup Activity Result Launcher to receive task creation results from InboxActivity
     * Modern way to handle activity results (replaces startActivityForResult)
     */
    private void setupActivityResultLauncher() {
        inboxActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String taskId = data.getStringExtra("task_id");
                    String boardId = data.getStringExtra("board_id");
                    
                    Log.d(TAG, "📥 Received result: Task created - taskId: " + taskId + ", boardId: " + boardId);
                    
                    if (boardId != null && taskId != null) {
                        // Reload tasks for the affected board
                        loadTasksForBoard(boardId);
                        Toast.makeText(this, "✅ Task added to board!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "✓ Reloaded tasks for board: " + boardId);
                    }
                }
            }
        );
        Log.d(TAG, "📥 ActivityResultLauncher registered");
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
//        TODO: fix this red line
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
        // Open CardDetailActivity for creating new task
        Intent intent = new Intent(this, CardDetailActivity.class);
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_ID, board.getId());
        intent.putExtra(CardDetailActivity.EXTRA_PROJECT_ID, projectId);
        intent.putExtra(CardDetailActivity.EXTRA_IS_EDIT_MODE, false);
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_NAME, board.getName()); // Pass board name
        
        // Store board ID for later use in onActivityResult
        intent.putExtra("board_id_for_reload", board.getId());
        
        startActivityForResult(intent, REQUEST_CODE_CREATE_TASK);
    }

    private void showTaskDetailBottomSheet(Task task, Board board) {
        // Open CardDetailActivity for editing task
        Intent intent = new Intent(this, CardDetailActivity.class);
        intent.putExtra(CardDetailActivity.EXTRA_TASK_ID, task.getId());
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_ID, board.getId());
        intent.putExtra(CardDetailActivity.EXTRA_PROJECT_ID, projectId);
        intent.putExtra(CardDetailActivity.EXTRA_IS_EDIT_MODE, true);
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_NAME, board.getName()); // Pass board name
        
        // Pass task details
        intent.putExtra(CardDetailActivity.EXTRA_TASK_TITLE, task.getTitle());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_DESCRIPTION, task.getDescription());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_PRIORITY, task.getPriority() != null ? task.getPriority().name() : "MEDIUM");
        intent.putExtra(CardDetailActivity.EXTRA_TASK_STATUS, task.getStatus() != null ? task.getStatus().name() : "TO_DO");
        intent.putExtra(CardDetailActivity.EXTRA_TASK_POSITION, task.getPosition());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_ASSIGNEE_ID, task.getAssigneeId());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_CREATED_BY, task.getCreatedBy());
        
        // Store board ID for later use in onActivityResult
        intent.putExtra("board_id_for_reload", board.getId());
        
        startActivityForResult(intent, REQUEST_CODE_EDIT_TASK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CREATE_TASK || requestCode == REQUEST_CODE_EDIT_TASK) {
                // Get board ID from intent
                final String boardIdForReload;
                if (data != null) {
                    boardIdForReload = data.getStringExtra("board_id_for_reload");
                } else {
                    boardIdForReload = null;
                }
                
                // Reload tasks after creation/update/delete
                if (boardIdForReload != null && !boardIdForReload.isEmpty()) {
                    Log.d(TAG, "🔄 Reloading tasks for board: " + boardIdForReload);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        loadTasksForBoard(boardIdForReload);
                    }, 500);
                } else {
                    // Reload all boards if we don't have specific board ID
                    Log.d(TAG, "🔄 Reloading all boards");
                    loadBoards();
                }
            }
        }
    }
}
