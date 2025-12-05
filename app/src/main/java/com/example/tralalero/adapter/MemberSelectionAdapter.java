package com.example.tralalero.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.ProjectMember;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MemberSelectionAdapter extends RecyclerView.Adapter<MemberSelectionAdapter.MemberViewHolder> {

    private List<ProjectMember> members = new ArrayList<>();
    private List<ProjectMember> filteredMembers = new ArrayList<>();
    private Set<String> selectedUserIds = new HashSet<>(); // ✅ Changed to Set for multi-select
    private OnSelectionChangeListener listener;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(Set<String> selectedUserIds);
    }

    public MemberSelectionAdapter(OnSelectionChangeListener listener) {
        this.listener = listener;
    }

    public void setMembers(List<ProjectMember> members) {
        this.members = members != null ? members : new ArrayList<>();
        this.filteredMembers = new ArrayList<>(this.members);
        notifyDataSetChanged();
    }

    public void setSelectedUserIds(Set<String> userIds) {
        this.selectedUserIds = userIds != null ? new HashSet<>(userIds) : new HashSet<>();
        notifyDataSetChanged();
    }

    public Set<String> getSelectedUserIds() {
        return new HashSet<>(selectedUserIds);
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
        boolean isSelected = selectedUserIds.contains(member.getUserId());
        holder.bind(member, isSelected, (selectedMember, checked) -> {
            if (checked) {
                selectedUserIds.add(selectedMember.getUserId());
            } else {
                selectedUserIds.remove(selectedMember.getUserId());
            }
            if (listener != null) {
                listener.onSelectionChanged(selectedUserIds);
            }
            notifyItemChanged(position);
        });
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
        private final CheckBox cbSelected; // ✅ Changed from ImageView to CheckBox

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivMemberAvatar);
            tvInitials = itemView.findViewById(R.id.tvMemberInitials);
            tvName = itemView.findViewById(R.id.tvMemberName);
            tvEmail = itemView.findViewById(R.id.tvMemberEmail);
            tvRole = itemView.findViewById(R.id.tvMemberRole);
            cbSelected = itemView.findViewById(R.id.cbMemberSelected);
        }

        public void bind(ProjectMember member, boolean isSelected, OnCheckChangeListener listener) {
            tvName.setText(member.getName());
            tvEmail.setText(member.getEmail());
            tvRole.setText(member.getRole());

            // Show initials
            tvInitials.setText(member.getInitials());

            // For now, just show initials
            ivAvatar.setVisibility(View.GONE);
            tvInitials.setVisibility(View.VISIBLE);

            // Set checkbox state
            cbSelected.setChecked(isSelected);

            // Set role color
            if ("ADMIN".equals(member.getRole())) {
                tvRole.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
            } else {
                tvRole.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            }

            // Handle clicks
            View.OnClickListener clickListener = v -> {
                cbSelected.setChecked(!cbSelected.isChecked());
                if (listener != null) {
                    listener.onCheckChanged(member, cbSelected.isChecked());
                }
            };

            itemView.setOnClickListener(clickListener);
            cbSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null && buttonView.isPressed()) {
                    listener.onCheckChanged(member, isChecked);
                }
            });
        }
    }

    interface OnCheckChangeListener {
        void onCheckChanged(ProjectMember member, boolean isChecked);
    }
}
