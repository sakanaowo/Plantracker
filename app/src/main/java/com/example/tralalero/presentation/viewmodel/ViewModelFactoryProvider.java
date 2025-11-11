package com.example.tralalero.presentation.viewmodel;

import com.example.tralalero.App.App;
import com.example.tralalero.data.remote.api.BoardApiService;
import com.example.tralalero.data.remote.api.ProjectApiService;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.remote.api.CommentApiService;
import com.example.tralalero.data.remote.api.AttachmentApiService;
import com.example.tralalero.data.remote.api.WorkspaceApiService;
import com.example.tralalero.data.repository.AuthRepositoryImpl;
import com.example.tralalero.data.repository.BoardRepositoryImpl;
import com.example.tralalero.data.repository.ProjectRepositoryImpl;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.data.repository.WorkspaceRepositoryImpl;
import com.example.tralalero.domain.repository.IAuthRepository;
import com.example.tralalero.domain.repository.IBoardRepository;
import com.example.tralalero.domain.repository.IProjectRepository;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.domain.repository.IWorkspaceRepository;
import com.example.tralalero.domain.usecase.auth.GetCurrentUserUseCase;
import com.example.tralalero.domain.usecase.auth.IsLoggedInUseCase;
import com.example.tralalero.domain.usecase.auth.LoginUseCase;
import com.example.tralalero.domain.usecase.auth.LogoutUseCase;
import com.example.tralalero.domain.usecase.auth.SignupUseCase;
import com.example.tralalero.domain.usecase.board.CreateBoardUseCase;
import com.example.tralalero.domain.usecase.board.DeleteBoardUseCase;
import com.example.tralalero.domain.usecase.board.GetBoardByIdUseCase;
import com.example.tralalero.domain.usecase.board.GetBoardsByProjectUseCase;
import com.example.tralalero.domain.usecase.board.GetBoardTasksUseCase;
import com.example.tralalero.domain.usecase.board.ReorderBoardsUseCase;
import com.example.tralalero.domain.usecase.board.UpdateBoardUseCase;
import com.example.tralalero.domain.usecase.project.CreateProjectUseCase;
import com.example.tralalero.domain.usecase.project.DeleteProjectUseCase;
import com.example.tralalero.domain.usecase.project.GetProjectByIdUseCase;
import com.example.tralalero.domain.usecase.project.SwitchBoardTypeUseCase;
import com.example.tralalero.domain.usecase.project.UpdateProjectKeyUseCase;
import com.example.tralalero.domain.usecase.project.UpdateProjectUseCase;
import com.example.tralalero.domain.usecase.task.GetTasksByBoardUseCase;
import com.example.tralalero.domain.usecase.workspace.CreateWorkspaceUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceBoardsUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceByIdUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceProjectsUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspacesUseCase;
import com.example.tralalero.network.ApiClient;

public class ViewModelFactoryProvider {

    private static WorkspaceApiService workspaceApi;
    private static BoardApiService boardApi;
    private static ProjectApiService projectApi;
    private static TaskApiService taskApi;
    private static CommentApiService commentApi;
    private static AttachmentApiService attachmentApi;

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
        if (commentApi == null) {
            commentApi = ApiClient.get(App.authManager).create(CommentApiService.class);
        }
        if (attachmentApi == null) {
            attachmentApi = ApiClient.get(App.authManager).create(AttachmentApiService.class);
        }
    }

    public static AuthViewModelFactory provideAuthViewModelFactory() {
        IAuthRepository repository = new AuthRepositoryImpl(App.getInstance());

        return new AuthViewModelFactory(
            new LoginUseCase(repository),
            new SignupUseCase(repository),
            new LogoutUseCase(repository),
            new GetCurrentUserUseCase(repository),
            new IsLoggedInUseCase(repository)
        );
    }

    public static WorkspaceViewModelFactory provideWorkspaceViewModelFactory() {
        initApis();

        IWorkspaceRepository workspaceRepository = new WorkspaceRepositoryImpl(workspaceApi);
        IProjectRepository projectRepository = new ProjectRepositoryImpl(projectApi);

        return new WorkspaceViewModelFactory(
            new GetWorkspacesUseCase(workspaceRepository),
            new GetWorkspaceByIdUseCase(workspaceRepository),
            new CreateWorkspaceUseCase(workspaceRepository),
            new GetWorkspaceProjectsUseCase(workspaceRepository),
            new GetWorkspaceBoardsUseCase(workspaceRepository),
            new CreateProjectUseCase(projectRepository),
            new DeleteProjectUseCase(projectRepository)
        );
    }

    public static ProjectViewModelFactory provideProjectViewModelFactory() {
        initApis();

        IProjectRepository projectRepository = new ProjectRepositoryImpl(projectApi);
        IBoardRepository boardRepository = new BoardRepositoryImpl(boardApi);
        ITaskRepository taskRepository = new TaskRepositoryImpl(taskApi, commentApi, attachmentApi);

        return new ProjectViewModelFactory(
            new GetProjectByIdUseCase(projectRepository),
            new CreateProjectUseCase(projectRepository),
            new UpdateProjectUseCase(projectRepository),
            new DeleteProjectUseCase(projectRepository),
            new SwitchBoardTypeUseCase(projectRepository),
            new UpdateProjectKeyUseCase(projectRepository),
            new GetBoardsByProjectUseCase(boardRepository),
            new GetTasksByBoardUseCase(taskRepository)
        );
    }

    public static BoardViewModelFactory provideBoardViewModelFactory() {
        initApis();
        IBoardRepository boardRepository = new BoardRepositoryImpl(boardApi);
        CommentApiService commentApi = ApiClient.get(App.authManager).create(CommentApiService.class);
        AttachmentApiService attachmentApi = ApiClient.get(App.authManager).create(AttachmentApiService.class);
        ITaskRepository taskRepository = new TaskRepositoryImpl(taskApi, commentApi, attachmentApi);

        return new BoardViewModelFactory(
            new GetBoardByIdUseCase(boardRepository),
            new GetBoardsByProjectUseCase(boardRepository),
            new CreateBoardUseCase(boardRepository),
            new UpdateBoardUseCase(boardRepository),
            new DeleteBoardUseCase(boardRepository),
            new ReorderBoardsUseCase(boardRepository),
            new GetBoardTasksUseCase(taskRepository) 
        );
    }
}