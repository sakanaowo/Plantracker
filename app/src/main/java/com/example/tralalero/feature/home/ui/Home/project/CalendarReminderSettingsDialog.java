package com.example.tralalero.feature.home.ui.Home.project;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.tralalero.R;

import java.util.ArrayList;
import java.util.List;

public class CalendarReminderSettingsDialog extends DialogFragment {
    private CheckBox cbReminder15Min;
    private CheckBox cbReminder1Hour;
    private CheckBox cbReminder1Day;
    private CheckBox cbReminder1Week;
    
    private List<Integer> currentReminders = new ArrayList<>();
    private OnSaveListener onSaveListener;
    
    public interface OnSaveListener {
        void onSave(List<Integer> reminderMinutes);
    }
    
    public void setCurrentReminders(List<Integer> reminders) {
        this.currentReminders = new ArrayList<>(reminders);
    }
    
    public void setOnSaveListener(OnSaveListener listener) {
        this.onSaveListener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
            .inflate(R.layout.dialog_calendar_reminder_settings, null);
        
        cbReminder15Min = view.findViewById(R.id.cbReminder15Min);
        cbReminder1Hour = view.findViewById(R.id.cbReminder1Hour);
        cbReminder1Day = view.findViewById(R.id.cbReminder1Day);
        cbReminder1Week = view.findViewById(R.id.cbReminder1Week);
        
        // Set current values
        cbReminder15Min.setChecked(currentReminders.contains(15));
        cbReminder1Hour.setChecked(currentReminders.contains(60));
        cbReminder1Day.setChecked(currentReminders.contains(1440));
        cbReminder1Week.setChecked(currentReminders.contains(10080));
        
        return new AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton("Lưu", (dialog, which) -> {
                List<Integer> newReminders = new ArrayList<>();
                if (cbReminder15Min.isChecked()) newReminders.add(15);
                if (cbReminder1Hour.isChecked()) newReminders.add(60);
                if (cbReminder1Day.isChecked()) newReminders.add(1440);
                if (cbReminder1Week.isChecked()) newReminders.add(10080);
                
                if (onSaveListener != null) {
                    onSaveListener.onSave(newReminders);
                }
            })
            .setNegativeButton("Hủy", null)
            .create();
    }
}
