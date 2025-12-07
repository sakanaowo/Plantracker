package com.example.tralalero.feature.home.ui.Home.project;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.model.Label;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks = new ArrayList<>();
    private OnTaskClickListener listener;
    private String currentBoardType; // "To Do", "In Progress", "Done"

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskCheckboxClick(Task task, String currentBoardType);
    }

    public void setCurrentBoardType(String boardType) {
        this.currentBoardType = boardType;
    }

    public TaskAdapter(OnTaskClickListener listener) {
        this.listener = listener;
    }
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateTask(Task updatedTask) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(updatedTask.getId())) {
                tasks.set(i, updatedTask);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks);  // Return a copy to avoid external modifications
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }
    @Override
    public int getItemCount() {
        return tasks.size();
    }
    class TaskViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkboxTask;
        private TextView tvTaskTitle;
        private TextView tvPriority;
        private TextView tvDueDate;
        private ChipGroup chipGroupLabels;
        private RecyclerView rvTaskAssignees;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxTask = itemView.findViewById(R.id.checkBoxTask);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            chipGroupLabels = itemView.findViewById(R.id.chipGroupLabels);
            rvTaskAssignees = itemView.findViewById(R.id.rvTaskAssignees);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTaskClick(tasks.get(position));
                }
            });
            
            // Handle checkbox click
            checkboxTask.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Task task = tasks.get(position);
                    listener.onTaskCheckboxClick(task, currentBoardType);
                }
            });
        }

        public void bind(Task task) {
            // Title
            tvTaskTitle.setText(task.getTitle() != null ? task.getTitle() : "No Title");
            
            // Labels
            if (task.getLabels() != null && !task.getLabels().isEmpty()) {
                chipGroupLabels.removeAllViews();
                chipGroupLabels.setVisibility(View.VISIBLE);
                
                for (Label label : task.getLabels()) {
                    Chip chip = new Chip(itemView.getContext());
                    chip.setText(label.getName());
                    chip.setChipBackgroundColorResource(android.R.color.transparent);
                    chip.setChipStrokeWidth(2f);
                    
                    // Parse color
                    try {
                        int color = Color.parseColor(label.getColor());
                        chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(color));
                        chip.setTextColor(color);
                    } catch (Exception e) {
                        chip.setChipStrokeColorResource(R.color.primary);
                        chip.setTextColor(itemView.getContext().getColor(R.color.primary));
                    }
                    
                    chip.setTextSize(10f);
                    chip.setClickable(false);
                    chip.setCheckable(false);
                    chipGroupLabels.addView(chip);
                }
            } else {
                chipGroupLabels.setVisibility(View.GONE);
            }
            
            // Priority
            if (task.getPriority() != null) {
                tvPriority.setVisibility(View.VISIBLE);
                tvPriority.setText(task.getPriority().name());
                
                switch (task.getPriority()) {
                    case HIGH:
                        tvPriority.setBackgroundResource(R.drawable.bg_priority_high);
                        break;
                    case MEDIUM:
                        tvPriority.setBackgroundResource(R.drawable.bg_priority_medium);
                        break;
                    case LOW:
                        tvPriority.setBackgroundResource(R.drawable.bg_priority_low);
                        break;
                }
            } else {
                tvPriority.setVisibility(View.GONE);
            }
            
            // Due Date with full date format
            if (task.getDueAt() != null) {
                tvDueDate.setVisibility(View.VISIBLE);
                
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                String dateText = sdf.format(task.getDueAt());
                tvDueDate.setText(dateText);
                
                // Color code based on due date status
                long now = System.currentTimeMillis();
                long dueTime = task.getDueAt().getTime();
                
                if (task.getStatus() != Task.TaskStatus.DONE) {
                    if (dueTime < now) {
                        // Overdue - Red
                        tvDueDate.setTextColor(Color.parseColor("#DC3545"));
                    } else if (dueTime - now < 24 * 60 * 60 * 1000) {
                        // Due within 24 hours - Orange
                        tvDueDate.setTextColor(Color.parseColor("#FD7E14"));
                    } else {
                        // Normal - Gray
                        tvDueDate.setTextColor(Color.parseColor("#666666"));
                    }
                } else {
                    tvDueDate.setTextColor(Color.parseColor("#666666"));
                }
            } else {
                tvDueDate.setVisibility(View.GONE);
            }
            
            // Assignees (hide for now, can be enhanced later)
            rvTaskAssignees.setVisibility(View.GONE);
            
            // Checkbox state
            boolean isDone = task.getStatus() == Task.TaskStatus.DONE;
            checkboxTask.setChecked(isDone);
            
            // Strikethrough for completed tasks
            if (isDone) {
                tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTaskTitle.setAlpha(0.6f);
            } else {
                tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTaskTitle.setAlpha(1.0f);
            }
        }
    }
}
