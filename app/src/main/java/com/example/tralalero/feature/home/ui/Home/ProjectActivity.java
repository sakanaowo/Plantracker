package com.example.tralalero.feature.home.ui.Home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.tralalero.R;
import com.example.tralalero.App.App;
import com.example.tralalero.adapter.BoardAdapter;
import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.feature.home.ui.Home.project.CardDetailActivity;
import com.example.tralalero.feature.home.ui.Home.project.CreateTaskBottomSheet;
import com.example.tralalero.feature.home.ui.Home.project.ProjectCalendarFragment;
import com.example.tralalero.presentation.viewmodel.BoardViewModel;
import com.example.tralalero.presentation.viewmodel.ProjectViewModel;
import com.example.tralalero.presentation.viewmodel.TaskViewModel;
import com.example.tralalero.presentation.viewmodel.TaskViewModelFactory;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.remote.api.CommentApiService;
import com.example.tralalero.data.remote.api.AttachmentApiService;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.domain.usecase.task.*;
import com.example.tralalero.network.ApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.material.tabs.TabLayout;

public class ProjectActivity extends AppCompatActivity implements BoardAdapter.OnBoardActionListener, BoardAdapter.OnTaskPositionChangeListener, BoardAdapter.OnTaskBoardChangeListener {
    private static final String TAG = "ProjectActivity";
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
    private TabLayout tabLayout;

    private String projectId;
    private String projectName;
    private String workspaceId;
    private String workspaceName;
    private List<Board> boards = new ArrayList<>();
    private final Map<String, List<Task>> tasksPerBoard = new HashMap<>();
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
        setupTabs();
        observeViewModels();
        loadBoards();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Reloading all boards to sync with InboxActivity changes");
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
        CommentApiService commentApiService = ApiClient.get(App.authManager).create(CommentApiService.class);
        AttachmentApiService attachmentApiService = ApiClient.get(App.authManager).create(AttachmentApiService.class);
        ITaskRepository repository = new TaskRepositoryImpl(apiService, commentApiService, attachmentApiService);

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
        UpdateCommentUseCase updateCommentUseCase = new UpdateCommentUseCase(repository);
        DeleteCommentUseCase deleteCommentUseCase = new DeleteCommentUseCase(repository);
        DeleteAttachmentUseCase deleteAttachmentUseCase = new DeleteAttachmentUseCase(repository);
        GetAttachmentViewUrlUseCase getAttachmentViewUrlUseCase = new GetAttachmentViewUrlUseCase(repository);

        TaskViewModelFactory taskFactory = new TaskViewModelFactory(
                getTaskByIdUseCase, getTasksByBoardUseCase, createTaskUseCase,
                updateTaskUseCase, deleteTaskUseCase, assignTaskUseCase,
                unassignTaskUseCase, moveTaskToBoardUseCase, updateTaskPositionUseCase,
                addCommentUseCase, getTaskCommentsUseCase, addAttachmentUseCase,
                getTaskAttachmentsUseCase, addChecklistUseCase, getTaskChecklistsUseCase,
                updateCommentUseCase, deleteCommentUseCase, deleteAttachmentUseCase, getAttachmentViewUrlUseCase,
                repository
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
        tabLayout = findViewById(R.id.tabLayout);
        ImageView ivProjectMenu = findViewById(R.id.ivProjectMenu);

        if (projectName != null && !projectName.isEmpty()) {
            tvProjectName.setText(projectName);
        }
        
      

        backButton.setOnClickListener(v -> {
            // Just finish() to return to previous WorkspaceActivity (don't create new one)
            finish();
        });
        
        // Click vào icon 3 chấm để mở Project Menu
        ivProjectMenu.setOnClickListener(v -> {
            showProjectMenu();
        });
    }
    
    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0: // Board tab
                        showBoardView();
                        break;
                    case 1: // Calendar tab
                        showCalendarFragment();
                        break;
                    case 2: // Event tab
                        // Dev 2 will handle
                        Toast.makeText(ProjectActivity.this, 
                            "Event tab - Coming soon", 
                            Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void showBoardView() {
        boardsRecyclerView.setVisibility(View.VISIBLE);
        
        // Hide calendar fragment if showing
        androidx.fragment.app.Fragment calendarFragment = getSupportFragmentManager()
            .findFragmentByTag("calendar_fragment");
        if (calendarFragment != null) {
            getSupportFragmentManager().beginTransaction()
                .hide(calendarFragment)
                .commit();
        }
    }
    
    private void showCalendarFragment() {
        boardsRecyclerView.setVisibility(View.GONE);
        
        androidx.fragment.app.Fragment calendarFragment = getSupportFragmentManager()
            .findFragmentByTag("calendar_fragment");
        
        if (calendarFragment == null) {
            calendarFragment = ProjectCalendarFragment.newInstance(projectId);
            getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, calendarFragment, "calendar_fragment")
                .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                .show(calendarFragment)
                .commit();
        }
    }
    
