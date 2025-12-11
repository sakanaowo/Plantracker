package com.example.tralalero.util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ItemTouchHelper for drag & drop tasks within a board
 * Enables reordering tasks by dragging them up/down
 */
public class TaskItemTouchHelper extends ItemTouchHelper.Callback {

    private final TaskItemTouchHelperListener listener;

    public interface TaskItemTouchHelperListener {
        /**
         * Called when task is moved from one position to another
         * @param fromPosition Original position
         * @param toPosition Target position
         */
        void onTaskMoved(int fromPosition, int toPosition);
        
        /**
         * Called when drag is completed and item is dropped
         * @param position Final position of the task
         */
        void onTaskDropped(int position);
    }

    public TaskItemTouchHelper(TaskItemTouchHelperListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        // Enable drag on long press
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        // Disable swipe (we have swipe left/right for board change)
        return false;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // Allow vertical drag only (up/down within same board)
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = 0; // No swipe
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, 
                         @NonNull RecyclerView.ViewHolder viewHolder, 
                         @NonNull RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        
        if (listener != null) {
            listener.onTaskMoved(fromPosition, toPosition);
        }
        
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Not used - swipe disabled
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        
        // Called when drag is finished
        int position = viewHolder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION && listener != null) {
            listener.onTaskDropped(position);
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE && viewHolder != null) {
            // Visual feedback when dragging starts
            viewHolder.itemView.setAlpha(0.7f);
            viewHolder.itemView.setScaleX(1.05f);
            viewHolder.itemView.setScaleY(1.05f);
        }

        super.onSelectedChanged(viewHolder, actionState);
    }
}
