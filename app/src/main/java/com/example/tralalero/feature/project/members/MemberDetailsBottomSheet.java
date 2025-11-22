package com.example.tralalero.feature.project.members;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.example.tralalero.R;
import com.example.tralalero.data.remote.dto.member.MemberDTO;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MemberDetailsBottomSheet extends BottomSheetDialogFragment {
    
    private static final String ARG_MEMBER = "member";
    
    private MemberDTO member;
    private OnMemberActionListener listener;
    
    // Views
    private ImageButton btnClose;
    private ImageView ivMemberAvatar;
    private TextView tvMemberName;
    private TextView tvMemberEmail;
    private Chip chipRole;
    private TextView tvMemberId;
    private TextView tvJoinedDate;
    private TextView tvUserId;
    private MaterialButton btnChangeRole;
    private MaterialButton btnRemoveMember;
    
    public interface OnMemberActionListener {
        void onChangeRole(MemberDTO member);
        void onRemoveMember(MemberDTO member);
    }
    
    public static MemberDetailsBottomSheet newInstance(MemberDTO member) {
        MemberDetailsBottomSheet fragment = new MemberDetailsBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MEMBER, member);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            member = (MemberDTO) getArguments().getSerializable(ARG_MEMBER);
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new BottomSheetDialog(requireContext(), getTheme());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_member_details, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        populateData();
        setupListeners();
    }
    
    private void initViews(View view) {
        btnClose = view.findViewById(R.id.btnClose);
        ivMemberAvatar = view.findViewById(R.id.ivMemberAvatar);
        tvMemberName = view.findViewById(R.id.tvMemberName);
        tvMemberEmail = view.findViewById(R.id.tvMemberEmail);
        chipRole = view.findViewById(R.id.chipRole);
        tvMemberId = view.findViewById(R.id.tvMemberId);
        tvJoinedDate = view.findViewById(R.id.tvJoinedDate);
        tvUserId = view.findViewById(R.id.tvUserId);
        btnChangeRole = view.findViewById(R.id.btnChangeRole);
        btnRemoveMember = view.findViewById(R.id.btnRemoveMember);
    }
    
    private void populateData() {
        if (member == null || member.getUser() == null) {
            return;
        }
        
        // Set name
        String name = member.getUser().getName();
        if (name != null && !name.isEmpty()) {
            tvMemberName.setText(name);
        } else {
            tvMemberName.setText("Unknown User");
        }
        
        // Set email
        String email = member.getUser().getEmail();
        if (email != null && !email.isEmpty()) {
            tvMemberEmail.setText(email);
        } else {
            tvMemberEmail.setVisibility(View.GONE);
        }
        
        // Set role
        String role = member.getRole();
        if (role != null && !role.isEmpty()) {
            chipRole.setText(role.toUpperCase());
            int roleColor = getRoleColor(role);
            chipRole.setChipBackgroundColorResource(roleColor);
        }
        
        // Set member ID
        String memberId = member.getId();
        if (memberId != null && !memberId.isEmpty()) {
            tvMemberId.setText(memberId);
        } else {
            tvMemberId.setText("N/A");
        }
        
        // Set joined date
        String createdAt = member.getCreatedAt();
        if (createdAt != null && !createdAt.isEmpty()) {
            try {
                // Parse ISO 8601 date string from backend
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                Date joinedDate = isoFormat.parse(createdAt);
                
                SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                tvJoinedDate.setText(displayFormat.format(joinedDate));
            } catch (Exception e) {
                android.util.Log.e("MemberDetails", "Error parsing date: " + createdAt, e);
                tvJoinedDate.setText(createdAt.substring(0, Math.min(10, createdAt.length())));
            }
        } else {
            tvJoinedDate.setText("N/A");
        }
        
        // Set user ID
        String userId = member.getUser().getId();
        if (userId != null && !userId.isEmpty()) {
            tvUserId.setText(userId);
        } else {
            tvUserId.setText("N/A");
        }
        
        // Load avatar with Glide
        String avatarUrl = member.getUser().getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(ivMemberAvatar);
        } else {
            ivMemberAvatar.setImageResource(R.drawable.ic_person);
        }
        
        // Disable actions for OWNER role
        if ("OWNER".equalsIgnoreCase(role)) {
            btnChangeRole.setEnabled(false);
            btnChangeRole.setAlpha(0.5f);
            btnRemoveMember.setEnabled(false);
            btnRemoveMember.setAlpha(0.5f);
        }
    }
    
    private int getRoleColor(String role) {
        switch (role.toUpperCase()) {
            case "OWNER":
                return R.color.role_owner;
            case "ADMIN":
                return R.color.role_admin;
            case "VIEWER":
                return R.color.role_viewer;
            default:
                return R.color.role_member;
        }
    }
    
    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());
        
        btnChangeRole.setOnClickListener(v -> {
            if (listener != null && member != null) {
                listener.onChangeRole(member);
                dismiss();
            }
        });
        
        btnRemoveMember.setOnClickListener(v -> {
            if (listener != null && member != null) {
                listener.onRemoveMember(member);
                dismiss();
            }
        });
    }
    
    public void setOnMemberActionListener(OnMemberActionListener listener) {
        this.listener = listener;
    }
}
