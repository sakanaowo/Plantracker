package com.example.tralalero.feature.home.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tralalero.App.App;
import com.example.tralalero.R;
import com.example.tralalero.data.remote.api.AttachmentApiService;
import com.example.tralalero.data.remote.api.CommentApiService;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.data.repository.TaskRepositoryImplWithCache;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.domain.usecase.task.*;
import com.example.tralalero.feature.home.ui.Home.project.TaskAdapter;
import com.example.tralalero.feature.home.ui.Home.project.TaskDetailBottomSheet;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.presentation.viewmodel.TaskViewModel;
import com.example.tralalero.presentation.viewmodel.TaskViewModelFactory;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class InboxFragment extends Fragment {
    private static final String TAG = "InboxFragment";
    private TaskViewModel taskViewModel;
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private RelativeLayout notiLayout;
    private LinearLayout inboxQuickAccess;
    private TextInputEditText inboxAddCard;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View loadingView;
    private View emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inbox, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViewModel();
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupNotificationCard();
        setupQuickAddTask();
        observeViewModel();
        
        taskViewModel.loadInboxTasks("", false);
    }

    private void setupViewModel() {
        TaskRepositoryImplWithCache repositoryWithCache = 
            App.dependencyProvider.getTaskRepositoryWithCache();
        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        CommentApiService commentApiService = ApiClient.get(App.authManager).create(CommentApiService.class);
        AttachmentApiService attachmentApiService = ApiClient.get(App.authManager).create(AttachmentApiService.class);
        ITaskRepository apiRepository = new TaskRepositoryImpl(apiService, commentApiService, attachmentApiService);

        GetTaskByIdUseCase getTaskByIdUseCase = new GetTaskByIdUseCase(apiRepository);
        GetTasksByBoardUseCase getTasksByBoardUseCase = new GetTasksByBoardUseCase(apiRepository);
        CreateTaskUseCase createTaskUseCase = new CreateTaskUseCase(apiRepository);
        CreateQuickTaskUseCase createQuickTaskUseCase = new CreateQuickTaskUseCase(apiRepository);
        UpdateTaskUseCase updateTaskUseCase = new UpdateTaskUseCase(apiRepository);
        DeleteTaskUseCase deleteTaskUseCase = new DeleteTaskUseCase(apiRepository);
        AssignTaskUseCase assignTaskUseCase = new AssignTaskUseCase(apiRepository);
        UnassignTaskUseCase unassignTaskUseCase = new UnassignTaskUseCase(apiRepository);
        MoveTaskToBoardUseCase moveTaskToBoardUseCase = new MoveTaskToBoardUseCase(apiRepository);
        UpdateTaskPositionUseCase updateTaskPositionUseCase = new UpdateTaskPositionUseCase(apiRepository);
        AddCommentUseCase addCommentUseCase = new AddCommentUseCase(apiRepository);
        GetTaskCommentsUseCase getTaskCommentsUseCase = new GetTaskCommentsUseCase(apiRepository);
        AddAttachmentUseCase addAttachmentUseCase = new AddAttachmentUseCase(apiRepository);
        GetTaskAttachmentsUseCase getTaskAttachmentsUseCase = new GetTaskAttachmentsUseCase(apiRepository);
        AddChecklistUseCase addChecklistUseCase = new AddChecklistUseCase(apiRepository);
        GetTaskChecklistsUseCase getTaskChecklistsUseCase = new GetTaskChecklistsUseCase(apiRepository);
        UpdateCommentUseCase updateCommentUseCase = new UpdateCommentUseCase(apiRepository);
        DeleteCommentUseCase deleteCommentUseCase = new DeleteCommentUseCase(apiRepository);
        DeleteAttachmentUseCase deleteAttachmentUseCase = new DeleteAttachmentUseCase(apiRepository);
        GetAttachmentViewUrlUseCase getAttachmentViewUrlUseCase = new GetAttachmentViewUrlUseCase(apiRepository);

        TaskViewModelFactory factory = new TaskViewModelFactory(
            getTaskByIdUseCase, getTasksByBoardUseCase, createTaskUseCase, createQuickTaskUseCase,
            updateTaskUseCase, deleteTaskUseCase, assignTaskUseCase, unassignTaskUseCase,
            moveTaskToBoardUseCase, updateTaskPositionUseCase, addCommentUseCase,
            getTaskCommentsUseCase, addAttachmentUseCase, getTaskAttachmentsUseCase,
            addChecklistUseCase, getTaskChecklistsUseCase, updateCommentUseCase,
            deleteCommentUseCase, deleteAttachmentUseCase, getAttachmentViewUrlUseCase,
            apiRepository
        );
        taskViewModel = new ViewModelProvider(this, factory).get(TaskViewModel.class);
        Log.d(TAG, "TaskViewModel initialized with cache-enabled repository");
    }
    
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewInbox);
        notiLayout = view.findViewById(R.id.notificationLayout);
        inboxQuickAccess = view.findViewById(R.id.inboxQuickAccess);
        inboxAddCard = view.findViewById(R.id.inboxAddCard);
        loadingView = view.findViewById(R.id.loadingView);
        emptyView = view.findViewById(R.id.emptyView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    }
    
    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                showTaskDetailBottomSheet(task);
            }
            
            @Override
            public void onTaskCheckboxClick(Task task, String currentBoardType) {
                // Inbox doesn't have board types, just complete task
                handleTaskCompleted(task);
            }
        });
        // Set board type as "Inbox" for checkbox logic
        taskAdapter.setCurrentBoardType("Inbox");
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(taskAdapter);
    }
    
    private void setupNotificationCard() {
        ImageButton btnCloseNotification = getView().findViewById(R.id.btnClosePjrDetail);
        if (btnCloseNotification != null) {
            btnCloseNotification.setOnClickListener(v -> notiLayout.setVisibility(View.GONE));
        }
    }
    
    private void setupQuickAddTask() {
        inboxAddCard.setOnClickListener(v -> inboxQuickAccess.setVisibility(View.VISIBLE));
        
        Button btnCancel = getView().findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
            inboxQuickAccess.setVisibility(View.GONE);
            inboxAddCard.setText("");
        });
        
        Button btnAdd = getView().findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            String taskTitle = inboxAddCard.getText().toString().trim();
            if (!taskTitle.isEmpty()) {
                createTask(taskTitle);
                inboxQuickAccess.setVisibility(View.GONE);
                inboxAddCard.setText("");
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập tên task!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void observeViewModel() {
        taskViewModel.getInboxTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                taskAdapter.setTasks(tasks);
                showContent();
            } else {
                taskAdapter.setTasks(new ArrayList<>());
                showEmpty();
            }
        });
        
        taskViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
            );
            swipeRefreshLayout.setOnRefreshListener(() -> {
                Log.d(TAG, "Pull-to-refresh - clearing cache and reloading");
                taskViewModel.loadInboxTasks("", true);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                }, 500);
            });
        }
    }
    
    private void showLoading() {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setVisibility(View.GONE);
        if (emptyView != null) emptyView.setVisibility(View.GONE);
        if (loadingView != null) loadingView.setVisibility(View.VISIBLE);
    }
    
    private void showContent() {
        if (loadingView != null) loadingView.setVisibility(View.GONE);
        if (emptyView != null) emptyView.setVisibility(View.GONE);
        if (swipeRefreshLayout != null) swipeRefreshLayout.setVisibility(View.VISIBLE);
    }
    
    private void showEmpty() {
        if (loadingView != null) loadingView.setVisibility(View.GONE);
        if (swipeRefreshLayout != null) swipeRefreshLayout.setVisibility(View.GONE);
        if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
    }

    private void createTask(String title) {
        taskViewModel.createQuickTask(title, "");
        Toast.makeText(getContext(), "Creating task...", Toast.LENGTH_SHORT).show();
    }

    private void showTaskDetailBottomSheet(Task task) {
        TaskDetailBottomSheet bottomSheet = TaskDetailBottomSheet.newInstance(task);
        bottomSheet.setOnActionClickListener(new TaskDetailBottomSheet.OnActionClickListener() {
            @Override
            public void onAssignTask(Task task) {
                // Handle assign
            }
            @Override
            public void onMoveTask(Task task) {
                // Handle move
            }
            @Override
            public void onAddComment(Task task) {
                // Handle comment
            }
            @Override
            public void onDeleteTask(Task task) {
                taskViewModel.deleteTask(task.getId());
            }
            @Override
            public void onUpdateStartDate(Task task, java.util.Date startDate) {
                // Handle start date
            }
            @Override
            public void onUpdateDueDate(Task task, java.util.Date dueDate) {
                // Handle due date
            }
            @Override
            public void onUpdateTask(Task task, String newTitle, String newDescription) {
                Task updatedTask = new Task(
                    task.getId(), task.getProjectId(), task.getBoardId(),
                    newTitle, newDescription, task.getIssueKey(), task.getType(),
                    task.getStatus(), task.getPriority(), task.getPosition(),
                    task.getAssigneeId(), task.getCreatedBy(), task.getSprintId(),
                    task.getEpicId(), task.getParentTaskId(), task.getStartAt(),
                    task.getDueAt(), task.getStoryPoints(), task.getOriginalEstimateSec(),
                    task.getRemainingEstimateSec(), task.getCreatedAt(), task.getUpdatedAt()
                );
                taskViewModel.updateTask(task.getId(), updatedTask);
            }
        });
        bottomSheet.show(getChildFragmentManager(), "TaskDetailBottomSheet");
    }
    
    private void handleTaskCompleted(Task task) {
        taskViewModel.toggleTaskComplete(task);
        Toast.makeText(getContext(), "Task updated", Toast.LENGTH_SHORT).show();
    }
}
