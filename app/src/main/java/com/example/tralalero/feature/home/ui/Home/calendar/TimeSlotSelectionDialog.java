package com.example.tralalero.feature.home.ui.Home.calendar;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.TimeSlot;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Dialog for selecting meeting time slots based on availability
 */
public class TimeSlotSelectionDialog extends DialogFragment {

    private MeetingSchedulerViewModel viewModel;
    private TimeSlotAdapter adapter;
    private OnTimeSlotSelectedListener listener;
    
    private List<String> selectedMemberIds;
    private String projectId;

    private TextInputEditText etDuration;
    private TextInputEditText etStartDate;
    private TextInputEditText etEndDate;
    private Button btnFindTimes;
    private Button btnClose;
    private ProgressBar progressBar;
    private RecyclerView rvTimeSlots;
    private TextView tvResultsTitle;
    private View layoutEmptyState;

    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public interface OnTimeSlotSelectedListener {
        void onTimeSlotSelected(TimeSlot timeSlot);
    }

    public static TimeSlotSelectionDialog newInstance(String projectId, List<String> memberIds) {
        TimeSlotSelectionDialog dialog = new TimeSlotSelectionDialog();
        Bundle args = new Bundle();
        args.putString("project_id", projectId);
        args.putStringArrayList("member_ids", (ArrayList<String>) memberIds);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnTimeSlotSelectedListener(OnTimeSlotSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);

        if (getArguments() != null) {
            projectId = getArguments().getString("project_id");
            selectedMemberIds = getArguments().getStringArrayList("member_ids");
        }

        // Initialize end date to 7 days from now
        endCalendar.add(Calendar.DAY_OF_MONTH, 7);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_time_slot_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupViewModel();
        setupRecyclerView();
        setupDatePickers();
        setupButtons();
        
        // Set initial dates
        etStartDate.setText(dateFormat.format(startCalendar.getTime()));
        etEndDate.setText(dateFormat.format(endCalendar.getTime()));
    }

    private void initializeViews(View view) {
        etDuration = view.findViewById(R.id.etDuration);
        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate = view.findViewById(R.id.etEndDate);
        btnFindTimes = view.findViewById(R.id.btnFindTimes);
        btnClose = view.findViewById(R.id.btnClose);
        progressBar = view.findViewById(R.id.progressBar);
        rvTimeSlots = view.findViewById(R.id.rvTimeSlots);
        tvResultsTitle = view.findViewById(R.id.tvResultsTitle);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MeetingSchedulerViewModel.class);

        // Observe suggested times
        viewModel.getSuggestedTimes().observe(getViewLifecycleOwner(), timeSlots -> {
            if (timeSlots != null && !timeSlots.isEmpty()) {
                adapter.submitList(timeSlots);
                showResults();
            } else {
                showEmptyState();
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });

        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new TimeSlotAdapter(timeSlot -> {
            if (listener != null) {
                listener.onTimeSlotSelected(timeSlot);
            }
            dismiss();
        });

        rvTimeSlots.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTimeSlots.setAdapter(adapter);
    }

    private void setupDatePickers() {
        etStartDate.setOnClickListener(v -> showDatePicker(true));
        etEndDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = isStartDate ? startCalendar : endCalendar;

        DatePickerDialog picker = new DatePickerDialog(
            getContext(),
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                String formattedDate = dateFormat.format(calendar.getTime());
                
                if (isStartDate) {
                    etStartDate.setText(formattedDate);
                } else {
                    etEndDate.setText(formattedDate);
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );

        picker.show();
    }

    private void setupButtons() {
        btnFindTimes.setOnClickListener(v -> findAvailableTimes());
        btnClose.setOnClickListener(v -> dismiss());
    }

    private void findAvailableTimes() {
        // Validate inputs
        String durationStr = etDuration.getText().toString();
        if (durationStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter duration", Toast.LENGTH_SHORT).show();
            return;
        }

        int duration = Integer.parseInt(durationStr);
        if (duration <= 0) {
            Toast.makeText(getContext(), "Duration must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call ViewModel to suggest times with Calendar objects
        viewModel.suggestTimes(duration, startCalendar, endCalendar);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        rvTimeSlots.setVisibility(View.GONE);
        tvResultsTitle.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showResults() {
        progressBar.setVisibility(View.GONE);
        rvTimeSlots.setVisibility(View.VISIBLE);
        tvResultsTitle.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        progressBar.setVisibility(View.GONE);
        rvTimeSlots.setVisibility(View.GONE);
        tvResultsTitle.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }
}
