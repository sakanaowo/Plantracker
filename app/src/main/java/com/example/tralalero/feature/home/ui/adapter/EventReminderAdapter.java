package com.example.tralalero.feature.home.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.EventReminder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying event reminders
 */
public class EventReminderAdapter extends RecyclerView.Adapter<EventReminderAdapter.ViewHolder> {
    
    private List<EventReminder> reminders = new ArrayList<>();
    private EventReminderActionListener listener;
    
    public interface EventReminderActionListener {
        void onViewEvent(EventReminder reminder);
        void onDismiss(EventReminder reminder);
    }
    
    public EventReminderAdapter(EventReminderActionListener listener) {
        this.listener = listener;
    }
    
    public void setReminders(List<EventReminder> reminders) {
        this.reminders = reminders != null ? reminders : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void updateReminders(List<EventReminder> reminders) {
        setReminders(reminders);
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_event_reminder, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventReminder reminder = reminders.get(position);
        holder.bind(reminder);
    }
    
    @Override
    public int getItemCount() {
        return reminders.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderName;
        TextView tvTimestamp;
        TextView tvEventTitle;
        TextView tvEventDateTime;
        TextView tvProjectName;
        TextView tvMessage;
        Button btnViewEvent;
        Button btnDismiss;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventDateTime = itemView.findViewById(R.id.tvEventDateTime);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            btnViewEvent = itemView.findViewById(R.id.btnViewEvent);
            btnDismiss = itemView.findViewById(R.id.btnDismiss);
        }
        
        void bind(EventReminder reminder) {
            tvSenderName.setText(reminder.getSenderName() + " sent you an event reminder");
            tvEventTitle.setText(reminder.getEventTitle());
            
            // Format date time
            String dateTime = reminder.getEventDate();
            if (reminder.getEventTime() != null && !reminder.getEventTime().isEmpty()) {
                dateTime += " at " + reminder.getEventTime();
            }
            tvEventDateTime.setText(dateTime);
            
            if (reminder.getProjectName() != null && !reminder.getProjectName().isEmpty()) {
                tvProjectName.setText("📋 " + reminder.getProjectName());
                tvProjectName.setVisibility(View.VISIBLE);
            } else {
                tvProjectName.setVisibility(View.GONE);
            }
            
            if (reminder.getMessage() != null && !reminder.getMessage().isEmpty()) {
                tvMessage.setText(reminder.getMessage());
                tvMessage.setVisibility(View.VISIBLE);
            } else {
                tvMessage.setVisibility(View.GONE);
            }
            
            // Format timestamp
            tvTimestamp.setText(getTimeAgo(reminder.getTimestamp()));
            
            // Set click listeners
            btnViewEvent.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewEvent(reminder);
                }
            });
            
            btnDismiss.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDismiss(reminder);
                }
            });
        }
        
        private String getTimeAgo(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;
            
            if (diff < 60000) { // Less than 1 minute
                return "Just now";
            } else if (diff < 3600000) { // Less than 1 hour
                return (diff / 60000) + "m ago";
            } else if (diff < 86400000) { // Less than 1 day
                return (diff / 3600000) + "h ago";
            } else if (diff < 604800000) { // Less than 1 week
                return (diff / 86400000) + "d ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            }
        }
    }
}
