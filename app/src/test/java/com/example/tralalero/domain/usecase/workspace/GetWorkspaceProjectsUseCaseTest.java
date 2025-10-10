package com.example.tralalero.domain.usecase.workspace;

import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IWorkspaceRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GetWorkspaceProjectsUseCaseTest {

    @Mock
    private IWorkspaceRepository mockRepository;

    @Mock
    private GetWorkspaceProjectsUseCase.Callback<List<Project>> mockCallback;

    private GetWorkspaceProjectsUseCase useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new GetWorkspaceProjectsUseCase(mockRepository);
    }

    @Test
    public void execute_ValidWorkspaceId_ShouldReturnProjects() {
        // Given
        String workspaceId = "workspace-123";
        List<Project> expectedProjects = Arrays.asList(
                new Project("1", workspaceId, "Project 1", "Desc 1", "PROJ1", "KANBAN"),
                new Project("2", workspaceId, "Project 2", "Desc 2", "PROJ2", "SCRUM")
        );

        // When
        useCase.execute(workspaceId, mockCallback);

        // Capture the callback
        ArgumentCaptor<IWorkspaceRepository.RepositoryCallback<List<Project>>> callbackCaptor =
                ArgumentCaptor.forClass(IWorkspaceRepository.RepositoryCallback.class);
        verify(mockRepository).getProjects(eq(workspaceId), callbackCaptor.capture());

        // Simulate success
        callbackCaptor.getValue().onSuccess(expectedProjects);

        // Then
        verify(mockCallback).onSuccess(expectedProjects);
    }

    @Test
    public void execute_NullWorkspaceId_ShouldReturnError() {
        // When
        useCase.execute(null, mockCallback);

        // Then
        verify(mockCallback).onError("Workspace ID cannot be empty");
        verify(mockRepository, never()).getProjects(anyString(), any());
    }

    @Test
    public void execute_EmptyWorkspaceId_ShouldReturnError() {
        // When
        useCase.execute("  ", mockCallback);

        // Then
        verify(mockCallback).onError("Workspace ID cannot be empty");
        verify(mockRepository, never()).getProjects(anyString(), any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_NullCallback_ShouldThrowException() {
        // When
        useCase.execute("workspace-123", null);
    }
}