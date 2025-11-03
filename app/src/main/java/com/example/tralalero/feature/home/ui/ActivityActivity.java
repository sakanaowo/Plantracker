package com.example.tralalero.feature.home.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.data.mapper.ActivityLogMapper;
import com.example.tralalero.data.remote.api.ActivityLogApiService;
import com.example.tralalero.data.remote.dto.activity.ActivityLogDTO;
import com.example.tralalero.domain.model.ActivityLog;
import com.example.tralalero.feature.home.ui.adapter.ActivityLogAdapter;
import com.example.tralalero.network.ApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityActivity extends BaseActivity {
    private static final String TAG = "ActivityActivity";
    
    private RecyclerView recyclerView;
    private ActivityLogAdapter adapter;
    private ActivityLogApiService activityLogApiService;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_activity_notification);
        
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

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActivityLogAdapter(this);
        
        // Set invitation click listener
        adapter.setOnInvitationClickListener(log -> {
            // Navigate to InvitationsActivity to respond
            Intent intent = new Intent(this, com.example.tralalero.feature.invitations.InvitationsActivity.class);
            startActivity(intent);
        });
        
        recyclerView.setAdapter(adapter);

        // Initialize API service with authentication
        AuthManager authManager = new AuthManager(getApplication());
        activityLogApiService = ApiClient.get(authManager).create(ActivityLogApiService.class);

        // Get current user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            loadUserActivityFeed();
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
        
        setupBottomNavigation(2); 
    }

    private void loadUserActivityFeed() {
        if (currentUserId == null) {
            return;
        }

        Log.d(TAG, "Loading activity feed for user: " + currentUserId);

        activityLogApiService.getUserActivityFeed(currentUserId, 50)
                .enqueue(new Callback<List<ActivityLogDTO>>() {
                    @Override
                    public void onResponse(Call<List<ActivityLogDTO>> call, Response<List<ActivityLogDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ActivityLogDTO> dtos = response.body();
                            Log.d(TAG, "Received " + dtos.size() + " activity logs");
                            
                            List<ActivityLog> activityLogs = new ArrayList<>();
                            for (ActivityLogDTO dto : dtos) {
                                activityLogs.add(ActivityLogMapper.toDomain(dto));
                            }
                            
                            adapter.setActivityLogs(activityLogs);
                            
                            if (activityLogs.isEmpty()) {
                                Toast.makeText(ActivityActivity.this, 
                                        "No activities yet. Start creating tasks!", 
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.e(TAG, "Failed to load activities: " + response.code() + " - " + response.message());
                            Toast.makeText(ActivityActivity.this, 
                                    "Failed to load activities", 
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ActivityLogDTO>> call, Throwable t) {
                        Log.e(TAG, "Error loading activities", t);
                        Toast.makeText(ActivityActivity.this, 
                                "Error: " + t.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
