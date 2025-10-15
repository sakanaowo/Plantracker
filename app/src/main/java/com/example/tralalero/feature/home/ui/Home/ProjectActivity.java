package com.example.tralalero.feature.home.ui.Home;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.example.tralalero.R;
import com.example.tralalero.domain.model.Board;
import com.example.tralalero.feature.home.ui.Home.project.ListProject;
import com.example.tralalero.feature.home.ui.Home.project.ListProjectAdapter;
import com.example.tralalero.presentation.viewmodel.BoardViewModel;
import com.example.tralalero.presentation.viewmodel.ProjectViewModel;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;
public class ProjectActivity extends AppCompatActivity {
    private static final String TAG = "ProjectActivity";
    private ImageButton backButton;
    private ProjectViewModel projectViewModel;
    private BoardViewModel boardViewModel;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private ListProjectAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvProjectName;
    private TextView tvWorkspaceName;
    private String projectId;
    private String projectName;
    private String workspaceId;
    private String workspaceName;
    private List<String> boardIds = new ArrayList<>();
    private int boardsCreatedCount = 0;
    private boolean isDefaultBoardsCreated = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.project_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getIntentData();
        setupViewModels();
        initViews();
        observeViewModels();
        setupViewPager();
        setupBackButton();
        loadBoards();
    }
    private void getIntentData() {
        projectId = getIntent().getStringExtra("project_id");
        projectName = getIntent().getStringExtra("project_name");
        workspaceId = getIntent().getStringExtra("workspace_id");
        workspaceName = getIntent().getStringExtra("workspace_name");
        Log.d(TAG, "=== ProjectActivity Started ===");
        Log.d(TAG, "Project ID: " + projectId);
        Log.d(TAG, "Project Name: " + projectName);
        Log.d(TAG, "Workspace ID: " + workspaceId);
        Log.d(TAG, "Workspace Name: " + workspaceName);
        if (projectId == null || projectId.isEmpty()) {
            Log.e(TAG, "ERROR: No project_id provided! Check Intent extras.");
            Toast.makeText(this, "Error: No project ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void setupViewModels() {
        projectViewModel = new ViewModelProvider(this,
                ViewModelFactoryProvider.provideProjectViewModelFactory()
        ).get(ProjectViewModel.class);
        boardViewModel = new ViewModelProvider(this,
                ViewModelFactoryProvider.provideBoardViewModelFactory()
        ).get(BoardViewModel.class);
        Log.d(TAG, "ViewModels setup complete");
    }
    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout1);
        viewPager2 = findViewById(R.id.PrjViewPager2);
        backButton = findViewById(R.id.btnClosePjrDetail);
        progressBar = findViewById(R.id.progressBar);
        tvProjectName = findViewById(R.id.tvProjectName);
        tvWorkspaceName = findViewById(R.id.tvWorkspaceName);
        if (progressBar == null) {
            Log.w(TAG, "ProgressBar not found in layout");
        }
        if (projectName != null && !projectName.isEmpty()) {
            tvProjectName.setText(projectName);
        } else {
            tvProjectName.setText("Project");
        }
        if (workspaceName != null && !workspaceName.isEmpty()) {
            tvWorkspaceName.setText(workspaceName);
        } else {
            tvWorkspaceName.setText("Workspace");
        }
    }
    private void setupViewPager() {
        adapter = new ListProjectAdapter(this, projectId);
        viewPager2.setAdapter(adapter);
        Log.d(TAG, "ViewPager2 setup complete");
    }
    private void setupBackButton() {
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectActivity.this, WorkspaceActivity.class);
            intent.putExtra("WORKSPACE_ID", workspaceId);
            startActivity(intent);
            finish();
        });
    }
    private void observeViewModels() {
        projectViewModel.getSelectedProject().observe(this, project -> {
            if (project != null) {
                Log.d(TAG, "Project loaded: " + project.getName());
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(project.getName());
                }
            }
        });
        projectViewModel.isLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            Log.d(TAG, "Project loading state: " + isLoading);
        });
        boardViewModel.isLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            Log.d(TAG, "Board loading state: " + isLoading);
        });
        projectViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Project error: " + error);
                projectViewModel.clearError();
            }
        });
        boardViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Board Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Board error: " + error);
                boardViewModel.clearError();
            }
        });
        boardViewModel.getProjectBoards().observe(this, boards -> {
            if (boards != null && !boards.isEmpty()) {
                Log.d(TAG, "✅ Boards loaded successfully: " + boards.size() + " boards");
                setupTabsWithBoardIds(boards);
            } else {
                Log.w(TAG, "⚠️ No boards found for project");
                Toast.makeText(this, "No boards found", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadBoards() {
        Log.d(TAG, "Loading boards for project: " + projectId);
        if (projectId == null || projectId.isEmpty()) {
            Log.e(TAG, "❌ Cannot load boards: projectId is null or empty");
            Toast.makeText(this, "Error: Invalid project ID", Toast.LENGTH_SHORT).show();
            return;
        }
        boardViewModel.loadBoardsByProject(projectId);
    }
    private void setupTabsWithDefaultBoards() {
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("TO DO");
                    break;
                case 1:
                    tab.setText("IN PROGRESS");
                    break;
                case 2:
                    tab.setText("DONE");
                    break;
            }
        }).attach();
        Log.d(TAG, "Tabs setup complete with " + adapter.getItemCount() + " tabs");
    }
    private void setupTabsWithBoardIds(List<Board> boards) {
        if (boards == null || boards.isEmpty()) {
            Log.e(TAG, "No boards to setup tabs");
            return;
        }
        boardIds.clear();
        for (Board board : boards) {
            boardIds.add(board.getId());
        }
        adapter.setBoardIds(boardIds);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            if (position < boards.size()) {
                tab.setText(boards.get(position).getName());
            }
        }).attach();
        Log.d(TAG, "Tabs setup with " + boards.size() + " boards");
        notifyFragmentsToReload();
    }
    private void notifyFragmentsToReload() {
        viewPager2.post(() -> {
            for (int position = 0; position < adapter.getItemCount(); position++) {
                String boardId = adapter.getBoardIdForPosition(position);
                if (boardId != null && !boardId.isEmpty()) {
                    Fragment fragment = getSupportFragmentManager()
                            .findFragmentByTag("f" + position);
                    if (fragment instanceof ListProject) {
                        ListProject listProjectFragment = (ListProject) fragment;
                        listProjectFragment.updateBoardIdAndReload(boardId);
                        Log.d(TAG, "Updated fragment at position " + position + " with boardId: " + boardId);
                    }
                }
            }
        });
    }
}
