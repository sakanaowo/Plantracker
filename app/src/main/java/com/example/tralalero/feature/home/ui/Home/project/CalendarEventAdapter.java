package com.example.tralalero.feature.home.ui.Home.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.CalendarEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalendarEventAdapter extends RecyclerView.Adapter<CalendarEventAdapter.ViewHolder> {
    
    private List<CalendarEvent> events = new ArrayList<>();
    private OnEventClickListener listener;
    
    public interface OnEventClickListener {
        void onEventClick(CalendarEvent event);
    }
    
    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }
    
    public void setEvents(List<CalendarEvent> events) {
        this.events = events != null ? events : new ArrayList<>();
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
        CalendarEvent event = events.get(position);
        holder.bind(event, listener);
    }
    
    @Override
    public int getItemCount() {
        return events.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivEventTypeIcon;
        private final TextView tvEventTitle;
        private final TextView tvEventTime;
        private final TextView tvEventType;
        private final ImageView ivSyncStatus;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventTypeIcon = itemView.findViewById(R.id.ivEventTypeIcon);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventTime = itemView.findViewById(R.id.tvEventTime);
            tvEventType = itemView.findViewById(R.id.tvEventType);
            ivSyncStatus = itemView.findViewById(R.id.ivSyncStatus);
        }
        
        public void bind(CalendarEvent event, OnEventClickListener listener) {
            tvEventTitle.setText(event.getTitle() != null ? event.getTitle() : "Event");
            
            // Format time from startAt
            if (event.getStartAt() != null && !event.getStartAt().isEmpty()) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    tvEventTime.setText(timeFormat.format(inputFormat.parse(event.getStartAt())));
                } catch (Exception e) {
                    tvEventTime.setText(event.getStartAt());
                }
            } else {
                tvEventTime.setText("--:--");
            }
            
            // Set event type based on description or location
            String typeText = "• Event";
            if (event.getDescription() != null && event.getDescription().contains("meeting")) {
                typeText = "• Meeting";
            } else if (event.getDescription() != null && event.getDescription().contains("deadline")) {
                typeText = "• Deadline";
            }
            tvEventType.setText(typeText);
            
            // Set icon and color
            int iconRes = R.drawable.ic_info;
            int colorRes = 0xFF2196F3; // Blue
            
            if (typeText.contains("Deadline")) {
                iconRes = R.drawable.square_ic;
                colorRes = 0xFFFF5722; // Red
            } else if (typeText.contains("Meeting")) {
                iconRes = R.drawable.ic_people;
                colorRes = 0xFF2196F3; // Blue
            }
            
            ivEventTypeIcon.setImageResource(iconRes);
            ivEventTypeIcon.setColorFilter(colorRes);
            
            // Show/hide sync status based on googleEventId
            ivSyncStatus.setVisibility(event.getGoogleEventId() != null ? View.VISIBLE : View.GONE);
            
            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });
        }
    }
}
