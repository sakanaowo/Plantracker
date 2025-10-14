package com.example.tralalero.feature.home.ui.Home.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * BottomSheet showing task details and actions
 * Uses card_detail.xml layout
 * 
 * Actions:
 * - Assign to user (via Members button)
 * - Move to board
 * - Add comment
 * - Delete task
 * 
 * @author Người 3
 * @date 14/10/2025
 */
public class TaskDetailBottomSheet extends BottomSheetDialogFragment {
    
    private static final String ARG_TASK_ID = "task_id";
    private static final String ARG_TASK_TITLE = "task_title";
    private static final String ARG_TASK_DESCRIPTION = "task_description";
    private static final String ARG_TASK_PRIORITY = "task_priority";
    private static final String ARG_TASK_STATUS = "task_status";
    private static final String ARG_TASK_DUE_DATE = "task_due_date";
    
    private Task task;
    private OnActionClickListener listener;
    
    // UI Components from card_detail.xml
    private ImageView ivClose;
    private RadioButton rbTaskTitle;
    private EditText etDescription;
    private EditText etDateStart;
    private EditText etDueDate;
    private MaterialButton btnMembers;
    private MaterialButton btnAddAttachment;
    private MaterialButton btnAddComment;
    private MaterialButton btnDeleteTask;
    
    /**
     * Interface for action callbacks
     */
    public interface OnActionClickListener {
        void onAssignTask(Task task);
        void onMoveTask(Task task);
        void onAddComment(Task task);
        void onDeleteTask(Task task);
    }
    
    /**
     * Create new instance with task data
     */
    public static TaskDetailBottomSheet newInstance(Task task) {
        TaskDetailBottomSheet fragment = new TaskDetailBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_TASK_ID, task.getId());
        args.putString(ARG_TASK_TITLE, task.getTitle());
        args.putString(ARG_TASK_DESCRIPTION, task.getDescription());
        args.putString(ARG_TASK_PRIORITY, task.getPriority().name());
        args.putString(ARG_TASK_STATUS, task.getStatus().name());
        
        if (task.getDueAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            args.putString(ARG_TASK_DUE_DATE, sdf.format(task.getDueAt()));
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
        
        // Initialize views
        initViews(view);
        
        // Load task data from arguments
        loadTaskData();
        
        // Setup listeners
        setupListeners();
    }
    
    private void initViews(View view) {
        // Views from card_detail.xml
        ivClose = view.findViewById(R.id.ivClose);
        rbTaskTitle = view.findViewById(R.id.rbTaskTitle); // Assuming RadioButton has this ID
        etDescription = view.findViewById(R.id.etDescription);
        etDateStart = view.findViewById(R.id.etDateStart);
        etDueDate = view.findViewById(R.id.etDueDate);
        btnMembers = view.findViewById(R.id.btnMembers); // Assign Task button
        btnAddAttachment = view.findViewById(R.id.btnAddAttachment); // Move Task button
        btnAddComment = view.findViewById(R.id.btnAddComment); // Add Comment button
        btnDeleteTask = view.findViewById(R.id.btnDeleteTask); // Delete Task button
        
        // Disable editing for read-only bottom sheet
        etDescription.setEnabled(false);
        etDateStart.setEnabled(false);
        etDueDate.setEnabled(false);
    }
    
    private void loadTaskData() {
        Bundle args = getArguments();
        if (args == null) return;
        
        // Reconstruct task from arguments with correct constructor signature
        // Task(id, projectId, boardId, title, description, issueKey, type, status, priority,
        //      position, assigneeId, createdBy, sprintId, epicId, parentTaskId, 
        //      startAt, dueAt, storyPoints, originalEstimateSec, remainingEstimateSec,
        //      createdAt, updatedAt)
        task = new Task(
            args.getString(ARG_TASK_ID),      // id
            null,                              // projectId
            null,                              // boardId
            args.getString(ARG_TASK_TITLE),   // title
            args.getString(ARG_TASK_DESCRIPTION), // description
            null,                              // issueKey
            null,                              // type (TaskType enum)
            null,                              // status (TaskStatus enum)
            null,                              // priority (TaskPriority enum)
            0.0,                               // position (double - cannot be null)
            null,                              // assigneeId
            null,                              // createdBy
            null,                              // sprintId
            null,                              // epicId
            null,                              // parentTaskId
            null,                              // startAt (Date)
            null,                              // dueAt (Date)
            null,                              // storyPoints (Integer)
            null,                              // originalEstimateSec (Integer)
            null,                              // remainingEstimateSec (Integer)
            null,                              // createdAt (Date)
            null                               // updatedAt (Date)
        );
        
        // Display data in card_detail.xml views
        if (rbTaskTitle != null) {
            rbTaskTitle.setText(args.getString(ARG_TASK_TITLE, "No Title"));
            rbTaskTitle.setChecked(false); // Not a selectable item in bottom sheet
            rbTaskTitle.setEnabled(false);
        }
        
        if (etDescription != null) {
            etDescription.setText(args.getString(ARG_TASK_DESCRIPTION, "No description"));
        }
        
        if (etDateStart != null) {
            // For now, use created date or leave empty
            etDateStart.setText(""); // TODO: Add start date if available
        }
        
        if (etDueDate != null) {
            String dueDate = args.getString(ARG_TASK_DUE_DATE);
            etDueDate.setText(dueDate != null ? dueDate : "No due date");
        }
    }
    
    private void setupListeners() {
        // Close button
        if (ivClose != null) {
            ivClose.setOnClickListener(v -> dismiss());
        }
        
        // Assign Task button (previously Members)
        if (btnMembers != null) {
            btnMembers.setOnClickListener(v -> {
                if (listener != null && task != null) {
                    listener.onAssignTask(task);
                }
                dismiss();
            });
        }
        
        // Move Task button (previously Add Attachment)
        if (btnAddAttachment != null) {
            btnAddAttachment.setOnClickListener(v -> {
                if (listener != null && task != null) {
                    listener.onMoveTask(task);
                }
                dismiss();
            });
        }
        
        // Add Comment button
        if (btnAddComment != null) {
            btnAddComment.setOnClickListener(v -> {
                if (listener != null && task != null) {
                    listener.onAddComment(task);
                }
                dismiss();
            });
        }
        
        // Delete Task button
        if (btnDeleteTask != null) {
            btnDeleteTask.setOnClickListener(v -> {
                if (listener != null && task != null) {
                    listener.onDeleteTask(task);
                }
                dismiss();
            });
        }
    }
}
