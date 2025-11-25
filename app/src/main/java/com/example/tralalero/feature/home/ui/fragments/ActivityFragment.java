package com.example.tralalero.feature.home.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.example.tralalero.data.repository.InvitationRepositoryImpl;
import com.example.tralalero.domain.model.ActivityLog;
import com.example.tralalero.domain.model.Invitation;
import com.example.tralalero.domain.repository.IInvitationRepository;
import com.example.tralalero.feature.home.ui.adapter.ActivityLogAdapter;
import com.example.tralalero.feature.invitations.InvitationsAdapter;
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
    
    // Invitations section
    private LinearLayout invitationsSection;
    private RecyclerView rvInvitations;
    private InvitationsAdapter invitationsAdapter;
    private IInvitationRepository invitationRepository;
    
    // Activity feed section
    private RecyclerView rvActivityFeed;
    private TextView tvEmptyActivity;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ActivityLogAdapter activityAdapter;
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
        
        // Initialize views
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        invitationsSection = view.findViewById(R.id.invitationsSection);
        rvInvitations = view.findViewById(R.id.rvInvitations);
        rvActivityFeed = view.findViewById(R.id.rvActivityFeed);
        tvEmptyActivity = view.findViewById(R.id.tvEmptyActivity);
        
        // Setup invitations RecyclerView with Accept/Decline handlers
        rvInvitations.setLayoutManager(new LinearLayoutManager(getContext()));
        invitationsAdapter = new InvitationsAdapter(new ArrayList<>(), new InvitationsAdapter.InvitationActionListener() {
            @Override
            public void onAccept(Invitation invitation) {
                handleAcceptInvitation(invitation);
            }

            @Override
            public void onDecline(Invitation invitation) {
                handleDeclineInvitation(invitation);
            }
        });
        rvInvitations.setAdapter(invitationsAdapter);
        
        // Setup activity feed RecyclerView
        rvActivityFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        activityAdapter = new ActivityLogAdapter(getContext());
        rvActivityFeed.setAdapter(activityAdapter);
        
        // Setup swipe refresh
        swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent
        );
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "🔄 Pull-to-refresh - reloading invitations and activity feed");
            loadAllData();
        });
        
        // Initialize repositories
        AuthManager authManager = new AuthManager(requireActivity().getApplication());
        activityLogApiService = ApiClient.get(authManager).create(ActivityLogApiService.class);
        invitationRepository = new InvitationRepositoryImpl(requireContext());

        // Get current user and load data
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            loadAllData();
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadAllData() {
        loadPendingInvitations();
        loadUserActivityFeed();
    }

    private void loadPendingInvitations() {
        if (currentUserId == null || getContext() == null) {
            return;
        }

        Log.d(TAG, "Loading pending invitations for user: " + currentUserId);

        invitationRepository.getMyInvitations(new IInvitationRepository.RepositoryCallback<List<Invitation>>() {
            @Override
            public void onSuccess(List<Invitation> invitations) {
                if (getContext() == null) return;
                
                Log.d(TAG, "Received " + invitations.size() + " pending invitations");
                
                if (invitations.isEmpty()) {
                    invitationsSection.setVisibility(View.GONE);
                } else {
                    invitationsSection.setVisibility(View.VISIBLE);
                    invitationsAdapter.updateInvitations(invitations);
                }
            }

            @Override
            public void onError(String message) {
                if (getContext() == null) return;
                
                Log.e(TAG, "Error loading invitations: " + message);
                invitationsSection.setVisibility(View.GONE);
            }
        });
    }
    
    private void handleAcceptInvitation(Invitation invitation) {
        Log.d(TAG, "Accepting invitation: " + invitation.getId());
        
        invitationRepository.acceptInvitation(
            invitation.getId(), 
            new IInvitationRepository.RepositoryCallback<String>() {
                @Override
                public void onSuccess(String message) {
                    if (getContext() == null) return;
                    
                    Toast.makeText(getContext(), "Invitation accepted!", Toast.LENGTH_SHORT).show();
                    // Reload both sections to show new activity log and remove invitation
                    loadAllData();
                }

                @Override
                public void onError(String error) {
                    if (getContext() == null) return;
                    
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    private void handleDeclineInvitation(Invitation invitation) {
        Log.d(TAG, "Declining invitation: " + invitation.getId());
        
        invitationRepository.declineInvitation(
            invitation.getId(), 
            new IInvitationRepository.RepositoryCallback<String>() {
                @Override
                public void onSuccess(String message) {
                    if (getContext() == null) return;
                    
                    Toast.makeText(getContext(), "Invitation declined", Toast.LENGTH_SHORT).show();
                    // Reload invitations to remove the declined one
                    loadPendingInvitations();
                }

                @Override
                public void onError(String error) {
                    if (getContext() == null) return;
                    
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        );
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
                            
                            activityAdapter.setActivityLogs(activityLogs);
                            
                            if (swipeRefreshLayout != null) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                            
                            if (activityLogs.isEmpty()) {
                                tvEmptyActivity.setVisibility(View.VISIBLE);
                                rvActivityFeed.setVisibility(View.GONE);
                            } else {
                                tvEmptyActivity.setVisibility(View.GONE);
                                rvActivityFeed.setVisibility(View.VISIBLE);
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
