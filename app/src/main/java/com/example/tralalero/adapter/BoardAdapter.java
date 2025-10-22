package com.example.tralalero.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.model.Task;

import java.util.ArrayList;
import java.util.List;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.BoardViewHolder> {

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

        BoardViewHolder(View itemView) {
            super(itemView);
            tvBoardTitle = itemView.findViewById(R.id.tvBoardTitle);
            taskRecycler = itemView.findViewById(R.id.taskRecycler);
            btnAddCard = itemView.findViewById(R.id.btnAddCard);

            taskRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            taskAdapter = new TaskAdapter(new ArrayList<>());
            taskRecycler.setAdapter(taskAdapter);
        }

        void bind(Board board, OnBoardActionListener listener) {
            tvBoardTitle.setText(board.getName());

            if (listener != null) {
                List<Task> tasks = listener.getTasksForBoard(board.getId());
                if (tasks != null) {
                    taskAdapter.updateTasks(tasks);
                }

                taskAdapter.setOnTaskClickListener(task -> {
                    listener.onTaskClick(task, board);
                });

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
                    
                    /**
                     * Calculate new position when moving task from 'from' to 'to'
                     * IMPORTANT: Call this BEFORE swapping items in list!
                     */
                    private double calculateNewPositionForMove(int fromPos, int toPos) {
                        int taskCount = taskAdapter.getItemCount();
                        
                        if (taskCount <= 1) {
                            return 1000.0;
                        }
                        
                        // Moving to first position
                        if (toPos == 0) {
                            Task firstTask = taskAdapter.getTaskAt(0);
                            if (firstTask != null) {
                                return firstTask.getPosition() / 2.0;
                            }
                            return 500.0;
                        }
                        
                        // Moving to last position
                        if (toPos >= taskCount - 1) {
                            Task lastTask = taskAdapter.getTaskAt(taskCount - 1);
                            if (lastTask != null) {
                                return lastTask.getPosition() + 1024.0;
                            }
                            return toPos * 1000.0;
                        }
                        
                        // Moving to middle position
                        // Need to find what will be prev/next AFTER the swap
                        if (fromPos < toPos) {
                            // Moving DOWN: will be between toPos and toPos+1
                            Task prevTask = taskAdapter.getTaskAt(toPos);
                            Task nextTask = taskAdapter.getTaskAt(toPos + 1);
                            if (prevTask != null && nextTask != null) {
                                double result = (prevTask.getPosition() + nextTask.getPosition()) / 2.0;
                                android.util.Log.d("BoardAdapter", "Move DOWN: prev=" + prevTask.getPosition() + 
                                    ", next=" + nextTask.getPosition() + ", new=" + result);
                                return result;
                            }
                        } else {
                            // Moving UP: will be between toPos-1 and toPos
                            Task prevTask = taskAdapter.getTaskAt(toPos - 1);
                            Task nextTask = taskAdapter.getTaskAt(toPos);
                            if (prevTask != null && nextTask != null) {
                                double result = (prevTask.getPosition() + nextTask.getPosition()) / 2.0;
                                android.util.Log.d("BoardAdapter", "Move UP: prev=" + prevTask.getPosition() + 
                                    ", next=" + nextTask.getPosition() + ", new=" + result);
                                return result;
                            }
                        }
                        
                        return toPos * 1000.0;
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
