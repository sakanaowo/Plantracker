package com.example.tralalero.feature.project.members;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tralalero.R;
import com.example.tralalero.data.remote.dto.member.MemberDTO;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
    
    private List<MemberDTO> members = new ArrayList<>();
    private OnMemberActionListener listener;
    private OnMemberClickListener clickListener;
    private String currentUserId;
    private String currentUserRole;

    public interface OnMemberActionListener {
        void onChangeRole(MemberDTO member);
        void onRemoveMember(MemberDTO member);
    }
    
    public interface OnMemberClickListener {
        void onMemberClick(MemberDTO member);
    }

    public void setOnMemberActionListener(OnMemberActionListener listener) {
        this.listener = listener;
    }
    
    public void setOnMemberClickListener(OnMemberClickListener clickListener) {
        this.clickListener = clickListener;
    }
    
    public void setCurrentUser(String userId, String role) {
        this.currentUserId = userId;
        this.currentUserRole = role;
        notifyDataSetChanged();
    }

    public void setMembers(List<MemberDTO> members) {
        this.members = members != null ? members : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_member_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemberDTO member = members.get(position);
        
        holder.tvName.setText(member.getUser().getName());
        holder.tvEmail.setText(member.getUser().getEmail());
        holder.chipRole.setText(member.getRole());
        
        // Set click listener for entire item
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onMemberClick(member);
            }
        });
        
        // Set role color
        int roleColor = getRoleColor(holder.itemView.getContext(), member.getRole());
        holder.chipRole.setChipBackgroundColor(ColorStateList.valueOf(roleColor));
        
        // Load avatar with circle crop
        if (member.getUser().getAvatarUrl() != null && !member.getUser().getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(member.getUser().getAvatarUrl())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_person);
        }
        
        // More options menu
        holder.btnMoreOptions.setOnClickListener(v -> showOptionsMenu(v, member));
    }

    private void showOptionsMenu(View anchor, MemberDTO member) {
        PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_member_options, popup.getMenu());
        
        // ✅ Check permissions before showing menu items
        boolean isViewingSelf = member.getUser() != null && 
            member.getUser().getId() != null && 
            member.getUser().getId().equals(currentUserId);
        
        boolean isCurrentUserOwner = "OWNER".equalsIgnoreCase(currentUserRole);
        boolean isCurrentUserAdmin = "ADMIN".equalsIgnoreCase(currentUserRole);
        boolean hasPermission = isCurrentUserOwner || isCurrentUserAdmin;
        
        boolean isMemberOwner = "OWNER".equalsIgnoreCase(member.getRole());
        
        // ADMIN can remove MEMBER and other ADMIN (but NOT OWNER)
        // OWNER can remove anyone except themselves
        boolean canRemove = !isViewingSelf && hasPermission && !isMemberOwner;
        boolean canChangeRole = hasPermission && !isMemberOwner;
        
        // Hide/show menu items based on permissions
        if (popup.getMenu().findItem(R.id.action_remove) != null) {
            popup.getMenu().findItem(R.id.action_remove).setVisible(canRemove);
        }
        if (popup.getMenu().findItem(R.id.action_change_role) != null) {
            popup.getMenu().findItem(R.id.action_change_role).setVisible(canChangeRole);
        }
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_change_role) {
                if (listener != null) listener.onChangeRole(member);
                return true;
            } else if (itemId == R.id.action_remove) {
                if (listener != null) listener.onRemoveMember(member);
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    private int getRoleColor(android.content.Context context, String role) {
        int colorResId;
        switch (role.toUpperCase()) {
            case "OWNER":
                colorResId = R.color.role_owner;
                break;
            case "ADMIN":
                colorResId = R.color.role_admin;
                break;
            case "VIEWER":
                colorResId = R.color.role_viewer;
                break;
            default:
                colorResId = R.color.role_member;
                break;
        }
        return context.getResources().getColor(colorResId, null);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvEmail;
        com.google.android.material.chip.Chip chipRole;
        ImageButton btnMoreOptions;

        ViewHolder(View view) {
            super(view);
            ivAvatar = view.findViewById(R.id.ivMemberAvatar);
            tvName = view.findViewById(R.id.tvMemberName);
            tvEmail = view.findViewById(R.id.tvMemberEmail);
            chipRole = view.findViewById(R.id.chipRole);
            btnMoreOptions = view.findViewById(R.id.btnMoreOptions);
        }
    }
}
