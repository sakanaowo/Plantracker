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
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.remote.dto.project.ProjectMemberDTO;
import com.example.tralalero.data.remote.dto.task.TaskAssigneeDTO;
import com.example.tralalero.data.remote.mapper.ProjectMemberMapper;
import com.example.tralalero.data.remote.mapper.TaskAssigneeMapper;
import com.example.tralalero.domain.model.ProjectMember;
import com.example.tralalero.domain.model.TaskAssignee;
import com.example.tralalero.network.ApiClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignMemberBottomSheet extends BottomSheetDialogFragment {
    
    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_TASK_ID = "task_id";
    
    private String projectId;
    private String taskId;
    private Set<String> currentAssigneeIds = new HashSet<>();
    private Set<String> selectedAssigneeIds = new HashSet<>();
    private OnAssigneesChangedListener listener;
    
    private ImageView ivClose;
    private TextInputEditText etSearchMember;
    private RecyclerView rvMembers;
    private TextView tvEmptyState;
    private TextView tvSelectedCount;
    private Button btnSave;
    private Button btnUnassignAll;
    
    private MemberSelectionAdapter adapter;
    private ProjectApiService projectApiService;
    private TaskApiService taskApiService;
    
    public interface OnAssigneesChangedListener {
        void onAssigneesChanged();
    }
    
    public static AssignMemberBottomSheet newInstance(String projectId, String taskId) {
        AssignMemberBottomSheet fragment = new AssignMemberBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        args.putString(ARG_TASK_ID, taskId);
        fragment.setArguments(args);
        return fragment;
    }
    
    public void setOnAssigneesChangedListener(OnAssigneesChangedListener listener) {
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
            taskId = getArguments().getString(ARG_TASK_ID);
        }
        
        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadCurrentAssignees();
    }
    
    private void initViews(View view) {
        ivClose = view.findViewById(R.id.ivClose);
        etSearchMember = view.findViewById(R.id.etSearchMember);
        rvMembers = view.findViewById(R.id.rvMembers);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvSelectedCount = view.findViewById(R.id.tvSelectedCount);
        btnSave = view.findViewById(R.id.btnSave);
        btnUnassignAll = view.findViewById(R.id.btnUnassignAll);
        
        projectApiService = ApiClient.get(App.authManager).create(ProjectApiService.class);
        taskApiService = ApiClient.get(App.authManager).create(TaskApiService.class);
    }
    
    private void setupRecyclerView() {
        adapter = new MemberSelectionAdapter(selectedIds -> {
            selectedAssigneeIds = new HashSet<>(selectedIds);
            updateSelectedCount();
        });
        
        rvMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMembers.setAdapter(adapter);
    }
    
    private void setupListeners() {
        ivClose.setOnClickListener(v -> dismiss());
        
        btnSave.setOnClickListener(v -> saveChanges());
        
        btnUnassignAll.setOnClickListener(v -> unassignAll());
        
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
    
    private void loadCurrentAssignees() {
        if (taskId == null) {
            loadMembers();
            return;
        }
        
        taskApiService.getTaskAssignees(taskId).enqueue(new Callback<List<TaskAssigneeDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<TaskAssigneeDTO>> call, @NonNull Response<List<TaskAssigneeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentAssigneeIds.clear();
                    for (TaskAssigneeDTO dto : response.body()) {
                        TaskAssignee assignee = TaskAssigneeMapper.toDomain(dto);
                        if (assignee != null) {
                            currentAssigneeIds.add(assignee.getUserId());
                        }
                    }
                    selectedAssigneeIds = new HashSet<>(currentAssigneeIds);
                    loadMembers();
                } else {
                    loadMembers();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<List<TaskAssigneeDTO>> call, @NonNull Throwable t) {
                showError("Failed to load current assignees");
                loadMembers();
            }
        });
    }
    
    private void loadMembers() {
        if (projectId == null) {
            showError("Invalid project");
            return;
        }
        
        projectApiService.getProjectMembers(projectId).enqueue(new Callback<List<ProjectMemberDTO>>() {
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
                        adapter.setSelectedUserIds(selectedAssigneeIds);
                        updateSelectedCount();
                        updateUnassignAllButton();
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
    
    private void saveChanges() {
        if (taskId == null) {
            showError("Invalid task");
            return;
        }
        
        // Find users to add and remove
        Set<String> toAdd = new HashSet<>(selectedAssigneeIds);
        toAdd.removeAll(currentAssigneeIds);
        
        Set<String> toRemove = new HashSet<>(currentAssigneeIds);
        toRemove.removeAll(selectedAssigneeIds);
        
        // If no changes, just dismiss
        if (toAdd.isEmpty() && toRemove.isEmpty()) {
            dismiss();
            return;
        }
        
        btnSave.setEnabled(false);
        
        // Remove users first
        if (!toRemove.isEmpty()) {
            for (String userId : toRemove) {
                taskApiService.unassignUser(taskId, userId).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        // Silent success
                    }
                    
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        showError("Failed to remove assignee");
                    }
                });
            }
        }
        
        // Add users
        if (!toAdd.isEmpty()) {
            Map<String, List<String>> body = new HashMap<>();
            body.put("userIds", new ArrayList<>(toAdd));
            
            taskApiService.assignUsers(taskId, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        if (listener != null) {
                            listener.onAssigneesChanged();
                        }
                        dismiss();
                    } else {
                        btnSave.setEnabled(true);
                        showError("Failed to assign users");
                    }
                }
                
                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    btnSave.setEnabled(true);
                    showError("Network error: " + t.getMessage());
                }
            });
        } else {
            // Only removals, notify and dismiss
            if (listener != null) {
                listener.onAssigneesChanged();
            }
            dismiss();
        }
    }
    
    private void unassignAll() {
        if (taskId == null || currentAssigneeIds.isEmpty()) {
            return;
        }
        
        btnUnassignAll.setEnabled(false);
        
        taskApiService.unassignAll(taskId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    if (listener != null) {
                        listener.onAssigneesChanged();
                    }
                    dismiss();
                } else {
                    btnUnassignAll.setEnabled(true);
                    showError("Failed to unassign all");
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                btnUnassignAll.setEnabled(true);
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void updateSelectedCount() {
        if (tvSelectedCount != null) {
            int count = selectedAssigneeIds.size();
            tvSelectedCount.setText(count + " selected");
            tvSelectedCount.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        }
    }
    
    private void updateUnassignAllButton() {
        if (btnUnassignAll != null) {
            btnUnassignAll.setVisibility(currentAssigneeIds.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }
    
    private void showEmptyState() {
        tvEmptyState.setVisibility(View.VISIBLE);
        rvMembers.setVisibility(View.GONE);
    }
    
    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
