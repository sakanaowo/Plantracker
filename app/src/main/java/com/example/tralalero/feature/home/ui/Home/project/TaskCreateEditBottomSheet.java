package com.example.tralalero.feature.home.ui.Home.project;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

public class TaskCreateEditBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_TASK = "task";
    private static final String ARG_BOARD_ID = "board_id";
    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_IS_EDIT_MODE = "is_edit_mode";
    private Task task;
    private String boardId;
    private String projectId;
    private boolean isEditMode;
    private String originalTaskId;
    private String originalAssigneeId;
    private String originalCreatedBy;
    private String originalStatus;
    private double originalPosition;
    private String originalCreatedAt;
    private String originalUpdatedAt;
    private TextView tvTitle;
    private ImageView ivClose;
    private TextInputEditText etTaskTitle;
    private TextInputEditText etDescription;
    private RadioGroup rgPriority;
    private RadioButton rbLow;
    private RadioButton rbMedium;
    private RadioButton rbHigh;
    private Button btnDelete;
    private Button btnCancel;
    private Button btnSave;
    private OnTaskActionListener listener;

    public interface OnTaskActionListener {
        void onTaskCreated(Task task);
        void onTaskUpdated(Task task);
        void onTaskDeleted(String taskId);
    }

    public static TaskCreateEditBottomSheet newInstanceForCreate(String boardId, String projectId) {
        TaskCreateEditBottomSheet fragment = new TaskCreateEditBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_BOARD_ID, boardId);
        args.putString(ARG_PROJECT_ID, projectId);
        args.putBoolean(ARG_IS_EDIT_MODE, false);
        fragment.setArguments(args);
        return fragment;
    }

    public static TaskCreateEditBottomSheet newInstanceForEdit(Task task, String boardId, String projectId) {
        TaskCreateEditBottomSheet fragment = new TaskCreateEditBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_BOARD_ID, boardId);
        args.putString(ARG_PROJECT_ID, projectId);
        args.putBoolean(ARG_IS_EDIT_MODE, true);
        args.putString("task_id", task.getId());
        args.putString("task_title", task.getTitle());
        args.putString("task_description", task.getDescription());
        args.putString("task_priority", task.getPriority() != null ? task.getPriority().name() : "MEDIUM");
        args.putString("task_status", task.getStatus() != null ? task.getStatus().name() : "TO_DO");
        args.putDouble("task_position", task.getPosition());
        args.putString("task_assignee_id", task.getAssigneeId());
        args.putString("task_created_by", task.getCreatedBy());

        fragment.setArguments(args);
        return fragment;
    }

    public void setOnTaskActionListener(OnTaskActionListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_task_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            boardId = getArguments().getString(ARG_BOARD_ID);
            projectId = getArguments().getString(ARG_PROJECT_ID);
            isEditMode = getArguments().getBoolean(ARG_IS_EDIT_MODE, false);
        }
        initViews(view);
        setupUI();
        setupListeners();
    }

    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tvTitle);
        ivClose = view.findViewById(R.id.ivClose);
        etTaskTitle = view.findViewById(R.id.etTaskTitle);
        etDescription = view.findViewById(R.id.etDescription);
        rgPriority = view.findViewById(R.id.rgPriority);
        rbLow = view.findViewById(R.id.rbLow);
        rbMedium = view.findViewById(R.id.rbMedium);
        rbHigh = view.findViewById(R.id.rbHigh);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupUI() {
        if (isEditMode) {
            tvTitle.setText("Edit Task");
            btnDelete.setVisibility(View.VISIBLE);
            btnSave.setText("Update");
            if (getArguments() != null) {
                etTaskTitle.setText(getArguments().getString("task_title", ""));
                etDescription.setText(getArguments().getString("task_description", ""));

                String priority = getArguments().getString("task_priority", "MEDIUM");
                switch (priority) {
                    case "LOW":
                        rbLow.setChecked(true);
                        break;
                    case "HIGH":
                        rbHigh.setChecked(true);
                        break;
                    default:
                        rbMedium.setChecked(true);
                        break;
                }
            }
        } else {
            tvTitle.setText("Create New Task");
            btnDelete.setVisibility(View.GONE);
            btnSave.setText("Create");
            rbMedium.setChecked(true); // Default priority
        }
    }

    private void setupListeners() {
        ivClose.setOnClickListener(v -> dismiss());
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveTask());
        btnDelete.setOnClickListener(v -> deleteTask());
    }

    private void saveTask() {
        String title = etTaskTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(getContext(), "Please enter task title", Toast.LENGTH_SHORT).show();
            etTaskTitle.requestFocus();
            return;
        }

        String description = etDescription.getText().toString().trim();
        Task.TaskPriority priority = Task.TaskPriority.MEDIUM;
        int selectedId = rgPriority.getCheckedRadioButtonId();
        if (selectedId == R.id.rbLow) {
            priority = Task.TaskPriority.LOW;
        } else if (selectedId == R.id.rbHigh) {
            priority = Task.TaskPriority.HIGH;
        }

        if (isEditMode) {
            if (getArguments() != null) {
                String taskId = getArguments().getString("task_id");
                String status = getArguments().getString("task_status", "TO_DO");
                double position = getArguments().getDouble("task_position", 0);
                String assigneeId = getArguments().getString("task_assignee_id");
                String createdBy = getArguments().getString("task_created_by");
                Task updatedTask = new Task(
                    taskId,
                    projectId,
                    boardId,
                    title,
                    description,
                    null, // issueKey - not editable here
                    null, // type - not editable here
                    Task.TaskStatus.valueOf(status), // ✅ PRESERVE original status
                    priority, // ✅ UPDATE priority
                    position, // ✅ PRESERVE position
                    assigneeId, // ✅ PRESERVE assignee
                    createdBy, // ✅ PRESERVE creator
                    null, // sprintId - not editable here
                    null, // epicId - not editable here
                    null, // parentTaskId - not editable here
                    null, // startAt - not editable here
                    null, // dueAt - not editable here
                    null, // storyPoints - not editable here
                    null, // originalEstimateSec - not editable here
                    null, // remainingEstimateSec - not editable here
                    null, // createdAt - backend handles
                    null, // updatedAt - backend handles
                    false, // calendarSyncEnabled - not editable here
                    null, // calendarReminderMinutes - not editable here
                    null, // calendarEventId - not editable here
                    null, // calendarSyncedAt - not editable here
                    null  // labels - not editable here
                );

                if (listener != null) {
                    listener.onTaskUpdated(updatedTask);
                }
            }
        } else {
            Task newTask = new Task(
                "", // id (will be generated by backend)
                projectId,
                boardId,
                title,
                description,
                null, // issueKey
                null, // type
                Task.TaskStatus.TO_DO, // ✅ Default status for new tasks
                priority,
                0.0, // position (backend will assign)
                null, // assigneeId
                null, // createdBy (backend will set from auth token)
                null, // sprintId
                null, // epicId
                null, // parentTaskId
                null, // startAt
                null, // dueAt
                null, // storyPoints
                null, // originalEstimateSec
                null, // remainingEstimateSec
                null, // createdAt (backend will set)
                null, // updatedAt (backend will set)
                false, // calendarSyncEnabled
                null, // calendarReminderMinutes
                null, // calendarEventId
                null, // calendarSyncedAt
                null  // labels
            );

            if (listener != null) {
                listener.onTaskCreated(newTask);
            }
        }

        dismiss();
    }

    private void deleteTask() {
        if (getArguments() != null) {
            String taskId = getArguments().getString("task_id");
            if (listener != null && taskId != null) {
                listener.onTaskDeleted(taskId);
            }
        }
        dismiss();
    }
}
