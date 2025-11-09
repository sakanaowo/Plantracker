package com.example.tralalero.feature.home.ui.Home.event;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.EventType;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Dialog for quick event creation
 */
public class QuickEventDialog extends DialogFragment {

    private QuickEventViewModel viewModel;
    private OnEventCreatedListener listener;
    
    private String projectId;

    private TextInputEditText etTitle;
    private TextInputEditText etDate;
    private TextInputEditText etTime;
    private AutoCompleteTextView actvDuration;
    private ChipGroup chipGroupType;
    private Chip chipMeeting, chipMilestone, chipOther;
    private SwitchMaterial switchGoogleMeet;
    private TextInputEditText etDescription;
    private Button btnCancel, btnCreate;

    private Calendar selectedCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    public interface OnEventCreatedListener {
        void onEventCreated();
    }

    public static QuickEventDialog newInstance(String projectId) {
        QuickEventDialog dialog = new QuickEventDialog();
        Bundle args = new Bundle();
        args.putString("project_id", projectId);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnEventCreatedListener(OnEventCreatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);

        if (getArguments() != null) {
            projectId = getArguments().getString("project_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_quick_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupViewModel();
        setupDurationDropdown();
        setupDateTimePickers();
        setupButtons();
        
        // Set default values
        setDefaultDateTime();
    }

    private void initializeViews(View view) {
        etTitle = view.findViewById(R.id.etTitle);
        etDate = view.findViewById(R.id.etDate);
        etTime = view.findViewById(R.id.etTime);
        actvDuration = view.findViewById(R.id.actvDuration);
        chipGroupType = view.findViewById(R.id.chipGroupType);
        chipMeeting = view.findViewById(R.id.chipMeeting);
        chipMilestone = view.findViewById(R.id.chipMilestone);
        chipOther = view.findViewById(R.id.chipOther);
        switchGoogleMeet = view.findViewById(R.id.switchGoogleMeet);
        etDescription = view.findViewById(R.id.etDescription);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnCreate = view.findViewById(R.id.btnCreate);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(QuickEventViewModel.class);

        // Observe event creation
        viewModel.getEventCreated().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                Toast.makeText(getContext(), "Event created successfully!", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onEventCreated();
                }
                dismiss();
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnCreate.setEnabled(!isLoading);
            btnCancel.setEnabled(!isLoading);
        });

        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDurationDropdown() {
        String[] durations = new String[]{
            "15 minutes", "30 minutes", "45 minutes",
            "60 minutes", "90 minutes", "120 minutes"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            getContext(),
            android.R.layout.simple_dropdown_item_1line,
            durations
        );

        actvDuration.setAdapter(adapter);
    }

    private void setupDateTimePickers() {
        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());
    }

    private void showDatePicker() {
        DatePickerDialog picker = new DatePickerDialog(
            getContext(),
            (view, year, month, dayOfMonth) -> {
                selectedCalendar.set(year, month, dayOfMonth);
                etDate.setText(dateFormat.format(selectedCalendar.getTime()));
            },
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }

    private void showTimePicker() {
        TimePickerDialog picker = new TimePickerDialog(
            getContext(),
            (view, hourOfDay, minute) -> {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedCalendar.set(Calendar.MINUTE, minute);
                etTime.setText(timeFormat.format(selectedCalendar.getTime()));
            },
            selectedCalendar.get(Calendar.HOUR_OF_DAY),
            selectedCalendar.get(Calendar.MINUTE),
            false
        );
        picker.show();
    }

    private void setupButtons() {
        btnCancel.setOnClickListener(v -> dismiss());
        btnCreate.setOnClickListener(v -> createEvent());
    }

    private void setDefaultDateTime() {
        etDate.setText(dateFormat.format(selectedCalendar.getTime()));
        etTime.setText(timeFormat.format(selectedCalendar.getTime()));
    }

    private void createEvent() {
        // Validate inputs
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Please enter event title", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateStr = etDate.getText().toString();
        String timeStr = etTime.getText().toString();
        if (dateStr.isEmpty() || timeStr.isEmpty()) {
            Toast.makeText(getContext(), "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse duration
        String durationStr = actvDuration.getText().toString();
        int duration = 60; // default
        try {
            duration = Integer.parseInt(durationStr.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            // Use default
        }

        // Get event type
        EventType eventType = EventType.MEETING;
        int selectedId = chipGroupType.getCheckedChipId();
        if (selectedId == chipMilestone.getId()) {
            eventType = EventType.MILESTONE;
        } else if (selectedId == chipOther.getId()) {
            eventType = EventType.OTHER;
        }

        String description = etDescription.getText().toString().trim();
        boolean createGoogleMeet = switchGoogleMeet.isChecked();

        // Call ViewModel with correct parameters:
        // projectId, title, description, date(Calendar), duration, type, recurrence, attendeeIds, createGoogleMeet
        viewModel.createEvent(
            projectId,
            title,
            description,
            selectedCalendar,
            duration,
            eventType.name(),
            "NONE", // recurrence
            new java.util.ArrayList<>(), // attendeeIds (empty for now)
            createGoogleMeet
        );
    }
}
