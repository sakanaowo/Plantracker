package com.example.tralalero.feature.home.ui.Home.project;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.adapter.AttachmentAdapter;
import com.example.tralalero.adapter.CommentAdapter;
import com.example.tralalero.App.App;
import com.example.tralalero.data.remote.api.ProjectApiService;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.remote.dto.project.ProjectMemberDTO;
import com.example.tralalero.data.remote.dto.task.TaskDTO;
import com.example.tralalero.data.remote.mapper.ProjectMemberMapper;
import com.example.tralalero.domain.model.ProjectMember;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.feature.home.ui.Home.task.CalendarSyncDialog;
import com.example.tralalero.feature.home.ui.Home.task.TaskCalendarSyncViewModel;
import com.example.tralalero.network.ApiClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class TaskDetailBottomSheet extends BottomSheetDialogFragment {
    private static final String ARG_TASK_OBJECT = "task_object";
    private static final String ARG_IS_EDIT_MODE = "is_edit_mode";
    
    private Task task;
    private String projectId;
    private String assigneeId;
    private OnActionClickListener listener;
    private ImageView ivClose;
    private EditText etTaskTitle;
    private EditText etDescription;
    private EditText etDueDate;
    private MaterialButton btnMembers;
    private MaterialButton btnAddAttachment;
    private MaterialButton btnAddComment;
    private MaterialButton btnCalendarSync;
    private MaterialButton btnDeleteTask;
    private MaterialButton btnConfirm;
    private MaterialButton btnLowPriority;
    private MaterialButton btnMediumPriority;
    private MaterialButton btnHighPriority;
    private Task.TaskPriority currentPriority = Task.TaskPriority.MEDIUM;
    private boolean isEditMode = false;
    
    // Comments and Attachments (inline)
    private RecyclerView rvComments;
    private RecyclerView rvAttachments;
    private TextView tvNoComments;
    private TextView tvNoAttachments;
    private CommentAdapter commentAdapter;
    private AttachmentAdapter attachmentAdapter;
    
    // Dropdown UI elements
    private android.widget.LinearLayout layoutAttachments;
    private android.widget.LinearLayout layoutDetails;
    private android.widget.LinearLayout layoutDetailsContent;
    private ImageView ivAttachmentDropdown;
    private ImageView ivDetailsDropdown;
    private TextView tvAttachmentCount;
    private ImageView ivAddAttachment;
    private boolean isAttachmentsExpanded = false;
    private boolean isDetailsExpanded = true;
    
    private Date selectedDueDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    
    private TaskCalendarSyncViewModel calendarSyncViewModel;
    private TaskDTO currentTaskDTO; // For calendar sync
    private TaskApiService taskApiService; // For fetching task details
    private boolean isTaskDetailLoaded = false; // Track if task details from API are loaded
    
    public interface OnActionClickListener {
        void onAssignTask(Task task);
        void onMoveTask(Task task);
        void onAddComment(Task task);
        void onDeleteTask(Task task);
        void onUpdateDueDate(Task task, Date dueDate);
        void onUpdateTask(Task task, String newTitle, String newDescription); // ✅ NEW: Save title & description
    }
    public static TaskDetailBottomSheet newInstance(Task task) {
        TaskDetailBottomSheet fragment = new TaskDetailBottomSheet();
        Bundle args = new Bundle();
        
        // Check if this is edit mode (task has ID) or add mode (new task)
        boolean isEditMode = task.getId() != null && !task.getId().isEmpty();
        args.putBoolean(ARG_IS_EDIT_MODE, isEditMode);
        
        // Store serializable Task object to preserve ALL fields
        args.putSerializable(ARG_TASK_OBJECT, task);
        
        fragment.setArguments(args);
        return fragment;
    }
    public void setOnActionClickListener(OnActionClickListener listener) {
        this.listener = listener;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.card_detail, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initViewModel();
        loadTaskData();
        setupListeners();
        observeViewModel();
    }
    private void initViews(View view) {
        ivClose = view.findViewById(R.id.ivClose);
        etTaskTitle = view.findViewById(R.id.etTaskTitle);
        etDescription = view.findViewById(R.id.etDescription);
        etDueDate = view.findViewById(R.id.etDueDate);
        btnMembers = view.findViewById(R.id.btnMembers);
        btnDeleteTask = view.findViewById(R.id.btnDeleteTask);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        
        // Priority Buttons
        btnLowPriority = view.findViewById(R.id.btnLowPriority);
        btnMediumPriority = view.findViewById(R.id.btnMediumPriority);
        btnHighPriority = view.findViewById(R.id.btnHighPriority);
        
        // Dropdown elements
        layoutAttachments = view.findViewById(R.id.layoutAttachments);
        layoutDetails = view.findViewById(R.id.layoutDetails);
        layoutDetailsContent = view.findViewById(R.id.layoutDetailsContent);
        ivAttachmentDropdown = view.findViewById(R.id.ivAttachmentDropdown);
        ivDetailsDropdown = view.findViewById(R.id.ivDetailsDropdown);
        tvAttachmentCount = view.findViewById(R.id.tvAttachmentCount);
        ivAddAttachment = view.findViewById(R.id.ivAddAttachment);
        rvAttachments = view.findViewById(R.id.rvAttachments);
        tvNoAttachments = view.findViewById(R.id.tvNoAttachments);
        
        // Remove default disabled state - will be set in loadTaskData with click listeners
        
        // Enable date field for editing
        etDueDate.setEnabled(true);
        etDueDate.setFocusable(false);
        etDueDate.setClickable(true);
    }
    
    private void loadTaskData() {
        Bundle args = getArguments();
        if (args == null) return;
        
        // Get edit mode flag
        isEditMode = args.getBoolean(ARG_IS_EDIT_MODE, false);
        
        // Load FULL Task object from Bundle (preserves ALL fields)
        task = (Task) args.getSerializable(ARG_TASK_OBJECT);
        if (task == null) {
            android.util.Log.e("TaskDetailBottomSheet", "❌ Task object is NULL from Bundle!");
            return;
        }
        
        android.util.Log.d("TaskDetailBottomSheet", "✅ Loaded task: " + task.getTitle());
        android.util.Log.d("TaskDetailBottomSheet", "  - ID: " + task.getId());
        android.util.Log.d("TaskDetailBottomSheet", "  - ProjectID: " + task.getProjectId());
        android.util.Log.d("TaskDetailBottomSheet", "  - BoardID: " + task.getBoardId());
        android.util.Log.d("TaskDetailBottomSheet", "  - Status: " + task.getStatus());
        android.util.Log.d("TaskDetailBottomSheet", "  - Priority: " + task.getPriority());
        android.util.Log.d("TaskDetailBottomSheet", "  - Description: " + task.getDescription());
        
        // Extract commonly used fields
        projectId = task.getProjectId();
        assigneeId = task.getAssigneeId();
        currentPriority = task.getPriority() != null ? task.getPriority() : Task.TaskPriority.MEDIUM;
        selectedDueDate = task.getDueAt();
        
        // Update button text based on mode
        if (btnConfirm != null) {
            if (isEditMode) {
                btnConfirm.setText("Save Changes");
            } else {
                btnConfirm.setText("Add Card");
            }
        }
        
        // Display task data in UI
        if (etTaskTitle != null) {
            etTaskTitle.setText(task.getTitle() != null ? task.getTitle() : "No Title");
            // Enable edit on click
            etTaskTitle.setEnabled(true);
            etTaskTitle.setOnClickListener(v -> {
                etTaskTitle.setEnabled(true);
                etTaskTitle.setFocusableInTouchMode(true);
                etTaskTitle.requestFocus();
                etTaskTitle.setSelection(etTaskTitle.getText().length());
            });
        }
        if (etDescription != null) {
            etDescription.setText(task.getDescription() != null ? task.getDescription() : "No description");
            // Enable edit on click
            etDescription.setEnabled(true);
            etDescription.setOnClickListener(v -> {
                etDescription.setEnabled(true);
                etDescription.setFocusableInTouchMode(true);
                etDescription.requestFocus();
                etDescription.setSelection(etDescription.getText().length());
            });
        }
        
        // Display due date
        if (etDueDate != null) {
            if (selectedDueDate != null) {
                etDueDate.setText(dateFormat.format(selectedDueDate));
            } else {
                etDueDate.setHint("Select due date");
            }
        }
        
        // Update priority UI
        updatePriorityUI();
    }
    
    private void setupListeners() {
        if (ivClose != null) {
            ivClose.setOnClickListener(v -> dismiss());
        }
        
        // Setup due date picker
        if (etDueDate != null) {
            etDueDate.setOnClickListener(v -> showDueDatePicker());
        }
        
        if (btnMembers != null) {
            btnMembers.setOnClickListener(v -> {
                selfAssignTask();
            });
        }
        if (btnAddAttachment != null) {
            btnAddAttachment.setOnClickListener(v -> {
                // Direct file picker action
                Toast.makeText(requireContext(), "File picker would open here", Toast.LENGTH_SHORT).show();
            });
        }
        if (btnAddComment != null) {
            btnAddComment.setOnClickListener(v -> {
                // Focus on comment input
                Toast.makeText(requireContext(), "Focus on comment input", Toast.LENGTH_SHORT).show();
            });
        }
        if (btnCalendarSync != null) {
            btnCalendarSync.setOnClickListener(v -> {
                openCalendarSyncDialog();
            });
        }
        if (btnDeleteTask != null) {
            btnDeleteTask.setOnClickListener(v -> {
                if (listener != null && task != null) {
                    listener.onDeleteTask(task);
                }
                dismiss();
            });
        }
        
        // Priority buttons
        if (btnLowPriority != null) {
            btnLowPriority.setOnClickListener(v -> {
                currentPriority = Task.TaskPriority.LOW;
                updatePriorityUI();
            });
        }
        
        if (btnMediumPriority != null) {
            btnMediumPriority.setOnClickListener(v -> {
                currentPriority = Task.TaskPriority.MEDIUM;
                updatePriorityUI();
            });
        }
        
        if (btnHighPriority != null) {
            btnHighPriority.setOnClickListener(v -> {
                currentPriority = Task.TaskPriority.HIGH;
                updatePriorityUI();
            });
        }
        
        // ✅ NEW: Save Changes button
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                if (listener != null && task != null) {
                    String newTitle = etTaskTitle != null ? etTaskTitle.getText().toString().trim() : task.getTitle();
                    String newDescription = etDescription != null ? etDescription.getText().toString().trim() : "";
                    
                    if (newTitle.isEmpty()) {
                        Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    listener.onUpdateTask(task, newTitle, newDescription);
                    Toast.makeText(requireContext(), "Saving changes...", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(requireContext(), "Cannot save: task data missing", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // Dropdown toggles
        if (layoutAttachments != null) {
            layoutAttachments.setOnClickListener(v -> toggleAttachmentsDropdown());
        }
        if (layoutDetails != null) {
            layoutDetails.setOnClickListener(v -> toggleDetailsDropdown());
        }
    }
    
    private void toggleAttachmentsDropdown() {
        isAttachmentsExpanded = !isAttachmentsExpanded;
        if (rvAttachments != null) {
            rvAttachments.setVisibility(isAttachmentsExpanded ? View.VISIBLE : View.GONE);
        }
        if (tvNoAttachments != null && attachmentAdapter != null) {
            tvNoAttachments.setVisibility(isAttachmentsExpanded && attachmentAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
        if (ivAttachmentDropdown != null) {
            ivAttachmentDropdown.setRotation(isAttachmentsExpanded ? 180 : 0);
        }
    }
    
    private void toggleDetailsDropdown() {
        isDetailsExpanded = !isDetailsExpanded;
        if (layoutDetailsContent != null) {
            layoutDetailsContent.setVisibility(isDetailsExpanded ? View.VISIBLE : View.GONE);
        }
        if (ivDetailsDropdown != null) {
            ivDetailsDropdown.setRotation(isDetailsExpanded ? 180 : 0);
        }
    }
    
    private void showDueDatePicker() {
        Calendar calendar = Calendar.getInstance();
        
        if (selectedDueDate != null) {
            calendar.setTime(selectedDueDate);
        }
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, selectedYear, selectedMonth, selectedDay) -> {
                Calendar newDate = Calendar.getInstance();
                newDate.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
                newDate.set(Calendar.MILLISECOND, 0);
                
                selectedDueDate = newDate.getTime();
                etDueDate.setText(dateFormat.format(selectedDueDate));
                
                if (listener != null && task != null) {
                    listener.onUpdateDueDate(task, selectedDueDate);
                }
                
                Toast.makeText(requireContext(), 
                    "Due date: " + dateFormat.format(selectedDueDate), 
                    Toast.LENGTH_SHORT).show();
            },
            year, month, day
        );
        
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }
    
    private void updatePriorityUI() {
        if (btnLowPriority == null || btnMediumPriority == null || btnHighPriority == null) {
            return;
        }
        
        // Reset all buttons to transparent background
        btnLowPriority.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btnMediumPriority.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btnHighPriority.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        
        // Set selected button background
        switch (currentPriority) {
            case LOW:
                btnLowPriority.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"));
                break;
            case MEDIUM:
                btnMediumPriority.setBackgroundColor(android.graphics.Color.parseColor("#FFF3E0"));
                break;
            case HIGH:
                btnHighPriority.setBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"));
                break;
        }
    }
    
    /**
     * Self-assign the task to current user (quick task - no role check needed)
     */
    private void selfAssignTask() {
        if (task == null || task.getId() == null) {
            Toast.makeText(requireContext(), "Cannot assign task: Invalid task", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get internal user ID from token manager
        String internalUserId = App.tokenManager.getInternalUserId();
        if (internalUserId == null || internalUserId.isEmpty()) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        
        TaskApiService taskApiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        
        // Prepare request body with internal user IDs array
        Map<String, List<String>> body = new HashMap<>();
        body.put("userIds", Arrays.asList(internalUserId));
        
        taskApiService.assignUsers(task.getId(), body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Task assigned to you", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to assign task", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Initialize ViewModel for Calendar Sync
     */
    private void initViewModel() {
        calendarSyncViewModel = new ViewModelProvider(this).get(TaskCalendarSyncViewModel.class);
        
        // Initialize API service
        taskApiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        
        // Fetch current task details if task has ID (edit mode)
        if (task != null && task.getId() != null && !task.getId().isEmpty()) {
            fetchTaskDetails(task.getId());
        }
    }
    
    /**
     * Observe ViewModel LiveData
     */
    private void observeViewModel() {
        // Observe sync update success
        calendarSyncViewModel.getSyncUpdatedTask().observe(getViewLifecycleOwner(), updatedTask -> {
            if (updatedTask != null) {
                currentTaskDTO = updatedTask;
                isTaskDetailLoaded = true;
                
                Log.d("TaskDetailBottomSheet", "Sync update successful. New state: " +
                    "enabled=" + updatedTask.getCalendarReminderEnabled() +
                    ", eventId=" + updatedTask.getCalendarEventId() +
                    ", lastSynced=" + updatedTask.getLastSyncedAt());
                
                Toast.makeText(requireContext(), 
                    updatedTask.getCalendarReminderEnabled() 
                        ? "✅ Calendar sync enabled" 
                        : "❌ Calendar sync disabled", 
                    Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observe sync errors
        calendarSyncViewModel.getSyncError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), "Sync error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Open Calendar Sync Dialog
     */
    private void openCalendarSyncDialog() {
        if (task == null || task.getId() == null || task.getId().isEmpty()) {
            Toast.makeText(requireContext(), "Task must be saved before syncing to calendar", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if task has due date
        if (task.getDueAt() == null) {
            Toast.makeText(requireContext(), "Task must have a due date to sync to calendar", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // If task details not loaded yet, show loading and wait
        if (!isTaskDetailLoaded) {
            Toast.makeText(requireContext(), "Loading task details...", Toast.LENGTH_SHORT).show();
            // Fetch again to ensure we have latest data
            fetchTaskDetails(task.getId());
            return;
        }

    }
    private void fetchTaskDetails(String taskId) {
        Log.d("TaskDetailBottomSheet", "Fetching task details for: " + taskId);
        
        taskApiService.getTaskById(taskId).enqueue(new Callback<TaskDTO>() {
            @Override
            public void onResponse(@NonNull Call<TaskDTO> call, @NonNull Response<TaskDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentTaskDTO = response.body();
                    isTaskDetailLoaded = true;
                    
                    Log.d("TaskDetailBottomSheet", "Task fetched successfully. Calendar sync: " + 
                        currentTaskDTO.getCalendarReminderEnabled() + 
                        ", Event ID: " + currentTaskDTO.getCalendarEventId() +
                        ", Reminder time: " + currentTaskDTO.getCalendarReminderTime() +
                        ", Last synced: " + currentTaskDTO.getLastSyncedAt());
                    
                    // ✅ FIX: Populate UI with fresh data from API
                    populateTaskUI(currentTaskDTO);

                } else {
                    Log.e("TaskDetailBottomSheet", "Failed to fetch task: " + response.code());
                    Toast.makeText(requireContext(), "Failed to load task details", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<TaskDTO> call, @NonNull Throwable t) {
                Log.e("TaskDetailBottomSheet", "Error fetching task details", t);
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * ✅ NEW: Populate UI fields with task data from API
     */
    private void populateTaskUI(TaskDTO taskDTO) {
        if (taskDTO == null) return;
        
        // Update description
        if (etDescription != null && taskDTO.getDescription() != null) {
            etDescription.setText(taskDTO.getDescription());
        }
        
        // Update due date - parse from ISO string to Date
        if (etDueDate != null && taskDTO.getDueAt() != null) {
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                selectedDueDate = isoFormat.parse(taskDTO.getDueAt());
                if (selectedDueDate != null) {
                    etDueDate.setText(dateFormat.format(selectedDueDate));
                }
            } catch (Exception e) {
                Log.e("TaskDetailBottomSheet", "Error parsing due date", e);
            }
        }
        
        Log.d("TaskDetailBottomSheet", "✅ UI populated with task data");
    }
}

