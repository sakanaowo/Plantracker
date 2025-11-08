package com.example.tralalero.feature.home.ui.Home.project;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.ProjectEvent;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Dialog for creating a new project event
 */
public class CreateEventDialog extends DialogFragment {
    private TextInputEditText etEventTitle;
    private TextInputEditText etEventDate;
    private TextInputEditText etEventTime;
    private TextInputEditText etDuration;
    private TextInputEditText etEventDescription;
    private RadioGroup rgEventType;
    private ChipGroup chipGroupAttendees;
    private SwitchMaterial switchCreateMeet;
    private Spinner spinnerRecurrence;
    
    private String projectId;
    private List<String> selectedAttendeeIds = new ArrayList<>();
    private OnEventCreatedListener listener;
    
    public interface OnEventCreatedListener {
        void onEventCreated(ProjectEvent event);
    }
    
    public static CreateEventDialog newInstance(String projectId) {
        CreateEventDialog dialog = new CreateEventDialog();
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
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        
        if (getArguments() != null) {
            projectId = getArguments().getString("project_id");
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_event, container, false);
        
        initViews(view);
        setupDateTimePickers();
        setupAttendees(view);
        setupRecurrence();
        setupButtons(view);
        
        return view;
    }
    
    private void initViews(View view) {
        etEventTitle = view.findViewById(R.id.etEventTitle);
        etEventDate = view.findViewById(R.id.etEventDate);
        etEventTime = view.findViewById(R.id.etEventTime);
        etDuration = view.findViewById(R.id.etDuration);
        etEventDescription = view.findViewById(R.id.etEventDescription);
        rgEventType = view.findViewById(R.id.rgEventType);
        chipGroupAttendees = view.findViewById(R.id.chipGroupAttendees);
        switchCreateMeet = view.findViewById(R.id.switchCreateMeet);
        spinnerRecurrence = view.findViewById(R.id.spinnerRecurrence);
    }
    
    private void setupDateTimePickers() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        // Date picker
        etEventDate.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    etEventDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
            datePicker.show();
        });
        
        // Time picker
        etEventTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    etEventTime.setText(timeFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            );
            timePicker.show();
        });
    }
    
    private void setupAttendees(View view) {
        View btnAddAttendees = view.findViewById(R.id.btnAddAttendees);
        if (btnAddAttendees != null) {
            btnAddAttendees.setOnClickListener(v -> {
                showSelectAttendeesDialog();
            });
        }
    }
    
    private void showSelectAttendeesDialog() {
        // TODO: Load project members và show selection dialog
        // Khi chọn xong, thêm chips vào chipGroupAttendees
        Toast.makeText(getContext(), "TODO: Select attendees from project members", Toast.LENGTH_SHORT).show();
        
        // Demo: Add a sample chip
        addAttendeeChip("user123", "John Doe");
    }
    
    private void addAttendeeChip(String userId, String userName) {
        Chip chip = new Chip(getContext());
        chip.setText(userName);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroupAttendees.removeView(chip);
            selectedAttendeeIds.remove(userId);
        });
        chipGroupAttendees.addView(chip);
        selectedAttendeeIds.add(userId);
    }
    
    private void setupRecurrence() {
        String[] recurrenceOptions = {
            "Không lặp lại",
            "Hàng ngày",
            "Hàng tuần",
            "Hai tuần một lần",
            "Hàng tháng"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            getContext(),
            android.R.layout.simple_spinner_item,
            recurrenceOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecurrence.setAdapter(adapter);
    }
    
    private void setupButtons(View view) {
        View btnClose = view.findViewById(R.id.btnCloseDialog);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }
        
        View btnCancel = view.findViewById(R.id.btnCancel);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dismiss());
        }
        
        View btnCreate = view.findViewById(R.id.btnCreateEvent);
        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> {
                if (validateInput()) {
                    createEvent();
                }
            });
        }
    }
    
    private boolean validateInput() {
        if (etEventTitle.getText() == null || etEventTitle.getText().toString().trim().isEmpty()) {
            etEventTitle.setError("Vui lòng nhập tiêu đề");
            etEventTitle.requestFocus();
            return false;
        }
        
        if (etEventDate.getText() == null || etEventDate.getText().toString().trim().isEmpty()) {
            etEventDate.setError("Vui lòng chọn ngày");
            etEventDate.requestFocus();
            return false;
        }
        
        if (etEventTime.getText() == null || etEventTime.getText().toString().trim().isEmpty()) {
            etEventTime.setError("Vui lòng chọn giờ");
            etEventTime.requestFocus();
            return false;
        }
        
        if (etDuration.getText() == null || etDuration.getText().toString().trim().isEmpty()) {
            etDuration.setError("Vui lòng nhập thời lượng");
            etDuration.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void createEvent() {
        // TODO: Build event object và call API
        ProjectEvent event = new ProjectEvent();
        event.setTitle(etEventTitle.getText().toString());
        event.setDate(parseDate(etEventDate.getText().toString()));
        event.setTime(etEventTime.getText().toString());
        
        String durationStr = etDuration.getText().toString();
        event.setDuration(durationStr.isEmpty() ? 60 : Integer.parseInt(durationStr));
        
        if (etEventDescription.getText() != null) {
            event.setDescription(etEventDescription.getText().toString());
        }
        
        event.setType(getSelectedEventType());
        event.setAttendeeIds(selectedAttendeeIds);
        event.setCreateGoogleMeet(switchCreateMeet.isChecked());
        event.setRecurrence(getSelectedRecurrence());
        event.setProjectId(projectId);
        
        // TODO: Call API to create event
        // For now, just callback with the event
        if (listener != null) {
            listener.onEventCreated(event);
        }
        
        dismiss();
    }
    
    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return format.parse(dateStr);
        } catch (Exception e) {
            return new Date();
        }
    }
    
    private String getSelectedEventType() {
        int selectedId = rgEventType.getCheckedRadioButtonId();
        if (selectedId == R.id.rbMeeting) return "MEETING";
        if (selectedId == R.id.rbMilestone) return "MILESTONE";
        if (selectedId == R.id.rbOther) return "OTHER";
        return "MEETING";
    }
    
    private String getSelectedRecurrence() {
        int position = spinnerRecurrence.getSelectedItemPosition();
        switch (position) {
            case 1: return "DAILY";
            case 2: return "WEEKLY";
            case 3: return "BIWEEKLY";
            case 4: return "MONTHLY";
            default: return "NONE";
        }
    }
}
