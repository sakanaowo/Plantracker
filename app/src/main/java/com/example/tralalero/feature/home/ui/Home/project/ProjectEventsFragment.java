package com.example.tralalero.feature.home.ui.Home.project;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.CreateEventRequest;
import com.example.tralalero.domain.model.ProjectEvent;
import com.example.tralalero.auth.storage.TokenManager;

import java.text.SimpleDateFormat;
import java.util.Locale;
import com.example.tralalero.data.remote.api.ProjectApiService;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.App.App;
import com.example.tralalero.data.remote.dto.project.ProjectMemberDTO;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment for displaying and managing project events
 */
public class ProjectEventsFragment extends Fragment {
    private RecyclerView rvEvents;
    private ProjectEventAdapter eventAdapter;
    private View layoutEmptyState;
    private ProgressBar progressBar;
    private ImageButton btnCreateEvent;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    private ProjectEventsViewModel viewModel;
    private String projectId;
    private String currentUserId;
    private String currentUserRole; // OWNER, ADMIN, or MEMBER
    
    // For search functionality
    private List<ProjectEvent> allEvents = new ArrayList<>();
    private View layoutSearchEvents;
    private TextInputEditText etSearchEvents;
    private ImageButton btnSearchEvents;
    private boolean isSearchVisible = false;
    
    public static ProjectEventsFragment newInstance(String projectId) {
        ProjectEventsFragment fragment = new ProjectEventsFragment();
        Bundle args = new Bundle();
        args.putString("project_id", projectId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString("project_id");
        }
        
        // Get current user ID
        TokenManager tokenManager = new TokenManager(requireContext());
        currentUserId = tokenManager.getInternalUserId();
        
        // Fetch user's role in this project
        fetchCurrentUserRole();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_events, container, false);
        
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupButtons(view);
        loadEvents();
        
