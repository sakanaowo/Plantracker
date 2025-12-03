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
import com.example.tralalero.App.App;
import com.example.tralalero.R;
import com.example.tralalero.data.repository.ActivityLogRepositoryImpl;
import com.example.tralalero.data.repository.MemberRepositoryImpl;
import com.example.tralalero.domain.repository.IActivityLogRepository;
import com.example.tralalero.domain.repository.IMemberRepository;
import com.example.tralalero.feature.project.members.InviteMemberDialog;
import com.example.tralalero.feature.project.members.MemberDetailsBottomSheet;
import com.example.tralalero.data.remote.dto.member.MemberDTO;
import com.example.tralalero.data.mapper.MemberMapper;
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
    private String currentUserRole; // OWNER, ADMIN, or MEMBER
    private IMemberRepository memberRepository;
    private IActivityLogRepository activityLogRepository;
    private TextView tvMenuTitle;
    private ImageButton btnClose, btnShare;
    private RecyclerView rvMembers, rvActivity;
    private MaterialButton btnInvite;
    private View layoutSynced;
    private View layoutDeleteProject;
    private View layoutLeaveProject;
    
    private MemberAdapter memberAdapter;
    private ActivityLogAdapter activityAdapter;
    private List<MemberDTO> currentMemberDTOs = new ArrayList<>();

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

        rvMembers = view.findViewById(R.id.rvMembers);
        rvActivity = view.findViewById(R.id.rvActivity);
        btnInvite = view.findViewById(R.id.btnInvite);

        layoutSynced = view.findViewById(R.id.layoutSynced);
        layoutDeleteProject = view.findViewById(R.id.layoutDeleteProject);
        layoutLeaveProject = view.findViewById(R.id.layoutLeaveProject);
        
        if (projectName != null) {
            tvMenuTitle.setText("Project menu");
        }
        
        // Initially hide both - will show appropriate one after fetching project
        if (layoutDeleteProject != null) {
            layoutDeleteProject.setVisibility(View.GONE);
        }
        if (layoutLeaveProject != null) {
            layoutLeaveProject.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());
        
        btnInvite.setOnClickListener(v -> {
            showInviteDialog();
        });
        
        layoutSynced.setOnClickListener(v -> {
            syncProjectData();
        });
        
        // Setup delete project listener
        if (layoutDeleteProject != null) {
            layoutDeleteProject.setOnClickListener(v -> showDeleteProjectDialog());
        }
        
        // Setup leave project listener
        if (layoutLeaveProject != null) {
            layoutLeaveProject.setOnClickListener(v -> showLeaveProjectDialog());
        }
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
    
    private void showDeleteProjectDialog() {
        if (getContext() == null) return;
        
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
            .setTitle("Delete Project")
            .setMessage("Are you sure you want to delete \"" + projectName + "\"? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> deleteProject())
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void deleteProject() {
        if (getContext() == null) return;
        
        Toast.makeText(getContext(), "Deleting project...", Toast.LENGTH_SHORT).show();
        
        com.example.tralalero.data.remote.api.ProjectApiService projectApi = 
            com.example.tralalero.network.ApiClient.get().create(com.example.tralalero.data.remote.api.ProjectApiService.class);
        
        projectApi.deleteProject(projectId).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "✅ Project deleted successfully", Toast.LENGTH_SHORT).show();
                        dismiss();
                        getActivity().finish(); // Close ProjectActivity and return to home
                    } else {
                        String errorMsg = "Failed to delete project";
                        if (response.code() == 403) {
                            errorMsg = "You don't have permission to delete this project";
                        } else if (response.code() == 404) {
                            errorMsg = "Project not found";
                        }
                        Toast.makeText(getContext(), "❌ " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
            }
            
            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "❌ Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("ProjectMenu", "Delete project failed", t);
                });
            }
        });
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
    
    private void showMemberDetailsForProjectMenu(UIMember uiMember) {
        if (uiMember == null || uiMember.memberData == null) {
            Toast.makeText(getContext(), "Unable to load member details", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get current user ID
        com.google.firebase.auth.FirebaseUser firebaseUser = 
            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentUserId = firebaseUser.getUid();
        
        // Use currentUserRole that was already fetched in fetchProjectDetails()
        String userRole = currentUserRole != null ? currentUserRole : "MEMBER";
        
        // Convert domain Member to MemberDTO
        MemberDTO memberDTO = MemberMapper.toDTO(uiMember.memberData);
        
        // Show member details bottom sheet
        MemberDetailsBottomSheet bottomSheet = MemberDetailsBottomSheet.newInstance(memberDTO, currentUserId, userRole);
        bottomSheet.setOnMemberActionListener(new MemberDetailsBottomSheet.OnMemberActionListener() {
            @Override
            public void onChangeRole(MemberDTO member) {
                showChangeRoleDialogForMember(member);
            }

            @Override
            public void onRemoveMember(MemberDTO member) {
                showRemoveMemberConfirmation(member);
            }
        });
        
        if (getChildFragmentManager() != null) {
            bottomSheet.show(getChildFragmentManager(), "MemberDetailsBottomSheet");
        }
    }
    
    private void showChangeRoleDialogForMember(MemberDTO member) {
        String[] roles = {"MEMBER", "ADMIN", "VIEWER"};
        int currentRoleIndex = 0;
        
        // Find current role index
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(member.getRole())) {
                currentRoleIndex = i;
                break;
            }
        }
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Change Role for " + member.getUser().getName())
                .setSingleChoiceItems(roles, currentRoleIndex, null)
                .setPositiveButton("Update", (dialog, which) -> {
                    android.widget.ListView lv = ((androidx.appcompat.app.AlertDialog) dialog).getListView();
                    int selectedPosition = lv.getCheckedItemPosition();
                    if (selectedPosition >= 0) {
                        String newRole = roles[selectedPosition];
                        updateMemberRole(member.getId(), newRole);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void updateMemberRole(String memberId, String newRole) {
        if (memberRepository == null || projectId == null) {
            Toast.makeText(getContext(), "Unable to update role", Toast.LENGTH_SHORT).show();
            return;
        }
        
        memberRepository.updateMemberRole(projectId, memberId, newRole, 
            new IMemberRepository.RepositoryCallback<com.example.tralalero.domain.model.Member>() {
                @Override
                public void onSuccess(com.example.tralalero.domain.model.Member data) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Role updated successfully!", Toast.LENGTH_SHORT).show();
                        loadProjectMembers(); // Refresh member list
                    }
                }

                @Override
                public void onError(String error) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }
    
    private void showRemoveMemberConfirmation(MemberDTO member) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Remove Member")
                .setMessage("Are you sure you want to remove " + member.getUser().getName() + " from this project?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    removeMember(member.getId());
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    
    private void removeMember(String memberId) {
        if (memberRepository == null || projectId == null) {
            Toast.makeText(getContext(), "Unable to remove member", Toast.LENGTH_SHORT).show();
            return;
        }
        
        memberRepository.removeMember(projectId, memberId,
            new IMemberRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Member removed successfully!", Toast.LENGTH_SHORT).show();
                        loadProjectMembers(); // Refresh member list
                    }
                }

                @Override
                public void onError(String error) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
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
        memberAdapter.setOnMemberClickListener(member -> {
            // Open member details bottom sheet
            showMemberDetailsForProjectMenu(member);
        });
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
        fetchProjectDetails(); // Fetch project to get currentUserRole
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
                    
                    // Convert domain members to DTO and store
                    currentMemberDTOs = domainMembers.stream()
                        .map(MemberMapper::toDTO)
                        .collect(java.util.stream.Collectors.toList());
                    
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
                        
                        UIMember uiMember = new UIMember(initials, domainMember.getUser().getName(), avatarUrl, isAdmin, domainMember.getId(), domainMember);
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
    
    /**
     * Fetch project details to get currentUserRole
     */
    private void fetchProjectDetails() {
        if (projectId == null) return;
        
        com.example.tralalero.data.remote.api.ProjectApiService projectApi = 
            com.example.tralalero.network.ApiClient.get(App.authManager)
                .create(com.example.tralalero.data.remote.api.ProjectApiService.class);
        
        projectApi.getProjectById(projectId).enqueue(new retrofit2.Callback<com.example.tralalero.data.remote.dto.project.ProjectDTO>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.tralalero.data.remote.dto.project.ProjectDTO> call, 
                                 retrofit2.Response<com.example.tralalero.data.remote.dto.project.ProjectDTO> response) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        currentUserRole = response.body().getCurrentUserRole();
                        Log.d("ProjectMenuBottomSheet", "✅ RAW currentUserRole from API: '" + currentUserRole + "'");
                        Log.d("ProjectMenuBottomSheet", "✅ Is null? " + (currentUserRole == null));
                        Log.d("ProjectMenuBottomSheet", "✅ Project ID: " + projectId);
                        Log.d("ProjectMenuBottomSheet", "✅ Response body: " + response.body());
                        updateUIBasedOnRole();
                    } else {
                        Log.e("ProjectMenuBottomSheet", "❌ Failed to get project: " + response.code());
                        // Hide both buttons if we can't determine role
                        if (layoutDeleteProject != null) layoutDeleteProject.setVisibility(View.GONE);
                        if (layoutLeaveProject != null) layoutLeaveProject.setVisibility(View.GONE);
                    }
                });
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.tralalero.data.remote.dto.project.ProjectDTO> call, Throwable t) {
                Log.e("ProjectMenuBottomSheet", "❌ Error fetching project", t);
            }
        });
    }
    
    /**
     * Show appropriate button based on user role
     */
    private void updateUIBasedOnRole() {
        if (layoutDeleteProject == null || layoutLeaveProject == null) {
            Log.e("ProjectMenuBottomSheet", "❌ Layout views are NULL!");
            return;
        }
        
        Log.d("ProjectMenuBottomSheet", "🔍 updateUIBasedOnRole - currentUserRole: '" + currentUserRole + "'");
        
        // Handle null role (workspace owner who is not project member)
        if (currentUserRole == null || currentUserRole.trim().isEmpty()) {
            Log.w("ProjectMenuBottomSheet", "⚠️ currentUserRole is null/empty - hiding both buttons");
            layoutDeleteProject.setVisibility(View.GONE);
            layoutLeaveProject.setVisibility(View.GONE);
            return;
        }
        
        String role = currentUserRole.trim().toUpperCase();
        Log.d("ProjectMenuBottomSheet", "🔍 Normalized role: '" + role + "'");
        
        if ("OWNER".equals(role)) {
            // Show Delete, hide Leave
            layoutDeleteProject.setVisibility(View.VISIBLE);
            layoutLeaveProject.setVisibility(View.GONE);
            Log.d("ProjectMenuBottomSheet", "✅ Showing DELETE button (user is OWNER)");
        } else if ("ADMIN".equals(role) || "MEMBER".equals(role)) {
            // Show Leave, hide Delete
            layoutDeleteProject.setVisibility(View.GONE);
            layoutLeaveProject.setVisibility(View.VISIBLE);
            Log.d("ProjectMenuBottomSheet", "✅ Showing LEAVE button (user is " + role + ")");
        } else {
            // Unknown role - hide both
            Log.w("ProjectMenuBottomSheet", "⚠️ Unknown role: " + role + " - hiding both buttons");
            layoutDeleteProject.setVisibility(View.GONE);
            layoutLeaveProject.setVisibility(View.GONE);
        }
    }
    
    /**
     * Show leave project confirmation dialog
     */
    private void showLeaveProjectDialog() {
        if (getContext() == null) return;
        
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
            .setTitle("Leave Project")
            .setMessage("Are you sure you want to leave \"" + projectName + "\"?")
            .setPositiveButton("Leave", (dialog, which) -> leaveProject())
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    /**
     * Leave project (for non-owners)
     */
    private void leaveProject() {
        if (getContext() == null) return;
        
        Toast.makeText(getContext(), "Leaving project...", Toast.LENGTH_SHORT).show();
        
        com.example.tralalero.domain.repository.IProjectRepository projectRepo = 
            new com.example.tralalero.data.repository.ProjectRepositoryImpl(getContext());
        
        projectRepo.leaveProject(projectId, new com.example.tralalero.domain.repository.IProjectRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "✅ Successfully left the project", Toast.LENGTH_SHORT).show();
                    dismiss();
                    getActivity().finish(); // Close ProjectActivity and return to home
                });
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "❌ " + error, Toast.LENGTH_LONG).show();
                    Log.e("ProjectMenu", "Leave project failed: " + error);
                });
            }
        });
    }

    static class UIMember {
        String initials;
        String name;
        String avatarUrl;
        boolean isAdmin;
        String memberId;
        com.example.tralalero.domain.model.Member memberData; // Store full member data

        UIMember(String initials, String name, String avatarUrl, boolean isAdmin, String memberId, com.example.tralalero.domain.model.Member memberData) {
            this.initials = initials;
            this.name = name;
            this.avatarUrl = avatarUrl;
            this.isAdmin = isAdmin;
            this.memberId = memberId;
            this.memberData = memberData;
        }
    }

    static class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
        private List<UIMember> members = new ArrayList<>();
        private OnMemberClickListener clickListener;
        
        interface OnMemberClickListener {
            void onMemberClick(UIMember member);
        }
        
        void setOnMemberClickListener(OnMemberClickListener listener) {
            this.clickListener = listener;
        }

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
            
            // Set click listener for entire item
            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onMemberClick(member);
                }
            });
            
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
