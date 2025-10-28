package com.example.tralalero.feature.home.ui.Home.project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.App.App;
import com.example.tralalero.adapter.AttachmentAdapter;
import com.example.tralalero.adapter.CommentAdapter;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.domain.model.Label;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.domain.usecase.task.*;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.presentation.viewmodel.TaskViewModel;
import com.example.tralalero.presentation.viewmodel.TaskViewModelFactory;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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
    private com.example.tralalero.presentation.viewmodel.LabelViewModel labelViewModel;
    private ImageView ivClose;
    private EditText etTaskTitle; // Changed from RadioButton to EditText
    private TextView tvBoardName; // NEW: Display board name
    private MaterialButton btnMembers;
    private MaterialButton btnAddAttachment;
    private MaterialButton btnAddComment;
    private MaterialButton btnDeleteTask;
    private MaterialButton btnConfirm;
    private EditText etDescription;
    private EditText etDateStart;
    private EditText etDueDate;
    private MaterialButton btnLowPriority;
    private MaterialButton btnMediumPriority;
    private MaterialButton btnHighPriority;
    private Task.TaskPriority currentPriority = Task.TaskPriority.MEDIUM;
    
    // RecyclerViews and Adapters
    private RecyclerView rvAttachments;
    private RecyclerView rvComments;
    private RecyclerView rvChecklist;
    private TextView tvNoAttachments;
    private TextView tvNoChecklist;
    private AttachmentAdapter attachmentAdapter;
    private CommentAdapter commentAdapter;
    private ChecklistAdapter checklistAdapter;
    private ImageView ivAddChecklist;
    private LinearLayout labelLayout;
    private HorizontalScrollView scrollViewLabels;
    private LinearLayout layoutSelectedLabels;
    private TextView tvNoLabels;
    private List<Label> selectedLabels = new ArrayList<>();

    TextView tvNoComments;

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
        btnConfirm = findViewById(R.id.btnConfirm);
        etDescription = findViewById(R.id.etDescription);
        etDateStart = findViewById(R.id.etDateStart);
        etDueDate = findViewById(R.id.etDueDate);
        
        // RecyclerViews
        rvAttachments = findViewById(R.id.rvAttachments);
        rvComments = findViewById(R.id.rvComments);
        rvChecklist = findViewById(R.id.rvChecklist);
        tvNoAttachments = findViewById(R.id.tvNoAttachments);
        tvNoComments = findViewById(R.id.tvNoComments);
        tvNoChecklist = findViewById(R.id.tvNoChecklist);
        ivAddChecklist = findViewById(R.id.ivAddChecklist);
        labelLayout = findViewById(R.id.labelLayout);
        scrollViewLabels = findViewById(R.id.scrollViewLabels);
        layoutSelectedLabels = findViewById(R.id.layoutSelectedLabels);
        tvNoLabels = findViewById(R.id.tvNoLabels);
        
        // Setup RecyclerViews
        setupRecyclerViews();
    }
    
    private void setupRecyclerViews() {
        // Setup Attachments RecyclerView
        rvAttachments.setLayoutManager(new LinearLayoutManager(this));
        attachmentAdapter = new AttachmentAdapter(new AttachmentAdapter.OnAttachmentClickListener() {
            @Override
            public void onDownloadClick(com.example.tralalero.domain.model.Attachment attachment) {
                // TODO: Implement download functionality
                Toast.makeText(CardDetailActivity.this, "Download: " + attachment.getFileName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(com.example.tralalero.domain.model.Attachment attachment) {
                // TODO: Implement delete attachment
                Toast.makeText(CardDetailActivity.this, "Delete attachment", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAttachmentClick(com.example.tralalero.domain.model.Attachment attachment) {
                // TODO: Open/View attachment
                Toast.makeText(CardDetailActivity.this, "View: " + attachment.getFileName(), Toast.LENGTH_SHORT).show();
            }
        });
        rvAttachments.setAdapter(attachmentAdapter);
        
        // Setup Comments RecyclerView
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(new CommentAdapter.OnCommentClickListener() {
            @Override
            public void onOptionsClick(com.example.tralalero.domain.model.TaskComment comment, int position) {
                // TODO: Show edit/delete options
                Toast.makeText(CardDetailActivity.this, "Comment options", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCommentClick(com.example.tralalero.domain.model.TaskComment comment) {
                // Optional: Handle comment click
            }
        });
        rvComments.setAdapter(commentAdapter);
        
        // Setup Checklist RecyclerView
        rvChecklist.setLayoutManager(new LinearLayoutManager(this));
        checklistAdapter = new ChecklistAdapter(new ChecklistAdapter.OnChecklistItemListener() {
            @Override
            public void onCheckboxChanged(com.example.tralalero.domain.model.ChecklistItem item, boolean isChecked) {
                // TODO: Update checklist item status
                if (isEditMode && taskId != null) {
                    taskViewModel.updateChecklistItem(taskId, item.getId(), isChecked);
                }
            }

            @Override
            public void onDeleteClick(com.example.tralalero.domain.model.ChecklistItem item) {
                // TODO: Delete checklist item
                if (isEditMode && taskId != null) {
                    taskViewModel.deleteChecklistItem(taskId, item.getId());
                }
            }

            @Override
            public void onItemClick(com.example.tralalero.domain.model.ChecklistItem item) {
                // Optional: Edit checklist item
                Toast.makeText(CardDetailActivity.this, "Edit: " + item.getContent(), Toast.LENGTH_SHORT).show();
            }
        });
        rvChecklist.setAdapter(checklistAdapter);
        
        // Set initial state - show placeholders, hide RecyclerViews
        rvAttachments.setVisibility(View.GONE);
        tvNoAttachments.setVisibility(View.VISIBLE);
        rvComments.setVisibility(View.GONE);
        tvNoComments.setVisibility(View.VISIBLE);
        rvChecklist.setVisibility(View.GONE);
        tvNoChecklist.setVisibility(View.VISIBLE);
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
        
        // Initialize LabelViewModel
        setupLabelViewModel();
        
        observeViewModel();
    }
    
    private void setupLabelViewModel() {
        com.example.tralalero.data.remote.api.LabelApiService labelApiService = 
            ApiClient.get(App.authManager).create(com.example.tralalero.data.remote.api.LabelApiService.class);
        com.example.tralalero.domain.repository.ILabelRepository labelRepository = 
            new com.example.tralalero.data.repository.LabelRepositoryImpl(labelApiService);
        
        com.example.tralalero.domain.usecase.label.GetLabelsByWorkspaceUseCase getLabelsByWorkspaceUseCase = 
            new com.example.tralalero.domain.usecase.label.GetLabelsByWorkspaceUseCase(labelRepository);
        com.example.tralalero.domain.usecase.label.GetLabelsByProjectUseCase getLabelsByProjectUseCase = 
            new com.example.tralalero.domain.usecase.label.GetLabelsByProjectUseCase(labelRepository);
        com.example.tralalero.domain.usecase.label.GetLabelByIdUseCase getLabelByIdUseCase = 
            new com.example.tralalero.domain.usecase.label.GetLabelByIdUseCase(labelRepository);
        com.example.tralalero.domain.usecase.label.CreateLabelUseCase createLabelUseCase = 
            new com.example.tralalero.domain.usecase.label.CreateLabelUseCase(labelRepository);
        com.example.tralalero.domain.usecase.label.CreateLabelInProjectUseCase createLabelInProjectUseCase = 
            new com.example.tralalero.domain.usecase.label.CreateLabelInProjectUseCase(labelRepository);
        com.example.tralalero.domain.usecase.label.UpdateLabelUseCase updateLabelUseCase = 
            new com.example.tralalero.domain.usecase.label.UpdateLabelUseCase(labelRepository);
        com.example.tralalero.domain.usecase.label.DeleteLabelUseCase deleteLabelUseCase = 
            new com.example.tralalero.domain.usecase.label.DeleteLabelUseCase(labelRepository);
        com.example.tralalero.domain.usecase.label.GetTaskLabelsUseCase getTaskLabelsUseCase = 
            new com.example.tralalero.domain.usecase.label.GetTaskLabelsUseCase(labelRepository);
        com.example.tralalero.domain.usecase.label.AssignLabelToTaskUseCase assignLabelToTaskUseCase = 
            new com.example.tralalero.domain.usecase.label.AssignLabelToTaskUseCase(labelRepository);
        com.example.tralalero.domain.usecase.label.RemoveLabelFromTaskUseCase removeLabelFromTaskUseCase = 
            new com.example.tralalero.domain.usecase.label.RemoveLabelFromTaskUseCase(labelRepository);
        
        com.example.tralalero.presentation.viewmodel.LabelViewModelFactory labelFactory = 
            new com.example.tralalero.presentation.viewmodel.LabelViewModelFactory(
                getLabelsByWorkspaceUseCase,
                getLabelsByProjectUseCase,
                getLabelByIdUseCase,
                createLabelUseCase,
                createLabelInProjectUseCase,
                updateLabelUseCase,
                deleteLabelUseCase,
                getTaskLabelsUseCase,
                assignLabelToTaskUseCase,
                removeLabelFromTaskUseCase
            );
        
        labelViewModel = new ViewModelProvider(this, labelFactory)
            .get(com.example.tralalero.presentation.viewmodel.LabelViewModel.class);
        
        // Observe task labels
        labelViewModel.getTaskLabels().observe(this, labels -> {
            if (labels != null) {
                selectedLabels.clear();
                selectedLabels.addAll(labels);
                displaySelectedLabels();
            }
        });
    }

    private void observeViewModel() {
        taskViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                taskViewModel.clearError();
            }
        });
        
        // Observe comments
        taskViewModel.getComments().observe(this, comments -> {
            android.util.Log.d("CardDetail", "Comments received: " + (comments != null ? comments.size() : "null"));
            if (comments != null && !comments.isEmpty()) {
                commentAdapter.setComments(comments);
                rvComments.setVisibility(View.VISIBLE);
                tvNoComments.setVisibility(View.GONE);
            } else {
                rvComments.setVisibility(View.GONE);
                tvNoComments.setVisibility(View.VISIBLE);
            }
        });
        
        // Observe attachments
        taskViewModel.getAttachments().observe(this, attachments -> {
            android.util.Log.d("CardDetail", "Attachments received: " + (attachments != null ? attachments.size() : "null"));
            if (attachments != null && !attachments.isEmpty()) {
                attachmentAdapter.setAttachments(attachments);
                rvAttachments.setVisibility(View.VISIBLE);
                tvNoAttachments.setVisibility(View.GONE);
            } else {
                rvAttachments.setVisibility(View.GONE);
                tvNoAttachments.setVisibility(View.VISIBLE);
            }
        });
        
        // Observe checklist items
        taskViewModel.getChecklistItems().observe(this, checklistItems -> {
            android.util.Log.d("CardDetail", "Checklist items received: " + (checklistItems != null ? checklistItems.size() : "null"));
            if (checklistItems != null && !checklistItems.isEmpty()) {
                checklistAdapter.setChecklistItems(checklistItems);
                rvChecklist.setVisibility(View.VISIBLE);
                tvNoChecklist.setVisibility(View.GONE);
            } else {
                rvChecklist.setVisibility(View.GONE);
                tvNoChecklist.setVisibility(View.VISIBLE);
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
        
        // Load attachments and comments if in edit mode
        if (isEditMode && taskId != null && !taskId.isEmpty()) {
            loadTaskAttachments();
            loadTaskComments();
            loadChecklistItems();
            loadTaskLabels();
        }
        
        // Display selected labels (initial state)
        displaySelectedLabels();
    }
    
    private void loadTaskAttachments() {
        if (taskId != null && !taskId.isEmpty()) {
            taskViewModel.loadTaskAttachments(taskId);
        }
    }
    
    private void loadTaskComments() {
        if (taskId != null && !taskId.isEmpty()) {
            taskViewModel.loadTaskComments(taskId);
        }
    }
    
    private void loadChecklistItems() {
        if (taskId != null && !taskId.isEmpty()) {
            taskViewModel.loadChecklistItems(taskId);
        }
    }
    
    private void loadTaskLabels() {
        // Load task labels from API via ViewModel
        if (taskId != null && !taskId.isEmpty() && labelViewModel != null) {
            labelViewModel.loadTaskLabels(taskId);
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
        ivAddChecklist.setOnClickListener(v -> {
            showAddChecklistDialog();
        });
        labelLayout.setOnClickListener(v -> {
            showLabelSelectionDialog();
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
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_URI);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Add Attachment")
                .setMessage("Add attachment URL:")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String attachmentUrl = input.getText().toString().trim();
                    if (!attachmentUrl.isEmpty() && taskId != null) {
                        // Create attachment object
                        String fileName = extractFileNameFromUrl(attachmentUrl);
                        String mimeType = getMimeTypeFromUrl(attachmentUrl);
                        
                        com.example.tralalero.domain.model.Attachment attachment = 
                            new com.example.tralalero.domain.model.Attachment(
                                null, // id (backend will generate)
                                taskId,
                                attachmentUrl,
                                fileName,
                                mimeType,
                                null, // size (unknown for URL)
                                null, // uploadedBy (backend will set)
                                new java.util.Date() // createdAt
                            );
                        
                        // Add via ViewModel
                        taskViewModel.addAttachment(taskId, attachment);
                    } else {
                        Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    // Helper method to extract filename from URL
    private String extractFileNameFromUrl(String url) {
        try {
            String[] parts = url.split("/");
            String fileName = parts[parts.length - 1];
            // Remove query parameters
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }
            return fileName.isEmpty() ? "attachment" : fileName;
        } catch (Exception e) {
            return "attachment";
        }
    }
    
    // Helper method to guess MIME type from URL extension
    private String getMimeTypeFromUrl(String url) {
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerUrl.endsWith(".png")) {
            return "image/png";
        } else if (lowerUrl.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerUrl.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerUrl.endsWith(".doc") || lowerUrl.endsWith(".docx")) {
            return "application/msword";
        } else if (lowerUrl.endsWith(".xls") || lowerUrl.endsWith(".xlsx")) {
            return "application/vnd.ms-excel";
        } else if (lowerUrl.endsWith(".zip")) {
            return "application/zip";
        } else if (lowerUrl.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lowerUrl.endsWith(".mp3")) {
            return "audio/mpeg";
        }
        return "application/octet-stream";
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

    private void showAddChecklistDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter checklist item");
        input.setPadding(50, 20, 50, 20);
        input.setMaxLines(2);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Add Checklist Item")
                .setMessage("What needs to be done?")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String content = input.getText().toString().trim();
                    if (!content.isEmpty() && taskId != null) {
                        com.example.tralalero.domain.model.ChecklistItem checklistItem = 
                                new com.example.tralalero.domain.model.ChecklistItem(
                                    "", // id (backend will generate)
                                    taskId, // checklistId = taskId
                                    content,
                                    false, // isDone - default to false
                                    0.0, // position (backend will handle)
                                    null // createdAt (backend will set)
                                );
                        taskViewModel.addChecklistItem(taskId, checklistItem);
                        Toast.makeText(this, "Checklist item added", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Please enter checklist content", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLabelSelectionDialog() {
        if (!isEditMode || taskId == null || taskId.isEmpty()) {
            Toast.makeText(this, "Please save the task first", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("CardDetailActivity", "Opening label dialog - projectId: " + projectId + ", taskId: " + taskId);
        
        // Get currently selected label IDs
        List<String> selectedLabelIds = new ArrayList<>();
        for (Label label : selectedLabels) {
            selectedLabelIds.add(label.getId());
        }

        LabelSelectionBottomSheet dialog = LabelSelectionBottomSheet.newInstance(
                projectId, 
                taskId, 
                selectedLabelIds
        );
        
        dialog.setOnLabelsUpdatedListener(() -> {
            // Reload task labels from backend to ensure sync
            if (labelViewModel != null && taskId != null) {
                labelViewModel.loadTaskLabels(taskId);
            }
        });
        
        dialog.show(getSupportFragmentManager(), "LabelSelectionBottomSheet");
    }

    private void displaySelectedLabels() {
        layoutSelectedLabels.removeAllViews();

        if (selectedLabels.isEmpty()) {
            scrollViewLabels.setVisibility(View.GONE);
            tvNoLabels.setVisibility(View.VISIBLE);
        } else {
            scrollViewLabels.setVisibility(View.VISIBLE);
            tvNoLabels.setVisibility(View.GONE);

            for (Label label : selectedLabels) {
                View labelChip = createLabelChip(label);
                layoutSelectedLabels.addView(labelChip);
            }
        }
    }

    private View createLabelChip(Label label) {
        // Create a colored chip for the label
        TextView chip = new TextView(this);
        chip.setText(label.getName() != null && !label.getName().isEmpty() 
            ? label.getName() 
            : "");
        
        // Set padding
        int padding = (int) (8 * getResources().getDisplayMetrics().density);
        chip.setPadding(padding * 2, padding, padding * 2, padding);
        
        // Set margin
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(padding / 2, 0, padding / 2, 0);
        chip.setLayoutParams(params);
        
        chip.setTextSize(14);
        chip.setMaxLines(1);
        
        // Create rounded background with color
        try {
            android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
            drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(4 * getResources().getDisplayMetrics().density);
            drawable.setColor(Color.parseColor(label.getColor()));
            chip.setBackground(drawable);
            
            // Determine text color based on background brightness
            int color = Color.parseColor(label.getColor());
            int brightness = (Color.red(color) * 299 + Color.green(color) * 587 + Color.blue(color) * 114) / 1000;
            chip.setTextColor(brightness > 128 ? Color.BLACK : Color.WHITE);
        } catch (Exception e) {
            android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
            drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(4 * getResources().getDisplayMetrics().density);
            drawable.setColor(Color.GRAY);
            chip.setBackground(drawable);
            chip.setTextColor(Color.WHITE);
        }
        
        return chip;
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
