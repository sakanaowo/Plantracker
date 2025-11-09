package com.example.tralalero.feature.home.ui.Home.task;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tralalero.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Dialog for configuring calendar sync settings for a task
 * 
 * Features:
 * - Enable/disable calendar sync toggle
 * - Reminder time selection (15/30/60 minutes)
 * - Display last synced timestamp
 * - Save callback to update task
 */
public class CalendarSyncDialog extends DialogFragment {
    
    private static final String ARG_TASK_ID = "task_id";
    private static final String ARG_TASK_TITLE = "task_title";
    private static final String ARG_DUE_DATE = "due_date";
    private static final String ARG_SYNC_ENABLED = "sync_enabled";
    private static final String ARG_REMINDER_TIME = "reminder_time";
    private static final String ARG_LAST_SYNCED = "last_synced";
    
    private String taskId;
    private String taskTitle;
    private Date dueDate;
    private boolean calendarSyncEnabled;
    private int reminderTimeMinutes;
    private Date lastSyncedAt;
    
    // UI Components
    private SwitchMaterial switchCalendarSync;
    private RadioGroup rgReminderTime;
    private RadioButton rb15Minutes;
    private RadioButton rb30Minutes;
    private RadioButton rb60Minutes;
    private TextView tvLastSynced;
    private MaterialButton btnCancel;
    private MaterialButton btnSave;
    
    private OnSyncSettingsChangedListener listener;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
    
    /**
     * Listener interface for calendar sync settings changes
     */
    public interface OnSyncSettingsChangedListener {
        void onSyncSettingsChanged(String taskId, boolean enabled, int reminderMinutes);
    }
    
