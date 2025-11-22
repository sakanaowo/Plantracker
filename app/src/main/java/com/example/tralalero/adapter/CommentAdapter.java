package com.example.tralalero.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.TaskComment;
import com.example.tralalero.data.remote.api.UserApiService;
import com.example.tralalero.data.remote.dto.user.UserDTO;
import com.example.tralalero.data.cache.UserCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    
    private List<TaskComment> comments = new ArrayList<>();
    private OnCommentClickListener listener;
    private UserApiService userApiService;
    private UserCache userCache;
    private Set<String> fetchingUsers = new HashSet<>();
    private android.content.Context context;
    
    public interface OnCommentClickListener {
        void onOptionsClick(TaskComment comment, int position);
        void onCommentClick(TaskComment comment);
    }
    
    public CommentAdapter(OnCommentClickListener listener, UserApiService userApiService) {
        this.listener = listener;
        this.userApiService = userApiService;
        this.userCache = UserCache.getInstance();
    }
    
    public void setContext(android.content.Context context) {
        this.context = context;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(comments.get(position));
    }
    
    @Override
    public int getItemCount() {
        return comments.size();
    }
    
    public void setComments(List<TaskComment> comments) {
        this.comments = comments != null ? comments : new ArrayList<>();
        fetchingUsers.clear();
        
        // Fetch user info for all unique users once
        Set<String> uniqueUsers = new HashSet<>();
        for (TaskComment comment : this.comments) {
            String userId = comment.getUserId();
            if (userId != null && !userId.isEmpty() && !userCache.hasUser(userId)) {
                uniqueUsers.add(userId);
            }
        }
        
        // Fetch info for users not in cache
        for (String userId : uniqueUsers) {
            fetchUserInfo(userId);
        }
        
        notifyDataSetChanged();
    }
    
    public List<TaskComment> getComments() {
        return new ArrayList<>(comments); // Return copy to prevent external modification
    }
    
    public void addComment(TaskComment comment) {
        comments.add(0, comment);
        notifyItemInserted(0);
    }
    
    public void removeComment(int position) {
        if (position >= 0 && position < comments.size()) {
            comments.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    public void updateComment(int position, TaskComment comment) {
        if (position >= 0 && position < comments.size()) {
            comments.set(position, comment);
            notifyItemChanged(position);
        }
    }
    
    /**
     * Clear user cache and force refresh avatars
     */
    public void refreshUserAvatars(String userId) {
        // Clear from cache
        if (userCache.hasUser(userId)) {
            userCache.removeUser(userId);
        }
        
        // Clear from fetching set
        fetchingUsers.remove(userId);
        
        // Force refresh all items for this user
        for (int i = 0; i < comments.size(); i++) {
            if (userId.equals(comments.get(i).getUserId())) {
                notifyItemChanged(i);
            }
        }
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivUserAvatar;
        private final TextView tvUserInitials;
        private final TextView tvUserName;
        private final TextView tvCommentTime;
        private final TextView tvCommentBody;
        private final ImageButton btnCommentOptions;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserInitials = itemView.findViewById(R.id.tvUserInitials);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            tvCommentBody = itemView.findViewById(R.id.tvCommentBody);
            btnCommentOptions = itemView.findViewById(R.id.btnCommentOptions);
        }
        
        public void bind(TaskComment comment) {
            if (comment == null) {
                android.util.Log.e("CommentAdapter", "❌ Comment is null!");
                return;
            }
            
            android.util.Log.d("CommentAdapter", "=== BINDING COMMENT ===");
            android.util.Log.d("CommentAdapter", "Comment ID: " + comment.getId());
            android.util.Log.d("CommentAdapter", "UserName: " + comment.getUserName());
            android.util.Log.d("CommentAdapter", "AvatarUrl: " + comment.getUserAvatarUrl());
            android.util.Log.d("CommentAdapter", "CreatedAt Date: " + comment.getCreatedAt());
            
            // Set user name
            String userName = getUserDisplayName(comment);
            tvUserName.setText(userName);
            android.util.Log.d("CommentAdapter", "✅ Set userName: " + userName);
            
            // Set user avatar
            loadUserAvatar(comment);
            
            // Set time (handle null date)
            String timeText = "";
            if (comment.getCreatedAt() != null) {
                timeText = getRelativeTime(comment.getCreatedAt());
                android.util.Log.d("CommentAdapter", "✅ Set time: " + timeText);
            } else {
                android.util.Log.e("CommentAdapter", "❌ CreatedAt is null!");
                timeText = "Unknown time";
            }
            tvCommentTime.setText(timeText);
            
            // Set body
            tvCommentBody.setText(comment.getBody());
            
            // Options button
            btnCommentOptions.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onOptionsClick(comment, position);
                    }
                }
            });
            
            // Item click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCommentClick(comment);
                }
            });
        }
        
        /**
         * Load user avatar - EXACTLY like ActivityLogAdapter
         */
        private void loadUserAvatar(TaskComment comment) {
            String userId = comment.getUserId();
            android.util.Log.d("CommentAdapter", "🖼️ loadUserAvatar for userId: " + userId);
            
            // Get avatar URL - prioritize comment data first
            String avatarUrl = comment.getUserAvatarUrl();
            android.util.Log.d("CommentAdapter", "Avatar from comment: " + avatarUrl);
            
            // If no avatar from comment, try cache
            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                if (userId != null && userCache.hasUser(userId)) {
                    UserDTO user = userCache.getUser(userId);
                    avatarUrl = user.getAvatarUrl();
                    android.util.Log.d("CommentAdapter", "Avatar from cache: " + avatarUrl);
                }
            }
            
            // If no avatar from cache, try Firebase for current user
            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                try {
                    com.google.firebase.auth.FirebaseUser currentUser = 
                        com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null && userId != null && userId.equals(currentUser.getUid())) {
                        android.net.Uri photoUrl = currentUser.getPhotoUrl();
                        if (photoUrl != null) {
                            avatarUrl = photoUrl.toString();
                            android.util.Log.d("CommentAdapter", "Avatar from Firebase: " + avatarUrl);
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("CommentAdapter", "Error checking Firebase: " + e.getMessage());
                }
            }
            
            // Load avatar or show initials (EXACTLY like ActivityLogAdapter)
            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                // Show ImageView, hide initials
                ivUserAvatar.setVisibility(android.view.View.VISIBLE);
                tvUserInitials.setVisibility(android.view.View.GONE);
                
                android.util.Log.d("CommentAdapter", "✅ Loading avatar with Glide");
                
                // Use context from adapter (like ActivityLogAdapter uses 'context')
                android.content.Context ctx = context != null ? context : itemView.getContext();
                com.bumptech.glide.Glide.with(ctx)
                    .load(avatarUrl.trim())
                    .transform(new com.bumptech.glide.load.resource.bitmap.CircleCrop())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(ivUserAvatar);
            } else {
                // Hide ImageView, show initials
                ivUserAvatar.setVisibility(android.view.View.GONE);
                tvUserInitials.setVisibility(android.view.View.VISIBLE);
                
                // Use comment's getUserInitials() method (like ActivityLog)
                String initials = comment.getUserInitials();
                tvUserInitials.setText(initials);
                
                android.util.Log.d("CommentAdapter", "✅ Showing initials: " + initials);
            }
            
            // Fetch user info if not in cache
            if (userId != null && !userCache.hasUser(userId) && !fetchingUsers.contains(userId)) {
                android.util.Log.d("CommentAdapter", "🌐 Fetching user info");
                fetchUserInfo(userId);
            }
        }
        
        // Helper method: Format relative time
        private String getRelativeTime(java.util.Date createdAt) {
            if (createdAt == null) return "";
            
            long diffMillis = System.currentTimeMillis() - createdAt.getTime();
            long diffSeconds = diffMillis / 1000;
            long diffMinutes = diffSeconds / 60;
            long diffHours = diffMinutes / 60;
            long diffDays = diffHours / 24;
            
            if (diffSeconds < 60) {
                return "Just now";
            } else if (diffMinutes < 60) {
                return diffMinutes + (diffMinutes == 1 ? " minute ago" : " minutes ago");
            } else if (diffHours < 24) {
                return diffHours + (diffHours == 1 ? " hour ago" : " hours ago");
            } else if (diffDays < 7) {
                return diffDays + (diffDays == 1 ? " day ago" : " days ago");
            } else if (diffDays < 30) {
                long weeks = diffDays / 7;
                return weeks + (weeks == 1 ? " week ago" : " weeks ago");
            } else if (diffDays < 365) {
                long months = diffDays / 30;
                return months + (months == 1 ? " month ago" : " months ago");
            } else {
                long years = diffDays / 365;
                return years + (years == 1 ? " year ago" : " years ago");
            }
        }
        
        /**
         * Get user display name - PRIORITIZE comment data, then cache, then Firebase, then fallback
         */
        private String getUserDisplayName(TaskComment comment) {
            // FIRST: Check if comment already has userName (from backend response)
            String userName = comment.getUserName();
            if (userName != null && !userName.trim().isEmpty()) {
                android.util.Log.d("CommentAdapter", "✅ Using userName from comment: " + userName);
                return userName;
            }
            
            // SECOND: Check cache
            String userId = comment.getUserId();
            if (userId == null || userId.isEmpty()) {
                return "Unknown User";
            }
            
            if (userCache.hasUser(userId)) {
                UserDTO user = userCache.getUser(userId);
                String name = user.getName();
                if (name != null && !name.isEmpty()) {
                    android.util.Log.d("CommentAdapter", "✅ Using userName from cache: " + name);
                    return name;
                }
            }
            
            // THIRD: Check Firebase current user
            try {
                com.google.firebase.auth.FirebaseUser currentUser = 
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null && userId.equals(currentUser.getUid())) {
                    String displayName = currentUser.getDisplayName();
                    if (displayName != null && !displayName.isEmpty()) {
                        android.util.Log.d("CommentAdapter", "✅ Using displayName from Firebase: " + displayName);
                        return displayName;
                    }
                    String email = currentUser.getEmail();
                    if (email != null) {
                        String emailName = email.split("@")[0];
                        android.util.Log.d("CommentAdapter", "✅ Using email name from Firebase: " + emailName);
                        return emailName;
                    }
                }
            } catch (Exception ignored) {}
            
            // Fallback
            android.util.Log.d("CommentAdapter", "⚠️ Using fallback: User");
            return "User";
        }
    }
    
    /**
     * Fetch user info from API and update cache
     */
    private void fetchUserInfo(String userId) {
            android.util.Log.d("CommentAdapter", "🌐 === fetchUserInfo START ===");
            android.util.Log.d("CommentAdapter", "UserId: " + userId);
            
            // Prevent duplicate fetches
            if (fetchingUsers.contains(userId) || userApiService == null) {
                android.util.Log.w("CommentAdapter", "❌ Already fetching or no API service");
                return;
            }
            
            fetchingUsers.add(userId);
            android.util.Log.d("CommentAdapter", "✅ Added to fetching set");
            
            userApiService.getUserById(userId).enqueue(new Callback<UserDTO>() {
                @Override
                public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                    android.util.Log.d("CommentAdapter", "📡 API Response code: " + response.code());
                    fetchingUsers.remove(userId);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        UserDTO user = response.body();
                        android.util.Log.d("CommentAdapter", "✅ Got user: " + user.getName());
                        android.util.Log.d("CommentAdapter", "📸 Avatar URL: " + user.getAvatarUrl());
                        
                        // Cache user
                        userCache.putUser(userId, user);
                        
                        // Update only specific items with this userId
                        for (int i = 0; i < comments.size(); i++) {
                            if (userId.equals(comments.get(i).getUserId())) {
                                android.util.Log.d("CommentAdapter", "♻️ Updating item at position: " + i);
                                notifyItemChanged(i);
                            }
                        }
                    } else {
                        android.util.Log.e("CommentAdapter", "❌ API failed: " + response.code());
                    }
                }
                
                @Override
                public void onFailure(Call<UserDTO> call, Throwable t) {
                    android.util.Log.e("CommentAdapter", "❌ API error: " + t.getMessage());
                    fetchingUsers.remove(userId);
                }
            });
        }
    }

