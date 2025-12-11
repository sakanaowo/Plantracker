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
        TextView tvCancellationReason;
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
            tvCancellationReason = itemView.findViewById(R.id.tvCancellationReason);
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
            
            // ✅ Check if event is cancelled
            boolean isCancelled = "CANCELLED".equalsIgnoreCase(event.getStatus());
            
            // ✅ Check if event is past (event end time has passed current time)
            boolean isPastEvent = false;
            if (event.getDate() != null) {
                Date now = new Date();
                // Calculate event end time = start date + duration (in minutes)
                Calendar eventEnd = Calendar.getInstance();
                eventEnd.setTime(event.getDate());
                eventEnd.add(Calendar.MINUTE, event.getDuration());
                isPastEvent = eventEnd.getTime().before(now);
            }
            
            // Status badge - show CANCELLED badge if cancelled
            if (tvEventStatus != null) {
                if (isCancelled) {
                    tvEventStatus.setVisibility(View.VISIBLE);
                    tvEventStatus.setText("CANCELLED");
                    tvEventStatus.setBackgroundResource(R.drawable.badge_cancelled);
                    tvEventStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
                } else {
                    tvEventStatus.setVisibility(View.GONE);
                }
            }
            
            // Format date with color coding
            if (event.getDate() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(event.getDate());
                tvEventDay.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
                
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
                tvEventMonth.setText(monthFormat.format(event.getDate()).toUpperCase());
                
                // Change date box color: gray for cancelled/past, blue for active
                if (layoutDateBox != null) {
                    if (isCancelled || isPastEvent) {
                        layoutDateBox.setBackgroundResource(R.drawable.rounded_background_gray);
                    } else {
                        layoutDateBox.setBackgroundResource(R.drawable.rounded_background_gradient_blue);
                    }
                }
            } else {
                tvEventDay.setText("--");
                tvEventMonth.setText("---");
            }
            
            // Title with icon and strikethrough for cancelled/past events
            String icon = getEventIcon(event.getType());
            tvEventTitle.setText(icon + " " + event.getTitle());
            if (isCancelled || isPastEvent) {
                tvEventTitle.setPaintFlags(tvEventTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                tvEventTitle.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            } else {
                tvEventTitle.setPaintFlags(tvEventTitle.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                tvEventTitle.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
            }
            
            // Time
            String timeText = event.getTime();
            if (event.getDuration() > 0) {
                timeText += " (" + event.getDuration() + " phút)";
            }
            tvEventTime.setText(timeText);
            if (isCancelled || isPastEvent) {
                tvEventTime.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            } else {
                tvEventTime.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
            }
            
            // Attendees
            int attendeeCount = event.getAttendeeIds() != null ? 
                event.getAttendeeIds().size() : event.getAttendeeCount();
            tvEventAttendees.setText(attendeeCount + " participants");
            
            // Google Meet link
            if (event.getMeetLink() != null && !event.getMeetLink().isEmpty()) {
                layoutMeetLink.setVisibility(View.VISIBLE);
                btnJoinMeeting.setVisibility(View.VISIBLE);
                tvMeetLink.setText(event.getMeetLink());
                
                // Disable join button for cancelled events
                if (isCancelled) {
                    btnJoinMeeting.setEnabled(false);
                    btnJoinMeeting.setAlpha(0.5f);
                } else {
                    btnJoinMeeting.setEnabled(true);
                    btnJoinMeeting.setAlpha(1.0f);
                }
            } else {
                layoutMeetLink.setVisibility(View.GONE);
                btnJoinMeeting.setVisibility(View.GONE);
            }
            
            // Show cancellation reason if event is cancelled
            if (tvCancellationReason != null) {
                if (isCancelled && event.getCancellationReason() != null && !event.getCancellationReason().isEmpty()) {
                    tvCancellationReason.setVisibility(View.VISIBLE);
                    tvCancellationReason.setText("❌ Lý do hủy: " + event.getCancellationReason());
                } else {
                    tvCancellationReason.setVisibility(View.GONE);
                }
            }
            
            // Click to expand/collapse
            itemView.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                layoutEventActions.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            });
            
            // Join meeting
            if (btnJoinMeeting != null) {
                btnJoinMeeting.setOnClickListener(v -> {
                    if (onJoinMeetingClickListener != null && !isCancelled) {
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
