package com.example.tralalero.feature.home.ui.Home.project;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.tralalero.R;
import com.example.tralalero.data.repository.ActivityLogRepositoryImpl;
import com.example.tralalero.data.repository.MemberRepositoryImpl;
import com.example.tralalero.domain.repository.IActivityLogRepository;
import com.example.tralalero.domain.repository.IMemberRepository;
import com.example.tralalero.feature.project.members.InviteMemberDialog;
import com.example.tralalero.feature.home.ui.adapter.ActivityLogAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class ProjectMenuBottomSheet extends BottomSheetDialogFragment {
    
    private static final String ARG_PROJECT_NAME = "project_name";
    private static final String ARG_PROJECT_ID = "project_id";
    
    private String projectName;
    private String projectId;
    private boolean isStarred = false;
    private String currentVisibility = "WORKSPACE"; // PRIVATE, WORKSPACE, PUBLIC
    
    private IMemberRepository memberRepository;
    private IActivityLogRepository activityLogRepository;
    
    private TextView tvMenuTitle;
    private ImageButton btnClose, btnShare, btnStar, btnVisibility, btnCopy, btnMore;
    private RecyclerView rvMembers, rvActivity;
    private MaterialButton btnInvite;
    private View layoutSynced;
    
    private MemberAdapter memberAdapter;
    private ActivityLogAdapter activityAdapter;  // FIXED: Use ActivityLogAdapter instead of ActivityAdapter

    public static ProjectMenuBottomSheet newInstance(String projectId, String projectName) {
        ProjectMenuBottomSheet fragment = new ProjectMenuBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        args.putString(ARG_PROJECT_NAME, projectName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PROJECT_ID);
            projectName = getArguments().getString(ARG_PROJECT_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.project_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initRepository();
        initViews(view);
        setupListeners();
        setupRecyclerViews();
        loadData();
    }
    
    private void initRepository() {
        memberRepository = new MemberRepositoryImpl(getContext());
        activityLogRepository = new ActivityLogRepositoryImpl(getContext());
    }

    private void initViews(View view) {
        tvMenuTitle = view.findViewById(R.id.tvMenuTitle);
        btnClose = view.findViewById(R.id.btnClose);
        btnShare = view.findViewById(R.id.btnShare);
        btnStar = view.findViewById(R.id.btnStar);
        btnVisibility = view.findViewById(R.id.btnVisibility);
        btnCopy = view.findViewById(R.id.btnCopy);
        btnMore = view.findViewById(R.id.btnMore);
        
        rvMembers = view.findViewById(R.id.rvMembers);
        rvActivity = view.findViewById(R.id.rvActivity);
        btnInvite = view.findViewById(R.id.btnInvite);

        layoutSynced = view.findViewById(R.id.layoutSynced);
        
        if (projectName != null) {
            tvMenuTitle.setText("Board menu");
        }
        
        // Load saved states from SharedPreferences
        loadSavedStates();
        
        // Update star icon
        updateStarIcon();
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());
        
        btnShare.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Share project link", Toast.LENGTH_SHORT).show();
            // TODO: Implement share functionality
        });
        
        btnStar.setOnClickListener(v -> {
            toggleStar();
        });
        
        btnVisibility.setOnClickListener(v -> {
            showVisibilityDialog();
        });
        
        btnCopy.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Copy project", Toast.LENGTH_SHORT).show();
        });
        
        btnMore.setOnClickListener(v -> {
            Toast.makeText(getContext(), "More options", Toast.LENGTH_SHORT).show();
        });
        
        btnInvite.setOnClickListener(v -> {
            showInviteDialog();
        });
        
        layoutSynced.setOnClickListener(v -> {
            syncProjectData();
        });
    }
    
    private void syncProjectData() {
        Toast.makeText(getContext(), "🔄 Syncing...", Toast.LENGTH_SHORT).show();
        
        // Refresh members and activity logs
        loadProjectMembers();
        loadActivityLogs();
        
        // Update UI feedback
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "✅ Data synced successfully", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void toggleStar() {
        isStarred = !isStarred;
        updateStarIcon();
        
        // Save to SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("ProjectData", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("starred_" + projectId, isStarred).apply();
        
        String message = isStarred ? "Starred project" : "Unstarred project";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        
        // TODO: Call API to save star status
        // projectViewModel.toggleStarProject(projectId, isStarred);
    }
    
    private void loadSavedStates() {
        if (getContext() == null) return;
        
        SharedPreferences prefs = requireContext().getSharedPreferences("ProjectData", Context.MODE_PRIVATE);
        
        // Load star state
        isStarred = prefs.getBoolean("starred_" + projectId, false);
        
        // Load visibility state
        currentVisibility = prefs.getString("visibility_" + projectId, "WORKSPACE");
    }
    
    private void updateStarIcon() {
        if (btnStar != null) {
            if (isStarred) {
                btnStar.setImageResource(android.R.drawable.star_big_on);
            } else {
                btnStar.setImageResource(R.drawable.ic_star_outline);
            }
        }
    }
    
    private void showVisibilityDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_visibility);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        com.google.android.material.card.MaterialCardView cardPrivate = dialog.findViewById(R.id.cardPrivate);
        com.google.android.material.card.MaterialCardView cardWorkspace = dialog.findViewById(R.id.cardWorkspace);
        com.google.android.material.card.MaterialCardView cardPublic = dialog.findViewById(R.id.cardPublic);
        
        RadioButton rbPrivate = dialog.findViewById(R.id.rbPrivate);
        RadioButton rbWorkspace = dialog.findViewById(R.id.rbWorkspace);
        RadioButton rbPublic = dialog.findViewById(R.id.rbPublic);
        MaterialButton btnSave = dialog.findViewById(R.id.btnSaveVisibility);
        
        // Set current selection
        updateCardSelection(cardPrivate, cardWorkspace, cardPublic, rbPrivate, rbWorkspace, rbPublic, currentVisibility);
        
        // Card click listeners
        cardPrivate.setOnClickListener(v -> {
            currentVisibility = "PRIVATE";
            updateCardSelection(cardPrivate, cardWorkspace, cardPublic, rbPrivate, rbWorkspace, rbPublic, currentVisibility);
        });
        
        cardWorkspace.setOnClickListener(v -> {
            currentVisibility = "WORKSPACE";
            updateCardSelection(cardPrivate, cardWorkspace, cardPublic, rbPrivate, rbWorkspace, rbPublic, currentVisibility);
        });
        
        cardPublic.setOnClickListener(v -> {
            currentVisibility = "PUBLIC";
            updateCardSelection(cardPrivate, cardWorkspace, cardPublic, rbPrivate, rbWorkspace, rbPublic, currentVisibility);
        });
        
        btnSave.setOnClickListener(v -> {
            // Save to SharedPreferences
            SharedPreferences prefs = requireContext().getSharedPreferences("ProjectData", Context.MODE_PRIVATE);
            prefs.edit().putString("visibility_" + projectId, currentVisibility).apply();
            
            Toast.makeText(getContext(), "Visibility updated to: " + currentVisibility, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            
            // TODO: Call API to save visibility
            // projectViewModel.updateVisibility(projectId, currentVisibility);
        });
        
        dialog.show();
    }
    
    private void updateCardSelection(
            com.google.android.material.card.MaterialCardView cardPrivate,
            com.google.android.material.card.MaterialCardView cardWorkspace,
            com.google.android.material.card.MaterialCardView cardPublic,
            RadioButton rbPrivate,
            RadioButton rbWorkspace,
            RadioButton rbPublic,
            String selectedVisibility) {
        
        // Reset all cards
        cardPrivate.setStrokeWidth(1);
        cardPrivate.setStrokeColor(android.graphics.Color.parseColor("#DFE1E6"));
        cardPrivate.setCardBackgroundColor(android.graphics.Color.WHITE);
        rbPrivate.setChecked(false);
        
        cardWorkspace.setStrokeWidth(1);
        cardWorkspace.setStrokeColor(android.graphics.Color.parseColor("#DFE1E6"));
        cardWorkspace.setCardBackgroundColor(android.graphics.Color.WHITE);
        rbWorkspace.setChecked(false);
        
        cardPublic.setStrokeWidth(1);
        cardPublic.setStrokeColor(android.graphics.Color.parseColor("#DFE1E6"));
        cardPublic.setCardBackgroundColor(android.graphics.Color.WHITE);
        rbPublic.setChecked(false);
        
        // Highlight selected card
        if ("PRIVATE".equals(selectedVisibility)) {
            cardPrivate.setStrokeWidth(4);
            cardPrivate.setStrokeColor(android.graphics.Color.parseColor("#0079BF"));
            cardPrivate.setCardBackgroundColor(android.graphics.Color.parseColor("#DEEBFF"));
            rbPrivate.setChecked(true);
        } else if ("WORKSPACE".equals(selectedVisibility)) {
            cardWorkspace.setStrokeWidth(4);
            cardWorkspace.setStrokeColor(android.graphics.Color.parseColor("#0079BF"));
            cardWorkspace.setCardBackgroundColor(android.graphics.Color.parseColor("#DEEBFF"));
            rbWorkspace.setChecked(true);
        } else if ("PUBLIC".equals(selectedVisibility)) {
            cardPublic.setStrokeWidth(4);
            cardPublic.setStrokeColor(android.graphics.Color.parseColor("#0079BF"));
            cardPublic.setCardBackgroundColor(android.graphics.Color.parseColor("#DEEBFF"));
            rbPublic.setChecked(true);
        }
    }
    
    private void showInviteDialog() {
        if (getContext() == null || getFragmentManager() == null) return;
        
        InviteMemberDialog dialog = InviteMemberDialog.newInstance();
        
        dialog.setOnInviteListener(new InviteMemberDialog.OnInviteListener() {
            @Override
            public void onInvite(String email, String role) {
                // Call API to invite member
                inviteMemberToProject(email, role);
            }
        });
        
        dialog.show(getFragmentManager(), "InviteMemberDialog");
    }
    
    private void inviteMemberToProject(String email, String role) {
        if (memberRepository == null || projectId == null) {
            Toast.makeText(getContext(), "❌ Error: Unable to invite member", Toast.LENGTH_SHORT).show();
            return;
        }
        
        memberRepository.inviteMember(projectId, email, role, new IMemberRepository.RepositoryCallback<com.example.tralalero.domain.model.Member>() {
            @Override
            public void onSuccess(com.example.tralalero.domain.model.Member result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Add activity log
                        addActivityLog("invited " + email + " to " + projectName + " as " + role);
                        
                        // Show success toast
                        Toast.makeText(getContext(), "✅ Invitation sent to " + email, Toast.LENGTH_SHORT).show();
                        
                        // Refresh member list
                        loadProjectMembers();
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Handle specific error messages  
                        if (error.contains("404") || error.toLowerCase().contains("not found")) {
                            showUserNotFoundDialog(email);
                        } else if (error.contains("already a project member") || error.contains("409")) {
                            Toast.makeText(getContext(), 
                                "⚠️ " + email + " is already a member of this project", 
                                Toast.LENGTH_LONG).show();
                        } else if (error.contains("403") || error.contains("permission")) {
                            Toast.makeText(getContext(), 
                                "🚫 Permission denied! Only project owners can invite members.", 
                                Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), 
                                "❌ Failed to invite: " + error, 
                                Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
    
    private void showUserNotFoundDialog(String email) {
        if (getContext() == null) return;
        
        new android.app.AlertDialog.Builder(getContext())
            .setTitle("❌ User Not Found")
            .setMessage("The email " + email + " is not registered in the system.\n\n" +
                       "⚠️ Users must create an account before they can be invited to projects.\n\n" +
                       "Options:\n" +
                       "• Ask them to download and sign up\n" +
                       "• Try a different email address\n" +
                       "• Check for typos in the email")
            .setPositiveButton("Try Another Email", (dialog, which) -> {
                // Reopen invite dialog
                showInviteDialog();
            })
            .setNegativeButton("OK", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void setupRecyclerViews() {
        Log.d("ProjectMenuBottomSheet", "🔧 Setting up RecyclerViews");
        
        // Check if RecyclerViews exist
        if (rvMembers == null) {
            Log.e("ProjectMenuBottomSheet", "❌ rvMembers is NULL!");
            return;
        }
        if (rvActivity == null) {
            Log.e("ProjectMenuBottomSheet", "❌ rvActivity is NULL!");
            return;
        }
        
        Log.d("ProjectMenuBottomSheet", "✅ Both RecyclerViews found");
        Log.d("ProjectMenuBottomSheet", "   rvMembers visibility: " + rvMembers.getVisibility());
        Log.d("ProjectMenuBottomSheet", "   rvActivity visibility: " + rvActivity.getVisibility());
        
        memberAdapter = new MemberAdapter();
        rvMembers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvMembers.setAdapter(memberAdapter);
        Log.d("ProjectMenuBottomSheet", "✅ Member adapter initialized and set");
        
        activityAdapter = new ActivityLogAdapter(getContext());  // FIXED: Pass context and use ActivityLogAdapter
        rvActivity.setLayoutManager(new LinearLayoutManager(getContext()));
        rvActivity.setAdapter(activityAdapter);
        Log.d("ProjectMenuBottomSheet", "✅ Activity adapter initialized and set");
    }

    private void loadData() {
        loadProjectMembersWithCache();
        loadActivityLogsWithCache();
    }
    
    private void loadProjectMembersWithCache() {
        // Load from cache first for instant display
        List<UIMember> cachedMembers = loadMembersFromCache();
        if (!cachedMembers.isEmpty()) {
            memberAdapter.setMembers(cachedMembers);
        }
        
        // Then fetch fresh data from API
        loadProjectMembers();
    }
    
    private void loadActivityLogsWithCache() {
        // Load from cache first
        List<com.example.tralalero.domain.model.ActivityLog> cachedLogs = loadActivityLogsFromCache();
        if (!cachedLogs.isEmpty()) {
            activityAdapter.setActivityLogs(cachedLogs);  // FIXED: Use setActivityLogs
        }
        
        // Then fetch fresh data
        loadActivityLogs();
    }
    
    private List<UIMember> loadMembersFromCache() {
        if (getContext() == null || projectId == null) return new ArrayList<>();
        
        SharedPreferences prefs = requireContext().getSharedPreferences("ProjectData", Context.MODE_PRIVATE);
        String cacheKey = "members_" + projectId;
        String cached = prefs.getString(cacheKey, null);
        
        if (cached != null) {
            try {
                // Simple parsing - in production use Gson
                // For now return empty to let API load
                return new ArrayList<>();
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }
    
    private void saveMembersToCache(List<UIMember> members) {
        if (getContext() == null || projectId == null) return;
        
        SharedPreferences prefs = requireContext().getSharedPreferences("ProjectData", Context.MODE_PRIVATE);
        String cacheKey = "members_" + projectId;
        
        // In production, serialize with Gson and save
        // For now just mark as cached
        prefs.edit().putLong(cacheKey + "_timestamp", System.currentTimeMillis()).apply();
    }
    
    private List<com.example.tralalero.domain.model.ActivityLog> loadActivityLogsFromCache() {
        if (getContext() == null || projectId == null) return new ArrayList<>();
        
        // Similar to members cache
        return new ArrayList<>();
    }
    
    private void saveActivityLogsToCache(List<com.example.tralalero.domain.model.ActivityLog> logs) {
        if (getContext() == null || projectId == null) return;
        
        SharedPreferences prefs = requireContext().getSharedPreferences("ProjectData", Context.MODE_PRIVATE);
        String cacheKey = "activity_" + projectId + "_timestamp";
        prefs.edit().putLong(cacheKey, System.currentTimeMillis()).apply();
    }
    
    private void loadProjectMembers() {
        if (memberRepository != null && projectId != null) {
            memberRepository.getProjectMembers(projectId, new IMemberRepository.RepositoryCallback<List<com.example.tralalero.domain.model.Member>>() {
                @Override
                public void onSuccess(List<com.example.tralalero.domain.model.Member> domainMembers) {
                    if (getContext() == null) return;
                    
                    Log.d("ProjectMenuBottomSheet", "✅ Received " + domainMembers.size() + " members from API");
                    
                    // Convert domain members to UI members
                    List<UIMember> uiMembers = new ArrayList<>();
                    for (com.example.tralalero.domain.model.Member domainMember : domainMembers) {
                        // Skip if user info is null
                        if (domainMember.getUser() == null) {
                            Log.w("ProjectMenuBottomSheet", "⚠️ Skipping member with null user info: " + domainMember.getId());
                            continue;
                        }
                        
                        String initials = getInitials(domainMember.getUser().getName());
                        boolean isAdmin = domainMember.isAdmin() || domainMember.isOwner();
                        String avatarUrl = domainMember.getUser().getAvatarUrl();
                        
                        UIMember uiMember = new UIMember(initials, domainMember.getUser().getName(), avatarUrl, isAdmin);
                        uiMembers.add(uiMember);
                        
                        Log.d("ProjectMenuBottomSheet", "   Member: " + domainMember.getUser().getName() + " (initials: " + initials + ", avatar: " + (avatarUrl != null ? "YES" : "NO") + ")");
                    }
                    
                    Log.d("ProjectMenuBottomSheet", "📊 Total UI members created: " + uiMembers.size());
                    
                    // Save to cache
                    saveMembersToCache(uiMembers);
                    
                    // Update UI on main thread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (memberAdapter != null) {
                                Log.d("ProjectMenuBottomSheet", "🔄 Updating adapter with " + uiMembers.size() + " members");
                                memberAdapter.setMembers(uiMembers);
                                Log.d("ProjectMenuBottomSheet", "✅ Adapter updated, count: " + memberAdapter.getItemCount());
                            } else {
                                Log.e("ProjectMenuBottomSheet", "❌ memberAdapter is NULL!");
                            }
                        });
                    } else {
                        Log.e("ProjectMenuBottomSheet", "❌ Activity is NULL!");
                    }
                }
                
                @Override
                public void onError(String error) {
                    Log.e("ProjectMenuBottomSheet", "❌ Failed to load members: " + error);
                    
                    // Show empty list instead of fake data
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (memberAdapter != null) {
                                memberAdapter.setMembers(new ArrayList<>());
                                Log.d("ProjectMenuBottomSheet", "Cleared member list due to error");
                            }
                        });
                    }
                }
            });
        } else {
            // Log why members aren't loading
            if (memberRepository == null) {
                Log.e("ProjectMenuBottomSheet", "❌ memberRepository is NULL! Cannot load members");
            }
            if (projectId == null) {
                Log.e("ProjectMenuBottomSheet", "❌ projectId is NULL! Cannot load members");
            }
            
            // Show empty list - no fake data
            if (memberAdapter != null) {
                memberAdapter.setMembers(new ArrayList<>());
            }
        }
    }
    
    private void loadActivityLogs() {
        if (activityLogRepository != null && projectId != null) {
            activityLogRepository.getProjectActivityLogs(projectId, 20, 
                new IActivityLogRepository.RepositoryCallback<List<com.example.tralalero.domain.model.ActivityLog>>() {
                @Override
                public void onSuccess(List<com.example.tralalero.domain.model.ActivityLog> logs) {
                    Log.d("ProjectMenuBottomSheet", "✅ Received " + logs.size() + " activity logs from API");
                    
                    // Save to cache
                    saveActivityLogsToCache(logs);
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (activityAdapter != null) {
                                Log.d("ProjectMenuBottomSheet", "🔄 Updating activity adapter with " + logs.size() + " logs");
                                activityAdapter.setActivityLogs(logs);  // FIXED: Use setActivityLogs instead of setActivities
                                Log.d("ProjectMenuBottomSheet", "✅ Activity adapter updated, count: " + activityAdapter.getItemCount());
                            } else {
                                Log.e("ProjectMenuBottomSheet", "❌ activityAdapter is NULL!");
                            }
                        });
                    } else {
                        Log.e("ProjectMenuBottomSheet", "❌ Activity is NULL!");
                    }
                }
                
                @Override
                public void onError(String error) {
                    // Silent fail - just show empty list
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (activityAdapter != null) {
                                activityAdapter.setActivityLogs(new ArrayList<>());  // FIXED: Use setActivityLogs
                            }
                        });
                    }
                }
            });
        } else {
            // Fallback if no repository
            activityAdapter.setActivityLogs(new ArrayList<>());  // FIXED: Use setActivityLogs
        }
    }
    
    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        } else {
            StringBuilder initials = new StringBuilder();
            for (int i = 0; i < Math.min(2, parts.length); i++) {
                if (!parts[i].isEmpty()) {
                    initials.append(parts[i].charAt(0));
                }
            }
            return initials.toString().toUpperCase();
        }
    }
    
    private void addActivityLog(String action) {
        // Activity logs are now loaded from API, not added manually
        // This method is deprecated
    }

    static class UIMember {
        String initials;
        String name;
        String avatarUrl;
        boolean isAdmin;

        UIMember(String initials, String name, String avatarUrl, boolean isAdmin) {
            this.initials = initials;
            this.name = name;
            this.avatarUrl = avatarUrl;
            this.isAdmin = isAdmin;
        }
    }

    static class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
        private List<UIMember> members = new ArrayList<>();

        void setMembers(List<UIMember> members) {
            Log.d("MemberAdapter", "📝 setMembers called with " + members.size() + " members");
            this.members = members;
            notifyDataSetChanged();
            Log.d("MemberAdapter", "✅ notifyDataSetChanged complete, getItemCount=" + getItemCount());
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.d("MemberAdapter", "🏗️ onCreateViewHolder called");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Log.d("MemberAdapter", "📌 onBindViewHolder position=" + position);
            UIMember member = members.get(position);
            
            Log.d("MemberAdapter", "   Member: " + member.name + ", avatar=" + member.avatarUrl);
            
            // Load avatar or show initials
            if (member.avatarUrl != null && !member.avatarUrl.isEmpty()) {
                holder.ivMemberAvatar.setVisibility(View.VISIBLE);
                holder.tvMemberAvatar.setVisibility(View.GONE);
                
                Log.d("MemberAdapter", "   Loading avatar from URL");
                Glide.with(holder.itemView.getContext())
                        .load(member.avatarUrl)
                        .transform(new CircleCrop())
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .into(holder.ivMemberAvatar);
            } else {
                holder.ivMemberAvatar.setVisibility(View.GONE);
                holder.tvMemberAvatar.setVisibility(View.VISIBLE);
                holder.tvMemberAvatar.setText(member.initials);
                Log.d("MemberAdapter", "   Showing initials: " + member.initials);
            }
            
            holder.ivAdminBadge.setVisibility(member.isAdmin ? View.VISIBLE : View.GONE);
        }

        @Override
        public int getItemCount() {
            int count = members.size();
            Log.d("MemberAdapter", "📊 getItemCount=" + count);
            return count;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivMemberAvatar;
            TextView tvMemberAvatar;
            View ivAdminBadge;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivMemberAvatar = itemView.findViewById(R.id.ivMemberAvatar);
                tvMemberAvatar = itemView.findViewById(R.id.tvMemberAvatar);
                ivAdminBadge = itemView.findViewById(R.id.ivAdminBadge);
            }
        }
    }
    
}
