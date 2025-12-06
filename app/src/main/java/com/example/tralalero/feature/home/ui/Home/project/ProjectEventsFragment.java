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
import com.example.tralalero.data.remote.api.ProjectApiService;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.App.App;
import com.example.tralalero.data.remote.dto.project.ProjectMemberDTO;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
                        // Sort events by startAt datetime - nearest events first (sắp đến -> xa nhất)
                        java.util.Collections.sort(events, (e1, e2) -> {
                            // Use startAt for more precise sorting (includes time)
                            String startAt1 = e1.getStartAt();
                            String startAt2 = e2.getStartAt();
                            
                            // Handle null startAt - fallback to date field
                            if (startAt1 == null && startAt2 == null) {
                                if (e1.getDate() == null && e2.getDate() == null) return 0;
                                if (e1.getDate() == null) return 1;
                                if (e2.getDate() == null) return -1;
                                return e1.getDate().compareTo(e2.getDate());
                            }
                            if (startAt1 == null) return 1;
                            if (startAt2 == null) return -1;
                            
                            // Compare by startAt timestamp (ISO 8601 format sorts correctly)
                            return startAt1.compareTo(startAt2);
                        });
                        
                        // Save to allEvents for search filtering
                        allEvents = new ArrayList<>(events);
                        
                        eventAdapter.setEvents(events);
                        rvEvents.setVisibility(View.VISIBLE);
                        layoutEmptyState.setVisibility(View.GONE);
                    } else {
                        allEvents = new ArrayList<>();
                        rvEvents.setVisibility(View.GONE);
                        layoutEmptyState.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(), 
                        "Lỗi: " + result.getErrorMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void showCreateEventDialog() {
        CreateEventDialog dialog = CreateEventDialog.newInstance(projectId);
        dialog.setOnEventCreatedListener(event -> {
            // Convert ProjectEvent to CreateEventRequest
            CreateEventRequest request = new CreateEventRequest();
            request.setTitle(event.getTitle());
            request.setDescription(event.getDescription());
            
            // ✅ FIX: Use ISO 8601 formatted startAt/endAt instead of separate date/time
            request.setStartAt(event.getStartAt());
            request.setEndAt(event.getEndAt());
            
            request.setDuration(event.getDuration());
            request.setType(event.getType());
            request.setLocation(event.getLocation());  // ✅ Add location
            request.setMeetingLink(event.getMeetingLink());  // ✅ Add meeting link
            request.setAttendeeIds(event.getAttendeeIds());
            request.setCreateGoogleMeet(event.isCreateGoogleMeet());
            request.setRecurrence(event.getRecurrence());
            request.setProjectId(projectId);
            
            // Call API via ViewModel
            viewModel.createEvent(request);
            
            Toast.makeText(getContext(), 
                "⏳ Đang tạo sự kiện...", 
                Toast.LENGTH_SHORT).show();
        });
        dialog.show(getChildFragmentManager(), "create_event");
    }
    
    private void showMeetCreatedSnackbar(ProjectEvent event) {
        if (getView() == null) return;
        
        Snackbar.make(getView(),
            "📹 Google Meet đã được tạo",
            Snackbar.LENGTH_LONG)
            .setAction("Sao chép link", v -> {
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
                "Không thể mở link họp", 
                Toast.LENGTH_SHORT).show();
        }
    }
    
    private void copyMeetLinkToClipboard(String meetLink) {
        ClipboardManager clipboard = (ClipboardManager) 
            getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Google Meet Link", meetLink);
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(getContext(), 
            "✅ Đã sao chép link", 
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
        
        // Hide/disable menu items based on permission
        if (!canModify) {
            popup.getMenu().findItem(R.id.action_edit).setVisible(false);
            popup.getMenu().findItem(R.id.action_cancel).setVisible(false);
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
                    Toast.makeText(getContext(), "✅ Đã cập nhật sự kiện", Toast.LENGTH_SHORT).show();
                    loadEvents();
                } else {
                    Toast.makeText(getContext(), 
                        "Lỗi: " + result.getErrorMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show(getChildFragmentManager(), "edit_event");
    }
    
    private void cancelEvent(ProjectEvent event) {
        new AlertDialog.Builder(getContext())
            .setTitle("Hủy sự kiện")
            .setMessage("Bạn có chắc muốn hủy sự kiện này? (Soft delete)")
            .setPositiveButton("Hủy sự kiện", (dialog, which) -> {
                viewModel.deleteEvent(event.getId()).observe(getViewLifecycleOwner(), result -> {
                    if (result.isSuccess()) {
                        Toast.makeText(getContext(), "✅ Đã hủy sự kiện", Toast.LENGTH_SHORT).show();
                        loadEvents();
                    } else {
                        Toast.makeText(getContext(), 
                            "Lỗi: " + result.getErrorMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Không", null)
            .show();
    }
    
    private void hardDeleteEvent(ProjectEvent event) {
        new AlertDialog.Builder(getContext())
            .setTitle("Xóa vĩnh viễn")
            .setMessage("⚠️ Bạn có chắc muốn XÓA VĨNH VIỄN sự kiện này?\n\nHành động này KHÔNG THỂ HOÀN TÁC!")
            .setPositiveButton("Xóa vĩnh viễn", (dialog, which) -> {
                viewModel.hardDeleteEvent(event.getId()).observe(getViewLifecycleOwner(), result -> {
                    if (result.isSuccess()) {
                        Toast.makeText(getContext(), "✅ Đã xóa vĩnh viễn sự kiện", Toast.LENGTH_SHORT).show();
                        loadEvents();
                    } else {
                        Toast.makeText(getContext(), 
                            "Lỗi: " + result.getErrorMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    private void sendReminder(ProjectEvent event) {
        viewModel.sendReminder(event.getId()).observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                Toast.makeText(getContext(), 
                    "✅ Đã gửi nhắc nhở tới người tham gia", 
                    Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), 
                    "Lỗi: " + result.getErrorMessage(), 
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
            // Show all events
            eventAdapter.setEvents(allEvents);
            if (allEvents.isEmpty()) {
                rvEvents.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                rvEvents.setVisibility(View.VISIBLE);
                layoutEmptyState.setVisibility(View.GONE);
            }
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
        
        // Update adapter
        eventAdapter.setEvents(filtered);
        
        // Update visibility
        if (filtered.isEmpty()) {
            rvEvents.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvEvents.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }
}
