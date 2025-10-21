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
public class TaskDetailBottomSheet extends BottomSheetDialogFragment {
    private static final String ARG_TASK_ID = "task_id";
    private static final String ARG_TASK_TITLE = "task_title";
    private static final String ARG_TASK_DESCRIPTION = "task_description";
    private static final String ARG_TASK_PRIORITY = "task_priority";
    private static final String ARG_TASK_STATUS = "task_status";
    private static final String ARG_TASK_DUE_DATE = "task_due_date";
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
    public interface OnActionClickListener {
        void onAssignTask(Task task);
        void onMoveTask(Task task);
        void onAddComment(Task task);
        void onDeleteTask(Task task);
    }
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
        initViews(view);
        loadTaskData();
        setupListeners();
    }
    private void initViews(View view) {
        ivClose = view.findViewById(R.id.ivClose);
//        rbTaskTitle = view.findViewById(R.id.rbTaskTitle);
        etDescription = view.findViewById(R.id.etDescription);
        etDateStart = view.findViewById(R.id.etDateStart);
        etDueDate = view.findViewById(R.id.etDueDate);
        btnMembers = view.findViewById(R.id.btnMembers); 
        btnAddAttachment = view.findViewById(R.id.btnAddAttachment); 
        btnAddComment = view.findViewById(R.id.btnAddComment); 
        btnDeleteTask = view.findViewById(R.id.btnDeleteTask); 
        etDescription.setEnabled(false);
        etDateStart.setEnabled(false);
        etDueDate.setEnabled(false);
    }
    private void loadTaskData() {
        Bundle args = getArguments();
        if (args == null) return;
        task = new Task(
            args.getString(ARG_TASK_ID),      
            null,                              
            null,                              
            args.getString(ARG_TASK_TITLE),   
            args.getString(ARG_TASK_DESCRIPTION), 
            null,                              
            null,                              
            null,                              
            null,                              
            0.0,                               
            null,                              
            null,                              
            null,                              
            null,                              
            null,                              
            null,                              
            null,                              
            null,                              
            null,                              
            null,                              
            null,                              
            null                               
        );
        if (rbTaskTitle != null) {
            rbTaskTitle.setText(args.getString(ARG_TASK_TITLE, "No Title"));
            rbTaskTitle.setChecked(false); 
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
        if (ivClose != null) {
            ivClose.setOnClickListener(v -> dismiss());
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
}
