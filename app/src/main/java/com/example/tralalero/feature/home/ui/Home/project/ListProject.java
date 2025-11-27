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
import com.example.tralalero.domain.model.Label;
import com.example.tralalero.presentation.viewmodel.TaskViewModel;
import com.example.tralalero.presentation.viewmodel.LabelViewModel;
import com.example.tralalero.presentation.viewmodel.LabelViewModelFactory;
import com.example.tralalero.data.repository.LabelRepositoryImpl;
import com.example.tralalero.domain.repository.ILabelRepository;
import com.example.tralalero.data.remote.api.LabelApiService;
import com.example.tralalero.domain.usecase.label.*;
import com.google.android.material.button.MaterialButton;
import android.widget.ImageButton;
import com.example.tralalero.presentation.viewmodel.TaskViewModelFactory;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.remote.api.CommentApiService;
import com.example.tralalero.data.remote.api.AttachmentApiService;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.App.App;
import com.example.tralalero.domain.usecase.task.*;
import com.example.tralalero.presentation.viewmodel.BoardViewModel;
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
    private LabelViewModel labelViewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TaskAdapter taskAdapter;
    private FloatingActionButton fabAddTask;
    
    // Filter related
    private MaterialButton btnFilterLabel;
    private TextView tvFilterStatus;
    private ImageButton btnClearFilter;
    private List<Task> allTasks = new ArrayList<>();
    private String selectedLabelId = null;
    private String selectedLabelName = null;

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
        // Filter UI elements not present in layout - commented out
        // btnFilterLabel = view.findViewById(R.id.btnFilterLabel);
        // tvFilterStatus = view.findViewById(R.id.tvFilterStatus);
        // btnClearFilter = view.findViewById(R.id.btnClearFilter);
        
        if (fabAddTask != null) {
            fabAddTask.setOnClickListener(v -> showCreateTaskDialog());
        }
        
        if (btnFilterLabel != null) {
            btnFilterLabel.setOnClickListener(v -> showLabelFilterDialog());
        }
        
        if (btnClearFilter != null) {
            btnClearFilter.setOnClickListener(v -> clearFilter());
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
        CreateQuickTaskUseCase createQuickTaskUseCase = new CreateQuickTaskUseCase(repository);
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
            createQuickTaskUseCase,
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
        
        // Create adapter with board ID
        taskAdapter = new com.example.tralalero.adapter.TaskAdapter(new ArrayList<>(), boardId);
        
        // Set click listener
        taskAdapter.setOnTaskClickListener(task -> {
            showTaskDetailBottomSheet(task);
            Log.d(TAG, "Task clicked: " + task.getId());
        });
        
        // Set move listener
        taskAdapter.setOnTaskMoveListener(new com.example.tralalero.adapter.TaskAdapter.OnTaskMoveListener() {
            @Override
            public void onMoveLeft(Task task, int position) {
                Log.d(TAG, "onMoveLeft called for task: " + task.getTitle());
            }

            @Override
            public void onMoveRight(Task task, int position) {
                Log.d(TAG, "onMoveRight called for task: " + task.getTitle());
            }
        });
        
        // Set status change listener for checkbox
        taskAdapter.setOnTaskStatusChangeListener((task, isDone) -> {
            handleCheckboxClick(task, type);
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
                    allTasks.clear();
                    allTasks.addAll(tasks);
                    applyFilter();
                    Log.d(TAG, "Loaded " + tasks.size() + " tasks for board: " + boardId);
                } else {
                    allTasks.clear();
                    applyFilter();
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
            
            // Clear filter when switching boards
            clearFilter();
            
            if (getArguments() != null) {
                getArguments().putString(ARG_BOARD_ID, newBoardId);
            }
            taskViewModel.getTasksForBoard(newBoardId).observe(getViewLifecycleOwner(), tasks -> {
                if (tasks != null && !tasks.isEmpty()) {
                    allTasks.clear();
                    allTasks.addAll(tasks);
                    applyFilter();
                    Log.d(TAG, "Loaded " + tasks.size() + " tasks for updated board: " + newBoardId);
                } else {
                    allTasks.clear();
                    applyFilter();
                    Log.d(TAG, "No tasks found for updated board: " + newBoardId);
                }
            });
            loadTasksForBoard();
        }
    }

    public String getBoardId() {
        return boardId;
    }
    
    /**
     * Handle checkbox click logic based on current board type:
     * - To Do → In Progress
     * - In Progress → Done  
     * - Done → To Do (reopen task)
     */
    private void handleCheckboxClick(Task task, String currentBoardType) {
        Log.d(TAG, "Checkbox clicked for task: " + task.getTitle() + " in board: " + currentBoardType);
        
        // Determine target status based on current board
        Task.TaskStatus targetStatus;
        String targetBoardName;
        
        if ("To Do".equals(currentBoardType)) {
            targetStatus = Task.TaskStatus.IN_PROGRESS;
            targetBoardName = "In Progress";
        } else if ("In Progress".equals(currentBoardType)) {
            targetStatus = Task.TaskStatus.DONE;
            targetBoardName = "Done";
        } else if ("Done".equals(currentBoardType)) {
            // Reopen task - move back to To Do
            targetStatus = Task.TaskStatus.TO_DO;
            targetBoardName = "To Do";
        } else {
            Log.e(TAG, "Unknown board type: " + currentBoardType);
            return;
        }
        
        // Find target board ID
        findBoardIdByName(targetBoardName, targetBoardId -> {
            if (targetBoardId != null) {
                // Move task to target board
                Log.d(TAG, "Moving task '" + task.getTitle() + "' from " + currentBoardType + " to " + targetBoardName);
                taskViewModel.moveTaskToBoard(task.getId(), targetBoardId, 1000.0); // Default position
                
                Toast.makeText(getContext(), 
                    "Task moved to " + targetBoardName, 
                    Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Could not find board: " + targetBoardName);
                Toast.makeText(getContext(), 
                    "Error: Could not find " + targetBoardName + " board", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Find board ID by board name
     */
    private void findBoardIdByName(String boardName, BoardIdCallback callback) {
        if (getActivity() == null || projectId == null) {
            callback.onBoardIdFound(null);
            return;
        }
        
        // Get BoardViewModel from activity to access all boards
        BoardViewModel boardViewModel = new ViewModelProvider(requireActivity()).get(BoardViewModel.class);
        
        // First load boards for this project
        boardViewModel.loadBoardsForProject(projectId);
        
        // Then observe the boards
        boardViewModel.getProjectBoards().observe(getViewLifecycleOwner(), boards -> {
            if (boards != null) {
                for (com.example.tralalero.domain.model.Board board : boards) {
                    if (boardName.equalsIgnoreCase(board.getName())) {
                        callback.onBoardIdFound(board.getId());
                        return;
                    }
                }
            }
            callback.onBoardIdFound(null);
        });
    }
    
    /**
     * Callback interface for board ID lookup
     */
    private interface BoardIdCallback {
        void onBoardIdFound(String boardId);
    }
    
    // ==================== FILTER METHODS ====================
    
    private void setupLabelViewModel() {
        if (projectId == null) return;
        
        LabelApiService apiService = ApiClient.get(App.authManager).create(LabelApiService.class);
        ILabelRepository repository = new LabelRepositoryImpl(apiService);
        
        GetLabelsByWorkspaceUseCase getLabelsByWorkspaceUseCase = new GetLabelsByWorkspaceUseCase(repository);
        GetLabelsByProjectUseCase getLabelsByProjectUseCase = new GetLabelsByProjectUseCase(repository);
        GetLabelByIdUseCase getLabelByIdUseCase = new GetLabelByIdUseCase(repository);
        CreateLabelUseCase createLabelUseCase = new CreateLabelUseCase(repository);
        CreateLabelInProjectUseCase createLabelInProjectUseCase = new CreateLabelInProjectUseCase(repository);
        UpdateLabelUseCase updateLabelUseCase = new UpdateLabelUseCase(repository);
        DeleteLabelUseCase deleteLabelUseCase = new DeleteLabelUseCase(repository);
        GetTaskLabelsUseCase getTaskLabelsUseCase = new GetTaskLabelsUseCase(repository);
        AssignLabelToTaskUseCase assignLabelToTaskUseCase = new AssignLabelToTaskUseCase(repository);
        RemoveLabelFromTaskUseCase removeLabelFromTaskUseCase = new RemoveLabelFromTaskUseCase(repository);
        
        LabelViewModelFactory factory = new LabelViewModelFactory(
                getLabelsByWorkspaceUseCase,
                getLabelsByProjectUseCase,
                getLabelByIdUseCase,
                createLabelUseCase,
                createLabelInProjectUseCase,
                updateLabelUseCase,
                deleteLabelUseCase,
                getTaskLabelsUseCase,
                assignLabelToTaskUseCase,
                removeLabelFromTaskUseCase
        );
        
        labelViewModel = new ViewModelProvider(this, factory).get(LabelViewModel.class);
    }
    
    private void showLabelFilterDialog() {
        if (projectId == null) {
            Toast.makeText(getContext(), "Project ID not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (labelViewModel == null) {
            setupLabelViewModel();
        }
        
        // Load labels and show dialog
        labelViewModel.loadLabelsByProject(projectId);
        
        labelViewModel.getLabels().observe(getViewLifecycleOwner(), labels -> {
            if (labels != null && !labels.isEmpty()) {
                showLabelSelectionDialog(labels);
            } else {
                Toast.makeText(getContext(), "No labels found in this project", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLabelSelectionDialog(List<Label> labels) {
        if (getContext() == null) return;
        
        String[] labelNames = new String[labels.size() + 1];
        labelNames[0] = "All tasks (No filter)";
        for (int i = 0; i < labels.size(); i++) {
            labelNames[i + 1] = labels.get(i).getName();
        }
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Filter by Label");
        builder.setItems(labelNames, (dialog, which) -> {
            if (which == 0) {
                // Clear filter
                clearFilter();
            } else {
                // Apply label filter
                Label selectedLabel = labels.get(which - 1);
                applyLabelFilter(selectedLabel.getId(), selectedLabel.getName());
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void applyLabelFilter(String labelId, String labelName) {
        this.selectedLabelId = labelId;
        this.selectedLabelName = labelName;
        
        Log.d(TAG, "Applying filter for label: " + labelName + " (ID: " + labelId + ")");
        
        // Update filter status UI
        if (tvFilterStatus != null) {
            tvFilterStatus.setVisibility(View.VISIBLE);
            tvFilterStatus.setText("Filtered by: " + labelName);
        }
        if (btnClearFilter != null) {
            btnClearFilter.setVisibility(View.VISIBLE);
        }
        if (btnFilterLabel != null) {
            btnFilterLabel.setText("Change Filter");
        }
        
        applyFilter();
    }
    
    private void clearFilter() {
        this.selectedLabelId = null;
        this.selectedLabelName = null;
        
        Log.d(TAG, "Clearing filter");
        
        // Update UI
        if (tvFilterStatus != null) {
            tvFilterStatus.setVisibility(View.GONE);
        }
        if (btnClearFilter != null) {
            btnClearFilter.setVisibility(View.GONE);
        }
        if (btnFilterLabel != null) {
            btnFilterLabel.setText("Filter by Label");
        }
        
        applyFilter();
    }
    
    private void applyFilter() {
        List<Task> filteredTasks;
        
        if (selectedLabelId == null) {
            // No filter - show all tasks
            filteredTasks = new ArrayList<>(allTasks);
        } else {
            // Filter by label
            filteredTasks = new ArrayList<>();
            for (Task task : allTasks) {
                if (task.getLabels() != null) {
                    for (Label label : task.getLabels()) {
                        if (label.getId().equals(selectedLabelId)) {
                            filteredTasks.add(task);
                            break;
                        }
                    }
                }
            }
        }
        
        // Update adapter
        taskAdapter.updateTasks(filteredTasks);
        
        // Update UI visibility
        if (filteredTasks.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            if (emptyView != null) {
                emptyView.setVisibility(View.VISIBLE);
                if (selectedLabelId != null) {
                    emptyView.setText("No tasks with label: " + selectedLabelName);
                } else {
                    emptyView.setText(getEmptyMessage());
                }
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
            }
        }
        
        Log.d(TAG, "Applied filter - showing " + filteredTasks.size() + " of " + allTasks.size() + " tasks");
    }
}