        return view;
    }
    
    private void initViews(View view) {
        rvEvents = view.findViewById(R.id.rvEvents);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        progressBar = view.findViewById(R.id.progressBar);
        btnCreateEvent = view.findViewById(R.id.btnCreateEvent);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        
        // Setup search views
        layoutSearchEvents = view.findViewById(R.id.layoutSearchEvents);
        etSearchEvents = view.findViewById(R.id.etSearchEvents);
        btnSearchEvents = view.findViewById(R.id.btnSearchEvents);
        
        // Setup search button click listener
        if (btnSearchEvents != null) {
            btnSearchEvents.setOnClickListener(v -> toggleSearch());
        }
        
        // Setup search field text watcher
        if (etSearchEvents != null) {
            etSearchEvents.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterEvents(s.toString());
                }
                
                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
            
            // Setup clear button to close search
            etSearchEvents.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    // Hide keyboard when search is pressed
                    android.view.inputmethod.InputMethodManager imm = 
                        (android.view.inputmethod.InputMethodManager) requireContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            });
        }
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProjectEventsViewModel.class);
        
        // Setup SwipeRefreshLayout
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
            );
            swipeRefreshLayout.setOnRefreshListener(() -> {
                android.util.Log.d("ProjectEvents", "🔄 Pull-to-refresh - reloading events");
                loadEvents();
            });
        }
        
        // Observe loading state
        viewModel.getLoadingState().observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            if (swipeRefreshLayout != null && !isLoading) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        
        // Observe create event success
        viewModel.getCreateEventSuccess().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                Toast.makeText(getContext(), 
                    "✅ Đã tạo sự kiện: " + event.getTitle(), 
                    Toast.LENGTH_SHORT).show();
                loadEvents();
                
                if (event.getMeetLink() != null) {
                    showMeetCreatedSnackbar(event);
                }
            }
        });
        
        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(getView(), 
                    "❌ Lỗi: " + error, 
                    Snackbar.LENGTH_LONG).show();
            }
        });
    }
    
    private void setupRecyclerView() {
        eventAdapter = new ProjectEventAdapter();
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEvents.setAdapter(eventAdapter);
        
        // Event click - show details
        eventAdapter.setOnEventClickListener(event -> {
            showEventDetails(event);
        });
        
        // Join meeting click
        eventAdapter.setOnJoinMeetingClickListener(event -> {
            if (event.getMeetLink() != null && !event.getMeetLink().isEmpty()) {
                joinGoogleMeet(event.getMeetLink());
            }
        });
        
        // Copy link click
        eventAdapter.setOnCopyLinkClickListener(event -> {
            if (event.getMeetLink() != null) {
                copyMeetLinkToClipboard(event.getMeetLink());
            }
        });
        
        // Menu click
        eventAdapter.setOnMenuClickListener((event, view) -> {
            showEventMenu(event, view);
        });
    }
    

    
    private void setupButtons(View view) {
        // Create Event button
        if (btnCreateEvent != null) {
            btnCreateEvent.setOnClickListener(v -> showCreateEventDialog());
        }
        
        // Empty state button
        View btnCreateFirstEvent = view.findViewById(R.id.btnCreateFirstEvent);
        if (btnCreateFirstEvent != null) {
            btnCreateFirstEvent.setOnClickListener(v -> showCreateEventDialog());
        }
    }
    
    private void loadEvents() {
        if (projectId == null) return;
        
        // Load all events without filter
        viewModel.loadProjectEvents(projectId, null)
            .observe(getViewLifecycleOwner(), result -> {
                if (result.isSuccess()) {
                    List<ProjectEvent> events = result.getData();
                    
                    if (events != null && !events.isEmpty()) {
                        // Save to allEvents for search filtering
                        allEvents = new ArrayList<>(events);
                        
                        // Sort and display events
                        sortAndDisplayEvents(events);
                    } else {
                        allEvents = new ArrayList<>();
                        rvEvents.setVisibility(View.GONE);
                        layoutEmptyState.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(), 
                        "Error: " + result.getErrorMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    /**
     * Sort and display events by priority:
     * 1. Upcoming events (not started yet, nearest first)
     * 2. Ongoing events (started but not ended)
     * 3. Cancelled events
     * 4. Overdue events (ended, oldest first)
     */
    private void sortAndDisplayEvents(List<ProjectEvent> events) {
        // Fix: Use ISO-8601 format with 'Z' (UTC) instead of XXX timezone
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date now = new Date();
        
        android.util.Log.d("ProjectEvents", "📊 Sorting " + events.size() + " events at " + now);
        
        // Debug: Log raw event data BEFORE sorting
        if (!events.isEmpty()) {
            android.util.Log.d("ProjectEvents", "🔍 First event raw data:");
            ProjectEvent first = events.get(0);
            android.util.Log.d("ProjectEvents", "  ID: " + first.getId());
            android.util.Log.d("ProjectEvents", "  Title: " + first.getTitle());
            android.util.Log.d("ProjectEvents", "  startAt: " + first.getStartAt());
            android.util.Log.d("ProjectEvents", "  endAt: " + first.getEndAt());
            android.util.Log.d("ProjectEvents", "  status: " + first.getStatus());
        }
        
        // Sort events
        Collections.sort(events, (e1, e2) -> {
            try {
                String status1 = e1.getStatus() != null ? e1.getStatus() : "ACTIVE";
                String status2 = e2.getStatus() != null ? e2.getStatus() : "ACTIVE";
                
                // Null check: If date is null, push event to bottom
                String endAt1 = e1.getEndAt();
                String endAt2 = e2.getEndAt();
                String startAt1 = e1.getStartAt();
                String startAt2 = e2.getStartAt();
                
                if (endAt1 == null || startAt1 == null) return 1;
                if (endAt2 == null || startAt2 == null) return -1;
                
                Date end1 = sdf.parse(endAt1);
                Date end2 = sdf.parse(endAt2);
                Date start1 = sdf.parse(startAt1);
                Date start2 = sdf.parse(startAt2);
                
                // Categorize events
                boolean isCancelled1 = "CANCELLED".equals(status1);
                boolean isCancelled2 = "CANCELLED".equals(status2);
                
                // Overdue: event ended (end < now) and not cancelled
                boolean isOverdue1 = end1 != null && end1.before(now) && !isCancelled1;
                boolean isOverdue2 = end2 != null && end2.before(now) && !isCancelled2;
                
                // Upcoming: event not started yet (start > now) and not cancelled
                boolean isUpcoming1 = start1 != null && start1.after(now) && !isCancelled1;
                boolean isUpcoming2 = start2 != null && start2.after(now) && !isCancelled2;
                
                // Ongoing: started but not ended (start <= now < end) and not cancelled
                boolean isOngoing1 = !isUpcoming1 && !isOverdue1 && !isCancelled1;
                boolean isOngoing2 = !isUpcoming2 && !isOverdue2 && !isCancelled2;
                
                // Priority order: Upcoming > Ongoing > Cancelled > Overdue
                
                // 1. Upcoming vs others
                if (isUpcoming1 && !isUpcoming2) return -1;
                if (!isUpcoming1 && isUpcoming2) return 1;
                if (isUpcoming1 && isUpcoming2) {
                    return start1.compareTo(start2); // Nearest first (ASC)
                }
                
                // 2. Ongoing vs others (not upcoming)
                if (isOngoing1 && !isOngoing2) return -1;
                if (!isOngoing1 && isOngoing2) return 1;
                if (isOngoing1 && isOngoing2) {
                    return start1.compareTo(start2); // Started first (ASC)
                }
                
                // 3. Cancelled vs Overdue
                if (isCancelled1 && !isCancelled2 && !isUpcoming2 && !isOngoing2) return -1;
                if (isOverdue1 && isCancelled2) return 1;
                if (isCancelled1 && isCancelled2) {
                    return start1.compareTo(start2); // Same as overdue - oldest first (ASC)
                }
                
                // 4. Both overdue - show oldest first (events that ended long time ago at bottom)
                if (isOverdue1 && isOverdue2) {
                    return start1.compareTo(start2); // Oldest first (ASC) - Dec 2, 3, 4...
                }
                
                return 0;
            } catch (Exception e) {
                android.util.Log.e("ProjectEvents", "Error sorting events", e);
                return 0;
            }
        });
        
        // Log sorted order with detailed timestamps
        try {
            android.util.Log.d("ProjectEvents", "✅ AFTER SORTING - Top 5 events:");
            for (int i = 0; i < Math.min(events.size(), 5); i++) {
                ProjectEvent e = events.get(i);
                String category = "CANCELLED".equals(e.getStatus()) ? "CANCELLED" : 
                                 (e.getEndAt() != null && sdf.parse(e.getEndAt()).before(now)) ? "OVERDUE" :
                                 (e.getStartAt() != null && sdf.parse(e.getStartAt()).after(now)) ? "UPCOMING" : "ONGOING";
                android.util.Log.d("ProjectEvents", String.format("#%d [%s] %s", i+1, category, e.getTitle()));
                android.util.Log.d("ProjectEvents", String.format("    startAt: %s", e.getStartAt()));
                android.util.Log.d("ProjectEvents", String.format("    endAt: %s", e.getEndAt()));
            }
        } catch (Exception ex) {
            android.util.Log.e("ProjectEvents", "Error logging", ex);
        }
        
        eventAdapter.setEvents(events);
        
        // Update visibility
        if (events.isEmpty()) {
            rvEvents.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvEvents.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }
    
    private void showCreateEventDialog() {
        CreateEventDialog dialog = CreateEventDialog.newInstance(projectId);
        dialog.setOnEventCreatedListener(event -> {
            // Convert ProjectEvent to CreateEventRequest
            CreateEventRequest request = new CreateEventRequest();
            request.setTitle(event.getTitle());
            request.setDescription(event.getDescription());
            
            // ✅ FIX: Convert date from Date object to yyyy-MM-dd string
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String dateStr = dateFormat.format(event.getDate());
            
            request.setDate(dateStr);  // "2025-12-07"
            request.setTime(event.getTime());  // "19:18"
            request.setDuration(event.getDuration());
            request.setType(event.getType());
            request.setLocation(event.getLocation());
            request.setMeetingLink(event.getMeetingLink());
            request.setAttendeeIds(event.getAttendeeIds());
            request.setCreateGoogleMeet(event.isCreateGoogleMeet());
            request.setRecurrence(event.getRecurrence());
            request.setProjectId(projectId);
            
            android.util.Log.d("ProjectEvents", "📤 Sending to backend:");
            android.util.Log.d("ProjectEvents", "  date: " + dateStr);
            android.util.Log.d("ProjectEvents", "  time: " + event.getTime());
            android.util.Log.d("ProjectEvents", "  duration: " + event.getDuration());
            
            // Call API via ViewModel
            viewModel.createEvent(request);
            
            Toast.makeText(getContext(), 
                "⏳ Creating event...", 
                Toast.LENGTH_SHORT).show();
        });
        dialog.show(getChildFragmentManager(), "create_event");
    }
    
    private void showMeetCreatedSnackbar(ProjectEvent event) {
        if (getView() == null) return;
        
        Snackbar.make(getView(),
            "📹 Google Meet created",
            Snackbar.LENGTH_LONG)
            .setAction("Copy link", v -> {
                copyMeetLinkToClipboard(event.getMeetLink());
            })
            .setActionTextColor(Color.parseColor("#4CAF50"))
            .show();
    }
    
    private void joinGoogleMeet(String meetLink) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(meetLink));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), 
                "Cannot open meeting link", 
                Toast.LENGTH_SHORT).show();
        }
    }
    
    private void copyMeetLinkToClipboard(String meetLink) {
        ClipboardManager clipboard = (ClipboardManager) 
            getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Google Meet Link", meetLink);
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(getContext(), 
            "✅ Link copied", 
            Toast.LENGTH_SHORT).show();
    }
    
    private void showEventDetails(ProjectEvent event) {
        EventDetailDialog dialog = EventDetailDialog.newInstance(event);
        dialog.show(getChildFragmentManager(), "event_detail");
    }
    
    private void showEventMenu(ProjectEvent event, View anchorView) {
        PopupMenu popup = new PopupMenu(getContext(), anchorView);
        popup.inflate(R.menu.menu_event_actions);
        
        // ✅ Check permission: Only event creator or OWNER/ADMIN can edit/delete
        boolean canModify = canUserModifyEvent(event);
        
        // ✅ Check if event is overdue or cancelled
        boolean isOverdueOrCancelled = isEventOverdueOrCancelled(event);
        
        // Hide/disable menu items based on permission and status
        if (!canModify) {
            popup.getMenu().findItem(R.id.action_edit).setVisible(false);
            popup.getMenu().findItem(R.id.action_cancel).setVisible(false);
            popup.getMenu().findItem(R.id.action_delete_permanent).setVisible(false);
        } else if (isOverdueOrCancelled) {
            // Overdue/Cancelled: Only show delete permanently
            popup.getMenu().findItem(R.id.action_edit).setVisible(false);
            popup.getMenu().findItem(R.id.action_cancel).setVisible(false);
            popup.getMenu().findItem(R.id.action_send_reminder).setVisible(false);
            popup.getMenu().findItem(R.id.action_delete_permanent).setVisible(true);
        } else {
            // Active events: Show edit, cancel, send reminder
            popup.getMenu().findItem(R.id.action_edit).setVisible(true);
            popup.getMenu().findItem(R.id.action_cancel).setVisible(true);
            popup.getMenu().findItem(R.id.action_send_reminder).setVisible(true);
            popup.getMenu().findItem(R.id.action_delete_permanent).setVisible(false);
        }
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                editEvent(event);
                return true;
            } else if (itemId == R.id.action_cancel) {
                cancelEvent(event);
                return true;
            } else if (itemId == R.id.action_delete_permanent) {
                hardDeleteEvent(event);
                return true;
            } else if (itemId == R.id.action_send_reminder) {
                sendReminder(event);
                return true;
            }
            return false;
        });
        
        popup.show();
    }
    
    /**
     * Check if current user can modify this event
     * Rules: Event creator OR project OWNER/ADMIN can modify
     */
    private boolean canUserModifyEvent(ProjectEvent event) {
        if (currentUserId == null) return false;
        
        // Event creator can always modify
        if (currentUserId.equals(event.getCreatedBy())) {
            return true;
        }
        
        // OWNER or ADMIN can modify any event
        if ("OWNER".equals(currentUserRole) || "ADMIN".equals(currentUserRole)) {
            return true;
        }
        
        // MEMBER can only modify their own events
        return false;
    }
    
    /**
     * Check if event is overdue or cancelled
     */
    private boolean isEventOverdueOrCancelled(ProjectEvent event) {
        // Check if cancelled
        if ("CANCELLED".equals(event.getStatus())) {
            return true;
        }
        
        // Check if overdue (end time has passed)
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
            Date endTime = sdf.parse(event.getEndAt());
            if (endTime != null && endTime.before(new Date())) {
                return true;
            }
        } catch (Exception e) {
            android.util.Log.e("ProjectEvents", "Error parsing date", e);
        }
        
        return false;
    }
    
    /**
     * Fetch current user's role in this project
     */
    private void fetchCurrentUserRole() {
        if (projectId == null || currentUserId == null) return;
        
        ProjectApiService projectApi = ApiClient.get(App.authManager).create(ProjectApiService.class);
        projectApi.getProjectMembers(projectId).enqueue(new Callback<java.util.List<ProjectMemberDTO>>() {
            @Override
            public void onResponse(@NonNull Call<java.util.List<ProjectMemberDTO>> call, 
                                 @NonNull Response<java.util.List<ProjectMemberDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (ProjectMemberDTO member : response.body()) {
                        if (currentUserId.equals(member.getUserId())) {
                            currentUserRole = member.getRole();
                            android.util.Log.d("ProjectEvents", "✅ User role: " + currentUserRole);
                            break;
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<java.util.List<ProjectMemberDTO>> call, @NonNull Throwable t) {
                android.util.Log.e("ProjectEvents", "❌ Failed to fetch user role", t);
            }
        });
    }
    
    private void editEvent(ProjectEvent event) {
        EditEventDialog dialog = EditEventDialog.newInstance(event);
        dialog.setOnEventUpdatedListener(request -> {
            viewModel.updateEvent(event.getId(), request).observe(getViewLifecycleOwner(), result -> {
                if (result.isSuccess()) {
                    Toast.makeText(getContext(), "✅ Event updated", Toast.LENGTH_SHORT).show();
                    loadEvents();
                } else {
                    Toast.makeText(getContext(), 
                        "Error: " + result.getErrorMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show(getChildFragmentManager(), "edit_event");
    }
    
    private void cancelEvent(ProjectEvent event) {
        new AlertDialog.Builder(getContext())
            .setTitle("Cancel Event")
            .setMessage("Are you sure you want to cancel this event?")
            .setPositiveButton("Cancel Event", (dialog, which) -> {
                viewModel.deleteEvent(event.getId()).observe(getViewLifecycleOwner(), result -> {
                    if (result.isSuccess()) {
                        Toast.makeText(getContext(), "✅ Event cancelled", Toast.LENGTH_SHORT).show();
                        loadEvents();
                    } else {
                        Toast.makeText(getContext(), 
                            "Error: " + result.getErrorMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    private void hardDeleteEvent(ProjectEvent event) {
        new AlertDialog.Builder(getContext())
            .setTitle("Delete Permanently")
            .setMessage("⚠️ Are you sure you want to PERMANENTLY DELETE this event?\n\nThis action CANNOT BE UNDONE!")
            .setPositiveButton("Delete Permanently", (dialog, which) -> {
                viewModel.hardDeleteEvent(event.getId()).observe(getViewLifecycleOwner(), result -> {
                    if (result.isSuccess()) {
                        Toast.makeText(getContext(), "✅ Event permanently deleted", Toast.LENGTH_SHORT).show();
                        loadEvents();
                    } else {
                        Toast.makeText(getContext(), 
                            "Error: " + result.getErrorMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void sendReminder(ProjectEvent event) {
        viewModel.sendReminder(event.getId()).observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                Toast.makeText(getContext(), 
                    "✅ Reminder sent to participants", 
                    Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), 
                    "Error: " + result.getErrorMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Toggle search bar visibility
     */
    private void toggleSearch() {
        if (isSearchVisible) {
            // Hide search bar
            hideSearch();
        } else {
            // Show search bar
            showSearch();
        }
    }
    
    /**
     * Show search bar and focus on input
     */
    private void showSearch() {
        isSearchVisible = true;
        if (layoutSearchEvents != null) {
            layoutSearchEvents.setVisibility(View.VISIBLE);
        }
        if (etSearchEvents != null) {
            etSearchEvents.requestFocus();
            // Show keyboard
            android.view.inputmethod.InputMethodManager imm = 
                (android.view.inputmethod.InputMethodManager) requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etSearchEvents, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }
    }
    
    /**
     * Hide search bar and clear search query
     */
    private void hideSearch() {
        isSearchVisible = false;
        if (layoutSearchEvents != null) {
            layoutSearchEvents.setVisibility(View.GONE);
        }
        if (etSearchEvents != null) {
            etSearchEvents.setText("");
            etSearchEvents.clearFocus();
        }
        // Hide keyboard
        android.view.inputmethod.InputMethodManager imm = 
            (android.view.inputmethod.InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getView() != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
        // Clear filter and show all events
        filterEvents("");
    }
    
    /**
     * Filter events by title search query
     */
    private void filterEvents(String query) {
        if (query == null || query.trim().isEmpty()) {
            // Show all events with sorting
            sortAndDisplayEvents(allEvents);
            return;
        }
        
        // Filter by title (case-insensitive)
        String lowerQuery = query.toLowerCase().trim();
        List<ProjectEvent> filtered = new ArrayList<>();
        
        for (ProjectEvent event : allEvents) {
            if (event.getTitle() != null && 
                event.getTitle().toLowerCase().contains(lowerQuery)) {
                filtered.add(event);
            }
        }
        
        // Sort and display filtered events
        sortAndDisplayEvents(filtered);
    }
}
