package com.example.tralalero.feature.home.ui.Home.project;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;

import java.util.Calendar;
import java.util.List;

/**
 * Adapter for calendar grid showing days with states:
 * - Normal: regular day
 * - Today: highlighted with border
 * - Selected: highlighted with background
 * - Outside Month: faded
 * - Has Events: shows dot indicator
 */
public class CalendarGridAdapter extends RecyclerView.Adapter<CalendarGridAdapter.DayViewHolder> {
    
    private List<CalendarDay> days;
    private Calendar selectedDate;
    private OnDayClickListener listener;
    
    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }
    
    public CalendarGridAdapter(List<CalendarDay> days, OnDayClickListener listener) {
        this.days = days;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        CalendarDay day = days.get(position);
        holder.bind(day, isSelected(day));
    }
    
    @Override
    public int getItemCount() {
        return days.size();
    }
    
    public void updateDays(List<CalendarDay> newDays) {
        this.days = newDays;
        notifyDataSetChanged();
    }
    
    public List<CalendarDay> getDays() {
        return days;
    }
    
    public void setSelectedDate(Calendar date) {
        this.selectedDate = date;
        notifyDataSetChanged();
    }
    
    private boolean isSelected(CalendarDay day) {
        if (selectedDate == null || !day.isCurrentMonth()) {
            return false;
        }
        return day.getDayOfMonth() == selectedDate.get(Calendar.DAY_OF_MONTH);
    }
    
    class DayViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDayNumber;
        private View dotIndicator;
        private View containerDay;
        
        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            dotIndicator = itemView.findViewById(R.id.dotIndicator);
            containerDay = itemView.findViewById(R.id.containerDay);
        }
        
        public void bind(CalendarDay day, boolean isSelected) {
            tvDayNumber.setText(String.valueOf(day.getDayOfMonth()));
            
            // Reset state
            tvDayNumber.setTypeface(null, Typeface.NORMAL);
            containerDay.setBackgroundResource(0);
            tvDayNumber.setTextColor(Color.BLACK);
            
            // Outside month: faded
            if (day.isOutsideMonth()) {
                tvDayNumber.setAlpha(0.3f);
                dotIndicator.setVisibility(View.GONE);
                containerDay.setOnClickListener(null);
                return;
            } else {
                tvDayNumber.setAlpha(1.0f);
            }
            
            // Today: highlighted border
            if (day.isToday()) {
                containerDay.setBackgroundResource(R.drawable.bg_calendar_today);
                tvDayNumber.setTypeface(null, Typeface.BOLD);
                tvDayNumber.setTextColor(Color.parseColor("#2196F3"));
            }
            
            // Selected: highlighted background
            if (isSelected) {
                containerDay.setBackgroundResource(R.drawable.bg_calendar_selected);
                tvDayNumber.setTextColor(Color.WHITE);
                tvDayNumber.setTypeface(null, Typeface.BOLD);
            }
            
            // Event indicator (dot)
            if (day.hasEvents()) {
                dotIndicator.setVisibility(View.VISIBLE);
            } else {
                dotIndicator.setVisibility(View.GONE);
            }
            
            // Click listener
            containerDay.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDayClick(day);
                }
            });
        }
    }
}
