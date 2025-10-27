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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.adapter.AttachmentAdapter;
import com.example.tralalero.adapter.CommentAdapter;
import com.example.tralalero.domain.model.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
public class TaskDetailBottomSheet extends BottomSheetDialogFragment {
    private static final String ARG_TASK_ID = "task_id";
    private static final String ARG_TASK_TITLE = "task_title";
    private static final String ARG_TASK_DESCRIPTION = "task_description";
    private static final String ARG_TASK_PRIORITY = "task_priority";
    private static final String ARG_TASK_STATUS = "task_status";
    private static final String ARG_TASK_START_DATE = "task_start_date";
    private static final String ARG_TASK_DUE_DATE = "task_due_date";
    private static final String ARG_IS_EDIT_MODE = "is_edit_mode";
    
    private Task task;
    private OnActionClickListener listener;
    private ImageView ivClose;
    private RadioButton rbTaskTitle;
    private EditText etDescription;
    private EditText etDateStart;
    private EditText etDueDate;
    private MaterialButton btnMembers;
    private MaterialButton btnAddAttachment;
    private MaterialButton btnAddComment;
    private MaterialButton btnDeleteTask;
    private MaterialButton btnConfirm;
    private boolean isEditMode = false;
    
    // RecyclerViews and Adapters
    private RecyclerView rvAttachments;
    private RecyclerView rvComments;
    private TextView tvNoAttachments;
    private TextView tvNoComments;
    private AttachmentAdapter attachmentAdapter;
    private CommentAdapter commentAdapter;
    
    private Date selectedStartDate;
    private Date selectedDueDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    
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
        loadTaskData();
        setupListeners();
    }
    private void initViews(View view) {
        ivClose = view.findViewById(R.id.ivClose);
        etDescription = view.findViewById(R.id.etDescription);
        etDateStart = view.findViewById(R.id.etDateStart);
        etDueDate = view.findViewById(R.id.etDueDate);
        btnMembers = view.findViewById(R.id.btnMembers); 
        btnAddAttachment = view.findViewById(R.id.btnAddAttachment); 
        btnAddComment = view.findViewById(R.id.btnAddComment); 
        btnDeleteTask = view.findViewById(R.id.btnDeleteTask);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        
        // RecyclerViews
        rvAttachments = view.findViewById(R.id.rvAttachments);
        rvComments = view.findViewById(R.id.rvComments);
        tvNoAttachments = view.findViewById(R.id.tvNoAttachments);
        tvNoComments = view.findViewById(R.id.tvNoComments);
        
        etDescription.setEnabled(false);
        
        // Setup RecyclerViews
        setupRecyclerViews();
        
        // Enable date fields for editing
        etDateStart.setEnabled(true);
        etDateStart.setFocusable(false);
        etDateStart.setClickable(true);
        
        etDueDate.setEnabled(true);
        etDueDate.setFocusable(false);
        etDueDate.setClickable(true);
    }
    
    private void setupRecyclerViews() {
        if (getContext() == null) return;
        
        // Setup Attachments RecyclerView
        rvAttachments.setLayoutManager(new LinearLayoutManager(getContext()));
        attachmentAdapter = new AttachmentAdapter(new AttachmentAdapter.OnAttachmentClickListener() {
            @Override
            public void onDownloadClick(com.example.tralalero.domain.model.Attachment attachment) {
                // TODO: Implement download functionality
                Toast.makeText(getContext(), "Download: " + attachment.getFileName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(com.example.tralalero.domain.model.Attachment attachment) {
                // TODO: Implement delete attachment
                Toast.makeText(getContext(), "Delete attachment", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAttachmentClick(com.example.tralalero.domain.model.Attachment attachment) {
                // TODO: Open/View attachment
                Toast.makeText(getContext(), "View: " + attachment.getFileName(), Toast.LENGTH_SHORT).show();
            }
        });
        rvAttachments.setAdapter(attachmentAdapter);
        
        // Setup Comments RecyclerView
        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        commentAdapter = new CommentAdapter(new CommentAdapter.OnCommentClickListener() {
            @Override
            public void onOptionsClick(com.example.tralalero.domain.model.TaskComment comment, int position) {
                // TODO: Show edit/delete options
                Toast.makeText(getContext(), "Comment options", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCommentClick(com.example.tralalero.domain.model.TaskComment comment) {
                // Optional: Handle comment click
            }
        });
        rvComments.setAdapter(commentAdapter);
        
        // Initially hide RecyclerViews and show placeholders
        rvAttachments.setVisibility(View.GONE);
        tvNoAttachments.setVisibility(View.VISIBLE);
        rvComments.setVisibility(View.GONE);
        tvNoComments.setVisibility(View.VISIBLE);
    }
    
    private void loadTaskData() {
        Bundle args = getArguments();
        if (args == null) return;
        
        // Get edit mode flag
        isEditMode = args.getBoolean(ARG_IS_EDIT_MODE, false);
        
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
        
        // Load attachments and comments if in edit mode and taskId exists
        if (isEditMode && task != null && task.getId() != null && !task.getId().isEmpty()) {
            // Note: Actual loading will be done by the host activity/fragment
            // via setAttachments() and setComments() methods
        }
    }
    
    // Public methods to set data from host Activity/Fragment
    public void setAttachments(java.util.List<com.example.tralalero.domain.model.Attachment> attachments) {
        if (attachmentAdapter != null) {
            attachmentAdapter.setAttachments(attachments);
            if (attachments != null && !attachments.isEmpty()) {
                rvAttachments.setVisibility(View.VISIBLE);
                tvNoAttachments.setVisibility(View.GONE);
            } else {
                rvAttachments.setVisibility(View.GONE);
                tvNoAttachments.setVisibility(View.VISIBLE);
            }
        }
    }
    
    public void setComments(java.util.List<com.example.tralalero.domain.model.TaskComment> comments) {
        if (commentAdapter != null) {
            commentAdapter.setComments(comments);
            if (comments != null && !comments.isEmpty()) {
                rvComments.setVisibility(View.VISIBLE);
                tvNoComments.setVisibility(View.GONE);
            } else {
                rvComments.setVisibility(View.GONE);
                tvNoComments.setVisibility(View.VISIBLE);
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
                if (listener != null && task != null) {
                    listener.onAssignTask(task);
                }
                dismiss();
            });
        }
        if (btnAddAttachment != null) {
            btnAddAttachment.setOnClickListener(v -> {
                if (listener != null && task != null) {
                    listener.onMoveTask(task);
                }
                dismiss();
            });
        }
        if (btnAddComment != null) {
            btnAddComment.setOnClickListener(v -> {
                if (listener != null && task != null) {
                    listener.onAddComment(task);
                }
                dismiss();
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
}
