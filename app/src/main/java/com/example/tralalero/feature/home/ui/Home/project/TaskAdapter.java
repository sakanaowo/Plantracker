package com.example.tralalero.feature.home.ui.Home.project;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    }
    public TaskAdapter(OnTaskClickListener listener) {
        this.listener = listener;
    }
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        notifyDataSetChanged();
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
        private TextView taskTitle;
        private TextView taskDescription;
        private TextView taskPriority;
        private TextView taskDueDate;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskPriority = itemView.findViewById(R.id.taskPriority);
            taskDueDate = itemView.findViewById(R.id.taskDueDate);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTaskClick(tasks.get(position));
                }
            });
        }
        public void bind(Task task) {
            taskTitle.setText(task.getTitle() != null ? task.getTitle() : "No Title");
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                taskDescription.setText(task.getDescription());
                taskDescription.setVisibility(View.VISIBLE);
            } else {
                taskDescription.setVisibility(View.GONE);
            }
            if (task.getPriority() != null) {
                taskPriority.setText(task.getPriority().name());  
                taskPriority.setVisibility(View.VISIBLE);
                switch (task.getPriority()) {
                    case HIGH:
                        taskPriority.setBackgroundColor(itemView.getContext().getResources()
                                .getColor(android.R.color.holo_red_light));
                        break;
                    case MEDIUM:
                        taskPriority.setBackgroundColor(itemView.getContext().getResources()
                                .getColor(android.R.color.holo_orange_light));
                        break;
                    case LOW:
                        taskPriority.setBackgroundColor(itemView.getContext().getResources()
                                .getColor(android.R.color.holo_green_light));
                        break;
                    default:
                        taskPriority.setBackgroundColor(itemView.getContext().getResources()
                                .getColor(android.R.color.darker_gray));
                        break;
                }
            } else {
                taskPriority.setVisibility(View.GONE);
            }
            if (task.getDueAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                taskDueDate.setText("Due: " + sdf.format(task.getDueAt()));
                taskDueDate.setVisibility(View.VISIBLE);
            } else {
                taskDueDate.setVisibility(View.GONE);
            }
        }
    }
}
