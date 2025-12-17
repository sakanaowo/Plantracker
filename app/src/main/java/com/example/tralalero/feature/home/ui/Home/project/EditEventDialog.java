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
import com.example.tralalero.App.App;
import com.example.tralalero.data.remote.api.MemberApiService;
import com.example.tralalero.data.remote.dto.member.MemberDTO;
import com.example.tralalero.domain.model.ProjectEvent;
import com.example.tralalero.domain.model.UpdateEventRequest;
import com.example.tralalero.domain.model.User;
import com.example.tralalero.feature.home.ui.Home.calendar.MemberSelectionBottomSheet;
import com.example.tralalero.network.ApiClient;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dialog for editing an existing project event
 */
public class EditEventDialog extends DialogFragment {
    private TextInputEditText etEventTitle;
    private TextInputEditText etEventDate;
    private TextInputEditText etEventTime;
    private TextInputEditText etDuration;
    private TextInputEditText etEventDescription;
    private RadioGroup rgEventType;
    private RadioButton rbMeeting;
    private RadioButton rbMilestone;
    private ChipGroup chipGroupAttendees;
    // ✅ REMOVED: spinnerRecurrence - Always NONE by default
    private android.widget.ProgressBar progressBarLoading;
    
    private String projectId;
    private ProjectEvent existingEvent;
    private List<String> selectedAttendeeIds = new ArrayList<>();
    private List<User> selectedAttendees = new ArrayList<>();
    private OnEventUpdatedListener listener;
    private boolean isLoadingMembers = false;
    
    public interface OnEventUpdatedListener {
        void onEventUpdated(UpdateEventRequest request);
    }
    
    public static EditEventDialog newInstance(ProjectEvent event) {
        EditEventDialog dialog = new EditEventDialog();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        dialog.setArguments(args);
        return dialog;
    }
    
