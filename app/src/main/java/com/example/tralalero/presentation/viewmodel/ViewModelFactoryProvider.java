package com.example.tralalero.presentation.viewmodel;

import com.example.tralalero.App.App;
import com.example.tralalero.auth.remote.AuthApi;
import com.example.tralalero.data.remote.api.WorkspaceApiService;
import com.example.tralalero.data.repository.WorkspaceRepositoryImpl;
import com.example.tralalero.domain.repository.IWorkspaceRepository;
import com.example.tralalero.domain.usecase.workspace.CreateWorkspaceUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceByIdUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspacesUseCase;
import com.example.tralalero.network.ApiClient;

public class ViewModelFactoryProvider {
    private static WorkspaceApiService workspaceApiService;
    private static AuthApi authApiService;

    private static void initApis() {
        if (workspaceApiService == null) {
            workspaceApiService = ApiClient.get(App.authManager).create(WorkspaceApiService.class);
        }
        if (authApiService == null) {
            authApiService = ApiClient.get(App.authManager).create(AuthApi.class);
        }
    }

    public static WorkspaceViewModelFactory provideWorkspaceViewModelFactory() {
        initApis();
        IWorkspaceRepository repository = new WorkspaceRepositoryImpl(workspaceApiService);
        return new WorkspaceViewModelFactory(
                new GetWorkspacesUseCase(repository),
                new GetWorkspaceByIdUseCase(repository),
                new CreateWorkspaceUseCase(repository),
                new UpdateWorkspaceUseCase(repository),
                new DeleteWorkspaceUseCase(repository)
        );
    }
}