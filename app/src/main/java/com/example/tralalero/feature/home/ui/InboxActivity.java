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

/**
 * InboxActivity - Quick Access for all tasks
 * Integrates with TaskViewModel to display all tasks across projects
 * 
 * Features:
 * - Display all tasks in RecyclerView
 * - Quick add task functionality
 * - Task actions via TaskDetailBottomSheet
 * - Filter/Search capabilities (TODO)
 * 
 * @author Người 3
 * @date 15/10/2025
 * @updated October 19, 2025 - Added pull-to-refresh
 */
public class InboxActivity extends com.example.tralalero.feature.home.ui.BaseActivity {
    private static final String TAG = "InboxActivity";
    public static final String ACTION_TASK_CREATED = "com.example.tralalero.TASK_CREATED";
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_BOARD_ID = "board_id";
    
    private TaskViewModel taskViewModel;
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private TextView emptyView;
    private RelativeLayout notiLayout;
    private LinearLayout inboxQuickAccess;
    private TextInputEditText inboxAddCard;
    private SwipeRefreshLayout swipeRefreshLayout;  // ← ADDED

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.inbox_main);

        // Apply window insets properly - separate for toolbar and navigation
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
        // Reload tasks every time the activity becomes visible
        // This ensures tasks persist when navigating back from other screens
        Log.d(TAG, "onResume() - Reloading all tasks");
        loadAllTasks();
    }

    private void setupViewModel() {
        // Use cache-enabled repository for ViewModel
        // This ensures tasks created will be saved to local database
        TaskRepositoryImplWithCache repositoryWithCache = 
            App.dependencyProvider.getTaskRepositoryWithCache();
        
        // Create use cases with cache-enabled repository
        // Note: We still need TaskRepositoryImpl for API-only operations
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
        
        // TODO: Add empty view to inbox_main.xml
        // emptyView = findViewById(R.id.tvEmptyInbox);
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
        // 1. Observe all tasks list
        taskViewModel.getTasks().observe(this, tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                taskAdapter.setTasks(tasks);
                recyclerView.setVisibility(View.VISIBLE);
                Log.d(TAG, "Tasks loaded: " + tasks.size());
            } else {
                taskAdapter.setTasks(new ArrayList<>());
                recyclerView.setVisibility(View.GONE);
                Log.d(TAG, "No tasks found");
            }
        });

        // chuyen task moi tao sang project activity de them vao board
        // 2. Observe newly created task ✨
        taskViewModel.getSelectedTask().observe(this, createdTask -> {
            if (createdTask != null) {
                Log.d(TAG, "New task created: " + createdTask.getTitle() + " (ID: " + createdTask.getId() + ")");
                
                // Save task to cache immediately (Room database)
                App.dependencyProvider.getTaskRepositoryWithCache().saveTaskToCache(createdTask);
                Log.d(TAG, "✓ Task saved to local database cache");
                
                // � Set result to return task info to ProjectActivity
                if (createdTask.getId() != null && createdTask.getBoardId() != null) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(EXTRA_TASK_ID, createdTask.getId());
                    resultIntent.putExtra(EXTRA_BOARD_ID, createdTask.getBoardId());
                    setResult(RESULT_OK, resultIntent);
                    Log.d(TAG, "� Result set: Task " + createdTask.getId() + " on board " + createdTask.getBoardId());
                }
                
                // Add new task to top of list immediately
                List<Task> currentTasks = taskViewModel.getTasks().getValue();
                if (currentTasks == null) {
                    currentTasks = new ArrayList<>();
                }
                
                // Check if task already exists (avoid duplicate)
                boolean taskExists = false;
                for (Task task : currentTasks) {
                    if (task.getId() != null && task.getId().equals(createdTask.getId())) {
                        taskExists = true;
                        break;
                    }
                }
                
                if (!taskExists) {
                    // Add to top of list
                    List<Task> updatedTasks = new ArrayList<>();
                    updatedTasks.add(createdTask);
                    updatedTasks.addAll(currentTasks);
                    
                    taskAdapter.setTasks(updatedTasks);
                    recyclerView.setVisibility(View.VISIBLE);
                    
                    // Scroll to top to show new task
                    recyclerView.smoothScrollToPosition(0);
                    
                    Toast.makeText(this, "✅ Đã thêm task vào database: " + createdTask.getTitle(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "New task added to UI list: " + createdTask.getTitle());
                }
                
                // Clear selected task to avoid re-triggering
                taskViewModel.clearSelectedTask();
            }
        });
        
        // 3. Observe loading state
        taskViewModel.isLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                Log.d(TAG, "Loading tasks...");
                // TODO: Show loading indicator
            } else {
                Log.d(TAG, "Loading complete");
                // TODO: Hide loading indicator
            }
        });
        
        // 4. Observe error state
        taskViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "❌ Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error: " + error);
            }
        });
    }
    
    /**
     * Setup pull-to-refresh functionality
     *
     * @author Minor Issues Fix - October 19, 2025
     */
    private void setupSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        if (swipeRefreshLayout != null) {
            // Configure refresh colors
            swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
            );

            // Set refresh listener
            swipeRefreshLayout.setOnRefreshListener(() -> {
                Log.d(TAG, "User triggered pull-to-refresh for tasks");
                forceRefreshTasks();
            });
        }
    }

    /**
     * Force refresh tasks (bypass cache)
     * Called by pull-to-refresh
     */
    private void forceRefreshTasks() {
        Log.d(TAG, "Force refreshing tasks from API...");

        // Clear task cache
        App.dependencyProvider.clearTaskCache();

        // Show refreshing indicator
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        // Reload tasks (will fetch from API)
        loadAllTasks();

        // Timeout after 5 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
                Log.d(TAG, "Refresh timeout");
            }
        }, 5000);
    }

    /**
     * Load all quick tasks with cache
     * Cache-first approach: instant load from cache, then refresh from API
     * Uses backend's GET /api/tasks/quick/defaults endpoint
     * 
     * @author Person 2
     */
    private void loadAllTasks() {
        Log.d(TAG, "Loading quick tasks with cache...");
        final long startTime = System.currentTimeMillis();
        
        // Step 1: Try loading from cache first (instant)
        App.dependencyProvider.getTaskRepositoryWithCache()
            .getAllTasks(new TaskRepositoryImplWithCache.TaskCallback() {
                @Override
                public void onSuccess(List<Task> tasks) {
                    long duration = System.currentTimeMillis() - startTime;

                    runOnUiThread(() -> {
                        if (tasks != null && !tasks.isEmpty()) {
                            taskAdapter.setTasks(tasks);
                            recyclerView.setVisibility(View.VISIBLE);

                            // Performance logging
                            String message = "⚡ Cache: " + duration + "ms (" + tasks.size() + " tasks)";
                            Toast.makeText(InboxActivity.this, message, Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "CACHE HIT: " + duration + "ms, " + tasks.size() + " tasks");
                        }
                    });
                    
                    // Step 2: Always refresh from API in background
                    loadQuickTasksFromApi();
                }

                @Override
                public void onCacheEmpty() {
                    runOnUiThread(() -> {
                        Log.d(TAG, "Cache empty - loading from API...");
                    });
                    
                    // Step 2: Load from API if cache is empty
                    loadQuickTasksFromApi();
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Cache error, loading from API...", e);
                    });
                    
                    // Step 2: Load from API on cache error
                    loadQuickTasksFromApi();
                }
            });
    }
    
    /**
     * Load quick tasks from backend API endpoint: GET /api/tasks/quick/defaults
     * Backend automatically finds user's default board (To Do board)
     * After loading, saves to cache for next time
     */
    private void loadQuickTasksFromApi() {
        Log.d(TAG, "Fetching quick tasks from API...");
        final long apiStartTime = System.currentTimeMillis();
        
        // Create API repository
        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        ITaskRepository apiRepository = new TaskRepositoryImpl(apiService);
        
        apiRepository.getQuickTasks(new RepositoryCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> tasks) {
                long apiDuration = System.currentTimeMillis() - apiStartTime;
                
                runOnUiThread(() -> {
                    // Stop refreshing indicator
                    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    if (tasks != null && !tasks.isEmpty()) {
                        taskAdapter.setTasks(tasks);
                        recyclerView.setVisibility(View.VISIBLE);
                        
                        String message = "🌐 API: " + apiDuration + "ms (" + tasks.size() + " tasks)";
                        Toast.makeText(InboxActivity.this, message, Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "API SUCCESS: " + apiDuration + "ms, " + tasks.size() + " quick tasks loaded");
                        
                        // Save to cache for next time
                        App.dependencyProvider.getTaskRepositoryWithCache().saveTasksToCache(tasks);
                        Log.d(TAG, "✓ Quick tasks saved to cache");
                    } else {
                        taskAdapter.setTasks(new ArrayList<>());
                        recyclerView.setVisibility(View.GONE);
                        Log.d(TAG, "No quick tasks found");
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // Stop refreshing on error
                    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    Toast.makeText(InboxActivity.this,
                            "Failed to load quick tasks: " + error,
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "API ERROR: " + error);
                });
            }
        });
    };
    
        private void createTask(String title) {
        Log.d(TAG, "Creating quick task: " + title);
        
        // Create TaskRepositoryImpl directly with API service
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
                    
                    // Save to cache
                    App.dependencyProvider.getTaskRepositoryWithCache().saveTaskToCache(result);
                    Log.d(TAG, "✓ Task saved to cache");
                    
                    // Add task to RecyclerView immediately (optimistic UI update)
                    List<Task> currentTasks = taskAdapter.getTasks();
                    if (currentTasks == null) {
                        currentTasks = new ArrayList<>();
                    }
                    
                    // Check if task already exists (avoid duplicate)
                    boolean taskExists = false;
                    for (Task task : currentTasks) {
                        if (task.getId() != null && task.getId().equals(result.getId())) {
                            taskExists = true;
                            break;
                        }
                    }
                    
                    if (!taskExists) {
                        // Add to top of list for immediate visibility
                        List<Task> updatedTasks = new ArrayList<>();
                        updatedTasks.add(result);
                        updatedTasks.addAll(currentTasks);
                        
                        taskAdapter.setTasks(updatedTasks);
                        recyclerView.setVisibility(View.VISIBLE);
                        
                        // Scroll to top to show new task
                        recyclerView.smoothScrollToPosition(0);
                        
                        Log.d(TAG, "✓ Task added to RecyclerView immediately");
                    }
                    
                    // Reload all tasks in background to ensure consistency
                    loadAllTasks();
                    
                    // Show success message
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
}

