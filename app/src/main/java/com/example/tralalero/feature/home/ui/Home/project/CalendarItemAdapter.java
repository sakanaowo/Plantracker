package com.example.tralalero.feature.home.ui.Home.project;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.CalendarEvent;
import com.example.tralalero.domain.model.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying both Tasks and Events in calendar view
 * Tasks shown with 🟠 orange indicator
 * Events shown with 🟢 green indicator
 */
public class CalendarItemAdapter extends RecyclerView.Adapter<CalendarItemAdapter.ViewHolder> {
    
    private List<CalendarItem> items = new ArrayList<>();
    private OnItemClickListener listener;
    
    public interface OnItemClickListener {
        void onTaskClick(Task task);
        void onEventClick(CalendarEvent event);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    /**
     * Set combined list of tasks and events
     */
    public void setItems(List<Task> tasks, List<CalendarEvent> events) {
        items.clear();
        
        // Add tasks
        if (tasks != null) {
            for (Task task : tasks) {
                items.add(CalendarItem.fromTask(task));
            }
        }
        
        // Add events
        if (events != null) {
            for (CalendarEvent event : events) {
                items.add(CalendarItem.fromEvent(event));
            }
        }
        
        // Sort by time
        items.sort((a, b) -> {
            Date dateA = a.getStartDate();
            Date dateB = b.getStartDate();
            if (dateA == null && dateB == null) return 0;
            if (dateA == null) return 1;
            if (dateB == null) return -1;
            return dateA.compareTo(dateB);
        });
        
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_calendar_event, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarItem item = items.get(position);
        holder.bind(item, listener);
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivEventTypeIcon;
        private final TextView tvEventTitle;
        private final TextView tvEventTime;
        private final TextView tvEventType;
        private final ImageView ivSyncStatus;
        private final View viewColorIndicator;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventTypeIcon = itemView.findViewById(R.id.ivEventTypeIcon);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventTime = itemView.findViewById(R.id.tvEventTime);
            tvEventType = itemView.findViewById(R.id.tvEventType);
            ivSyncStatus = itemView.findViewById(R.id.ivSyncStatus);
            viewColorIndicator = itemView.findViewById(R.id.viewColorIndicator);
        }
        
        public void bind(CalendarItem item, OnItemClickListener listener) {
            if (item.isTask()) {
                bindTask(item.getTask(), listener);
            } else {
                bindEvent(item.getEvent(), listener);
            }
        }
        
        private void bindTask(Task task, OnItemClickListener listener) {
            // Set title
            tvEventTitle.setText(task.getTitle() != null ? task.getTitle() : "Task");
            
            // Format time
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            if (task.getDueAt() != null) {
                tvEventTime.setText("⏰ " + timeFormat.format(task.getDueAt()));
            } else if (task.getStartAt() != null) {
                tvEventTime.setText("📅 " + timeFormat.format(task.getStartAt()));
            } else {
                tvEventTime.setText("--:--");
            }
            
            // Set type with task priority
            String typeText = "🟠 Task";
            if (task.getPriority() != null) {
                switch (task.getPriority()) {
                    case HIGH:
                        typeText = "🟠 Task • High Priority";
                        break;
                    case MEDIUM:
                        typeText = "🟠 Task • Medium Priority";
                        break;
                    case LOW:
                        typeText = "🟠 Task • Low Priority";
                        break;
                }
            }
            tvEventType.setText(typeText);
            
            // Set ORANGE color for tasks
            int taskColor = 0xFFFF9800; // Orange
            if (task.getPriority() == Task.TaskPriority.HIGH) {
                taskColor = 0xFFFF5722; // Red-Orange for high priority
            }
            
            ivEventTypeIcon.setImageResource(R.drawable.square_ic);
            ivEventTypeIcon.setColorFilter(taskColor);
            
            // Set color indicator
            if (viewColorIndicator != null) {
                viewColorIndicator.setBackgroundColor(taskColor);
            }
            
            // Show sync status if task is synced to calendar
            if (task.hasCalendarEvent()) {
                ivSyncStatus.setVisibility(View.VISIBLE);
                ivSyncStatus.setColorFilter(0xFF4CAF50); // Green for synced
            } else {
                ivSyncStatus.setVisibility(View.GONE);
            }
            
            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });
        }
        
        private void bindEvent(CalendarEvent event, OnItemClickListener listener) {
            // Set title
            tvEventTitle.setText(event.getTitle() != null ? event.getTitle() : "Event");
            
            // Format time
            if (event.getStartAt() != null && !event.getStartAt().isEmpty()) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Date startDate = inputFormat.parse(event.getStartAt());
                    tvEventTime.setText("📅 " + timeFormat.format(startDate));
                } catch (Exception e) {
                    tvEventTime.setText(event.getStartAt());
                }
            } else {
                tvEventTime.setText("--:--");
            }
            
            // Set type
            String typeText = "🟢 Event";
            if (event.getDescription() != null) {
                if (event.getDescription().contains("meeting")) {
                    typeText = "🟢 Meeting";
                } else if (event.getDescription().contains("deadline")) {
                    typeText = "🟢 Deadline";
                }
            }
            tvEventType.setText(typeText);
            
            // Set GREEN color for events
            int eventColor = 0xFF4CAF50; // Green
            
            ivEventTypeIcon.setImageResource(R.drawable.ic_google_calendar);
            ivEventTypeIcon.setColorFilter(eventColor);
            
            // Set color indicator
            if (viewColorIndicator != null) {
                viewColorIndicator.setBackgroundColor(eventColor);
            }
            
            // Show sync status
            if (event.getGoogleEventId() != null) {
                ivSyncStatus.setVisibility(View.VISIBLE);
                ivSyncStatus.setColorFilter(eventColor);
            } else {
                ivSyncStatus.setVisibility(View.GONE);
            }
            
            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });
        }
    }
}
