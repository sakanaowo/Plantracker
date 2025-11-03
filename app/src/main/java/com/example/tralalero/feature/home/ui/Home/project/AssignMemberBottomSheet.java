package com.example.tralalero.feature.home.ui.Home.project;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.adapter.MemberSelectionAdapter;
import com.example.tralalero.App.App;
import com.example.tralalero.data.remote.api.ProjectApiService;
import com.example.tralalero.data.remote.dto.project.ProjectMemberDTO;
import com.example.tralalero.data.remote.mapper.ProjectMemberMapper;
import com.example.tralalero.domain.model.ProjectMember;
import com.example.tralalero.network.ApiClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignMemberBottomSheet extends BottomSheetDialogFragment {
    
    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_CURRENT_ASSIGNEE_ID = "current_assignee_id";
    
    private String projectId;
    private String currentAssigneeId;
    private OnMemberSelectedListener listener;
    
    private ImageView ivClose;
    private TextInputEditText etSearchMember;
    private RecyclerView rvMembers;
    private TextView tvEmptyState;
    private Button btnUnassign;
    private View currentAssigneeSection;
    
    private MemberSelectionAdapter adapter;
    private ProjectApiService apiService;
    
    public interface OnMemberSelectedListener {
        void onMemberSelected(ProjectMember member);
        void onUnassign();
    }
    
    public static AssignMemberBottomSheet newInstance(String projectId, String currentAssigneeId) {
        AssignMemberBottomSheet fragment = new AssignMemberBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        args.putString(ARG_CURRENT_ASSIGNEE_ID, currentAssigneeId);
        fragment.setArguments(args);
        return fragment;
    }
    
    public void setOnMemberSelectedListener(OnMemberSelectedListener listener) {
        this.listener = listener;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_assign_member, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PROJECT_ID);
            currentAssigneeId = getArguments().getString(ARG_CURRENT_ASSIGNEE_ID);
        }
        
        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadMembers();
    }
    
    private void initViews(View view) {
        ivClose = view.findViewById(R.id.ivClose);
        etSearchMember = view.findViewById(R.id.etSearchMember);
        rvMembers = view.findViewById(R.id.rvMembers);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        btnUnassign = view.findViewById(R.id.btnUnassign);
        currentAssigneeSection = view.findViewById(R.id.currentAssigneeSection);
        
        apiService = ApiClient.get(App.authManager).create(ProjectApiService.class);
        
        // Show/hide unassign button based on current assignee
        if (currentAssigneeId == null || currentAssigneeId.isEmpty()) {
            btnUnassign.setVisibility(View.GONE);
        }
    }
    
    private void setupRecyclerView() {
        adapter = new MemberSelectionAdapter(member -> {
            if (listener != null) {
                listener.onMemberSelected(member);
            }
            dismiss();
        });
        
        adapter.setCurrentAssigneeId(currentAssigneeId);
        rvMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMembers.setAdapter(adapter);
    }
    
    private void setupListeners() {
        ivClose.setOnClickListener(v -> dismiss());
        
        btnUnassign.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUnassign();
            }
            dismiss();
        });
        
        etSearchMember.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void loadMembers() {
        if (projectId == null) {
            showError("Invalid project");
            return;
        }
        
        apiService.getProjectMembers(projectId).enqueue(new Callback<List<ProjectMemberDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProjectMemberDTO>> call, @NonNull Response<List<ProjectMemberDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProjectMember> members = new ArrayList<>();
                    for (ProjectMemberDTO dto : response.body()) {
                        ProjectMember member = ProjectMemberMapper.toDomain(dto);
                        if (member != null) {
                            members.add(member);
                        }
                    }
                    
                    if (members.isEmpty()) {
                        showEmptyState();
                    } else {
                        adapter.setMembers(members);
                        tvEmptyState.setVisibility(View.GONE);
                        rvMembers.setVisibility(View.VISIBLE);
                    }
                } else {
                    showError("Failed to load members");
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<List<ProjectMemberDTO>> call, @NonNull Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void showEmptyState() {
        tvEmptyState.setVisibility(View.VISIBLE);
        rvMembers.setVisibility(View.GONE);
    }
    
    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
