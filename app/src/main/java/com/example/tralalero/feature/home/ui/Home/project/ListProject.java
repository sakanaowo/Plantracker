package com.example.tralalero.feature.home.ui.Home.project;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static android.app.Activity.RESULT_OK;

import com.example.tralalero.R;
import com.example.tralalero.adapter.TaskAdapter;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.presentation.viewmodel.TaskViewModel;
import com.example.tralalero.presentation.viewmodel.TaskViewModelFactory;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.remote.api.CommentApiService;
import com.example.tralalero.data.remote.api.AttachmentApiService;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.App.App;
import com.example.tralalero.domain.usecase.task.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ListProject extends Fragment {
    private static final String TAG = "ListProject";
    private static final String ARG_TYPE = "type";
    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_BOARD_ID = "board_id";
    private static final int REQUEST_CODE_CREATE_TASK = 1001;
    private static final int REQUEST_CODE_EDIT_TASK = 1002;

    private String type;
    private String projectId;
    private String boardId;
    private TaskViewModel taskViewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TaskAdapter taskAdapter;
    private FloatingActionButton fabAddTask;

    public static ListProject newInstance(String type, String projectId, String boardId) {
        ListProject fragment = new ListProject();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_PROJECT_ID, projectId);
        args.putString(ARG_BOARD_ID, boardId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_frm, container, false);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE);
            projectId = getArguments().getString(ARG_PROJECT_ID);
            boardId = getArguments().getString(ARG_BOARD_ID);
        }

        Log.d(TAG, "onCreateView - Type: " + type + ", BoardId: " + boardId);
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        observeViewModel();
        if (boardId != null && !boardId.isEmpty()) {
            loadTasksForBoard();
        } else {
            Log.w(TAG, "BoardId not available yet, waiting for boards to load...");
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (boardId == null || boardId.isEmpty()) {
            if (getActivity() != null) {
                Log.d(TAG, "onResume - Still waiting for boardId");
            }
        }
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        fabAddTask = view.findViewById(R.id.fabAddTask);
        if (fabAddTask != null) {
            fabAddTask.setOnClickListener(v -> showCreateTaskDialog());
        }
    }

    private void setupViewModel() {
        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        CommentApiService commentApiService = ApiClient.get(App.authManager).create(CommentApiService.class);
        AttachmentApiService attachmentApiService = ApiClient.get(App.authManager).create(AttachmentApiService.class);
        ITaskRepository repository = new TaskRepositoryImpl(apiService, commentApiService, attachmentApiService);
        GetTaskByIdUseCase getTaskByIdUseCase = new GetTaskByIdUseCase(repository);
        GetTasksByBoardUseCase getTasksByBoardUseCase = new GetTasksByBoardUseCase(repository);
        CreateTaskUseCase createTaskUseCase = new CreateTaskUseCase(repository);
        UpdateTaskUseCase updateTaskUseCase = new UpdateTaskUseCase(repository);
        DeleteTaskUseCase deleteTaskUseCase = new DeleteTaskUseCase(repository);
        AssignTaskUseCase assignTaskUseCase = new AssignTaskUseCase(repository);
        UnassignTaskUseCase unassignTaskUseCase = new UnassignTaskUseCase(repository);
        MoveTaskToBoardUseCase moveTaskToBoardUseCase = new MoveTaskToBoardUseCase(repository);
        UpdateTaskPositionUseCase updateTaskPositionUseCase = new UpdateTaskPositionUseCase(repository);
        AddCommentUseCase addCommentUseCase = new AddCommentUseCase(repository);
        GetTaskCommentsUseCase getTaskCommentsUseCase = new GetTaskCommentsUseCase(repository);
        AddAttachmentUseCase addAttachmentUseCase = new AddAttachmentUseCase(repository);
        GetTaskAttachmentsUseCase getTaskAttachmentsUseCase = new GetTaskAttachmentsUseCase(repository);
        AddChecklistUseCase addChecklistUseCase = new AddChecklistUseCase(repository);
        GetTaskChecklistsUseCase getTaskChecklistsUseCase = new GetTaskChecklistsUseCase(repository);
        UpdateCommentUseCase updateCommentUseCase = new UpdateCommentUseCase(repository);
        DeleteCommentUseCase deleteCommentUseCase = new DeleteCommentUseCase(repository);
        DeleteAttachmentUseCase deleteAttachmentUseCase = new DeleteAttachmentUseCase(repository);
        GetAttachmentViewUrlUseCase getAttachmentViewUrlUseCase = new GetAttachmentViewUrlUseCase(repository);
        TaskViewModelFactory factory = new TaskViewModelFactory(
            getTaskByIdUseCase,
            getTasksByBoardUseCase,
            createTaskUseCase,
            updateTaskUseCase,
            deleteTaskUseCase,
            assignTaskUseCase,
            unassignTaskUseCase,
            moveTaskToBoardUseCase,
            updateTaskPositionUseCase,
            addCommentUseCase,
            getTaskCommentsUseCase,
            addAttachmentUseCase,
            getTaskAttachmentsUseCase,
            addChecklistUseCase,
            getTaskChecklistsUseCase,
            updateCommentUseCase,
            deleteCommentUseCase,
            deleteAttachmentUseCase,
            getAttachmentViewUrlUseCase,
            repository
        );
        taskViewModel = new ViewModelProvider(requireActivity(), factory).get(TaskViewModel.class);
        Log.d(TAG, "TaskViewModel initialized");
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(new ArrayList<>());
        taskAdapter.setOnTaskClickListener(task -> {
            showTaskDetailBottomSheet(task);
            Log.d(TAG, "Task clicked: " + task.getId());
        });
        taskAdapter.setOnTaskMoveListener(new TaskAdapter.OnTaskMoveListener() {
            @Override
            public void onMoveLeft(Task task, int position) {
                Log.d(TAG, "onMoveLeft called for task: " + task.getTitle());
            }

            @Override
            public void onMoveRight(Task task, int position) {
                Log.d(TAG, "onMoveRight called for task: " + task.getTitle());
            }
        });

        recyclerView.setAdapter(taskAdapter);
    }

    private void moveTaskToPosition(int fromPosition, int toPosition) {
        Task movedTask = taskAdapter.getTaskAt(fromPosition);
        if (movedTask == null) {
            Log.e(TAG, "❌ Cannot move: task at position " + fromPosition + " is null");
            return;
        }
        double newPosition = calculateNewPositionForMove(fromPosition, toPosition);

        Log.d(TAG, "Moving task '" + movedTask.getTitle() + "' from " + fromPosition + " to " + toPosition + 
                   ", new position value: " + newPosition);
        taskAdapter.moveItem(fromPosition, toPosition);
        taskViewModel.updateTaskPosition(movedTask.getId(), newPosition);
    }

    private double calculateNewPositionForMove(int fromPos, int toPos) {
        int taskCount = taskAdapter.getItemCount();

        if (taskCount <= 1) {
            return 1000.0;
        }
        if (toPos == 0) {
            Task firstTask = taskAdapter.getTaskAt(0);
            if (firstTask != null) {
                return firstTask.getPosition() / 2.0;
            }
            return 500.0;
        }
        if (toPos >= taskCount - 1) {
            Task lastTask = taskAdapter.getTaskAt(taskCount - 1);
            if (lastTask != null) {
                return lastTask.getPosition() + 1024.0;
            }
            return toPos * 1000.0;
        }
        if (fromPos < toPos) {
            Task prevTask = taskAdapter.getTaskAt(toPos);
            Task nextTask = taskAdapter.getTaskAt(toPos + 1);
            if (prevTask != null && nextTask != null) {
                return (prevTask.getPosition() + nextTask.getPosition()) / 2.0;
            }
        } else {
            Task prevTask = taskAdapter.getTaskAt(toPos - 1);
            Task nextTask = taskAdapter.getTaskAt(toPos);
            if (prevTask != null && nextTask != null) {
                return (prevTask.getPosition() + nextTask.getPosition()) / 2.0;
            }
        }

        return toPos * 1000.0;
    }

    private void observeViewModel() {
        if (boardId != null && !boardId.isEmpty()) {
            taskViewModel.getTasksForBoard(boardId).observe(getViewLifecycleOwner(), tasks -> {
                if (tasks != null && !tasks.isEmpty()) {
                    taskAdapter.updateTasks(tasks);
                    Log.d(TAG, "Loaded " + tasks.size() + " tasks for board: " + boardId);
                    recyclerView.setVisibility(View.VISIBLE);
                    if (emptyView != null) {
                        emptyView.setVisibility(View.GONE);
                    }
                } else {
                    recyclerView.setVisibility(View.GONE);
                    if (emptyView != null) {
                        emptyView.setVisibility(View.VISIBLE);
                        emptyView.setText(getEmptyMessage());
                    }
                    Log.d(TAG, "No tasks found for board: " + boardId);
                }
            });
        } else {
            Log.w(TAG, "Cannot observe tasks yet - boardId is null");
        }
        taskViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            Log.d(TAG, "Loading state for " + type + ": " + isLoading);
        });
        taskViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error loading tasks for " + type + ": " + error);
                taskViewModel.clearError();
            }
        });
    }
    private void loadTasksForBoard() {
        if (boardId == null || boardId.isEmpty()) {
            Log.e(TAG, "Cannot load tasks: boardId is null");
            return;
        }

        Log.d(TAG, "Loading tasks for board: " + boardId);
        taskViewModel.loadTasksByBoard(boardId);
    }

    private void showTaskDetailBottomSheet(Task task) {
        Intent intent = new Intent(getContext(), CardDetailActivity.class);
        intent.putExtra(CardDetailActivity.EXTRA_TASK_ID, task.getId());
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_ID, boardId);
        intent.putExtra(CardDetailActivity.EXTRA_PROJECT_ID, projectId);
        intent.putExtra(CardDetailActivity.EXTRA_IS_EDIT_MODE, true);
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_NAME, type); // Pass board name
        intent.putExtra(CardDetailActivity.EXTRA_TASK_TITLE, task.getTitle());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_DESCRIPTION, task.getDescription());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_PRIORITY, task.getPriority() != null ? task.getPriority().name() : "MEDIUM");
        intent.putExtra(CardDetailActivity.EXTRA_TASK_STATUS, task.getStatus() != null ? task.getStatus().name() : "TO_DO");
        intent.putExtra(CardDetailActivity.EXTRA_TASK_POSITION, task.getPosition());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_ASSIGNEE_ID, task.getAssigneeId());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_CREATED_BY, task.getCreatedBy());

        startActivityForResult(intent, REQUEST_CODE_EDIT_TASK);
    }

    private void showCreateTaskDialog() {
        if (boardId == null || boardId.isEmpty()) {
            Toast.makeText(getContext(), "Board not ready yet, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (projectId == null || projectId.isEmpty()) {
            Toast.makeText(getContext(), "Project ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Opening CardDetailActivity for creating task in board: " + boardId);
        Intent intent = new Intent(getContext(), CardDetailActivity.class);
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_ID, boardId);
        intent.putExtra(CardDetailActivity.EXTRA_PROJECT_ID, projectId);
        intent.putExtra(CardDetailActivity.EXTRA_IS_EDIT_MODE, false);
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_NAME, type); // Pass board name

        startActivityForResult(intent, REQUEST_CODE_CREATE_TASK);
    }

    private String getEmptyMessage() {
        return "No tasks in " + type;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CREATE_TASK) {
                Log.d(TAG, "Task created, reloading tasks...");
                if (boardId != null && !boardId.isEmpty()) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        loadTasksForBoard();
                    }, 500);
                }
            } else if (requestCode == REQUEST_CODE_EDIT_TASK) {
                Log.d(TAG, "Task updated/deleted, reloading tasks...");
                if (boardId != null && !boardId.isEmpty()) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        loadTasksForBoard();
                    }, 500);
                }
            }
        }
    }

    public void updateBoardIdAndReload(String newBoardId) {
        if (newBoardId != null && !newBoardId.isEmpty() && !newBoardId.equals(this.boardId)) {
            Log.d(TAG, "Updating boardId from " + this.boardId + " to " + newBoardId);
            this.boardId = newBoardId;
            if (getArguments() != null) {
                getArguments().putString(ARG_BOARD_ID, newBoardId);
            }
            taskViewModel.getTasksForBoard(newBoardId).observe(getViewLifecycleOwner(), tasks -> {
                if (tasks != null && !tasks.isEmpty()) {
                    taskAdapter.updateTasks(tasks);
                    Log.d(TAG, "Loaded " + tasks.size() + " tasks for updated board: " + newBoardId);
                    recyclerView.setVisibility(View.VISIBLE);
                    if (emptyView != null) {
                        emptyView.setVisibility(View.GONE);
                    }
                } else {
                    recyclerView.setVisibility(View.GONE);
                    if (emptyView != null) {
                        emptyView.setVisibility(View.VISIBLE);
                        emptyView.setText(getEmptyMessage());
                    }
                    Log.d(TAG, "No tasks found for updated board: " + newBoardId);
                }
            });
            loadTasksForBoard();
        }
    }

    public String getBoardId() {
        return boardId;
    }
}

