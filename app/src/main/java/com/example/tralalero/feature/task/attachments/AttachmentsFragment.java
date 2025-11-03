package com.example.tralalero.feature.task.attachments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.adapter.AttachmentAdapter;
import com.example.tralalero.App.App;
import com.example.tralalero.data.remote.api.AttachmentApiService;
import com.example.tralalero.data.remote.api.CommentApiService;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.domain.model.Attachment;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.domain.usecase.task.AddAttachmentUseCase;
import com.example.tralalero.domain.usecase.task.GetTaskAttachmentsUseCase;
import com.example.tralalero.feature.task.attachments.AttachmentUploader;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.presentation.viewmodel.TaskViewModel;
import com.example.tralalero.presentation.viewmodel.TaskViewModelFactory;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Date;

/**
 * BottomSheet fragment to show attachments and pick/upload files via AttachmentUploader
 */
public class AttachmentsFragment extends BottomSheetDialogFragment {
    private static final int PICK_FILE = 1001;
    private static final String ARG_TASK_ID = "arg_task_id";
    private String taskId;
    private TaskViewModel taskViewModel;
    private AttachmentAdapter adapter;
    private AttachmentUploader uploader;

    public static AttachmentsFragment newInstance(String taskId) {
        AttachmentsFragment f = new AttachmentsFragment();
        Bundle b = new Bundle();
        b.putString(ARG_TASK_ID, taskId);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) taskId = getArguments().getString(ARG_TASK_ID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_attachments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rvAttachmentsFragment);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AttachmentAdapter(new AttachmentAdapter.OnAttachmentClickListener() {
            @Override
            public void onDownloadClick(Attachment attachment) {
                // TODO: Open or download using View URL
            }

            @Override
            public void onDeleteClick(Attachment attachment) {
                // TODO: Delete via ViewModel
            }

            @Override
            public void onAttachmentClick(Attachment attachment) {
                // no-op
            }
        });
        rv.setAdapter(adapter);

        Button btnPick = view.findViewById(R.id.btnPickFileFragment);
        TextView tvEmpty = view.findViewById(R.id.tvNoAttachmentsFragment);

        setupViewModelAndUploader();

        taskViewModel.getAttachments().observe(getViewLifecycleOwner(), attachments -> {
            if (attachments != null && !attachments.isEmpty()) {
                adapter.setAttachments(attachments);
                rv.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                rv.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        btnPick.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Select file"), PICK_FILE);
        });

        if (taskId != null) taskViewModel.loadTaskAttachments(taskId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null && taskId != null) {
                uploader.uploadFile(taskId, uri, new AttachmentUploader.UploadCallback() {
                    @Override
                    public void onProgress(int percent) {
                        // Could show progress UI (not implemented)
                        android.util.Log.d("AttachmentsFragment", "Upload progress: " + percent + "%");
                    }

                    @Override
                    public void onSuccess(com.example.tralalero.data.remote.dto.task.AttachmentDTO attachmentDTO) {
                        android.util.Log.d("AttachmentsFragment", "Upload success! File: " + attachmentDTO.fileName);
                        // Map to domain Attachment minimally and register via ViewModel
                        Attachment a = new Attachment(
                                attachmentDTO.id,
                                taskId,
                                attachmentDTO.url != null ? attachmentDTO.url : "",
                                attachmentDTO.fileName,
                                attachmentDTO.mimeType,
                                attachmentDTO.size,
                                null,
                                new Date()
                        );
                        taskViewModel.addAttachment(taskId, a);
                        
                        // Reload attachments to refresh UI
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                android.widget.Toast.makeText(getContext(), 
                                    "File uploaded successfully!", 
                                    android.widget.Toast.LENGTH_SHORT).show();
                                taskViewModel.loadTaskAttachments(taskId);
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        android.util.Log.e("AttachmentsFragment", "Upload error: " + error);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                android.widget.Toast.makeText(getContext(), 
                                    "Upload failed: " + error, 
                                    android.widget.Toast.LENGTH_LONG).show();
                            });
                        }
                    }
                });
            }
        }
    }

    private void setupViewModelAndUploader() {
        TaskApiService taskApi = ApiClient.get(App.authManager).create(TaskApiService.class);
        AttachmentApiService attachmentApi = ApiClient.get(App.authManager).create(AttachmentApiService.class);
        CommentApiService commentApi = ApiClient.get(App.authManager).create(CommentApiService.class);
        ITaskRepository repository = new TaskRepositoryImpl(taskApi, commentApi, attachmentApi);

        // Build minimal factory like CardDetailActivity
        com.example.tralalero.domain.usecase.task.GetTaskByIdUseCase getTaskByIdUseCase = new com.example.tralalero.domain.usecase.task.GetTaskByIdUseCase(repository);
        com.example.tralalero.domain.usecase.task.GetTasksByBoardUseCase getTasksByBoardUseCase = new com.example.tralalero.domain.usecase.task.GetTasksByBoardUseCase(repository);
        com.example.tralalero.domain.usecase.task.CreateTaskUseCase createTaskUseCase = new com.example.tralalero.domain.usecase.task.CreateTaskUseCase(repository);
        com.example.tralalero.domain.usecase.task.UpdateTaskUseCase updateTaskUseCase = new com.example.tralalero.domain.usecase.task.UpdateTaskUseCase(repository);
        com.example.tralalero.domain.usecase.task.DeleteTaskUseCase deleteTaskUseCase = new com.example.tralalero.domain.usecase.task.DeleteTaskUseCase(repository);
        com.example.tralalero.domain.usecase.task.AssignTaskUseCase assignTaskUseCase = new com.example.tralalero.domain.usecase.task.AssignTaskUseCase(repository);
        com.example.tralalero.domain.usecase.task.UnassignTaskUseCase unassignTaskUseCase = new com.example.tralalero.domain.usecase.task.UnassignTaskUseCase(repository);
        com.example.tralalero.domain.usecase.task.MoveTaskToBoardUseCase moveTaskToBoardUseCase = new com.example.tralalero.domain.usecase.task.MoveTaskToBoardUseCase(repository);
        com.example.tralalero.domain.usecase.task.UpdateTaskPositionUseCase updateTaskPositionUseCase = new com.example.tralalero.domain.usecase.task.UpdateTaskPositionUseCase(repository);
        com.example.tralalero.domain.usecase.task.AddCommentUseCase addCommentUseCase = new com.example.tralalero.domain.usecase.task.AddCommentUseCase(repository);
        com.example.tralalero.domain.usecase.task.GetTaskCommentsUseCase getTaskCommentsUseCase = new com.example.tralalero.domain.usecase.task.GetTaskCommentsUseCase(repository);
        AddAttachmentUseCase addAttachmentUseCase = new AddAttachmentUseCase(repository);
        GetTaskAttachmentsUseCase getTaskAttachmentsUseCase = new GetTaskAttachmentsUseCase(repository);
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

        // Attachment uploader uses AttachmentApiService and context
        uploader = new AttachmentUploader(attachmentApi, requireContext());
    }
}
