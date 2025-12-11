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
    private static final String ARG_CURRENT_USER_ID = "current_user_id";
    private static final String ARG_CURRENT_USER_ROLE = "current_user_role";
    
    private MemberDTO member;
    private String currentUserId;
    private String currentUserRole;
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
    private TextView tvMemberBio;
    private MaterialButton btnChangeRole;
    private MaterialButton btnRemoveMember;
    private View layoutActions;
    
    public interface OnMemberActionListener {
        void onChangeRole(MemberDTO member);
        void onRemoveMember(MemberDTO member);
    }
    
    public static MemberDetailsBottomSheet newInstance(MemberDTO member, String currentUserId, String currentUserRole) {
        MemberDetailsBottomSheet fragment = new MemberDetailsBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MEMBER, member);
        args.putString(ARG_CURRENT_USER_ID, currentUserId);
        args.putString(ARG_CURRENT_USER_ROLE, currentUserRole);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            member = (MemberDTO) getArguments().getSerializable(ARG_MEMBER);
            currentUserId = getArguments().getString(ARG_CURRENT_USER_ID);
            currentUserRole = getArguments().getString(ARG_CURRENT_USER_ROLE);
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
        tvMemberBio = view.findViewById(R.id.tvMemberBio);
        btnChangeRole = view.findViewById(R.id.btnChangeRole);
        btnRemoveMember = view.findViewById(R.id.btnRemoveMember);
        layoutActions = view.findViewById(R.id.layoutActions);
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
        
        // Set job title (previously Member ID field)
        String jobTitle = member.getUser().getJobTitle();
        if (jobTitle != null && !jobTitle.isEmpty()) {
            tvMemberId.setText(jobTitle);
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
        
        // Set phone number (previously User ID field)
        String phoneNumber = member.getUser().getPhoneNumber();
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            tvUserId.setText(phoneNumber);
        } else {
            tvUserId.setText("N/A");
        }
        
        // Set bio
        String bio = member.getUser().getBio();
        if (bio != null && !bio.isEmpty()) {
            tvMemberBio.setText(bio);
            tvMemberBio.setVisibility(View.VISIBLE);
        } else {
            tvMemberBio.setText("N/A");
            tvMemberBio.setVisibility(View.VISIBLE);
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
        
        // Control button visibility based on permissions
        updateButtonVisibility(role);
    }
    
    private void updateButtonVisibility(String memberRole) {
        // Check if viewing self
        boolean isViewingSelf = member != null && member.getUser() != null && 
            member.getUser().getId() != null && 
            member.getUser().getId().equals(currentUserId);
        
        // Check if current user is owner or admin
        boolean isCurrentUserOwner = "OWNER".equalsIgnoreCase(currentUserRole);
        boolean isCurrentUserAdmin = "ADMIN".equalsIgnoreCase(currentUserRole);
        boolean hasPermission = isCurrentUserOwner || isCurrentUserAdmin;
        
        // Check if member being viewed is owner
        boolean isMemberOwner = "OWNER".equalsIgnoreCase(memberRole);
        
        // Determine visibility for each button
        boolean showRemoveButton = !isViewingSelf && isCurrentUserOwner && !isMemberOwner;
        boolean showChangeRoleButton = hasPermission && !isMemberOwner;
        
        // Update Remove Member button visibility
        if (showRemoveButton) {
            btnRemoveMember.setVisibility(View.VISIBLE);
        } else {
            btnRemoveMember.setVisibility(View.GONE);
        }
        
        // Update Change Role button visibility
        if (showChangeRoleButton) {
            btnChangeRole.setVisibility(View.VISIBLE);
            btnChangeRole.setEnabled(true);
            btnChangeRole.setAlpha(1.0f);
        } else {
            btnChangeRole.setVisibility(View.GONE);
        }
        
        // Hide entire Actions section if both buttons are hidden
        if (!showRemoveButton && !showChangeRoleButton) {
            layoutActions.setVisibility(View.GONE);
        } else {
            layoutActions.setVisibility(View.VISIBLE);
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
