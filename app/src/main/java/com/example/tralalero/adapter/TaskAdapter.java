package com.example.tralalero.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.util.CrossBoardDragHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    private static final String TAG = "TaskAdapter";
    private List<Task> taskList;
    private String boardId; // ✅ NEW: Track which board this adapter belongs to
    private Context context;
    private OnTaskClickListener listener;
    private OnTaskMoveListener moveListener;
    private OnTaskStatusChangeListener statusChangeListener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public interface OnTaskMoveListener {
        void onMoveLeft(Task task, int position);
        void onMoveRight(Task task, int position);
    }
    
    public interface OnTaskStatusChangeListener {
        void onTaskStatusChanged(Task task, boolean isDone);
    }

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
        this.context = null;
    }
    
    // ✅ NEW: Constructor with boardId for cross-board drag
    public TaskAdapter(List<Task> taskList, String boardId) {
        this.taskList = taskList;
        this.boardId = boardId;
        this.context = null;
    }
    
    public TaskAdapter(List<Task> taskList, String boardId, Context context) {
        this.taskList = taskList;
        this.boardId = boardId;
        this.context = context;
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void setOnTaskMoveListener(OnTaskMoveListener moveListener) {
        this.moveListener = moveListener;
    }
    
    public void setOnTaskStatusChangeListener(OnTaskStatusChangeListener statusChangeListener) {
        this.statusChangeListener = statusChangeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.board_detail_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = taskList.get(position);
        boolean isFirst = position == 0;
        boolean isLast = position == taskList.size() - 1;
        holder.bind(task, boardId, context, listener, moveListener, statusChangeListener, isFirst, isLast);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateTasks(List<Task> newTasks) {
        // ✅ Create a new ArrayList to ensure adapter detects changes
        this.taskList = newTasks != null ? new ArrayList<>(newTasks) : new ArrayList<>();
        Log.d(TAG, "✅ TaskAdapter updated with " + this.taskList.size() + " tasks for board: " + boardId);
        notifyDataSetChanged();
    }

    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition < 0 || fromPosition >= taskList.size() ||
            toPosition < 0 || toPosition >= taskList.size()) {
            return;
        }
        
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(taskList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(taskList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public Task getTaskAt(int position) {
        if (position < 0 || position >= taskList.size()) {
            return null;
        }
        return taskList.get(position);
    }
    
    public List<Task> getTasks() {
        return taskList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView tvTaskTitle;
        ImageButton btnMoveLeft;
        ImageButton btnMoveRight;
        RecyclerView rvTaskLabels;
        RecyclerView rvTaskAssignees;
        TaskLabelAdapter labelAdapter;
        TaskAssigneeAdapter assigneeAdapter;

        ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBoxTask);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            btnMoveLeft = itemView.findViewById(R.id.btnMoveLeft);
            btnMoveRight = itemView.findViewById(R.id.btnMoveRight);
            rvTaskLabels = itemView.findViewById(R.id.rvTaskLabels);
            rvTaskAssignees = itemView.findViewById(R.id.rvTaskAssignees);
            
            // Setup label adapter
            labelAdapter = new TaskLabelAdapter();
            rvTaskLabels.setAdapter(labelAdapter);
        }

        void bind(Task task, String boardId, Context context, OnTaskClickListener listener, OnTaskMoveListener moveListener, 
                  OnTaskStatusChangeListener statusChangeListener, boolean isFirst, boolean isLast) {
            tvTaskTitle.setText(task.getTitle());

            // Setup labels
            android.util.Log.d("TaskAdapter", "Task: " + task.getTitle() + ", hasLabels: " + task.hasLabels() + 
                ", hasAssignees: " + task.hasAssignees());
            if (task.hasLabels()) {
                android.util.Log.d("TaskAdapter", "Labels count: " + task.getLabels().size());
                rvTaskLabels.setVisibility(View.VISIBLE);
                labelAdapter.setLabels(task.getLabels());
            } else {
                rvTaskLabels.setVisibility(View.GONE);
            }
            
            // Setup assignees
            if (task.hasAssignees() && context != null) {
                android.util.Log.d("TaskAdapter", "Assignees count: " + task.getAssignees().size());
                rvTaskAssignees.setVisibility(View.VISIBLE);
                if (assigneeAdapter == null) {
                    assigneeAdapter = new TaskAssigneeAdapter(context);
                    rvTaskAssignees.setAdapter(assigneeAdapter);
                }
                // Convert AssigneeInfo to adapter's AssigneeInfo
                List<TaskAssigneeAdapter.AssigneeInfo> assignees = new ArrayList<>();
                for (com.example.tralalero.domain.model.AssigneeInfo assignee : task.getAssignees()) {
                    android.util.Log.d("TaskAdapter", "Assignee: " + assignee.getName() + ", avatar: " + assignee.getAvatarUrl());
                    assignees.add(new TaskAssigneeAdapter.AssigneeInfo(
                        assignee.getId(),
                        assignee.getName(),
                        assignee.getAvatarUrl()
                    ));
                }
                assigneeAdapter.setAssignees(assignees);
            } else {
                rvTaskAssignees.setVisibility(View.GONE);
            }

            // ✅ FIX: Check actual task status (enum comparison)
            boolean isCompleted = task.getStatus() == Task.TaskStatus.DONE;
            checkBox.setChecked(isCompleted);
            tvTaskTitle.setAlpha(isCompleted ? 0.5f : 1f);

            // Clear previous listeners to avoid duplicate callbacks
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setOnClickListener(null);
            
            // Set new listener for checkbox
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Only trigger if user actually changed it (not programmatic)
                if (buttonView.isPressed()) {
                    tvTaskTitle.setAlpha(isChecked ? 0.5f : 1f);
                    if (statusChangeListener != null) {
                        Log.d(TAG, "Checkbox changed: task=" + task.getTitle() + ", isDone=" + isChecked);
                        statusChangeListener.onTaskStatusChanged(task, isChecked);
                    }
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });
            
            // ✅ REMOVED: Arrow buttons - replaced with drag & drop
            btnMoveLeft.setVisibility(View.GONE);
            btnMoveRight.setVisibility(View.GONE);
            
            // ✅ NEW: Enable cross-board drag on long press
            itemView.setOnLongClickListener(v -> {
                if (boardId != null) {
                    Log.d(TAG, "Starting drag for task: " + task.getTitle() + " from board: " + boardId);
                    return CrossBoardDragHelper.startDrag(itemView, task, boardId);
                } else {
                    Log.w(TAG, "Cannot start drag: boardId is null");
                    return false;
                }
            });
        }
    }
}
