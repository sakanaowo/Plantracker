package com.example.tralalero.feature.home.ui.Home.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tralalero.R;
import com.example.tralalero.domain.model.ProjectEvent;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * RecyclerView Adapter for displaying project events
 */
public class ProjectEventAdapter extends RecyclerView.Adapter<ProjectEventAdapter.ViewHolder> {
    private List<ProjectEvent> events = new ArrayList<>();
    private OnEventClickListener onEventClickListener;
    private OnJoinMeetingClickListener onJoinMeetingClickListener;
    private OnCopyLinkClickListener onCopyLinkClickListener;
    private OnMenuClickListener onMenuClickListener;
    
    public interface OnEventClickListener {
        void onEventClick(ProjectEvent event);
    }
    
    public interface OnJoinMeetingClickListener {
        void onJoinMeeting(ProjectEvent event);
    }
    
    public interface OnCopyLinkClickListener {
        void onCopyLink(ProjectEvent event);
    }
    
    public interface OnMenuClickListener {
        void onMenuClick(ProjectEvent event, View anchorView);
    }
    
    public void setEvents(List<ProjectEvent> events) {
        this.events = events != null ? events : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setOnEventClickListener(OnEventClickListener listener) {
        this.onEventClickListener = listener;
    }
    
    public void setOnJoinMeetingClickListener(OnJoinMeetingClickListener listener) {
        this.onJoinMeetingClickListener = listener;
    }
    
    public void setOnCopyLinkClickListener(OnCopyLinkClickListener listener) {
        this.onCopyLinkClickListener = listener;
    }
    
    public void setOnMenuClickListener(OnMenuClickListener listener) {
        this.onMenuClickListener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_project_event, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProjectEvent event = events.get(position);
        holder.bind(event);
    }
    
    @Override
    public int getItemCount() {
        return events.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventDay;
        TextView tvEventMonth;
        TextView tvEventTitle;
        TextView tvEventTime;
        TextView tvEventAttendees;
        TextView tvMeetLink;
        TextView tvEventStatus;
        LinearLayout layoutDateBox;
        LinearLayout layoutEventActions;
        LinearLayout layoutMeetLink;
        Button btnJoinMeeting;
        Button btnViewDetails;
        ImageButton btnCopyMeetLink;
        ImageButton btnEventMenu;
        
        private boolean isExpanded = false;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventDay = itemView.findViewById(R.id.tvEventDay);
            tvEventMonth = itemView.findViewById(R.id.tvEventMonth);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventTime = itemView.findViewById(R.id.tvEventTime);
            tvEventAttendees = itemView.findViewById(R.id.tvEventAttendees);
            tvMeetLink = itemView.findViewById(R.id.tvMeetLink);
            tvEventStatus = itemView.findViewById(R.id.tvEventStatus);
            layoutDateBox = itemView.findViewById(R.id.layoutDateBox);
            layoutEventActions = itemView.findViewById(R.id.layoutEventActions);
            layoutMeetLink = itemView.findViewById(R.id.layoutMeetLink);
            btnJoinMeeting = itemView.findViewById(R.id.btnJoinMeeting);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnCopyMeetLink = itemView.findViewById(R.id.btnCopyMeetLink);
            btnEventMenu = itemView.findViewById(R.id.btnEventMenu);
        }
        
        void bind(ProjectEvent event) {
            if (event == null) return;
            
            // ✅ FIX: Check if event is past using current timestamp (includes time)
            boolean isPastEvent = false;
            if (event.getDate() != null) {
                Date now = new Date();
                isPastEvent = event.getDate().before(now);
            }
            
            // Status badge (HIDDEN - no longer needed)
            if (tvEventStatus != null) {
                tvEventStatus.setVisibility(View.GONE);
            }
            
            // Format date with color coding
            if (event.getDate() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(event.getDate());
                tvEventDay.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
                
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
                tvEventMonth.setText(monthFormat.format(event.getDate()).toUpperCase());
                
                // Keep date box color consistent (blue gradient)
                if (layoutDateBox != null) {
                    layoutDateBox.setBackgroundResource(R.drawable.rounded_background_gradient_blue);
                }
            } else {
                tvEventDay.setText("--");
                tvEventMonth.setText("---");
            }
            
            // Title with icon
            String icon = getEventIcon(event.getType());
            tvEventTitle.setText(icon + " " + event.getTitle());
            
            // Time
            String timeText = event.getTime();
            if (event.getDuration() > 0) {
                timeText += " (" + event.getDuration() + " phút)";
            }
            tvEventTime.setText(timeText);
            
            // Attendees
            int attendeeCount = event.getAttendeeIds() != null ? 
                event.getAttendeeIds().size() : event.getAttendeeCount();
            tvEventAttendees.setText(attendeeCount + " người tham gia");
            
            // Google Meet link
            if (event.getMeetLink() != null && !event.getMeetLink().isEmpty()) {
                layoutMeetLink.setVisibility(View.VISIBLE);
                btnJoinMeeting.setVisibility(View.VISIBLE);
                tvMeetLink.setText(event.getMeetLink());
            } else {
                layoutMeetLink.setVisibility(View.GONE);
                btnJoinMeeting.setVisibility(View.GONE);
            }
            
            // Click to expand/collapse
            itemView.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                layoutEventActions.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            });
            
            // Join meeting
            if (btnJoinMeeting != null) {
                btnJoinMeeting.setOnClickListener(v -> {
                    if (onJoinMeetingClickListener != null) {
                        onJoinMeetingClickListener.onJoinMeeting(event);
                    }
                });
            }
            
            // View details
            if (btnViewDetails != null) {
                btnViewDetails.setOnClickListener(v -> {
                    if (onEventClickListener != null) {
                        onEventClickListener.onEventClick(event);
                    }
                });
            }
            
            // Copy link
            if (btnCopyMeetLink != null) {
                btnCopyMeetLink.setOnClickListener(v -> {
                    if (onCopyLinkClickListener != null) {
                        onCopyLinkClickListener.onCopyLink(event);
                    }
                });
            }
            
            // Menu
            if (btnEventMenu != null) {
                btnEventMenu.setOnClickListener(v -> {
                    if (onMenuClickListener != null) {
                        onMenuClickListener.onMenuClick(event, v);
                    }
                });
            }
        }
        
        private String getEventIcon(String type) {
            if (type == null) return "📌";
            
            switch (type) {
                case "MEETING": return "📋";
                case "MILESTONE": return "🎯";
                default: return "📌";
            }
        }
    }
}
