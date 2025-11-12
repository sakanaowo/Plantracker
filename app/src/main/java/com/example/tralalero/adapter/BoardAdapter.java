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

import java.util.ArrayList;
import java.util.List;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.BoardViewHolder> {
    private static final String TAG = "BoardAdapter";

    private List<Board> boards = new ArrayList<>();
    private OnBoardActionListener listener;

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
        holder.bind(board, listener);
    }

    @Override
    public int getItemCount() {
        return boards.size();
    }

    static class BoardViewHolder extends RecyclerView.ViewHolder {
        TextView tvBoardTitle;
        RecyclerView taskRecycler;
        LinearLayout btnAddCard;
        TaskAdapter taskAdapter;
        ItemTouchHelper itemTouchHelper; // ✅ ADD: Drag & drop support

        BoardViewHolder(View itemView) {
            super(itemView);
            tvBoardTitle = itemView.findViewById(R.id.tvBoardTitle);
            taskRecycler = itemView.findViewById(R.id.taskRecycler);
            btnAddCard = itemView.findViewById(R.id.btnAddCard);

            taskRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            taskAdapter = new TaskAdapter(new ArrayList<>()); // Will be replaced in bind()
            taskRecycler.setAdapter(taskAdapter);
        }

        void bind(Board board, OnBoardActionListener listener) {
            tvBoardTitle.setText(board.getName());

            if (listener != null) {
                // ✅ NEW: Create TaskAdapter with boardId for cross-board drag
                List<Task> tasks = listener.getTasksForBoard(board.getId());
                if (tasks != null) {
                    taskAdapter = new TaskAdapter(tasks, board.getId());
                    taskRecycler.setAdapter(taskAdapter);
                }

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
                        new com.example.tralalero.util.CrossBoardDragHelper.BoardDragListener(
                            board.getId(), 
                            taskRecycler,
                            (taskId, sourceBoardId, targetBoardId, position) -> {
                                Log.d(TAG, "Task dropped: taskId=" + taskId + 
                                    ", from=" + sourceBoardId + ", to=" + targetBoardId + 
                                    ", position=" + position);
                                ((OnCrossBoardDragListener) listener).onTaskDroppedOnBoard(
                                    taskId, sourceBoardId, targetBoardId, position
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
         * @param taskId ID of the task being moved
         * @param sourceBoardId ID of the board the task came from
         * @param targetBoardId ID of the board the task was dropped on
         * @param position Position in the target board where task was dropped
         */
        void onTaskDroppedOnBoard(String taskId, String sourceBoardId, String targetBoardId, int position);
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
