package com.example.tralalero.feature.home.ui.Home.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.CalendarEvent;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ProjectCalendarFragment extends Fragment {

    private String projectId;
    private Calendar selectedCalendar;
    
    private TextView tvCurrentMonth;
    private RecyclerView rvCalendarEvents;
    private ProgressBar progressBar;
    private TextView tvSelectedDate;
    private TextView tvEventCount;
    private TextView tvEventDatesIndicator;
    private View cardEventIndicator;
    private LinearLayout layoutEmptyState;
    private CalendarView calendarView;
    private CalendarDecorator calendarDecorator;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    private Button btnPreviousMonth;
    private Button btnNextMonth;
    
    // Store event dates for MaterialDatePicker decorator
    private Set<Long> eventDateTimestamps = new HashSet<>();
    // ✅ FIX: Removed btnSyncCalendar and btnFilter per user request
    private FloatingActionButton fabAddEvent;
    
    private CalendarEventAdapter eventAdapter;
    private ProjectCalendarViewModel viewModel;
    
    private SimpleDateFormat monthYearFormat;
    private SimpleDateFormat dateFormat;

    public static ProjectCalendarFragment newInstance(String projectId) {
        ProjectCalendarFragment fragment = new ProjectCalendarFragment();
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
        selectedCalendar = Calendar.getInstance();
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProjectCalendarViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_calendar, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupButtons();
        setupObservers();
        loadCalendarData();
        updateDateDisplay();
        
        return view;
    }

    private void initViews(View view) {
        tvCurrentMonth = view.findViewById(R.id.tvCurrentMonth);
        rvCalendarEvents = view.findViewById(R.id.rvCalendarEvents);
        progressBar = view.findViewById(R.id.progressBar);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        tvEventCount = view.findViewById(R.id.tvEventCount);
        tvEventDatesIndicator = view.findViewById(R.id.tvEventDatesIndicator);
        cardEventIndicator = view.findViewById(R.id.cardEventIndicator);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        calendarView = view.findViewById(R.id.calendarView);
        calendarDecorator = view.findViewById(R.id.calendarDecorator);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        // ✅ FIX: Removed findViewById for btnSyncCalendar and btnFilter
        fabAddEvent = view.findViewById(R.id.fabAddEvent);
        
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        setupSwipeRefresh();
        setupCalendarView();
        setupFabButton();
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
            );
            swipeRefreshLayout.setOnRefreshListener(() -> {
                android.util.Log.d("ProjectCalendar", "🔄 Pull-to-refresh - reloading calendar data");
                loadCalendarData();
            });
        }
    }

    private void setupFabButton() {
        if (fabAddEvent != null) {
            fabAddEvent.setOnClickListener(v -> {
                showCreateEventDialog();
            });
        }
    }
    
    private void showCreateEventDialog() {
        if (projectId != null && !projectId.isEmpty()) {
            CreateEventDialog dialog = CreateEventDialog.newInstance(projectId);
            dialog.setOnEventCreatedListener(event -> {
                // Reload calendar data after event created
                loadCalendarData();
                Toast.makeText(getContext(), "Event created successfully", Toast.LENGTH_SHORT).show();
            });
            dialog.show(getParentFragmentManager(), "create_event");
        } else {
            Toast.makeText(getContext(), "No project selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        eventAdapter = new CalendarEventAdapter();
        rvCalendarEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCalendarEvents.setAdapter(eventAdapter);
        
        eventAdapter.setOnEventClickListener(event -> {
            Toast.makeText(getContext(), "Event: " + event.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }
    
    private void setupCalendarView() {
        if (calendarView != null) {
            calendarView.setDate(selectedCalendar.getTimeInMillis(), false, true);
            
            calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                selectedCalendar.set(year, month, dayOfMonth);
                updateDateDisplay();
                
                int currentYear = selectedCalendar.get(Calendar.YEAR);
                int currentMonth = selectedCalendar.get(Calendar.MONTH);
                int currentDay = selectedCalendar.get(Calendar.DAY_OF_MONTH);
                viewModel.filterEventsByDate(currentYear, currentMonth, currentDay);
            });
        }
    }

    private void setupButtons() {
        btnPreviousMonth.setOnClickListener(v -> {
            selectedCalendar.add(Calendar.MONTH, -1);
            calendarView.setDate(selectedCalendar.getTimeInMillis(), true, true);
            updateDateDisplay();
            loadCalendarData();
        });
        
        btnNextMonth.setOnClickListener(v -> {
            selectedCalendar.add(Calendar.MONTH, 1);
            calendarView.setDate(selectedCalendar.getTimeInMillis(), true, true);
            updateDateDisplay();
            loadCalendarData();
        });
        
        // Make CalendarView clickable to show MaterialDatePicker
        calendarView.setOnClickListener(v -> showMaterialCalendarPicker());
    }
    
    private void setupObservers() {
        // Observe all events (for date marking)
        viewModel.getAllEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                updateEventDatesIndicator(events);
            }
        });
        
        // Observe filtered events
        viewModel.getFilteredEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                updateEventsList(events);
            }
        });
        
        // Observe loading state
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (swipeRefreshLayout != null && !isLoading) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        
        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(getView(), error, Snackbar.LENGTH_LONG)
                    .setAction("Retry", v -> loadCalendarData())
                    .show();
            }
        });
        
        // Observe sync success
        viewModel.getSyncSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Calendar synced successfully", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observe calendar not connected
        viewModel.getCalendarNotConnected().observe(getViewLifecycleOwner(), notConnected -> {
            if (notConnected != null && notConnected) {
                Toast.makeText(getContext(), 
                    "Connect Google Calendar in Account settings to sync events", 
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateDateDisplay() {
        String monthYearText = monthYearFormat.format(selectedCalendar.getTime());
        tvCurrentMonth.setText(monthYearText);
        
        String selectedDateText = dateFormat.format(selectedCalendar.getTime());
        tvSelectedDate.setText("Selected date: " + selectedDateText);
    }

    private void loadCalendarData() {
        if (projectId == null || projectId.isEmpty()) {
            Toast.makeText(getContext(), "No project ID available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int year = selectedCalendar.get(Calendar.YEAR);
        int month = selectedCalendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-indexed
        int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);
        
        viewModel.loadProjectCalendarEvents(projectId, year, month);
        
        // After loading, filter events for currently selected date
        // This will be applied once events are loaded (via observer)
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(() -> {
            viewModel.filterEventsByDate(year, month - 1, day); // month is 0-indexed for Calendar
        }, 300); // Small delay to ensure events are loaded first
    }

    private void updateEventsList(List<CalendarEvent> events) {
        if (events == null || events.isEmpty()) {
            // Show empty state
            rvCalendarEvents.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
            tvEventCount.setText("0 items");
        } else {
            // Show events list
            rvCalendarEvents.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            
            // Count tasks and events separately
            int taskCount = 0;
            int eventCount = 0;
            for (CalendarEvent event : events) {
                if (event.isTask()) {
                    taskCount++;
                } else {
                    eventCount++;
                }
            }
            
            String countText;
            if (taskCount > 0 && eventCount > 0) {
                countText = taskCount + " task" + (taskCount > 1 ? "s" : "") + ", " +
                           eventCount + " event" + (eventCount > 1 ? "s" : "");
            } else if (taskCount > 0) {
                countText = taskCount + " task" + (taskCount > 1 ? "s" : "");
            } else {
                countText = eventCount + " event" + (eventCount > 1 ? "s" : "");
            }
            tvEventCount.setText(countText);
            
            eventAdapter.setEvents(events);
        }
    }
    
    /**
     * Show empty state when no events exist
     * This is called when calendar data is loaded and list is empty
     */
    private void showEmptyState() {
        rvCalendarEvents.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        tvEventCount.setText("0 events");
    }
    
    /**
     * Hide empty state when events exist
     */
    private void hideEmptyState() {
        rvCalendarEvents.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
    }
    
    /**
     * Update indicator showing which dates have tasks (from eventType)
     */
    private Set<Integer> taskDates = new HashSet<>();
    private Set<Integer> eventDates = new HashSet<>();
    
    private void updateEventDatesIndicator(List<CalendarEvent> events) {
        eventDateTimestamps.clear();
        taskDates.clear();
        eventDates.clear();
        
        if (events == null || events.isEmpty()) {
            cardEventIndicator.setVisibility(View.GONE);
            return;
        }
        
        // Extract unique dates with tasks and events
        int currentYear = selectedCalendar.get(Calendar.YEAR);
        int currentMonth = selectedCalendar.get(Calendar.MONTH);
        
        for (CalendarEvent item : events) {
            if (item.getStartAt() != null) {
                try {
                    // Parse date
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    isoFormat.setLenient(false);
                    Date itemDate = isoFormat.parse(item.getStartAt());
                    
                    if (itemDate != null) {
                        Calendar itemCal = Calendar.getInstance();
                        itemCal.setTime(itemDate);
                        
                        // Normalize to start of day for comparison
                        itemCal.set(Calendar.HOUR_OF_DAY, 0);
                        itemCal.set(Calendar.MINUTE, 0);
                        itemCal.set(Calendar.SECOND, 0);
                        itemCal.set(Calendar.MILLISECOND, 0);
                        
                        // Store timestamp for MaterialDatePicker
                        eventDateTimestamps.add(itemCal.getTimeInMillis());
                        
                        // Only include if same month/year for text indicator
                        if (itemCal.get(Calendar.YEAR) == currentYear && 
                            itemCal.get(Calendar.MONTH) == currentMonth) {
                            
                            int dayOfMonth = itemCal.get(Calendar.DAY_OF_MONTH);
                            
                            // Separate tasks and events based on eventType
                            if (item.isTask()) {
                                taskDates.add(dayOfMonth);
                            } else {
                                eventDates.add(dayOfMonth);
                            }
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("Calendar", "Error parsing date: " + item.getStartAt(), e);
                }
            }
        }
        
        updateCombinedDateIndicator();
    }
    
    /**
     * Update combined indicator showing dates with both tasks and events
     */
    private void updateCombinedDateIndicator() {
        // Combine all dates
        Set<Integer> allDates = new HashSet<>();
        allDates.addAll(taskDates);
        allDates.addAll(eventDates);
        
        if (allDates.isEmpty()) {
            cardEventIndicator.setVisibility(View.GONE);
        } else {
            cardEventIndicator.setVisibility(View.VISIBLE);
            
            // Sort dates
            List<Integer> sortedDates = new ArrayList<>(allDates);
            java.util.Collections.sort(sortedDates);
            
            // Build indicator text with colored markers
            StringBuilder datesText = new StringBuilder();
            
            if (taskDates.size() > 0 && eventDates.size() > 0) {
                datesText.append("📅 ");
                datesText.append(taskDates.size()).append(" ngày có task, ");
                datesText.append(eventDates.size()).append(" ngày có event: ");
            } else if (taskDates.size() > 0) {
                datesText.append("🟠 Ngày có task: ");
            } else {
                datesText.append("🟢 Ngày có event: ");
            }
            
            for (int i = 0; i < Math.min(sortedDates.size(), 15); i++) {
                if (i > 0) datesText.append(", ");
                
                int date = sortedDates.get(i);
                boolean hasTask = taskDates.contains(date);
                boolean hasEvent = eventDates.contains(date);
                
                if (hasTask && hasEvent) {
                    datesText.append(date).append("🔶"); // Both
                } else if (hasTask) {
                    datesText.append(date).append("🟠"); // Task only
                } else {
                    datesText.append(date).append("🟢"); // Event only
                }
            }
            
            if (sortedDates.size() > 15) {
                datesText.append(" +").append(sortedDates.size() - 15).append(" ngày khác");
            }
            
            tvEventDatesIndicator.setText(datesText.toString());
            tvEventDatesIndicator.setTextSize(14);
            tvEventDatesIndicator.setTextColor(getResources().getColor(R.color.colorAccent, null));
            tvEventDatesIndicator.setOnClickListener(v -> showMaterialCalendarPicker());
        }
        
        // Update calendar decorations
        updateCalendarDecorations();
    }
    
    /**
     * Update calendar decorations (colored dots on dates)
     */
    private void updateCalendarDecorations() {
        if (calendarDecorator == null || calendarView == null) return;
        
        // Combine decorations
        Map<Integer, CalendarDecorator.DateDecoration> decorations = new HashMap<>();
        
        for (Integer day : taskDates) {
            decorations.put(day, new CalendarDecorator.DateDecoration(
                true, eventDates.contains(day)
            ));
        }
        
        for (Integer day : eventDates) {
            if (!decorations.containsKey(day)) {
                decorations.put(day, new CalendarDecorator.DateDecoration(
                    false, true
                ));
            }
        }
        
        // Calculate cell dimensions (approximate)
        int calendarWidth = calendarView.getWidth();
        int calendarHeight = calendarView.getHeight();
        
        if (calendarWidth == 0 || calendarHeight == 0) {
            // Wait for layout
            calendarView.post(() -> updateCalendarDecorations());
            return;
        }
        
        // Match decorator height to calendar
        android.view.ViewGroup.LayoutParams params = calendarDecorator.getLayoutParams();
        if (params.height != calendarHeight) {
            params.height = calendarHeight;
            calendarDecorator.setLayoutParams(params);
        }
        
        int cellWidth = calendarWidth / 7;
        
        // Estimate cell height based on calendar height
        // CalendarView typically shows ~6 weeks
        int cellHeight = calendarHeight / 8; // Approximate (header + 6 weeks)
        
        // Get first day of month offset
        Calendar firstDay = (Calendar) selectedCalendar.clone();
        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        int startOffset = firstDay.get(Calendar.DAY_OF_WEEK) - 1; // Sunday = 0
        
        calendarDecorator.setDecorations(decorations, cellWidth, cellHeight, startOffset);
    }
    
    /**
     * Show MaterialDatePicker with event dates marked using custom decorator
     * This provides a modern calendar UI with visual indicators for dates with events
     */
    private void showMaterialCalendarPicker() {
        // Create constraints to highlight event dates
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        
        // Set validator to highlight dates with events
        constraintsBuilder.setValidator(new CalendarConstraints.DateValidator() {
            @Override
            public boolean isValid(long date) {
                return true; // All dates are selectable
            }
            
            @Override
            public int describeContents() {
                return 0;
            }
            
            @Override
            public void writeToParcel(android.os.Parcel dest, int flags) {
                // Required for Parcelable but not used
            }
        });
        
        // Build MaterialDatePicker
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(selectedCalendar.getTimeInMillis())
            .setCalendarConstraints(constraintsBuilder.build())
            .build();
        
        // Handle date selection
        picker.addOnPositiveButtonClickListener(selection -> {
            selectedCalendar.setTimeInMillis(selection);
            calendarView.setDate(selection, true, true);
            updateDateDisplay();
            
            int year = selectedCalendar.get(Calendar.YEAR);
            int month = selectedCalendar.get(Calendar.MONTH);
            int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);
            
            viewModel.filterEventsByDate(year, month, day);
        });
        
        picker.show(getParentFragmentManager(), "MATERIAL_DATE_PICKER");
    }
}
