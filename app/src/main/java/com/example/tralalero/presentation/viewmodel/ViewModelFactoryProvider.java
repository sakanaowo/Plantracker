package com.example.tralalero.presentation.viewmodel;

import com.example.tralalero.App.App;
import com.example.tralalero.data.remote.api.BoardApiService;
import com.example.tralalero.data.remote.api.ProjectApiService;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.remote.api.WorkspaceApiService;
import com.example.tralalero.data.repository.BoardRepositoryImpl;
import com.example.tralalero.data.repository.ProjectRepositoryImpl;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.data.repository.WorkspaceRepositoryImpl;
import com.example.tralalero.domain.repository.IBoardRepository;
import com.example.tralalero.domain.repository.IProjectRepository;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.domain.repository.IWorkspaceRepository;
import com.example.tralalero.domain.usecase.board.CreateBoardUseCase;
import com.example.tralalero.domain.usecase.board.DeleteBoardUseCase;
import com.example.tralalero.domain.usecase.board.GetBoardByIdUseCase;
import com.example.tralalero.domain.usecase.board.GetBoardTasksUseCase;
import com.example.tralalero.domain.usecase.board.ReorderBoardsUseCase;
import com.example.tralalero.domain.usecase.board.UpdateBoardUseCase;
import com.example.tralalero.domain.usecase.project.CreateProjectUseCase;
import com.example.tralalero.domain.usecase.project.DeleteProjectUseCase;
import com.example.tralalero.domain.usecase.project.GetProjectByIdUseCase;
import com.example.tralalero.domain.usecase.project.SwitchBoardTypeUseCase;
import com.example.tralalero.domain.usecase.project.UpdateProjectKeyUseCase;
import com.example.tralalero.domain.usecase.project.UpdateProjectUseCase;
import com.example.tralalero.domain.usecase.workspace.CreateWorkspaceUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceBoardsUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceByIdUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceProjectsUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspacesUseCase;
import com.example.tralalero.network.ApiClient;

/**
 * Helper class to provide ViewModelFactories with dependencies injection.
 * Reduces boilerplate code when setting up ViewModels in Activities/Fragments.
 * 
 * Usage example:
 * <pre>
 * workspaceViewModel = new ViewModelProvider(this, 
 *     ViewModelFactoryProvider.provideWorkspaceViewModelFactory()
 * ).get(WorkspaceViewModel.class);
 * </pre>
 *
 * @author Người 2 - Phase 5
 * @date 14/10/2025
 */
public class ViewModelFactoryProvider {
    
    private static WorkspaceApiService workspaceApi;
    private static BoardApiService boardApi;
    private static ProjectApiService projectApi;
    private static TaskApiService taskApi;
    
    /**
     * Initialize API services (lazy initialization)
     */
    private static void initApis() {
        if (workspaceApi == null) {
            workspaceApi = ApiClient.get(App.authManager).create(WorkspaceApiService.class);
        }
        if (boardApi == null) {
            boardApi = ApiClient.get(App.authManager).create(BoardApiService.class);
        }
        if (projectApi == null) {
            projectApi = ApiClient.get(App.authManager).create(ProjectApiService.class);
        }
        if (taskApi == null) {
            taskApi = ApiClient.get(App.authManager).create(TaskApiService.class);
        }
    }
    
    /**
     * Provide WorkspaceViewModelFactory with all dependencies
     * 
     * @return WorkspaceViewModelFactory instance ready to use
     */
    public static WorkspaceViewModelFactory provideWorkspaceViewModelFactory() {
        initApis();
        
        IWorkspaceRepository repository = new WorkspaceRepositoryImpl(workspaceApi);
        
        return new WorkspaceViewModelFactory(
            new GetWorkspacesUseCase(repository),
            new GetWorkspaceByIdUseCase(repository),
            new CreateWorkspaceUseCase(repository),
            new GetWorkspaceProjectsUseCase(repository),
            new GetWorkspaceBoardsUseCase(repository)
        );
    }
    
    /**
     * Provide ProjectFactory with all dependencies
     * 
     * @return ProjectFactory instance ready to use
     */
    public static ProjectFactory provideProjectViewModelFactory() {
        initApis();
        
        IProjectRepository repository = new ProjectRepositoryImpl(projectApi);
        
        return new ProjectFactory(
            new GetProjectByIdUseCase(repository),
            new CreateProjectUseCase(repository),
            new UpdateProjectUseCase(repository),
            new DeleteProjectUseCase(repository),
            new SwitchBoardTypeUseCase(repository),
            new UpdateProjectKeyUseCase(repository)
        );
    }
    
    /**
     * Provide BoardFactory with all dependencies
     * 
     * @return BoardFactory instance ready to use
     */
    public static BoardFactory provideBoardViewModelFactory() {
        initApis();
        
        // BoardRepository needs BoardApiService
        IBoardRepository boardRepository = new BoardRepositoryImpl(boardApi);
        
        // GetBoardTasksUseCase needs TaskRepository
        ITaskRepository taskRepository = new TaskRepositoryImpl(taskApi);
        
        return new BoardFactory(
            new GetBoardByIdUseCase(boardRepository),
            new CreateBoardUseCase(boardRepository),
            new UpdateBoardUseCase(boardRepository),
            new DeleteBoardUseCase(boardRepository),
            new ReorderBoardsUseCase(boardRepository),
            new GetBoardTasksUseCase(taskRepository) // Uses TaskRepository, not BoardRepository!
        );
    }
}