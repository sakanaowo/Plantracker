package com.example.tralalero.feature.home.ui.Home.project;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.data.remote.dto.user.UserDTO;

import java.util.ArrayList;
import java.util.List;

public class AssigneeAdapter extends RecyclerView.Adapter<AssigneeAdapter.AssigneeViewHolder> {

    private List<UserDTO> assignees = new ArrayList<>();
    private OnAssigneeActionListener listener;

    public interface OnAssigneeActionListener {
        void onRemoveAssignee(UserDTO user);
    }

    public AssigneeAdapter(OnAssigneeActionListener listener) {
        this.listener = listener;
    }

    public void setAssignees(List<UserDTO> assignees) {
        this.assignees = assignees != null ? assignees : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<UserDTO> getAssignees() {
        return assignees;
    }

    @NonNull
    @Override
    public AssigneeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assignee_chip, parent, false);
        return new AssigneeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssigneeViewHolder holder, int position) {
        UserDTO user = assignees.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return assignees.size();
    }

    class AssigneeViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials;
        TextView tvName;
        ImageView ivRemove;

        public AssigneeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tvAssigneeInitials);
            tvName = itemView.findViewById(R.id.tvAssigneeName);
            ivRemove = itemView.findViewById(R.id.ivRemoveAssignee);
        }

        public void bind(UserDTO user) {
            String name = user.getName() != null ? user.getName() : 
                         (user.getEmail() != null ? user.getEmail() : "Unknown");
            
            tvName.setText(name);
            
            // Generate initials
            String initials = getInitials(name);
            tvInitials.setText(initials);
            
            // Random color based on user ID
            int color = generateColorFromString(user.getId() != null ? user.getId() : name);
            tvInitials.setBackgroundColor(color);
            
            // Remove button
            ivRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveAssignee(user);
                }
            });
        }

        private String getInitials(String name) {
            if (name == null || name.isEmpty()) return "?";
            
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
            } else {
                return name.substring(0, Math.min(2, name.length())).toUpperCase();
            }
        }

        private int generateColorFromString(String str) {
            int hash = str.hashCode();
            int[] colors = {
                Color.parseColor("#4CAF50"), // Green
                Color.parseColor("#2196F3"), // Blue
                Color.parseColor("#FF9800"), // Orange
                Color.parseColor("#9C27B0"), // Purple
                Color.parseColor("#F44336"), // Red
                Color.parseColor("#00BCD4"), // Cyan
                Color.parseColor("#FF5722"), // Deep Orange
                Color.parseColor("#3F51B5")  // Indigo
            };
            return colors[Math.abs(hash) % colors.length];
        }
    }
}
