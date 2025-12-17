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
import android.widget.PopupMenu;
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
import com.example.tralalero.data.remote.api.ProjectApiService;
import com.example.tralalero.data.remote.dto.project.ProjectDTO;
import com.example.tralalero.data.remote.dto.project.ProjectMemberDTO;
import com.example.tralalero.data.remote.dto.task.TaskDTO;
import com.example.tralalero.data.remote.dto.task.TaskCalendarSyncRequest;
import com.example.tralalero.data.remote.mapper.ProjectMemberMapper;
import com.example.tralalero.domain.model.ProjectMember;
import com.example.tralalero.domain.model.AssigneeInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import com.example.tralalero.data.remote.api.UserApiService;
import com.example.tralalero.data.remote.api.GoogleAuthApiService;
import com.example.tralalero.feature.task.attachments.AttachmentUploader;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.domain.model.Label;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.model.GoogleCalendarStatusResponse;
import com.example.tralalero.domain.model.AuthUrlResponse;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.domain.usecase.task.*;
import com.example.tralalero.feature.home.ui.Home.task.TaskCalendarSyncViewModel;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.presentation.viewmodel.TaskViewModel;
import com.example.tralalero.presentation.viewmodel.TaskViewModelFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardDetailActivity extends AppCompatActivity {
    private static final String TAG = "CardDetailActivity";
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_BOARD_ID = "board_id";
    public static final String EXTRA_BOARD_NAME = "board_name"; 
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
    private String projectType;  // PERSONAL or TEAM
    private boolean isEditMode;
    private TaskViewModel taskViewModel;
    private TaskCalendarSyncViewModel calendarSyncViewModel;
    private com.example.tralalero.presentation.viewmodel.LabelViewModel labelViewModel;
    private ImageView ivClose;
    private ImageView ivMore;
    private EditText etTaskTitle; // Changed from RadioButton to EditText
    private TextView tvBoardName; // NEW: Display board name
    private TextView tvProjectBadge;
    private MaterialButton btnBoardStatus;
    private MaterialButton btnMembers;
    private MaterialButton btnAddAttachment;
    private MaterialButton btnAddComment;
    private MaterialButton btnDeleteTask;
    private MaterialButton btnConfirm;
    private EditText etDescription;
    private EditText etDueDate;
    private MaterialButton btnLowPriority;
    private MaterialButton btnMediumPriority;
    private MaterialButton btnHighPriority;
    private Task.TaskPriority currentPriority = Task.TaskPriority.MEDIUM;
    private Task.TaskStatus currentStatus = Task.TaskStatus.TO_DO;
    
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
    private TaskApiService taskApiService; // ✅ For calendar sync API calls
    
    // Attachment Upload Progress UI
    private LinearLayout layoutCardUploadProgress;
    private TextView tvCardUploadProgress;
    private android.widget.ProgressBar progressBarCardUpload;
    
    // Calendar Sync UI
    private SwitchMaterial switchCalendarSync;
    private MaterialButton btnViewInCalendar;
    private TextView tvCalendarEventInfo;
    private boolean isCalendarSyncEnabled = false;
    private boolean isPopulatingCalendarUI = false; // ✅ Flag to prevent listener trigger during UI population
    private boolean isGoogleCalendarConnected = false; // ✅ Store actual connection state from API
    private GoogleAuthApiService googleAuthApiService;
    
    // Dropdown UI elements
    private LinearLayout layoutAttachments;
    private LinearLayout layoutDetails;
    private LinearLayout layoutDetailsContent;
    private ImageView ivAttachmentDropdown;
    private ImageView ivDetailsDropdown;
    private TextView tvAttachmentCount;
    private boolean isAttachmentsExpanded = false;
    private boolean isDetailsExpanded = true;
    
    // New UI elements for interactive details
    private LinearLayout layoutAssignee;
    private LinearLayout layoutPriority;
    private LinearLayout layoutLabels;
    private LinearLayout layoutStartDate;
    private RecyclerView rvAssignees;
    private TextView tvNoAssignees;
    private ImageView ivAddAssignee;
    private AssigneeAdapter assigneeAdapter;
    private TextView tvPriority;
    private View viewPriorityIndicator;
    private EditText etStartDate;
    private MaterialButton btnQuickAssign;
    private MaterialButton btnSaveChanges;
    private boolean hasUnsavedChanges = false;
    private boolean isPopulatingUI = false; // Flag to prevent TextWatcher trigger during load
    private String currentAssigneeId = null;
    
    // Permission fields
    private String currentUserId;
    private String currentUserRole; // OWNER, ADMIN, or MEMBER
    private String taskCreatorId;
    private List<String> taskAssigneeIds = new ArrayList<>();
    private boolean hasShownPermissionToast = false; // Flag to prevent toast spam

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
        setupCalendarSyncUI();
        
        // ✅ Get current user ID from TokenManager
        com.example.tralalero.auth.storage.TokenManager tokenManager = new com.example.tralalero.auth.storage.TokenManager(this);
        currentUserId = tokenManager.getInternalUserId();
        
        // ✅ Fetch user role for permission checks
        if (projectId != null && currentUserId != null) {
            fetchCurrentUserRole();
        }
        
        // ✅ Load task data if in edit mode
        if (isEditMode && taskId != null && !taskId.isEmpty()) {
            android.util.Log.d(TAG, "📱 onCreate - Edit mode detected, loading task: " + taskId);
            loadTaskDetails();
            loadTaskAttachments();
            loadTaskComments();
            loadChecklistItems();
            loadTaskLabels();
        } else {
            android.util.Log.d(TAG, "📱 onCreate - Create mode (isEditMode=" + isEditMode + ", taskId=" + taskId + ")");
        }
        
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
            loadTaskDetails();     // ✅ Reload task details (description, dates, calendar sync)
            loadTaskAttachments();
            loadTaskComments();
            loadChecklistItems();  // CRITICAL - Reload checklist items!
            loadTaskLabels();
        }
    }
    
    /**
     * Fetch current user's role in this project for permission checks
     */
    private void fetchCurrentUserRole() {
        if (projectId == null || currentUserId == null) {
            android.util.Log.d("CardDetail", "⚠️ fetchCurrentUserRole - projectId or currentUserId is null");
            return;
        }
        
        android.util.Log.d("CardDetail", "🔍 Fetching user role for userId: " + currentUserId + " in project: " + projectId);
        
        ProjectApiService projectApi = ApiClient.get(App.authManager).create(ProjectApiService.class);
        projectApi.getProjectMembers(projectId).enqueue(new Callback<List<ProjectMemberDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProjectMemberDTO>> call, 
                                 @NonNull Response<List<ProjectMemberDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("CardDetail", "📋 Found " + response.body().size() + " project members");
                    for (ProjectMemberDTO member : response.body()) {
                        android.util.Log.d("CardDetail", "  - Member: userId=" + member.getUserId() + ", role=" + member.getRole());
                        if (currentUserId.equals(member.getUserId())) {
                            currentUserRole = member.getRole();
                            android.util.Log.d("CardDetail", "✅ User role assigned: " + currentUserRole);
                            // Update UI permissions after role is loaded
                            updateTaskPermissionUI();
                            break;
                        }
                    }
                    if (currentUserRole == null) {
                        android.util.Log.e("CardDetail", "❌ Current user not found in project members!");
                    }
                } else {
                    android.util.Log.e("CardDetail", "❌ Failed to fetch members - Response code: " + response.code());
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<List<ProjectMemberDTO>> call, @NonNull Throwable t) {
                android.util.Log.e("CardDetail", "❌ Failed to fetch user role", t);
            }
        });
    }
    
    /**
     * Check if current user can modify this task
     * Rules: Task creator OR assignees OR project OWNER/ADMIN can modify
     */
    private boolean canUserModifyTask() {
        if (currentUserId == null) return false;
        
        // Task creator can always modify
        if (currentUserId.equals(taskCreatorId)) {
            return true;
        }
        
        // Task assignees can modify
        if (taskAssigneeIds != null && taskAssigneeIds.contains(currentUserId)) {
            return true;
        }
        
        // OWNER or ADMIN can modify any task
        if ("OWNER".equals(currentUserRole) || "ADMIN".equals(currentUserRole)) {
            return true;
        }
        
        // If role hasn't loaded yet, allow modification (don't block prematurely)
        // The permission will be re-checked once role is loaded
        if (currentUserRole == null) {
            android.util.Log.d("CardDetail", "  ⚠️ Role not loaded yet, allowing modification temporarily");
            return true;
        }
        
        // MEMBER can only modify their own tasks or tasks they're assigned to
        return false;
    }
    
    /**
     * Check if current user can DELETE this task
     * Rules: ONLY Task creator OR project OWNER/ADMIN can delete
     */
    private boolean canUserDeleteTask() {
        android.util.Log.d("CardDetail", "🔍 DELETE PERMISSION CHECK:");
        android.util.Log.d("CardDetail", "  - currentUserId: " + currentUserId);
        android.util.Log.d("CardDetail", "  - taskCreatorId: " + taskCreatorId);
        android.util.Log.d("CardDetail", "  - currentUserRole: " + currentUserRole);
        
        if (currentUserId == null) {
            android.util.Log.d("CardDetail", "  ❌ currentUserId is null");
            return false;
        }
        
        // Task creator can always delete
        if (taskCreatorId != null && currentUserId.equals(taskCreatorId)) {
            android.util.Log.d("CardDetail", "  ✅ User is task creator - CAN DELETE");
            return true;
        }
        
        // OWNER or ADMIN can delete any task
        if (currentUserRole != null && 
            ("OWNER".equalsIgnoreCase(currentUserRole) || "ADMIN".equalsIgnoreCase(currentUserRole))) {
            android.util.Log.d("CardDetail", "  ✅ User is OWNER/ADMIN - CAN DELETE");
            return true;
        }
        
        // If role hasn't loaded yet, allow deletion temporarily (will be re-checked)
        if (currentUserRole == null) {
            android.util.Log.d("CardDetail", "  ⚠️ Role not loaded yet, allowing deletion temporarily");
            return true;
        }
        
        // MEMBER and assignees CANNOT delete (even if assigned)
        android.util.Log.d("CardDetail", "  ❌ User is not creator and not OWNER/ADMIN - CANNOT DELETE");
        return false;
    }

    /**
     * Update UI based on task permissions
     */
    private void updateTaskPermissionUI() {
        android.util.Log.d("CardDetail", "🔧 updateTaskPermissionUI called");
        android.util.Log.d("CardDetail", "  - currentUserId: " + currentUserId);
        android.util.Log.d("CardDetail", "  - currentUserRole: " + currentUserRole);
        android.util.Log.d("CardDetail", "  - taskCreatorId: " + taskCreatorId);
        android.util.Log.d("CardDetail", "  - taskAssigneeIds: " + taskAssigneeIds);
        
        boolean canModify = canUserModifyTask();
        android.util.Log.d("CardDetail", "  - canModify result: " + canModify);
        
        if (btnDeleteTask != null) {
            if (canModify && isEditMode) {
                btnDeleteTask.setVisibility(View.VISIBLE);
                btnDeleteTask.setEnabled(true);
            } else {
                btnDeleteTask.setVisibility(View.GONE);
            }
        }
        
        // Disable editing if no permission
        if (!canModify && isEditMode) {
            etTaskTitle.setEnabled(false);
            etDescription.setEnabled(false);
            btnConfirm.setEnabled(false);
            btnConfirm.setAlpha(0.5f);
            btnSaveChanges.setEnabled(false);
            btnSaveChanges.setAlpha(0.5f);
            
            // Show message ONLY ONCE
            if (!hasShownPermissionToast) {
                android.util.Log.d("CardDetail", "  ⚠️ Showing permission toast (first time)");
                Toast.makeText(this, "⚠️ Bạn không có quyền chỉnh sửa task này", Toast.LENGTH_SHORT).show();
                hasShownPermissionToast = true;
            } else {
                android.util.Log.d("CardDetail", "  ⚠️ Permission toast already shown, skipping");
            }
        }
    }

    private void getIntentData() {
        if (getIntent() != null) {
            // ✅ Debug: Log all extras
            android.util.Log.d(TAG, "=== Intent Extras Debug ===");
            android.os.Bundle extras = getIntent().getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Object value = extras.get(key);
                    android.util.Log.d(TAG, "  Extra: " + key + " = " + value + " (type: " + (value != null ? value.getClass().getSimpleName() : "null") + ")");
                }
            } else {
                android.util.Log.d(TAG, "  No extras in Intent!");
            }
            android.util.Log.d(TAG, "=========================");
            
            taskId = getIntent().getStringExtra(EXTRA_TASK_ID);
            boardId = getIntent().getStringExtra(EXTRA_BOARD_ID);
            boardName = getIntent().getStringExtra(EXTRA_BOARD_NAME);
            projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
            isEditMode = getIntent().getBooleanExtra(EXTRA_IS_EDIT_MODE, false);
            
            android.util.Log.d(TAG, "📋 Parsed values:");
            android.util.Log.d(TAG, "  taskId: " + taskId);
            android.util.Log.d(TAG, "  boardId: " + boardId);
            android.util.Log.d(TAG, "  projectId: " + projectId);
            android.util.Log.d(TAG, "  isEditMode: " + isEditMode + " (from key: '" + EXTRA_IS_EDIT_MODE + "')");
        }
    }

    private void initViews() {
        ivClose = findViewById(R.id.ivClose);
        ivMore = findViewById(R.id.ivMore);
        etTaskTitle = findViewById(R.id.etTaskTitle); // Changed from rbTaskTitle
        tvBoardName = findViewById(R.id.tvBoardName); // NEW
        tvProjectBadge = findViewById(R.id.tvProjectBadge);
        btnBoardStatus = findViewById(R.id.btnBoardStatus);
        btnMembers = findViewById(R.id.btnMembers);

        btnDeleteTask = findViewById(R.id.btnDeleteTask);
        btnConfirm = findViewById(R.id.btnConfirm);
        etDescription = findViewById(R.id.etDescription);
        etDueDate = findViewById(R.id.etDueDate);
        
        // Priority Buttons
        btnLowPriority = findViewById(R.id.btnLowPriority);
        btnMediumPriority = findViewById(R.id.btnMediumPriority);
        btnHighPriority = findViewById(R.id.btnHighPriority);
        
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
        
        // Attachment Upload Progress UI
        layoutCardUploadProgress = findViewById(R.id.layoutCardUploadProgress);
        tvCardUploadProgress = findViewById(R.id.tvCardUploadProgress);
        progressBarCardUpload = findViewById(R.id.progressBarCardUpload);
        
        // Calendar Sync Views
        switchCalendarSync = findViewById(R.id.switchCalendarSync);
        btnViewInCalendar = findViewById(R.id.btnViewInCalendar);
        tvCalendarEventInfo = findViewById(R.id.tvCalendarEventInfo);
        
        // Dropdown elements
        layoutAttachments = findViewById(R.id.layoutAttachments);
        layoutDetails = findViewById(R.id.layoutDetails);
        layoutDetailsContent = findViewById(R.id.layoutDetailsContent);
        ivAttachmentDropdown = findViewById(R.id.ivAttachmentDropdown);
        ivDetailsDropdown = findViewById(R.id.ivDetailsDropdown);
        tvAttachmentCount = findViewById(R.id.tvAttachmentCount);
        
        // Details section interactive elements
        layoutAssignee = findViewById(R.id.layoutAssignee);
        layoutPriority = findViewById(R.id.layoutPriority);
        layoutLabels = findViewById(R.id.layoutLabels);
        layoutStartDate = findViewById(R.id.layoutStartDate);
        rvAssignees = findViewById(R.id.rvAssignees);
        tvNoAssignees = findViewById(R.id.tvNoAssignees);
        ivAddAssignee = findViewById(R.id.ivAddAssignee);
        tvPriority = findViewById(R.id.tvPriority);
        viewPriorityIndicator = findViewById(R.id.viewPriorityIndicator);
        etStartDate = findViewById(R.id.etStartDate);
        btnQuickAssign = findViewById(R.id.btnQuickAssign);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        
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
        
        // Setup Assignees RecyclerView
        LinearLayoutManager assigneesLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvAssignees.setLayoutManager(assigneesLayoutManager);
        assigneeAdapter = new AssigneeAdapter(user -> {
            // Remove assignee callback
            markUnsavedChanges();
            assigneeAdapter.getAssignees().remove(user);
            assigneeAdapter.notifyDataSetChanged();
            updateAssigneesVisibility();
        });
        rvAssignees.setAdapter(assigneeAdapter);
        
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
        this.taskApiService = apiService; // ✅ Store reference for calendar sync
        CommentApiService commentApiService = ApiClient.get(App.authManager).create(CommentApiService.class);
        AttachmentApiService attachmentApiService = ApiClient.get(App.authManager).create(AttachmentApiService.class);
        UserApiService userApiService = ApiClient.get(App.authManager).create(UserApiService.class);
        this.userApiService = userApiService; // Store reference for CommentAdapter
        ITaskRepository repository = new TaskRepositoryImpl(apiService, commentApiService, attachmentApiService);
        GetTaskByIdUseCase getTaskByIdUseCase = new GetTaskByIdUseCase(repository);
        GetTasksByBoardUseCase getTasksByBoardUseCase = new GetTasksByBoardUseCase(repository);
        CreateTaskUseCase createTaskUseCase = new CreateTaskUseCase(repository);
        CreateQuickTaskUseCase createQuickTaskUseCase = new CreateQuickTaskUseCase(repository);
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
                createQuickTaskUseCase,
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
        calendarSyncViewModel = new ViewModelProvider(this).get(TaskCalendarSyncViewModel.class);
        
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
        
        // Set context for avatar loading (like ActivityLogAdapter)
        commentAdapter.setContext(this);
        
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
            android.util.Log.d("CardDetailActivity", "📋 Labels observer triggered: " + (labels != null ? labels.size() : 0) + " labels");
            if (labels != null) {
                selectedLabels.clear();
                selectedLabels.addAll(labels);
                android.util.Log.d("CardDetailActivity", "  ✅ Updating UI with " + selectedLabels.size() + " labels");
                displaySelectedLabels();
            } else {
                android.util.Log.d("CardDetailActivity", "  ⚠️ Labels is null, clearing UI");
                selectedLabels.clear();
                displaySelectedLabels();
            }
        });
    }

    private void observeViewModel() {
        // ✅ Observe task details (for loading calendar sync state)
        taskViewModel.getSelectedTask().observe(this, task -> {
            if (task != null && isEditMode) {
                // ✅ Set flag to prevent TextWatcher from marking as changed
                isPopulatingUI = true;
                
                android.util.Log.d(TAG, "✅ Task loaded from API - ID: " + task.getId());
                android.util.Log.d(TAG, "  Title: " + task.getTitle());
                android.util.Log.d(TAG, "  BoardId: " + task.getBoardId());
                android.util.Log.d(TAG, "  BoardName: " + task.getBoardName());
                android.util.Log.d(TAG, "  Description: " + (task.getDescription() != null ? task.getDescription().substring(0, Math.min(50, task.getDescription().length())) : "null"));
                android.util.Log.d(TAG, "  StartAt: " + task.getStartAt());
                android.util.Log.d(TAG, "  DueAt: " + task.getDueAt());
                android.util.Log.d(TAG, "  Priority: " + task.getPriority());
                android.util.Log.d(TAG, "  Status: " + task.getStatus());
                android.util.Log.d(TAG, "  Assignees count: " + (task.getAssignees() != null ? task.getAssignees().size() : 0));
                android.util.Log.d(TAG, "  Labels count: " + (task.getLabels() != null ? task.getLabels().size() : 0));
                android.util.Log.d(TAG, "  CalendarSyncEnabled: " + task.isCalendarSyncEnabled());
                
                // ✅ Populate ALL task fields in UI
                
                // ✅ Populate title
                if (task.getTitle() != null && !task.getTitle().isEmpty()) {
                    etTaskTitle.setText(task.getTitle());
                    android.util.Log.d(TAG, "  ✅ Title populated in UI: " + task.getTitle());
                }
                
                if (task.getDescription() != null) {
                    etDescription.setText(task.getDescription());
                    android.util.Log.d(TAG, "  ✅ Description populated in UI");
                }
                
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
                if (task.getStartAt() != null) {
                    etStartDate.setText(displayFormat.format(task.getStartAt()));
                    android.util.Log.d(TAG, "  ✅ Start date populated: " + displayFormat.format(task.getStartAt()));
                } else if (task.getCreatedAt() != null) {
                    // Auto-fill with createdAt if startAt is null
                    etStartDate.setText(displayFormat.format(task.getCreatedAt()));
                    android.util.Log.d(TAG, "  ✅ Start date auto-filled with createdAt: " + displayFormat.format(task.getCreatedAt()));
                } else {
                    etStartDate.setText("");
                    android.util.Log.d(TAG, "  ⚠️ Start date cleared (null)");
                }
                
                if (task.getDueAt() != null) {
                    etDueDate.setText(displayFormat.format(task.getDueAt()));
                    android.util.Log.d(TAG, "  ✅ Due date populated: " + displayFormat.format(task.getDueAt()));
                } else {
                    etDueDate.setText("");
                    android.util.Log.d(TAG, "  ⚠️ Due date cleared (null)");
                }
                
                // ✅ Update priority
                if (task.getPriority() != null) {
                    currentPriority = task.getPriority();
                    updatePriorityUIDetails();
                    android.util.Log.d(TAG, "  ✅ Priority populated: " + currentPriority);
                }
                
                // ✅ Update assignees RecyclerView (support multiple assignees)
                if (task.getAssignees() != null && !task.getAssignees().isEmpty()) {
                    List<com.example.tralalero.data.remote.dto.user.UserDTO> assignees = new ArrayList<>();
                    for (AssigneeInfo assigneeInfo : task.getAssignees()) {
                        com.example.tralalero.data.remote.dto.user.UserDTO user = new com.example.tralalero.data.remote.dto.user.UserDTO();
                        user.setId(assigneeInfo.getId());
                        user.setName(assigneeInfo.getName());
                        user.setEmail(assigneeInfo.getEmail() != null ? assigneeInfo.getEmail() : "");
                        assignees.add(user);
                    }
                    assigneeAdapter.setAssignees(assignees);
                    updateAssigneesVisibility();
                    android.util.Log.d(TAG, "  ✅ " + assignees.size() + " assignee(s) populated");
                } else {
                    assigneeAdapter.setAssignees(new ArrayList<>());
                    updateAssigneesVisibility();
                    android.util.Log.d(TAG, "  ⚠️ No assignees");
                }
                
                // ✅ Update task status
                if (task.getStatus() != null) {
                    currentStatus = task.getStatus();
                    android.util.Log.d(TAG, "  ✅ Task status populated: " + currentStatus);
                }
                
                // ✅ Update board name from task (both tvBoardName and btnBoardStatus)
                if (task.getBoardName() != null && !task.getBoardName().isEmpty()) {
                    boardName = task.getBoardName();
                    tvBoardName.setText("In " + boardName);
                    updateBoardNameUI();
                    android.util.Log.d(TAG, "  ✅ Board name populated: " + boardName);
                }
                
                // Populate calendar sync UI
                populateCalendarSyncUI(task);
                
                // ✅ Update calendar sync availability after populating UI
                updateCalendarSyncAvailability();
                
                // ✅ Reset flag after all UI population is done
                isPopulatingUI = false;
                android.util.Log.d(TAG, "  ✅ UI population complete");
            }
        });
        
        // Observe calendar sync updates
        calendarSyncViewModel.getSyncUpdatedTask().observe(this, updatedTask -> {
            if (updatedTask != null) {
                android.util.Log.d("CardDetailActivity", "✅ Calendar sync successful: enabled=" + 
                    updatedTask.getCalendarReminderEnabled() + ", eventId=" + updatedTask.getCalendarEventId());
            }
        });
        
        calendarSyncViewModel.getSyncError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Calendar sync error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
        
        taskViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                taskViewModel.clearError();
            }
        });
        
        // ✅ Observe task update result to reload UI with fresh data
        taskViewModel.getUpdateTaskResult().observe(this, updatedTask -> {
            if (updatedTask != null) {
                android.util.Log.d(TAG, "✅ Task updated successfully, reloading from server...");
                // Reload task to get complete data from backend (including assignees/labels)
                taskViewModel.loadTaskById(taskId);
                Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
            }
        });
        
        taskViewModel.getUpdateError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                android.util.Log.e(TAG, "❌ Update failed: " + error);
                Toast.makeText(this, "Update failed: " + error, Toast.LENGTH_SHORT).show();
                // Re-enable save button on error
                hasUnsavedChanges = true;
                btnSaveChanges.setVisibility(View.VISIBLE);
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
                rvAttachments.setVisibility(isAttachmentsExpanded ? View.VISIBLE : View.GONE);
                tvNoAttachments.setVisibility(View.GONE);
                tvAttachmentCount.setText(String.valueOf(attachments.size()));
                tvAttachmentCount.setVisibility(View.VISIBLE);
                android.util.Log.d("CardDetail", "Attachments RecyclerView visibility updated");
            } else {
                android.util.Log.d("CardDetail", "No attachments - showing empty state");
                rvAttachments.setVisibility(View.GONE);
                tvNoAttachments.setVisibility(isAttachmentsExpanded ? View.VISIBLE : View.GONE);
                tvAttachmentCount.setVisibility(View.GONE);
            }
        });
    }

    private void setupUI() {
        if (isEditMode) {
            // ✅ Only populate from Intent extras if taskId is NOT present
            // If taskId exists, loadTaskDetails() will populate from API with fresh data
            if (taskId == null || taskId.isEmpty()) {
                etTaskTitle.setText(getIntent().getStringExtra(EXTRA_TASK_TITLE));

                String priorityStr = getIntent().getStringExtra(EXTRA_TASK_PRIORITY);
                if (priorityStr != null) {
                    try {
                        currentPriority = Task.TaskPriority.valueOf(priorityStr);
                    } catch (IllegalArgumentException e) {
                        currentPriority = Task.TaskPriority.MEDIUM;
                    }
                }
            } else {
                // Task data will be loaded from API by loadTaskDetails()
                android.util.Log.d(TAG, "Edit mode with taskId - data will be loaded from API");
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
        
        // Load project name
        loadProjectName();
        
        // Update priority UI
        updatePriorityUI();
        
        // Load attachments and comments if in edit mode
        if (isEditMode && taskId != null && !taskId.isEmpty()) {
            loadTaskAttachments();
            loadTaskComments();
            loadChecklistItems();
            loadTaskLabels();
            loadTaskDetails();
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
        android.util.Log.d("CardDetailActivity", "📋 loadTaskLabels called - taskId: " + taskId + ", labelViewModel: " + (labelViewModel != null ? "initialized" : "NULL"));
        // Load task labels from API via ViewModel
        if (taskId != null && !taskId.isEmpty() && labelViewModel != null) {
            android.util.Log.d("CardDetailActivity", "  ✅ Loading labels from API");
            labelViewModel.loadTaskLabels(taskId);
        } else {
            android.util.Log.e("CardDetailActivity", "  ❌ Cannot load labels - conditions not met");
        }
    }
    

    private void loadTaskDetails() {
        if (taskId != null && !taskId.isEmpty()) {
            android.util.Log.d(TAG, "🔍 Loading task details for taskId: " + taskId);
            
            // Load task and extract creator/assignees for permission check
            taskViewModel.getSelectedTask().observe(this, task -> {
                if (task != null) {
                    taskCreatorId = task.getCreatedBy();
                    
                    // Extract assignee IDs
                    if (task.getAssignees() != null) {
                        taskAssigneeIds.clear();
                        for (AssigneeInfo assignee : task.getAssignees()) {
                            if (assignee.getId() != null) {
                                taskAssigneeIds.add(assignee.getId());
                            }
                        }
                    }
                    
                    android.util.Log.d("CardDetail", "✅ Task loaded - creator: " + taskCreatorId + ", assignees: " + taskAssigneeIds);
                    
                    // Update permission UI
                    updateTaskPermissionUI();
                }
            });
            
            taskViewModel.loadTaskById(taskId);
        }
    }
    
    private void loadProjectName() {
        if (projectId == null || projectId.isEmpty()) return;
        
        ProjectApiService projectApiService = ApiClient.get(App.authManager).create(ProjectApiService.class);
        projectApiService.getProjectById(projectId).enqueue(new Callback<ProjectDTO>() {
            @Override
            public void onResponse(@NonNull Call<ProjectDTO> call, @NonNull Response<ProjectDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProjectDTO project = response.body();
                    tvProjectBadge.setText(project.getName());
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ProjectDTO> call, @NonNull Throwable t) {
                android.util.Log.e(TAG, "Failed to load project name", t);
            }
        });
    }

    private void setupListeners() {
        ivClose.setOnClickListener(v -> {
            finish();
        });
        
        ivMore.setOnClickListener(v -> {
            showMoreOptionsMenu();
        });
        
        btnMembers.setOnClickListener(v -> {
            checkRoleAndAssign();
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
        etDueDate.setOnClickListener(v -> showDatePickerDialog(false));
        
        // Priority buttons
        btnLowPriority.setOnClickListener(v -> {
            currentPriority = Task.TaskPriority.LOW;
            updatePriorityUI();
        });
        
        btnMediumPriority.setOnClickListener(v -> {
            currentPriority = Task.TaskPriority.MEDIUM;
            updatePriorityUI();
        });
        
        btnHighPriority.setOnClickListener(v -> {
            currentPriority = Task.TaskPriority.HIGH;
            updatePriorityUI();
        });
        
        // Dropdown toggles
        layoutAttachments.setOnClickListener(v -> toggleAttachmentsDropdown());
        layoutDetails.setOnClickListener(v -> toggleDetailsDropdown());
        
        // Details section interactions
        layoutAssignee.setOnClickListener(v -> checkRoleAndAssign());
        btnQuickAssign.setOnClickListener(v -> checkRoleAndAssign());
        layoutPriority.setOnClickListener(v -> showPrioritySelectionDialog());
        layoutLabels.setOnClickListener(v -> showLabelSelectionDialog());
        etStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        
        // Board status selection
        btnBoardStatus.setOnClickListener(v -> showBoardSelectionDialog());
        
        // ✅ TEXT WATCHER: Mark unsaved changes on text input
        android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                // Don't mark as changed if we're just populating UI from API
                if (!isPopulatingUI) {
                    markUnsavedChanges();
                }
            }
        };
        
        etTaskTitle.addTextChangedListener(textWatcher);
        etDescription.addTextChangedListener(textWatcher);
        
        // ✅ SAVE BUTTON: Save all changes
        btnSaveChanges.setOnClickListener(v -> {
            updateTask();
        });
        
        // ✅ ADD ASSIGNEE: Show member selection dialog
        ivAddAssignee.setOnClickListener(v -> checkRoleAndAssign());
    }
    
    private void toggleAttachmentsDropdown() {
        isAttachmentsExpanded = !isAttachmentsExpanded;
        rvAttachments.setVisibility(isAttachmentsExpanded ? View.VISIBLE : View.GONE);
        tvNoAttachments.setVisibility(isAttachmentsExpanded && (attachmentAdapter == null || attachmentAdapter.getItemCount() == 0) ? View.VISIBLE : View.GONE);
        ivAttachmentDropdown.setRotation(isAttachmentsExpanded ? 180 : 0);
    }
    
    private void toggleDetailsDropdown() {
        isDetailsExpanded = !isDetailsExpanded;
        layoutDetailsContent.setVisibility(isDetailsExpanded ? View.VISIBLE : View.GONE);
        ivDetailsDropdown.setRotation(isDetailsExpanded ? 180 : 0);
    }
    
    private void updatePriorityUI() {
        // Reset all buttons to outlined style
        btnLowPriority.setBackgroundColor(Color.TRANSPARENT);
        btnMediumPriority.setBackgroundColor(Color.TRANSPARENT);
        btnHighPriority.setBackgroundColor(Color.TRANSPARENT);
        
        // Set selected button to filled style
        switch (currentPriority) {
            case LOW:
                btnLowPriority.setBackgroundColor(Color.parseColor("#E8F5E9"));
                break;
            case MEDIUM:
                btnMediumPriority.setBackgroundColor(Color.parseColor("#FFF3E0"));
                break;
            case HIGH:
                btnHighPriority.setBackgroundColor(Color.parseColor("#FFEBEE"));
                break;
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
        
        // ✅ Parse dates from EditTexts with time to avoid timezone issues
        Date dueAt = null;
        Date startAt = new Date(); // Auto-set start date to current time
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
        
        String dueDateStr = etDueDate.getText().toString().trim();
        if (!TextUtils.isEmpty(dueDateStr)) {
            try {
                // Append default time (12:00) if no time specified to avoid timezone shift
                String dateTimeStr = dueDateStr.contains(" ") ? dueDateStr : dueDateStr + " 12:00";
                dueAt = displayFormat.parse(dateTimeStr);
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error parsing due date: " + dueDateStr, e);
            }
        }
        
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
                null,            // sprintId
                null,            // epicId
                null,            // parentTaskId
                startAt,         // ✅ startAt (Date)
                dueAt,           // ✅ dueAt (Date)
                null,            // storyPoints
                null,            // originalEstimateSec
                null,            // remainingEstimateSec
                null,            // createdAt
                null,            // updatedAt
                // Calendar sync fields
                isCalendarSyncEnabled,
                null,            // calendar reminder minutes
                null,            // calendarEventId (backend will generate)
                null,            // calendarSyncedAt (backend will set)
                null,            // labels
                null             // assignees
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

        // ✅ Get description from EditText UI
        String description = etDescription.getText().toString().trim();
        
        // ✅ Parse dates from EditTexts with time to avoid timezone issues
        Date dueAt = null;
        Date startAt = null;
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
        
        String dueDateStr = etDueDate.getText().toString().trim();
        if (!TextUtils.isEmpty(dueDateStr)) {
            try {
                // Append default time (12:00) if no time specified to avoid timezone shift
                String dateTimeStr = dueDateStr.contains(" ") ? dueDateStr : dueDateStr + " 12:00";
                dueAt = displayFormat.parse(dateTimeStr);
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error parsing due date: " + dueDateStr, e);
            }
        }
        
        // ✅ FIX: Get current task to preserve existing fields
        Task currentTask = taskViewModel.getSelectedTask().getValue();
        if (currentTask == null) {
            Toast.makeText(this, "Error: Task not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // ✅ Create updated task with ONLY changed fields + preserved fields
        // This prevents null fields from overwriting existing data
        Task updatedTask = new Task(
                taskId,
                currentTask.getProjectId(),      // ✅ Preserve
                currentTask.getBoardId(),        // ✅ Preserve
                title,                           // ✅ Changed
                description,                     // ✅ Changed
                currentTask.getIssueKey(),       // ✅ Preserve
                currentTask.getType(),           // ✅ Preserve
                currentTask.getStatus(),         // ✅ Preserve (status changed via drag-drop, not here)
                currentPriority,                 // ✅ Changed
                currentTask.getPosition(),       // ✅ Preserve
                currentTask.getAssigneeId(),     // ✅ Preserve (assignee changed separately)
                currentTask.getCreatedBy(),      // ✅ Preserve
                currentTask.getSprintId(),       // ✅ Preserve
                currentTask.getEpicId(),         // ✅ Preserve
                currentTask.getParentTaskId(),   // ✅ Preserve
                startAt,                         // ✅ Changed (null if not set)
                dueAt,                           // ✅ Changed
                currentTask.getStoryPoints(),    // ✅ Preserve
                currentTask.getOriginalEstimateSec(),  // ✅ Preserve
                currentTask.getRemainingEstimateSec(), // ✅ Preserve
                currentTask.getCreatedAt(),      // ✅ Preserve
                currentTask.getUpdatedAt(),      // ✅ Backend will update
                // Calendar sync fields
                isCalendarSyncEnabled,
                null,  // calendar reminder minutes - no longer used
                currentTask.getCalendarEventId(),     // ✅ Preserve (backend will update)
                currentTask.getCalendarSyncedAt(),    // ✅ Preserve (backend will update)
                currentTask.getLabels(),              // ✅ Preserve
                currentTask.getAssignees()            // ✅ Preserve
        );
        
        taskViewModel.updateTask(taskId, updatedTask);
        
        // After updating task, sync calendar settings if task has due date
        if (isCalendarSyncEnabled && dueAt != null) {
            android.util.Log.d("CardDetailActivity", "📅 Syncing calendar: taskId=" + taskId + 
                ", enabled=" + isCalendarSyncEnabled + ", reminder=30");
            
            // Convert Date to ISO string for backend
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            String dueAtISO = isoFormat.format(dueAt);
            
            calendarSyncViewModel.updateCalendarSync(
                taskId, 
                isCalendarSyncEnabled, 
                30,  // default 30 minutes reminder
                dueAtISO
            );
        } else if (!isCalendarSyncEnabled) {
            // Disable sync if toggle is OFF
            android.util.Log.d("CardDetailActivity", "🗑️ Disabling calendar sync: taskId=" + taskId);
            calendarSyncViewModel.updateCalendarSync(taskId, false, 30, null);
        }
        
        // ✅ Hide save button - UI will reload via observer when update succeeds
        hasUnsavedChanges = false;
        btnSaveChanges.setVisibility(View.GONE);
    }

    private void showMoreOptionsMenu() {
        PopupMenu popup = new PopupMenu(this, ivMore);
        popup.getMenuInflater().inflate(R.menu.task_more_options, popup.getMenu());
        
        // Check user permissions for delete option - ONLY task creator OR admin/owner
        boolean canDelete = canUserDeleteTask();
        android.util.Log.d("CardDetail", "🔍 showMoreOptionsMenu - canDelete: " + canDelete);
        popup.getMenu().findItem(R.id.menu_delete_task).setVisible(canDelete);
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_delete_task) {
                showDeleteConfirmation();
                return true;
            }
            return false;
        });
        
        popup.show();
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
                null, null, null, null, null, null, null, null, null, null,
                false, null, null, null, null, null  // calendar sync + labels + assignees
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

    /**
     * Check user's role and either show AssignMemberBottomSheet (for owners)
     * or directly self-assign (for non-owners)
     */
    private void checkRoleAndAssign() {
        if (projectId == null || projectId.isEmpty()) {
            Toast.makeText(this, "Cannot assign task: Invalid project", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (taskId == null || taskId.isEmpty()) {
            Toast.makeText(this, "Task not yet created. Please save the task first.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Load project info to check project type
        ProjectApiService projectApiService = ApiClient.get(App.authManager).create(ProjectApiService.class);
        projectApiService.getProjectById(projectId).enqueue(new Callback<ProjectDTO>() {
            @Override
            public void onResponse(@NonNull Call<ProjectDTO> call, @NonNull Response<ProjectDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProjectDTO projectDTO = response.body();
                    projectType = projectDTO.getType();
                    
                    android.util.Log.d("CardDetailActivity", "Project type: " + projectType);
                    
                    // PERSONAL project: Only one person, auto self-assign
                    if ("PERSONAL".equalsIgnoreCase(projectType)) {
                        selfAssignTaskForPersonalProject();
                        return;
                    }
                    
                    // TEAM project: Check user role for permissions
                    checkRoleForTeamProject();
                } else {
                    Toast.makeText(CardDetailActivity.this, "Failed to load project info", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ProjectDTO> call, @NonNull Throwable t) {
                Toast.makeText(CardDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void checkRoleForTeamProject() {
        // Get current user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        final String currentUserId = currentUser.getUid();
        
        // Fetch project members to check current user's role
        ProjectApiService projectApiService = ApiClient.get(App.authManager).create(ProjectApiService.class);
        projectApiService.getProjectMembers(projectId).enqueue(new Callback<List<ProjectMemberDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProjectMemberDTO>> call, @NonNull Response<List<ProjectMemberDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String currentUserRole = null;
                    String currentUserInternalId = null;
                    
                    android.util.Log.d("CardDetailActivity", "Current Firebase UID: " + currentUserId);
                    android.util.Log.d("CardDetailActivity", "Members count: " + response.body().size());
                    
                    // Find current user's role and internal ID using Firebase UID
                    for (ProjectMemberDTO dto : response.body()) {
                        ProjectMember member = ProjectMemberMapper.toDomain(dto);
                        if (member != null) {
                            android.util.Log.d("CardDetailActivity", "Member: " + member.getName() + 
                                ", UserId: " + member.getUserId() + 
                                ", Role: " + member.getRole());
                            
                            // Compare userId (which is Firebase UID from backend)
                            if (member.getUserId() != null && member.getUserId().equals(currentUserId)) {
                                currentUserRole = member.getRole();
                                currentUserInternalId = member.getUserId();
                                android.util.Log.d("CardDetailActivity", "✅ Found current user! Role: " + currentUserRole + ", Internal ID: " + currentUserInternalId);
                                break;
                            }
                        }
                    }
                    
                    if (currentUserRole == null || currentUserInternalId == null) {
                        android.util.Log.e("CardDetailActivity", "❌ Current user not found in team project members!");
                        Toast.makeText(CardDetailActivity.this, "You are not a member of this team project", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // TEAM project permission logic:
                    // - OWNER/ADMIN: Can assign anyone (open bottomsheet)
                    // - MEMBER: Can only self-assign
                    if ("OWNER".equalsIgnoreCase(currentUserRole) || "ADMIN".equalsIgnoreCase(currentUserRole)) {
                        // Owner/Admin: Show member selection dialog
                        showAssignTaskDialog();
                    } else {
                        // Member: Self-assign directly using internal user ID
                        selfAssignTask(currentUserInternalId);
                    }
                } else {
                    Toast.makeText(CardDetailActivity.this, "Failed to check permissions", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<List<ProjectMemberDTO>> call, @NonNull Throwable t) {
                Toast.makeText(CardDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showAssignTaskDialog() {
        if (projectId == null || projectId.isEmpty()) {
            Toast.makeText(this, "Cannot assign task: Invalid project", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (taskId == null || taskId.isEmpty()) {
            Toast.makeText(this, "Task not yet created. Please save the task first.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Open new AssignMemberBottomSheet with multi-select support
        AssignMemberBottomSheet assignSheet = AssignMemberBottomSheet.newInstance(projectId, taskId);
        assignSheet.setOnAssigneesChangedListener(new AssignMemberBottomSheet.OnAssigneesChangedListener() {
            @Override
            public void onAssigneesChanged() {
                // Reload task details to show updated assignees
                Toast.makeText(CardDetailActivity.this, 
                    "Assignees updated", 
                    Toast.LENGTH_SHORT).show();
                // TODO: Reload task details if needed
            }
        });
        
        assignSheet.show(getSupportFragmentManager(), "assign_member");
    }

    private void selfAssignTask(String internalUserId) {
        if (taskId == null || taskId.isEmpty()) {
            Toast.makeText(this, "Task not yet created. Please save the task first.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (internalUserId == null || internalUserId.isEmpty()) {
            Toast.makeText(this, "Cannot assign task: Invalid user ID", Toast.LENGTH_SHORT).show();
            return;
        }
        
        android.util.Log.d("CardDetailActivity", "Self-assigning task with internal user ID: " + internalUserId);
        
        // Call API to assign task using internal user ID
        TaskApiService taskApiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        Map<String, List<String>> body = new HashMap<>();
        body.put("userIds", Arrays.asList(internalUserId));
        
        taskApiService.assignUsers(taskId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CardDetailActivity.this, "Task assigned to you", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CardDetailActivity.this, "Failed to assign task", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(CardDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void selfAssignTaskForPersonalProject() {
        if (taskId == null || taskId.isEmpty()) {
            Toast.makeText(this, "Task not yet created. Please save the task first.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get current user's internal ID from token manager
        String internalUserId = App.tokenManager.getInternalUserId();
        
        if (internalUserId == null || internalUserId.isEmpty()) {
            Toast.makeText(this, "Cannot assign task: User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        android.util.Log.d("CardDetailActivity", "Personal project - Self-assigning task to internal user ID: " + internalUserId);
        
        // Call API to assign task using internal user ID
        TaskApiService taskApiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        Map<String, List<String>> body = new HashMap<>();
        body.put("userIds", Arrays.asList(internalUserId));
        
        taskApiService.assignUsers(taskId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CardDetailActivity.this, "✅ Task assigned to you", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CardDetailActivity.this, "Failed to assign task", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(CardDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
            android.util.Log.d("CardDetailActivity", "🔄 Labels updated callback triggered, reloading labels for task: " + taskId);
            // Reload task labels from backend to ensure sync
            if (labelViewModel != null && taskId != null) {
                labelViewModel.loadTaskLabels(taskId);
            } else {
                android.util.Log.e("CardDetailActivity", "❌ Cannot reload labels - labelViewModel or taskId is null");
            }
        });
        
        dialog.show(getSupportFragmentManager(), "LabelSelectionBottomSheet");
    }

    private void displaySelectedLabels() {
        android.util.Log.d("CardDetailActivity", "🎨 displaySelectedLabels called with " + selectedLabels.size() + " labels");
        layoutSelectedLabels.removeAllViews();

        if (selectedLabels.isEmpty()) {
            android.util.Log.d("CardDetailActivity", "  ℹ️ No labels, showing empty state");
            scrollViewLabels.setVisibility(View.GONE);
            tvNoLabels.setVisibility(View.VISIBLE);
        } else {
            android.util.Log.d("CardDetailActivity", "  ✅ Showing " + selectedLabels.size() + " labels");
            scrollViewLabels.setVisibility(View.VISIBLE);
            tvNoLabels.setVisibility(View.GONE);

            for (Label label : selectedLabels) {
                android.util.Log.d("CardDetailActivity", "    - Adding label: " + label.getName() + " (" + label.getColor() + ")");
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
    
    private void setupCalendarSyncUI() {
        // Initialize Google Auth API Service
        googleAuthApiService = ApiClient.get(App.authManager).create(GoogleAuthApiService.class);
        
        // Check Google Calendar connection status
        checkGoogleCalendarConnection();
        
        // Toggle calendar sync
        switchCalendarSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // ✅ Skip if we're populating UI from loaded task data
            if (isPopulatingCalendarUI) {
                android.util.Log.d(TAG, "⏭️ Skipping listener callback - populating UI");
                return;
            }
            
            android.util.Log.d(TAG, "👆 User toggled calendar sync: " + isChecked);
            isCalendarSyncEnabled = isChecked;
            
            if (isChecked && !isGoogleCalendarConnected()) {
                showConnectGoogleCalendarDialog();
            } else if (isChecked) {
                // ✅ AUTO-SAVE: Sync to calendar when enabled
                if (isEditMode && taskId != null) {
                    autoSaveCalendarSync();
                }
            } else {
                // ✅ AUTO-SAVE: Unsync from calendar when disabled
                if (isEditMode && taskId != null) {
                    autoUnsyncCalendar();
                }
            }
        });
        
        // View in calendar button
        btnViewInCalendar.setOnClickListener(v -> {
            if (isEditMode && taskId != null) {
                // For now, just show a message since we need the calendarEventId from backend
                Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
                // TODO: Implement when backend provides calendarEventId
                // openGoogleCalendarEvent(calendarEventId);
            }
        });
    }
    
    private void checkGoogleCalendarConnection() {
        googleAuthApiService.getIntegrationStatus().enqueue(new retrofit2.Callback<GoogleCalendarStatusResponse>() {
            @Override
            public void onResponse(retrofit2.Call<GoogleCalendarStatusResponse> call, 
                                 retrofit2.Response<GoogleCalendarStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isGoogleCalendarConnected = response.body().isConnected(); // ✅ Store state
                    updateCalendarSyncUI(isGoogleCalendarConnected);
                } else {
                    // Default to not connected
                    isGoogleCalendarConnected = false;
                    updateCalendarSyncUI(false);
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<GoogleCalendarStatusResponse> call, Throwable t) {
                android.util.Log.e(TAG, "Failed to check calendar connection", t);
                isGoogleCalendarConnected = false;
                updateCalendarSyncUI(false);
            }
        });
    }
    
    private boolean isGoogleCalendarConnected() {
        // ✅ Return stored connection state instead of UI state
        return isGoogleCalendarConnected;
    }
    
    private void updateCalendarSyncUI(boolean connected) {
        if (!connected) {
            switchCalendarSync.setEnabled(false);
            switchCalendarSync.setChecked(false);
            tvCalendarEventInfo.setText("⚠️ Chưa kết nối Google Calendar. Nhấn vào switch để kết nối.");
        } else {
            switchCalendarSync.setEnabled(true);
        }
    }
    
    /**
     * ✅ NEW: Populate calendar sync UI from loaded task data
     */
    private void populateCalendarSyncUI(Task task) {
        // ⚠️ CRITICAL: Set flag to prevent listener from triggering during UI population
        isPopulatingCalendarUI = true;
        
        // Check if task has due date - REQUIRED for calendar sync
        // ✅ Don't set etDueDate here - already populated in main observer
        boolean hasDueDate = (task.getDueAt() != null);
        
        // Only enable calendar sync if Google Calendar connected AND task has due date
        boolean canEnableSync = isGoogleCalendarConnected() && hasDueDate;
        
        if (!canEnableSync) {
            switchCalendarSync.setEnabled(false);
            switchCalendarSync.setChecked(false);
            
            if (!hasDueDate) {
                tvCalendarEventInfo.setText("⚠️ Cần thêm due date để sync với Calendar");
            } else {
                tvCalendarEventInfo.setText("⚠️ Chưa kết nối Google Calendar");
            }
            
            // Clear flag
            isPopulatingCalendarUI = false;
            return;
        }
        
        // Task has due date and calendar connected - populate sync state
        switchCalendarSync.setEnabled(true);
        
        if (task.isCalendarSyncEnabled()) {
            switchCalendarSync.setChecked(true);
            isCalendarSyncEnabled = true;
            
            // Show event info if synced
            if (task.getCalendarEventId() != null && task.getCalendarSyncedAt() != null) {
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
                String syncTime = displayFormat.format(task.getCalendarSyncedAt());
                tvCalendarEventInfo.setText("✅ Đã đồng bộ lúc: " + syncTime);
                btnViewInCalendar.setVisibility(View.VISIBLE);
            } else {
                tvCalendarEventInfo.setText("");
            }
        } else {
            switchCalendarSync.setChecked(false);
            isCalendarSyncEnabled = false;
            
        }
        
        // ✅ Clear flag - listener can now respond to user interactions
        isPopulatingCalendarUI = false;
    }
    
    private void showConnectGoogleCalendarDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Kết nối Google Calendar")
            .setMessage("Để sử dụng tính năng đồng bộ lịch, bạn cần kết nối với Google Calendar.")
            .setPositiveButton("Kết nối ngay", (dialog, which) -> {
                startGoogleCalendarAuth();
            })
            .setNegativeButton("Để sau", (dialog, which) -> {
                switchCalendarSync.setChecked(false);
            })
            .show();
    }
    
    private void startGoogleCalendarAuth() {
        googleAuthApiService.getAuthUrl().enqueue(new retrofit2.Callback<AuthUrlResponse>() {
            @Override
            public void onResponse(retrofit2.Call<AuthUrlResponse> call, 
                                 retrofit2.Response<AuthUrlResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String authUrl = response.body().getAuthUrl();
                    // Open browser with auth URL
                    Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(authUrl));
                    startActivity(intent);
                    Toast.makeText(CardDetailActivity.this, 
                        "Vui lòng hoàn tất đăng nhập trên trình duyệt", 
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(CardDetailActivity.this, 
                        "Không thể lấy URL đăng nhập", 
                        Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<AuthUrlResponse> call, Throwable t) {
                android.util.Log.e(TAG, "Failed to get auth URL", t);
                Toast.makeText(CardDetailActivity.this, 
                    "Lỗi: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * AUTO-SAVE: Sync task to Google Calendar when user enables calendar sync
     */
    private void autoSaveCalendarSync() {
        android.util.Log.d(TAG, "🔄 Auto-saving calendar sync...");
        
        // Get current task data
        String title = etTaskTitle.getText().toString().trim();
        String dueDateStr = etDueDate.getText().toString().trim();
        
        if (TextUtils.isEmpty(dueDateStr)) {
            Toast.makeText(this, "⚠️ Vui lòng chọn ngày hạn chót để đồng bộ với Calendar", Toast.LENGTH_SHORT).show();
            // Revert switch
            isPopulatingCalendarUI = true;
            switchCalendarSync.setChecked(false);
            isPopulatingCalendarUI = false;
            return;
        }
        
        // Parse due date
        Date dueAt = null;
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
        try {
            String dateTimeStr = dueDateStr.contains(" ") ? dueDateStr : dueDateStr + " 12:00";
            dueAt = displayFormat.parse(dateTimeStr);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error parsing due date: " + dueDateStr, e);
            Toast.makeText(this, "❌ Lỗi định dạng ngày", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Call API to sync
        TaskCalendarSyncRequest request = new TaskCalendarSyncRequest(
            true,  // enabled
            30,    // default 30 minutes reminder
            title,
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(dueAt)
        );
        
        taskApiService.updateCalendarSync(taskId, request).enqueue(new retrofit2.Callback<TaskDTO>() {
            @Override
            public void onResponse(retrofit2.Call<TaskDTO> call, retrofit2.Response<TaskDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d(TAG, "✅ Calendar sync saved successfully");
                    Toast.makeText(CardDetailActivity.this, "✅ Đã đồng bộ với Google Calendar", Toast.LENGTH_SHORT).show();
                } else {
                    android.util.Log.e(TAG, "❌ Failed to sync calendar: " + response.code());
                    Toast.makeText(CardDetailActivity.this, "❌ Lỗi đồng bộ Calendar", Toast.LENGTH_SHORT).show();
                    
                    // Revert switch
                    isPopulatingCalendarUI = true;
                    switchCalendarSync.setChecked(false);
                    isPopulatingCalendarUI = false;
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<TaskDTO> call, Throwable t) {
                android.util.Log.e(TAG, "❌ Network error syncing calendar", t);
                Toast.makeText(CardDetailActivity.this, "❌ Lỗi mạng", Toast.LENGTH_SHORT).show();
                
                // Revert switch
                isPopulatingCalendarUI = true;
                switchCalendarSync.setChecked(false);
                isPopulatingCalendarUI = false;
            }
        });
    }
    
    /**
     * AUTO-SAVE: Unsync task from Google Calendar when user disables calendar sync
     */
    private void autoUnsyncCalendar() {
        android.util.Log.d(TAG, "🔄 Auto-unsyncing from calendar...");
        
        taskApiService.unsyncCalendar(taskId).enqueue(new retrofit2.Callback<TaskDTO>() {
            @Override
            public void onResponse(retrofit2.Call<TaskDTO> call, retrofit2.Response<TaskDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d(TAG, "✅ Calendar unsync successful");
                    Toast.makeText(CardDetailActivity.this, "✅ Đã ngắt đồng bộ với Calendar", Toast.LENGTH_SHORT).show();
                } else {
                    android.util.Log.e(TAG, "❌ Failed to unsync calendar: " + response.code());
                    Toast.makeText(CardDetailActivity.this, "❌ Lỗi ngắt đồng bộ", Toast.LENGTH_SHORT).show();
                    
                    // Revert switch
                    isPopulatingCalendarUI = true;
                    switchCalendarSync.setChecked(true);
                    isPopulatingCalendarUI = false;
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<TaskDTO> call, Throwable t) {
                android.util.Log.e(TAG, "❌ Network error unsyncing calendar", t);
                Toast.makeText(CardDetailActivity.this, "❌ Lỗi mạng", Toast.LENGTH_SHORT).show();
                
                // Revert switch
                isPopulatingCalendarUI = true;
                switchCalendarSync.setChecked(true);
                isPopulatingCalendarUI = false;
            }
        });
    }
    
    private void openGoogleCalendarEvent(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Chưa có sự kiện trong Calendar", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse("content://com.android.calendar/events/" + eventId));
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            // Fallback: web browser
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse("https://calendar.google.com/calendar/event?eid=" + eventId));
            startActivity(intent);
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
                    // After date selected, show time picker
                    showTimePickerDialog(isStartDate, selectedYear, selectedMonth, selectedDay);
                },
                year, month, day
        );

        datePickerDialog.show();
    }
    
    /**
     * Show time picker after date selection
     */
    private void showTimePickerDialog(boolean isStartDate, int year, int month, int day) {
        int defaultHour = isStartDate ? 9 : 17; // Default: 9 AM for start, 5 PM for due date
        int defaultMinute = 0;
        
        android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    // Format: dd/MM/yyyy HH:mm
                    String date = String.format(Locale.getDefault(), 
                            "%02d/%02d/%d %02d:%02d", day, month + 1, year, selectedHour, selectedMinute);
                    if (isStartDate) {
                        etStartDate.setText(date);
                        // ✅ Mark unsaved changes instead of auto-save
                        markUnsavedChanges();
                    } else {
                        etDueDate.setText(date);
                        // ✅ Mark unsaved changes instead of auto-save
                        markUnsavedChanges();
                        // ✅ Check calendar sync availability when due date changes
                        updateCalendarSyncAvailability();
                    }
                },
                defaultHour, defaultMinute, true // 24-hour format
        );
        
        timePickerDialog.show();
    }
    
    /**
     * Update task date on server (auto-save)
     */
    private void updateTaskDate(boolean isStartDate, String dateStr) {
        if (taskId == null) return;
        
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
        try {
            Date date = displayFormat.parse(dateStr);
            
            TaskApiService taskApiService = ApiClient.get(App.authManager).create(TaskApiService.class);
            Map<String, Object> updates = new HashMap<>();
            updates.put(isStartDate ? "startAt" : "dueAt", date);
            
            taskApiService.updateTask(taskId, updates).enqueue(new Callback<com.example.tralalero.data.remote.dto.task.TaskDTO>() {
                @Override
                public void onResponse(@NonNull Call<com.example.tralalero.data.remote.dto.task.TaskDTO> call, @NonNull Response<com.example.tralalero.data.remote.dto.task.TaskDTO> response) {
                    if (response.isSuccessful()) {
                        android.util.Log.d(TAG, (isStartDate ? "Start" : "Due") + " date updated");
                    }
                }
                
                @Override
                public void onFailure(@NonNull Call<com.example.tralalero.data.remote.dto.task.TaskDTO> call, @NonNull Throwable t) {
                    Toast.makeText(CardDetailActivity.this, "Failed to update date", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error parsing date: " + dateStr, e);
        }
    }
    
    /**
     * ✅ NEW: Update calendar sync toggle based on due date availability
     */
    private void updateCalendarSyncAvailability() {
        String dueDate = etDueDate.getText().toString().trim();
        boolean hasDueDate = !TextUtils.isEmpty(dueDate);
        boolean isCalendarConnected = isGoogleCalendarConnected();
        
        if (!hasDueDate) {
            // No due date - disable calendar sync
            switchCalendarSync.setEnabled(false);
            switchCalendarSync.setChecked(false);
            isCalendarSyncEnabled = false;
            tvCalendarEventInfo.setText("⚠️ Cần thêm due date để sync với Calendar");
        } else if (!isCalendarConnected) {
            // Has due date but not connected
            switchCalendarSync.setEnabled(false);
            switchCalendarSync.setChecked(false);
            isCalendarSyncEnabled = false;
            tvCalendarEventInfo.setText("⚠️ Chưa kết nối Google Calendar. Nhấn vào switch để kết nối.");
        } else {
            // Has due date AND connected - enable toggle
            switchCalendarSync.setEnabled(true);
        }
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
                android.util.Log.e("CardDetail", "Failed to get signed URL from backend: " + error);
                
                // ✅ FALLBACK: Use URL from DB if backend API fails
                String dbUrl = attachment.getUrl();
                if (dbUrl != null && !dbUrl.isEmpty() && 
                    (dbUrl.startsWith("http://") || dbUrl.startsWith("https://"))) {
                    android.util.Log.d("CardDetail", "Using fallback URL from DB: " + dbUrl);
                    callback.accept(dbUrl);
                } else {
                    android.util.Log.e("CardDetail", "No valid URL available (backend failed, DB URL empty)");
                    Toast.makeText(CardDetailActivity.this, "Failed to fetch file: " + error, Toast.LENGTH_SHORT).show();
                    callback.accept(null);
                }
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
        
        // Check if it's an image file
        String mimeType = attachment.getMimeType();
        boolean isImage = mimeType != null && mimeType.startsWith("image/");
        
        // Get signed URL for viewing
        getAttachmentViewUrl(attachment, (signedUrl) -> {
            if (signedUrl != null && !signedUrl.isEmpty()) {
                if (isImage) {
                    // Show image preview dialog
                    showImagePreview(signedUrl, fileName);
                } else {
                    // Open with system intent for non-images
                    openWithIntent(signedUrl, fileName);
                }
            } else {
                Toast.makeText(this, "Cannot open file - backend integration needed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Show image preview dialog
     */
    private void showImagePreview(String imageUrl, String fileName) {
        com.example.tralalero.feature.task.attachments.ImagePreviewDialog dialog = 
            new com.example.tralalero.feature.task.attachments.ImagePreviewDialog(this, imageUrl, fileName);
        
        // Set download listener
        dialog.setOnDownloadClickListener(() -> {
            performDownload(imageUrl, fileName);
        });
        
        dialog.show();
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
        
        // Show progress UI
        if (layoutCardUploadProgress != null) {
            layoutCardUploadProgress.setVisibility(View.VISIBLE);
            progressBarCardUpload.setProgress(0);
            tvCardUploadProgress.setText("Preparing upload...");
        }
        
        attachmentUploader.uploadFile(taskId, uri, new AttachmentUploader.UploadCallback() {
            @Override
            public void onProgress(int percent) {
                android.util.Log.d("CardDetail", "Upload progress: " + percent + "%");
                runOnUiThread(() -> {
                    if (progressBarCardUpload != null) {
                        progressBarCardUpload.setProgress(percent);
                        tvCardUploadProgress.setText("Uploading... " + percent + "%");
                    }
                });
            }

            @Override
            public void onSuccess(com.example.tralalero.data.remote.dto.task.AttachmentDTO attachmentDTO) {
                android.util.Log.d("CardDetail", "Upload success! File: " + attachmentDTO.fileName);
                runOnUiThread(() -> {
                    // Hide progress UI
                    if (layoutCardUploadProgress != null) {
                        layoutCardUploadProgress.setVisibility(View.GONE);
                        progressBarCardUpload.setProgress(0);
                    }
                    
                    // ✅ Just reload attachments from server (which has full data including URL)
                    // Don't manually add attachment as attachmentDTO from upload doesn't have URL
                    loadTaskAttachments(); // Reload to refresh UI with complete data from backend
                    
                    Toast.makeText(CardDetailActivity.this, 
                        "File uploaded successfully!", 
                        Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("CardDetail", "Upload error: " + error);
                runOnUiThread(() -> {
                    // Hide progress UI
                    if (layoutCardUploadProgress != null) {
                        layoutCardUploadProgress.setVisibility(View.GONE);
                        progressBarCardUpload.setProgress(0);
                    }
                    
                    Toast.makeText(CardDetailActivity.this, 
                        "Upload failed: " + error, 
                        Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    /**
     * Show Assignee Selection Dialog
     */
    private void showAssigneeSelectionDialog() {
        if (projectId == null || projectId.isEmpty()) {
            Toast.makeText(this, "Cannot assign task: Invalid project", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (taskId == null || taskId.isEmpty()) {
            Toast.makeText(this, "Task not yet created. Please save the task first.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Open new AssignMemberBottomSheet with multi-select support
        AssignMemberBottomSheet assignSheet = AssignMemberBottomSheet.newInstance(projectId, taskId);
        assignSheet.setOnAssigneesChangedListener(new AssignMemberBottomSheet.OnAssigneesChangedListener() {
            @Override
            public void onAssigneesChanged() {
                // Reload task details to show updated assignees
                loadTaskDetails();
            }
        });
        
        assignSheet.show(getSupportFragmentManager(), "assign_member");
    }
    
    /**
     * Show Priority Selection Dialog
     */
    private void showPrioritySelectionDialog() {
        String[] priorities = {"Low", "Medium", "High"};
        int currentIndex = currentPriority == Task.TaskPriority.LOW ? 0 : 
                          currentPriority == Task.TaskPriority.HIGH ? 2 : 1;
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Priority")
            .setSingleChoiceItems(priorities, currentIndex, (dialog, which) -> {
                switch (which) {
                    case 0:
                        currentPriority = Task.TaskPriority.LOW;
                        break;
                    case 1:
                        currentPriority = Task.TaskPriority.MEDIUM;
                        break;
                    case 2:
                        currentPriority = Task.TaskPriority.HIGH;
                        break;
                }
                updatePriorityUIDetails();
                
                // ✅ Mark unsaved changes instead of auto-save
                markUnsavedChanges();
                
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    /**
     * Update Priority UI in Details section
     */
    private void updatePriorityUIDetails() {
        switch (currentPriority) {
            case LOW:
                tvPriority.setText("Low");
                viewPriorityIndicator.setBackgroundColor(Color.parseColor("#4CAF50"));
                break;
            case MEDIUM:
                tvPriority.setText("Medium");
                viewPriorityIndicator.setBackgroundColor(Color.parseColor("#FF9800"));
                break;
            case HIGH:
                tvPriority.setText("High");
                viewPriorityIndicator.setBackgroundColor(Color.parseColor("#F44336"));
                break;
        }
    }
    
    /**
     * Update task priority on server
     */
    private void updateTaskPriority() {
        if (taskId == null) return;
        
        TaskApiService taskApiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        Map<String, Object> updates = new HashMap<>();
        updates.put("priority", currentPriority.name());
        
        taskApiService.updateTask(taskId, updates).enqueue(new Callback<com.example.tralalero.data.remote.dto.task.TaskDTO>() {
            @Override
            public void onResponse(@NonNull Call<com.example.tralalero.data.remote.dto.task.TaskDTO> call, @NonNull Response<com.example.tralalero.data.remote.dto.task.TaskDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CardDetailActivity.this, "Priority updated", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<com.example.tralalero.data.remote.dto.task.TaskDTO> call, @NonNull Throwable t) {
                Toast.makeText(CardDetailActivity.this, "Failed to update priority", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Show Board Selection Dialog
     */
    private void showBoardSelectionDialog() {
        if (!isEditMode || taskId == null || taskId.isEmpty()) {
            Toast.makeText(this, "Please save the task first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // TODO: Show actual board selection dialog with board list from project
        // For now, just show status selection (To Do/In Progress/Done boards)
        BoardSelectionBottomSheet bottomSheet = BoardSelectionBottomSheet.newInstance(currentStatus);
        bottomSheet.setOnBoardSelectedListener(newStatus -> {
            currentStatus = newStatus;
            // Map status to board name
            switch (newStatus) {
                case TO_DO:
                    boardName = "To Do";
                    break;
                case IN_PROGRESS:
                    boardName = "In Progress";
                    break;
                case DONE:
                    boardName = "Done";
                    break;
            }
            updateBoardNameUI();
            tvBoardName.setText("In " + boardName);
            // ✅ Mark unsaved changes instead of auto-save
            markUnsavedChanges();
        });
        bottomSheet.show(getSupportFragmentManager(), "board_selection");
    }
    
    /**
     * Update Board Name UI (display in btnBoardStatus)
     */
    private void updateBoardNameUI() {
        if (boardName == null || boardName.isEmpty()) {
            boardName = "To Do"; // Default
        }
        
        int boardColor = 0;
        
        // Set color based on board name
        switch (boardName) {
            case "To Do":
                boardColor = Color.parseColor("#9E9E9E");
                break;
            case "In Progress":
                boardColor = Color.parseColor("#2196F3");
                break;
            case "Done":
                boardColor = Color.parseColor("#4CAF50");
                break;
            default:
                boardColor = Color.parseColor("#9E9E9E");
                break;
        }
        
        btnBoardStatus.setText(boardName);
        btnBoardStatus.setTextColor(boardColor);
        btnBoardStatus.setStrokeColor(android.content.res.ColorStateList.valueOf(boardColor));
        btnBoardStatus.setIconTint(android.content.res.ColorStateList.valueOf(boardColor));
    }
    
    /**
     * Update task status on server
     */
    private void updateTaskStatus(Task.TaskStatus newStatus) {
        if (taskId == null) return;
        
        TaskApiService taskApiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus.name());
        
        taskApiService.updateTask(taskId, updates).enqueue(new Callback<com.example.tralalero.data.remote.dto.task.TaskDTO>() {
            @Override
            public void onResponse(@NonNull Call<com.example.tralalero.data.remote.dto.task.TaskDTO> call, @NonNull Response<com.example.tralalero.data.remote.dto.task.TaskDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CardDetailActivity.this, "Moved to " + newStatus.name().replace("_", " "), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CardDetailActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<com.example.tralalero.data.remote.dto.task.TaskDTO> call, @NonNull Throwable t) {
                Toast.makeText(CardDetailActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Mark that there are unsaved changes and show save button
     */
    private void markUnsavedChanges() {
        if (!hasUnsavedChanges) {
            hasUnsavedChanges = true;
            btnSaveChanges.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Update assignees RecyclerView visibility
     */
    private void updateAssigneesVisibility() {
        if (assigneeAdapter.getItemCount() > 0) {
            rvAssignees.setVisibility(View.VISIBLE);
            tvNoAssignees.setVisibility(View.GONE);
        } else {
            rvAssignees.setVisibility(View.GONE);
            tvNoAssignees.setVisibility(View.VISIBLE);
        }
    }
}
