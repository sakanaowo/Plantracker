package com.example.tralalero.feature.home.ui.Home.project;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.Label;

import java.util.ArrayList;
import java.util.List;

public class LabelSelectionAdapter extends RecyclerView.Adapter<LabelSelectionAdapter.LabelViewHolder> {

    private List<Label> labels = new ArrayList<>();
    private List<String> selectedLabelIds = new ArrayList<>();
    private OnLabelActionListener listener;

    public interface OnLabelActionListener {
        void onLabelChecked(Label label, boolean isChecked);
        void onEditLabel(Label label);
    }

    public LabelSelectionAdapter(OnLabelActionListener listener) {
        this.listener = listener;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels != null ? labels : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSelectedLabels(List<String> selectedIds) {
        this.selectedLabelIds = selectedIds != null ? selectedIds : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LabelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_label_selection, parent, false);
        return new LabelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LabelViewHolder holder, int position) {
        Label label = labels.get(position);
        boolean isSelected = selectedLabelIds.contains(label.getId());
        holder.bind(label, isSelected);
    }

    @Override
    public int getItemCount() {
        return labels.size();
    }

    class LabelViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbLabel;
        private View viewLabelColor;
        private TextView tvLabelName;
        private ImageButton btnEditLabel;

        public LabelViewHolder(@NonNull View itemView) {
            super(itemView);
            cbLabel = itemView.findViewById(R.id.cbLabel);
            viewLabelColor = itemView.findViewById(R.id.viewLabelColor);
            tvLabelName = itemView.findViewById(R.id.tvLabelName);
            btnEditLabel = itemView.findViewById(R.id.btnEditLabel);
        }

        public void bind(Label label, boolean isSelected) {
            // Set label name
            tvLabelName.setText(label.getName() != null && !label.getName().isEmpty() 
                ? label.getName() 
                : "Unnamed Label");

            // Set color
            try {
                viewLabelColor.setBackgroundColor(Color.parseColor(label.getColor()));
            } catch (Exception e) {
                viewLabelColor.setBackgroundColor(Color.GRAY);
            }

            // Set checkbox state
            cbLabel.setOnCheckedChangeListener(null);
            cbLabel.setChecked(isSelected);

            // Checkbox listener
            cbLabel.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onLabelChecked(label, isChecked);
                }
                // Note: selectedLabelIds will be updated via setSelectedLabels() 
                // after backend sync, no need for optimistic update here
            });

            // Edit button listener
            btnEditLabel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditLabel(label);
                }
            });

            // Item click toggles checkbox
            itemView.setOnClickListener(v -> cbLabel.setChecked(!cbLabel.isChecked()));
        }
    }
}
