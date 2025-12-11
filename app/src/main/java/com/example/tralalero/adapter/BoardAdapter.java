package com.example.tralalero.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.util.TaskItemTouchHelper;
import com.example.tralalero.util.CrossBoardDragHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.BoardViewHolder> {
    private static final String TAG = "BoardAdapter";

    private List<Board> boards = new ArrayList<>();
    private OnBoardActionListener listener;
    private Map<String, TaskAdapter> taskAdapterMap = new HashMap<>(); // ✅ Store adapters by boardId

    public interface OnBoardActionListener {
        void onAddCardClick(Board board);

        void onBoardMenuClick(Board board);

        void onTaskClick(Task task, Board board);

        List<Task> getTasksForBoard(String boardId);
    }

    public BoardAdapter(OnBoardActionListener listener) {
        this.listener = listener;
    }

    public void setBoards(List<Board> boards) {
        this.boards = boards;
        notifyDataSetChanged();
    }

    /**
     * ✅ Update tasks for a specific board without recreating adapter
     */
    public void updateTasksForBoard(String boardId, List<Task> tasks) {
        TaskAdapter adapter = taskAdapterMap.get(boardId);
        if (adapter != null) {
            adapter.updateTasks(tasks != null ? tasks : new ArrayList<>());
            Log.d(TAG, "✅ Updated " + (tasks != null ? tasks.size() : 0) + " tasks for board " + boardId);
        }
    }

    @NonNull
    @Override
    public BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.board_list_item, parent, false);
        return new BoardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BoardViewHolder holder, int position) {
        Board board = boards.get(position);
        holder.bind(board, listener, this); // ✅ Pass BoardAdapter reference
    }

    @Override
    public int getItemCount() {
        return boards.size();
    }

    static class BoardViewHolder extends RecyclerView.ViewHolder {
        TextView tvBoardTitle;
        RecyclerView taskRecycler;
        TextView tvEmptyState;
        LinearLayout btnAddCard;
        TaskAdapter taskAdapter;
        ItemTouchHelper itemTouchHelper; // ✅ ADD: Drag & drop support

        BoardViewHolder(View itemView) {
            super(itemView);
            tvBoardTitle = itemView.findViewById(R.id.tvBoardTitle);
            taskRecycler = itemView.findViewById(R.id.taskRecycler);
            tvEmptyState = itemView.findViewById(R.id.tvEmptyState);
            btnAddCard = itemView.findViewById(R.id.btnAddCard);

            taskRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            taskAdapter = new TaskAdapter(new ArrayList<>(), null, itemView.getContext());
            taskRecycler.setAdapter(taskAdapter);
        }

        void bind(Board board, OnBoardActionListener listener, BoardAdapter boardAdapter) {
            tvBoardTitle.setText(board.getName());

            if (listener != null) {
                // ✅ Get tasks for this board
                List<Task> tasks = listener.getTasksForBoard(board.getId());
                
                // ✅ Reuse existing TaskAdapter or create new one
                TaskAdapter adapter = boardAdapter.taskAdapterMap.get(board.getId());
                if (adapter == null) {
                    // Create new adapter for this board
                    adapter = new TaskAdapter(tasks != null ? tasks : new ArrayList<>(), board.getId(), itemView.getContext());
                    boardAdapter.taskAdapterMap.put(board.getId(), adapter);
                    taskRecycler.setAdapter(adapter);
                } else {
                    // Update existing adapter with new tasks
                    adapter.updateTasks(tasks != null ? tasks : new ArrayList<>());
                }
                taskAdapter = adapter;
                
                // Show/hide empty state
                updateEmptyState(tasks == null || tasks.isEmpty());

                taskAdapter.setOnTaskClickListener(task -> {
                    listener.onTaskClick(task, board);
                });

                // ✅ ADD: Setup drag & drop for tasks
                TaskItemTouchHelper.TaskItemTouchHelperListener dragListener = new TaskItemTouchHelper.TaskItemTouchHelperListener() {
                    @Override
                    public void onTaskMoved(int fromPosition, int toPosition) {
                        // Update adapter immediately for smooth animation
                        taskAdapter.moveItem(fromPosition, toPosition);
                    }

                    @Override
                    public void onTaskDropped(int position) {
                        // Calculate new position and update backend
                        Task task = taskAdapter.getTaskAt(position);
                        if (task != null && listener instanceof OnTaskPositionChangeListener) {
                            double newPosition = calculateNewPosition(position);
                            ((OnTaskPositionChangeListener) listener).onTaskPositionChanged(task, newPosition, board);
                        }
                    }
                    
                    private double calculateNewPosition(int position) {
                        List<Task> tasks = taskAdapter.getTasks();
                        int taskCount = tasks.size();
                        
                        if (taskCount <= 1) {
                            return 1000.0;
                        }
                        
                        // Moving to first position
                        if (position == 0) {
                            Task firstTask = tasks.get(0);
                            if (firstTask != null) {
                                return firstTask.getPosition() / 2.0;
                            }
                            return 500.0;
                        }
                        
                        // Moving to last position
                        if (position >= taskCount - 1) {
                            Task lastTask = tasks.get(taskCount - 1);
                            if (lastTask != null) {
                                return lastTask.getPosition() + 1024.0;
                            }
                            return position * 1000.0;
                        }
                        
                        // Moving to middle position
                        Task prevTask = tasks.get(position - 1);
                        Task nextTask = tasks.get(position + 1);
                        
                        if (prevTask != null && nextTask != null) {
                            return (prevTask.getPosition() + nextTask.getPosition()) / 2.0;
                        }
                        
                        return position * 1000.0;
                    }
                };
                
                // ✅ REMOVED: ItemTouchHelper for within-board drag (replaced with cross-board drag)
                // ✅ REMOVED: TaskAdapter.OnTaskMoveListener (arrow buttons removed)
                
                // ✅ NEW: Setup cross-board drag listener on RecyclerView
                if (listener instanceof OnCrossBoardDragListener) {
                    com.example.tralalero.util.CrossBoardDragHelper.BoardDragListener dragDropListener = 
                        new CrossBoardDragHelper.BoardDragListener(
                            board.getId(),
                            taskRecycler,
                            (task, sourceBoardId, targetBoardId, position) -> {
                                Log.d(TAG, "Task dropped: taskId=" + task.getId() + 
                                    ", from=" + sourceBoardId + ", to=" + targetBoardId + 
                                    ", position=" + position);
                                ((OnCrossBoardDragListener) listener).onTaskDroppedOnBoard(
                                    task, sourceBoardId, targetBoardId, position
                                );
                            }
                        );
                    taskRecycler.setOnDragListener(dragDropListener);
                }
                
                // Setup checkbox status change listener
                taskAdapter.setOnTaskStatusChangeListener(new TaskAdapter.OnTaskStatusChangeListener() {
                    @Override
                    public void onTaskStatusChanged(Task task, boolean isDone) {
                        if (listener instanceof OnTaskStatusChangeListener) {
                            ((OnTaskStatusChangeListener) listener).onTaskStatusChanged(task, isDone);
                        }
                    }
                });

                btnAddCard.setOnClickListener(v -> {
                    listener.onAddCardClick(board);
                });
            }
        }
        
        /**
         * Update empty state visibility based on task count
         */
        void updateEmptyState(boolean isEmpty) {
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            }
        }
    }

    public interface OnTaskPositionChangeListener {
        void onTaskPositionChanged(Task task, double newPosition, Board board);
    }

    // ✅ REMOVED: OnTaskBoardChangeListener (arrow buttons removed)
    
    /**
     * ✅ NEW: Interface for cross-board drag & drop
     */
    public interface OnCrossBoardDragListener {
        /**
         * Called when a task is dropped on a board
         * @param task The task being moved
         * @param sourceBoardId ID of the board the task came from
         * @param targetBoardId ID of the board the task was dropped on
         * @param position Position in the target board where task was dropped
         */
        void onTaskDroppedOnBoard(Task task, String sourceBoardId, String targetBoardId, int position);
    }
    
    public interface OnTaskStatusChangeListener {
        /**
         * Called when task checkbox is toggled
         * @param task The task whose status changed
         * @param isDone true if marked as done, false otherwise
         */
        void onTaskStatusChanged(Task task, boolean isDone);
    }
}
