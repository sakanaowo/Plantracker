package com.example.tralalero.feature.home.ui.Home.project;
import android.app.DatePickerDialog;
import android.os.Bundle;
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
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.remote.dto.task.TaskDTO;
import com.example.tralalero.domain.model.ProjectMember;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.feature.home.ui.Home.task.CalendarSyncDialog;
import com.example.tralalero.feature.home.ui.Home.task.TaskCalendarSyncViewModel;
import com.example.tralalero.network.ApiClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class TaskDetailBottomSheet extends BottomSheetDialogFragment {
    private static final String ARG_TASK_ID = "task_id";
    private static final String ARG_TASK_TITLE = "task_title";
    private static final String ARG_TASK_DESCRIPTION = "task_description";
    private static final String ARG_TASK_PRIORITY = "task_priority";
    private static final String ARG_TASK_STATUS = "task_status";
    private static final String ARG_TASK_START_DATE = "task_start_date";
    private static final String ARG_TASK_DUE_DATE = "task_due_date";
    private static final String ARG_IS_EDIT_MODE = "is_edit_mode";
    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_ASSIGNEE_ID = "assignee_id";
    
    private Task task;
    private String projectId;
    private String assigneeId;
    private OnActionClickListener listener;
    private ImageView ivClose;
    private RadioButton rbTaskTitle;
    private EditText etDescription;
    private EditText etDateStart;
    private EditText etDueDate;
    private MaterialButton btnMembers;
    private MaterialButton btnAddAttachment;
    private MaterialButton btnAddComment;
    private MaterialButton btnCalendarSync;
    private MaterialButton btnDeleteTask;
    private MaterialButton btnConfirm;
    private boolean isEditMode = false;
    
    // Comments and Attachments (inline)
    private RecyclerView rvComments;
    private RecyclerView rvAttachments;
    private TextView tvNoComments;
    private TextView tvNoAttachments;
    private CommentAdapter commentAdapter;
    private AttachmentAdapter attachmentAdapter;
    
    private Date selectedStartDate;
    private Date selectedDueDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    
    private TaskCalendarSyncViewModel calendarSyncViewModel;
    private TaskDTO currentTaskDTO; // For calendar sync
    
    public interface OnActionClickListener {
        void onAssignTask(Task task);
        void onMoveTask(Task task);
        void onAddComment(Task task);
        void onDeleteTask(Task task);
        void onUpdateStartDate(Task task, Date startDate);
        void onUpdateDueDate(Task task, Date dueDate);
    }
    public static TaskDetailBottomSheet newInstance(Task task) {
        TaskDetailBottomSheet fragment = new TaskDetailBottomSheet();
        Bundle args = new Bundle();
        
        // Check if this is edit mode (task has ID) or add mode (new task)
        boolean isEditMode = task.getId() != null && !task.getId().isEmpty();
        args.putBoolean(ARG_IS_EDIT_MODE, isEditMode);
        
        args.putString(ARG_TASK_ID, task.getId());
        args.putString(ARG_TASK_TITLE, task.getTitle());
        args.putString(ARG_TASK_DESCRIPTION, task.getDescription());
        args.putString(ARG_TASK_PRIORITY, task.getPriority().name());
        args.putString(ARG_TASK_STATUS, task.getStatus().name());
        args.putString(ARG_PROJECT_ID, task.getProjectId());
        args.putString(ARG_ASSIGNEE_ID, task.getAssigneeId());
        
        // Store dates as timestamps
        if (task.getStartAt() != null) {
            args.putLong(ARG_TASK_START_DATE, task.getStartAt().getTime());
        }
        if (task.getDueAt() != null) {
            args.putLong(ARG_TASK_DUE_DATE, task.getDueAt().getTime());
        }
        
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
        etDescription = view.findViewById(R.id.etDescription);
        etDateStart = view.findViewById(R.id.etDateStart);
        etDueDate = view.findViewById(R.id.etDueDate);
        btnMembers = view.findViewById(R.id.btnMembers); 
        btnAddAttachment = view.findViewById(R.id.btnAddAttachment); 
        btnAddComment = view.findViewById(R.id.btnAddComment); 
        btnCalendarSync = view.findViewById(R.id.btnCalendarSync);
        btnDeleteTask = view.findViewById(R.id.btnDeleteTask);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        
        etDescription.setEnabled(false);
        
        // Enable date fields for editing
        etDateStart.setEnabled(true);
        etDateStart.setFocusable(false);
        etDateStart.setClickable(true);
        
        etDueDate.setEnabled(true);
        etDueDate.setFocusable(false);
        etDueDate.setClickable(true);
    }
    
    private void loadTaskData() {
        Bundle args = getArguments();
        if (args == null) return;
        
        // Get edit mode flag
        isEditMode = args.getBoolean(ARG_IS_EDIT_MODE, false);
        
        // Get project ID and assignee ID
        projectId = args.getString(ARG_PROJECT_ID);
        assigneeId = args.getString(ARG_ASSIGNEE_ID);
        
        // Update button text based on mode
        if (btnConfirm != null) {
            if (isEditMode) {
                btnConfirm.setText("Save Changes");
            } else {
                btnConfirm.setText("Add Card");
            }
        }
        
        // Load start date from timestamp
        long startDateTimestamp = args.getLong(ARG_TASK_START_DATE, -1);
        if (startDateTimestamp != -1) {
            selectedStartDate = new Date(startDateTimestamp);
        }
        
        // Load due date from timestamp
        long dueDateTimestamp = args.getLong(ARG_TASK_DUE_DATE, -1);
        if (dueDateTimestamp != -1) {
            selectedDueDate = new Date(dueDateTimestamp);
        }
        
        task = new Task(
            args.getString(ARG_TASK_ID),           // id
            null,                                   // projectId
            null,                                   // boardId
            args.getString(ARG_TASK_TITLE),        // title
            args.getString(ARG_TASK_DESCRIPTION),  // description
            null,                                   // issueKey
            null,                                   // type
            null,                                   // status
            null,                                   // priority
            0.0,                                    // position
            null,                                   // assigneeId
            null,                                   // createdBy
            null,                                   // sprintId
            null,                                   // epicId
            null,                                   // parentTaskId
            selectedStartDate,                      // startAt
            selectedDueDate,                        // dueAt
            null,                                   // storyPoints
            null,                                   // originalEstimateSec
            null,                                   // remainingEstimateSec
            null,                                   // createdAt
            null                                    // updatedAt
        );
        
        if (rbTaskTitle != null) {
            rbTaskTitle.setText(args.getString(ARG_TASK_TITLE, "No Title"));
            rbTaskTitle.setChecked(false); 
            rbTaskTitle.setEnabled(false);
        }
        if (etDescription != null) {
            etDescription.setText(args.getString(ARG_TASK_DESCRIPTION, "No description"));
        }
        
        // Display start date
        if (etDateStart != null) {
            if (selectedStartDate != null) {
                etDateStart.setText(dateFormat.format(selectedStartDate));
            } else {
                etDateStart.setHint("Select start date");
            }
        }
        
        // Display due date
        if (etDueDate != null) {
            if (selectedDueDate != null) {
                etDueDate.setText(dateFormat.format(selectedDueDate));
            } else {
                etDueDate.setHint("Select due date");
            }
        }
    }
    
    private void setupListeners() {
        if (ivClose != null) {
            ivClose.setOnClickListener(v -> dismiss());
        }
        
        // Setup start date picker
        if (etDateStart != null) {
            etDateStart.setOnClickListener(v -> showStartDatePicker());
        }
        
        // Setup due date picker
        if (etDueDate != null) {
            etDueDate.setOnClickListener(v -> showDueDatePicker());
        }
        
        if (btnMembers != null) {
            btnMembers.setOnClickListener(v -> {
                showAssignMemberDialog();
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
    }
    
    private void showStartDatePicker() {
        Calendar calendar = Calendar.getInstance();
        
        if (selectedStartDate != null) {
            calendar.setTime(selectedStartDate);
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
                
                selectedStartDate = newDate.getTime();
                etDateStart.setText(dateFormat.format(selectedStartDate));
                
                if (listener != null && task != null) {
                    listener.onUpdateStartDate(task, selectedStartDate);
                }
                
                Toast.makeText(requireContext(), 
                    "Start date: " + dateFormat.format(selectedStartDate), 
                    Toast.LENGTH_SHORT).show();
            },
            year, month, day
        );
        
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
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
    
    private void showAssignMemberDialog() {
        if (projectId == null || projectId.isEmpty()) {
            Toast.makeText(requireContext(), "Cannot assign task: Invalid project", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (task == null || task.getId() == null) {
            Toast.makeText(requireContext(), "Cannot assign task: Invalid task", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Use new multi-select AssignMemberBottomSheet
        AssignMemberBottomSheet assignSheet = AssignMemberBottomSheet.newInstance(projectId, task.getId());
        assignSheet.setOnAssigneesChangedListener(new AssignMemberBottomSheet.OnAssigneesChangedListener() {
            @Override
            public void onAssigneesChanged() {
                // Reload task details to show updated assignees
                Toast.makeText(requireContext(), "Assignees updated", Toast.LENGTH_SHORT).show();
                // TODO: Implement reload task details from API to update UI with new assignees
                // This will be implemented when updating task cards to show multiple avatars
            }
        });
        
        assignSheet.show(getParentFragmentManager(), "assign_member");
    }
    
    /**
     * Initialize ViewModel for Calendar Sync
     */
    private void initViewModel() {
        calendarSyncViewModel = new ViewModelProvider(this).get(TaskCalendarSyncViewModel.class);
    }
    
    /**
     * Observe ViewModel LiveData
     */
    private void observeViewModel() {
        // Observe sync update success
        calendarSyncViewModel.getSyncUpdatedTask().observe(getViewLifecycleOwner(), updatedTask -> {
            if (updatedTask != null) {
                currentTaskDTO = updatedTask;
                Toast.makeText(requireContext(), 
                    updatedTask.getCalendarReminderEnabled() 
                        ? "Calendar sync enabled" 
                        : "Calendar sync disabled", 
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
        
        // Get current sync settings from TaskDTO (if available) or defaults
        boolean currentSyncEnabled = false;
        int currentReminderTime = 15;
        Date lastSyncedAtDate = null;
        
        if (currentTaskDTO != null) {
            Boolean reminderEnabled = currentTaskDTO.getCalendarReminderEnabled();
            currentSyncEnabled = reminderEnabled != null && reminderEnabled;
            
            Integer reminderTime = currentTaskDTO.getCalendarReminderTime();
            if (reminderTime != null) {
                currentReminderTime = reminderTime;
            }
            
            // Parse ISO string to Date
            String lastSyncedAt = currentTaskDTO.getLastSyncedAt();
            if (lastSyncedAt != null && !lastSyncedAt.isEmpty()) {
                try {
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    lastSyncedAtDate = isoFormat.parse(lastSyncedAt);
                } catch (Exception e) {
                    // Ignore parse errors
                }
            }
        }
        
        // Show dialog
        CalendarSyncDialog dialog = CalendarSyncDialog.newInstance(
            task.getId(),
            task.getTitle(),
            task.getDueAt(),
            currentSyncEnabled,
            currentReminderTime,
            lastSyncedAtDate
        );
        
        dialog.setOnSyncSettingsChangedListener((taskId, enabled, reminderMinutes) -> {
            // Call ViewModel to update calendar sync
            calendarSyncViewModel.updateCalendarSync(taskId, enabled, reminderMinutes);
        });
        
        dialog.show(getParentFragmentManager(), "calendar_sync");
    }
}

