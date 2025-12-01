package com.example.tralalero.feature.home.ui.Home.project;
import android.graphics.Paint;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
                .inflate(R.layout.inbox_item1, parent, false);
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
        private TextView tvTaskName;
        private TextView tvTaskDescription;
        private TextView tvTaskTime;
        private ImageButton btnTime;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxTask = itemView.findViewById(R.id.checkboxTask);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvTaskDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvTaskTime = itemView.findViewById(R.id.tvTaskTime);
//            btnTime = itemView.findViewById(R.id.btnTime);
            
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
                    // Don't change checkbox state here, let the listener handle it
                    // This prevents immediate UI change before backend confirmation
                    listener.onTaskCheckboxClick(task, currentBoardType);
                }
            });
        }
        public void bind(Task task) {
            tvTaskName.setText(task.getTitle() != null ? task.getTitle() : "No Title");
            
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                tvTaskDescription.setText(task.getDescription());
                tvTaskDescription.setVisibility(View.VISIBLE);
            } else {
                tvTaskDescription.setVisibility(View.GONE);
            }
            
            // Display due date or priority
            if (task.getDueAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("'Due:' hh:mm a", Locale.getDefault());
                tvTaskTime.setText(sdf.format(task.getDueAt()));
                tvTaskTime.setVisibility(View.VISIBLE);
            } else if (task.getPriority() != null) {
                tvTaskTime.setText("Priority: " + task.getPriority().name());
                tvTaskTime.setVisibility(View.VISIBLE);
            } else {
                tvTaskTime.setVisibility(View.GONE);
            }
            
            // Check if task is DONE - show visual indicator
            boolean isDone = task.getStatus() == Task.TaskStatus.DONE;
            checkboxTask.setChecked(isDone);
            
            // Apply strikethrough for completed tasks
            if (isDone) {
                tvTaskName.setPaintFlags(tvTaskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTaskDescription.setPaintFlags(tvTaskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                // Make text slightly grayed out
                tvTaskName.setAlpha(0.6f);
                tvTaskDescription.setAlpha(0.6f);
            } else {
                tvTaskName.setPaintFlags(tvTaskName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTaskDescription.setPaintFlags(tvTaskDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTaskName.setAlpha(1.0f);
                tvTaskDescription.setAlpha(1.0f);
            }
        }
    }
}
