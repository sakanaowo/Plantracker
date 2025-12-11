package com.example.tralalero.feature.project.labels;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tralalero.R;
import com.example.tralalero.data.remote.dto.label.LabelDTO;

import java.util.ArrayList;
import java.util.List;

public class LabelAdapter extends RecyclerView.Adapter<LabelAdapter.ViewHolder> {
    
    private List<LabelDTO> labels = new ArrayList<>();
    private OnLabelActionListener listener;

    public interface OnLabelActionListener {
        void onDeleteLabel(LabelDTO label);
    }

    public void setOnLabelActionListener(OnLabelActionListener listener) {
        this.listener = listener;
    }

    public void setLabels(List<LabelDTO> labels) {
        this.labels = labels != null ? labels : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_label, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LabelDTO label = labels.get(position);
        
        holder.tvName.setText(label.getName());
        
        // Set task count
        int count = label.getTaskCount() != null ? label.getTaskCount() : 0;
        holder.tvTaskCount.setText(count + (count == 1 ? " task" : " tasks"));
        
        // Set color indicator
        try {
            int color = Color.parseColor(label.getColor());
            holder.vColorIndicator.setBackgroundColor(color);
        } catch (Exception e) {
            holder.vColorIndicator.setBackgroundColor(Color.GRAY);
        }
        
        // Delete button
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteLabel(label);
            }
        });
    }

    @Override
    public int getItemCount() {
        return labels.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View vColorIndicator;
        TextView tvName, tvTaskCount;
        ImageButton btnDelete;

        ViewHolder(View view) {
            super(view);
            vColorIndicator = view.findViewById(R.id.vColorIndicator);
            tvName = view.findViewById(R.id.tvLabelName);
            tvTaskCount = view.findViewById(R.id.tvTaskCount);
            btnDelete = view.findViewById(R.id.btnDeleteLabel);
        }
    }
}
