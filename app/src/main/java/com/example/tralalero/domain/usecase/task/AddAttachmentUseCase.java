package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.model.Attachment;
import com.example.tralalero.domain.repository.ITaskRepository;

import java.util.Arrays;
import java.util.List;

/**
 * UseCase: Add attachment to task
 *
 * Input: String taskId, Attachment attachment
 * Output: Attachment (created)
 *
 * Business Logic:
 * - Validate file size < 10MB
 * - Validate mime type (image, pdf, doc)
 * - Validate URL is not empty
 */
public class AddAttachmentUseCase {

    private final ITaskRepository taskRepository;

    // Constants
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain"
    );

    public AddAttachmentUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Add attachment to task
     *
     * @param taskId Task ID (UUID format)
     * @param attachment Attachment to add
     * @param callback Callback to receive result
     */
    public void execute(String taskId, Attachment attachment, Callback<Attachment> callback) {
        // Validate task ID
        if (taskId == null || taskId.trim().isEmpty()) {
            callback.onError("Task ID cannot be empty");
            return;
        }

        if (!isValidUUID(taskId)) {
            callback.onError("Invalid task ID format");
            return;
        }

        // Validate attachment
        if (attachment == null) {
            callback.onError("Attachment cannot be null");
            return;
        }

        // Validate URL (required field)
        if (attachment.getUrl() == null || attachment.getUrl().trim().isEmpty()) {
            callback.onError("File URL cannot be empty");
            return;
        }

        // Validate URL format (basic check)
        if (!isValidUrl(attachment.getUrl())) {
            callback.onError("Invalid file URL format");
            return;
        }

        // Validate file size if provided
        if (attachment.getSize() != null && attachment.getSize() > MAX_FILE_SIZE) {
            callback.onError("File size cannot exceed 10MB");
            return;
        }

        // Validate mime type if provided
        if (attachment.getMimeType() != null && !isAllowedMimeType(attachment.getMimeType())) {
            callback.onError("File type not allowed. Allowed types: images, PDF, Word, Excel, PowerPoint, Text");
            return;
        }

        // Call repository
        taskRepository.addAttachment(taskId, attachment,
                new ITaskRepository.RepositoryCallback<Attachment>() {
                    @Override
                    public void onSuccess(Attachment result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                }
        );
    }

    /**
     * Check if mime type is allowed
     */
    private boolean isAllowedMimeType(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return false;
        }
        return ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase());
    }

    /**
     * Validate URL format (basic check)
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        // Basic URL validation
        return url.startsWith("http://") ||
                url.startsWith("https://") ||
                url.startsWith("file://");
    }

    /**
     * Validate UUID format
     */
    private boolean isValidUUID(String uuid) {
        return uuid.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}