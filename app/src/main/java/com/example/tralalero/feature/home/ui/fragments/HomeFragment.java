package com.example.tralalero.feature.home.ui.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tralalero.App.App;
import com.example.tralalero.R;
import com.example.tralalero.adapter.ProjectAdapter;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.repository.ProjectRepositoryImpl;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IProjectRepository;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.feature.home.ui.Home.WorkspaceActivity;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.presentation.viewmodel.AuthViewModel;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;
import com.example.tralalero.service.MyFirebaseMessagingService;
import com.example.tralalero.util.FCMHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerBoard;
    private ProjectAdapter projectAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = "HomeFragment";
    private IProjectRepository projectRepository;
    private AuthViewModel authViewModel;
    
    // Broadcast receiver for project updates
    private BroadcastReceiver projectUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received PROJECT_UPDATED broadcast");
            loadProjects();
        }
    };

    // FCM Permission Launcher (Android 13+)
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Notification permission granted");
                    getFCMTokenAndRegister();
                } else {
                    Log.w(TAG, "Notification permission denied");
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Thông báo đẩy đã bị tắt", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViewModels();
        setupRecyclerView(view);
        setupSwipeRefresh(view);
        loadProjects();
        
        // Register broadcast receiver for project updates
        androidx.localbroadcastmanager.content.LocalBroadcastManager
                .getInstance(requireContext())
                .registerReceiver(projectUpdateReceiver, new IntentFilter("PROJECT_UPDATED"));

        setupNotificationPermission();
        setupAddProjectButton(view);
        setupQuickTaskInput(view);
    }
    
    private void setupViewModels() {
        projectRepository = new ProjectRepositoryImpl(requireContext());
        authViewModel = new ViewModelProvider(
                this,
                ViewModelFactoryProvider.provideAuthViewModelFactory()
        ).get(AuthViewModel.class);
    }

    private void loadProjects() {
        Log.d(TAG, "Loading all user projects...");
        final long startTime = System.currentTimeMillis();
        
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        projectRepository.getAllUserProjects(new IProjectRepository.RepositoryCallback<List<Project>>() {
            @Override
            public void onSuccess(List<Project> projects) {
                long duration = System.currentTimeMillis() - startTime;
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        if (projects != null && !projects.isEmpty()) {
                            projectAdapter.setProjectList(projects);
                            String message = "✓ Loaded " + projects.size() + " projects (" + duration + "ms)";
                            Log.i(TAG, message);
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "No projects found");
                            Toast.makeText(getContext(), "No projects available", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading projects: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        Toast.makeText(getContext(),
                            "Error loading projects: " + error, 
                            Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void setupSwipeRefresh(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
            );
            swipeRefreshLayout.setOnRefreshListener(() -> {
                Log.d(TAG, "🔄 User triggered pull-to-refresh - clearing cache");
                projectRepository.clearCache();
                loadProjects();
            });
        }
    }

    private void setupRecyclerView(View view) {
        recyclerBoard = view.findViewById(R.id.recyclerBoard);
        recyclerBoard.setLayoutManager(new LinearLayoutManager(getContext()));
        projectAdapter = new ProjectAdapter(getContext());
        projectAdapter.setOnProjectClickListener(project -> {
            String projectId = project.getId();
            String projectName = project.getName();
            String workspaceId = project.getWorkspaceId();
            Log.d(TAG, "Clicked project: " + projectName + " (ID: " + projectId + ")");
            
            Intent intent = new Intent(getContext(), WorkspaceActivity.class);
            intent.putExtra("WORKSPACE_ID", workspaceId);
            intent.putExtra("PROJECT_ID", projectId);
            intent.putExtra("PROJECT_NAME", projectName);
            startActivity(intent);
        });
        recyclerBoard.setAdapter(projectAdapter);
    }
    
    private void setupAddProjectButton(View view) {
        ImageButton btnAddProject = view.findViewById(R.id.btnAddProject);
        if (btnAddProject != null) {
            btnAddProject.setOnClickListener(v -> {
                showCreateProjectDialog();
            });
        }
    }
    
    private void showCreateProjectDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_project, null);
        EditText etProjectName = dialogView.findViewById(R.id.etProjectName);
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Create New Project");
        builder.setView(dialogView);
        builder.setPositiveButton("Create", null);
        builder.setNegativeButton("Cancel", null);
        
        android.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String projectName = etProjectName.getText().toString().trim();
                
                if (projectName.isEmpty()) {
                    etProjectName.setError("Project name cannot be empty");
                    Toast.makeText(getContext(), "Project name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                createProjectInOwnedWorkspace(projectName);
                dialog.dismiss();
            });
        });
        
        dialog.show();
    }
    
    private void createProjectInOwnedWorkspace(String projectName) {
        Log.d(TAG, "Creating project: " + projectName);
        Toast.makeText(getContext(), "Creating project...", Toast.LENGTH_SHORT).show();
        
        Project newProject = new Project(
            null, null, projectName, null, null, "KANBAN", null
        );
        
        projectRepository.createProject(null, newProject, new IProjectRepository.RepositoryCallback<Project>() {
            @Override
            public void onSuccess(Project project) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d(TAG, "✓ Project created successfully: " + project.getName());
                        Toast.makeText(getContext(), "Project created!", Toast.LENGTH_SHORT).show();
                        loadProjects();
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e(TAG, "✗ Failed to create project: " + error);
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void setupNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                getFCMTokenAndRegister();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            getFCMTokenAndRegister();
        }
    }

    private void getFCMTokenAndRegister() {
        if (getContext() == null) return;
        
        FCMHelper.getFCMToken(getContext(), new FCMHelper.FCMTokenCallback() {
            @Override
            public void onSuccess(String token) {
                Log.d(TAG, "FCM Token received: " + token);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Đã kích hoạt thông báo đẩy", Toast.LENGTH_SHORT).show();
                    MyFirebaseMessagingService.registerTokenWithBackend(getContext(), token);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to get FCM token", e);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Không thể lấy FCM token", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void setupQuickTaskInput(View view) {
        EditText cardNew = view.findViewById(R.id.cardNew);
        LinearLayout inboxForm = view.findViewById(R.id.inboxForm);
        
        cardNew.setOnClickListener(v -> inboxForm.setVisibility(View.VISIBLE));
        
        Button btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
            inboxForm.setVisibility(View.GONE);
            cardNew.setText("");
            hideKeyboard(v);
        });
        
        Button btnAdd = view.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            String text = cardNew.getText().toString().trim();
            if (!text.isEmpty()) {
                createQuickTask(text);
                inboxForm.setVisibility(View.GONE);
                cardNew.setText("");
                hideKeyboard(v);
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void createQuickTask(String title) {
        Log.d(TAG, "Creating quick task: " + title);
        Toast.makeText(getContext(), "Creating task...", Toast.LENGTH_SHORT).show();
        
        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        ITaskRepository taskRepository = new TaskRepositoryImpl(apiService, null, null);
        
        taskRepository.createQuickTask(title, "", new ITaskRepository.RepositoryCallback<com.example.tralalero.domain.model.Task>() {
            @Override
            public void onSuccess(com.example.tralalero.domain.model.Task task) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d(TAG, "✓ Quick task created: " + task.getTitle());
                        Toast.makeText(getContext(), "✓ Task added to inbox", Toast.LENGTH_SHORT).show();
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e(TAG, "✗ Failed to create quick task: " + error);
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
    
    private void hideKeyboard(View view) {
        if (view != null && getContext() != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                view.clearFocus();
            }
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        androidx.localbroadcastmanager.content.LocalBroadcastManager
                .getInstance(requireContext())
                .unregisterReceiver(projectUpdateReceiver);
    }
}
