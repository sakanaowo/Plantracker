package com.example.tralalero.feature.project.members;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tralalero.R;
import com.example.tralalero.data.mapper.MemberMapper;
import com.example.tralalero.data.remote.dto.member.MemberDTO;
import com.example.tralalero.data.repository.MemberRepositoryImpl;
import com.example.tralalero.domain.model.Member;
import com.example.tralalero.domain.repository.IMemberRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MembersFragment extends Fragment {
    
    private static final String TAG = "MembersFragment";
    private static final String ARG_PROJECT_ID = "project_id";
    
    private String projectId;
    private RecyclerView rvMembers;
    private ProgressBar progressBar;
    private TextView tvNoMembers;
    private FloatingActionButton fabInvite;
    private ImageButton btnBack;
    
    private MemberAdapter adapter;
    private IMemberRepository repository;
    private List<MemberDTO> currentMembers = new ArrayList<>();

    public static MembersFragment newInstance(String projectId) {
        MembersFragment fragment = new MembersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PROJECT_ID);
        }
        repository = new MemberRepositoryImpl(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_members, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupAdapter();
        setupListeners();
        loadMembers();
    }

    private void initViews(View view) {
        rvMembers = view.findViewById(R.id.rvMembers);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoMembers = view.findViewById(R.id.tvNoMembers);
        fabInvite = view.findViewById(R.id.fabInviteMember);
        btnBack = view.findViewById(R.id.btnBack);
    }

    private void setupAdapter() {
        adapter = new MemberAdapter();
        
        // Handle member item click - show details
        adapter.setOnMemberClickListener(member -> {
            showMemberDetails(member);
        });
        
        // Handle member actions from menu
        adapter.setOnMemberActionListener(new MemberAdapter.OnMemberActionListener() {
            @Override
            public void onChangeRole(MemberDTO member) {
                showChangeRoleDialog(member);
            }

            @Override
            public void onRemoveMember(MemberDTO member) {
                showRemoveConfirmation(member);
            }
        });
        
        rvMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMembers.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        fabInvite.setOnClickListener(v -> showInviteDialog());
    }

    private void loadMembers() {
        showLoading(true);
        
        repository.getProjectMembers(projectId, new IMemberRepository.RepositoryCallback<List<Member>>() {
            @Override
            public void onSuccess(List<Member> data) {
                if (isAdded()) {
                    showLoading(false);
                    // Convert Member domain models to MemberDTO for adapter
                    List<MemberDTO> dtoList = data.stream()
                        .map(MemberMapper::toDTO)
                        .collect(Collectors.toList());
                    currentMembers = dtoList; // Store for later use
                    adapter.setMembers(dtoList);
                    updateEmptyState(data.isEmpty());
                }
            }

            @Override
            public void onError(String error) {
                if (isAdded()) {
                    showLoading(false);
                    Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Load error: " + error);
                }
            }
        });
    }

    private void showInviteDialog() {
        InviteMemberDialog dialog = InviteMemberDialog.newInstance();
        dialog.setOnInviteListener((email, role) -> {
            inviteMember(email, role);
        });
        dialog.show(getChildFragmentManager(), "InviteMemberDialog");
    }
    
    private void showMemberDetails(MemberDTO member) {
        // Get current user ID
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentUserId = firebaseUser.getUid();
        
        // Find current user's role in the project
        String currentUserRole = "MEMBER"; // Default
        for (MemberDTO m : currentMembers) {
            if (m.getUser() != null && currentUserId.equals(m.getUser().getId())) {
                currentUserRole = m.getRole();
                break;
            }
        }
        
        MemberDetailsBottomSheet bottomSheet = MemberDetailsBottomSheet.newInstance(member, currentUserId, currentUserRole);
        bottomSheet.setOnMemberActionListener(new MemberDetailsBottomSheet.OnMemberActionListener() {
            @Override
            public void onChangeRole(MemberDTO member) {
                showChangeRoleDialog(member);
            }

            @Override
            public void onRemoveMember(MemberDTO member) {
                showRemoveConfirmation(member);
            }
        });
        bottomSheet.show(getChildFragmentManager(), "MemberDetailsBottomSheet");
    }

    private void inviteMember(String email, String role) {
        showLoading(true);
        
        repository.inviteMember(projectId, email, role, new IMemberRepository.RepositoryCallback<Member>() {
            @Override
            public void onSuccess(Member data) {
                if (isAdded()) {
                    showLoading(false);
                    Toast.makeText(requireContext(), "Member invited!", Toast.LENGTH_SHORT).show();
                    loadMembers(); // Refresh
                }
            }

            @Override
            public void onError(String error) {
                if (isAdded()) {
                    showLoading(false);
                    Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showChangeRoleDialog(MemberDTO member) {
        String[] roles = {"MEMBER", "ADMIN", "VIEWER"};
        int currentRoleIndex = 0;
        
        // Find current role index
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(member.getRole())) {
                currentRoleIndex = i;
                break;
            }
        }
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Change Role for " + member.getUser().getName())
                .setSingleChoiceItems(roles, currentRoleIndex, null)
                .setPositiveButton("Update", (dialog, which) -> {
                    ListView lv = ((AlertDialog) dialog).getListView();
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
        showLoading(true);
        
        repository.updateMemberRole(projectId, memberId, newRole, 
            new IMemberRepository.RepositoryCallback<Member>() {
                @Override
                public void onSuccess(Member data) {
                    if (isAdded()) {
                        showLoading(false);
                        Toast.makeText(requireContext(), "Role updated!", Toast.LENGTH_SHORT).show();
                        loadMembers(); // Refresh
                    }
                }

                @Override
                public void onError(String error) {
                    if (isAdded()) {
                        showLoading(false);
                        Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }

    private void showRemoveConfirmation(MemberDTO member) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Member")
                .setMessage("Remove " + member.getUser().getName() + " from this project?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    removeMember(member.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void removeMember(String memberId) {
        showLoading(true);
        
        repository.removeMember(projectId, memberId, 
            new IMemberRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    if (isAdded()) {
                        showLoading(false);
                        Toast.makeText(requireContext(), "Member removed!", Toast.LENGTH_SHORT).show();
                        loadMembers(); // Refresh
                    }
                }

                @Override
                public void onError(String error) {
                    if (isAdded()) {
                        showLoading(false);
                        Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateEmptyState(boolean isEmpty) {
        tvNoMembers.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvMembers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
