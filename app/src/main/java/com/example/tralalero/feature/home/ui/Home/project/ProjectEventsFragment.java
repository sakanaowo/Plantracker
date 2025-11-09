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

import com.example.tralalero.R;
import com.example.tralalero.domain.model.CreateEventRequest;
import com.example.tralalero.domain.model.ProjectEvent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Fragment for displaying and managing project events
 */
public class ProjectEventsFragment extends Fragment {
    private RecyclerView rvEvents;
    private ProjectEventAdapter eventAdapter;
    private TabLayout tabLayoutEvents;
    private View layoutEmptyState;
    private ProgressBar progressBar;
    private FloatingActionButton fabCreateEvent;
    
    private ProjectEventsViewModel viewModel;
    private String projectId;
    private ProjectEventsViewModel.EventFilter currentFilter = ProjectEventsViewModel.EventFilter.UPCOMING;
    
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
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_events, container, false);
        
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupTabs();
        setupButtons(view);
        loadEvents();
        
        return view;
    }
    
    private void initViews(View view) {
        rvEvents = view.findViewById(R.id.rvEvents);
        tabLayoutEvents = view.findViewById(R.id.tabLayoutEvents);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        progressBar = view.findViewById(R.id.progressBar);
        fabCreateEvent = view.findViewById(R.id.fabCreateEvent);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProjectEventsViewModel.class);
        
        // Observe loading state
        viewModel.getLoadingState().observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
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
    
    private void setupTabs() {
        tabLayoutEvents.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        currentFilter = ProjectEventsViewModel.EventFilter.UPCOMING;
                        break;
                    case 1:
                        currentFilter = ProjectEventsViewModel.EventFilter.PAST;
                        break;
                    case 2:
                        currentFilter = ProjectEventsViewModel.EventFilter.RECURRING;
                        break;
                }
                loadEvents();
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void setupButtons(View view) {
        // FAB
        if (fabCreateEvent != null) {
            fabCreateEvent.setOnClickListener(v -> showCreateEventDialog());
        }
        
        // Empty state button
        View btnCreateFirstEvent = view.findViewById(R.id.btnCreateFirstEvent);
        if (btnCreateFirstEvent != null) {
            btnCreateFirstEvent.setOnClickListener(v -> showCreateEventDialog());
        }
        
        // Filter button
        View btnFilterEvents = view.findViewById(R.id.btnFilterEvents);
        if (btnFilterEvents != null) {
            btnFilterEvents.setOnClickListener(v -> showFilterDialog());
        }
        
        // Search button
        View btnSearchEvents = view.findViewById(R.id.btnSearchEvents);
        if (btnSearchEvents != null) {
            btnSearchEvents.setOnClickListener(v -> showSearchDialog());
        }
    }
    
    private void loadEvents() {
        if (projectId == null) return;
        
        viewModel.loadProjectEvents(projectId, currentFilter)
            .observe(getViewLifecycleOwner(), result -> {
                if (result.isSuccess()) {
                    java.util.List<ProjectEvent> events = result.getData();
                    
                    if (events != null && !events.isEmpty()) {
                        eventAdapter.setEvents(events);
                        rvEvents.setVisibility(View.VISIBLE);
                        layoutEmptyState.setVisibility(View.GONE);
                    } else {
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
            
            // Format date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            request.setDate(dateFormat.format(event.getDate()));
            request.setTime(event.getTime());
            
            request.setDuration(event.getDuration());
            request.setType(event.getType());
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
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                editEvent(event);
                return true;
            } else if (itemId == R.id.action_cancel) {
                cancelEvent(event);
                return true;
            } else if (itemId == R.id.action_send_reminder) {
                sendReminder(event);
                return true;
            }
            return false;
        });
        
        popup.show();
    }
    
    private void editEvent(ProjectEvent event) {
        // TODO: Mở edit dialog
        Toast.makeText(getContext(), "TODO: Edit event", Toast.LENGTH_SHORT).show();
    }
    
    private void cancelEvent(ProjectEvent event) {
        new AlertDialog.Builder(getContext())
            .setTitle("Hủy sự kiện")
            .setMessage("Bạn có chắc muốn hủy sự kiện này?")
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
    
    private void showFilterDialog() {
        // TODO: Implement filter dialog
        Toast.makeText(getContext(), "TODO: Filter events", Toast.LENGTH_SHORT).show();
    }
    
    private void showSearchDialog() {
        // TODO: Implement search
        Toast.makeText(getContext(), "TODO: Search events", Toast.LENGTH_SHORT).show();
    }
}
