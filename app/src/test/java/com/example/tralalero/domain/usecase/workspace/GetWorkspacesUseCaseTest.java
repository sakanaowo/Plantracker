package com.example.tralalero.domain.usecase.workspace;

import com.example.tralalero.domain.model.Workspace;
import com.example.tralalero.domain.repository.IWorkspaceRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GetWorkspacesUseCase
 */
public class GetWorkspacesUseCaseTest {

    @Mock
    private IWorkspaceRepository mockRepository;

    @Mock
    private GetWorkspacesUseCase.Callback<List<Workspace>> mockCallback;

    private GetWorkspacesUseCase useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new GetWorkspacesUseCase(mockRepository);
    }

    @Test
    public void execute_Success_ShouldReturnWorkspaces() {
        // Given
        List<Workspace> expectedWorkspaces = Arrays.asList(
                new Workspace("1", "Workspace 1", "PERSONAL", "user1"),
                new Workspace("2", "Workspace 2", "TEAM", "user1")
        );

        // When
        useCase.execute(mockCallback);

        // Capture the callback passed to repository
        ArgumentCaptor<IWorkspaceRepository.RepositoryCallback<List<Workspace>>> callbackCaptor =
                ArgumentCaptor.forClass(IWorkspaceRepository.RepositoryCallback.class);
        verify(mockRepository).getWorkspaces(callbackCaptor.capture());

        // Simulate success
        callbackCaptor.getValue().onSuccess(expectedWorkspaces);

        // Then
        verify(mockCallback).onSuccess(expectedWorkspaces);
        verify(mockCallback, never()).onError(anyString());
    }

    @Test
    public void execute_RepositoryError_ShouldForwardError() {
        // Given
        String errorMessage = "Network error";

        // When
        useCase.execute(mockCallback);

        // Capture the callback passed to repository
        ArgumentCaptor<IWorkspaceRepository.RepositoryCallback<List<Workspace>>> callbackCaptor =
                ArgumentCaptor.forClass(IWorkspaceRepository.RepositoryCallback.class);
        verify(mockRepository).getWorkspaces(callbackCaptor.capture());

        // Simulate error
        callbackCaptor.getValue().onError(errorMessage);

        // Then
        verify(mockCallback).onError(errorMessage);
        verify(mockCallback, never()).onSuccess(any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_NullCallback_ShouldThrowException() {
        // When
        useCase.execute(null);

        // Then - exception thrown
    }
}