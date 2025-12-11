package com.example.tralalero.feature.home.ui.Home.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.data.dto.event.TimeSlotSuggestion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Adapter for displaying meeting time slot suggestions
 */
public class MeetingSuggestionsAdapter extends RecyclerView.Adapter<MeetingSuggestionsAdapter.SuggestionViewHolder> {
    
    private List<TimeSlotSuggestion> suggestions = new ArrayList<>();
    private OnSlotSelectedListener listener;
    private java.util.Map<String, String> userIdToNameMap = new java.util.HashMap<>();
    
    public interface OnSlotSelectedListener {
        void onSlotSelected(TimeSlotSuggestion slot);
    }
    
    public void setSuggestions(List<TimeSlotSuggestion> suggestions) {
        this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setOnSlotSelectedListener(OnSlotSelectedListener listener) {
        this.listener = listener;
    }
    
    /**
     * Set user ID to name mapping for displaying participant names
     */
    public void setUserNameMap(java.util.Map<String, String> userNameMap) {
        this.userIdToNameMap = userNameMap != null ? userNameMap : new java.util.HashMap<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_meeting_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        TimeSlotSuggestion slot = suggestions.get(position);
        holder.bind(slot);
    }
    
    @Override
    public int getItemCount() {
        return suggestions.size();
    }
    
    class SuggestionViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvDate;
        private TextView tvTime;
        private TextView tvAvailability;
        private TextView tvBusyParticipants;
        private Button btnSelectSlot;
        
        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvAvailability = itemView.findViewById(R.id.tvAvailability);
            tvBusyParticipants = itemView.findViewById(R.id.tvBusyParticipants);
            btnSelectSlot = itemView.findViewById(R.id.btnSelectSlot);
        }
        
        public void bind(TimeSlotSuggestion slot) {
            // Parse ISO 8601 dates
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            
            try {
                Date startDate = isoFormat.parse(slot.getStartTime());
                Date endDate = isoFormat.parse(slot.getEndTime());
                
                if (startDate != null && endDate != null) {
                    // Date: "Monday, Dec 9, 2025"
                    tvDate.setText(dateFormat.format(startDate));
                    
                    // Time: "10:00 AM - 10:30 AM"
                    String timeRange = timeFormat.format(startDate) + " - " + timeFormat.format(endDate);
                    tvTime.setText(timeRange);
                }
            } catch (ParseException e) {
                tvDate.setText("Invalid date");
                tvTime.setText("--:-- - --:--");
            }
            
            // Availability
            int availableCount = slot.getAvailableUserIds() != null ? slot.getAvailableUserIds().size() : 0;
            int busyCount = slot.getBusyUserIds() != null ? slot.getBusyUserIds().size() : 0;
            tvAvailability.setText(availableCount + " available, " + busyCount + " busy");
            
            // Busy participants - show names directly
            if (busyCount > 0) {
                tvBusyParticipants.setVisibility(View.VISIBLE);
                StringBuilder busyNames = new StringBuilder("Busy: ");
                List<String> busyUserIds = slot.getBusyUserIds();
                for (int i = 0; i < busyUserIds.size(); i++) {
                    busyNames.append(getUserDisplayName(busyUserIds.get(i)));
                    if (i < busyUserIds.size() - 1) {
                        busyNames.append(", ");
                    }
                }
                tvBusyParticipants.setText(busyNames.toString());
                tvBusyParticipants.setOnClickListener(v -> showParticipantDetailsDialog(slot));
            } else {
                tvBusyParticipants.setVisibility(View.GONE);
            }
            
            // Select button
            btnSelectSlot.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSlotSelected(slot);
                }
            });
        }
        
        /**
         * Show dialog with detailed participant availability
         */
        private void showParticipantDetailsDialog(TimeSlotSuggestion slot) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(itemView.getContext());
            builder.setTitle("Participant Availability");
            
            StringBuilder message = new StringBuilder();
            
            // Available participants
            if (slot.getAvailableUserIds() != null && !slot.getAvailableUserIds().isEmpty()) {
                message.append("✅ Available (").append(slot.getAvailableUserIds().size()).append("):\n");
                for (String userId : slot.getAvailableUserIds()) {
                    message.append("• ").append(getUserDisplayName(userId)).append("\n");
                }
                message.append("\n");
            }
            
            // Busy participants
            if (slot.getBusyUserIds() != null && !slot.getBusyUserIds().isEmpty()) {
                message.append("⛔ Busy (").append(slot.getBusyUserIds().size()).append("):\n");
                for (String userId : slot.getBusyUserIds()) {
                    message.append("• ").append(getUserDisplayName(userId)).append("\n");
                }
            }
            
            builder.setMessage(message.toString());
            builder.setPositiveButton("OK", null);
            builder.show();
        }
        
        /**
         * Get user display name from ID using the provided name map
         */
        private String getUserDisplayName(String userId) {
            // Try to get name from map
            if (userIdToNameMap.containsKey(userId)) {
                return userIdToNameMap.get(userId);
            }
            
            // Fallback: show truncated ID
            if (userId.length() > 8) {
                return userId.substring(0, 8) + "...";
            }
            return userId;
        }
    }
}
