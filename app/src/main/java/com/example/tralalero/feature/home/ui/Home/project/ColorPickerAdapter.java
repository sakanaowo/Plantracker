package com.example.tralalero.feature.home.ui.Home.project;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ColorViewHolder> {

    private final List<String> colors;
    private String selectedColor;
    private OnColorSelectedListener listener;

    // Predefined colors matching Trello's color palette
    private static final List<String> DEFAULT_COLORS = Arrays.asList(
            "#61BD4F", "#F2D600", "#FF9F1A", "#EB5A46", "#C377E0",
            "#00C2E0", "#51E898", "#FF78CB", "#00B8D9", "#C9372C",
            "#4C9AFF", "#79F2C0", "#FFAB00", "#FF5630", "#6554C0",
            "#B3D4FF", "#79E2F2", "#FFF0B3", "#FFE2BD", "#DFE1E6"
    );

    public interface OnColorSelectedListener {
        void onColorSelected(String color);
    }

    public ColorPickerAdapter(OnColorSelectedListener listener) {
        this.colors = new ArrayList<>(DEFAULT_COLORS);
        this.listener = listener;
    }

    public void setSelectedColor(String color) {
        this.selectedColor = color;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_color_picker, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        String color = colors.get(position);
        holder.bind(color, color.equals(selectedColor));
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    class ColorViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private FrameLayout layoutColor;
        private ImageView ivCheck;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            layoutColor = itemView.findViewById(R.id.layoutColor);
            ivCheck = itemView.findViewById(R.id.ivCheck);
        }

        public void bind(String color, boolean isSelected) {
            // Set background color
            try {
                layoutColor.setBackgroundColor(Color.parseColor(color));
            } catch (Exception e) {
                layoutColor.setBackgroundColor(Color.GRAY);
            }

            // Show/hide checkmark
            ivCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            // Set stroke for selected item
            if (isSelected) {
                cardView.setStrokeColor(Color.parseColor("#2196F3"));
                cardView.setStrokeWidth(6);
            } else {
                cardView.setStrokeColor(Color.TRANSPARENT);
                cardView.setStrokeWidth(0);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onColorSelected(color);
                }
                selectedColor = color;
                notifyDataSetChanged();
            });
        }
    }
}
