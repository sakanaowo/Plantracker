package com.example.tralalero.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.usecase.board.GetBoardsByProjectUseCase;
import com.example.tralalero.domain.usecase.project.GetProjectByIdUseCase;
import com.example.tralalero.domain.usecase.project.CreateProjectUseCase;
import com.example.tralalero.domain.usecase.project.UpdateProjectUseCase;
import com.example.tralalero.domain.usecase.project.DeleteProjectUseCase;
import com.example.tralalero.domain.usecase.project.SwitchBoardTypeUseCase;
import com.example.tralalero.domain.usecase.project.UpdateProjectKeyUseCase;
import com.example.tralalero.domain.usecase.task.GetTasksByBoardUseCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ProjectViewModel extends ViewModel {
    private final GetProjectByIdUseCase getProjectByIdUseCase;
    private final CreateProjectUseCase createProjectUseCase;
    private final UpdateProjectUseCase updateProjectUseCase;
    private final DeleteProjectUseCase deleteProjectUseCase;
    private final SwitchBoardTypeUseCase switchBoardTypeUseCase;
    private final UpdateProjectKeyUseCase updateProjectKeyUseCase;
    private final GetBoardsByProjectUseCase getBoardsByProjectUseCase;
    private final GetTasksByBoardUseCase getTasksByBoardUseCase;
    
    private final MutableLiveData<Project> selectedProjectLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Board>> boardsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, List<Task>>> tasksPerBoardLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> selectedProjectIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> projectDeletedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> projectCreatedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> projectUpdatedLiveData = new MutableLiveData<>(false);

    public ProjectViewModel(
            GetProjectByIdUseCase getProjectByIdUseCase,
            CreateProjectUseCase createProjectUseCase,
            UpdateProjectUseCase updateProjectUseCase,
            DeleteProjectUseCase deleteProjectUseCase,
            SwitchBoardTypeUseCase switchBoardTypeUseCase,
            UpdateProjectKeyUseCase updateProjectKeyUseCase,
            GetBoardsByProjectUseCase getBoardsByProjectUseCase,
            GetTasksByBoardUseCase getTasksByBoardUseCase
    ) {
        this.getProjectByIdUseCase = getProjectByIdUseCase;
        this.createProjectUseCase = createProjectUseCase;
        this.updateProjectUseCase = updateProjectUseCase;
        this.deleteProjectUseCase = deleteProjectUseCase;
        this.switchBoardTypeUseCase = switchBoardTypeUseCase;
        this.updateProjectKeyUseCase = updateProjectKeyUseCase;
        this.getBoardsByProjectUseCase = getBoardsByProjectUseCase;
        this.getTasksByBoardUseCase = getTasksByBoardUseCase;
    }

    public LiveData<Project> getSelectedProject() {
        return selectedProjectLiveData;
    }

    public LiveData<List<Board>> getBoards() {
        return boardsLiveData;
    }

    public LiveData<Map<String, List<Task>>> getTasksPerBoard() {
        return tasksPerBoardLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<Boolean> isProjectDeleted() {
        return projectDeletedLiveData;
    }

    public LiveData<Boolean> isProjectCreated() {
        return projectCreatedLiveData;
    }

    public LiveData<Boolean> isProjectUpdated() {
        return projectUpdatedLiveData;
    }

    // ✅ Select project - auto-loads boards and tasks
    public void selectProject(String projectId) {
        if (projectId == null) return;
        
        selectedProjectIdLiveData.setValue(projectId);
        
        // Load project details
        loadProjectById(projectId);
        
        // Auto-load boards for this project
        loadBoardsForProject(projectId);
    }
    
    public void loadBoardsForProject(String projectId) {
        loadingLiveData.setValue(true);
        
        getBoardsByProjectUseCase.execute(projectId, new GetBoardsByProjectUseCase.Callback<List<Board>>() {
            @Override
            public void onSuccess(List<Board> boards) {
                boardsLiveData.setValue(boards);
                
                // Auto-load tasks for all boards
                if (boards != null && !boards.isEmpty()) {
                    loadTasksForAllBoards(boards);
                } else {
                    loadingLiveData.setValue(false);
                    tasksPerBoardLiveData.setValue(new HashMap<>());
                }
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }
    
    private void loadTasksForAllBoards(List<Board> boards) {
        Map<String, List<Task>> tasksMap = new HashMap<>();
        AtomicInteger pendingRequests = new AtomicInteger(boards.size());
        
        for (Board board : boards) {
            getTasksByBoardUseCase.execute(board.getId(), new GetTasksByBoardUseCase.Callback<List<Task>>() {
                @Override
                public void onSuccess(List<Task> tasks) {
                    synchronized (tasksMap) {
                        tasksMap.put(board.getId(), tasks != null ? tasks : new ArrayList<>());
                    }
                    
                    if (pendingRequests.decrementAndGet() == 0) {
                        // All tasks loaded
                        loadingLiveData.setValue(false);
                        tasksPerBoardLiveData.setValue(new HashMap<>(tasksMap));
                    }
                }

                @Override
                public void onError(String error) {
                    synchronized (tasksMap) {
                        tasksMap.put(board.getId(), new ArrayList<>());
                    }
                    
                    if (pendingRequests.decrementAndGet() == 0) {
                        loadingLiveData.setValue(false);
                        tasksPerBoardLiveData.setValue(new HashMap<>(tasksMap));
                    }
                }
            });
        }
    }

    public void loadProjectById(String projectId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getProjectByIdUseCase.execute(projectId, new GetProjectByIdUseCase.Callback<Project>() {
            @Override
            public void onSuccess(Project result) {
                loadingLiveData.setValue(false);
                selectedProjectLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void createProject(String workspaceId, Project project) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        projectCreatedLiveData.setValue(false);

        createProjectUseCase.execute(workspaceId, project, new CreateProjectUseCase.Callback<Project>() {
            @Override
            public void onSuccess(Project result) {
                loadingLiveData.setValue(false);
                selectedProjectLiveData.setValue(result);
                projectCreatedLiveData.setValue(true);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                projectCreatedLiveData.setValue(false);
            }
        });
    }

    public void updateProject(String projectId, Project project) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        projectUpdatedLiveData.setValue(false);

        updateProjectUseCase.execute(projectId, project, new UpdateProjectUseCase.Callback<Project>() {
            @Override
            public void onSuccess(Project result) {
                loadingLiveData.setValue(false);
                selectedProjectLiveData.setValue(result);
                projectUpdatedLiveData.setValue(true);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                projectUpdatedLiveData.setValue(false);
            }
        });
    }

    public void deleteProject(String projectId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        projectDeletedLiveData.setValue(false);

        deleteProjectUseCase.execute(projectId, new DeleteProjectUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                selectedProjectLiveData.setValue(null);
                projectDeletedLiveData.setValue(true);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                projectDeletedLiveData.setValue(false);
            }
        });
    }

    public void switchBoardType(String projectId, String newBoardType) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        switchBoardTypeUseCase.execute(projectId, newBoardType, new SwitchBoardTypeUseCase.Callback<Project>() {
            @Override
            public void onSuccess(Project result) {
                loadingLiveData.setValue(false);
                selectedProjectLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void updateProjectKey(String projectId, String newKey) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        updateProjectKeyUseCase.execute(projectId, newKey, new UpdateProjectKeyUseCase.Callback<Project>() {
            @Override
            public void onSuccess(Project result) {
                loadingLiveData.setValue(false);
                selectedProjectLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void clearError() {
        errorLiveData.setValue(null);
    }

    public void resetFlags() {
        projectCreatedLiveData.setValue(false);
        projectUpdatedLiveData.setValue(false);
        projectDeletedLiveData.setValue(false);
    }
    
    // ✅ Refresh specific board tasks after external changes (e.g. task move, create, delete)
    public void refreshBoardTasks(String boardId) {
        Map<String, List<Task>> currentMap = tasksPerBoardLiveData.getValue();
        if (currentMap == null) {
            currentMap = new HashMap<>();
        }
        
        final Map<String, List<Task>> updatedMap = new HashMap<>(currentMap);
        
        getTasksByBoardUseCase.execute(boardId, new GetTasksByBoardUseCase.Callback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> tasks) {
                updatedMap.put(boardId, tasks != null ? tasks : new ArrayList<>());
                tasksPerBoardLiveData.setValue(updatedMap);
            }

            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
            }
        });
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
    }
}