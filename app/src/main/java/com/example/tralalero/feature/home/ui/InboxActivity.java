package com.example.tralalero.feature.home.ui;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.tralalero.R;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.data.repository.TaskRepositoryImplWithCache;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.domain.repository.ITaskRepository.RepositoryCallback;
import com.example.tralalero.domain.usecase.task.*;
import com.example.tralalero.feature.home.ui.Home.project.TaskAdapter;
import com.example.tralalero.feature.home.ui.Home.project.TaskDetailBottomSheet;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.presentation.viewmodel.TaskViewModel;
import com.example.tralalero.presentation.viewmodel.TaskViewModelFactory;
import com.example.tralalero.App.App;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class InboxActivity extends com.example.tralalero.feature.home.ui.BaseActivity {
    private static final String TAG = "InboxActivity";
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_BOARD_ID = "board_id";

    private TaskViewModel taskViewModel;
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private RelativeLayout notiLayout;
    private LinearLayout inboxQuickAccess;
    private TextInputEditText inboxAddCard;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View loadingView;
    private View emptyView;
    private boolean isInitialLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.inbox_main);
        View toolbar = findViewById(R.id.topAppBar);
        View bottomNav = findViewById(R.id.bottomNavigation);

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return WindowInsetsCompat.CONSUMED;
        });

        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        setupViewModel();
        initViews();
        setupRecyclerView();
        setupSwipeRefresh();  // ← ADDED: Setup pull-to-refresh
        setupNotificationCard();
        setupQuickAddTask();
        observeViewModel();
        setupBottomNavigation(1); 
        loadAllTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() - Reloading all tasks");
        loadAllTasks();
    }

    private void setupViewModel() {
        TaskRepositoryImplWithCache repositoryWithCache = 
            App.dependencyProvider.getTaskRepositoryWithCache();
        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        ITaskRepository apiRepository = new TaskRepositoryImpl(apiService);

        GetTaskByIdUseCase getTaskByIdUseCase = new GetTaskByIdUseCase(apiRepository);
        GetTasksByBoardUseCase getTasksByBoardUseCase = new GetTasksByBoardUseCase(apiRepository);
        CreateTaskUseCase createTaskUseCase = new CreateTaskUseCase(apiRepository);
        UpdateTaskUseCase updateTaskUseCase = new UpdateTaskUseCase(apiRepository);
        DeleteTaskUseCase deleteTaskUseCase = new DeleteTaskUseCase(apiRepository);
        AssignTaskUseCase assignTaskUseCase = new AssignTaskUseCase(apiRepository);
        UnassignTaskUseCase unassignTaskUseCase = new UnassignTaskUseCase(apiRepository);
        MoveTaskToBoardUseCase moveTaskToBoardUseCase = new MoveTaskToBoardUseCase(apiRepository);
        UpdateTaskPositionUseCase updateTaskPositionUseCase = new UpdateTaskPositionUseCase(apiRepository);
        AddCommentUseCase addCommentUseCase = new AddCommentUseCase(apiRepository);
        GetTaskCommentsUseCase getTaskCommentsUseCase = new GetTaskCommentsUseCase(apiRepository);
        AddAttachmentUseCase addAttachmentUseCase = new AddAttachmentUseCase(apiRepository);
        GetTaskAttachmentsUseCase getTaskAttachmentsUseCase = new GetTaskAttachmentsUseCase(apiRepository);
        AddChecklistUseCase addChecklistUseCase = new AddChecklistUseCase(apiRepository);
        GetTaskChecklistsUseCase getTaskChecklistsUseCase = new GetTaskChecklistsUseCase(apiRepository);

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
        taskViewModel = new ViewModelProvider(this, factory).get(TaskViewModel.class);
        Log.d(TAG, "TaskViewModel initialized with cache-enabled repository");
    }
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewInbox);
        notiLayout = findViewById(R.id.notificationLayout);
        inboxQuickAccess = findViewById(R.id.inboxQuickAccess);
        inboxAddCard = findViewById(R.id.inboxAddCard);
        loadingView = findViewById(R.id.loadingView);
        emptyView = findViewById(R.id.emptyView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }
    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(task -> {
            showTaskDetailBottomSheet(task);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);
        Log.d(TAG, "RecyclerView setup complete");
    }
    private void setupNotificationCard() {
        ImageButton btnCloseNotification = findViewById(R.id.btnClosePjrDetail);
        btnCloseNotification.setOnClickListener(v -> notiLayout.setVisibility(View.GONE));
    }
    private void setupQuickAddTask() {
        inboxAddCard.setOnClickListener(v -> inboxQuickAccess.setVisibility(View.VISIBLE));
        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
            inboxQuickAccess.setVisibility(View.GONE);
            inboxAddCard.setText("");
        });
        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            String taskTitle = inboxAddCard.getText().toString().trim();
            if (!taskTitle.isEmpty()) {
                createTask(taskTitle);
                inboxQuickAccess.setVisibility(View.GONE);
                inboxAddCard.setText("");
            } else {
                Toast.makeText(this, "Vui lòng nhập tên task!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void observeViewModel() {
        taskViewModel.getTasks().observe(this, tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                taskAdapter.setTasks(tasks);
                showContent();
                Log.d(TAG, "Tasks loaded: " + tasks.size());
            } else {
                taskAdapter.setTasks(new ArrayList<>());
                showEmpty();
                Log.d(TAG, "No tasks found");
            }
        });
        taskViewModel.getSelectedTask().observe(this, createdTask -> {
            if (createdTask != null) {
                Log.d(TAG, "New task created: " + createdTask.getTitle() + " (ID: " + createdTask.getId() + ")");
                App.dependencyProvider.getTaskRepositoryWithCache().saveTaskToCache(createdTask);
                Log.d(TAG, "✓ Task saved to local database cache");
                if (createdTask.getId() != null && createdTask.getBoardId() != null) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(EXTRA_TASK_ID, createdTask.getId());
                    resultIntent.putExtra(EXTRA_BOARD_ID, createdTask.getBoardId());
                    setResult(RESULT_OK, resultIntent);
                    Log.d(TAG, "� Result set: Task " + createdTask.getId() + " on board " + createdTask.getBoardId());
                }
                List<Task> currentTasks = taskViewModel.getTasks().getValue();
                if (currentTasks == null) {
                    currentTasks = new ArrayList<>();
                }
                boolean taskExists = false;
                for (Task task : currentTasks) {
                    if (task.getId() != null && task.getId().equals(createdTask.getId())) {
                        taskExists = true;
                        break;
                    }
                }

                if (!taskExists) {
                    List<Task> updatedTasks = new ArrayList<>();
                    updatedTasks.add(createdTask);
                    updatedTasks.addAll(currentTasks);

                    taskAdapter.setTasks(updatedTasks);
                    showContent();
                    recyclerView.smoothScrollToPosition(0);

                    Toast.makeText(this, "✅ Đã thêm task vào database: " + createdTask.getTitle(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "New task added to UI list: " + createdTask.getTitle());
                }
                taskViewModel.clearSelectedTask();
            }
        });
        taskViewModel.isLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                Log.d(TAG, "Loading tasks...");
            } else {
                Log.d(TAG, "Loading complete");
            }
        });
        taskViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "❌ Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error: " + error);
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
            );
            swipeRefreshLayout.setOnRefreshListener(() -> {
                Log.d(TAG, "User triggered pull-to-refresh for tasks");
                isInitialLoad = false;  // Not initial load anymore
                forceRefreshTasks();
            });
        }
    }
    
    private void showLoading() {
        swipeRefreshLayout.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
        Log.d(TAG, "Showing loading view");
    }
    
    private void showContent() {
        loadingView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        Log.d(TAG, "Showing content");
    }
    
    private void showEmpty() {
        loadingView.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        Log.d(TAG, "Showing empty view");
    }

    private void forceRefreshTasks() {
        Log.d(TAG, "Force refreshing tasks from API...");
        App.dependencyProvider.clearTaskCache();
        isInitialLoad = false;  // Not initial load
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        loadAllTasks();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
                Log.d(TAG, "Refresh timeout");
            }
        }, 5000);
    }

    private void loadAllTasks() {
        // Show loading only if no cache and initial load
        if (isInitialLoad) {
            showLoading();
        }
        
        Log.d(TAG, "Loading quick tasks with cache...");
        final long startTime = System.currentTimeMillis();
        App.dependencyProvider.getTaskRepositoryWithCache()
            .getAllTasks(new TaskRepositoryImplWithCache.TaskCallback() {
                @Override
                public void onSuccess(List<Task> tasks) {
                    long duration = System.currentTimeMillis() - startTime;

                    runOnUiThread(() -> {
                        if (tasks != null && !tasks.isEmpty()) {
                            taskAdapter.setTasks(tasks);
                            showContent();
                            String message = "⚡ Cache: " + duration + "ms (" + tasks.size() + " tasks)";
                            Toast.makeText(InboxActivity.this, message, Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "CACHE HIT: " + duration + "ms, " + tasks.size() + " tasks");
                        }
                    });
                    loadQuickTasksFromApi();
                }

                @Override
                public void onCacheEmpty() {
                    runOnUiThread(() -> {
                        Log.d(TAG, "Cache empty - loading from API...");
                        // Keep showing loading since cache is empty
                    });
                    loadQuickTasksFromApi();
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Cache error, loading from API...", e);
                    });
                    loadQuickTasksFromApi();
                }
            });
    }

    private void loadQuickTasksFromApi() {
        Log.d(TAG, "Fetching quick tasks from API...");
        final long apiStartTime = System.currentTimeMillis();
        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        ITaskRepository apiRepository = new TaskRepositoryImpl(apiService);

        apiRepository.getQuickTasks(new RepositoryCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> tasks) {
                long apiDuration = System.currentTimeMillis() - apiStartTime;

                runOnUiThread(() -> {
                    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    if (tasks != null && !tasks.isEmpty()) {
                        taskAdapter.setTasks(tasks);
                        showContent();

                        String message = "🌐 API: " + apiDuration + "ms (" + tasks.size() + " tasks)";
                        Toast.makeText(InboxActivity.this, message, Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "API SUCCESS: " + apiDuration + "ms, " + tasks.size() + " quick tasks loaded");
                        App.dependencyProvider.getTaskRepositoryWithCache().saveTasksToCache(tasks);
                        Log.d(TAG, "✓ Quick tasks saved to cache");
                    } else {
                        taskAdapter.setTasks(new ArrayList<>());
                        showEmpty();
                        Log.d(TAG, "No quick tasks found");
                    }
                    
                    isInitialLoad = false;  // Initial load complete
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    // Show empty view on error if no tasks loaded
                    if (taskAdapter.getTasks().isEmpty()) {
                        showEmpty();
                    }

                    Toast.makeText(InboxActivity.this,
                            "Failed to load quick tasks: " + error,
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "API ERROR: " + error);
                    
                    isInitialLoad = false;  // Initial load complete (even with error)
                });
            }
        });
    };

        private void createTask(String title) {
        Log.d(TAG, "Creating quick task: " + title);
        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        ITaskRepository taskRepository = new TaskRepositoryImpl(apiService);

        taskRepository.createQuickTask(title, "", new ITaskRepository.RepositoryCallback<Task>() {
            @Override
            public void onSuccess(Task result) {
                runOnUiThread(() -> {
                    Log.d(TAG, "✓ Quick task created successfully");
                    Log.d(TAG, "  ID: " + result.getId());
                    Log.d(TAG, "  Title: " + result.getTitle());
                    Log.d(TAG, "  Project: " + result.getProjectId());
                    Log.d(TAG, "  Board: " + result.getBoardId());
                    App.dependencyProvider.getTaskRepositoryWithCache().saveTaskToCache(result);
                    Log.d(TAG, "✓ Task saved to cache");
                    List<Task> currentTasks = taskAdapter.getTasks();
                    if (currentTasks == null) {
                        currentTasks = new ArrayList<>();
                    }
                    boolean taskExists = false;
                    for (Task task : currentTasks) {
                        if (task.getId() != null && task.getId().equals(result.getId())) {
                            taskExists = true;
                            break;
                        }
                    }

                    if (!taskExists) {
                        List<Task> updatedTasks = new ArrayList<>();
                        updatedTasks.add(result);
                        updatedTasks.addAll(currentTasks);

                        taskAdapter.setTasks(updatedTasks);
                        showContent();
                        recyclerView.smoothScrollToPosition(0);

                        Log.d(TAG, "✓ Task added to RecyclerView immediately");
                    }
                    loadAllTasks();
                    android.widget.Toast.makeText(InboxActivity.this, 
                        "✓ Task created", 
                        android.widget.Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Failed to create quick task: " + error);
                    android.widget.Toast.makeText(InboxActivity.this, 
                        "Error creating task: " + error, 
                        android.widget.Toast.LENGTH_LONG).show();
                });
            }
        });

        Log.d(TAG, "Quick task creation request sent");
    }

    private void showTaskDetailBottomSheet(Task task) {
        TaskDetailBottomSheet bottomSheet = TaskDetailBottomSheet.newInstance(task);
        bottomSheet.setOnActionClickListener(new TaskDetailBottomSheet.OnActionClickListener() {
            @Override
            public void onAssignTask(Task task) {
                handleAssignTask(task);
            }
            @Override
            public void onMoveTask(Task task) {
                handleMoveTask(task);
            }
            @Override
            public void onAddComment(Task task) {
                handleAddComment(task);
            }
            @Override
            public void onDeleteTask(Task task) {
                handleDeleteTask(task);
            }
            @Override
            public void onUpdateStartDate(Task task, java.util.Date startDate) {
                handleUpdateStartDate(task, startDate);
            }
            @Override
            public void onUpdateDueDate(Task task, java.util.Date dueDate) {
                handleUpdateDueDate(task, dueDate);
            }
        });
        bottomSheet.show(getSupportFragmentManager(), "TaskDetailBottomSheet");
    }
    private void handleAssignTask(Task task) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter user ID");
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Assign Task")
            .setMessage("Assign \"" + task.getTitle() + "\" to user:")
            .setView(input)
            .setPositiveButton("Assign", (dialog, which) -> {
                String userId = input.getText().toString().trim();
                if (!userId.isEmpty()) {
                    taskViewModel.assignTask(task.getId(), userId);
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        loadAllTasks();
                        Toast.makeText(this, "Task assigned successfully", Toast.LENGTH_SHORT).show();
                    }, 500);
                } else {
                    Toast.makeText(this, "User ID cannot be empty", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    private void handleMoveTask(Task task) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter target board ID");
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Move Task")
            .setMessage("Move \"" + task.getTitle() + "\" to board:")
            .setView(input)
            .setPositiveButton("Move", (dialog, which) -> {
                String targetBoardId = input.getText().toString().trim();
                if (!targetBoardId.isEmpty()) {
                    taskViewModel.moveTaskToBoard(task.getId(), targetBoardId, 0);
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        loadAllTasks();
                        Toast.makeText(this, "Task moved successfully", Toast.LENGTH_SHORT).show();
                    }, 500);
                } else {
                    Toast.makeText(this, "Board ID cannot be empty", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    private void handleAddComment(Task task) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter your comment");
        input.setMinLines(3);
        input.setMaxLines(5);
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add Comment")
            .setMessage("Add comment to \"" + task.getTitle() + "\":")
            .setView(input)
            .setPositiveButton("Add", (dialog, which) -> {
                String commentText = input.getText().toString().trim();
                if (!commentText.isEmpty()) {
                    com.example.tralalero.domain.model.TaskComment comment = 
                        new com.example.tralalero.domain.model.TaskComment(
                            null,
                            task.getId(),
                            null,
                            commentText,
                            new java.util.Date()
                        );
                    taskViewModel.addComment(task.getId(), comment);
                    Toast.makeText(this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    private void handleDeleteTask(Task task) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete \"" + task.getTitle() + "\"?")
            .setPositiveButton("Delete", (dialog, which) -> {
                taskViewModel.deleteTask(task.getId());
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    loadAllTasks();
                    Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                }, 500);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void handleUpdateStartDate(Task task, java.util.Date startDate) {
        Log.d(TAG, "handleUpdateStartDate: " + task.getId() + " -> " + startDate);
        
        // Create updated task with new start date
        Task updatedTask = new Task(
            task.getId(),
            task.getProjectId(),
            task.getBoardId(),
            task.getTitle(),
            task.getDescription(),
            task.getIssueKey(),
            task.getType(),
            task.getStatus(),
            task.getPriority(),
            task.getPosition(),
            task.getAssigneeId(),
            task.getCreatedBy(),
            task.getSprintId(),
            task.getEpicId(),
            task.getParentTaskId(),
            startDate,  // Updated start date
            task.getDueAt(),
            task.getStoryPoints(),
            task.getOriginalEstimateSec(),
            task.getRemainingEstimateSec(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
        
        // Update via ViewModel
        taskViewModel.updateTask(task.getId(), updatedTask);
        
        // Save to cache
        App.dependencyProvider.getTaskRepositoryWithCache().saveTaskToCache(updatedTask);
        
        // Reload after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            loadAllTasks();
        }, 1000);
        
        Toast.makeText(this, "Start date updated", Toast.LENGTH_SHORT).show();
    }
    
    private void handleUpdateDueDate(Task task, java.util.Date dueDate) {
        Log.d(TAG, "handleUpdateDueDate: " + task.getId() + " -> " + dueDate);
        
        // Create updated task with new due date
        Task updatedTask = new Task(
            task.getId(),
            task.getProjectId(),
            task.getBoardId(),
            task.getTitle(),
            task.getDescription(),
            task.getIssueKey(),
            task.getType(),
            task.getStatus(),
            task.getPriority(),
            task.getPosition(),
            task.getAssigneeId(),
            task.getCreatedBy(),
            task.getSprintId(),
            task.getEpicId(),
            task.getParentTaskId(),
            task.getStartAt(),
            dueDate,  // Updated due date
            task.getStoryPoints(),
            task.getOriginalEstimateSec(),
            task.getRemainingEstimateSec(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
        
        // Update via ViewModel
        taskViewModel.updateTask(task.getId(), updatedTask);
        
        // Save to cache
        App.dependencyProvider.getTaskRepositoryWithCache().saveTaskToCache(updatedTask);
        
        // Reload after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            loadAllTasks();
        }, 1000);
        
        Toast.makeText(this, "Due date updated", Toast.LENGTH_SHORT).show();
    }
}

