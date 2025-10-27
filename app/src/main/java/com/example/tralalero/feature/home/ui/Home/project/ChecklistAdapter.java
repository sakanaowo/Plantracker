package com.example.tralalero.feature.home.ui.Home.project;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.ChecklistItem;

import java.util.ArrayList;
import java.util.List;

public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ChecklistViewHolder> {

    private List<ChecklistItem> checklistItems = new ArrayList<>();
    private OnChecklistItemListener listener;

    public interface OnChecklistItemListener {
        void onCheckboxChanged(ChecklistItem item, boolean isChecked);
        void onDeleteClick(ChecklistItem item);
        void onItemClick(ChecklistItem item);
    }

    public ChecklistAdapter(OnChecklistItemListener listener) {
        this.listener = listener;
    }

    public void setChecklistItems(List<ChecklistItem> items) {
        this.checklistItems = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChecklistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checklist, parent, false);
        return new ChecklistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChecklistViewHolder holder, int position) {
        ChecklistItem item = checklistItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return checklistItems.size();
    }

    class ChecklistViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbChecklistItem;
        private TextView tvChecklistContent;
        private ImageButton btnDeleteChecklistItem;

        public ChecklistViewHolder(@NonNull View itemView) {
            super(itemView);
            cbChecklistItem = itemView.findViewById(R.id.cbChecklistItem);
            tvChecklistContent = itemView.findViewById(R.id.tvChecklistContent);
            btnDeleteChecklistItem = itemView.findViewById(R.id.btnDeleteChecklistItem);
        }

        public void bind(ChecklistItem item) {
            // Set content
            tvChecklistContent.setText(item.getContent());
            
            // Set checkbox state
            cbChecklistItem.setOnCheckedChangeListener(null); // Remove listener to prevent triggering
            cbChecklistItem.setChecked(item.isDone());
            
            // Apply strikethrough if done
            if (item.isDone()) {
                tvChecklistContent.setPaintFlags(tvChecklistContent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvChecklistContent.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
            } else {
                tvChecklistContent.setPaintFlags(tvChecklistContent.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvChecklistContent.setTextColor(itemView.getContext().getColor(android.R.color.black));
            }

            // Checkbox listener
            cbChecklistItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onCheckboxChanged(item, isChecked);
                }
            });

            // Delete button listener
            btnDeleteChecklistItem.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(item);
                    }
                }
            });

            // Item click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
