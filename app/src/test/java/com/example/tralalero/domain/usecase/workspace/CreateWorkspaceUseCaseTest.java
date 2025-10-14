package com.example.tralalero.domain.usecase.workspace;

import com.example.tralalero.domain.model.Workspace;
import com.example.tralalero.domain.repository.IWorkspaceRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CreateWorkspaceUseCaseTest {

    @Mock
    private IWorkspaceRepository mockRepository;

    @Mock
    private CreateWorkspaceUseCase.Callback<Workspace> mockCallback;

    private CreateWorkspaceUseCase useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new CreateWorkspaceUseCase(mockRepository);
    }

    @Test
    public void execute_ValidWorkspace_ShouldCreateSuccessfully() {
        // Given
        Workspace inputWorkspace = new Workspace(null, "  Test Workspace  ", "personal", "user1");
        Workspace expectedWorkspace = new Workspace("1", "Test Workspace", "PERSONAL", "user1");

        // When
        useCase.execute(inputWorkspace, mockCallback);

        // Capture the workspace passed to repository
        ArgumentCaptor<Workspace> workspaceCaptor = ArgumentCaptor.forClass(Workspace.class);
        ArgumentCaptor<IWorkspaceRepository.RepositoryCallback<Workspace>> callbackCaptor =
                ArgumentCaptor.forClass(IWorkspaceRepository.RepositoryCallback.class);
        verify(mockRepository).createWorkspace(workspaceCaptor.capture(), callbackCaptor.capture());

        // Verify workspace was trimmed and type was uppercased
        Workspace capturedWorkspace = workspaceCaptor.getValue();
        assertEquals("Test Workspace", capturedWorkspace.getName());
        assertEquals("PERSONAL", capturedWorkspace.getType());

        // Simulate success
        callbackCaptor.getValue().onSuccess(expectedWorkspace);

        // Then
        verify(mockCallback).onSuccess(expectedWorkspace);
    }

    @Test
    public void execute_NullWorkspace_ShouldReturnError() {
        // When
        useCase.execute(null, mockCallback);

        // Then
        verify(mockCallback).onError("Workspace cannot be null");
        verify(mockRepository, never()).createWorkspace(any(), any());
    }

    @Test
    public void execute_EmptyName_ShouldReturnError() {
        // Given
        Workspace workspace = new Workspace(null, "", "PERSONAL", "user1");

        // When
        useCase.execute(workspace, mockCallback);

        // Then
        verify(mockCallback).onError("Workspace name cannot be empty");
        verify(mockRepository, never()).createWorkspace(any(), any());
    }

    @Test
    public void execute_NameTooLong_ShouldReturnError() {
        // Given
        String longName = "a".repeat(101); // 101 characters
        Workspace workspace = new Workspace(null, longName, "PERSONAL", "user1");

        // When
        useCase.execute(workspace, mockCallback);

        // Then
        verify(mockCallback).onError("Workspace name cannot exceed 100 characters");
        verify(mockRepository, never()).createWorkspace(any(), any());
    }

    @Test
    public void execute_InvalidType_ShouldReturnError() {
        // Given
        Workspace workspace = new Workspace(null, "Test", "INVALID", "user1");

        // When
        useCase.execute(workspace, mockCallback);

        // Then
        verify(mockCallback).onError("Workspace type must be PERSONAL or TEAM");
        verify(mockRepository, never()).createWorkspace(any(), any());
    }

    @Test
    public void execute_EmptyType_ShouldReturnError() {
        // Given
        Workspace workspace = new Workspace(null, "Test", "", "user1");

        // When
        useCase.execute(workspace, mockCallback);

        // Then
        verify(mockCallback).onError("Workspace type cannot be empty");
        verify(mockRepository, never()).createWorkspace(any(), any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_NullCallback_ShouldThrowException() {
        // Given
        Workspace workspace = new Workspace(null, "Test", "PERSONAL", "user1");

        // When
        useCase.execute(workspace, null);
    }
}