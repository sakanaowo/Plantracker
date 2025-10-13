package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.repository.ITaskRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * UseCase: Get all tasks in a board
 *
 * Matches Backend: GET /api/tasks/by-board/:boardId
 *
 * Input: String boardId
 * Output: List<Task>
 *
 * Business Logic:
 * - Sort by position ASC (for drag & drop ordering)
 * - Backend already filters deleted tasks
 */
public class GetTasksByBoardUseCase {

    private final ITaskRepository taskRepository;

    public GetTasksByBoardUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Execute: Get tasks by board ID
     *
     * @param boardId Board ID (UUID format)
     * @param callback Callback to receive result
     */
    public void execute(String boardId, Callback<List<Task>> callback) {
        // Validate input
        if (boardId == null || boardId.trim().isEmpty()) {
            callback.onError("Board ID cannot be empty");
            return;
        }

        // Validate UUID format
        if (!isValidUUID(boardId)) {
            callback.onError("Invalid board ID format");
            return;
        }

        // Call repository
        taskRepository.getTasksByBoard(boardId, new ITaskRepository.RepositoryCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> result) {
                // Business logic: Sort by position ASC
                // Note: Backend already filters deleted tasks
                List<Task> sorted = sortByPosition(result);
                callback.onSuccess(sorted);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Sort tasks by position ASC (for drag & drop ordering)
     */
    private List<Task> sortByPosition(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return new ArrayList<>();
        }

        List<Task> sorted = new ArrayList<>(tasks);
        Collections.sort(sorted, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return Double.compare(t1.getPosition(), t2.getPosition());
            }
        });

        return sorted;
    }

    /**
     * Validate UUID format
     */
    private boolean isValidUUID(String uuid) {
        return uuid.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }

    /**
     * Callback interface
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}