    /**
     * Create new instance of CalendarSyncDialog
     * 
     * @param taskId Task ID
     * @param taskTitle Task title for display
     * @param dueDate Task due date (required for sync)
     * @param syncEnabled Current sync enabled status
     * @param reminderMinutes Current reminder time in minutes
     * @param lastSyncedAt Last sync timestamp (null if never synced)
     * @return Dialog instance
     */
    public static CalendarSyncDialog newInstance(
            String taskId,
            String taskTitle,
            Date dueDate,
            boolean syncEnabled,
            int reminderMinutes,
            Date lastSyncedAt
    ) {
        CalendarSyncDialog dialog = new CalendarSyncDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TASK_ID, taskId);
        args.putString(ARG_TASK_TITLE, taskTitle);
        if (dueDate != null) {
            args.putLong(ARG_DUE_DATE, dueDate.getTime());
        }
        args.putBoolean(ARG_SYNC_ENABLED, syncEnabled);
        args.putInt(ARG_REMINDER_TIME, reminderMinutes);
        if (lastSyncedAt != null) {
            args.putLong(ARG_LAST_SYNCED, lastSyncedAt.getTime());
        }
        dialog.setArguments(args);
        return dialog;
    }
    
    public void setOnSyncSettingsChangedListener(OnSyncSettingsChangedListener listener) {
        this.listener = listener;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_calendar_sync, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Load arguments
        loadArguments();
        
        // Initialize views
        initializeViews(view);
        
        // Setup listeners
        setupListeners();
        
        // Load current settings
        loadCurrentSettings();
    }
    
    private void loadArguments() {
        Bundle args = getArguments();
        if (args == null) return;
        
        taskId = args.getString(ARG_TASK_ID);
        taskTitle = args.getString(ARG_TASK_TITLE);
        
        long dueDateTimestamp = args.getLong(ARG_DUE_DATE, -1);
        if (dueDateTimestamp != -1) {
            dueDate = new Date(dueDateTimestamp);
        }
        
        calendarSyncEnabled = args.getBoolean(ARG_SYNC_ENABLED, false);
        reminderTimeMinutes = args.getInt(ARG_REMINDER_TIME, 30); // Default 30 min
        
        long lastSyncedTimestamp = args.getLong(ARG_LAST_SYNCED, -1);
        if (lastSyncedTimestamp != -1) {
            lastSyncedAt = new Date(lastSyncedTimestamp);
        }
    }
    
    private void initializeViews(View view) {
        // Title
        TextView tvTaskTitle = view.findViewById(R.id.tvTaskTitle);
        if (tvTaskTitle != null && taskTitle != null) {
            tvTaskTitle.setText(taskTitle);
        }
        
        // Sync toggle
        switchCalendarSync = view.findViewById(R.id.switchCalendarSync);
        
        // Reminder time radio group
        rgReminderTime = view.findViewById(R.id.rgReminderTime);
        rb15Minutes = view.findViewById(R.id.rb15Minutes);
        rb30Minutes = view.findViewById(R.id.rb30Minutes);
        rb60Minutes = view.findViewById(R.id.rb60Minutes);
        
        // Last synced text
        tvLastSynced = view.findViewById(R.id.tvLastSynced);
        
        // Buttons
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSave = view.findViewById(R.id.btnSave);
    }
    
    private void setupListeners() {
        // Sync toggle listener
        switchCalendarSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Enable/disable reminder options based on sync toggle
            rgReminderTime.setEnabled(isChecked);
            rb15Minutes.setEnabled(isChecked);
            rb30Minutes.setEnabled(isChecked);
            rb60Minutes.setEnabled(isChecked);
            
            if (!isChecked) {
                // Show warning if disabling sync
                tvLastSynced.setText("Disabling sync will remove this task from your Google Calendar");
                tvLastSynced.setTextColor(getResources().getColor(android.R.color.holo_orange_dark, null));
            } else {
                updateLastSyncedText();
            }
        });
        
        // Cancel button
        btnCancel.setOnClickListener(v -> dismiss());
        
        // Save button
        btnSave.setOnClickListener(v -> saveSettings());
    }
    
    private void loadCurrentSettings() {
        // Check if task has due date (required for sync)
        if (dueDate == null) {
            // Disable sync if no due date
            switchCalendarSync.setEnabled(false);
            switchCalendarSync.setChecked(false);
            tvLastSynced.setText("⚠️ Task must have a due date to enable calendar sync");
            tvLastSynced.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
            
            // Disable all controls
            rgReminderTime.setEnabled(false);
            rb15Minutes.setEnabled(false);
            rb30Minutes.setEnabled(false);
            rb60Minutes.setEnabled(false);
            btnSave.setEnabled(false);
            return;
        }
        
        // Set sync toggle
        switchCalendarSync.setChecked(calendarSyncEnabled);
        
        // Set reminder time radio button
        switch (reminderTimeMinutes) {
            case 15:
                rb15Minutes.setChecked(true);
                break;
            case 60:
                rb60Minutes.setChecked(true);
                break;
            case 30:
            default:
                rb30Minutes.setChecked(true);
                break;
        }
        
        // Enable/disable reminder options based on sync status
        rgReminderTime.setEnabled(calendarSyncEnabled);
        rb15Minutes.setEnabled(calendarSyncEnabled);
        rb30Minutes.setEnabled(calendarSyncEnabled);
        rb60Minutes.setEnabled(calendarSyncEnabled);
        
        // Update last synced text
        updateLastSyncedText();
    }
    
    private void updateLastSyncedText() {
        if (lastSyncedAt != null) {
            tvLastSynced.setText("Last synced: " + dateFormat.format(lastSyncedAt));
            tvLastSynced.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        } else if (calendarSyncEnabled) {
            tvLastSynced.setText("Not yet synced. Will sync when you save.");
            tvLastSynced.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        } else {
            tvLastSynced.setText("Calendar sync is disabled");
            tvLastSynced.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        }
    }
    
    private void saveSettings() {
        // Get current values
        boolean enabled = switchCalendarSync.isChecked();
        int reminderMinutes = 30; // default
        
        // Get selected reminder time
        int checkedId = rgReminderTime.getCheckedRadioButtonId();
        if (checkedId == R.id.rb15Minutes) {
            reminderMinutes = 15;
        } else if (checkedId == R.id.rb30Minutes) {
            reminderMinutes = 30;
        } else if (checkedId == R.id.rb60Minutes) {
            reminderMinutes = 60;
        }
        
        // Callback to listener
        if (listener != null) {
            listener.onSyncSettingsChanged(taskId, enabled, reminderMinutes);
        }
        
        // Show success message
        String message = enabled ? 
            "Calendar sync enabled. Task will sync to Google Calendar." :
            "Calendar sync disabled. Task removed from Google Calendar.";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        
        // Dismiss dialog
        dismiss();
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Use Material dialog theme
        return new MaterialAlertDialogBuilder(requireContext())
                .create();
    }
}
