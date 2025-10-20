package com.example.tralalero.feature.home.ui.Home.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.tralalero.R;
import com.example.tralalero.App.App;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.domain.usecase.task.*;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.presentation.viewmodel.TaskViewModel;
import com.example.tralalero.presentation.viewmodel.TaskViewModelFactory;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Activity for creating and editing task details
 * Uses card_detail.xml layout
 * 
 * @author Modified from BottomSheet to Activity
 * @date October 20, 2025
 */
public class CardDetailActivity extends AppCompatActivity {

    // Intent extras
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_BOARD_ID = "board_id";
    public static final String EXTRA_BOARD_NAME = "board_name"; // NEW: Board name to display
    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_IS_EDIT_MODE = "is_edit_mode";
    
    // Intent extras for edit mode
    public static final String EXTRA_TASK_TITLE = "task_title";
    public static final String EXTRA_TASK_DESCRIPTION = "task_description";
    public static final String EXTRA_TASK_PRIORITY = "task_priority";
    public static final String EXTRA_TASK_STATUS = "task_status";
    public static final String EXTRA_TASK_POSITION = "task_position";
    public static final String EXTRA_TASK_ASSIGNEE_ID = "task_assignee_id";
    public static final String EXTRA_TASK_CREATED_BY = "task_created_by";

    // Data
    private String taskId;
    private String boardId;
    private String boardName;
    private String projectId;
    private boolean isEditMode;

    // ViewModel
    private TaskViewModel taskViewModel;

    // UI Components - Top bar
    private ImageView ivClose;
    private EditText etTaskTitle; // Changed from RadioButton to EditText
    private TextView tvBoardName; // NEW: Display board name

    // UI Components - Quick Actions
    private MaterialButton btnMembers;
    private MaterialButton btnAddAttachment;
    private MaterialButton btnAddComment;
    private MaterialButton btnDeleteTask;
    private MaterialButton btnConfirm; // NEW: Confirm button

    // UI Components - Input fields
    private EditText etDescription;
    private EditText etDateStart;
    private EditText etDueDate;
//    private TextView tvTimer;  // Commented out - Timer feature not needed
//    private ImageButton btnPlay;  // Commented out - Timer feature not needed
    
    // UI Components - Expandable sections
    private TextView tvTodolistHeader;
    private LinearLayout llTodolistContainer;
    private TextView tvAttachmentHeader;
    private LinearLayout llAttachmentContainer;
    private TextView tvCommentsHeader;
    private LinearLayout llCommentsContainer;
    private TextView tvNoComments;
    
    // UI Components - Priority (sẽ thêm vào layout)
    private MaterialButton btnLowPriority;
    private MaterialButton btnMediumPriority;
    private MaterialButton btnHighPriority;
    
