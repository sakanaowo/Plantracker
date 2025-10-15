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
        ItemTouchHelper itemTouchHelper;

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

                btnAddCard.setOnClickListener(v -> {
                    listener.onAddCardClick(board);
                });

                setupDragAndDrop(board, listener);
            }
        }

        private void setupDragAndDrop(Board board, OnBoardActionListener listener) {
            ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0
            ) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView,
                                      @NonNull RecyclerView.ViewHolder viewHolder,
                                      @NonNull RecyclerView.ViewHolder target) {
                    int fromPosition = viewHolder.getAdapterPosition();
                    int toPosition = target.getAdapterPosition();

                    taskAdapter.moveItem(fromPosition, toPosition);

                    if (listener instanceof OnTaskPositionChangeListener) {
                        Task movedTask = taskAdapter.getTaskAt(toPosition);
                        ((OnTaskPositionChangeListener) listener).onTaskPositionChanged(
                                movedTask, toPosition, board
                        );
                    }

                    return true;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                }

                @Override
                public boolean isLongPressDragEnabled() {
                    return true;
                }

                @Override
                public boolean isItemViewSwipeEnabled() {
                    return false;
                }
            };

            itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(taskRecycler);
        }
    }

    public interface OnTaskPositionChangeListener {
        void onTaskPositionChanged(Task task, int newPosition, Board board);
    }
}
