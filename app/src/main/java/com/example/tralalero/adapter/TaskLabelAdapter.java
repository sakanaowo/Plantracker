package com.example.tralalero.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.Label;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class TaskLabelAdapter extends RecyclerView.Adapter<TaskLabelAdapter.ViewHolder> {
    
    private List<Label> labels = new ArrayList<>();

    public void setLabels(List<Label> labels) {
        this.labels = labels != null ? labels : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_label_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Label label = labels.get(position);
        holder.bind(label);
    }

    @Override
    public int getItemCount() {
        return labels.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        Chip chipLabel;

        ViewHolder(View itemView) {
            super(itemView);
            chipLabel = (Chip) itemView;
        }

        void bind(Label label) {
            chipLabel.setText(label.getName());
            
            // Set chip color from label color
            try {
                int color = Color.parseColor(label.getColor());
                chipLabel.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(color));
                
                // Calculate text color based on background brightness
                int textColor = isColorDark(color) ? Color.WHITE : Color.BLACK;
                chipLabel.setTextColor(textColor);
            } catch (Exception e) {
                chipLabel.setChipBackgroundColorResource(android.R.color.holo_blue_light);
                chipLabel.setTextColor(Color.WHITE);
            }
        }
        
        private boolean isColorDark(int color) {
            // Calculate luminance
            double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
            return darkness >= 0.5;
        }
    }
}