    // Current selected priority
    private Task.TaskPriority currentPriority = Task.TaskPriority.MEDIUM;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_detail);

        // Get intent data
        getIntentData();

        // Initialize views
        initViews();

        // Setup ViewModel
        setupViewModel();

        // Setup UI based on mode
        setupUI();

        // Setup listeners
        setupListeners();
        
        // Setup back pressed callback
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Just close without saving - user can click Close button to save
                finish();
            }
        });
    }

    private void getIntentData() {
        if (getIntent() != null) {
            taskId = getIntent().getStringExtra(EXTRA_TASK_ID);
            boardId = getIntent().getStringExtra(EXTRA_BOARD_ID);
            boardName = getIntent().getStringExtra(EXTRA_BOARD_NAME);
            projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
            isEditMode = getIntent().getBooleanExtra(EXTRA_IS_EDIT_MODE, false);
        }
    }

    private void initViews() {
        // Top bar
        ivClose = findViewById(R.id.ivClose);
        etTaskTitle = findViewById(R.id.etTaskTitle); // Changed from rbTaskTitle
        tvBoardName = findViewById(R.id.tvBoardName); // NEW

        // Quick Actions
        btnMembers = findViewById(R.id.btnMembers);
        btnAddAttachment = findViewById(R.id.btnAddAttachment);
        btnAddComment = findViewById(R.id.btnAddComment);
        btnDeleteTask = findViewById(R.id.btnDeleteTask);
        btnConfirm = findViewById(R.id.btnConfirm); // NEW: Confirm button

        // Input fields
        etDescription = findViewById(R.id.etDescription);
        etDateStart = findViewById(R.id.etDateStart);
        etDueDate = findViewById(R.id.etDueDate);
//        tvTimer = findViewById(R.id.tvTimer);
//        btnPlay = findViewById(R.id.btnPlay);
        
        // Expandable sections
        tvTodolistHeader = findViewById(R.id.tvTodolistHeader);
        llTodolistContainer = findViewById(R.id.llTodolistContainer);
        tvAttachmentHeader = findViewById(R.id.tvAttachmentHeader);
        llAttachmentContainer = findViewById(R.id.llAttachmentContainer);
        tvCommentsHeader = findViewById(R.id.tvCommentsHeader);
        llCommentsContainer = findViewById(R.id.llCommentsContainer);
        tvNoComments = findViewById(R.id.tvNoComments);
    }

    private void setupViewModel() {
        // Create repository
        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        ITaskRepository repository = new TaskRepositoryImpl(apiService);


            // onBackPressed migrated to OnBackPressedDispatcher
        // Create UseCases
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

        // Create Factory
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

        // Observe ViewModel
        observeViewModel();
    }

    private void observeViewModel() {
        // Observe task creation/update success
        taskViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                taskViewModel.clearError();
            }
        });
        
        // Observe comments
        taskViewModel.getComments().observe(this, comments -> {
            if (comments != null && !comments.isEmpty()) {
                displayComments(comments);
            } else {
                tvNoComments.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupUI() {
        if (isEditMode) {
            // Edit mode - load existing task data
            etTaskTitle.setText(getIntent().getStringExtra(EXTRA_TASK_TITLE));
            etDescription.setText(getIntent().getStringExtra(EXTRA_TASK_DESCRIPTION));
            
            // Set current priority
            String priorityStr = getIntent().getStringExtra(EXTRA_TASK_PRIORITY);
            if (priorityStr != null) {
                try {
                    currentPriority = Task.TaskPriority.valueOf(priorityStr);
                } catch (IllegalArgumentException e) {
                    currentPriority = Task.TaskPriority.MEDIUM;
                }
            }
            
            btnDeleteTask.setVisibility(View.VISIBLE);
            btnConfirm.setText("Save Changes"); // Edit mode button text
        } else {
            // Create mode
            etTaskTitle.setText("");
            etTaskTitle.setHint("Enter task title");
            currentPriority = Task.TaskPriority.MEDIUM;
            btnDeleteTask.setVisibility(View.GONE);
            btnConfirm.setText("Add Card"); // Create mode button text
        }
        
        // Display board name
        if (boardName != null && !boardName.isEmpty()) {
            tvBoardName.setText("In " + boardName);
            tvBoardName.setVisibility(View.VISIBLE);
        } else {
            tvBoardName.setVisibility(View.GONE);
        }
        
        // Load comments if in edit mode
        if (isEditMode && taskId != null && !taskId.isEmpty()) {
//            taskViewModel.loadComments(taskId);
        }
    }

    private void setupListeners() {
        // Close button - Just close without saving
        ivClose.setOnClickListener(v -> {
            finish();
        });

        // Members button - Assign task to user
        btnMembers.setOnClickListener(v -> {
            showAssignTaskDialog();
        });

        // Add Attachment button
        btnAddAttachment.setOnClickListener(v -> {
            showAddAttachmentDialog();
        });

        // Add Comment button
        btnAddComment.setOnClickListener(v -> {
            showAddCommentDialog();
        });

        // Delete Task button
        btnDeleteTask.setOnClickListener(v -> {
            showDeleteConfirmation();
        });

        // Confirm button - Add or Save changes
        btnConfirm.setOnClickListener(v -> {
            if (isEditMode) {
                updateTask();
            } else {
                createTask();
            }
        });

        // Timer Play button - REMOVED (Timer feature not needed)
        // if (btnPlay != null) {
        //     btnPlay.setOnClickListener(v -> {
        //         Toast.makeText(this, "Timer feature - Coming soon", Toast.LENGTH_SHORT).show();
        //     });
        // }
        
        // Date pickers
        etDateStart.setOnClickListener(v -> showDatePickerDialog(true));
        etDueDate.setOnClickListener(v -> showDatePickerDialog(false));
        
        // Expandable sections toggle
        tvTodolistHeader.setOnClickListener(v -> toggleSection(llTodolistContainer));
        tvAttachmentHeader.setOnClickListener(v -> toggleSection(llAttachmentContainer));
        tvCommentsHeader.setOnClickListener(v -> toggleSection(llCommentsContainer));
    }
    
    /**
     * Toggle visibility of expandable section
     */
    private void toggleSection(LinearLayout container) {
        if (container.getVisibility() == View.VISIBLE) {
            container.setVisibility(View.GONE);
        } else {
            container.setVisibility(View.VISIBLE);
        }
    }

    private void createTask() {
        // Validate input
        String title = etTaskTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Please enter task title", Toast.LENGTH_SHORT).show();
            etTaskTitle.requestFocus();
            return;
        }

        String description = etDescription.getText().toString().trim();

        // Create new task with current priority
        Task newTask = new Task(
                "",              // id (backend will generate)
                projectId,       // projectId
                boardId,         // boardId
                title,           // title
                description,     // description
                null,            // issueKey
                null,            // type
                Task.TaskStatus.TO_DO, // Default status
                currentPriority, // Use selected priority
                0.0,             // position
                null,            // assigneeId
                null,            // createdBy (backend will set)
                null, null, null, null, null, null, null, null, null, null
        );

        // Call ViewModel to create task
        taskViewModel.createTask(newTask);
        Toast.makeText(this, "Task created successfully", Toast.LENGTH_SHORT).show();

        // Close activity and return success with board ID
        Intent resultIntent = new Intent();
        resultIntent.putExtra("board_id_for_reload", boardId);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void updateTask() {
        // Validate input
        String title = etTaskTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Please enter task title", Toast.LENGTH_SHORT).show();
            etTaskTitle.requestFocus();
            return;
        }

        String description = etDescription.getText().toString().trim();

        // Get original task data from intent
        String status = getIntent().getStringExtra(EXTRA_TASK_STATUS);
        double position = getIntent().getDoubleExtra(EXTRA_TASK_POSITION, 0);
        String assigneeId = getIntent().getStringExtra(EXTRA_TASK_ASSIGNEE_ID);
        String createdBy = getIntent().getStringExtra(EXTRA_TASK_CREATED_BY);

        // Create updated task with preserved data and current priority
        Task updatedTask = new Task(
                taskId,
                projectId,
                boardId,
                title,
                description,
                null,
                null,
                status != null ? Task.TaskStatus.valueOf(status) : Task.TaskStatus.TO_DO,
                currentPriority, // Use current selected priority
                position,
                assigneeId,
                createdBy,
                null, null, null, null, null, null, null, null, null, null
        );

        // Call ViewModel to update task
        taskViewModel.updateTask(taskId, updatedTask);
        Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();

        // Close activity and return success with board ID
        Intent resultIntent = new Intent();
        resultIntent.putExtra("board_id_for_reload", boardId);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showDeleteConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteTask();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTask() {
        if (taskId != null && !taskId.isEmpty()) {
            taskViewModel.deleteTask(taskId);
            Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show();

            // Close activity and return success with board ID
            Intent resultIntent = new Intent();
            resultIntent.putExtra("board_id_for_reload", boardId);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
    
    /**
     * Show dialog to assign task to a user
     * Option 1: Self-assign (for project owner)
     * Option 2: Assign to member (disabled - under development)
     */
    private void showAssignTaskDialog() {
        // Create options array
        CharSequence[] options = {
            "Self-assign (Assign to me)",
            "Assign to member (Coming soon)"
        };
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Assign Task")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // Option 1: Self-assign
                            selfAssignTask();
                            break;
                        case 1:
                            // Option 2: Assign to member (disabled - under development)
                            Toast.makeText(this, "Assign to member feature is under development", 
                                    Toast.LENGTH_SHORT).show();
                            // TODO: Implement assign to member when ready
                            // showAssignToMemberDialog();
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Self-assign the task to current user (project owner)
     */
    private void selfAssignTask() {
        if (taskId != null && !taskId.isEmpty()) {
            // Get current user UUID (database ID) from TokenManager
            String currentUserId = App.tokenManager.getUserId();
            
            if (currentUserId != null && !currentUserId.isEmpty()) {
                taskViewModel.assignTask(taskId, currentUserId);
                Toast.makeText(this, "Task assigned to you", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Unable to get current user ID. Please sign in again.", 
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Task not yet created. Please save the task first.", 
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show dialog to assign task to a project member
     * TODO: Implement this when member management is ready
     */
    private void showAssignToMemberDialog() {
        // This will be implemented when member management feature is ready
        // For now, show a simple input dialog (placeholder)
        EditText input = new EditText(this);
        input.setHint("Enter member email or ID");
        input.setPadding(50, 20, 50, 20);
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Assign to Member")
                .setMessage("Assign this task to:")
                .setView(input)
                .setPositiveButton("Assign", (dialog, which) -> {
                    String userId = input.getText().toString().trim();
                    if (!userId.isEmpty() && taskId != null) {
                        taskViewModel.assignTask(taskId, userId);
                        Toast.makeText(this, "Task assigned to " + userId, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Please enter a valid member ID", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Show dialog to add attachment
     */
    private void showAddAttachmentDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter attachment URL");
        input.setPadding(50, 20, 50, 20);
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Add Attachment")
                .setMessage("Add attachment URL:")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String attachmentUrl = input.getText().toString().trim();
                    if (!attachmentUrl.isEmpty() && taskId != null) {
                        // TODO: Call addAttachment use case when implemented
                        Toast.makeText(this, "Attachment added: " + attachmentUrl, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Show dialog to add comment
     */
    private void showAddCommentDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter your comment");
        input.setPadding(50, 20, 50, 20);
        input.setMinLines(3);
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Add Comment")
                .setMessage("Write a comment:")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String commentText = input.getText().toString().trim();
                    if (!commentText.isEmpty() && taskId != null) {
                        // Create TaskComment object
                        com.example.tralalero.domain.model.TaskComment comment = 
                                new com.example.tralalero.domain.model.TaskComment(
                                    "", // id (backend will generate)
                                    taskId,
                                    null, // userId (backend will get from auth)
                                    commentText,
                                    null // createdAt (backend will set)
                                );
                        taskViewModel.addComment(taskId, comment);
                        Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show();
                        
                        // Reload comments after adding
//                        taskViewModel.loadComments(taskId);
                    } else {
                        Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Display comments list with user info and timestamp
     * @param comments List of TaskComment objects
     */
    private void displayComments(java.util.List<com.example.tralalero.domain.model.TaskComment> comments) {
        // Clear existing comments
        llCommentsContainer.removeAllViews();
        
        if (comments == null || comments.isEmpty()) {
            tvNoComments.setVisibility(View.VISIBLE);
            return;
        }
        
        tvNoComments.setVisibility(View.GONE);
        
        // Date formatter
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        
        // Add each comment
        for (com.example.tralalero.domain.model.TaskComment comment : comments) {
            // Create comment item layout
            LinearLayout commentItem = new LinearLayout(this);
            commentItem.setOrientation(LinearLayout.VERTICAL);
            commentItem.setPadding(16, 12, 16, 12);
            commentItem.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
            
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            itemParams.setMargins(0, 0, 0, 8);
            commentItem.setLayoutParams(itemParams);
            
            // Comment header (user and time)
            LinearLayout headerLayout = new LinearLayout(this);
            headerLayout.setOrientation(LinearLayout.HORIZONTAL);
            headerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            
            // User icon
            ImageView userIcon = new ImageView(this);
            userIcon.setImageResource(R.drawable.acc_icon);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(20, 20);
            iconParams.setMargins(0, 0, 8, 0);
            userIcon.setLayoutParams(iconParams);
            headerLayout.addView(userIcon);
            
            // User name/ID
            TextView tvUserName = new TextView(this);
            String userName = comment.getUserId() != null ? comment.getUserId() : "Unknown User";
            tvUserName.setText(userName);
            tvUserName.setTextSize(12);
            tvUserName.setTextColor(getResources().getColor(android.R.color.black));
            tvUserName.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams userParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
            );
            tvUserName.setLayoutParams(userParams);
            headerLayout.addView(tvUserName);
            
            // Timestamp
            TextView tvTimestamp = new TextView(this);
            if (comment.getCreatedAt() != null) {
                tvTimestamp.setText(dateFormatter.format(comment.getCreatedAt()));
            } else {
                tvTimestamp.setText("Just now");
            }
            tvTimestamp.setTextSize(10);
            tvTimestamp.setTextColor(getResources().getColor(android.R.color.darker_gray));
            headerLayout.addView(tvTimestamp);
            
            commentItem.addView(headerLayout);
            
            // Comment text
            TextView tvCommentText = new TextView(this);
//            tvCommentText.setText(comment.getContent());
            tvCommentText.setTextSize(12);
            tvCommentText.setTextColor(getResources().getColor(android.R.color.black));
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            textParams.setMargins(28, 8, 0, 0);  // Indent under user icon
            tvCommentText.setLayoutParams(textParams);
            commentItem.addView(tvCommentText);
            
            // Add comment item to container
            llCommentsContainer.addView(commentItem);
        }
    }
    
    /**
     * Show date picker dialog
     * @param isStartDate true for start date, false for due date
     */
    private void showDatePickerDialog(boolean isStartDate) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH);
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        
        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format(Locale.getDefault(), 
                            "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    if (isStartDate) {
                        etDateStart.setText(date);
                    } else {
                        etDueDate.setText(date);
                    }
                },
                year, month, day
        );
        
        datePickerDialog.show();
    }
}