    private void showProjectMenu() {
        com.example.tralalero.feature.home.ui.Home.project.ProjectMenuBottomSheet bottomSheet = 
            com.example.tralalero.feature.home.ui.Home.project.ProjectMenuBottomSheet.newInstance(projectId, projectName);
        bottomSheet.show(getSupportFragmentManager(), "ProjectMenuBottomSheet");
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false
        );
        boardsRecyclerView.setLayoutManager(layoutManager);

        boardAdapter = new BoardAdapter(this); // ✅ Pass 'this' as listener

        boardsRecyclerView.setAdapter(boardAdapter);
    }
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
    public void onTaskPositionChanged(Task task, double newPosition, Board board) {
        Log.d(TAG, "🔄 Task '" + task.getTitle() + "' updating position to " + newPosition);
        taskViewModel.updateTaskPosition(task.getId(), newPosition);
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            loadTasksForBoard(board.getId());
        }, 300);
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
        CreateTaskBottomSheet bottomSheet = CreateTaskBottomSheet.newInstance();
        bottomSheet.setOnTaskCreatedListener(title -> {
            // Create task with just title
            Task newTask = new Task(
                null,              // id - will be generated by backend
                projectId,         // projectId
                board.getId(),     // boardId
                title,             // title from dialog
                "",                // empty description
                null,              // issueKey
                null,              // type
                Task.TaskStatus.TO_DO,    // default status
                Task.TaskPriority.MEDIUM, // default priority
                0.0,               // position (double)
                null,              // assigneeId
                App.tokenManager.getInternalUserId(), // createdBy
                null,              // sprintId
                null,              // epicId
                null,              // parentTaskId
                null,              // startAt
                null,              // dueAt
                null,              // storyPoints
                null,              // originalEstimateSec
                null,              // remainingEstimateSec
                null,              // createdAt
                null               // updatedAt
            );

            // Create task via API
            taskViewModel.createTask(newTask);
            
            // Observe creation result via selectedTaskLiveData
            taskViewModel.getSelectedTask().observe(this, createdTask -> {
                if (createdTask != null && createdTask.getTitle().equals(title)) {
                    Toast.makeText(this, "✅ Task created: " + createdTask.getTitle(), Toast.LENGTH_SHORT).show();
                    
                    // Open task detail for editing
                    Intent intent = new Intent(this, CardDetailActivity.class);
                    intent.putExtra(CardDetailActivity.EXTRA_TASK_ID, createdTask.getId());
                    intent.putExtra(CardDetailActivity.EXTRA_BOARD_ID, board.getId());
                    intent.putExtra(CardDetailActivity.EXTRA_PROJECT_ID, projectId);
                    intent.putExtra(CardDetailActivity.EXTRA_IS_EDIT_MODE, true);
                    intent.putExtra(CardDetailActivity.EXTRA_BOARD_NAME, board.getName());
                    intent.putExtra("board_id_for_reload", board.getId());
                    startActivityForResult(intent, REQUEST_CODE_EDIT_TASK);
                    
                    // Reload tasks
                    loadTasksForBoard(board.getId());
                    
                    // Remove observer to prevent multiple triggers
                    taskViewModel.getSelectedTask().removeObservers(this);
                }
            });
            
            taskViewModel.getError().observe(this, error -> {
                if (error != null && !error.isEmpty()) {
                    Toast.makeText(this, "❌ Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        bottomSheet.show(getSupportFragmentManager(), "CreateTaskBottomSheet");
    }

    private void showTaskDetailBottomSheet(Task task, Board board) {
        Intent intent = new Intent(this, CardDetailActivity.class);
        intent.putExtra(CardDetailActivity.EXTRA_TASK_ID, task.getId());
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_ID, board.getId());
        intent.putExtra(CardDetailActivity.EXTRA_PROJECT_ID, projectId);
        intent.putExtra(CardDetailActivity.EXTRA_IS_EDIT_MODE, true);
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_NAME, board.getName()); // Pass board name
        intent.putExtra(CardDetailActivity.EXTRA_TASK_TITLE, task.getTitle());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_DESCRIPTION, task.getDescription());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_PRIORITY, task.getPriority() != null ? task.getPriority().name() : "MEDIUM");
        intent.putExtra(CardDetailActivity.EXTRA_TASK_STATUS, task.getStatus() != null ? task.getStatus().name() : "TO_DO");
        intent.putExtra(CardDetailActivity.EXTRA_TASK_POSITION, task.getPosition());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_ASSIGNEE_ID, task.getAssigneeId());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_CREATED_BY, task.getCreatedBy());
        intent.putExtra("board_id_for_reload", board.getId());

        startActivityForResult(intent, REQUEST_CODE_EDIT_TASK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CREATE_TASK || requestCode == REQUEST_CODE_EDIT_TASK) {
                final String boardIdForReload;
                if (data != null) {
                    boardIdForReload = data.getStringExtra("board_id_for_reload");
                } else {
                    boardIdForReload = null;
                }
                if (boardIdForReload != null && !boardIdForReload.isEmpty()) {
                    Log.d(TAG, "🔄 Reloading tasks for board: " + boardIdForReload);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        loadTasksForBoard(boardIdForReload);
                    }, 500);
                } else {
                    Log.d(TAG, "🔄 Reloading all boards");
                    loadBoards();
                }
            }
        }
    }

    @Override
    public void onMoveTaskToBoard(Task task, Board currentBoard, int direction) {
        Log.d(TAG, "onMoveTaskToBoard: task=" + task.getTitle() + ", currentBoard=" + currentBoard.getName() + ", direction=" + direction);
        int currentBoardIndex = -1;
        for (int i = 0; i < boards.size(); i++) {
            if (boards.get(i).getId().equals(currentBoard.getId())) {
                currentBoardIndex = i;
                break;
            }
        }

        if (currentBoardIndex == -1) {
            Log.e(TAG, "Current board not found in boards list");
            Toast.makeText(this, "Error: Board not found", Toast.LENGTH_SHORT).show();
            return;
        }
        int targetBoardIndex = currentBoardIndex + direction;
        if (targetBoardIndex < 0) {
            Toast.makeText(this, "Cannot move left - already at first board", Toast.LENGTH_SHORT).show();
            return;
        }

        if (targetBoardIndex >= boards.size()) {
            Toast.makeText(this, "Cannot move right - already at last board", Toast.LENGTH_SHORT).show();
            return;
        }
        Board targetBoard = boards.get(targetBoardIndex);

        Log.d(TAG, "Moving task '" + task.getTitle() + "' from '" + currentBoard.getName() + "' to '" + targetBoard.getName() + "'");
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        taskViewModel.moveTaskToBoard(task.getId(), targetBoard.getId(), 0.0);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            loadTasksForBoard(currentBoard.getId());
            loadTasksForBoard(targetBoard.getId());

            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }

            Toast.makeText(this, "Moved to " + targetBoard.getName(), Toast.LENGTH_SHORT).show();
        }, 500);
    }
}
