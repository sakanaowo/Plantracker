package com.example.tralalero.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.Task;

import java.util.Collections;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    private List<Task> taskList;
    private OnTaskClickListener listener;
    private OnTaskDragListener dragListener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public interface OnTaskDragListener {
        void onTaskMoved(int fromPosition, int toPosition);
    }

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void setOnTaskDragListener(OnTaskDragListener dragListener) {
        this.dragListener = dragListener;
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
        holder.bind(task, listener);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateTasks(List<Task> newTasks) {
        this.taskList = newTasks;
        notifyDataSetChanged();
    }

    // ✅ Hỗ trợ kéo thả
    public void moveItem(int fromPosition, int toPosition) {
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

        if (dragListener != null) {
            dragListener.onTaskMoved(fromPosition, toPosition);
        }
    }

    public Task getTaskAt(int position) {
        return taskList.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView tvTaskTitle;

        ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBoxTask);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
        }

        void bind(Task task, OnTaskClickListener listener) {
            // Set task title
            tvTaskTitle.setText(task.getTitle());

            // Set checkbox state (if task has completion status)
            checkBox.setChecked(false); // TODO: Bind with task.isCompleted() if available

            // Checkbox change listener
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                tvTaskTitle.setAlpha(isChecked ? 0.5f : 1f);
                // TODO: Call API to update task status
            });

            // ✅ Click entire item to open detail
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });

            // Prevent checkbox click from triggering item click
            checkBox.setOnClickListener(v -> {
                // Just toggle checkbox, don't open detail
            });
        }
    }
}
