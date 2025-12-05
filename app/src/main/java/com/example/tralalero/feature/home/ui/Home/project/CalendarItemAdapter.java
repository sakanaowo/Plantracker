package com.example.tralalero.feature.home.ui.Home.project;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.data.remote.dto.task.TaskDTO;
import com.example.tralalero.data.remote.dto.event.EventDTO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for combined task/event list in calendar view
 */
public class CalendarItemAdapter extends RecyclerView.Adapter<CalendarItemAdapter.ItemViewHolder> {
    
    private List<CalendarItem> items;
    private OnItemClickListener listener;
    
    public interface OnItemClickListener {
        void onTaskClick(TaskDTO task);
        void onEventClick(EventDTO event);
    }
    
    public CalendarItemAdapter(List<CalendarItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_task_event, parent, false);
        return new ItemViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        CalendarItem item = items.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    public void updateItems(List<CalendarItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
    
    class ItemViewHolder extends RecyclerView.ViewHolder {
        private View typeIndicator;
        private TextView tvTitle;
        private TextView tvType;
        private TextView tvInfo;
        
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            typeIndicator = itemView.findViewById(R.id.typeIndicator);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvType = itemView.findViewById(R.id.tvType);
            tvInfo = itemView.findViewById(R.id.tvInfo);
        }
        
        public void bind(CalendarItem item) {
            tvTitle.setText(item.getTitle());
            
            if (item.getType() == CalendarItem.Type.TASK) {
                // Task styling
                typeIndicator.setBackgroundColor(Color.parseColor("#2196F3"));
                tvType.setText("TASK");
                tvType.setBackgroundColor(Color.parseColor("#2196F3"));
                
                // Show board name
                String boardName = item.getTask().getBoards() != null ? 
                        item.getTask().getBoards().getName() : "Unknown Board";
                tvInfo.setText("Board: " + boardName);
                
                // Click handler
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onTaskClick(item.getTask());
                    }
                });
                
            } else {
                // Event styling
                typeIndicator.setBackgroundColor(Color.parseColor("#FF9800"));
                tvType.setText("EVENT");
                tvType.setBackgroundColor(Color.parseColor("#FF9800"));
                
                // Show event time
                String timeInfo = formatEventTime(item.getEvent().getStartAt(), item.getEvent().getEndAt());
                tvInfo.setText(timeInfo);
                
                // Click handler
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEventClick(item.getEvent());
                    }
                });
            }
        }
        
        private String formatEventTime(String startAt, String endAt) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
                
                Date start = inputFormat.parse(startAt);
                String startTime = timeFormat.format(start);
                
                if (endAt != null && !endAt.isEmpty()) {
                    Date end = inputFormat.parse(endAt);
                    String endTime = timeFormat.format(end);
                    return startTime + " - " + endTime;
                }
                
                return "Time: " + startTime;
            } catch (Exception e) {
                return "Event";
            }
        }
    }
}
