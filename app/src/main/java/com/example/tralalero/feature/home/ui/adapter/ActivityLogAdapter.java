package com.example.tralalero.feature.home.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.tralalero.R;
import com.example.tralalero.domain.model.ActivityLog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ActivityLogAdapter extends RecyclerView.Adapter<ActivityLogAdapter.ActivityLogViewHolder> {

    private List<ActivityLog> activityLogs = new ArrayList<>();
    private Context context;

    public ActivityLogAdapter(Context context) {
        this.context = context;
    }

    public void setActivityLogs(List<ActivityLog> activityLogs) {
        this.activityLogs = activityLogs;
        notifyDataSetChanged();
    }

    public void addActivityLogs(List<ActivityLog> newLogs) {
        int startPosition = this.activityLogs.size();
        this.activityLogs.addAll(newLogs);
        notifyItemRangeInserted(startPosition, newLogs.size());
    }

    @NonNull
    @Override
    public ActivityLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_log, parent, false);
        return new ActivityLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityLogViewHolder holder, int position) {
        ActivityLog log = activityLogs.get(position);
        holder.bind(log);
    }

    @Override
    public int getItemCount() {
        return activityLogs.size();
    }

    class ActivityLogViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivUserAvatar;
        private TextView tvUserInitials;
        private TextView tvActivityMessage;
        private TextView tvActivityTime;

        public ActivityLogViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserInitials = itemView.findViewById(R.id.tvUserInitials);
            tvActivityMessage = itemView.findViewById(R.id.tvActivityMessage);
            tvActivityTime = itemView.findViewById(R.id.tvActivityTime);
        }

        public void bind(ActivityLog log) {
            if (log == null) {
                return;
            }
            
            // Set activity message
            String message = log.getFormattedMessage();
            tvActivityMessage.setText(message != null ? message : "Activity");

            // Set time
            String createdAt = log.getCreatedAt();
            tvActivityTime.setText(formatTimeAgo(createdAt));

            // Load user avatar or show initials
            if (log.getUserAvatar() != null && !log.getUserAvatar().isEmpty()) {
                ivUserAvatar.setVisibility(View.VISIBLE);
                tvUserInitials.setVisibility(View.GONE);
                Glide.with(context)
                        .load(log.getUserAvatar())
                        .transform(new CircleCrop())
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .into(ivUserAvatar);
            } else {
                ivUserAvatar.setVisibility(View.GONE);
                tvUserInitials.setVisibility(View.VISIBLE);
                tvUserInitials.setText(log.getUserInitials());
            }
        }

        private String formatTimeAgo(String isoTimestamp) {
            if (isoTimestamp == null || isoTimestamp.isEmpty()) {
                return "Unknown time";
            }
            
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = isoFormat.parse(isoTimestamp);

                if (date == null) {
                    return isoTimestamp;
                }

                long now = System.currentTimeMillis();
                long diffMs = now - date.getTime();
                long diffSec = diffMs / 1000;
                long diffMin = diffSec / 60;
                long diffHour = diffMin / 60;
                long diffDay = diffHour / 24;

                if (diffSec < 60) {
                    return "just now";
                } else if (diffMin < 60) {
                    return diffMin + " minute" + (diffMin > 1 ? "s" : "") + " ago";
                } else if (diffHour < 24) {
                    return diffHour + " hour" + (diffHour > 1 ? "s" : "") + " ago";
                } else if (diffDay < 7) {
                    return diffDay + " day" + (diffDay > 1 ? "s" : "") + " ago";
                } else {
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d 'at' h:mm a", Locale.US);
                    return displayFormat.format(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                // Return a fallback message instead of the raw timestamp
                return "Recently";
            } catch (Exception e) {
                e.printStackTrace();
                return "Unknown time";
            }
        }
    }
}
