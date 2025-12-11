package com.example.tralalero.feature.home.ui.Home.project;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.App.App;
import com.example.tralalero.data.remote.api.ProjectApiService;
import com.example.tralalero.data.remote.dto.calendar.CalendarDataResponse;
import com.example.tralalero.data.remote.dto.task.TaskDTO;
import com.example.tralalero.data.remote.dto.event.EventDTO;
import com.example.tralalero.network.ApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * New Calendar Fragment with custom grid layout
 * - Month/Year header with navigation
 * - 7-column grid (Mon-Sun) with day states
 * - Event indicators (dots) for days with tasks/events
 * - Task/Event list below calendar for selected date
 */
public class ProjectCalendarFragment extends Fragment {
    private static final String TAG = "ProjectCalendarFragment";
    
    private String projectId;
    private Calendar currentCalendar;
    private Calendar selectedCalendar;
    
    public static ProjectCalendarFragment newInstance(String projectId) {
        ProjectCalendarFragment fragment = new ProjectCalendarFragment();
        Bundle args = new Bundle();
        args.putString("project_id", projectId);
        fragment.setArguments(args);
        return fragment;
    }
    
    // Header views
    private TextView tvMonthYear;
    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    
    // Calendar grid
    private RecyclerView rvCalendarGrid;
    private CalendarGridAdapter calendarGridAdapter;
    
    // Task/Event list for selected date
    private TextView tvSelectedDate;
    private RecyclerView rvTasksEvents;
    private TextView tvEmptyState;
    private ProgressBar progressBar;
    
    private SimpleDateFormat monthYearFormat;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat apiMonthFormat;
    
    // API
    private ProjectApiService projectApiService;
    
