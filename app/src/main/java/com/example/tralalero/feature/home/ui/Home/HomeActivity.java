package com.example.tralalero.feature.home.ui.Home;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tralalero.App.App;
import com.example.tralalero.R;
import com.example.tralalero.adapter.ProjectAdapter;
import com.example.tralalero.data.repository.ProjectRepositoryImpl;
import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IProjectRepository;
import com.example.tralalero.feature.home.ui.BaseActivity;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;
import com.example.tralalero.service.MyFirebaseMessagingService;
import com.example.tralalero.util.FCMHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.tralalero.presentation.viewmodel.AuthViewModel;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.network.ApiClient;

import java.util.List;

public class HomeActivity extends BaseActivity {
    private RecyclerView recyclerBoard;
    private ProjectAdapter projectAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = "HomeActivity";
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
                    // Trigger FCM token registration
                    getFCMTokenAndRegister();
                } else {
                    Log.w(TAG, "Notification permission denied");
                    Toast.makeText(this, "Thông báo đẩy đã bị tắt", Toast.LENGTH_SHORT).show();
                }
            });
    private void setupViewModels() {
        projectRepository = new ProjectRepositoryImpl(this);
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
                
                runOnUiThread(() -> {
                    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    if (projects != null && !projects.isEmpty()) {
                        projectAdapter.setProjectList(projects);
                        String message = "✓ Loaded " + projects.size() + " projects (" + duration + "ms)";
                        Log.i(TAG, message);
                        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "No projects found");
                        Toast.makeText(HomeActivity.this, "No projects available", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading projects: " + error);
                runOnUiThread(() -> {
                    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    Toast.makeText(HomeActivity.this,
                        "Error loading projects: " + error, 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
            );
            swipeRefreshLayout.setOnRefreshListener(() -> {
                Log.d(TAG, "🔄 User triggered pull-to-refresh - clearing cache");
                // Clear cache to force fresh data from API
                projectRepository.clearCache();
                loadProjects();
            });
        }
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                view.clearFocus();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        View titleView = findViewById(R.id.tvTitle);
        View bottomNav = findViewById(R.id.bottomNavigation);

        ViewCompat.setOnApplyWindowInsetsListener(titleView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return WindowInsetsCompat.CONSUMED;
        });

        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        setupViewModels();
        setupRecyclerView();
        setupSwipeRefresh();
        loadProjects();  // Load all user projects
        
        // Register broadcast receiver for project updates
        androidx.localbroadcastmanager.content.LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(projectUpdateReceiver, new IntentFilter("PROJECT_UPDATED"));

        setupTestRepositoryButton();
        setupNotificationPermission(); // Request notification permission
        setupAddProjectButton();

        EditText cardNew = findViewById(R.id.cardNew);
        LinearLayout inboxForm = findViewById(R.id.inboxForm);
        cardNew.setOnClickListener(v -> inboxForm.setVisibility(View.VISIBLE));
        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
                    inboxForm.setVisibility(View.GONE);
                    cardNew.setText("");
                    hideKeyboard(v);
                }
        );
        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            String text = cardNew.getText().toString().trim();
            if (!text.isEmpty()) {
                // Create quick task via repository (same as InboxActivity)
                createQuickTask(text);
                inboxForm.setVisibility(View.GONE);
                cardNew.setText(""); // clear sau khi lưu
                hideKeyboard(v);
            } else {
                Toast.makeText(this, "Vui lòng nhập dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
        setupBottomNavigation(0);
    }
    private void setupRecyclerView() {
        recyclerBoard = findViewById(R.id.recyclerBoard);
        recyclerBoard.setLayoutManager(new LinearLayoutManager(this));
        projectAdapter = new ProjectAdapter(this);
        projectAdapter.setOnProjectClickListener(project -> {
            String projectId = project.getId();
            String projectName = project.getName();
            String workspaceId = project.getWorkspaceId();
            Log.d(TAG, "Clicked project: " + projectName + " (ID: " + projectId + ")");
            
            // Navigate to WorkspaceActivity with project selected
            Intent intent = new Intent(HomeActivity.this, WorkspaceActivity.class);
            intent.putExtra("WORKSPACE_ID", workspaceId);
            intent.putExtra("PROJECT_ID", projectId);
            intent.putExtra("PROJECT_NAME", projectName);
            startActivity(intent);
        });
        recyclerBoard.setAdapter(projectAdapter);
    }
    private void setupTestRepositoryButton() {
        FloatingActionButton fabTest = findViewById(R.id.fabTestRepository);
        if (fabTest != null) {
            fabTest.setVisibility(View.GONE);

            fabTest.setVisibility(View.GONE);
        }
    }
    
    /**
     * Setup add project button to show workspace selection dialog
     */
    private void setupAddProjectButton() {
        ImageButton btnAddProject = findViewById(R.id.btnAddProject);
        if (btnAddProject != null) {
            btnAddProject.setOnClickListener(v -> {
                showCreateProjectDialog();
            });
        }
    }
    
    /**
     * Show dialog to create a new project
     * User needs to select a workspace first
     */
    private void showCreateProjectDialog() {
        // Show dialog directly - will use user's first owned workspace
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_project, null);
        EditText etProjectName = dialogView.findViewById(R.id.etProjectName);
        EditText etProjectDescription = dialogView.findViewById(R.id.etProjectDescription);
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Create New Project");
        builder.setView(dialogView);
        builder.setPositiveButton("Create", null); // Set null first
        builder.setNegativeButton("Cancel", null);
        
        android.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String projectName = etProjectName.getText().toString().trim();
                String projectDescription = etProjectDescription.getText().toString().trim();
                
                if (projectName.isEmpty()) {
                    etProjectName.setError("Project name cannot be empty");
                    Toast.makeText(this, "Project name cannot be empty", Toast.LENGTH_SHORT).show();
                    return; // Don't close dialog
                }
                
                Log.d(TAG, "Dialog Create button clicked - calling createProjectInOwnedWorkspace");
                createProjectInOwnedWorkspace(projectName, projectDescription);
                dialog.dismiss();
            });
        });
        
        dialog.show();
    }
    
    /**
     * Create project directly - backend will auto-find user's workspace
     */
    private void createProjectInOwnedWorkspace(String projectName, String projectDescription) {
        Log.d(TAG, "Creating project: " + projectName);
        
        // Show loading
        Toast.makeText(this, "Creating project...", Toast.LENGTH_SHORT).show();
        
        // Create Project object (null for id, workspaceId, key, boardType - backend will fill these)
        Project newProject = new Project(
            null,              // id - backend generates
            null,              // workspaceId - backend auto-finds from user
            projectName, 
            projectDescription,
            null,              // key - backend generates from name
            "KANBAN"           // boardType - default to KANBAN
        );
        
        // Call repository to create project (pass null for workspaceId - backend will auto-find)
        IProjectRepository projectRepository = new ProjectRepositoryImpl(this);
        projectRepository.createProject(null, newProject, new IProjectRepository.RepositoryCallback<Project>() {
            @Override
            public void onSuccess(Project project) {
                runOnUiThread(() -> {
                    Log.d(TAG, "✓ Project created successfully: " + project.getName());
                    Toast.makeText(HomeActivity.this, "Project created!", Toast.LENGTH_SHORT).show();
                    
                    // Refresh project list
                    loadProjects();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "✗ Failed to create project: " + error);
                    Toast.makeText(HomeActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Request notification permission for Android 13+ (API 33+)
     * For Android 12 and below, permission is granted by default
     */
    private void setupNotificationPermission() {
        // Android 13+ requires runtime permission for notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission already granted");
                // Already granted, trigger FCM token registration
                getFCMTokenAndRegister();
            } else {
                // Request permission
                Log.d(TAG, "Requesting notification permission");
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            // Android 12 and below - no runtime permission needed
            Log.d(TAG, "Android < 13, notification permission not required");
            getFCMTokenAndRegister();
        }
    }

    /**
     * Get FCM token and register with backend server
     */
    private void getFCMTokenAndRegister() {
        Log.d(TAG, "getFCMTokenAndRegister called");
        FCMHelper.getFCMToken(this, new FCMHelper.FCMTokenCallback() {
            @Override
            public void onSuccess(String token) {
                Log.d(TAG, "FCM Token received: " + token);
                Toast.makeText(HomeActivity.this, "Đã kích hoạt thông báo đẩy", Toast.LENGTH_SHORT).show();
                
                // Send token to backend server
                MyFirebaseMessagingService.registerTokenWithBackend(HomeActivity.this, token);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to get FCM token", e);
                Toast.makeText(HomeActivity.this, "Không thể lấy FCM token", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Create quick task in inbox via API (same logic as InboxActivity)
     */
    private void createQuickTask(String title) {
        Log.d(TAG, "Creating quick task: " + title);
        Toast.makeText(this, "Creating task...", Toast.LENGTH_SHORT).show();
        
        // Create task repository
        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        ITaskRepository taskRepository = new TaskRepositoryImpl(apiService, null, null);
        
        // Call create quick task API
        taskRepository.createQuickTask(title, "", new ITaskRepository.RepositoryCallback<com.example.tralalero.domain.model.Task>() {
            @Override
            public void onSuccess(com.example.tralalero.domain.model.Task task) {
                runOnUiThread(() -> {
                    Log.d(TAG, "✓ Quick task created: " + task.getTitle());
                    Toast.makeText(HomeActivity.this, "✓ Task added to inbox", Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "✗ Failed to create quick task: " + error);
                    Toast.makeText(HomeActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister broadcast receiver
        androidx.localbroadcastmanager.content.LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(projectUpdateReceiver);
    }
}
