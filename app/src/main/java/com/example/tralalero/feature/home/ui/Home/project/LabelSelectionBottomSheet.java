package com.example.tralalero.feature.home.ui.Home.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.App.App;
import com.example.tralalero.R;
import com.example.tralalero.data.remote.api.LabelApiService;
import com.example.tralalero.data.repository.LabelRepositoryImpl;
import com.example.tralalero.domain.model.Label;
import com.example.tralalero.domain.repository.ILabelRepository;
import com.example.tralalero.domain.usecase.label.AssignLabelToTaskUseCase;
import com.example.tralalero.domain.usecase.label.CreateLabelInProjectUseCase;
import com.example.tralalero.domain.usecase.label.CreateLabelUseCase;
import com.example.tralalero.domain.usecase.label.DeleteLabelUseCase;
import com.example.tralalero.domain.usecase.label.GetLabelByIdUseCase;
import com.example.tralalero.domain.usecase.label.GetLabelsByProjectUseCase;
import com.example.tralalero.domain.usecase.label.GetLabelsByWorkspaceUseCase;
import com.example.tralalero.domain.usecase.label.GetTaskLabelsUseCase;
import com.example.tralalero.domain.usecase.label.RemoveLabelFromTaskUseCase;
import com.example.tralalero.domain.usecase.label.UpdateLabelUseCase;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.presentation.viewmodel.LabelViewModel;
import com.example.tralalero.presentation.viewmodel.LabelViewModelFactory;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class LabelSelectionBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_TASK_ID = "task_id";
    private static final String ARG_SELECTED_LABEL_IDS = "selected_label_ids";

    private ImageView ivClose, ivAddLabel;
    private RecyclerView rvLabels;
    private TextView tvNoLabels;

    private LabelSelectionAdapter adapter;
    private LabelViewModel labelViewModel;
    private String projectId;
    private String taskId;
    private List<String> selectedLabelIds = new ArrayList<>();
    private List<Label> allLabels = new ArrayList<>();
    private OnLabelsUpdatedListener listener;

    public interface OnLabelsUpdatedListener {
        void onLabelsUpdated();
    }

    public static LabelSelectionBottomSheet newInstance(String projectId, String taskId, List<String> selectedLabelIds) {
        LabelSelectionBottomSheet fragment = new LabelSelectionBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        args.putString(ARG_TASK_ID, taskId);
        args.putStringArrayList(ARG_SELECTED_LABEL_IDS, new ArrayList<>(selectedLabelIds != null ? selectedLabelIds : new ArrayList<>()));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PROJECT_ID);
            taskId = getArguments().getString(ARG_TASK_ID);
            selectedLabelIds = getArguments().getStringArrayList(ARG_SELECTED_LABEL_IDS);
            if (selectedLabelIds == null) {
                selectedLabelIds = new ArrayList<>();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_label_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupListeners();
        loadLabels();
    }

    private void initViews(View view) {
        ivClose = view.findViewById(R.id.ivClose);
        ivAddLabel = view.findViewById(R.id.ivAddLabel);
        rvLabels = view.findViewById(R.id.rvLabels);
        tvNoLabels = view.findViewById(R.id.tvNoLabels);
    }

    private void setupViewModel() {
        // Initialize API service
        LabelApiService apiService = ApiClient.get(App.authManager).create(LabelApiService.class);
        
        // Initialize repository
        ILabelRepository repository = new LabelRepositoryImpl(apiService);
        
        // Initialize use cases
        GetLabelsByWorkspaceUseCase getLabelsByWorkspaceUseCase = new GetLabelsByWorkspaceUseCase(repository);
        GetLabelsByProjectUseCase getLabelsByProjectUseCase = new GetLabelsByProjectUseCase(repository);
        GetLabelByIdUseCase getLabelByIdUseCase = new GetLabelByIdUseCase(repository);
        CreateLabelUseCase createLabelUseCase = new CreateLabelUseCase(repository);
        CreateLabelInProjectUseCase createLabelInProjectUseCase = new CreateLabelInProjectUseCase(repository);
        UpdateLabelUseCase updateLabelUseCase = new UpdateLabelUseCase(repository);
        DeleteLabelUseCase deleteLabelUseCase = new DeleteLabelUseCase(repository);
        GetTaskLabelsUseCase getTaskLabelsUseCase = new GetTaskLabelsUseCase(repository);
        AssignLabelToTaskUseCase assignLabelToTaskUseCase = new AssignLabelToTaskUseCase(repository);
        RemoveLabelFromTaskUseCase removeLabelFromTaskUseCase = new RemoveLabelFromTaskUseCase(repository);
        
        // Create factory
        LabelViewModelFactory factory = new LabelViewModelFactory(
                getLabelsByWorkspaceUseCase,
                getLabelsByProjectUseCase,
                getLabelByIdUseCase,
                createLabelUseCase,
                createLabelInProjectUseCase,
                updateLabelUseCase,
                deleteLabelUseCase,
                getTaskLabelsUseCase,
                assignLabelToTaskUseCase,
                removeLabelFromTaskUseCase
        );
        
        // Initialize ViewModel
        labelViewModel = new ViewModelProvider(this, factory).get(LabelViewModel.class);
        
        // Observe labels data
        labelViewModel.getLabels().observe(getViewLifecycleOwner(), labels -> {
            if (labels != null) {
                allLabels = labels;
                adapter.setLabels(labels);
                adapter.setSelectedLabels(selectedLabelIds);
                
                if (labels.isEmpty()) {
                    rvLabels.setVisibility(View.GONE);
                    tvNoLabels.setVisibility(View.VISIBLE);
                } else {
                    rvLabels.setVisibility(View.VISIBLE);
                    tvNoLabels.setVisibility(View.GONE);
                }
            }
        });
        
        // Observe errors
        labelViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observe operation success/failure for rollback
        labelViewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                if (success) {
                    // Operation succeeded
                    // Reload labels after create/update/delete operations
                    if (projectId != null) {
                        labelViewModel.loadLabelsByProject(projectId);
                    }
                } else {
                    // Operation failed - reload to sync with backend
                    // This will update checkboxes to reflect actual backend state
                    if (taskId != null) {
                        labelViewModel.loadTaskLabels(taskId);
                        // Update local selectedLabelIds from backend
                        labelViewModel.getTaskLabels().observe(getViewLifecycleOwner(), taskLabels -> {
                            if (taskLabels != null) {
                                selectedLabelIds.clear();
                                for (Label label : taskLabels) {
                                    selectedLabelIds.add(label.getId());
                                }
                                adapter.setSelectedLabels(selectedLabelIds);
                            }
                        });
                    }
                }
                labelViewModel.resetOperationSuccess();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new LabelSelectionAdapter(new LabelSelectionAdapter.OnLabelActionListener() {
            @Override
            public void onLabelChecked(Label label, boolean isChecked) {
                if (isChecked) {
                    // Optimistic update
                    if (!selectedLabelIds.contains(label.getId())) {
                        selectedLabelIds.add(label.getId());
                    }
                    
                    // Call API to assign label to task
                    if (taskId != null && labelViewModel != null) {
                        labelViewModel.assignLabelToTask(taskId, label.getId());
                    }
                } else {
                    // Optimistic update
                    selectedLabelIds.remove(label.getId());
                    
                    // Call API to remove label from task
                    if (taskId != null && labelViewModel != null) {
                        labelViewModel.removeLabelFromTask(taskId, label.getId());
                    }
                }
            }

            @Override
            public void onEditLabel(Label label) {
                showLabelFormDialog(label);
            }
        });

        rvLabels.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvLabels.setAdapter(adapter);
    }

    private void setupListeners() {
        ivClose.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLabelsUpdated();
            }
            dismiss();
        });

        ivAddLabel.setOnClickListener(v -> showLabelFormDialog(null));
    }

    private void loadLabels() {
        // Load labels from backend via ViewModel
        if (projectId != null && labelViewModel != null) {
            android.util.Log.d("LabelBottomSheet", "Loading labels for projectId: " + projectId);
            labelViewModel.loadLabelsByProject(projectId);
        } else {
            android.util.Log.w("LabelBottomSheet", "Cannot load labels - projectId: " + projectId + ", viewModel: " + labelViewModel);
            // Fallback to mock data if no projectId
            List<Label> mockLabels = getMockLabels();
            allLabels = mockLabels;
            adapter.setLabels(mockLabels);
            adapter.setSelectedLabels(selectedLabelIds);

            if (mockLabels.isEmpty()) {
                rvLabels.setVisibility(View.GONE);
                tvNoLabels.setVisibility(View.VISIBLE);
            } else {
                rvLabels.setVisibility(View.VISIBLE);
                tvNoLabels.setVisibility(View.GONE);
            }
        }
    }

    private List<Label> getMockLabels() {
        // Mock data for testing
        List<Label> labels = new ArrayList<>();
        labels.add(new Label("1", projectId, "Copy Request", "#C9B53B"));
        labels.add(new Label("2", projectId, "Important", "#F2D600"));
        labels.add(new Label("3", projectId, "One more step", "#FF9F1A"));
        labels.add(new Label("4", projectId, "Priority", "#EB5A46"));
        labels.add(new Label("5", projectId, "Design Team", "#C377E0"));
        labels.add(new Label("6", projectId, "Product Marketing", "#0079BF"));
        labels.add(new Label("7", projectId, "Trello Tip", "#00C2E0"));
        labels.add(new Label("8", projectId, "Halp", "#51E898"));
        return labels;
    }

    private void showLabelFormDialog(@Nullable Label label) {
        FragmentManager fm = getChildFragmentManager();
        LabelFormBottomSheet dialog = LabelFormBottomSheet.newInstance(projectId, label);
        dialog.setOnLabelSaveListener(new LabelFormBottomSheet.OnLabelSaveListener() {
            @Override
            public void onLabelSaved(Label savedLabel) {
                if (labelViewModel != null && projectId != null) {
                    if (label == null) {
                        // Create new label via API
                        labelViewModel.createLabelInProject(projectId, savedLabel);
                    } else {
                        // Update existing label via API
                        labelViewModel.updateLabel(label.getId(), savedLabel, projectId);
                    }
                } else {
                    Toast.makeText(requireContext(), "Cannot save label: No project ID", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLabelDeleted(String labelId) {
                if (labelViewModel != null && projectId != null) {
                    // Delete label via API
                    labelViewModel.deleteLabel(labelId, projectId);
                    selectedLabelIds.remove(labelId);
                } else {
                    Toast.makeText(requireContext(), "Cannot delete label: No project ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show(fm, "LabelFormBottomSheet");
    }

    private void assignLabelToTask(String labelId) {
        if (labelViewModel != null && taskId != null) {
            labelViewModel.assignLabelToTask(taskId, labelId);
        }
    }

    private void removeLabelFromTask(String labelId) {
        if (labelViewModel != null && taskId != null) {
            labelViewModel.removeLabelFromTask(taskId, labelId);
        }
    }

    public void setOnLabelsUpdatedListener(OnLabelsUpdatedListener listener) {
        this.listener = listener;
    }
}
