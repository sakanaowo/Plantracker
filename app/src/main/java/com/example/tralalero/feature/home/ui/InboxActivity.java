package com.example.tralalero.feature.home.ui;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.tralalero.R;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.repository.ITaskRepository;
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
 */
public class InboxActivity extends com.example.tralalero.feature.home.ui.BaseActivity {
    
    private static final String TAG = "InboxActivity";
    
    // ViewModels
    private TaskViewModel taskViewModel;
    
    // UI Components
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private TextView emptyView;
    private RelativeLayout notiLayout;
    private LinearLayout inboxQuickAccess;
    private TextInputEditText inboxAddCard;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.inbox_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize ViewModels
        setupViewModel();
        
        // Initialize UI
        initViews();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup Notification card
        setupNotificationCard();
        
        // Setup Quick Add Task
        setupQuickAddTask();
        
        // Observe ViewModel
        observeViewModel();
        
        // Setup Bottom Navigation
        setupBottomNavigation(1); // Inbox tab index = 1
        
        // Load all tasks
        loadAllTasks();
    }
    
    /**
     * Initialize ViewModel with Factory
     */
    private void setupViewModel() {
        // Create repository
        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        ITaskRepository repository = new TaskRepositoryImpl(apiService);
        
        // Create all 15 UseCases
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
        
        // Create Factory with all UseCases
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
        
        // Create ViewModel
        taskViewModel = new ViewModelProvider(this, factory).get(TaskViewModel.class);
        Log.d(TAG, "TaskViewModel initialized");
    }
    
    /**
     * Initialize UI components
     */
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewInbox);
        notiLayout = findViewById(R.id.notificationLayout);
        inboxQuickAccess = findViewById(R.id.inboxQuickAccess);
        inboxAddCard = findViewById(R.id.inboxAddCard);
        
        // TODO: Add empty view to inbox_main.xml
        // emptyView = findViewById(R.id.tvEmptyInbox);
    }
    
    /**
     * Setup RecyclerView with TaskAdapter
     */
    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(task -> {
            // Task clicked - show detail bottom sheet
            showTaskDetailBottomSheet(task);
        });
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);
        
        Log.d(TAG, "RecyclerView setup complete");
    }
    
    /**
     * Setup notification card (dismissible)
     */
    private void setupNotificationCard() {
        ImageButton btnCloseNotification = findViewById(R.id.btnClosePjrDetail);
        btnCloseNotification.setOnClickListener(v -> notiLayout.setVisibility(View.GONE));
    }
    
    /**
     * Setup Quick Add Task functionality
     */

    private void setupQuickAddTask() {
        // Show quick add form when clicking input
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
                // Create new task
                createTask(taskTitle);
                
                // Hide form and clear input
                inboxQuickAccess.setVisibility(View.GONE);
                inboxAddCard.setText("");
            } else {
                Toast.makeText(this, "Vui lòng nhập tên task!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Observe TaskViewModel LiveData
     */
    private void observeViewModel() {
        // Observe tasks list
        taskViewModel.getTasks().observe(this, tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                taskAdapter.setTasks(tasks);
                recyclerView.setVisibility(View.VISIBLE);
                // if (emptyView != null) emptyView.setVisibility(View.GONE);
                Log.d(TAG, "Tasks loaded: " + tasks.size());
            } else {
                taskAdapter.setTasks(new ArrayList<>());
                recyclerView.setVisibility(View.GONE);
                // if (emptyView != null) {
                //     emptyView.setVisibility(View.VISIBLE);
                //     emptyView.setText("No tasks in inbox");
                // }
                Log.d(TAG, "No tasks found");
            }
        });
        
        // Observe loading state
        taskViewModel.isLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                Log.d(TAG, "Loading tasks...");
                // TODO: Show loading indicator
            } else {
                Log.d(TAG, "Loading complete");
                // TODO: Hide loading indicator
            }
        });
        
        // Observe errors
        taskViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error: " + error);
            }
        });
    }
    
    /**
     * Load all tasks (inbox shows all tasks across projects)
     */
    private void loadAllTasks() {
        // TODO: For now, we need a way to get all tasks
        // Option 1: Load tasks from a specific "Inbox" board
        // Option 2: Implement GetAllTasksUseCase
        // Option 3: Load tasks from default project/board
        
        // Placeholder: Load tasks from first board (will need proper implementation)
        String defaultBoardId = "inbox-board-id"; // TODO: Get from preferences or API
        taskViewModel.loadTasksByBoard(defaultBoardId);
        
        Log.d(TAG, "Loading all inbox tasks");
    }
    
    /**
     * Create a new task
     */
    private void createTask(String title) {
        // Create Task object with minimal info
        Task newTask = new Task(
            null,                    // id - will be generated by backend
            null,                    // projectId - inbox has no specific project
            "inbox-board-id",        // boardId - inbox board
            title,                   // title
            "",                      // description
            null,                    // issueKey
            null,                    // type
            null,                    // status - will default to TO_DO
            null,                    // priority
            0.0,                     // position
            null,                    // assigneeId
            null,                    // createdBy - should get from current user
            null,                    // sprintId
            null,                    // epicId
            null,                    // parentTaskId
            null,                    // startAt
            null,                    // dueAt
            null,                    // storyPoints
            null,                    // originalEstimateSec
            null,                    // remainingEstimateSec
            null,                    // createdAt
            null                     // updatedAt
        );
        
        taskViewModel.createTask(newTask);
        Toast.makeText(this, "Đã thêm task: " + title, Toast.LENGTH_SHORT).show();
        
        // Reload tasks after creation
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            loadAllTasks();
        }, 500);
    }
    
    /**
     * Show TaskDetailBottomSheet with task actions
     */
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
    
    // ===== TASK ACTION HANDLERS =====
    
    /**
     * Handle assign task action
     */
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
    
    /**
     * Handle move task action
     */
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
    
    /**
     * Handle add comment action
     */
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
    
    /**
     * Handle delete task action
     */
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