package com.example.tralalero.feature.home.ui.Home.project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.App.App;
import com.example.tralalero.adapter.AttachmentAdapter;
import com.example.tralalero.adapter.CommentAdapter;
import com.example.tralalero.data.remote.api.AttachmentApiService;
import com.example.tralalero.data.remote.api.CommentApiService;
import com.example.tralalero.data.remote.api.UserApiService;
import com.example.tralalero.feature.task.attachments.AttachmentUploader;
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
    private RecyclerView rvChecklist;
    private TextView tvNoChecklist;
    private ChecklistAdapter checklistAdapter;
    private ImageView ivAddChecklist;
    private LinearLayout labelLayout;
    private HorizontalScrollView scrollViewLabels;
    private LinearLayout layoutSelectedLabels;
    private TextView tvNoLabels;
    private List<Label> selectedLabels = new ArrayList<>();
    
    // Comments and Attachments
    private RecyclerView rvComments;
    private RecyclerView rvAttachments;
    private TextView tvNoComments;
    private TextView tvNoAttachments;
    private CommentAdapter commentAdapter;
    private AttachmentAdapter attachmentAdapter;
    private ImageView ivAddAttachment;
    private LinearLayout layoutCommentInput;
    private EditText etNewComment;
    private ImageButton btnSendComment;
    private AttachmentUploader attachmentUploader;
    private ActivityResultLauncher<String> filePickerLauncher;
    private UserApiService userApiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_detail);
        
        // Setup file picker launcher
        filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null && taskId != null) {
                    android.util.Log.d("CardDetail", "File selected, starting upload for taskId: " + taskId);
                    uploadFile(result);
                }
            }
        );
        
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

    @Override
    protected void onResume() {
        super.onResume();
        
        // Reload data when returning to this activity
        if (isEditMode && taskId != null && !taskId.isEmpty()) {
            loadTaskAttachments();
            loadTaskComments();
            loadChecklistItems();  // CRITICAL - Reload checklist items!
            loadTaskLabels();
        }
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
        rvChecklist = findViewById(R.id.rvChecklist);
        tvNoChecklist = findViewById(R.id.tvNoChecklist);
        ivAddChecklist = findViewById(R.id.ivAddChecklist);
        labelLayout = findViewById(R.id.labelLayout);
        scrollViewLabels = findViewById(R.id.scrollViewLabels);
        layoutSelectedLabels = findViewById(R.id.layoutSelectedLabels);
        tvNoLabels = findViewById(R.id.tvNoLabels);
        
        // Comments and Attachments
        rvComments = findViewById(R.id.rvComments);
        rvAttachments = findViewById(R.id.rvAttachments);
        tvNoComments = findViewById(R.id.tvNoComments);
        tvNoAttachments = findViewById(R.id.tvNoAttachments);
        ivAddAttachment = findViewById(R.id.ivAddAttachment);
        layoutCommentInput = findViewById(R.id.layoutCommentInput);
        etNewComment = findViewById(R.id.etNewComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        
        // Test buttons (temporary)
        Button btnTestComment = findViewById(R.id.btnTestComment);
        Button btnTestAttachment = findViewById(R.id.btnTestAttachment);
        LinearLayout llTestButtons = findViewById(R.id.llTestButtons);
        
        // Show test buttons for debugging
        llTestButtons.setVisibility(View.VISIBLE);
        
        // Setup RecyclerViews
        setupRecyclerViews();
    }
    
    private void setupRecyclerViews() {
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
        
        // Setup Comments RecyclerView
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        // CommentAdapter will be initialized in setupViewModel() after userApiService is available
        
        // Setup Attachments RecyclerView
        rvAttachments.setLayoutManager(new LinearLayoutManager(this));
        attachmentAdapter = new AttachmentAdapter(new AttachmentAdapter.OnAttachmentClickListener() {
            @Override
            public void onDownloadClick(com.example.tralalero.domain.model.Attachment attachment) {
                // TODO: Download attachment
                downloadAttachment(attachment);
            }

            @Override
            public void onDeleteClick(com.example.tralalero.domain.model.Attachment attachment) {
                // TODO: Delete attachment
                deleteAttachment(attachment);
            }

            @Override
            public void onAttachmentClick(com.example.tralalero.domain.model.Attachment attachment) {
                // TODO: Open attachment
                openAttachment(attachment);
            }
        });
        rvAttachments.setAdapter(attachmentAdapter);
        
        // Set initial state - show placeholders, hide RecyclerViews
        rvChecklist.setVisibility(View.GONE);
        tvNoChecklist.setVisibility(View.VISIBLE);
        
        // Comments and Attachments are always visible (no toggle)
        // Show "No items" text initially, RecyclerViews will show when data loads
        rvComments.setVisibility(View.GONE);
        tvNoComments.setVisibility(View.VISIBLE);
        rvAttachments.setVisibility(View.GONE);
        tvNoAttachments.setVisibility(View.VISIBLE);
    }

    private void setupViewModel() {
        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        CommentApiService commentApiService = ApiClient.get(App.authManager).create(CommentApiService.class);
        AttachmentApiService attachmentApiService = ApiClient.get(App.authManager).create(AttachmentApiService.class);
        UserApiService userApiService = ApiClient.get(App.authManager).create(UserApiService.class);
        this.userApiService = userApiService; // Store reference for CommentAdapter
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
                repository
        );
        taskViewModel = new ViewModelProvider(this, factory).get(TaskViewModel.class);
        
        // Setup AttachmentUploader
        attachmentUploader = new AttachmentUploader(attachmentApiService, this);
        
        // Initialize CommentAdapter now that userApiService is available
        setupCommentAdapter();
        
        // Initialize LabelViewModel
        setupLabelViewModel();
        
        observeViewModel();
    }
    
    private void setupCommentAdapter() {
        commentAdapter = new CommentAdapter(new CommentAdapter.OnCommentClickListener() {
            @Override
            public void onOptionsClick(com.example.tralalero.domain.model.TaskComment comment, int position) {
                // TODO: Show edit/delete options
                showCommentOptionsDialog(comment, position);
            }

            @Override
            public void onCommentClick(com.example.tralalero.domain.model.TaskComment comment) {
                // Optional: Handle comment click
            }
        }, userApiService);
        rvComments.setAdapter(commentAdapter);
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
        
        // Observe checklist items
        taskViewModel.getChecklistItems().observe(this, checklistItems -> {
            android.util.Log.d("CardDetail", "🎨 UI Observer triggered");
            android.util.Log.d("CardDetail", "  📊 Received " + (checklistItems != null ? checklistItems.size() : 0) + " items");
            
            if (checklistItems != null && !checklistItems.isEmpty()) {
                android.util.Log.d("CardDetail", "  ✅ Showing RecyclerView with " + checklistItems.size() + " items");
                for (int i = 0; i < Math.min(checklistItems.size(), 5); i++) {
                    android.util.Log.d("CardDetail", "    Item " + i + ": " + checklistItems.get(i).getContent());
                }
                checklistAdapter.setChecklistItems(checklistItems);
                rvChecklist.setVisibility(View.VISIBLE);
                tvNoChecklist.setVisibility(View.GONE);
            } else {
                android.util.Log.d("CardDetail", "  ❌ Showing empty state (items null or empty)");
                rvChecklist.setVisibility(View.GONE);
                tvNoChecklist.setVisibility(View.VISIBLE);
            }
        });
        
        // Observe comments
        taskViewModel.getComments().observe(this, comments -> {
            android.util.Log.d("CardDetail", "Comments observer triggered - count: " + (comments != null ? comments.size() : 0));
            if (comments != null && !comments.isEmpty()) {
                android.util.Log.d("CardDetail", "Setting " + comments.size() + " comments to adapter");
                commentAdapter.setComments(comments);
                rvComments.setVisibility(View.VISIBLE);
                tvNoComments.setVisibility(View.GONE);
                android.util.Log.d("CardDetail", "Comments RecyclerView made visible");
            } else {
                android.util.Log.d("CardDetail", "No comments - showing empty state");
                rvComments.setVisibility(View.GONE);
                tvNoComments.setVisibility(View.VISIBLE);
            }
        });
        
        // Observe attachments
        taskViewModel.getAttachments().observe(this, attachments -> {
            android.util.Log.d("CardDetail", "Attachments observer triggered - count: " + (attachments != null ? attachments.size() : 0));
            if (attachments != null && !attachments.isEmpty()) {
                android.util.Log.d("CardDetail", "Setting " + attachments.size() + " attachments to adapter");
                attachmentAdapter.setAttachments(attachments);
                rvAttachments.setVisibility(View.VISIBLE);
                tvNoAttachments.setVisibility(View.GONE);
                android.util.Log.d("CardDetail", "Attachments RecyclerView made visible");
            } else {
                android.util.Log.d("CardDetail", "No attachments - showing empty state");
                rvAttachments.setVisibility(View.GONE);
                tvNoAttachments.setVisibility(View.VISIBLE);
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
            android.util.Log.d("CardDetail", "Loading attachments for taskId: " + taskId);
            taskViewModel.loadTaskAttachments(taskId);
        } else {
            android.util.Log.e("CardDetail", "Cannot load attachments - taskId is null or empty");
        }
    }
    
    private void loadTaskComments() {
        if (taskId != null && !taskId.isEmpty()) {
            android.util.Log.d("CardDetail", "Loading comments for taskId: " + taskId);
            taskViewModel.loadTaskComments(taskId);
        } else {
            android.util.Log.e("CardDetail", "Cannot load comments - taskId is null or empty");
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
            // Direct file picker - no toggle needed
            android.util.Log.d("CardDetail", "btnAddAttachment clicked - isEditMode: " + isEditMode + ", taskId: " + taskId);
            if (!isEditMode || taskId == null || taskId.isEmpty()) {
                Toast.makeText(this, "Please save the task first", Toast.LENGTH_SHORT).show();
                return;
            }
            pickFile();
        });
        btnAddComment.setOnClickListener(v -> {
            // Focus on comment input - no toggle needed
            if (!isEditMode || taskId == null || taskId.isEmpty()) {
                Toast.makeText(this, "Please save the task first", Toast.LENGTH_SHORT).show();
                return;
            }
            etNewComment.requestFocus();
        });
        ivAddChecklist.setOnClickListener(v -> {
            showAddChecklistDialog();
        });
        
        // Add attachment button
        ivAddAttachment.setOnClickListener(v -> {
            android.util.Log.d("CardDetail", "ivAddAttachment clicked - isEditMode: " + isEditMode + ", taskId: " + taskId);
            if (!isEditMode || taskId == null || taskId.isEmpty()) {
                Toast.makeText(this, "Please save the task first", Toast.LENGTH_SHORT).show();
                return;
            }
            pickFile();
        });
        
        // Send comment button
        btnSendComment.setOnClickListener(v -> {
            addComment();
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
        
        // Test button listeners
        Button btnTestComment = findViewById(R.id.btnTestComment);
        Button btnTestAttachment = findViewById(R.id.btnTestAttachment);
        
        btnTestComment.setOnClickListener(v -> addTestComment());
        btnTestAttachment.setOnClickListener(v -> addTestAttachment());
    }
    
    /**
     * Add test comment for debugging
     */
    private void addTestComment() {
        java.util.List<com.example.tralalero.domain.model.TaskComment> testComments = new java.util.ArrayList<>();
        testComments.add(new com.example.tralalero.domain.model.TaskComment(
            "test-comment-" + System.currentTimeMillis(), 
            taskId, 
            "test-user", 
            "This is a test comment with options button. Click ⋮ to test edit/delete.",
            new java.util.Date()
        ));
        
        if (commentAdapter.getItemCount() == 0) {
            commentAdapter.setComments(testComments);
            rvComments.setVisibility(View.VISIBLE);
            tvNoComments.setVisibility(View.GONE);
        } else {
            commentAdapter.addComment(testComments.get(0));
        }
        
        Toast.makeText(this, "Test comment added", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Add test attachment for debugging
     */
    private void addTestAttachment() {
        java.util.List<com.example.tralalero.domain.model.Attachment> testAttachments = new java.util.ArrayList<>();
        testAttachments.add(new com.example.tralalero.domain.model.Attachment(
            "test-attachment-" + System.currentTimeMillis(),
            taskId,
            "https://example.com/test-file.jpg",
            "test-image.jpg",
            "image/jpeg",
            1024000,  // 1MB
            "test-user",
            new java.util.Date()
        ));
        
        if (attachmentAdapter.getItemCount() == 0) {
            attachmentAdapter.setAttachments(testAttachments);
            rvAttachments.setVisibility(View.VISIBLE);
            tvNoAttachments.setVisibility(View.GONE);
        } else {
            attachmentAdapter.addAttachment(testAttachments.get(0));
        }
        
        Toast.makeText(this, "Test attachment added", Toast.LENGTH_SHORT).show();
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

    /**
     * Add Comment
     */
    private void addComment() {
        String text = etNewComment.getText().toString().trim();
        android.util.Log.d("CardDetail", "addComment called with text: '" + text + "'");
        
        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (taskId != null) {
            android.util.Log.d("CardDetail", "Creating comment for taskId: " + taskId);
            com.example.tralalero.domain.model.TaskComment comment = 
                new com.example.tralalero.domain.model.TaskComment("", taskId, null, text, null);
            taskViewModel.addComment(taskId, comment);
            etNewComment.setText("");
            Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show();
        } else {
            android.util.Log.e("CardDetail", "Cannot add comment - taskId is null");
        }
    }

    /**
     * Show Comment Options Dialog (Edit/Delete)
     */
    private void showCommentOptionsDialog(com.example.tralalero.domain.model.TaskComment comment, int position) {
        String[] options = {"Edit", "Delete"};
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Comment Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit
                            showEditCommentDialog(comment);
                            break;
                        case 1: // Delete
                            showDeleteCommentConfirmation(comment);
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Show Edit Comment Dialog
     */
    private void showEditCommentDialog(com.example.tralalero.domain.model.TaskComment comment) {
        EditText editText = new EditText(this);
        editText.setText(comment.getBody());
        editText.setSelection(comment.getBody().length()); // Move cursor to end
        editText.setMinLines(2);
        editText.setMaxLines(6);
        editText.setPadding(50, 20, 50, 20);
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Edit Comment")
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newText = editText.getText().toString().trim();
                    if (!newText.isEmpty()) {
                        updateComment(comment, newText);
                    } else {
                        Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        
        // Show keyboard
        editText.requestFocus();
    }
    
    /**
     * Show Delete Comment Confirmation
     */
    private void showDeleteCommentConfirmation(com.example.tralalero.domain.model.TaskComment comment) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteComment(comment);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Update Comment
     */
    private void updateComment(com.example.tralalero.domain.model.TaskComment comment, String newText) {
        android.util.Log.d("CardDetail", "Updating comment: " + comment.getId() + " with new text: " + newText);
        
        // Check if this is a test comment (starts with "test-comment-")
        if (comment.getId().startsWith("test-comment-")) {
            // Handle test comment update locally
            updateTestComment(comment, newText);
            return;
        }
        
        // Create updated comment
        com.example.tralalero.domain.model.TaskComment updatedComment = 
            new com.example.tralalero.domain.model.TaskComment(
                comment.getId(),
                comment.getTaskId(),
                comment.getUserId(),
                newText,
                comment.getCreatedAt()
            );
        
        // Call ViewModel update method
        taskViewModel.updateComment(comment.getId(), newText);
    }
    
    /**
     * Delete Comment
     */
    private void deleteComment(com.example.tralalero.domain.model.TaskComment comment) {
        android.util.Log.d("CardDetail", "Deleting comment: " + comment.getId());
        
        // Check if this is a test comment (starts with "test-comment-")
        if (comment.getId().startsWith("test-comment-")) {
            // Handle test comment deletion locally
            deleteTestComment(comment);
            return;
        }
        
        // Call ViewModel delete method
        taskViewModel.deleteComment(comment.getId(), taskId);
    }
    
    /**
     * Update test comment locally
     */
    private void updateTestComment(com.example.tralalero.domain.model.TaskComment comment, String newText) {
        // Find the comment in current adapter data and update it
        java.util.List<com.example.tralalero.domain.model.TaskComment> currentComments = getCurrentComments();
        for (int i = 0; i < currentComments.size(); i++) {
            if (currentComments.get(i).getId().equals(comment.getId())) {
                // Create updated comment
                com.example.tralalero.domain.model.TaskComment updatedComment = 
                    new com.example.tralalero.domain.model.TaskComment(
                        comment.getId(),
                        comment.getTaskId(),
                        comment.getUserId(),
                        newText,
                        comment.getCreatedAt()
                    );
                commentAdapter.updateComment(i, updatedComment);
                Toast.makeText(this, "Test comment updated locally", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }
    
    /**
     * Delete test comment locally
     */
    private void deleteTestComment(com.example.tralalero.domain.model.TaskComment comment) {
        // Find the comment in current adapter data and remove it
        java.util.List<com.example.tralalero.domain.model.TaskComment> currentComments = getCurrentComments();
        for (int i = 0; i < currentComments.size(); i++) {
            if (currentComments.get(i).getId().equals(comment.getId())) {
                commentAdapter.removeComment(i);
                Toast.makeText(this, "Test comment deleted locally", Toast.LENGTH_SHORT).show();
                
                // Show "no comments" if list is empty
                if (commentAdapter.getItemCount() == 0) {
                    rvComments.setVisibility(View.GONE);
                    tvNoComments.setVisibility(View.VISIBLE);
                }
                return;
            }
        }
    }
    
    /**
     * Get current comments from adapter
     */
    private java.util.List<com.example.tralalero.domain.model.TaskComment> getCurrentComments() {
        return commentAdapter.getComments();
    }

    /**
     * Pick File for Upload
     */
    private void pickFile() {
        android.util.Log.d("CardDetail", "pickFile() called - launching file picker");
        filePickerLauncher.launch("*/*");
    }

    /**
     * Download Attachment
     */
    private void downloadAttachment(com.example.tralalero.domain.model.Attachment attachment) {
        android.util.Log.d("CardDetail", "Downloading attachment: " + attachment.getFileName());
        
        // Check if filename is null
        String originalFileName = attachment.getFileName();
        final String fileName = (originalFileName == null || originalFileName.isEmpty()) 
            ? "download_" + System.currentTimeMillis() 
            : originalFileName;
        
        // Get the full URL - need to call backend to get signed URL first
        getAttachmentViewUrl(attachment, (signedUrl) -> {
            if (signedUrl != null && !signedUrl.isEmpty()) {
                performDownload(signedUrl, fileName);
            } else {
                Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Get signed URL for attachment viewing/downloading
     */
    private void getAttachmentViewUrl(com.example.tralalero.domain.model.Attachment attachment, 
                                     java.util.function.Consumer<String> callback) {
        // Call ViewModel to get signed URL
        taskViewModel.getAttachmentViewUrl(attachment.getId(), new TaskViewModel.AttachmentViewUrlCallback() {
            @Override
            public void onSuccess(String viewUrl) {
                callback.accept(viewUrl);
            }
            
            @Override
            public void onError(String error) {
                android.util.Log.e("CardDetail", "Failed to get attachment view URL: " + error);
                Toast.makeText(CardDetailActivity.this, "Failed to get download URL: " + error, Toast.LENGTH_SHORT).show();
                callback.accept(null);
            }
        });
    }

    // TODO : check
    /**
     * Perform actual download with signed URL
     */
    private void performDownload(String signedUrl, String fileName) {
        try {
            // Create download manager request
            android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(
                android.net.Uri.parse(signedUrl)
            );
            
            request.setDestinationInExternalPublicDir(
                android.os.Environment.DIRECTORY_DOWNLOADS, 
                fileName
            );
            
            request.setNotificationVisibility(
                android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            );
            
            request.setTitle("Downloading " + fileName);
            request.setDescription("Downloading attachment from PlantTracker");
            
            // Get download manager and enqueue
            android.app.DownloadManager downloadManager = 
                (android.app.DownloadManager) getSystemService(android.content.Context.DOWNLOAD_SERVICE);
            
            if (downloadManager != null) {
                downloadManager.enqueue(request);
                Toast.makeText(this, "Downloading " + fileName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Download service not available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            android.util.Log.e("CardDetail", "Download error: " + e.getMessage());
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Delete Attachment
     */
    private void deleteAttachment(com.example.tralalero.domain.model.Attachment attachment) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Attachment")
                .setMessage("Are you sure you want to delete '" + attachment.getFileName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    performDeleteAttachment(attachment);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Perform actual attachment deletion
     */
    private void performDeleteAttachment(com.example.tralalero.domain.model.Attachment attachment) {
        android.util.Log.d("CardDetail", "Deleting attachment: " + attachment.getId());
        
        // Check if this is a test attachment (starts with "test-attachment-")
        if (attachment.getId().startsWith("test-attachment-")) {
            // Handle test attachment deletion locally
            deleteTestAttachment(attachment);
            return;
        }
        
        // Call ViewModel delete method
        taskViewModel.deleteAttachment(attachment.getId(), taskId);
    }
    
    /**
     * Delete test attachment locally
     */
    private void deleteTestAttachment(com.example.tralalero.domain.model.Attachment attachment) {
        // Find the attachment in current adapter data and remove it
        java.util.List<com.example.tralalero.domain.model.Attachment> currentAttachments = getCurrentAttachments();
        for (int i = 0; i < currentAttachments.size(); i++) {
            if (currentAttachments.get(i).getId().equals(attachment.getId())) {
                attachmentAdapter.removeAttachment(i);
                Toast.makeText(this, "Test attachment deleted locally", Toast.LENGTH_SHORT).show();
                
                // Show "no attachments" if list is empty
                if (attachmentAdapter.getItemCount() == 0) {
                    rvAttachments.setVisibility(View.GONE);
                    tvNoAttachments.setVisibility(View.VISIBLE);
                }
                return;
            }
        }
    }
    
    /**
     * Get current attachments from adapter
     */
    private java.util.List<com.example.tralalero.domain.model.Attachment> getCurrentAttachments() {
        return attachmentAdapter.getAttachments();
    }

    /**
     * Open Attachment
     */
    private void openAttachment(com.example.tralalero.domain.model.Attachment attachment) {
        android.util.Log.d("CardDetail", "Opening attachment: " + attachment.getFileName());
        
        // Check if fileName is null
        final String fileName = attachment.getFileName();
        if (fileName == null || fileName.isEmpty()) {
            Toast.makeText(this, "Invalid file name", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get signed URL for viewing
        getAttachmentViewUrl(attachment, (signedUrl) -> {
            if (signedUrl != null && !signedUrl.isEmpty()) {
                openWithIntent(signedUrl, fileName);
            } else {
                Toast.makeText(this, "Cannot open file - backend integration needed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Open file with Intent using signed URL
     */
    private void openWithIntent(String signedUrl, String fileName) {
        try {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            android.net.Uri uri = android.net.Uri.parse(signedUrl);
            
            // Determine MIME type based on file extension
            String mimeType = getMimeType(fileName);
            intent.setDataAndType(uri, mimeType);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            
            // Try to start the intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // No app can handle this file type, offer to download instead
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("No App Found")
                        .setMessage("No app found to open this file type. Would you like to download it instead?")
                        .setPositiveButton("Download", (dialog, which) -> 
                            performDownload(signedUrl, fileName))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        } catch (Exception e) {
            android.util.Log.e("CardDetail", "Error opening attachment: " + e.getMessage());
            Toast.makeText(this, "Error opening file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Get MIME type based on file extension
     */
    private String getMimeType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "*/*";
        }
        
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot <= 0 || lastDot >= fileName.length() - 1) {
            return "*/*";
        }
        
        String extension = fileName.substring(lastDot + 1).toLowerCase();
        
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "doc":
            case "docx":
                return "application/msword";
            case "xls":
            case "xlsx":
                return "application/vnd.ms-excel";
            case "ppt":
            case "pptx":
                return "application/vnd.ms-powerpoint";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "mp4":
                return "video/mp4";
            case "mp3":
                return "audio/mp3";
            case "txt":
                return "text/plain";
            case "zip":
                return "application/zip";
            default:
                return "*/*";
        }
    }
    
    /**
     * Upload file using AttachmentUploader
     */
    private void uploadFile(android.net.Uri uri) {
        if (attachmentUploader == null) {
            android.util.Log.e("CardDetail", "AttachmentUploader is null");
            Toast.makeText(this, "Upload service not initialized", Toast.LENGTH_SHORT).show();
            return;
        }
        
        attachmentUploader.uploadFile(taskId, uri, new AttachmentUploader.UploadCallback() {
            @Override
            public void onProgress(int percent) {
                android.util.Log.d("CardDetail", "Upload progress: " + percent + "%");
                runOnUiThread(() -> {
                    // TODO: Show progress bar UI
                    // For now just log progress
                });
            }

            @Override
            public void onSuccess(com.example.tralalero.data.remote.dto.task.AttachmentDTO attachmentDTO) {
                android.util.Log.d("CardDetail", "Upload success! File: " + attachmentDTO.fileName);
                runOnUiThread(() -> {
                    // Map DTO to domain model
                    com.example.tralalero.domain.model.Attachment attachment = 
                        new com.example.tralalero.domain.model.Attachment(
                            attachmentDTO.id,
                            taskId,
                            attachmentDTO.url != null ? attachmentDTO.url : "",
                            attachmentDTO.fileName,
                            attachmentDTO.mimeType,
                            attachmentDTO.size,
                            null,
                            new java.util.Date()
                        );
                    
                    // Add to ViewModel and reload list
                    taskViewModel.addAttachment(taskId, attachment);
                    loadTaskAttachments(); // Reload to refresh UI
                    
                    Toast.makeText(CardDetailActivity.this, 
                        "File uploaded successfully!", 
                        Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("CardDetail", "Upload error: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(CardDetailActivity.this, 
                        "Upload failed: " + error, 
                        Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
