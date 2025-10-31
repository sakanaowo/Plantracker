package com.example.tralalero.feature.project.labels;

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
import com.example.tralalero.data.mapper.LabelMapper;
import com.example.tralalero.data.remote.api.LabelApiService;
import com.example.tralalero.data.remote.dto.label.LabelDTO;
import com.example.tralalero.data.repository.LabelRepositoryImpl;
import com.example.tralalero.domain.model.Label;
import com.example.tralalero.domain.repository.ILabelRepository;
import com.example.tralalero.network.ApiClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class LabelsFragment extends Fragment {
    
    private static final String TAG = "LabelsFragment";
    private static final String ARG_PROJECT_ID = "project_id";
    
    private String projectId;
    private RecyclerView rvLabels;
    private ProgressBar progressBar;
    private TextView tvNoLabels;
    private FloatingActionButton fabCreate;
    private ImageButton btnBack;
    
    private LabelAdapter adapter;
    private ILabelRepository repository;

    public static LabelsFragment newInstance(String projectId) {
        LabelsFragment fragment = new LabelsFragment();
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
        // Create repository with LabelApiService from ApiClient
        LabelApiService apiService = ApiClient.get().create(LabelApiService.class);
        repository = new LabelRepositoryImpl(apiService);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_labels, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupAdapter();
        setupListeners();
        loadLabels();
    }

    private void initViews(View view) {
        rvLabels = view.findViewById(R.id.rvLabels);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoLabels = view.findViewById(R.id.tvNoLabels);
        fabCreate = view.findViewById(R.id.fabCreateLabel);
        btnBack = view.findViewById(R.id.btnBack);
    }

    private void setupAdapter() {
        adapter = new LabelAdapter();
        adapter.setOnLabelActionListener(label -> {
            showDeleteConfirmation(label);
        });
        
        rvLabels.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvLabels.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        fabCreate.setOnClickListener(v -> showCreateDialog());
    }

    private void loadLabels() {
        showLoading(true);
        
        // Use getLabelsByProject instead of getProjectLabels
        repository.getLabelsByProject(projectId, new ILabelRepository.RepositoryCallback<List<Label>>() {
            @Override
            public void onSuccess(List<Label> data) {
                if (isAdded()) {
                    showLoading(false);
                    // Convert Label domain models to LabelDTO for adapter
                    List<LabelDTO> dtoList = LabelMapper.toDtoList(data);
                    adapter.setLabels(dtoList);
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

    private void showCreateDialog() {
        CreateLabelDialog dialog = CreateLabelDialog.newInstance();
        dialog.setOnLabelActionListener(new CreateLabelDialog.OnLabelActionListener() {
            @Override
            public void onCreateLabel(String name, String color) {
                createLabel(name, color);
            }

            @Override
            public void onUpdateLabel(String labelId, String name, String color) {
                // Not used in create mode
            }
        });
        dialog.show(getChildFragmentManager(), "CreateLabelDialog");
    }

    private void createLabel(String name, String color) {
        showLoading(true);
        
        // Create Label object and use createLabelInProject
        Label label = new Label(null, projectId, name, color);
        repository.createLabelInProject(projectId, label, new ILabelRepository.RepositoryCallback<Label>() {
            @Override
            public void onSuccess(Label data) {
                if (isAdded()) {
                    showLoading(false);
                    Toast.makeText(requireContext(), "Label created!", Toast.LENGTH_SHORT).show();
                    loadLabels(); // Refresh
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

    private void showDeleteConfirmation(LabelDTO label) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Label")
                .setMessage("Delete \"" + label.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteLabel(label);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteLabel(LabelDTO label) {
        showLoading(true);
        
        repository.deleteLabel(label.getId(), new ILabelRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (isAdded()) {
                    showLoading(false);
                    Toast.makeText(requireContext(), "Label deleted!", Toast.LENGTH_SHORT).show();
                    loadLabels(); // Refresh
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

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateEmptyState(boolean isEmpty) {
        tvNoLabels.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvLabels.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
