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
import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.data.remote.api.MemberApiService;
import com.example.tralalero.data.remote.dto.member.MemberDTO;
import com.example.tralalero.domain.model.ProjectEvent;
import com.example.tralalero.domain.model.User;
import com.example.tralalero.feature.home.ui.Home.calendar.MemberSelectionBottomSheet;
import com.example.tralalero.network.ApiClient;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private RadioButton rbMeeting;
    private RadioButton rbMilestone;
    private ChipGroup chipGroupAttendees;
    // ✅ REMOVED: switchCreateMeet and cardGoogleMeet - Always create Google Meet by default
    // ✅ REMOVED: spinnerRecurrence - Always NONE by default
    private android.widget.ProgressBar progressBarLoading;
    
    private String projectId;
    private String currentUserId;
    private List<String> selectedAttendeeIds = new ArrayList<>();
    private List<User> selectedAttendees = new ArrayList<>();  // ✅ ADD: Track full User objects
    private OnEventCreatedListener listener;
    private boolean isLoadingMembers = false;
    
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
        
        // Get current user ID
        TokenManager tokenManager = new TokenManager(requireContext());
        currentUserId = tokenManager.getInternalUserId();
        
        // Auto-add self to attendees
        if (currentUserId != null && !selectedAttendeeIds.contains(currentUserId)) {
            selectedAttendeeIds.add(currentUserId);
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
        
        // ✅ Show chip for current user (auto-added)
        loadCurrentUserAndShowChip();
        
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
        // ✅ REMOVED: switchCreateMeet and cardGoogleMeet - Always create Google Meet by default
        // ✅ REMOVED: spinnerRecurrence findViewById - Always NONE by default
        progressBarLoading = view.findViewById(R.id.progressBarLoading);
        
        // ✅ SIMPLIFIED: No longer need meeting link and location fields
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
        // Prevent multiple concurrent requests
        if (isLoadingMembers) {
            Toast.makeText(getContext(), "Loading members, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading state
        isLoadingMembers = true;
        if (progressBarLoading != null) {
            progressBarLoading.setVisibility(View.VISIBLE);
        }
        
        // Load project members from API
        MemberApiService api = ApiClient.get(App.authManager).create(MemberApiService.class);
        api.getMembers(projectId).enqueue(new Callback<List<MemberDTO>>() {
            @Override
            public void onResponse(Call<List<MemberDTO>> call, 
                                 Response<List<MemberDTO>> response) {
                // Hide loading state
                isLoadingMembers = false;
                if (progressBarLoading != null) {
                    progressBarLoading.setVisibility(View.GONE);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = new ArrayList<>();
                    User currentUser = null;
                    
                    for (MemberDTO dto : response.body()) {
                        if (dto.getUser() != null) {
                            User user = new User(
                                dto.getUserId(),
                                dto.getUser().getName(),
                                dto.getUser().getEmail(),
                                dto.getUser().getAvatarUrl(),
                                null  // firebaseUid not in MemberDTO
                            );
                            
                            // ✅ Hide current user from selection list
                            if (!dto.getUserId().equals(currentUserId)) {
                                users.add(user);
                            } else {
                                currentUser = user;
                                // ✅ Ensure current user is in selected attendees
                                if (!selectedAttendees.stream().anyMatch(u -> u.getId().equals(currentUserId))) {
                                    selectedAttendees.add(user);
                                }
                            }
                        }
                    }
                    
                    final User finalCurrentUser = currentUser;

                    // ✅ FIX: Show member selection bottom sheet with pre-selected members
                    MemberSelectionBottomSheet sheet = MemberSelectionBottomSheet.newInstance(users, selectedAttendees);
                    sheet.setListener(selected -> {
                        // Clear existing chips
                        chipGroupAttendees.removeAllViews();
                        selectedAttendeeIds.clear();
                        selectedAttendees.clear();  // ✅ Clear full User list
                        
                        // ✅ Always add current user first (auto-included)
                        if (currentUserId != null && finalCurrentUser != null) {
                            selectedAttendeeIds.add(currentUserId);
                            selectedAttendees.add(finalCurrentUser);
                            addAttendeeChip(finalCurrentUser.getId(), finalCurrentUser.getName() + " (Bạn)");
                        }
                        
                        // Add chips for selected members
                        for (User u : selected) {
                            if (!u.getId().equals(currentUserId)) {  // Skip current user (already added)
                                addAttendeeChip(u.getId(), u.getName());
                                selectedAttendees.add(u);  // ✅ Track full User object
                            }
                        }
                    });
                    sheet.show(getParentFragmentManager(), "select_members");
                } else {
                    Toast.makeText(getContext(), "Failed to load members", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MemberDTO>> call, Throwable t) {
                // Hide loading state
                isLoadingMembers = false;
                if (progressBarLoading != null) {
                    progressBarLoading.setVisibility(View.GONE);
                }
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Load current user info and display chip (auto-added attendee)
     */
    private void loadCurrentUserAndShowChip() {
        if (currentUserId == null || projectId == null) return;
        
        MemberApiService api = ApiClient.get(App.authManager).create(MemberApiService.class);
        api.getMembers(projectId).enqueue(new Callback<List<MemberDTO>>() {
            @Override
            public void onResponse(Call<List<MemberDTO>> call, Response<List<MemberDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (MemberDTO dto : response.body()) {
                        if (currentUserId.equals(dto.getUserId()) && dto.getUser() != null) {
                            // Found current user - add chip
                            User currentUser = new User(
                                dto.getUserId(),
                                dto.getUser().getName(),
                                dto.getUser().getEmail(),
                                dto.getUser().getAvatarUrl(),
                                null
                            );
                            selectedAttendees.add(currentUser);
                            addAttendeeChip(currentUser.getId(), currentUser.getName() + " (Bạn)");
                            break;
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(Call<List<MemberDTO>> call, Throwable t) {
                // Silently fail - chip won't show but user is still in attendee list
            }
        });
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
        // Build event object và call API
        ProjectEvent event = new ProjectEvent();
        event.setTitle(etEventTitle.getText().toString());
        event.setDate(parseDate(etEventDate.getText().toString()));
        event.setTime(etEventTime.getText().toString());
        
        String durationStr = etDuration.getText().toString();
        int durationMinutes = durationStr.isEmpty() ? 60 : Integer.parseInt(durationStr);
        event.setDuration(durationMinutes);
        
        if (etEventDescription.getText() != null && !etEventDescription.getText().toString().trim().isEmpty()) {
            event.setDescription(etEventDescription.getText().toString());
        }
        
        // ✅ SIMPLIFIED: Always create MEETING type with Google Meet enabled by default
        event.setType("MEETING");
        event.setCreateGoogleMeet(true); // Always create Google Meet
        
        event.setAttendeeIds(selectedAttendeeIds);
        event.setRecurrence("NONE"); // ✅ Always NONE - no recurrence support
        event.setProjectId(projectId);
        
        // ✅ NEW: No need to set startAt/endAt - backend will calculate from date + time + duration
        // FE sends: date="2025-12-07", time="19:18", duration=60
        // BE creates: startAt="2025-12-07T19:18:00+07:00", endAt="2025-12-07T20:18:00+07:00"
        
        android.util.Log.d("CreateEventDialog", "📅 Creating event:");
        android.util.Log.d("CreateEventDialog", "  Date: " + etEventDate.getText().toString());
        android.util.Log.d("CreateEventDialog", "  Time: " + etEventTime.getText().toString());
        android.util.Log.d("CreateEventDialog", "  Duration: " + durationMinutes + " minutes");
        
        // Call API to create event
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
    
    // ✅ REMOVED: getSelectedRecurrence() - Always NONE by default
}
