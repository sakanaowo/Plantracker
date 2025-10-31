package com.example.tralalero.feature.project.labels;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tralalero.R;

public class ColorPaletteAdapter extends RecyclerView.Adapter<ColorPaletteAdapter.ViewHolder> {
    
    // 20 predefined colors from backend
    private static final String[] COLORS = {
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8",
        "#F7DC6F", "#BB8FCE", "#85C1E2", "#F8B4D9", "#52B788",
        "#FFD93D", "#6BCF7F", "#95E1D3", "#F38181", "#AA96DA",
        "#FCBAD3", "#A8D8EA", "#FFAAA5", "#FFD3B6", "#DCEDC1"
    };
    
    private int selectedPosition = 0;
    private OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(String color);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.listener = listener;
    }

    public String getSelectedColor() {
        return COLORS[selectedPosition];
    }

    public void setSelectedColor(String color) {
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i].equalsIgnoreCase(color)) {
                int oldPosition = selectedPosition;
                selectedPosition = i;
                notifyItemChanged(oldPosition);
                notifyItemChanged(selectedPosition);
                break;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_color, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String color = COLORS[position];
        
        try {
            int colorInt = Color.parseColor(color);
            holder.vColor.setBackgroundTintList(ColorStateList.valueOf(colorInt));
        } catch (Exception e) {
            holder.vColor.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        }
        
        // Show selection indicator
        holder.vSelection.setVisibility(
            position == selectedPosition ? View.VISIBLE : View.GONE
        );
        
        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onColorSelected(color);
            }
        });
    }

    @Override
    public int getItemCount() {
        return COLORS.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View vColor;
        ImageView vSelection;

        ViewHolder(View view) {
            super(view);
            vColor = view.findViewById(R.id.vColor);
            vSelection = view.findViewById(R.id.vSelection);
        }
    }
}
