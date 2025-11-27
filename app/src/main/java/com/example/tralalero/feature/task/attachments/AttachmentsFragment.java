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
                handleAttachmentDownload(attachment);
            }

            @Override
            public void onDeleteClick(Attachment attachment) {
                handleAttachmentDelete(attachment);
            }

            @Override
            public void onAttachmentClick(Attachment attachment) {
                handleAttachmentClick(attachment);
            }
        });
        rv.setAdapter(adapter);

        Button btnPick = view.findViewById(R.id.btnPickFileFragment);
        TextView tvEmpty = view.findViewById(R.id.tvNoAttachmentsFragment);
        
        // Upload progress views
        View layoutUploadProgress = view.findViewById(R.id.layoutUploadProgress);
        TextView tvUploadProgress = view.findViewById(R.id.tvUploadProgress);
        android.widget.ProgressBar progressBarUpload = view.findViewById(R.id.progressBarUpload);

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
                // Get progress views from the current view
                View currentView = getView();
                if (currentView != null) {
                    View layoutUploadProgress = currentView.findViewById(R.id.layoutUploadProgress);
                    TextView tvUploadProgress = currentView.findViewById(R.id.tvUploadProgress);
                    android.widget.ProgressBar progressBarUpload = currentView.findViewById(R.id.progressBarUpload);
                    
                    // Show progress UI
                    layoutUploadProgress.setVisibility(View.VISIBLE);
                    progressBarUpload.setProgress(0);
                    
                    uploader.uploadFile(taskId, uri, new AttachmentUploader.UploadCallback() {
                        @Override
                        public void onProgress(int percent) {
                            android.util.Log.d("AttachmentsFragment", "Upload progress: " + percent + "%");
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    progressBarUpload.setProgress(percent);
                                    tvUploadProgress.setText("Uploading... " + percent + "%");
                                });
                            }
                        }

                        @Override
                        public void onSuccess(com.example.tralalero.data.remote.dto.task.AttachmentDTO attachmentDTO) {
                            android.util.Log.d("AttachmentsFragment", "Upload success! File: " + attachmentDTO.fileName);
                            // ✅ Just reload attachments from server (which has full data including URL)
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    // Hide progress UI
                                    layoutUploadProgress.setVisibility(View.GONE);
                                    progressBarUpload.setProgress(0);
                                    
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
                                    // Hide progress UI
                                    layoutUploadProgress.setVisibility(View.GONE);
                                    progressBarUpload.setProgress(0);
                                    
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
        com.example.tralalero.domain.usecase.task.CreateQuickTaskUseCase createQuickTaskUseCase = new com.example.tralalero.domain.usecase.task.CreateQuickTaskUseCase(repository);
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

        // Attachment uploader uses AttachmentApiService and context
        uploader = new AttachmentUploader(attachmentApi, requireContext());
    }
    
    /**
     * Handle attachment click - preview image or open other files
     */
    private void handleAttachmentClick(Attachment attachment) {
        String fileName = attachment.getFileName();
        if (fileName == null || fileName.isEmpty()) {
            android.widget.Toast.makeText(getContext(), "Invalid file name", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if it's an image
        String mimeType = attachment.getMimeType();
        boolean isImage = mimeType != null && mimeType.startsWith("image/");
        
        // Get signed URL from backend
        taskViewModel.getAttachmentViewUrl(attachment.getId(), new com.example.tralalero.presentation.viewmodel.TaskViewModel.AttachmentViewUrlCallback() {
            @Override
            public void onSuccess(String viewUrl) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (isImage) {
                            showImagePreview(viewUrl, fileName);
                        } else {
                            openWithIntent(viewUrl, fileName);
                        }
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        android.widget.Toast.makeText(getContext(), "Failed to get file URL: " + error, android.widget.Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    
    /**
     * Show image preview dialog
     */
    private void showImagePreview(String imageUrl, String fileName) {
        if (getActivity() == null) return;
        
        ImagePreviewDialog dialog = new ImagePreviewDialog(getActivity(), imageUrl, fileName);
        dialog.setOnDownloadClickListener(() -> {
            performDownload(imageUrl, fileName);
        });
        dialog.show();
    }
    
    /**
     * Open file with system intent
     */
    private void openWithIntent(String url, String fileName) {
        if (getActivity() == null) return;
        
        try {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            android.net.Uri uri = android.net.Uri.parse(url);
            String mimeType = getMimeType(fileName);
            intent.setDataAndType(uri, mimeType);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            android.widget.Toast.makeText(getContext(), "Cannot open file: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Handle attachment download
     */
    private void handleAttachmentDownload(Attachment attachment) {
        taskViewModel.getAttachmentViewUrl(attachment.getId(), new com.example.tralalero.presentation.viewmodel.TaskViewModel.AttachmentViewUrlCallback() {
            @Override
            public void onSuccess(String viewUrl) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        performDownload(viewUrl, attachment.getFileName());
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        android.widget.Toast.makeText(getContext(), "Failed to download: " + error, android.widget.Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    
    /**
     * Perform actual download
     */
    private void performDownload(String url, String fileName) {
        if (getActivity() == null) return;
        
        try {
            android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(android.net.Uri.parse(url));
            request.setTitle(fileName);
            request.setDescription("Downloading attachment");
            request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, fileName);
            
            android.app.DownloadManager dm = (android.app.DownloadManager) getActivity().getSystemService(android.content.Context.DOWNLOAD_SERVICE);
            if (dm != null) {
                dm.enqueue(request);
                android.widget.Toast.makeText(getContext(), "Downloading " + fileName, android.widget.Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            android.widget.Toast.makeText(getContext(), "Download failed: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Handle attachment delete
     */
    private void handleAttachmentDelete(Attachment attachment) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Attachment")
            .setMessage("Are you sure you want to delete '" + attachment.getFileName() + "'?")
            .setPositiveButton("Delete", (dialog, which) -> {
                taskViewModel.deleteAttachment(attachment.getId(), taskId);
                android.widget.Toast.makeText(getContext(), "Attachment deleted", android.widget.Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    /**
     * Get MIME type from file name
     */
    private String getMimeType(String fileName) {
        if (fileName == null || fileName.isEmpty()) return "*/*";
        
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot <= 0 || lastDot >= fileName.length() - 1) return "*/*";
        
        String ext = fileName.substring(lastDot + 1).toLowerCase();
        
        switch (ext) {
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "pdf": return "application/pdf";
            case "doc":
            case "docx": return "application/msword";
            case "xls":
            case "xlsx": return "application/vnd.ms-excel";
            case "txt": return "text/plain";
            default: return "*/*";
        }
    }
}
