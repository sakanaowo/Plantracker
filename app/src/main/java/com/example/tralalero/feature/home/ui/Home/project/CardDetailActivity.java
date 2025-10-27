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

public class CardDetailActivity extends AppCompatActivity {
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_BOARD_ID = "board_id";
    public static final String EXTRA_BOARD_NAME = "board_name"; // NEW: Board name to display
    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_IS_EDIT_MODE = "is_edit_mode";
    public static final String EXTRA_TASK_TITLE = "task_title";
    public static final String EXTRA_TASK_DESCRIPTION = "task_description";
    public static final String EXTRA_TASK_PRIORITY = "task_priority";
    public static final String EXTRA_TASK_STATUS = "task_status";
    public static final String EXTRA_TASK_POSITION = "task_position";
    public static final String EXTRA_TASK_ASSIGNEE_ID = "task_assignee_id";
    public static final String EXTRA_TASK_CREATED_BY = "task_created_by";
    private String taskId;
    private String boardId;
    private String boardName;
    private String projectId;
    private boolean isEditMode;
    private TaskViewModel taskViewModel;
    private ImageView ivClose;
    private EditText etTaskTitle; // Changed from RadioButton to EditText
    private TextView tvBoardName; // NEW: Display board name
    private MaterialButton btnMembers;
    private MaterialButton btnAddAttachment;
    private MaterialButton btnAddComment;
    private MaterialButton btnDeleteTask;
    private MaterialButton btnConfirm; // NEW: Confirm button
    private EditText etDescription;
    private EditText etDateStart;
    private EditText etDueDate;
    private TextView tvTodolistHeader;
    private LinearLayout llTodolistContainer;
    private TextView tvAttachmentHeader;
    private LinearLayout llAttachmentContainer;
    private TextView tvCommentsHeader;
    private LinearLayout llCommentsContainer;
    private TextView tvNoComments;
    private MaterialButton btnLowPriority;
    private MaterialButton btnMediumPriority;
    private MaterialButton btnHighPriority;
    private Task.TaskPriority currentPriority = Task.TaskPriority.MEDIUM;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_detail);
        getIntentData();
        initViews();
        setupViewModel();
        setupUI();
        setupListeners();
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
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
        ivClose = findViewById(R.id.ivClose);
        etTaskTitle = findViewById(R.id.etTaskTitle); // Changed from rbTaskTitle
        tvBoardName = findViewById(R.id.tvBoardName); // NEW
        btnMembers = findViewById(R.id.btnMembers);
        btnAddAttachment = findViewById(R.id.btnAddAttachment);
        btnAddComment = findViewById(R.id.btnAddComment);
        btnDeleteTask = findViewById(R.id.btnDeleteTask);
        btnConfirm = findViewById(R.id.btnConfirm); // NEW: Confirm button
        etDescription = findViewById(R.id.etDescription);
        etDateStart = findViewById(R.id.etDateStart);
        etDueDate = findViewById(R.id.etDueDate);
        tvTodolistHeader = findViewById(R.id.tvTodolistHeader);
        llTodolistContainer = findViewById(R.id.llTodolistContainer);
        tvAttachmentHeader = findViewById(R.id.tvAttachmentHeader);
        llAttachmentContainer = findViewById(R.id.llAttachmentContainer);
        tvCommentsHeader = findViewById(R.id.tvCommentsHeader);
        llCommentsContainer = findViewById(R.id.llCommentsContainer);
        tvNoComments = findViewById(R.id.tvNoComments);
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
        taskViewModel = new ViewModelProvider(this, factory).get(TaskViewModel.class);
        observeViewModel();
    }

    private void observeViewModel() {
        taskViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                taskViewModel.clearError();
            }
        });
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
            etTaskTitle.setText(getIntent().getStringExtra(EXTRA_TASK_TITLE));
            etDescription.setText(getIntent().getStringExtra(EXTRA_TASK_DESCRIPTION));
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
            etTaskTitle.setText("");
            etTaskTitle.setHint("Enter task title");
            currentPriority = Task.TaskPriority.MEDIUM;
            btnDeleteTask.setVisibility(View.GONE);
            btnConfirm.setText("Add Card"); // Create mode button text
        }
        if (boardName != null && !boardName.isEmpty()) {
            tvBoardName.setText("In " + boardName);
            tvBoardName.setVisibility(View.VISIBLE);
        } else {
            tvBoardName.setVisibility(View.GONE);
        }
        if (isEditMode && taskId != null && !taskId.isEmpty()) {
        }
    }

    private void setupListeners() {
        ivClose.setOnClickListener(v -> {
            finish();
        });
        btnMembers.setOnClickListener(v -> {
            showAssignTaskDialog();
        });
        btnAddAttachment.setOnClickListener(v -> {
            showAddAttachmentDialog();
        });
        btnAddComment.setOnClickListener(v -> {
            showAddCommentDialog();
        });
        btnDeleteTask.setOnClickListener(v -> {
            showDeleteConfirmation();
        });
        btnConfirm.setOnClickListener(v -> {
            if (isEditMode) {
                updateTask();
            } else {
                createTask();
            }
        });
        etDateStart.setOnClickListener(v -> showDatePickerDialog(true));
        etDueDate.setOnClickListener(v -> showDatePickerDialog(false));
        tvTodolistHeader.setOnClickListener(v -> toggleSection(llTodolistContainer));
        tvAttachmentHeader.setOnClickListener(v -> toggleSection(llAttachmentContainer));
        tvCommentsHeader.setOnClickListener(v -> toggleSection(llCommentsContainer));
    }

    private void toggleSection(LinearLayout container) {
        if (container.getVisibility() == View.VISIBLE) {
            container.setVisibility(View.GONE);
        } else {
            container.setVisibility(View.VISIBLE);
        }
    }

    private void createTask() {
        String title = etTaskTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Please enter task title", Toast.LENGTH_SHORT).show();
            etTaskTitle.requestFocus();
            return;
        }

        String description = etDescription.getText().toString().trim();
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
        taskViewModel.createTask(newTask);
        Toast.makeText(this, "Task created successfully", Toast.LENGTH_SHORT).show();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("board_id_for_reload", boardId);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void updateTask() {
        String title = etTaskTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Please enter task title", Toast.LENGTH_SHORT).show();
            etTaskTitle.requestFocus();
            return;
        }

        String description = etDescription.getText().toString().trim();
        String status = getIntent().getStringExtra(EXTRA_TASK_STATUS);
        double position = getIntent().getDoubleExtra(EXTRA_TASK_POSITION, 0);
        String assigneeId = getIntent().getStringExtra(EXTRA_TASK_ASSIGNEE_ID);
        String createdBy = getIntent().getStringExtra(EXTRA_TASK_CREATED_BY);
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
        taskViewModel.updateTask(taskId, updatedTask);
        Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
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
            String status = getIntent().getStringExtra(EXTRA_TASK_STATUS);
            double position = getIntent().getDoubleExtra(EXTRA_TASK_POSITION, 0);
            String assigneeId = getIntent().getStringExtra(EXTRA_TASK_ASSIGNEE_ID);
            String createdBy = getIntent().getStringExtra(EXTRA_TASK_CREATED_BY);
            String title = etTaskTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            Task taskToDelete = new Task(
                taskId,
                projectId,
                boardId,  // Important: contains boardId for reload
                title,
                description,
                null,
                null,
                status != null ? Task.TaskStatus.valueOf(status) : Task.TaskStatus.TO_DO,
                currentPriority,
                position,
                assigneeId,
                createdBy,
                null, null, null, null, null, null, null, null, null, null
            );
            taskViewModel.setSelectedTask(taskToDelete);
            taskViewModel.deleteTask(taskId);
            Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("board_id_for_reload", boardId);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    private void showAssignTaskDialog() {
        CharSequence[] options = {
            "Self-assign (Assign to me)",
            "Assign to member (Coming soon)"
        };

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Assign Task")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            selfAssignTask();
                            break;
                        case 1:
                            Toast.makeText(this, "Assign to member feature is under development", 
                                    Toast.LENGTH_SHORT).show();
                            // TODO: Implement assign to member when ready
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void selfAssignTask() {
        if (taskId != null && !taskId.isEmpty()) {
            // Use internal UUID for backend API (not Firebase UID)
            String internalUserId = App.tokenManager.getInternalUserId();
            
            android.util.Log.d("CardDetailActivity", "Self-assign - TaskId: " + taskId);
            android.util.Log.d("CardDetailActivity", "Internal UserId: " + internalUserId);

            if (internalUserId != null && !internalUserId.isEmpty()) {
                taskViewModel.assignTask(taskId, internalUserId);
                Toast.makeText(this, "Task assigned to you", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Unable to get user ID. Please sign out and sign in again.", 
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
                    } else {
                        Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void displayComments(java.util.List<com.example.tralalero.domain.model.TaskComment> comments) {
        llCommentsContainer.removeAllViews();

        if (comments == null || comments.isEmpty()) {
            tvNoComments.setVisibility(View.VISIBLE);
            return;
        }

        tvNoComments.setVisibility(View.GONE);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        for (com.example.tralalero.domain.model.TaskComment comment : comments) {
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
            LinearLayout headerLayout = new LinearLayout(this);
            headerLayout.setOrientation(LinearLayout.HORIZONTAL);
            headerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            ImageView userIcon = new ImageView(this);
            userIcon.setImageResource(R.drawable.acc_icon);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(20, 20);
            iconParams.setMargins(0, 0, 8, 0);
            userIcon.setLayoutParams(iconParams);
            headerLayout.addView(userIcon);
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
            TextView tvCommentText = new TextView(this);
            tvCommentText.setTextSize(12);
            tvCommentText.setTextColor(getResources().getColor(android.R.color.black));
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            textParams.setMargins(28, 8, 0, 0);  // Indent under user icon
            tvCommentText.setLayoutParams(textParams);
            commentItem.addView(tvCommentText);
            llCommentsContainer.addView(commentItem);
        }
    }

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
