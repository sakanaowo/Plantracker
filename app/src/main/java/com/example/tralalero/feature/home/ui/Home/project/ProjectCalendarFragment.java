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

import com.example.tralalero.R;
import com.example.tralalero.domain.model.CalendarEvent;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProjectCalendarFragment extends Fragment {

    private String projectId;
    private Calendar selectedCalendar;
    
    private TextView tvCurrentMonth;
    private RecyclerView rvCalendarEvents;
    private ProgressBar progressBar;
    private TextView tvSelectedDate;
    private TextView tvEventCount;
    private LinearLayout layoutEmptyState;
    private CalendarView calendarView;
    
    private Button btnPreviousMonth;
    private Button btnNextMonth;
    private Button btnSyncCalendar;
    private Button btnFilter;
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
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        calendarView = view.findViewById(R.id.calendarView);
        
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        btnSyncCalendar = view.findViewById(R.id.btnSyncCalendar);
        btnFilter = view.findViewById(R.id.btnFilter);
        fabAddEvent = view.findViewById(R.id.fabAddEvent);
        
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        setupCalendarView();
        setupFabButton();
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
                loadCalendarData();
                
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
        
        btnSyncCalendar.setOnClickListener(v -> {
            if (projectId != null && !projectId.isEmpty()) {
                viewModel.syncWithGoogleCalendar(projectId);
                Toast.makeText(getContext(), "Syncing with Google Calendar...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No project selected", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnFilter.setOnClickListener(v -> {
            // Filter by selected date
            int year = selectedCalendar.get(Calendar.YEAR);
            int month = selectedCalendar.get(Calendar.MONTH);
            int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);
            viewModel.filterEventsByDate(year, month, day);
        });
    }
    
    private void setupObservers() {
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
        
        viewModel.loadProjectCalendarEvents(projectId, year, month);
    }

    private void updateEventsList(List<CalendarEvent> events) {
        if (events == null || events.isEmpty()) {
            rvCalendarEvents.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
            tvEventCount.setText("0 events");
        } else {
            rvCalendarEvents.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            tvEventCount.setText(events.size() + " event" + (events.size() > 1 ? "s" : ""));
            eventAdapter.setEvents(events);
        }
    }
}
