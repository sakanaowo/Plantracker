package com.example.tralalero.feature.home.ui.Home.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.ProjectEvent;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Dialog for displaying event details
 */
public class EventDetailDialog extends DialogFragment {
    private ProjectEvent event;
    
    private TextView tvDialogTitle;
    private TextView tvDialogDescription;
    private TextView tvDialogDate;
    private TextView tvDialogTime;
    private TextView tvDialogDuration;
    private TextView tvDialogType;
    private TextView tvDialogAttendees;
    private TextView tvDialogRecurrence;
    private TextView tvDialogMeetLink;
    private MaterialButton btnClose;
    
    public static EventDetailDialog newInstance(ProjectEvent event) {
        EventDetailDialog dialog = new EventDetailDialog();
        Bundle args = new Bundle();
        // TODO: Pass event as Parcelable or Serializable
        dialog.setArguments(args);
        dialog.event = event;
        return dialog;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_event_detail, container, false);
        
        initViews(view);
        displayEventDetails();
        setupButtons();
        
        return view;
    }
    
    private void initViews(View view) {
        tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        tvDialogDescription = view.findViewById(R.id.tvDialogDescription);
        tvDialogDate = view.findViewById(R.id.tvDialogDate);
        tvDialogTime = view.findViewById(R.id.tvDialogTime);
        tvDialogDuration = view.findViewById(R.id.tvDialogDuration);
        tvDialogType = view.findViewById(R.id.tvDialogType);
        tvDialogAttendees = view.findViewById(R.id.tvDialogAttendees);
        tvDialogRecurrence = view.findViewById(R.id.tvDialogRecurrence);
        tvDialogMeetLink = view.findViewById(R.id.tvDialogMeetLink);
        btnClose = view.findViewById(R.id.btnClose);
    }
    
    private void displayEventDetails() {
        if (event == null) return;
        
        // Title
        if (tvDialogTitle != null) {
            String icon = getEventIcon(event.getType());
            tvDialogTitle.setText(icon + " " + event.getTitle());
        }
        
        // Description
        if (tvDialogDescription != null) {
            if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                tvDialogDescription.setText(event.getDescription());
                tvDialogDescription.setVisibility(View.VISIBLE);
            } else {
                tvDialogDescription.setVisibility(View.GONE);
            }
        }
        
        // Date
        if (tvDialogDate != null && event.getDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            tvDialogDate.setText(dateFormat.format(event.getDate()));
        }
        
        // Time
        if (tvDialogTime != null) {
            tvDialogTime.setText(event.getTime());
        }
        
        // Duration
        if (tvDialogDuration != null) {
            tvDialogDuration.setText(event.getDuration() + " phút");
        }
        
        // Type
        if (tvDialogType != null) {
            tvDialogType.setText(getEventTypeLabel(event.getType()));
        }
        
        // Attendees
        if (tvDialogAttendees != null) {
            int count = event.getAttendeeIds() != null ? 
                event.getAttendeeIds().size() : event.getAttendeeCount();
            tvDialogAttendees.setText(count + " người tham gia");
        }
        
        // Recurrence
        if (tvDialogRecurrence != null) {
            String recurrence = getRecurrenceLabel(event.getRecurrence());
            tvDialogRecurrence.setText(recurrence);
        }
        
        // Google Meet Link
        if (tvDialogMeetLink != null) {
            if (event.getMeetLink() != null && !event.getMeetLink().isEmpty()) {
                tvDialogMeetLink.setText(event.getMeetLink());
                tvDialogMeetLink.setVisibility(View.VISIBLE);
            } else {
                tvDialogMeetLink.setVisibility(View.GONE);
            }
        }
    }
    
    private void setupButtons() {
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }
    }
    
    private String getEventIcon(String type) {
        if (type == null) return "📌";
        
        switch (type) {
            case "MEETING": return "📋";
            case "MILESTONE": return "🎯";
            default: return "📌";
        }
    }
    
    private String getEventTypeLabel(String type) {
        if (type == null) return "Khác";
        
        switch (type) {
            case "MEETING": return "Cuộc họp";
            case "MILESTONE": return "Cột mốc";
            default: return "Khác";
        }
    }
    
    private String getRecurrenceLabel(String recurrence) {
        if (recurrence == null || recurrence.equals("NONE")) {
            return "Không lặp lại";
        }
        
        switch (recurrence) {
            case "DAILY": return "Hàng ngày";
            case "WEEKLY": return "Hàng tuần";
            case "BIWEEKLY": return "Hai tuần một lần";
            case "MONTHLY": return "Hàng tháng";
            default: return "Không lặp lại";
        }
    }
}
