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
    private Set<String> fetchingUsers = new HashSet<>(); // Track ongoing API calls
    
    public interface OnCommentClickListener {
        void onOptionsClick(TaskComment comment, int position);
        void onCommentClick(TaskComment comment);
    }
    
    public CommentAdapter(OnCommentClickListener listener, UserApiService userApiService) {
        this.listener = listener;
        this.userApiService = userApiService;
        this.userCache = UserCache.getInstance();
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
        private final TextView tvUserName;
        private final TextView tvCommentTime;
        private final TextView tvCommentBody;
        private final ImageButton btnCommentOptions;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            tvCommentBody = itemView.findViewById(R.id.tvCommentBody);
            btnCommentOptions = itemView.findViewById(R.id.btnCommentOptions);
        }
        
        public void bind(TaskComment comment) {
            // Set user name
            String userName = getUserDisplayName(comment);
            tvUserName.setText(userName);
            
            // Set user avatar
            setUserAvatar(comment);
            
            // Set time and body
            tvCommentTime.setText(getRelativeTime(comment.getCreatedAt()));
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
         * Set user avatar - NO LOOP, check cache then Firebase then default
         */
        private void setUserAvatar(TaskComment comment) {
            String userId = comment.getUserId();
            android.util.Log.d("CommentAdapter", "🖼️ setUserAvatar for userId: " + userId);
            
            // No userId? Default avatar
            if (userId == null || userId.isEmpty()) {
                android.util.Log.w("CommentAdapter", "❌ No userId, using default");
                ivUserAvatar.setImageResource(R.drawable.acc_icon);
                return;
            }
            
            // Check cache for avatar URL
            String avatarUrl = null;
            if (userCache.hasUser(userId)) {
                UserDTO user = userCache.getUser(userId);
                avatarUrl = user.getAvatarUrl();
                android.util.Log.d("CommentAdapter", "📦 Cached avatar URL: " + avatarUrl);
            } else {
                android.util.Log.d("CommentAdapter", "❌ User not in cache");
            }
            
            // If have valid avatar URL, load it
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                android.util.Log.d("CommentAdapter", "✅ Loading avatar from cache: " + avatarUrl);
                loadAvatarWithGlide(avatarUrl);
                return;
            }
            
            // Try Firebase photo for CURRENT user only
            // (Backend /api/users/{id} returns null, but Firebase has the photo)
            try {
                com.google.firebase.auth.FirebaseUser currentUser = 
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null && userId.equals(currentUser.getUid())) {
                    android.net.Uri photoUrl = currentUser.getPhotoUrl();
                    android.util.Log.d("CommentAdapter", "🔥 Current user Firebase photo: " + photoUrl);
                    if (photoUrl != null) {
                        android.util.Log.d("CommentAdapter", "✅ Loading Firebase avatar");
                        loadAvatarWithGlide(photoUrl.toString());
                        return;
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("CommentAdapter", "Error checking Firebase: " + e.getMessage());
            }
            
            // Default avatar (for users with no avatar or backend returns null)
            android.util.Log.d("CommentAdapter", "🔸 Using default avatar (no URL available)");
            ivUserAvatar.setImageResource(R.drawable.acc_icon);
            
            // Fetch user info ONLY if not in cache at all
            // Do NOT fetch if cached with null avatar (to prevent loop)
            if (!userCache.hasUser(userId) && !fetchingUsers.contains(userId)) {
                android.util.Log.d("CommentAdapter", "🌐 Fetching user info (not in cache)");
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
         * Get user display name - cache or fallback only, NO API fetch
         */
        private String getUserDisplayName(TaskComment comment) {
            String userId = comment.getUserId();
            
            if (userId == null || userId.isEmpty()) {
                return "Unknown User";
            }
            
            // Check cache first
            if (userCache.hasUser(userId)) {
                UserDTO user = userCache.getUser(userId);
                String name = user.getName();
                return (name != null && !name.isEmpty()) ? name : "User";
            }
            
            // Check Firebase current user
            try {
                com.google.firebase.auth.FirebaseUser currentUser = 
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null && userId.equals(currentUser.getUid())) {
                    String displayName = currentUser.getDisplayName();
                    if (displayName != null && !displayName.isEmpty()) {
                        return displayName;
                    }
                    String email = currentUser.getEmail();
                    if (email != null) {
                        return email.split("@")[0];
                    }
                }
            } catch (Exception ignored) {}
            
            // Fallback
            return "User";
        }
        
        /**
         * Load avatar with Glide
         */
        private void loadAvatarWithGlide(String imageUrl) {
            android.util.Log.d("CommentAdapter", "🎨 loadAvatarWithGlide called with: " + imageUrl);
            
            if (itemView.getContext() == null) {
                android.util.Log.w("CommentAdapter", "❌ Context is null");
                return;
            }
            
            // Check if Activity is still alive
            if (itemView.getContext() instanceof android.app.Activity) {
                android.app.Activity activity = (android.app.Activity) itemView.getContext();
                if (activity.isFinishing() || activity.isDestroyed()) {
                    android.util.Log.w("CommentAdapter", "❌ Activity is finishing/destroyed");
                    return;
                }
            }
            
            android.util.Log.d("CommentAdapter", "✅ Loading with Glide...");
            com.bumptech.glide.Glide.with(itemView.getContext())
                .load(imageUrl)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .circleCrop()
                .error(R.drawable.acc_icon)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e,
                                              Object model,
                                              com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                              boolean isFirstResource) {
                        android.util.Log.e("CommentAdapter", "❌ Glide load FAILED for: " + imageUrl);
                        if (e != null) {
                            android.util.Log.e("CommentAdapter", "Error: " + e.getMessage());
                        }
                        return false;
                    }
                    
                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource,
                                                 Object model,
                                                 com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                 com.bumptech.glide.load.DataSource dataSource,
                                                 boolean isFirstResource) {
                        android.util.Log.d("CommentAdapter", "✅ Glide load SUCCESS for: " + imageUrl);
                        return false;
                    }
                })
                .into(ivUserAvatar);
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