    public void setOnEventUpdatedListener(OnEventUpdatedListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        
        if (getArguments() != null) {
            existingEvent = (ProjectEvent) getArguments().getSerializable("event");
            if (existingEvent != null) {
                projectId = existingEvent.getProjectId();
            }
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
        // ✅ REMOVED: setupRecurrence() - Always NONE by default
        setupButtons(view);
        
        // Pre-fill with existing event data
        prefillEventData();
        
        return view;
    }
    
    private void initViews(View view) {
        etEventTitle = view.findViewById(R.id.etEventTitle);
        etEventDate = view.findViewById(R.id.etEventDate);
        etEventTime = view.findViewById(R.id.etEventTime);
        etDuration = view.findViewById(R.id.etDuration);
        etEventDescription = view.findViewById(R.id.etEventDescription);
        rgEventType = view.findViewById(R.id.rgEventType);
        rbMeeting = view.findViewById(R.id.rbMeeting);
        rbMilestone = view.findViewById(R.id.rbMilestone);
        chipGroupAttendees = view.findViewById(R.id.chipGroupAttendees);
        // ✅ REMOVED: spinnerRecurrence findViewById - Always NONE by default
        progressBarLoading = view.findViewById(R.id.progressBarLoading);
    }
    
    private void prefillEventData() {
        if (existingEvent == null) return;
        
        // Set title
        etEventTitle.setText(existingEvent.getTitle());
        
        // Set description
        if (existingEvent.getDescription() != null) {
            etEventDescription.setText(existingEvent.getDescription());
        }
        
        // Set date
        if (existingEvent.getDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etEventDate.setText(dateFormat.format(existingEvent.getDate()));
        }
        
        // Set time
        if (existingEvent.getTime() != null) {
            etEventTime.setText(existingEvent.getTime());
        }
        
        // Set duration
        etDuration.setText(String.valueOf(existingEvent.getDuration()));
        
        // Set event type
        if ("MILESTONE".equals(existingEvent.getType())) {
            rbMilestone.setChecked(true);
        } else {
            rbMeeting.setChecked(true);
        }
        
        // ✅ REMOVED: Set recurrence - Always NONE by default
        
        // Set attendees
        if (existingEvent.getAttendeeIds() != null) {
            selectedAttendeeIds = new ArrayList<>(existingEvent.getAttendeeIds());
        }
    }
    
    // ✅ REMOVED: getRecurrencePosition() - Always NONE by default
    private int getRecurrencePosition_REMOVED(String recurrence) {
        switch (recurrence) {
            case "DAILY": return 1;
            case "WEEKLY": return 2;
            case "BIWEEKLY": return 3;
            case "MONTHLY": return 4;
            default: return 0;
        }
    }
    
    private void setupAttendees(View view) {
        if (chipGroupAttendees == null) return;
        
        // Load project members
        loadProjectMembers();
        
        View btnAddAttendees = view.findViewById(R.id.btnAddAttendees);
        if (btnAddAttendees != null) {
            btnAddAttendees.setOnClickListener(v -> {
                showMemberSelectionBottomSheet();
            });
        }
    }
    
    private void loadProjectMembers() {
        if (projectId == null || isLoadingMembers) return;
        
        isLoadingMembers = true;
        if (progressBarLoading != null) {
            progressBarLoading.setVisibility(View.VISIBLE);
        }
        
        MemberApiService memberApiService = ApiClient.get(App.authManager).create(MemberApiService.class);
        memberApiService.getMembers(projectId).enqueue(new Callback<List<MemberDTO>>() {
            @Override
            public void onResponse(Call<List<MemberDTO>> call, Response<List<MemberDTO>> response) {
                isLoadingMembers = false;
                if (progressBarLoading != null) {
                    progressBarLoading.setVisibility(View.GONE);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    List<MemberDTO> members = response.body();
                    updateAttendeesChips(members);
                }
            }
            
            @Override
            public void onFailure(Call<List<MemberDTO>> call, Throwable t) {
                isLoadingMembers = false;
                if (progressBarLoading != null) {
                    progressBarLoading.setVisibility(View.GONE);
                }
            }
        });
    }
    
    private void updateAttendeesChips(List<MemberDTO> allMembers) {
        if (chipGroupAttendees == null) return;
        
        chipGroupAttendees.removeAllViews();
        
        for (String attendeeId : selectedAttendeeIds) {
            MemberDTO member = findMemberById(allMembers, attendeeId);
            if (member != null && member.getUser() != null) {
                addAttendeeChip(member.getUser().getName(), attendeeId);
            }
        }
    }
    
    private MemberDTO findMemberById(List<MemberDTO> members, String userId) {
        for (MemberDTO member : members) {
            if (member.getUserId().equals(userId)) {
                return member;
            }
        }
        return null;
    }
    
    private void addAttendeeChip(String name, String userId) {
        Chip chip = new Chip(getContext());
        chip.setText(name);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroupAttendees.removeView(chip);
            selectedAttendeeIds.remove(userId);
        });
        chipGroupAttendees.addView(chip);
    }
    
    private void showMemberSelectionBottomSheet() {
        // TODO: Load members and show selection
        Toast.makeText(getContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
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
    
    // ✅ REMOVED: setupRecurrence() - Always NONE by default
    
    private void setupButtons(View view) {
        View btnClose = view.findViewById(R.id.btnCloseDialog);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }
        
        View btnCancel = view.findViewById(R.id.btnCancel);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dismiss());
        }
        
        // Change button text to "Update"
        View btnCreate = view.findViewById(R.id.btnCreateEvent);
        if (btnCreate != null && btnCreate instanceof Button) {
            ((Button) btnCreate).setText("Cập nhật");
            btnCreate.setOnClickListener(v -> {
                if (validateInput()) {
                    updateEvent();
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
    
    private void updateEvent() {
        UpdateEventRequest request = new UpdateEventRequest();
        request.setTitle(etEventTitle.getText().toString());
        
        if (etEventDescription.getText() != null && !etEventDescription.getText().toString().trim().isEmpty()) {
            request.setDescription(etEventDescription.getText().toString());
        }
        
        String durationStr = etDuration.getText().toString();
        int durationMinutes = durationStr.isEmpty() ? 60 : Integer.parseInt(durationStr);
        request.setDuration(durationMinutes);
        
        // Format startAt and endAt with ISO 8601 timezone (UTC)
        String startAtISO = formatToISO8601(etEventDate.getText().toString(), etEventTime.getText().toString());
        String endAtISO = formatToISO8601WithDuration(etEventDate.getText().toString(), etEventTime.getText().toString(), durationMinutes);
        request.setStartAt(startAtISO);
        request.setEndAt(endAtISO);
        
        request.setAttendeeIds(selectedAttendeeIds);
        
        if (listener != null) {
            listener.onEventUpdated(request);
        }
        
        dismiss();
    }
    
    private String formatToISO8601(String dateStr, String timeStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateStr + " " + timeStr);
            
            // Get timezone offset of device (e.g., "+07:00" for Vietnam GMT+7)
            TimeZone tz = TimeZone.getDefault();
            int offsetMillis = tz.getOffset(date.getTime());
            int offsetHours = offsetMillis / (1000 * 60 * 60);
            int offsetMinutes = (Math.abs(offsetMillis) / (1000 * 60)) % 60;
            String offsetStr = String.format(Locale.US, "%+03d:%02d", offsetHours, offsetMinutes);
            
            // Format as local time WITH timezone offset
            SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            
            return iso8601Format.format(date) + offsetStr;
        } catch (Exception e) {
            e.printStackTrace();
            SimpleDateFormat fallbackFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            TimeZone tz = TimeZone.getDefault();
            int offsetMillis = tz.getOffset(System.currentTimeMillis());
            int offsetHours = offsetMillis / (1000 * 60 * 60);
            int offsetMinutes = (Math.abs(offsetMillis) / (1000 * 60)) % 60;
            String offsetStr = String.format(Locale.US, "%+03d:%02d", offsetHours, offsetMinutes);
            return fallbackFormat.format(new Date()) + offsetStr;
        }
    }
    
    private String formatToISO8601WithDuration(String dateStr, String timeStr, int durationMinutes) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date startDate = inputFormat.parse(dateStr + " " + timeStr);
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.add(Calendar.MINUTE, durationMinutes);
            
            // Get timezone offset of device (e.g., "+07:00" for Vietnam GMT+7)
            TimeZone tz = TimeZone.getDefault();
            int offsetMillis = tz.getOffset(cal.getTimeInMillis());
            int offsetHours = offsetMillis / (1000 * 60 * 60);
            int offsetMinutes = (Math.abs(offsetMillis) / (1000 * 60)) % 60;
            String offsetStr = String.format(Locale.US, "%+03d:%02d", offsetHours, offsetMinutes);
            
            // Format as local time WITH timezone offset
            SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            
            return iso8601Format.format(cal.getTime()) + offsetStr;
        } catch (Exception e) {
            e.printStackTrace();
            return formatToISO8601(dateStr, timeStr);
        }
    }
}
