package com.example.tralalero.feature.home.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.tralalero.R;
import com.example.tralalero.domain.model.ActivityLog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ActivityLogAdapter extends RecyclerView.Adapter<ActivityLogAdapter.ActivityLogViewHolder> {

    private List<ActivityLog> activityLogs = new ArrayList<>();
    private Context context;
    private OnInvitationClickListener invitationClickListener;
    private String currentUserId;

    public interface OnInvitationClickListener {
        void onInvitationClick(ActivityLog log);
    }

    public ActivityLogAdapter(Context context) {
        this.context = context;
        
        // Get current user ID from Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            this.currentUserId = user.getUid();
        }
    }

    public void setOnInvitationClickListener(OnInvitationClickListener listener) {
        this.invitationClickListener = listener;
    }

    public void setActivityLogs(List<ActivityLog> activityLogs) {
        this.activityLogs = activityLogs;
        notifyDataSetChanged();
    }

    public void addActivityLogs(List<ActivityLog> newLogs) {
        int startPosition = this.activityLogs.size();
        this.activityLogs.addAll(newLogs);
        notifyItemRangeInserted(startPosition, newLogs.size());
    }

    @NonNull
    @Override
    public ActivityLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_log, parent, false);
        return new ActivityLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityLogViewHolder holder, int position) {
        ActivityLog log = activityLogs.get(position);
        holder.bind(log);
    }

    @Override
    public int getItemCount() {
        return activityLogs.size();
    }

    class ActivityLogViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivUserAvatar;
        private TextView tvUserInitials;
        private TextView tvActivityMessage;
        private TextView tvActivityTime;

        public ActivityLogViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserInitials = itemView.findViewById(R.id.tvUserInitials);
            tvActivityMessage = itemView.findViewById(R.id.tvActivityMessage);
            tvActivityTime = itemView.findViewById(R.id.tvActivityTime);
        }

        public void bind(ActivityLog log) {
            if (log == null) {
                return;
            }
            
            // Check if this is an invitation
            boolean isInvitation = "ADDED".equals(log.getAction()) && "MEMBERSHIP".equals(log.getEntityType());
            
            // Set click listener for invitations
            if (isInvitation) {
                itemView.setOnClickListener(v -> {
                    if (invitationClickListener != null) {
                        invitationClickListener.onInvitationClick(log);
                    }
                });
                itemView.setClickable(true);
                itemView.setFocusable(true);
            } else {
                itemView.setOnClickListener(null);
                itemView.setClickable(false);
                itemView.setFocusable(false);
            }
            
            // Set activity message with improved formatting
            String message = formatActivityMessage(log);
            tvActivityMessage.setText(message);

            // Set time
            String createdAt = log.getCreatedAt();
            tvActivityTime.setText(formatTimeAgo(createdAt));

            // Load user avatar or show initials
            if (log.getUserAvatar() != null && !log.getUserAvatar().isEmpty()) {
                ivUserAvatar.setVisibility(View.VISIBLE);
                tvUserInitials.setVisibility(View.GONE);
                Glide.with(context)
                        .load(log.getUserAvatar())
                        .transform(new CircleCrop())
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .into(ivUserAvatar);
            } else {
                ivUserAvatar.setVisibility(View.GONE);
                tvUserInitials.setVisibility(View.VISIBLE);
                tvUserInitials.setText(log.getUserInitials());
            }
        }
        
        private String formatActivityMessage(ActivityLog log) {
            boolean isSelf = currentUserId != null && currentUserId.equals(log.getUserId());
            String userName = isSelf ? "You" : log.getUserName();
            String action = log.getAction();
            String entityType = log.getEntityType();
            String entityName = log.getEntityName();
            
            switch (action) {
                case "CREATED":
                    if ("TASK".equals(entityType)) {
                        return userName + " created task \"" + entityName + "\"";
                    } else if ("PROJECT".equals(entityType)) {
                        return userName + " created project \"" + entityName + "\"";
                    } else if ("EVENT".equals(entityType)) {
                        return userName + " created event \"" + entityName + "\"";
                    } else if ("BOARD".equals(entityType)) {
                        return userName + " created board \"" + entityName + "\"";
                    }
                    return userName + " created " + entityType.toLowerCase() + " \"" + entityName + "\"";
                
                case "ADDED":
                    if ("MEMBERSHIP".equals(entityType)) {
                        // Backend creates this log when user ACCEPTS invitation
                        // userId is the person who accepted (person who joined)
                        // invitedBy is the person who sent the invitation
                        String invitedBy = log.getInvitedBy();
                        
                        if (isSelf) {
                            // Current user joined the project
                            return "You joined the project";
                        } else if (currentUserId != null && currentUserId.equals(invitedBy)) {
                            // Current user is the person who invited
                            return entityName + " accepted your invitation and joined the project";
                        } else {
                            // Someone else joined
                            return entityName + " joined the project";
                        }
                    }
                    return userName + " added " + entityType.toLowerCase();
                
                case "ASSIGNED":
                    if ("TASK".equals(entityType)) {
                        // Check if current user is the assignee (received the task)
                        String assigneeId = log.getAssigneeId();
                        if (assigneeId != null && assigneeId.equals(currentUserId)) {
                            // Current user received the task
                            if (isSelf) {
                                // User assigned task to themselves
                                return "You assigned yourself task \"" + entityName + "\"";
                            } else {
                                // Someone else assigned task to current user
                                return "You received task \"" + entityName + "\" from " + userName;
                            }
                        } else {
                            // Current user assigned task to someone else, or viewing someone else's assignment
                            if (isSelf) {
                                return "You assigned task \"" + entityName + "\"";
                            }
                            return userName + " assigned task \"" + entityName + "\"";
                        }
                    }
                    return userName + " assigned " + entityName;
                
                case "UPDATED":
                    return userName + " updated " + entityType.toLowerCase() + " \"" + entityName + "\"";
                
                case "DELETED":
                    return userName + " deleted " + entityType.toLowerCase() + " \"" + entityName + "\"";
                
                case "COMMENTED":
                    return userName + " commented on \"" + entityName + "\"";
                
                case "MOVED":
                    return userName + " moved task \"" + entityName + "\"";
                
                case "COMPLETED":
                    return userName + " completed task \"" + entityName + "\"";
                
                case "REOPENED":
                    return userName + " reopened task \"" + entityName + "\"";
                
                default:
                    return userName + " " + action.toLowerCase() + " " + entityType.toLowerCase();
            }
        }

        private String formatTimeAgo(String isoTimestamp) {
            if (isoTimestamp == null || isoTimestamp.isEmpty()) {
                return "Unknown time";
            }
            
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = isoFormat.parse(isoTimestamp);

                if (date == null) {
                    return isoTimestamp;
                }

                long now = System.currentTimeMillis();
                long diffMs = now - date.getTime();
                long diffSec = diffMs / 1000;
                long diffMin = diffSec / 60;
                long diffHour = diffMin / 60;
                long diffDay = diffHour / 24;

                if (diffSec < 60) {
                    return "just now";
                } else if (diffMin < 60) {
                    return diffMin + " minute" + (diffMin > 1 ? "s" : "") + " ago";
                } else if (diffHour < 24) {
                    return diffHour + " hour" + (diffHour > 1 ? "s" : "") + " ago";
                } else if (diffDay < 7) {
                    return diffDay + " day" + (diffDay > 1 ? "s" : "") + " ago";
                } else {
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d 'at' h:mm a", Locale.US);
                    return displayFormat.format(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                // Return a fallback message instead of the raw timestamp
                return "Recently";
            } catch (Exception e) {
                e.printStackTrace();
                return "Unknown time";
            }
        }
    }
}
