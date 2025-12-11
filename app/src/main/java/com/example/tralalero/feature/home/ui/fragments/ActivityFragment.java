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

import com.example.tralalero.BuildConfig;
import com.example.tralalero.R;
import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.data.mapper.ActivityLogMapper;
import com.example.tralalero.data.network.WebSocketManager;
import com.example.tralalero.data.remote.api.ActivityLogApiService;
import com.example.tralalero.data.remote.api.EventApiService;
import com.example.tralalero.data.remote.dto.activity.ActivityLogDTO;
import com.example.tralalero.data.repository.InvitationRepositoryImpl;
import com.example.tralalero.domain.model.ActivityLog;
import com.example.tralalero.domain.model.Invitation;
import com.example.tralalero.domain.model.EventReminder;
import com.example.tralalero.domain.repository.IInvitationRepository;
import com.example.tralalero.feature.home.ui.adapter.ActivityLogAdapter;
import com.example.tralalero.feature.home.ui.adapter.EventReminderAdapter;
import com.example.tralalero.feature.invitations.InvitationsAdapter;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.utils.ActivityBadgeManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityFragment extends Fragment implements WebSocketManager.ActivityLogListener {
    private static final String TAG = "ActivityFragment";
    
    // Invitations section
    private LinearLayout invitationsSection;
    private RecyclerView rvInvitations;
    private InvitationsAdapter invitationsAdapter;
    private IInvitationRepository invitationRepository;
    
    // Event reminders section
    private LinearLayout eventRemindersSection;
    private RecyclerView rvEventReminders;
    private EventReminderAdapter eventReminderAdapter;
    
    // Activity feed section
    private RecyclerView rvActivityFeed;
    private TextView tvEmptyActivity;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ActivityLogAdapter activityAdapter;
    private ActivityLogApiService activityLogApiService;
    private EventApiService eventApiService;
    private String currentUserId;
    private WebSocketManager webSocketManager;
    private ActivityBadgeManager badgeManager;

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
        eventRemindersSection = view.findViewById(R.id.eventRemindersSection);
        rvEventReminders = view.findViewById(R.id.rvEventReminders);
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
        
        // Setup event reminders RecyclerView
        rvEventReminders.setLayoutManager(new LinearLayoutManager(getContext()));
        eventReminderAdapter = new EventReminderAdapter(new EventReminderAdapter.EventReminderActionListener() {
            @Override
            public void onViewEvent(EventReminder reminder) {
                handleViewEvent(reminder);
            }

            @Override
            public void onDismiss(EventReminder reminder) {
                handleDismissReminder(reminder);
            }
        });
        rvEventReminders.setAdapter(eventReminderAdapter);
        
        // Setup activity feed RecyclerView
        rvActivityFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        activityAdapter = new ActivityLogAdapter(getContext());
        
        // Set click listeners for navigation
        activityAdapter.setOnActivityClickListener(new ActivityLogAdapter.OnActivityClickListener() {
            @Override
            public void onTaskClick(String taskId, String projectId) {
                Log.d(TAG, "Navigating to task: " + taskId + " in project: " + projectId);
                navigateToTask(taskId, projectId);
            }

            @Override
            public void onEventClick(String eventId, String projectId) {
                Log.d(TAG, "Navigating to event: " + eventId + " in project: " + projectId);
                navigateToEvent(eventId, projectId);
            }

            @Override
            public void onProjectClick(String projectId) {
                Log.d(TAG, "Navigating to project: " + projectId);
                navigateToProject(projectId);
            }
        });
        
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
        eventApiService = ApiClient.get(authManager).create(EventApiService.class);
        invitationRepository = new InvitationRepositoryImpl(requireContext());
        badgeManager = new ActivityBadgeManager(requireContext());

        // Get current user and load data
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            
            // Initialize WebSocket for real-time updates
            webSocketManager = WebSocketManager.getInstance();
            webSocketManager.addActivityLogListener(this);
            
            // Connect WebSocket if not already connected
            if (!webSocketManager.isConnected()) {
                firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String idToken = task.getResult().getToken();
                        String wsUrl = BuildConfig.WS_URL.replace("/api/", "");
                        Log.d(TAG, "Connecting WebSocket to: " + wsUrl);
                        webSocketManager.connect(wsUrl, idToken);
                    } else {
                        Log.e(TAG, "Failed to get Firebase token for WebSocket");
                    }
                });
            }
            
            loadAllData();
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Clear badge when user views activity feed
        if (badgeManager != null) {
            badgeManager.markAllAsRead();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webSocketManager != null) {
            webSocketManager.removeActivityLogListener(this);
        }
        if (badgeManager != null) {
            badgeManager.release();
        }
    }
    
    @Override
    public void onActivityLogReceived(JSONObject log) {
        // Handle new activity log from WebSocket on main thread
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                try {
                    // Convert JSON to ActivityLog directly
                    ActivityLog newLog = new ActivityLog();
                    newLog.setId(log.optString("id"));
                    newLog.setUserId(log.optString("user_id"));
                    newLog.setAction(log.optString("action"));
                    newLog.setEntityType(log.optString("entity_type"));
                    newLog.setEntityId(log.optString("entity_id"));
                    newLog.setEntityName(log.optString("entity_name"));
                    newLog.setProjectId(log.optString("project_id"));
                    newLog.setTimestamp(log.optString("created_at"));
                    
                    // Parse user info
                    if (log.has("users") && !log.isNull("users")) {
                        JSONObject users = log.getJSONObject("users");
                        newLog.setUserName(users.optString("name"));
                        newLog.setUserAvatar(users.optString("avatarUrl"));
                    }
                    
                    // Parse project info
                    if (log.has("projects") && !log.isNull("projects")) {
                        JSONObject projects = log.getJSONObject("projects");
                        newLog.setProjectName(projects.optString("name"));
                    }
                    
                    // Parse metadata
                    if (log.has("metadata") && !log.isNull("metadata")) {
                        JSONObject metadata = log.getJSONObject("metadata");
                        newLog.setMetadata(metadata.toString());
                    }
                    
                    // Add to top of list
                    activityAdapter.addActivityLog(newLog);
                    
                    // Show UI if was empty
                    if (tvEmptyActivity.getVisibility() == View.VISIBLE) {
                        tvEmptyActivity.setVisibility(View.GONE);
                        rvActivityFeed.setVisibility(View.VISIBLE);
                    }
                    
                    // Scroll to top
                    rvActivityFeed.smoothScrollToPosition(0);
                    
                    // Play notification sound and show badge if fragment not visible
                    if (!isResumed() && badgeManager != null) {
                        badgeManager.playNotificationSound();
                        badgeManager.incrementUnreadCount();
                        Log.d(TAG, "🔔 Notification sound + badge triggered");
                    }
                    
                    Log.d(TAG, "✅ Real-time activity log added: " + newLog.getEntityName());
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing real-time activity log", e);
                }
            });
        }
    }
    
    private void loadAllData() {
        loadPendingInvitations();
        loadEventReminders();
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
    
    private void loadEventReminders() {
        if (currentUserId == null || getContext() == null) {
            return;
        }

        Log.d(TAG, "Loading event reminders for user: " + currentUserId);

        eventApiService.getMyEventReminders().enqueue(new Callback<List<EventApiService.EventReminderDTO>>() {
            @Override
            public void onResponse(Call<List<EventApiService.EventReminderDTO>> call, Response<List<EventApiService.EventReminderDTO>> response) {
                if (getContext() == null) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    List<EventApiService.EventReminderDTO> dtos = response.body();
                    List<EventReminder> reminders = new ArrayList<>();
                    
                    for (EventApiService.EventReminderDTO dto : dtos) {
                        EventReminder reminder = new EventReminder();
                        reminder.setId(dto.getId());
                        reminder.setEventId(dto.getEventId());
                        reminder.setEventTitle(dto.getEventTitle());
                        reminder.setEventDate(dto.getEventDate());
                        reminder.setEventTime(dto.getEventTime());
                        reminder.setProjectId(dto.getProjectId());
                        reminder.setProjectName(dto.getProjectName());
                        reminder.setSenderName(dto.getSenderName());
                        reminder.setMessage(dto.getMessage());
                        reminder.setTimestamp(dto.getTimestamp());
                        reminder.setRead(dto.isRead());
                        reminders.add(reminder);
                    }
                    
                    Log.d(TAG, "Received " + reminders.size() + " event reminders");
                    
                    if (reminders.isEmpty()) {
                        eventRemindersSection.setVisibility(View.GONE);
                    } else {
                        eventRemindersSection.setVisibility(View.VISIBLE);
                        eventReminderAdapter.updateReminders(reminders);
                    }
                } else {
                    Log.e(TAG, "Failed to load event reminders: " + response.code());
                    eventRemindersSection.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<EventApiService.EventReminderDTO>> call, Throwable t) {
                if (getContext() == null) return;
                Log.e(TAG, "Error loading event reminders: " + t.getMessage());
                eventRemindersSection.setVisibility(View.GONE);
            }
        });
    }
    
    private void handleViewEvent(EventReminder reminder) {
        Log.d(TAG, "Viewing event: " + reminder.getEventId());
        
        // Mark as read
        eventApiService.markReminderAsRead(reminder.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Reminder marked as read");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Failed to mark reminder as read: " + t.getMessage());
            }
        });
        
        // TODO: Navigate to event details
        // For now, show a toast
        if (getContext() != null) {
            Toast.makeText(getContext(), "Opening event: " + reminder.getEventTitle(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void handleDismissReminder(EventReminder reminder) {
        Log.d(TAG, "Dismissing reminder: " + reminder.getId());
        
        eventApiService.dismissEventReminder(reminder.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (getContext() == null) return;
                
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Reminder dismissed", Toast.LENGTH_SHORT).show();
                    loadEventReminders();
                } else {
                    Toast.makeText(getContext(), "Failed to dismiss reminder", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (getContext() == null) return;
                Log.e(TAG, "Error dismissing reminder: " + t.getMessage());
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
    
    /**
     * Navigate directly to task detail
     */
    private void navigateToTask(String taskId, String projectId) {
        if (getContext() == null) return;
        
        // Open CardDetailActivity directly
        Intent intent = new Intent(getContext(), com.example.tralalero.feature.home.ui.Home.project.CardDetailActivity.class);
        intent.putExtra(com.example.tralalero.feature.home.ui.Home.project.CardDetailActivity.EXTRA_TASK_ID, taskId);
        intent.putExtra(com.example.tralalero.feature.home.ui.Home.project.CardDetailActivity.EXTRA_PROJECT_ID, projectId);
        intent.putExtra(com.example.tralalero.feature.home.ui.Home.project.CardDetailActivity.EXTRA_IS_EDIT_MODE, true);
        startActivity(intent);
        
        Log.d(TAG, "Navigated to task detail: " + taskId + " in project: " + projectId);
    }
    
    /**
     * Navigate to event in ProjectActivity Event tab
     */
    private void navigateToEvent(String eventId, String projectId) {
        if (getContext() == null) return;
        
        // Open ProjectActivity with Event tab (index 4)
        Intent intent = new Intent(getContext(), com.example.tralalero.feature.home.ui.Home.ProjectActivity.class);
        intent.putExtra("PROJECT_ID", projectId);
        intent.putExtra("TAB_INDEX", 4); // Tab 4 = Event view
        startActivity(intent);
        
        Log.d(TAG, "Navigated to Event tab for event: " + eventId + " in project: " + projectId);
    }
    
    /**
     * Navigate to project detail in ProjectActivity
     */
    private void navigateToProject(String projectId) {
        if (getContext() == null) return;
        
        Intent intent = new Intent(getContext(), com.example.tralalero.feature.home.ui.Home.ProjectActivity.class);
        intent.putExtra("PROJECT_ID", projectId);
        startActivity(intent);
        
        Log.d(TAG, "Navigated to project: " + projectId);
    }
}
