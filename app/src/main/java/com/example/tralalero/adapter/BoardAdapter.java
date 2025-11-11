package com.example.tralalero.adapter;

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

    private List<Board> boards = new ArrayList<>();
    private java.util.Map<String, List<Task>> tasksPerBoard = new java.util.HashMap<>();
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

    public void setTasksPerBoard(java.util.Map<String, List<Task>> tasksPerBoard) {
        this.tasksPerBoard = tasksPerBoard;
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
        List<Task> tasks = tasksPerBoard.get(board.getId());
        holder.bind(board, tasks, listener);
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
            taskAdapter = new TaskAdapter(new ArrayList<>());
            taskRecycler.setAdapter(taskAdapter);
        }

        void bind(Board board, List<Task> tasks, OnBoardActionListener listener) {
            tvBoardTitle.setText(board.getName());

            if (listener != null) {
                if (tasks != null) {
                    taskAdapter.updateTasks(tasks);
                } else {
                    taskAdapter.updateTasks(new ArrayList<>());
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
                
                TaskItemTouchHelper touchHelper = new TaskItemTouchHelper(dragListener);
                if (itemTouchHelper != null) {
                    itemTouchHelper.attachToRecyclerView(null); // Detach old one
                }
                itemTouchHelper = new ItemTouchHelper(touchHelper);
                itemTouchHelper.attachToRecyclerView(taskRecycler);

                // Setup move left/right listeners
                taskAdapter.setOnTaskMoveListener(new TaskAdapter.OnTaskMoveListener() {
                    @Override
                    public void onMoveLeft(Task task, int position) {
                        // Move task to previous board (left)
                        if (listener instanceof OnTaskBoardChangeListener) {
                            ((OnTaskBoardChangeListener) listener).onMoveTaskToBoard(task, board, -1);
                        }
                    }

                    @Override
                    public void onMoveRight(Task task, int position) {
                        // Move task to next board (right)
                        if (listener instanceof OnTaskBoardChangeListener) {
                            ((OnTaskBoardChangeListener) listener).onMoveTaskToBoard(task, board, +1);
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

    public interface OnTaskBoardChangeListener {
        /**
         * Move task to adjacent board
         * @param task The task to move
         * @param currentBoard The current board containing the task
         * @param direction -1 for left (previous board), +1 for right (next board)
         */
        void onMoveTaskToBoard(Task task, Board currentBoard, int direction);
    }
}
