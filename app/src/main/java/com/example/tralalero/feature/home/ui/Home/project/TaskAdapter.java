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
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskCompleted(Task task);
    }
    public TaskAdapter(OnTaskClickListener listener) {
        this.listener = listener;
    }
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        notifyDataSetChanged();
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
            checkboxTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Task task = tasks.get(position);
                    // Apply strikethrough to task name when checked
                    if (isChecked) {
                        tvTaskName.setPaintFlags(tvTaskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        tvTaskDescription.setPaintFlags(tvTaskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        
                        // Notify listener to move task to Done board
                        if (listener != null) {
                            listener.onTaskCompleted(task);
                        }
                    } else {
                        tvTaskName.setPaintFlags(tvTaskName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                        tvTaskDescription.setPaintFlags(tvTaskDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    }
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
            
            // TODO: Set checkbox state from task.isCompleted() if you have that field
            checkboxTask.setChecked(false);
        }
    }
}
