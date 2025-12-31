package com.example.tralalero.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;

/**
 * Custom RecyclerView that supports maxHeight attribute
 * Useful for board columns that need to scroll when content exceeds max height
 */
public class MaxHeightRecyclerView extends RecyclerView {
    private int maxHeight = -1; // -1 means no limit

    public MaxHeightRecyclerView(@NonNull Context context) {
        super(context);
    }

    public MaxHeightRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MaxHeightRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightRecyclerView);
            maxHeight = a.getDimensionPixelSize(R.styleable.MaxHeightRecyclerView_maxHeight, -1);
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        if (maxHeight > 0) {
            // Limit height to maxHeight
            int measuredHeight = MeasureSpec.getSize(heightSpec);
            if (measuredHeight > maxHeight) {
                heightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
            }
        }
        super.onMeasure(widthSpec, heightSpec);
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        requestLayout();
    }
}
