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
    private OnActivityClickListener activityClickListener;
    private String currentUserId;

    public interface OnInvitationClickListener {
        void onInvitationClick(ActivityLog log);
    }
    
    public interface OnActivityClickListener {
        void onTaskClick(String taskId, String projectId);
        void onEventClick(String eventId, String projectId);
        void onProjectClick(String projectId);
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
    
    public void setOnActivityClickListener(OnActivityClickListener listener) {
        this.activityClickListener = listener;
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
            
            // Check if this is a clickable entity (task, event, project)
            String entityType = log.getEntityType();
            String entityId = log.getEntityId();
            String projectId = log.getProjectId();
            boolean isClickable = isInvitation || 
                ("TASK".equals(entityType) && entityId != null) ||
                ("EVENT".equals(entityType) && entityId != null) ||
                ("PROJECT".equals(entityType) && entityId != null);
            
            android.util.Log.d("ActivityLogAdapter", "Bind log - EntityType: " + entityType + ", EntityId: " + entityId + ", ProjectId: " + projectId + ", isClickable: " + isClickable);
            
            // Set click listener
            if (isClickable) {
                itemView.setOnClickListener(v -> {
                    android.util.Log.d("ActivityLogAdapter", "Item clicked - EntityType: " + entityType + ", EntityId: " + entityId + ", ProjectId: " + projectId);
                    
                    if (isInvitation && invitationClickListener != null) {
                        invitationClickListener.onInvitationClick(log);
                    } else if (activityClickListener != null) {
                        if ("TASK".equals(entityType) && entityId != null && projectId != null) {
                            android.util.Log.d("ActivityLogAdapter", "Navigating to task: " + entityId);
                            activityClickListener.onTaskClick(entityId, projectId);
                        } else if ("EVENT".equals(entityType) && entityId != null && projectId != null) {
                            android.util.Log.d("ActivityLogAdapter", "Navigating to event: " + entityId);
                            activityClickListener.onEventClick(entityId, projectId);
                        } else if ("PROJECT".equals(entityType) && entityId != null) {
                            android.util.Log.d("ActivityLogAdapter", "Navigating to project: " + entityId);
                            activityClickListener.onProjectClick(entityId);
                        } else {
                            android.util.Log.w("ActivityLogAdapter", "Cannot navigate - missing data. EntityType: " + entityType + ", EntityId: " + entityId + ", ProjectId: " + projectId);
                        }
                    } else {
                        android.util.Log.w("ActivityLogAdapter", "Activity click listener is null");
                    }
                });
                itemView.setClickable(true);
                itemView.setFocusable(true);
            } else {
                itemView.setOnClickListener(null);
                itemView.setClickable(false);
                itemView.setFocusable(false);
            }
            
            // Set activity message using domain model's formatted message
            String message = log.getFormattedMessage();
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
        
        private String getMetadataValue(Object metadata, String key) {
            try {
                if (metadata instanceof java.util.Map) {
                    java.util.Map<String, Object> metadataMap = (java.util.Map<String, Object>) metadata;
                    Object value = metadataMap.get(key);
                    return value != null ? value.toString() : null;
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
            return null;
        }

        private String formatActivityMessage(ActivityLog log) {
            boolean isSelf = currentUserId != null && currentUserId.equals(log.getUserId());
            String userName = isSelf ? "You" : log.getUserName();
            String action = log.getAction();
            String entityType = log.getEntityType();
            String entityName = log.getEntityName();
            
            // Get projectName from ActivityLog model (populated from backend)
            String projectName = log.getProjectName();
            
            // Null safety for entityName
            if (entityName == null) {
                entityName = "";
            }
            
            switch (action) {
                case "CREATED":
                    if ("TASK".equals(entityType)) {
                        String taskMsg = userName + " created task \"" + entityName + "\"";
                        if (projectName != null && !projectName.isEmpty()) {
                            taskMsg += " in project \"" + projectName + "\"";
                        }
                        return taskMsg;
                    } else if ("PROJECT".equals(entityType)) {
                        return userName + " created project \"" + entityName + "\"";
                    } else if ("EVENT".equals(entityType)) {
                        String eventMsg = userName + " created event \"" + entityName + "\"";
                        if (projectName != null && !projectName.isEmpty()) {
                            eventMsg += " in project \"" + projectName + "\"";
                        }
                        return eventMsg;
                    } else if ("BOARD".equals(entityType)) {
                        return userName + " created board \"" + entityName + "\"";
                    }
                    return userName + " created " + entityType.toLowerCase() + (entityName.isEmpty() ? "" : " \"" + entityName + "\"");
                
                case "ADDED":
                    if ("MEMBERSHIP".equals(entityType)) {
                        // ✅ NEW FORMAT: entityName is now the PROJECT name
                        // memberName is in metadata
                        String membershipProjectName = entityName;
                        String memberName = getMetadataValue(log.getMetadata(), "memberName");
                        String invitationType = getMetadataValue(log.getMetadata(), "type");
                        
                        // ⚠️ BACKWARD COMPATIBILITY: Old data had memberName in entityName
                        // If membershipProjectName looks like a person name or entityName == memberName, 
                        // it's probably old data where entityName was memberName
                        boolean isOldFormat = false;
                        if (memberName != null && membershipProjectName != null && 
                            (membershipProjectName.equals(memberName) || membershipProjectName.contains(memberName))) {
                            // Old format detected - entityName was member name
                            isOldFormat = true;
                            // Try to get project name from newValue (old structure)
                            String oldProjectName = null;
                            try {
                                if (log.getNewValue() instanceof java.util.Map) {
                                    java.util.Map<String, Object> newVal = (java.util.Map<String, Object>) log.getNewValue();
                                    Object pName = newVal.get("projectName");
                                    if (pName != null) oldProjectName = pName.toString();
                                }
                            } catch (Exception e) {
                                // Ignore
                            }
                            if (oldProjectName != null && !oldProjectName.isEmpty()) {
                                membershipProjectName = oldProjectName;
                            }
                        }
                        
                        if ("INVITATION_ACCEPTED".equals(invitationType)) {
                            // Someone accepted invitation - they joined the project
                            if (isSelf) {
                                return "You joined project \"" + membershipProjectName + "\"";
                            } else {
                                // The person who joined is the userName (not memberName)
                                return userName + " joined project \"" + membershipProjectName + "\"";
                            }
                        } else {
                            // Someone invited a new member
                            String invitee = memberName != null ? memberName : "a member";
                            if (isSelf) {
                                return "You invited " + invitee + " to project \"" + membershipProjectName + "\"";
                            } else {
                                return userName + " invited " + invitee + " to project \"" + membershipProjectName + "\"";
                            }
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
                    if ("TASK".equals(entityType)) {
                        String updateMsg = userName + " updated task \"" + entityName + "\"";
                        if (projectName != null && !projectName.isEmpty()) {
                            updateMsg += " in project \"" + projectName + "\"";
                        }
                        return updateMsg;
                    } else if ("PROJECT".equals(entityType)) {
                        return userName + " updated project \"" + entityName + "\"";
                    } else if ("EVENT".equals(entityType)) {
                        String updateEventMsg = userName + " updated event \"" + entityName + "\"";
                        if (projectName != null && !projectName.isEmpty()) {
                            updateEventMsg += " in project \"" + projectName + "\"";
                        }
                        return updateEventMsg;
                    } else if ("MEMBERSHIP".equals(entityType)) {
                        // ✅ Role change: entityName is PROJECT name, memberName in metadata
                        String roleProjectName = entityName;
                        String memberName = getMetadataValue(log.getMetadata(), "memberName");
                        String oldRole = getMetadataValue(log.getMetadata(), "oldRole");
                        String newRole = getMetadataValue(log.getMetadata(), "newRole");
                        
                        String roleChange = "";
                        if (oldRole != null && newRole != null) {
                            roleChange = " from " + oldRole + " to " + newRole;
                        }
                        
                        // Check if current user's role was changed
                        if (currentUserId != null && memberName != null) {
                            // If we can get the member's user info, check if it's the current user
                            // For now, check if it's self by comparing names
                            if (isSelf && userName.equals(memberName)) {
                                // User's own role was changed by someone else
                                return userName + " changed your role" + roleChange + " in project \"" + roleProjectName + "\"";
                            }
                        }
                        
                        String memberInfo = memberName != null ? memberName : "a member";
                        
                        if (isSelf) {
                            // User changed someone's role
                            return "You changed " + memberInfo + "'s role" + roleChange + " in project \"" + roleProjectName + "\"";
                        } else {
                            return userName + " changed " + memberInfo + "'s role" + roleChange + " in project \"" + roleProjectName + "\"";
                        }
                    }
                    return userName + " updated " + entityType.toLowerCase() + (!entityName.isEmpty() ? " \"" + entityName + "\"" : "");
                
                case "DELETED":
                    if ("PROJECT".equals(entityType)) {
                        return userName + " deleted project \"" + entityName + "\"";
                    } else if ("EVENT".equals(entityType)) {
                        String deleteEventMsg = userName + " deleted event \"" + entityName + "\"";
                        if (projectName != null && !projectName.isEmpty()) {
                            deleteEventMsg += " in project \"" + projectName + "\"";
                        }
                        return deleteEventMsg;
                    } else if ("TASK".equals(entityType)) {
                        String deleteTaskMsg = userName + " deleted task \"" + entityName + "\"";
                        if (projectName != null && !projectName.isEmpty()) {
                            deleteTaskMsg += " in project \"" + projectName + "\"";
                        }
                        return deleteTaskMsg;
                    }
                    return userName + " deleted " + entityType.toLowerCase() + (!entityName.isEmpty() ? " \"" + entityName + "\"" : "");
                
                case "REMOVED":
                    if ("MEMBERSHIP".equals(entityType)) {
                        // ✅ Member removed/left: entityName is PROJECT name, memberName in metadata
                        String removedProjectName = entityName;
                        String memberName = getMetadataValue(log.getMetadata(), "memberName");
                        String memberId = getMetadataValue(log.getMetadata(), "memberId");
                        
                        // Check if the action performer is the same as the member being removed
                        boolean selfLeft = log.getUserId() != null && log.getUserId().equals(memberId);
                        
                        if (isSelf && selfLeft) {
                            // Current user left the project themselves
                            return "You left project \"" + removedProjectName + "\"";
                        } else if (isSelf) {
                            // Current user removed someone else
                            String removedMember = memberName != null ? memberName : "a member";
                            return "You removed " + removedMember + " from project \"" + removedProjectName + "\"";
                        } else if (selfLeft) {
                            // Someone else left the project
                            return userName + " left project \"" + removedProjectName + "\"";
                        } else {
                            // Someone removed another member
                            String removedMember = memberName != null ? memberName : "a member";
                            return userName + " removed " + removedMember + " from project \"" + removedProjectName + "\"";
                        }
                    }
                    return userName + " removed " + entityType.toLowerCase() + (!entityName.isEmpty() ? " \"" + entityName + "\"" : "");

                case "ATTACHED":
                    if ("ATTACHMENT".equals(entityType)) {
                        // Try to get filename from metadata
                        String fileName = getMetadataValue(log.getMetadata(), "fileName");
                        if (fileName != null) {
                            return userName + " added attachment \"" + fileName + "\"";
                        }
                        return userName + " added an attachment";
                    }
                    return userName + " attached " + entityType.toLowerCase();

                case "COMMENTED":
                    if (!entityName.isEmpty()) {
                        return userName + " commented on \"" + entityName + "\"";
                    }
                    return userName + " added a comment";
                
                case "MOVED":
                    // Try to get board names from oldValue/newValue
                    String fromBoardName = null;
                    String toBoardName = null;
                    try {
                        if (log.getOldValue() instanceof java.util.Map) {
                            java.util.Map<String, Object> oldVal = (java.util.Map<String, Object>) log.getOldValue();
                            Object boardName = oldVal.get("boardName");
                            if (boardName != null) fromBoardName = boardName.toString();
                        }
                        if (log.getNewValue() instanceof java.util.Map) {
                            java.util.Map<String, Object> newVal = (java.util.Map<String, Object>) log.getNewValue();
                            Object boardName = newVal.get("boardName");
                            if (boardName != null) toBoardName = boardName.toString();
                        }
                    } catch (Exception e) {
                        // Ignore parsing errors
                    }

                    if (fromBoardName != null && toBoardName != null) {
                        return userName + " moved task \"" + entityName + "\" from " + fromBoardName + " to " + toBoardName;
                    } else if (!entityName.isEmpty()) {
                        return userName + " moved task \"" + entityName + "\"";
                    }
                    return userName + " moved a task";
                
                case "COMPLETED":
                    if (!entityName.isEmpty()) {
                        return userName + " completed task \"" + entityName + "\"";
                    }
                    return userName + " completed a task";
                
                case "REOPENED":
                    if (!entityName.isEmpty()) {
                        return userName + " reopened task \"" + entityName + "\"";
                    }
                    return userName + " reopened a task";
                
                case "UNASSIGNED":
                    if (!entityName.isEmpty()) {
                        return userName + " unassigned task \"" + entityName + "\"";
                    }
                    return userName + " unassigned a task";
                
                case "CHECKED":
                    if ("TASK_CHECKLIST_ITEM".equals(entityType) && !entityName.isEmpty()) {
                        return userName + " checked \"" + entityName + "\"";
                    }
                    return userName + " checked an item";
                
                case "UNCHECKED":
                    if ("TASK_CHECKLIST_ITEM".equals(entityType) && !entityName.isEmpty()) {
                        return userName + " unchecked \"" + entityName + "\"";
                    }
                    return userName + " unchecked an item";

                default:
                    if (!entityName.isEmpty()) {
                        return userName + " " + action.toLowerCase() + " " + entityType.toLowerCase() + " \"" + entityName + "\"";
                    }
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