    // Data cache
    private List<TaskDTO> allTasks;
    private List<EventDTO> allEvents;
    private Set<String> datesWithItems;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            projectId = getArguments().getString("project_id");
        }
        
        currentCalendar = Calendar.getInstance();
        selectedCalendar = null; // No date selected by default
        
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        apiMonthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        
        projectApiService = ApiClient.get(App.authManager).create(ProjectApiService.class);
        
        allTasks = new ArrayList<>();
        allEvents = new ArrayList<>();
        datesWithItems = new HashSet<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_calendar, container, false);
        
        initViews(view);
        setupCalendarGrid();
        setupTaskEventList();
        setupListeners();
        
        // Load initial data for current month
        loadMonthData(currentCalendar);
        
        return view;
    }
    
    private void initViews(View view) {
        // Header
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        
        // Calendar grid
        rvCalendarGrid = view.findViewById(R.id.rvCalendarGrid);
        
        // Task/Event list
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        rvTasksEvents = view.findViewById(R.id.rvTasksEvents);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        progressBar = view.findViewById(R.id.progressBar);
        
        updateMonthYearHeader();
    }
    
    private void setupCalendarGrid() {
        // 7 columns for days of week
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 7);
        rvCalendarGrid.setLayoutManager(layoutManager);
        
        calendarGridAdapter = new CalendarGridAdapter(new ArrayList<>(), new CalendarGridAdapter.OnDayClickListener() {
            @Override
            public void onDayClick(CalendarDay day) {
                if (!day.isCurrentMonth()) return; // Ignore clicks on outside month days
                
                selectedCalendar = (Calendar) currentCalendar.clone();
                selectedCalendar.set(Calendar.DAY_OF_MONTH, day.getDayOfMonth());
                
                // Update selection visual
                calendarGridAdapter.setSelectedDate(selectedCalendar);
                
                // Load tasks/events for this date
                loadTasksForDate(selectedCalendar);
            }
        });
        
        rvCalendarGrid.setAdapter(calendarGridAdapter);
        
        // Generate initial calendar days
        generateCalendarDays();
    }
    
    private void setupTaskEventList() {
        // Setup adapter with navigation handlers
        CalendarItemAdapter adapter = new CalendarItemAdapter(new ArrayList<>(), new CalendarItemAdapter.OnItemClickListener() {
            @Override
            public void onTaskClick(TaskDTO task) {
                // Show task detail bottom sheet
                showTaskDetail(task);
            }
            
            @Override
            public void onEventClick(EventDTO event) {
                // Navigate to event tab and focus event
                navigateToEventTab(event.getId());
            }
        });
        
        rvTasksEvents.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        rvTasksEvents.setAdapter(adapter);
        
        // Hide by default
        tvSelectedDate.setVisibility(View.GONE);
        rvTasksEvents.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);
    }
    
    private void setupListeners() {
        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateMonthYearHeader();
            generateCalendarDays();
            loadMonthData(currentCalendar);
            clearSelection();
        });
        
        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateMonthYearHeader();
            generateCalendarDays();
            loadMonthData(currentCalendar);
            clearSelection();
        });
    }
    
    private void updateMonthYearHeader() {
        tvMonthYear.setText(monthYearFormat.format(currentCalendar.getTime()));
    }
    
    private void generateCalendarDays() {
        List<CalendarDay> days = new ArrayList<>();
        
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        int monthStartDay = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday, 2=Monday...
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Adjust to Monday start (1=Monday, 7=Sunday)
        int firstDayOfWeek = monthStartDay == Calendar.SUNDAY ? 7 : monthStartDay - 1;
        
        // Add days from previous month
        Calendar prevMonth = (Calendar) cal.clone();
        prevMonth.add(Calendar.MONTH, -1);
        int daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        for (int i = firstDayOfWeek - 1; i > 0; i--) {
            days.add(new CalendarDay(daysInPrevMonth - i + 1, false, false, false));
        }
        
        // Add days of current month
        Calendar today = Calendar.getInstance();
        for (int day = 1; day <= daysInMonth; day++) {
            boolean isToday = today.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                             today.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                             today.get(Calendar.DAY_OF_MONTH) == day;
            days.add(new CalendarDay(day, true, isToday, false));
        }
        
        // Fill remaining cells with next month days
        int remainingDays = 42 - days.size();
        for (int i = 1; i <= remainingDays; i++) {
            days.add(new CalendarDay(i, false, false, false));
        }
        
        calendarGridAdapter.updateDays(days);
    }
    
    private void loadMonthData(Calendar calendar) {
        String month = apiMonthFormat.format(calendar.getTime());
        Log.d(TAG, "Loading data for month: " + month);
        
        progressBar.setVisibility(View.VISIBLE);
        
        projectApiService.getCalendarData(projectId, month).enqueue(new Callback<CalendarDataResponse>() {
            @Override
            public void onResponse(Call<CalendarDataResponse> call, Response<CalendarDataResponse> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    CalendarDataResponse data = response.body();
                    
                    // Cache data
                    allTasks = data.getTasks() != null ? data.getTasks() : new ArrayList<>();
                    allEvents = data.getEvents() != null ? data.getEvents() : new ArrayList<>();
                    datesWithItems.clear();
                    if (data.getDatesWithItems() != null) {
                        datesWithItems.addAll(data.getDatesWithItems());
                    }
                    
                    Log.d(TAG, "✅ Loaded " + allTasks.size() + " tasks, " + allEvents.size() + " events");
                    Log.d(TAG, "✅ Dates with items: " + datesWithItems.size());
                    
                    // Update calendar grid with event indicators
                    updateCalendarWithEventIndicators();
                } else {
                    Log.e(TAG, "Failed to load calendar data: " + response.code());
                    Toast.makeText(getContext(), "Failed to load calendar data", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<CalendarDataResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading calendar data", t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateCalendarWithEventIndicators() {
        List<CalendarDay> days = calendarGridAdapter.getDays();
        if (days == null || days.isEmpty()) {
            return;
        }
        
        Calendar cal = (Calendar) currentCalendar.clone();
        
        for (CalendarDay day : days) {
            if (day.isCurrentMonth()) {
                cal.set(Calendar.DAY_OF_MONTH, day.getDayOfMonth());
                String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
                
                boolean hasEvents = datesWithItems.contains(dateStr);
                day.setHasEvents(hasEvents);
            }
        }
        
        calendarGridAdapter.notifyDataSetChanged();
    }
    
    private void loadTasksForDate(Calendar date) {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date.getTime());
        Log.d(TAG, "Loading tasks for date: " + dateStr);
        
        // Filter tasks for this date
        List<TaskDTO> tasksForDate = new ArrayList<>();
        for (TaskDTO task : allTasks) {
            if (task.getDueAt() != null && task.getDueAt().startsWith(dateStr)) {
                tasksForDate.add(task);
            }
        }
        
        // Filter events for this date
        List<EventDTO> eventsForDate = new ArrayList<>();
        for (EventDTO event : allEvents) {
            if (event.getStartAt() != null && event.getStartAt().startsWith(dateStr)) {
                eventsForDate.add(event);
            }
        }
        
        Log.d(TAG, "Found " + tasksForDate.size() + " tasks, " + eventsForDate.size() + " events");
        
        tvSelectedDate.setText(dateFormat.format(date.getTime()));
        tvSelectedDate.setVisibility(View.VISIBLE);
        
        if (tasksForDate.isEmpty() && eventsForDate.isEmpty()) {
            rvTasksEvents.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No tasks or events on this date");
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvTasksEvents.setVisibility(View.VISIBLE);
            
            // Combine tasks and events into CalendarItem list
            List<CalendarItem> items = new ArrayList<>();
            for (TaskDTO task : tasksForDate) {
                items.add(CalendarItem.fromTask(task));
            }
            for (EventDTO event : eventsForDate) {
                items.add(CalendarItem.fromEvent(event));
            }
            
            // Update adapter
            CalendarItemAdapter adapter = (CalendarItemAdapter) rvTasksEvents.getAdapter();
            if (adapter != null) {
                adapter.updateItems(items);
            }
        }
    }
    
    private void clearSelection() {
        selectedCalendar = null;
        calendarGridAdapter.setSelectedDate(null);
        tvSelectedDate.setVisibility(View.GONE);
        rvTasksEvents.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);
    }
    
    /**
     * Show task detail bottom sheet
     */
    private void showTaskDetail(TaskDTO task) {
        if (getActivity() instanceof com.example.tralalero.feature.home.ui.Home.ProjectActivity) {
            com.example.tralalero.feature.home.ui.Home.ProjectActivity activity = 
                (com.example.tralalero.feature.home.ui.Home.ProjectActivity) getActivity();
            
            activity.showTaskDetailFromDTO(task);
        }
    }
    
    /**
     * Navigate to Event tab and focus on specific event
     */
    private void navigateToEventTab(String eventId) {
        if (getActivity() instanceof com.example.tralalero.feature.home.ui.Home.ProjectActivity) {
            com.example.tralalero.feature.home.ui.Home.ProjectActivity activity = 
                (com.example.tralalero.feature.home.ui.Home.ProjectActivity) getActivity();
            
            // Switch to Event tab (index 4)
            com.google.android.material.tabs.TabLayout tabLayout = activity.findViewById(R.id.tabLayout);
            if (tabLayout != null) {
                com.google.android.material.tabs.TabLayout.Tab eventTab = tabLayout.getTabAt(4);
                if (eventTab != null) {
                    eventTab.select();
                    
                    // TODO: Scroll to specific event after tab switch
                    // This requires exposing a method in ProjectActivity to focus on an event
                    Log.d(TAG, "Navigated to event tab for event: " + eventId);
                }
            }
        }
    }
}
