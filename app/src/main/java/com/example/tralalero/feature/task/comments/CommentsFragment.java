package com.example.tralalero.feature.task.comments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.adapter.CommentAdapter;
import com.example.tralalero.App.App;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.remote.api.CommentApiService;
import com.example.tralalero.data.remote.api.AttachmentApiService;
import com.example.tralalero.data.remote.api.UserApiService;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.domain.model.TaskComment;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.domain.usecase.task.AddCommentUseCase;
import com.example.tralalero.domain.usecase.task.GetTaskCommentsUseCase;
import com.example.tralalero.domain.usecase.task.GetTaskByIdUseCase;
import com.example.tralalero.domain.usecase.task.GetTasksByBoardUseCase;
import com.example.tralalero.domain.usecase.task.CreateTaskUseCase;
import com.example.tralalero.domain.usecase.task.CreateQuickTaskUseCase;
import com.example.tralalero.domain.usecase.task.UpdateTaskUseCase;
import com.example.tralalero.domain.usecase.task.DeleteTaskUseCase;
import com.example.tralalero.domain.usecase.task.AssignTaskUseCase;
import com.example.tralalero.domain.usecase.task.UnassignTaskUseCase;
import com.example.tralalero.domain.usecase.task.MoveTaskToBoardUseCase;
import com.example.tralalero.domain.usecase.task.UpdateTaskPositionUseCase;
import com.example.tralalero.presentation.viewmodel.TaskViewModel;
import com.example.tralalero.presentation.viewmodel.TaskViewModelFactory;
import com.example.tralalero.network.ApiClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

/**
 * BottomSheet fragment to show and add comments for a task (per Dev2 guide).
 * Expects a taskId argument via newInstance.
 */
public class CommentsFragment extends BottomSheetDialogFragment {

    private static final String ARG_TASK_ID = "arg_task_id";
    private String taskId;
    private TaskViewModel taskViewModel;
    private CommentAdapter adapter;
    private UserApiService userApiService;

    public static CommentsFragment newInstance(String taskId) {
        CommentsFragment f = new CommentsFragment();
        Bundle b = new Bundle();
        b.putString(ARG_TASK_ID, taskId);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskId = getArguments().getString(ARG_TASK_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rvCommentsFragment);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // CommentAdapter will be initialized in setupViewModel() after userApiService is available

        EditText etComment = view.findViewById(R.id.etNewComment);
        Button btnAdd = view.findViewById(R.id.btnAddCommentFragment);
        TextView tvEmpty = view.findViewById(R.id.tvNoCommentsFragment);

        setupViewModel();
        
        // Setup adapter after ViewModel is ready
        setupCommentAdapter(rv);

        // Observe
        taskViewModel.getComments().observe(getViewLifecycleOwner(), comments -> {
            if (comments != null && !comments.isEmpty()) {
                adapter.setComments(comments);
                rv.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                rv.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        btnAdd.setOnClickListener(v -> {
            String text = etComment.getText().toString().trim();
            if (!TextUtils.isEmpty(text) && taskId != null) {
                TaskComment comment = new TaskComment("", taskId, null, text, null);
                taskViewModel.addComment(taskId, comment);
                etComment.setText("");
            }
        });

        // Initial load
        if (taskId != null) {
            taskViewModel.loadTaskComments(taskId);
        }
    }

    private void setupViewModel() {
        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        CommentApiService commentApiService = ApiClient.get(App.authManager).create(CommentApiService.class);
        AttachmentApiService attachmentApiService = ApiClient.get(App.authManager).create(AttachmentApiService.class);
        UserApiService userApiService = ApiClient.get(App.authManager).create(UserApiService.class);
        this.userApiService = userApiService; // Store reference for CommentAdapter
        ITaskRepository repository = new TaskRepositoryImpl(apiService, commentApiService, attachmentApiService);

        // Only create the use-cases we need for comments (others required by factory kept minimal)
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
        com.example.tralalero.domain.usecase.task.AddAttachmentUseCase addAttachmentUseCase = new com.example.tralalero.domain.usecase.task.AddAttachmentUseCase(repository);
        com.example.tralalero.domain.usecase.task.GetTaskAttachmentsUseCase getTaskAttachmentsUseCase = new com.example.tralalero.domain.usecase.task.GetTaskAttachmentsUseCase(repository);
        com.example.tralalero.domain.usecase.task.AddChecklistUseCase addChecklistUseCase = new com.example.tralalero.domain.usecase.task.AddChecklistUseCase(repository);
        com.example.tralalero.domain.usecase.task.GetTaskChecklistsUseCase getTaskChecklistsUseCase = new com.example.tralalero.domain.usecase.task.GetTaskChecklistsUseCase(repository);
        com.example.tralalero.domain.usecase.task.UpdateCommentUseCase updateCommentUseCase = new com.example.tralalero.domain.usecase.task.UpdateCommentUseCase(repository);
        com.example.tralalero.domain.usecase.task.DeleteCommentUseCase deleteCommentUseCase = new com.example.tralalero.domain.usecase.task.DeleteCommentUseCase(repository);
        com.example.tralalero.domain.usecase.task.DeleteAttachmentUseCase deleteAttachmentUseCase = new com.example.tralalero.domain.usecase.task.DeleteAttachmentUseCase(repository);
        com.example.tralalero.domain.usecase.task.GetAttachmentViewUrlUseCase getAttachmentViewUrlUseCase = new com.example.tralalero.domain.usecase.task.GetAttachmentViewUrlUseCase(repository);

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

        taskViewModel = new ViewModelProvider(this, factory).get(TaskViewModel.class);
    }
    
    private void setupCommentAdapter(RecyclerView rv) {
        adapter = new CommentAdapter(new CommentAdapter.OnCommentClickListener() {
            @Override
            public void onOptionsClick(TaskComment comment, int position) {
                // For now show a tiny placeholder (edit/delete can be wired later)
            }

            @Override
            public void onCommentClick(TaskComment comment) {
                // no-op
            }
        }, userApiService);
        rv.setAdapter(adapter);
    }
}

