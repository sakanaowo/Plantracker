package com.example.tralalero.util;

import android.content.ClipData;
import android.content.ClipDescription;
import android.view.DragEvent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.Task;

/**
 * Helper class for cross-board drag & drop functionality
 * Allows dragging tasks between different boards
 */
public class CrossBoardDragHelper {
    
    public static final String MIME_TYPE_TASK = "application/x-task";
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_SOURCE_BOARD_ID = "source_board_id";
    
    /**
     * Start drag operation for a task
     */
    public static boolean startDrag(View itemView, Task task, String sourceBoardId) {
        // Create clip data with task info
        ClipData.Item item = new ClipData.Item(task.getId());
        ClipData dragData = new ClipData(
            task.getTitle(),
            new String[] { MIME_TYPE_TASK },
            item
        );
        
        // Store source board ID and task ID in clip data
        dragData.getDescription().getExtras().putString(EXTRA_TASK_ID, task.getId());
        dragData.getDescription().getExtras().putString(EXTRA_SOURCE_BOARD_ID, sourceBoardId);
        
        // Create drag shadow
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(itemView);
        
        // Start drag
        return itemView.startDragAndDrop(dragData, shadowBuilder, task, 0);
    }
    
    /**
     * Drag listener for board RecyclerViews
     */
    public static class BoardDragListener implements View.OnDragListener {
        private final String targetBoardId;
        private final OnTaskDroppedListener listener;
        private final RecyclerView recyclerView;
        
        public interface OnTaskDroppedListener {
            void onTaskDropped(String taskId, String sourceBoardId, String targetBoardId, int position);
        }
        
        public BoardDragListener(String targetBoardId, RecyclerView recyclerView, OnTaskDroppedListener listener) {
            this.targetBoardId = targetBoardId;
            this.recyclerView = recyclerView;
            this.listener = listener;
        }
        
        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // Check if we can accept this drag
                    return event.getClipDescription().hasMimeType(MIME_TYPE_TASK);
                    
                case DragEvent.ACTION_DRAG_ENTERED:
                    // Highlight drop target
                    v.setBackgroundResource(R.color.drag_target_highlight);
                    return true;
                    
                case DragEvent.ACTION_DRAG_EXITED:
                    // Remove highlight
                    v.setBackground(null);
                    return true;
                    
                case DragEvent.ACTION_DROP:
                    // Handle drop
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    String taskId = item.getText().toString();
                    String sourceBoardId = event.getClipDescription().getExtras()
                        .getString(EXTRA_SOURCE_BOARD_ID);
                    
                    // Calculate drop position
                    int position = calculateDropPosition(event.getX(), event.getY());
                    
                    // Notify listener
                    if (listener != null) {
                        listener.onTaskDropped(taskId, sourceBoardId, targetBoardId, position);
                    }
                    
                    // Remove highlight
                    v.setBackground(null);
                    return true;
                    
                case DragEvent.ACTION_DRAG_ENDED:
                    // Clean up
                    v.setBackground(null);
                    return true;
                    
                default:
                    return false;
            }
        }
        
        private int calculateDropPosition(float x, float y) {
            if (recyclerView == null) return 0;
            
            View childView = recyclerView.findChildViewUnder(x, y);
            if (childView != null) {
                return recyclerView.getChildAdapterPosition(childView);
            }
            
            // Drop at end if no child found
            return recyclerView.getAdapter() != null ? 
                recyclerView.getAdapter().getItemCount() : 0;
        }
    }
}
