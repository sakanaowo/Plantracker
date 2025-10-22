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

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public interface OnTaskMoveListener {
        void onMoveUp(int position);
        void onMoveDown(int position);
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
        holder.bind(task, listener, moveListener, isFirst, isLast);
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
        return taskList.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView tvTaskTitle;
        ImageButton btnMoveUp;
        ImageButton btnMoveDown;

        ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBoxTask);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            btnMoveUp = itemView.findViewById(R.id.btnMoveUp);
            btnMoveDown = itemView.findViewById(R.id.btnMoveDown);
        }

        void bind(Task task, OnTaskClickListener listener, OnTaskMoveListener moveListener, 
                  boolean isFirst, boolean isLast) {
            tvTaskTitle.setText(task.getTitle());

            checkBox.setChecked(false); 

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                tvTaskTitle.setAlpha(isChecked ? 0.5f : 1f);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });

            // Stop checkbox from consuming clicks
            checkBox.setOnClickListener(v -> {
                // Prevent propagation to itemView
                v.setClickable(true);
            });
            
            // ✅ Setup move up button with logging
            btnMoveUp.setVisibility(View.VISIBLE);
            btnMoveUp.setEnabled(!isFirst);
            btnMoveUp.setAlpha(isFirst ? 0.3f : 1.0f);
            btnMoveUp.setClickable(true);
            btnMoveUp.setFocusable(true);
            btnMoveUp.setOnClickListener(v -> {
                Log.d(TAG, "btnMoveUp clicked at position " + getAdapterPosition() + ", isFirst=" + isFirst);
                if (moveListener != null && !isFirst) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Log.d(TAG, "Calling moveListener.onMoveUp(" + position + ")");
                        moveListener.onMoveUp(position);
                    } else {
                        Log.w(TAG, "Position is NO_POSITION, cannot move");
                    }
                } else {
                    Log.w(TAG, "Cannot move up: moveListener=" + moveListener + ", isFirst=" + isFirst);
                }
            });
            
            // ✅ Setup move down button with logging
            btnMoveDown.setVisibility(View.VISIBLE);
            btnMoveDown.setEnabled(!isLast);
            btnMoveDown.setAlpha(isLast ? 0.3f : 1.0f);
            btnMoveDown.setClickable(true);
            btnMoveDown.setFocusable(true);
            btnMoveDown.setOnClickListener(v -> {
                Log.d(TAG, "btnMoveDown clicked at position " + getAdapterPosition() + ", isLast=" + isLast);
                if (moveListener != null && !isLast) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Log.d(TAG, "Calling moveListener.onMoveDown(" + position + ")");
                        moveListener.onMoveDown(position);
                    } else {
                        Log.w(TAG, "Position is NO_POSITION, cannot move");
                    }
                } else {
                    Log.w(TAG, "Cannot move down: moveListener=" + moveListener + ", isLast=" + isLast);
                }
            });
        }
    }
}
