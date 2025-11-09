package com.example.tralalero.feature.home.ui.Home.calendar;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.TimeSlot;
import com.google.android.material.chip.Chip;

/**
 * Adapter for displaying time slot suggestions with availability scores
 */
public class TimeSlotAdapter extends ListAdapter<TimeSlot, TimeSlotAdapter.ViewHolder> {

    private OnTimeSlotClickListener listener;

    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(TimeSlot timeSlot);
    }

    public TimeSlotAdapter(OnTimeSlotClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<TimeSlot> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<TimeSlot>() {
            @Override
            public boolean areItemsTheSame(@NonNull TimeSlot oldItem, @NonNull TimeSlot newItem) {
                return oldItem.getStart().equals(newItem.getStart());
            }

            @Override
            public boolean areContentsTheSame(@NonNull TimeSlot oldItem, @NonNull TimeSlot newItem) {
                return oldItem.getScore() == newItem.getScore() &&
                       oldItem.getAvailableUsers().size() == newItem.getAvailableUsers().size();
            }
        };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvTimeRange;
        TextView tvAvailability;
        Chip chipScore;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
            tvAvailability = itemView.findViewById(R.id.tvAvailability);
            chipScore = itemView.findViewById(R.id.chipScore);
        }

        void bind(TimeSlot slot, OnTimeSlotClickListener listener) {
            // Use helper methods from TimeSlot model
            tvDate.setText(slot.getFormattedDate());
            tvTimeRange.setText(slot.getFormattedTimeRange());
            
            int availableCount = slot.getAvailableUsers() != null ? slot.getAvailableUsers().size() : 0;
            tvAvailability.setText(availableCount + " available");
            chipScore.setText(slot.getScoreBadge());

            // Color-code based on score
            int color;
            if (slot.getScore() >= 80) {
                color = Color.parseColor("#4CAF50"); // Green - Excellent
            } else if (slot.getScore() >= 60) {
                color = Color.parseColor("#FF9800"); // Orange - Good
            } else {
                color = Color.parseColor("#F44336"); // Red - Fair
            }
            chipScore.setChipBackgroundColor(ColorStateList.valueOf(color));

            // Handle click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTimeSlotClick(slot);
                }
            });
        }
    }
}
