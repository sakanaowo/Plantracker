package com.example.tralalero.feature.home.ui.Home.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tralalero.R;
import com.example.tralalero.domain.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for member selection with search filter capability
 */
public class MemberSelectionAdapter extends RecyclerView.Adapter<MemberSelectionAdapter.ViewHolder> {

    private List<User> allMembers;
    private List<User> filteredMembers;
    private List<String> selectedIds = new ArrayList<>();
    private OnMemberToggleListener listener;

    public interface OnMemberToggleListener {
        void onToggle(User user, boolean isSelected);
    }

    public MemberSelectionAdapter(List<User> members, OnMemberToggleListener listener) {
        this.allMembers = members;
        this.filteredMembers = new ArrayList<>(members);
        this.listener = listener;
    }
    
    /**
     * ✅ ADD: Set pre-selected members
     */
    public void setSelectedMembers(List<User> preSelected) {
        selectedIds.clear();
        for (User user : preSelected) {
            selectedIds.add(user.getId());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_member_selectable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User member = filteredMembers.get(position);
        holder.bind(member, selectedIds.contains(member.getId()));
    }

    @Override
    public int getItemCount() {
        return filteredMembers.size();
    }

    public void filter(String query) {
        filteredMembers.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredMembers.addAll(allMembers);
        } else {
            String lowerQuery = query.toLowerCase();
            for (User member : allMembers) {
                if (member.getName().toLowerCase().contains(lowerQuery) ||
                    member.getEmail().toLowerCase().contains(lowerQuery)) {
                    filteredMembers.add(member);
                }
            }
        }

        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        TextView tvEmail;
        CheckBox checkbox;

        ViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivMemberAvatar);
            tvName = itemView.findViewById(R.id.tvMemberName);
            tvEmail = itemView.findViewById(R.id.tvMemberEmail);
            checkbox = itemView.findViewById(R.id.cbMemberSelected);
        }

        void bind(User member, boolean isSelected) {
            tvName.setText(member.getName());
            tvEmail.setText(member.getEmail());
            
            // ✅ FIX: Set listener to null BEFORE changing checkbox state
            checkbox.setOnCheckedChangeListener(null);
            checkbox.setChecked(isSelected);

            // Load avatar
            if (member.getAvatarUrl() != null && !member.getAvatarUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(member.getAvatarUrl())
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_person);
            }

            // ✅ FIX: Set listener AFTER checkbox state is set
            // Handle click on entire item
            itemView.setOnClickListener(v -> toggleSelection(member));
            
            // Handle checkbox click
            checkbox.setOnCheckedChangeListener((buttonView, checked) -> {
                toggleSelection(member);
            });
        }
        
        private void toggleSelection(User member) {
            boolean newState = !selectedIds.contains(member.getId());
            
            if (newState) {
                selectedIds.add(member.getId());
            } else {
                selectedIds.remove(member.getId());
            }
            
            checkbox.setChecked(newState);
            
            if (listener != null) {
                listener.onToggle(member, newState);
            }
        }
    }
}
