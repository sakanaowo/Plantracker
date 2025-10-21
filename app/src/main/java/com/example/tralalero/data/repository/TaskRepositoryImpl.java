package com.example.tralalero.data.repository;

import com.example.tralalero.data.mapper.AttachmentMapper;
import com.example.tralalero.data.mapper.ChecklistMapper;
import com.example.tralalero.data.mapper.TaskCommentMapper;
import com.example.tralalero.data.mapper.TaskMapper;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.remote.dto.task.AttachmentDTO;
import com.example.tralalero.data.remote.dto.task.CheckListDTO;
import com.example.tralalero.data.remote.dto.task.TaskCommentDTO;
import com.example.tralalero.data.remote.dto.task.TaskDTO;
import com.example.tralalero.domain.model.Attachment;
import com.example.tralalero.domain.model.Checklist;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.model.TaskComment;
import com.example.tralalero.domain.repository.ITaskRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskRepositoryImpl implements ITaskRepository {
    private final TaskApiService apiService;

    public TaskRepositoryImpl(TaskApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void getTaskById(String taskId, RepositoryCallback<Task> callback) {
        apiService.getTaskById(taskId).enqueue(new Callback<TaskDTO>() {
            @Override
            public void onResponse(Call<TaskDTO> call, Response<TaskDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(TaskMapper.toDomain(response.body()));
                } else {
                    callback.onError("Task not found: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TaskDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getTasksByBoard(String boardId, RepositoryCallback<List<Task>> callback) {
        apiService.getTasksByBoard(boardId).enqueue(new Callback<List<TaskDTO>>() {
            @Override
            public void onResponse(Call<List<TaskDTO>> call, Response<List<TaskDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(TaskMapper.toDomainList(response.body()));
                } else {
                    callback.onError("Failed to fetch tasks: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<TaskDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getTasksByProject(String projectId, RepositoryCallback<List<Task>> callback) {
        callback.onError("Get tasks by project not yet implemented in API");
    }

    /**
     * Get all quick tasks from user's default board (To Do board)
     * Backend finds user's personal workspace, default project, and "To Do" board
     * 
     * @param callback Callback to receive list of quick tasks or error
     */
    @Override
    public void getQuickTasks(RepositoryCallback<List<Task>> callback) {
        android.util.Log.d("TaskRepositoryImpl", "Getting quick tasks from default board");
        
        apiService.getQuickTasks().enqueue(new Callback<List<TaskDTO>>() {
            @Override
            public void onResponse(Call<List<TaskDTO>> call, Response<List<TaskDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Task> tasks = new ArrayList<>();
                    for (TaskDTO dto : response.body()) {
                        tasks.add(TaskMapper.toDomain(dto));
                    }
                    android.util.Log.d("TaskRepositoryImpl", "✓ Loaded " + tasks.size() + " quick tasks");
                    callback.onSuccess(tasks);
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorBody = e.getMessage();
                    }
                    android.util.Log.e("TaskRepositoryImpl", "Failed to get quick tasks: " + response.code());
                    android.util.Log.e("TaskRepositoryImpl", "Error body: " + errorBody);
                    callback.onError("Failed to get quick tasks: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<TaskDTO>> call, Throwable t) {
                android.util.Log.e("TaskRepositoryImpl", "Network error getting quick tasks", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void createTask(String boardId, Task task, RepositoryCallback<Task> callback) {
        // Use toDtoForCreate() which only sends required fields: projectId, boardId, title, assigneeId
        TaskDTO dto = TaskMapper.toDtoForCreate(task);

        // Debug logging
        android.util.Log.d("TaskRepositoryImpl", "Creating task:");
        android.util.Log.d("TaskRepositoryImpl", "  task.title: " + task.getTitle());
        android.util.Log.d("TaskRepositoryImpl", "  task.projectId: " + task.getProjectId());
        android.util.Log.d("TaskRepositoryImpl", "  task.boardId: " + task.getBoardId());
        android.util.Log.d("TaskRepositoryImpl", "  task.position (original): " + task.getPosition());
        android.util.Log.d("TaskRepositoryImpl", "  DTO.projectId: " + dto.getProjectId());
        android.util.Log.d("TaskRepositoryImpl", "  DTO.boardId: " + dto.getBoardId());
        android.util.Log.d("TaskRepositoryImpl", "  DTO.title: " + dto.getTitle());
        android.util.Log.d("TaskRepositoryImpl", "  DTO.position: " + dto.getPosition());

        apiService.createTask(dto).enqueue(new Callback<TaskDTO>() {
            @Override
            public void onResponse(Call<TaskDTO> call, Response<TaskDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("TaskRepositoryImpl", "✓ Task created successfully: " + response.body().getId());
                    callback.onSuccess(TaskMapper.toDomain(response.body()));
                } else {
                    // Get error body for more details
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorBody = e.getMessage();
                    }
                    
                    android.util.Log.e("TaskRepositoryImpl", "Failed to create task: " + response.code());
                    android.util.Log.e("TaskRepositoryImpl", "Error body: " + errorBody);
                    android.util.Log.e("TaskRepositoryImpl", "Request URL: " + call.request().url());
                    
                    callback.onError("Failed to create task: " + response.code() + " - " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<TaskDTO> call, Throwable t) {
                android.util.Log.e("TaskRepositoryImpl", "Network error creating task", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Create a quick task - automatically assigns to default project/board
     * Backend finds user's personal workspace, default project, and "To Do" board
     * 
     * @param title Task title
     * @param description Optional task description
     * @param callback Callback to receive created task or error
     */
    public void createQuickTask(String title, String description, RepositoryCallback<Task> callback) {
        java.util.Map<String, String> quickTaskData = new java.util.HashMap<>();
        quickTaskData.put("title", title);
        if (description != null && !description.isEmpty()) {
            quickTaskData.put("description", description);
        }

        android.util.Log.d("TaskRepositoryImpl", "Creating quick task:");
        android.util.Log.d("TaskRepositoryImpl", "  title: " + title);
        android.util.Log.d("TaskRepositoryImpl", "  description: " + description);

        apiService.createQuickTask(quickTaskData).enqueue(new Callback<TaskDTO>() {
            @Override
            public void onResponse(Call<TaskDTO> call, Response<TaskDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("TaskRepositoryImpl", "✓ Quick task created successfully: " + response.body().getId());
                    callback.onSuccess(TaskMapper.toDomain(response.body()));
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorBody = e.getMessage();
                    }
                    
                    android.util.Log.e("TaskRepositoryImpl", "Failed to create quick task: " + response.code());
                    android.util.Log.e("TaskRepositoryImpl", "Error body: " + errorBody);
                    
                    callback.onError("Failed to create quick task: " + response.code() + " - " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<TaskDTO> call, Throwable t) {
                android.util.Log.e("TaskRepositoryImpl", "Network error creating quick task", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void updateTask(String taskId, Task task, RepositoryCallback<Task> callback) {
        TaskDTO dto = TaskMapper.toDto(task);

        apiService.updateTask(taskId, dto).enqueue(new Callback<TaskDTO>() {
            @Override
            public void onResponse(Call<TaskDTO> call, Response<TaskDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(TaskMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to update task: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TaskDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void deleteTask(String taskId, RepositoryCallback<Void> callback) {
        apiService.deleteTask(taskId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to delete task: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void moveTaskToBoard(String taskId, String targetBoardId, double position, RepositoryCallback<Task> callback) {
        TaskDTO dto = new TaskDTO();
        dto.setBoardId(targetBoardId);
        dto.setPosition(position);

        apiService.updateTask(taskId, dto).enqueue(new Callback<TaskDTO>() {
            @Override
            public void onResponse(Call<TaskDTO> call, Response<TaskDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(TaskMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to move task: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TaskDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void updateTaskPosition(String taskId, double newPosition, RepositoryCallback<Task> callback) {
        TaskDTO dto = new TaskDTO();
        dto.setPosition(newPosition);

        apiService.updateTask(taskId, dto).enqueue(new Callback<TaskDTO>() {
            @Override
            public void onResponse(Call<TaskDTO> call, Response<TaskDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(TaskMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to update position: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TaskDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void assignTask(String taskId, String userId, RepositoryCallback<Task> callback) {
        TaskDTO dto = new TaskDTO();
        dto.setAssigneeId(userId);

        apiService.updateTask(taskId, dto).enqueue(new Callback<TaskDTO>() {
            @Override
            public void onResponse(Call<TaskDTO> call, Response<TaskDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(TaskMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to assign task: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TaskDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void unassignTask(String taskId, RepositoryCallback<Task> callback) {
        TaskDTO dto = new TaskDTO();
        dto.setAssigneeId(null);

        apiService.updateTask(taskId, dto).enqueue(new Callback<TaskDTO>() {
            @Override
            public void onResponse(Call<TaskDTO> call, Response<TaskDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(TaskMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to unassign task: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TaskDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getAttachments(String taskId, RepositoryCallback<List<Attachment>> callback) {
        apiService.getTaskAttachments(taskId).enqueue(new Callback<List<AttachmentDTO>>() {
            @Override
            public void onResponse(Call<List<AttachmentDTO>> call, Response<List<AttachmentDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(AttachmentMapper.toDomainList(response.body()));
                } else {
                    callback.onError("Failed to fetch attachments: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<AttachmentDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void addAttachment(String taskId, Attachment attachment, RepositoryCallback<Attachment> callback) {
        AttachmentDTO dto = AttachmentMapper.toDto(attachment);

        apiService.addTaskAttachment(taskId, dto).enqueue(new Callback<AttachmentDTO>() {
            @Override
            public void onResponse(Call<AttachmentDTO> call, Response<AttachmentDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(AttachmentMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to add attachment: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AttachmentDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void deleteAttachment(String attachmentId, RepositoryCallback<Void> callback) {
        callback.onError("Delete attachment not yet implemented in API");
    }

    @Override
    public void getComments(String taskId, RepositoryCallback<List<TaskComment>> callback) {
        apiService.getTaskComments(taskId).enqueue(new Callback<List<TaskCommentDTO>>() {
            @Override
            public void onResponse(Call<List<TaskCommentDTO>> call, Response<List<TaskCommentDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(TaskCommentMapper.toDomainList(response.body()));
                } else {
                    callback.onError("Failed to fetch comments: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<TaskCommentDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void addComment(String taskId, TaskComment comment, RepositoryCallback<TaskComment> callback) {
        TaskCommentDTO dto = TaskCommentMapper.toDto(comment);

        apiService.addTaskComment(taskId, dto).enqueue(new Callback<TaskCommentDTO>() {
            @Override
            public void onResponse(Call<TaskCommentDTO> call, Response<TaskCommentDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(TaskCommentMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to add comment: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TaskCommentDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void updateComment(String commentId, String body, RepositoryCallback<TaskComment> callback) {
        callback.onError("Update comment not yet implemented in API");
    }

    @Override
    public void deleteComment(String commentId, RepositoryCallback<Void> callback) {
        callback.onError("Delete comment not yet implemented in API");
    }

    @Override
    public void getChecklists(String taskId, RepositoryCallback<List<Checklist>> callback) {
        apiService.getTaskChecklists(taskId).enqueue(new Callback<List<CheckListDTO>>() {
            @Override
            public void onResponse(Call<List<CheckListDTO>> call, Response<List<CheckListDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(ChecklistMapper.toDomainList(response.body()));
                } else {
                    callback.onError("Failed to fetch checklists: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<CheckListDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void addChecklist(String taskId, Checklist checklist, RepositoryCallback<Checklist> callback) {
        callback.onError("Add checklist not yet implemented in API");
    }

    @Override
    public void updateChecklist(String checklistId, Checklist checklist, RepositoryCallback<Checklist> callback) {
        callback.onError("Update checklist not yet implemented in API");
    }

    @Override
    public void deleteChecklist(String checklistId, RepositoryCallback<Void> callback) {
        callback.onError("Delete checklist not yet implemented in API");
    }

    @Override
    public void addLabel(String taskId, String labelId, RepositoryCallback<Void> callback) {
        callback.onError("Add label not yet implemented in API");
    }

    @Override
    public void removeLabel(String taskId, String labelId, RepositoryCallback<Void> callback) {
        callback.onError("Remove label not yet implemented in API");
    }
}
