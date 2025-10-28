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

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    
    private List<TaskComment> comments = new ArrayList<>();
    private OnCommentClickListener listener;
    
    public interface OnCommentClickListener {
        void onOptionsClick(TaskComment comment, int position);
        void onCommentClick(TaskComment comment);
    }
    
    public CommentAdapter(OnCommentClickListener listener) {
        this.listener = listener;
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
        notifyDataSetChanged();
    }
    
    public void addComment(TaskComment comment) {
        comments.add(0, comment); // Add to top
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
            // Set user name (TaskComment doesn't have nested User, so use userId)
            tvUserName.setText("User " + comment.getUserId());
            ivUserAvatar.setImageResource(R.drawable.acc_icon);
            
            // Set relative time
            tvCommentTime.setText(getRelativeTime(comment.getCreatedAt()));
            
            // Set comment body
            tvCommentBody.setText(comment.getBody());
            
            // Options button click (edit/delete)
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
    }
}
