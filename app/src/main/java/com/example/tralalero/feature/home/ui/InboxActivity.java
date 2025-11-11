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
import com.example.tralalero.data.remote.api.CommentApiService;
import com.example.tralalero.data.remote.api.AttachmentApiService;
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
        setupSwipeRefresh();
        setupNotificationCard();
        setupQuickAddTask();
        observeViewModel(); // ✅ Must be BEFORE loadInboxTasks
        setupBottomNavigation(1);
        
        // ✅ NEW: Load inbox tasks once via ViewModel
        // Note: loadInboxTasks is currently a stub, will be implemented later
        taskViewModel.loadInboxTasks("");
    }

    // ✅ REMOVED: No more onResume reload!
    // ViewModel keeps state, no need to reload on resume

    private void setupViewModel() {
        TaskRepositoryImplWithCache repositoryWithCache = 
            App.dependencyProvider.getTaskRepositoryWithCache();
        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        CommentApiService commentApiService = ApiClient.get(App.authManager).create(CommentApiService.class);
        AttachmentApiService attachmentApiService = ApiClient.get(App.authManager).create(AttachmentApiService.class);
        ITaskRepository apiRepository = new TaskRepositoryImpl(apiService, commentApiService, attachmentApiService);

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
        UpdateCommentUseCase updateCommentUseCase = new UpdateCommentUseCase(apiRepository);
        DeleteCommentUseCase deleteCommentUseCase = new DeleteCommentUseCase(apiRepository);
        DeleteAttachmentUseCase deleteAttachmentUseCase = new DeleteAttachmentUseCase(apiRepository);
        GetAttachmentViewUrlUseCase getAttachmentViewUrlUseCase = new GetAttachmentViewUrlUseCase(apiRepository);

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
            getTaskChecklistsUseCase,
            updateCommentUseCase,
            deleteCommentUseCase,
            deleteAttachmentUseCase,
            getAttachmentViewUrlUseCase,
            apiRepository
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
        taskAdapter = new TaskAdapter(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                showTaskDetailBottomSheet(task);
            }
            
            @Override
            public void onTaskCompleted(Task task) {
                handleTaskCompleted(task);
            }
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
        // ✅ NEW: Observe inbox tasks from ViewModel (single source of truth)
        taskViewModel.getInboxTasks().observe(this, tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                taskAdapter.setTasks(tasks);
                showContent();
                Log.d(TAG, "✅ Inbox tasks updated: " + tasks.size());
            } else {
                taskAdapter.setTasks(new ArrayList<>());
                showEmpty();
                Log.d(TAG, "No inbox tasks");
            }
        });
        
        // ✅ Observe loading state (optional - can show spinner)
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
                Log.d(TAG, "✅ Pull-to-refresh via ViewModel");
                taskViewModel.loadInboxTasks("");
                // Stop refreshing animation (observer will update UI)
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                }, 500);
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

    // ✅ REMOVED: loadAllTasks(), loadQuickTasksFromApi(), forceRefreshTasks()
    // All replaced by taskViewModel.loadInboxTasks() + observer pattern

    /**
     * ✅ NEW: Create task via ViewModel with optimistic update
     * This replaces direct API call pattern
     */
    private void createTask(String title) {
        Log.d(TAG, "✅ Creating quick task via ViewModel: " + title);
        
        // Create new task object using immutable constructor (optimistic temp task)
        String tempId = "temp_" + System.currentTimeMillis();
        java.util.Date now = new java.util.Date();
        Task newTask = new Task(
            tempId,          // id (temp)
            null,            // projectId
            null,            // boardId (inbox)
            title,           // title
            "",             // description
            null,            // issueKey
            Task.TaskType.TASK,
            Task.TaskStatus.TO_DO,
            Task.TaskPriority.MEDIUM,
            0.0,             // position
            null,            // assigneeId
            null,            // createdBy
            null,            // sprintId
            null,            // epicId
            null,            // parentTaskId
            null,            // startAt
            null,            // dueAt
            null,            // storyPoints
            null,            // originalEstimateSec
            null,            // remainingEstimateSec
            now,             // createdAt
            now              // updatedAt
        );

        // ✅ Call ViewModel - optimistic update will show instantly
        taskViewModel.createTask(newTask);
        
        // Observer auto-updates UI via inboxTasksLiveData
        android.widget.Toast.makeText(this, 
            "✓ Creating task...", 
            android.widget.Toast.LENGTH_SHORT).show();
        
        Log.d(TAG, "✅ Task creation delegated to ViewModel (optimistic update)");
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
                    // ✅ REMOVED: loadAllTasks() - observer auto-updates
                    Toast.makeText(this, "Task assigned successfully", Toast.LENGTH_SHORT).show();
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
                    // ✅ REMOVED: loadAllTasks() - optimistic update handles it
                    Toast.makeText(this, "Task moved successfully", Toast.LENGTH_SHORT).show();
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
                // ✅ REMOVED: loadAllTasks() - optimistic delete removes instantly
                Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
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
        
        // ✅ REMOVED: Save to cache + reload - ViewModel handles via optimistic update
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
        
        // ✅ REMOVED: Reload - ViewModel optimistic update handles it
        Toast.makeText(this, "Due date updated", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * ✅ REFACTORED: Use ViewModel's toggleTaskComplete for instant checkbox
     * No manual UI update needed - observer auto-updates!
     */
    private void handleTaskCompleted(Task task) {
        Log.d(TAG, "✅ Toggle task complete via ViewModel: " + task.getId());
        
        // ✅ Single line! ViewModel handles optimistic update + API + rollback
        taskViewModel.toggleTaskComplete(task);
        
        // Observer automatically updates UI
        Toast.makeText(this, "✓ Task toggled", Toast.LENGTH_SHORT).show();
    }
}

