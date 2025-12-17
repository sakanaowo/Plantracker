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
        // Format: "taskId|sourceBoardId" in text
        String dragText = task.getId() + "|" + sourceBoardId;
        ClipData.Item item = new ClipData.Item(dragText);
        ClipData dragData = new ClipData(
            task.getTitle(),
            new String[] { MIME_TYPE_TASK },
            item
        );
        
        // Create drag shadow
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(itemView);
        
        // Start drag - pass task as local state
        return itemView.startDragAndDrop(dragData, shadowBuilder, task, 0);
    }
    
    /**
     * Drag listener for entire board cards (not just RecyclerView)
     */
    public static class BoardDragListener implements View.OnDragListener {
        private final String targetBoardId;
        private final OnTaskDroppedListener listener;
        private final RecyclerView recyclerView;
        private final View boardCard; // The entire board card view
        private android.graphics.drawable.Drawable originalBackground; // Store original background
        
        public interface OnTaskDroppedListener {
            void onTaskDropped(Task task, String sourceBoardId, String targetBoardId, int position);
        }
        
        public BoardDragListener(String targetBoardId, View boardCard, RecyclerView recyclerView, OnTaskDroppedListener listener) {
            this.targetBoardId = targetBoardId;
            this.boardCard = boardCard;
            this.recyclerView = recyclerView;
            this.listener = listener;
        }
        
        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // Save original background when drag starts
                    originalBackground = v.getBackground();
                    // Check if we can accept this drag
                    return event.getClipDescription().hasMimeType(MIME_TYPE_TASK);
                    
                case DragEvent.ACTION_DRAG_ENTERED:
                    // Highlight drop target with border and background
                    v.setBackgroundResource(R.drawable.drag_drop_highlight_border);
                    return true;
                    
                case DragEvent.ACTION_DRAG_EXITED:
                    // Restore original background
                    v.setBackground(originalBackground);
                    return true;
                    
                case DragEvent.ACTION_DRAG_LOCATION:
                    // Continuous feedback while dragging over
                    return true;
                    
                case DragEvent.ACTION_DROP:
                    // Handle drop
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    String dragText = item.getText().toString();
                    
                    // Parse "taskId|sourceBoardId"
                    String[] parts = dragText.split("\\|");
                    if (parts.length != 2) {
                        return false; // Invalid format
                    }
                    String sourceBoardId = parts[1];
                    
                    // Retrieve Task object from local state
                    Task task = (Task) event.getLocalState();
                    if (task == null) {
                        return false; // No task data
                    }
                    
                    // Calculate drop position
                    int position = calculateDropPosition(event.getX(), event.getY());
                    
                    // Notify listener with Task object
                    if (listener != null) {
                        listener.onTaskDropped(task, sourceBoardId, targetBoardId, position);
                    }
                    
                    // Restore original background
                    v.setBackground(originalBackground);
                    return true;
                    
                case DragEvent.ACTION_DRAG_ENDED:
                    // Restore original background on drag end
                    v.setBackground(originalBackground);
                    return true;
                    
                default:
                    return false;
            }
        }
        
        private int calculateDropPosition(float x, float y) {
            if (recyclerView == null) return 0;
            
            // Convert board card coordinates to RecyclerView coordinates
            int[] boardLocation = new int[2];
            int[] recyclerLocation = new int[2];
            boardCard.getLocationOnScreen(boardLocation);
            recyclerView.getLocationOnScreen(recyclerLocation);
            
            // Adjust x, y to be relative to RecyclerView
            float recyclerX = x + (boardLocation[0] - recyclerLocation[0]);
            float recyclerY = y + (boardLocation[1] - recyclerLocation[1]);
            
            View childView = recyclerView.findChildViewUnder(recyclerX, recyclerY);
            if (childView != null) {
                return recyclerView.getChildAdapterPosition(childView);
            }
            
            // Drop at end if no child found
            return recyclerView.getAdapter() != null ? 
                recyclerView.getAdapter().getItemCount() : 0;
        }
    }
}
