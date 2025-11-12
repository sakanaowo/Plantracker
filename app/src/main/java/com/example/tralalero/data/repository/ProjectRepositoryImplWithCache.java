package com.example.tralalero.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.tralalero.data.local.database.dao.ProjectDao;
import com.example.tralalero.data.local.database.entity.ProjectEntity;
import com.example.tralalero.data.mapper.DtoToEntityMapper;
import com.example.tralalero.data.mapper.ProjectEntityMapper;
import com.example.tralalero.data.mapper.ProjectMapper;
import com.example.tralalero.data.remote.api.ProjectApiService;
import com.example.tralalero.data.remote.dto.project.ProjectDTO;
import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IProjectRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Project Repository with Room Database caching
 * Pattern: Cache-first with silent background refresh
 */
public class ProjectRepositoryImplWithCache implements IProjectRepository {
    private static final String TAG = "ProjectRepositoryCache";
    
    private final ProjectApiService apiService;
    private final ProjectDao projectDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    
    public ProjectRepositoryImplWithCache(ProjectApiService apiService, ProjectDao projectDao) {
        this.apiService = apiService;
        this.projectDao = projectDao;
        this.executorService = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "ProjectRepositoryImplWithCache initialized");
    }
    
    // ==================== GET PROJECT BY ID ====================
    
    @Override
    public void getProjectById(String projectId, RepositoryCallback<Project> callback) {
        // Input validation
        if (projectId == null || projectId.trim().isEmpty()) {
            Log.e(TAG, "Invalid projectId");
            if (callback != null) {
                mainHandler.post(() -> callback.onError("Invalid project ID"));
            }
            return;
        }
        
        if (callback == null) {
            Log.e(TAG, "Callback is null for getProjectById");
            return;
        }
        
        executorService.execute(() -> {
            try {
                // 1. Return from cache immediately
                ProjectEntity cached = projectDao.getProjectByIdSync(projectId);
                if (cached != null) {
                    Project cachedProject = ProjectEntityMapper.toDomain(cached);
                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onSuccess(cachedProject);
                        }
                    });
                    Log.d(TAG, "✓ Returned cached project: " + projectId);
                }
                
                // 2. Fetch from network in background
                fetchProjectFromNetwork(projectId, callback, cached == null);
            } catch (Exception e) {
                Log.e(TAG, "✗ Error in getProjectById: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError("Error getting project: " + e.getMessage());
                    }
                });
            }
        });
    }
    
    private void fetchProjectFromNetwork(String projectId, RepositoryCallback<Project> callback, boolean isFirstLoad) {
        apiService.getProjectById(projectId).enqueue(new Callback<ProjectDTO>() {
            @Override
            public void onResponse(Call<ProjectDTO> call, Response<ProjectDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        ProjectDTO dto = response.body();
                        Project project = ProjectMapper.toDomain(dto);

                        // FIXED: Cache using DtoToEntityMapper to preserve issueSeq, createdAt, updatedAt
                        executorService.execute(() -> {
                            try {
                                projectDao.insertProject(DtoToEntityMapper.projectDtoToEntity(dto));
                                Log.d(TAG, "✓ Cached project from network with full data: " + projectId);
                            } catch (Exception e) {
                                Log.e(TAG, "Error caching project", e);
                            }
                        });
                        
                        // Only callback if first load (no cache)
                        if (isFirstLoad && callback != null) {
                            mainHandler.post(() -> callback.onSuccess(project));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing project response", e);
                        if (isFirstLoad && callback != null) {
                            mainHandler.post(() -> callback.onError("Error processing response: " + e.getMessage()));
                        }
                    }
                } else if (isFirstLoad && callback != null) {
                    mainHandler.post(() -> callback.onError("Project not found: " + response.code()));
                }
            }
            
            @Override
            public void onFailure(Call<ProjectDTO> call, Throwable t) {
                if (isFirstLoad && callback != null) {
                    mainHandler.post(() -> callback.onError("Network error: " + t.getMessage()));
                }
            }
        });
    }
    
    // ==================== CREATE PROJECT ====================
    
    @Override
    public void createProject(String workspaceId, Project project, RepositoryCallback<Project> callback) {
        // Input validation
        if (workspaceId == null || workspaceId.trim().isEmpty()) {
            if (callback != null) {
                mainHandler.post(() -> callback.onError("workspaceId cannot be null or empty"));
            }
            return;
        }
        
        if (project == null) {
            if (callback != null) {
                mainHandler.post(() -> callback.onError("Project cannot be null"));
            }
            return;
        }
        
        if (callback == null) {
            Log.e(TAG, "createProject: callback is null");
            return;
        }
        
        try {
            ProjectDTO dto = ProjectMapper.toDto(project);
            dto.setWorkspaceId(workspaceId);
            
            apiService.createProject(dto).enqueue(new Callback<ProjectDTO>() {
                @Override
                public void onResponse(Call<ProjectDTO> call, Response<ProjectDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            Project newProject = ProjectMapper.toDomain(response.body());

                            // Cache immediately
                            executorService.execute(() -> {
                                try {
                                    projectDao.insertProject(ProjectEntityMapper.toEntity(newProject));
                                    Log.d(TAG, "✓ Cached new project: " + newProject.getId());
                                } catch (Exception e) {
                                    Log.e(TAG, "Error caching new project", e);
                                }
                            });
                            
                            // Callback on main thread
                            mainHandler.post(() -> callback.onSuccess(newProject));
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing create response", e);
                            mainHandler.post(() -> callback.onError("Failed to process response: " + e.getMessage()));
                        }
                    } else {
                        mainHandler.post(() -> callback.onError("Failed to create project: " + response.code()));
                    }
                }
                
                @Override
                public void onFailure(Call<ProjectDTO> call, Throwable t) {
                    mainHandler.post(() -> callback.onError("Network error: " + t.getMessage()));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating project", e);
            mainHandler.post(() -> callback.onError("Error creating project: " + e.getMessage()));
        }
    }
    
    // ==================== UPDATE PROJECT ====================
    
    @Override
    public void updateProject(String projectId, Project project, RepositoryCallback<Project> callback) {
        // Input validation
        if (projectId == null || projectId.trim().isEmpty()) {
            if (callback != null) {
                mainHandler.post(() -> callback.onError("projectId cannot be null or empty"));
            }
            return;
        }
        
        if (project == null) {
            if (callback != null) {
                mainHandler.post(() -> callback.onError("Project cannot be null"));
            }
            return;
        }
        
        if (callback == null) {
            Log.e(TAG, "updateProject: callback is null");
            return;
        }
        
        try {
            ProjectDTO dto = ProjectMapper.toDto(project);
            
            apiService.updateProject(projectId, dto).enqueue(new Callback<ProjectDTO>() {
                @Override
                public void onResponse(Call<ProjectDTO> call, Response<ProjectDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            ProjectDTO responseDto = response.body();
                            Project updatedProject = ProjectMapper.toDomain(responseDto);

                            // FIXED: Update cache using DtoToEntityMapper to preserve issueSeq, createdAt, updatedAt
                            executorService.execute(() -> {
                                try {
                                    projectDao.updateProject(DtoToEntityMapper.projectDtoToEntity(responseDto));
                                    Log.d(TAG, "✓ Updated cached project with full data: " + projectId);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error updating cache", e);
                                }
                            });
                            
                            // Callback on main thread
                            mainHandler.post(() -> callback.onSuccess(updatedProject));
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing update response", e);
                            mainHandler.post(() -> callback.onError("Failed to process response: " + e.getMessage()));
                        }
                    } else {
                        mainHandler.post(() -> callback.onError("Failed to update project: " + response.code()));
                    }
                }
                
                @Override
                public void onFailure(Call<ProjectDTO> call, Throwable t) {
                    mainHandler.post(() -> callback.onError("Network error: " + t.getMessage()));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error updating project", e);
            mainHandler.post(() -> callback.onError("Error updating project: " + e.getMessage()));
        }
    }
    
    // ==================== DELETE PROJECT ====================
    
    @Override
    public void deleteProject(String projectId, RepositoryCallback<Void> callback) {
        // Input validation
        if (projectId == null || projectId.trim().isEmpty()) {
            if (callback != null) {
                mainHandler.post(() -> callback.onError("projectId cannot be null or empty"));
            }
            return;
        }
        
        if (callback == null) {
            Log.e(TAG, "deleteProject: callback is null");
            return;
        }
        
        try {
            apiService.deleteProject(projectId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        // Delete from cache
                        executorService.execute(() -> {
                            try {
                                projectDao.deleteProjectById(projectId);
                                Log.d(TAG, "✓ Deleted cached project: " + projectId);
                            } catch (Exception e) {
                                Log.e(TAG, "Error deleting from cache", e);
                            }
                        });
                        
                        // Callback on main thread
                        mainHandler.post(() -> callback.onSuccess(null));
                    } else {
                        mainHandler.post(() -> callback.onError("Failed to delete project: " + response.code()));
                    }
                }
                
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    mainHandler.post(() -> callback.onError("Network error: " + t.getMessage()));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error deleting project", e);
            mainHandler.post(() -> callback.onError("Error deleting project: " + e.getMessage()));
        }
    }
    
    // ==================== UPDATE PROJECT KEY ====================
    
    @Override
    public void updateProjectKey(String projectId, String newKey, RepositoryCallback<Project> callback) {
        // Input validation
        if (projectId == null || projectId.trim().isEmpty()) {
            if (callback != null) {
                mainHandler.post(() -> callback.onError("projectId cannot be null or empty"));
            }
            return;
        }
        
        if (newKey == null || newKey.trim().isEmpty()) {
            if (callback != null) {
                mainHandler.post(() -> callback.onError("newKey cannot be null or empty"));
            }
            return;
        }
        
        if (callback == null) {
            Log.e(TAG, "updateProjectKey: callback is null");
            return;
        }
        
        try {
            ProjectDTO dto = new ProjectDTO();
            dto.setKey(newKey);
            
            apiService.updateProject(projectId, dto).enqueue(new Callback<ProjectDTO>() {
                @Override
                public void onResponse(Call<ProjectDTO> call, Response<ProjectDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            Project updatedProject = ProjectMapper.toDomain(response.body());

                            // Update cache
                            executorService.execute(() -> {
                                try {
                                    projectDao.updateProject(ProjectEntityMapper.toEntity(updatedProject));
                                    Log.d(TAG, "✓ Updated project key in cache: " + projectId);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error updating cache", e);
                                }
                            });
                            
                            mainHandler.post(() -> callback.onSuccess(updatedProject));
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing response", e);
                            mainHandler.post(() -> callback.onError("Failed to process response: " + e.getMessage()));
                        }
                    } else {
                        mainHandler.post(() -> callback.onError("Failed to update project key: " + response.code()));
                    }
                }
                
                @Override
                public void onFailure(Call<ProjectDTO> call, Throwable t) {
                    mainHandler.post(() -> callback.onError("Network error: " + t.getMessage()));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error updating project key", e);
            mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
        }
    }
    
    // ==================== UPDATE BOARD TYPE ====================
    
    @Override
    public void updateBoardType(String projectId, String boardType, RepositoryCallback<Project> callback) {
        // Input validation
        if (projectId == null || projectId.trim().isEmpty()) {
            if (callback != null) {
                mainHandler.post(() -> callback.onError("projectId cannot be null or empty"));
            }
            return;
        }
        
        if (boardType == null || boardType.trim().isEmpty()) {
            if (callback != null) {
                mainHandler.post(() -> callback.onError("boardType cannot be null or empty"));
            }
            return;
        }
        
        if (callback == null) {
            Log.e(TAG, "updateBoardType: callback is null");
            return;
        }
        
        try {
            ProjectDTO dto = new ProjectDTO();
            dto.setBoardType(boardType);
            
            apiService.updateProject(projectId, dto).enqueue(new Callback<ProjectDTO>() {
                @Override
                public void onResponse(Call<ProjectDTO> call, Response<ProjectDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            ProjectDTO responseDto = response.body();
                            Project updatedProject = ProjectMapper.toDomain(responseDto);

                            // FIXED: Update cache using DtoToEntityMapper
                            executorService.execute(() -> {
                                try {
                                    projectDao.updateProject(DtoToEntityMapper.projectDtoToEntity(responseDto));
                                    Log.d(TAG, "✓ Updated board type in cache with full data: " + projectId);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error updating cache", e);
                                }
                            });
                            
                            mainHandler.post(() -> callback.onSuccess(updatedProject));
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing response", e);
                            mainHandler.post(() -> callback.onError("Failed to process response: " + e.getMessage()));
                        }
                    } else {
                        mainHandler.post(() -> callback.onError("Failed to update board type: " + response.code()));
                    }
                }
                
                @Override
                public void onFailure(Call<ProjectDTO> call, Throwable t) {
                    mainHandler.post(() -> callback.onError("Network error: " + t.getMessage()));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error updating board type", e);
            mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
        }
    }
    
    // ==================== GET PROJECT SUMMARY ====================
    
    @Override
    public void getProjectSummary(String projectId, RepositoryCallback<com.example.tralalero.data.dto.project.ProjectSummaryResponse> callback) {
        // Input validation
        if (projectId == null || projectId.trim().isEmpty()) {
            if (callback != null) {
                mainHandler.post(() -> callback.onError("projectId cannot be null or empty"));
            }
            return;
        }
        
        if (callback == null) {
            Log.e(TAG, "getProjectSummary: callback is null");
            return;
        }
        
        try {
            apiService.getProjectSummary(projectId).enqueue(new Callback<com.example.tralalero.data.dto.project.ProjectSummaryResponse>() {
                @Override
                public void onResponse(Call<com.example.tralalero.data.dto.project.ProjectSummaryResponse> call, 
                                     Response<com.example.tralalero.data.dto.project.ProjectSummaryResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        mainHandler.post(() -> callback.onSuccess(response.body()));
                    } else {
                        mainHandler.post(() -> callback.onError("Failed to get project summary: " + response.code()));
                    }
                }

                @Override
                public void onFailure(Call<com.example.tralalero.data.dto.project.ProjectSummaryResponse> call, Throwable t) {
                    mainHandler.post(() -> callback.onError("Network error: " + t.getMessage()));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error getting project summary", e);
            mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
        }
    }
    
    // ==================== GET ALL USER PROJECTS ====================
    
    @Override
    public void getAllUserProjects(RepositoryCallback<List<Project>> callback) {
        if (callback == null) {
            Log.e(TAG, "getAllUserProjects: callback is null");
            return;
        }
        
        // This cached repository doesn't support getAllUserProjects yet
        // Delegate to API-only implementation
        try {
            apiService.getAllUserProjects().enqueue(new Callback<List<ProjectDTO>>() {
                @Override
                public void onResponse(Call<List<ProjectDTO>> call, Response<List<ProjectDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            List<Project> projects = new ArrayList<>();
                            for (ProjectDTO dto : response.body()) {
                                projects.add(ProjectMapper.toDomain(dto));
                            }
                            mainHandler.post(() -> callback.onSuccess(projects));
                            
                            // TODO: Cache all projects if needed
                            Log.d(TAG, "✓ Loaded " + projects.size() + " user projects from API");
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing projects", e);
                            mainHandler.post(() -> callback.onError("Error processing projects: " + e.getMessage()));
                        }
                    } else {
                        mainHandler.post(() -> callback.onError("Failed to get user projects: " + response.code()));
                    }
                }
                
                @Override
                public void onFailure(Call<List<ProjectDTO>> call, Throwable t) {
                    mainHandler.post(() -> callback.onError("Network error: " + t.getMessage()));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error getting all user projects", e);
            mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
        }
    }
    
    // ==================== CACHE MANAGEMENT ====================
    
    /**
     * Clear all cached projects
     */
    public void clearCache() {
        executorService.execute(() -> {
            projectDao.deleteAll();
            Log.d(TAG, "✓ Cache cleared");
        });
    }
    
    /**
     * Shutdown executor service gracefully
     * CRITICAL: Must be called when repository is no longer needed
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        Log.e(TAG, "ExecutorService did not terminate");
                    }
                }
                Log.d(TAG, "✓ ExecutorService shutdown complete");
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
                Log.e(TAG, "Shutdown interrupted", e);
            }
        }
    }
}
