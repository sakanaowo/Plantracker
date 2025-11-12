package com.example.tralalero.adapter;

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

import java.util.Collections;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    private static final String TAG = "TaskAdapter";
    private List<Task> taskList;
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
        holder.bind(task, listener, moveListener, statusChangeListener, isFirst, isLast);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateTasks(List<Task> newTasks) {
        this.taskList = newTasks;
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

        ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBoxTask);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            btnMoveLeft = itemView.findViewById(R.id.btnMoveLeft);
            btnMoveRight = itemView.findViewById(R.id.btnMoveRight);
        }

        void bind(Task task, OnTaskClickListener listener, OnTaskMoveListener moveListener, 
                  OnTaskStatusChangeListener statusChangeListener, boolean isFirst, boolean isLast) {
            tvTaskTitle.setText(task.getTitle());

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
            
            // Setup move left button
            btnMoveLeft.setVisibility(View.VISIBLE);
            btnMoveLeft.setClickable(true);
            btnMoveLeft.setFocusable(true);
            btnMoveLeft.setOnClickListener(v -> {
                Log.d(TAG, "btnMoveLeft clicked at position " + getAdapterPosition());
                if (moveListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Log.d(TAG, "Calling moveListener.onMoveLeft(" + position + ")");
                        moveListener.onMoveLeft(task, position);
                    } else {
                        Log.w(TAG, "Position is NO_POSITION, cannot move");
                    }
                } else {
                    Log.w(TAG, "Cannot move left: moveListener is null");
                }
            });
            
            // Setup move right button
            btnMoveRight.setVisibility(View.VISIBLE);
            btnMoveRight.setClickable(true);
            btnMoveRight.setFocusable(true);
            btnMoveRight.setOnClickListener(v -> {
                Log.d(TAG, "btnMoveRight clicked at position " + getAdapterPosition());
                if (moveListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Log.d(TAG, "Calling moveListener.onMoveRight(" + position + ")");
                        moveListener.onMoveRight(task, position);
                    } else {
                        Log.w(TAG, "Position is NO_POSITION, cannot move");
                    }
                } else {
                    Log.w(TAG, "Cannot move right: moveListener is null");
                }
            });
        }
    }
}
