package com.example.tralalero.feature.home.ui.Home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tralalero.R;
import com.example.tralalero.feature.home.ui.Home.project.ListProjectAdapter;
import com.example.tralalero.feature.home.ui.InboxActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProjectActivity extends AppCompatActivity {
    private static final String TAG = "ProjectActivity";
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

        TabLayout tabLayout = findViewById(R.id.tabLayout1);
        ViewPager2 viewPager2 = findViewById(R.id.PrjViewPager2);
        
        // Lấy project_id từ Intent
        String projectId = getIntent().getStringExtra("project_id");
        String projectName = getIntent().getStringExtra("project_name");
        String workspaceId = getIntent().getStringExtra("workspace_id");
        
        // Debug logging
        Log.d(TAG, "=== ProjectActivity Started ===");
        Log.d(TAG, "Project ID: " + projectId);
        Log.d(TAG, "Project Name: " + projectName);
        Log.d(TAG, "Workspace ID: " + workspaceId);
        
        if (projectId == null || projectId.isEmpty()) {
            Log.e(TAG, "ERROR: No project_id provided! Check Intent extras.");
        }
        
        // Sử dụng ListProjectAdapter mới với projectId
        ListProjectAdapter adapter = new ListProjectAdapter(this, projectId);
        viewPager2.setAdapter(adapter);

        ImageButton backButton = findViewById(R.id.btnClosePjrDetail);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectActivity.this, WorkspaceActivity.class);
            intent.putExtra("workspace_id", workspaceId); // truyền workspace ID
            startActivity(intent);
        });

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
    }
}
