package com.example.tralalero.feature.home.ui.Home.project;

import android.graphics.Color;
import android.util.Log;
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
    private static final String TAG = "ColorPickerAdapter";

    private final List<String> colors;
    private String selectedColor;
    private OnColorSelectedListener listener;

    // Predefined colors matching backend palette (Tailwind CSS colors)
    // Must match LABEL_COLORS in backend: src/common/constants/labels.constant.ts
    private static final List<String> DEFAULT_COLORS = Arrays.asList(
            "#EF4444", // Red
            "#F97316", // Orange
            "#F59E0B", // Amber
            "#EAB308", // Yellow
            "#84CC16", // Lime
            "#22C55E", // Green
            "#10B981", // Emerald
            "#14B8A6", // Teal
            "#06B6D4", // Cyan
            "#0EA5E9", // Sky
            "#3B82F6", // Blue
            "#6366F1", // Indigo
            "#8B5CF6", // Violet
            "#A855F7", // Purple
            "#D946EF", // Fuchsia
            "#EC4899", // Pink
            "#F43F5E", // Rose
            "#6B7280"  // Gray
    );

    public interface OnColorSelectedListener {
        void onColorSelected(String color);
    }

    public ColorPickerAdapter(OnColorSelectedListener listener) {
        this.colors = new ArrayList<>(DEFAULT_COLORS);
        this.listener = listener;
        Log.d(TAG, "ColorPickerAdapter created with " + colors.size() + " colors");
    }

    public void setSelectedColor(String color) {
        Log.d(TAG, "setSelectedColor: " + color);
        this.selectedColor = color;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_color_picker, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        String color = colors.get(position);
        Log.d(TAG, "onBindViewHolder position=" + position + ", color=" + color + ", isSelected=" + color.equals(selectedColor));
        holder.bind(color, color.equals(selectedColor));
    }

    @Override
    public int getItemCount() {
        int count = colors.size();
        Log.d(TAG, "getItemCount: " + count);
        return count;
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
            Log.d(TAG, "bind: color=" + color + ", isSelected=" + isSelected);
            
            // Set background color
            try {
                layoutColor.setBackgroundColor(Color.parseColor(color));
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse color: " + color, e);
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
                Log.d(TAG, "Color clicked: " + color);
                if (listener != null) {
                    listener.onColorSelected(color);
                }
                selectedColor = color;
                notifyDataSetChanged();
            });
        }
    }
}
