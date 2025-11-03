package com.example.tralalero.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.ProjectMember;

import java.util.ArrayList;
import java.util.List;

public class MemberSelectionAdapter extends RecyclerView.Adapter<MemberSelectionAdapter.MemberViewHolder> {

    private List<ProjectMember> members = new ArrayList<>();
    private List<ProjectMember> filteredMembers = new ArrayList<>();
    private String currentAssigneeId;
    private OnMemberClickListener listener;

    public interface OnMemberClickListener {
        void onMemberClick(ProjectMember member);
    }

    public MemberSelectionAdapter(OnMemberClickListener listener) {
        this.listener = listener;
    }

    public void setMembers(List<ProjectMember> members) {
        this.members = members != null ? members : new ArrayList<>();
        this.filteredMembers = new ArrayList<>(this.members);
        notifyDataSetChanged();
    }

    public void setCurrentAssigneeId(String assigneeId) {
        this.currentAssigneeId = assigneeId;
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredMembers.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredMembers.addAll(members);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (ProjectMember member : members) {
                if (member.getName().toLowerCase().contains(lowerQuery) ||
                        member.getEmail().toLowerCase().contains(lowerQuery)) {
                    filteredMembers.add(member);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_selectable, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        ProjectMember member = filteredMembers.get(position);
        boolean isSelected = currentAssigneeId != null && currentAssigneeId.equals(member.getUserId());
        holder.bind(member, isSelected, listener);
    }

    @Override
    public int getItemCount() {
        return filteredMembers.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivAvatar;
        private final TextView tvInitials;
        private final TextView tvName;
        private final TextView tvEmail;
        private final TextView tvRole;
        private final ImageView ivSelected;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivMemberAvatar);
            tvInitials = itemView.findViewById(R.id.tvMemberInitials);
            tvName = itemView.findViewById(R.id.tvMemberName);
            tvEmail = itemView.findViewById(R.id.tvMemberEmail);
            tvRole = itemView.findViewById(R.id.tvMemberRole);
            ivSelected = itemView.findViewById(R.id.ivSelected);
        }

        public void bind(ProjectMember member, boolean isSelected, OnMemberClickListener listener) {
            tvName.setText(member.getName());
            tvEmail.setText(member.getEmail());
            tvRole.setText(member.getRole());

            // Show initials
            tvInitials.setText(member.getInitials());

            // TODO: Load avatar image using Glide/Picasso if needed
            // For now, just show initials
            ivAvatar.setVisibility(View.GONE);
            tvInitials.setVisibility(View.VISIBLE);

            // Show selected indicator
            ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            // Set role color
            if ("ADMIN".equals(member.getRole())) {
                tvRole.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
            } else {
                tvRole.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMemberClick(member);
                }
            });
        }
    }
}
