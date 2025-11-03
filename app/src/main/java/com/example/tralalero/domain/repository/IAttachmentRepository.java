package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.Attachment;

import java.util.List;

public interface IAttachmentRepository {
    void getAttachments(String taskId, ITaskRepository.RepositoryCallback<List<Attachment>> callback);

    void addAttachment(String taskId, Attachment attachment, ITaskRepository.RepositoryCallback<Attachment> callback);

    void deleteAttachment(String attachmentId, ITaskRepository.RepositoryCallback<Void> callback);
}
