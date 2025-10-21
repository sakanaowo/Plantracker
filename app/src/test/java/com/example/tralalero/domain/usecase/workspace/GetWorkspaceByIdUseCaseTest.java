package com.example.tralalero.domain.usecase.workspace;

import com.example.tralalero.domain.model.Workspace;
import com.example.tralalero.domain.repository.IWorkspaceRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GetWorkspaceByIdUseCaseTest {

    @Mock
    private IWorkspaceRepository mockRepository;

    @Mock
    private GetWorkspaceByIdUseCase.Callback<Workspace> mockCallback;

    private GetWorkspaceByIdUseCase useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new GetWorkspaceByIdUseCase(mockRepository);
    }

    @Test
    public void execute_ValidId_ShouldReturnWorkspace() {
        // Given
        String workspaceId = "workspace-123";
        Workspace expectedWorkspace = new Workspace("workspace-123", "Test Workspace", "PERSONAL", "user1");

        // When
        useCase.execute(workspaceId, mockCallback);

        // Capture the callback
        ArgumentCaptor<IWorkspaceRepository.RepositoryCallback<Workspace>> callbackCaptor =
                ArgumentCaptor.forClass(IWorkspaceRepository.RepositoryCallback.class);
        verify(mockRepository).getWorkspaceById(eq(workspaceId), callbackCaptor.capture());

        // Simulate success
        callbackCaptor.getValue().onSuccess(expectedWorkspace);

        // Then
        verify(mockCallback).onSuccess(expectedWorkspace);
        verify(mockCallback, never()).onError(anyString());
    }

    @Test
    public void execute_NullId_ShouldReturnError() {
        // When
        useCase.execute(null, mockCallback);

        // Then
        verify(mockCallback).onError("Workspace ID cannot be empty");
        verify(mockRepository, never()).getWorkspaceById(anyString(), any());
    }

    @Test
    public void execute_EmptyId_ShouldReturnError() {
        // When
        useCase.execute("", mockCallback);

        // Then
        verify(mockCallback).onError("Workspace ID cannot be empty");
        verify(mockRepository, never()).getWorkspaceById(anyString(), any());
    }

    @Test
    public void execute_WorkspaceNotFound_ShouldReturnError() {
        // Given
        String workspaceId = "non-existent";

        // When
        useCase.execute(workspaceId, mockCallback);

        // Capture the callback
        ArgumentCaptor<IWorkspaceRepository.RepositoryCallback<Workspace>> callbackCaptor =
                ArgumentCaptor.forClass(IWorkspaceRepository.RepositoryCallback.class);
        verify(mockRepository).getWorkspaceById(eq(workspaceId), callbackCaptor.capture());

        // Simulate null result
        callbackCaptor.getValue().onSuccess(null);

        // Then
        verify(mockCallback).onError("Workspace not found");
        verify(mockCallback, never()).onSuccess(any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_NullCallback_ShouldThrowException() {
        // When
        useCase.execute("workspace-123", null);
    }
}