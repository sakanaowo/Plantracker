package com.example.tralalero.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.tralalero.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Custom calendar view that displays event and task markers on specific dates
 */
public class EventCalendarView extends GridLayout {
    
    public interface OnDateSelectedListener {
        void onDateSelected(int year, int month, int day);
    }
    
    private Calendar currentMonth;
    private Set<Long> eventDates = new HashSet<>();
    private Set<Long> taskDates = new HashSet<>();
    private OnDateSelectedListener dateSelectedListener;
    private final String[] weekDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    
    public EventCalendarView(Context context) {
        super(context);
        init();
    }
    
    public EventCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public EventCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        currentMonth = Calendar.getInstance();
        setColumnCount(7);
        setRowCount(7); // Header + 6 weeks max
        buildCalendar();
    }
    
    /**
     * Set dates that have events
     */
    public void setEventDates(Set<Long> dates) {
        this.eventDates = dates != null ? dates : new HashSet<>();
        buildCalendar();
    }
    
    /**
     * Set dates that have tasks
     */
    public void setTaskDates(Set<Long> dates) {
        this.taskDates = dates != null ? dates : new HashSet<>();
        buildCalendar();
    }
    
    /**
     * Set listener for date selection
     */
    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.dateSelectedListener = listener;
    }
    
    /**
     * Set both event and task dates
     */
    public void setEventAndTaskDates(Set<Long> eventDates, Set<Long> taskDates) {
        this.eventDates = eventDates != null ? eventDates : new HashSet<>();
        this.taskDates = taskDates != null ? taskDates : new HashSet<>();
        android.util.Log.d("EventCalendarView", "setEventAndTaskDates called - Events: " + this.eventDates.size() + ", Tasks: " + this.taskDates.size());
        if (!this.eventDates.isEmpty()) {
            android.util.Log.d("EventCalendarView", "Event timestamps: " + this.eventDates);
        }
        buildCalendar();
        android.util.Log.d("EventCalendarView", "Calendar rebuilt with markers");
    }
    
    /**
     * Change to a specific month
     */
    public void setMonth(int year, int month) {
        currentMonth.set(Calendar.YEAR, year);
        currentMonth.set(Calendar.MONTH, month);
        buildCalendar();
    }
    
    /**
     * Get the currently displayed month name and year
     */
    public String getCurrentMonthYear() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        return sdf.format(currentMonth.getTime());
    }
    
    /**
     * Build the calendar grid
     */
    private void buildCalendar() {
        removeAllViews();
        
        // Add weekday headers
        for (String day : weekDays) {
            TextView header = new TextView(getContext());
            header.setText(day);
            header.setGravity(Gravity.CENTER);
            header.setTextColor(Color.parseColor("#666666"));
            header.setTextSize(12);
            
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(0);
            params.setMargins(4, 4, 4, 4);
            header.setLayoutParams(params);
            
            addView(header);
        }
        
        // Get first day of month and number of days
        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        int currentDay = 1;
        int row = 1;
        
        // Build day cells
        for (int i = 0; i < 42; i++) { // 6 weeks max
            if (i < firstDayOfWeek || currentDay > daysInMonth) {
                // Empty cell
                View empty = new View(getContext());
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 60;
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                params.rowSpec = GridLayout.spec(row);
                params.setMargins(4, 4, 4, 4);
                empty.setLayoutParams(params);
                addView(empty);
            } else {
                // Day cell with marker
                addView(createDayView(currentDay, row));
                currentDay++;
            }
            
            // Move to next row after 7 columns
            if ((i + 1) % 7 == 0) {
                row++;
            }
            
            // Stop if we've displayed all days
            if (currentDay > daysInMonth) {
                break;
            }
        }
    }
    
    /**
     * Create a day view with event/task markers
     */
    private View createDayView(int day, int row) {
        TextView dayView = new TextView(getContext());
        dayView.setText(String.valueOf(day));
        dayView.setGravity(Gravity.CENTER);
        dayView.setTextColor(Color.parseColor("#212121"));
        dayView.setTextSize(14);
        
        // Make clickable
        dayView.setClickable(true);
        dayView.setFocusable(true);
        dayView.setOnClickListener(v -> {
            if (dateSelectedListener != null) {
                int year = currentMonth.get(Calendar.YEAR);
                int month = currentMonth.get(Calendar.MONTH);
                dateSelectedListener.onDateSelected(year, month, day);
            }
        });
        
        // Get timestamp for this day (normalized to midnight)
        Calendar dayCal = (Calendar) currentMonth.clone();
        dayCal.set(Calendar.DAY_OF_MONTH, day);
        dayCal.set(Calendar.HOUR_OF_DAY, 0);
        dayCal.set(Calendar.MINUTE, 0);
        dayCal.set(Calendar.SECOND, 0);
        dayCal.set(Calendar.MILLISECOND, 0);
        long dayTimestamp = dayCal.getTimeInMillis();
        
        // Check if this day has events or tasks
        boolean hasEvent = eventDates.contains(dayTimestamp);
        boolean hasTask = taskDates.contains(dayTimestamp);
        
        if (hasEvent || hasTask) {
            android.util.Log.d("EventCalendarView", "Day " + day + " has markers - Event: " + hasEvent + ", Task: " + hasTask + " (timestamp: " + dayTimestamp + ")");
        }
        
        // Add marker drawable
        if (hasEvent && hasTask) {
            // Both event and task - show both dots
            Drawable eventDot = ContextCompat.getDrawable(getContext(), R.drawable.event_dot);
            Drawable taskDot = ContextCompat.getDrawable(getContext(), R.drawable.task_dot);
            
            // Combine both markers (this shows them side by side)
            // For simplicity, we'll just show the event dot (green) as primary
            dayView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, eventDot);
        } else if (hasEvent) {
            // Only event
            Drawable eventDot = ContextCompat.getDrawable(getContext(), R.drawable.event_dot);
            dayView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, eventDot);
        } else if (hasTask) {
            // Only task
            Drawable taskDot = ContextCompat.getDrawable(getContext(), R.drawable.task_dot);
            dayView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, taskDot);
        }
        
        // Set padding to ensure dot is visible below text
        dayView.setPadding(8, 8, 8, 16);
        
        // Today's date highlight
        Calendar today = Calendar.getInstance();
        if (currentMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            currentMonth.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
            day == today.get(Calendar.DAY_OF_MONTH)) {
            
            // Highlight today with a circle background
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(Color.parseColor("#E3F2FD"));
            bg.setStroke(2, Color.parseColor("#2196F3"));
            dayView.setBackground(bg);
        }
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 60;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.rowSpec = GridLayout.spec(row);
        params.setMargins(4, 4, 4, 4);
        dayView.setLayoutParams(params);
        
        return dayView;
    }
}
