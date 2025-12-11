package com.example.tralalero.adapter;

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

import java.util.ArrayList;
import java.util.List;

public class TaskAssigneeAdapter extends RecyclerView.Adapter<TaskAssigneeAdapter.ViewHolder> {
    
    private List<AssigneeInfo> assignees = new ArrayList<>();
    private Context context;

    public static class AssigneeInfo {
        public String id;
        public String name;
        public String avatarUrl;

        public AssigneeInfo(String id, String name, String avatarUrl) {
            this.id = id;
            this.name = name;
            this.avatarUrl = avatarUrl;
        }
    }

    public TaskAssigneeAdapter(Context context) {
        this.context = context;
    }

    public void setAssignees(List<AssigneeInfo> assignees) {
        this.assignees = assignees != null ? assignees : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_assignee, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AssigneeInfo assignee = assignees.get(position);
        holder.bind(assignee, context);
    }

    @Override
    public int getItemCount() {
        return assignees.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAssigneeAvatar;
        TextView tvAssigneeInitials;

        ViewHolder(View itemView) {
            super(itemView);
            ivAssigneeAvatar = itemView.findViewById(R.id.ivAssigneeAvatar);
            tvAssigneeInitials = itemView.findViewById(R.id.tvAssigneeInitials);
        }

        void bind(AssigneeInfo assignee, Context context) {
            // Load avatar or show initials
            if (assignee.avatarUrl != null && !assignee.avatarUrl.isEmpty()) {
                ivAssigneeAvatar.setVisibility(View.VISIBLE);
                tvAssigneeInitials.setVisibility(View.GONE);
                Glide.with(context)
                        .load(assignee.avatarUrl)
                        .transform(new CircleCrop())
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .into(ivAssigneeAvatar);
            } else {
                ivAssigneeAvatar.setVisibility(View.GONE);
                tvAssigneeInitials.setVisibility(View.VISIBLE);
                tvAssigneeInitials.setText(getInitials(assignee.name));
            }
        }
        
        private String getInitials(String name) {
            if (name == null || name.isEmpty()) {
                return "??";
            }
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
            }
            return name.substring(0, Math.min(2, name.length())).toUpperCase();
        }
    }
}
