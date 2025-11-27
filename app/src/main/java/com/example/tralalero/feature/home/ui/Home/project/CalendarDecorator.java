package com.example.tralalero.feature.home.ui.Home.project;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom view to draw colored dots on calendar dates
 * Orange dots for tasks, Green dots for events
 */
public class CalendarDecorator extends View {
    private static final String TAG = "CalendarDecorator";
    
    private Map<Integer, DateDecoration> decorations = new HashMap<>();
    private Paint taskPaint;
    private Paint eventPaint;
    private Paint bothPaint;
    
    private int cellWidth = 0;
    private int cellHeight = 0;
    private int startOffset = 0; // Offset for first day of month
    
    public CalendarDecorator(Context context) {
        super(context);
        init();
    }
    
    public CalendarDecorator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        // Orange paint for tasks
        taskPaint = new Paint();
        taskPaint.setColor(0xFFFF9800); // Orange
        taskPaint.setStyle(Paint.Style.FILL);
        taskPaint.setAntiAlias(true);
        
        // Green paint for events
        eventPaint = new Paint();
        eventPaint.setColor(0xFF4CAF50); // Green
        eventPaint.setStyle(Paint.Style.FILL);
        eventPaint.setAntiAlias(true);
        
        // Mixed paint for both
        bothPaint = new Paint();
        bothPaint.setColor(0xFFFF6F00); // Dark orange
        bothPaint.setStyle(Paint.Style.FILL);
        bothPaint.setAntiAlias(true);
    }
    
    public void setDecorations(Map<Integer, DateDecoration> decorations, int cellWidth, int cellHeight, int startOffset) {
        this.decorations = decorations;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.startOffset = startOffset;
        Log.d(TAG, "Set decorations: count=" + decorations.size() + " cellW=" + cellWidth + " cellH=" + cellHeight + " offset=" + startOffset);
        for (Integer day : decorations.keySet()) {
            Log.d(TAG, "  Day " + day + ": " + decorations.get(day).hasTask + "/" + decorations.get(day).hasEvent);
        }
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (cellWidth == 0 || cellHeight == 0) {
            return;
        }
        
        // CalendarView internal structure has padding and header
        // We need to adjust for the actual content area
        int headerHeight = cellHeight * 2; // Title row + weekday header
        int leftPadding = 0; // CalendarView typically has no left padding
        
        // Draw dots for each decorated date
        for (Map.Entry<Integer, DateDecoration> entry : decorations.entrySet()) {
            int day = entry.getKey();
            DateDecoration decoration = entry.getValue();
            
            // Calculate position (7 columns per week)
            // day-1 because day starts from 1, then add startOffset for first day position
            int position = (day - 1) + startOffset;
            int row = position / 7;
            int col = position % 7;
            
            Log.d(TAG, "Drawing day " + day + ": position=" + position + " row=" + row + " col=" + col);
            
            // Calculate center of cell (accounting for header)
            float centerX = leftPadding + col * cellWidth + cellWidth / 2f;
            float centerY = headerHeight + row * cellHeight + cellHeight * 0.8f; // Position dot near bottom
            
            float dotRadius = cellWidth * 0.08f; // Slightly larger dot for visibility
            
            // Draw based on type
            if (decoration.hasTask && decoration.hasEvent) {
                // Both - draw two dots side by side
                canvas.drawCircle(centerX - dotRadius * 1.5f, centerY, dotRadius, taskPaint);
                canvas.drawCircle(centerX + dotRadius * 1.5f, centerY, dotRadius, eventPaint);
            } else if (decoration.hasTask) {
                // Only task - orange dot
                canvas.drawCircle(centerX, centerY, dotRadius, taskPaint);
            } else if (decoration.hasEvent) {
                // Only event - green dot
                canvas.drawCircle(centerX, centerY, dotRadius, eventPaint);
            }
        }
    }
    
    /**
     * Decoration info for a specific date
     */
    public static class DateDecoration {
        public boolean hasTask;
        public boolean hasEvent;
        
        public DateDecoration(boolean hasTask, boolean hasEvent) {
            this.hasTask = hasTask;
            this.hasEvent = hasEvent;
        }
    }
}
