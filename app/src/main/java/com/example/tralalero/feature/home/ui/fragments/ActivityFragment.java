package com.example.tralalero.feature.home.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

public class ActivityFragment extends Fragment {
    private static final String TAG = "ActivityFragment";
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ActivityLogAdapter adapter;
    private ActivityLogApiService activityLogApiService;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ActivityLogAdapter(getContext());
        
        swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent
        );
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "🔄 Pull-to-refresh - reloading activity feed");
            loadUserActivityFeed();
        });
        
        adapter.setOnInvitationClickListener(log -> {
            Intent intent = new Intent(getContext(), com.example.tralalero.feature.invitations.InvitationsActivity.class);
            startActivity(intent);
        });
        
        recyclerView.setAdapter(adapter);

        AuthManager authManager = new AuthManager(requireActivity().getApplication());
        activityLogApiService = ApiClient.get(authManager).create(ActivityLogApiService.class);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            loadUserActivityFeed();
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserActivityFeed() {
        if (currentUserId == null || getContext() == null) {
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
                            
                            if (swipeRefreshLayout != null) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                            
                            if (activityLogs.isEmpty() && getContext() != null) {
                                Toast.makeText(getContext(), 
                                        "No activities yet. Start creating tasks!", 
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.e(TAG, "Failed to load activities: " + response.code());
                            if (swipeRefreshLayout != null) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                            if (getContext() != null) {
                                Toast.makeText(getContext(), 
                                        "Failed to load activities", 
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ActivityLogDTO>> call, Throwable t) {
                        Log.e(TAG, "Error loading activities", t);
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        if (getContext() != null) {
                            Toast.makeText(getContext(), 
                                    "Error: " + t.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
