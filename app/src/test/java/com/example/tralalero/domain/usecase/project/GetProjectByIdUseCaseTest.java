package com.example.tralalero.domain.usecase.project;

import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IProjectRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GetProjectByIdUseCaseTest {

    @Mock
    private IProjectRepository mockRepository;

    @Mock
    private GetProjectByIdUseCase.Callback<Project> mockCallback;

    private GetProjectByIdUseCase useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new GetProjectByIdUseCase(mockRepository);
    }

    @Test
    public void execute_ValidId_ShouldReturnProject() {
        // Given
        String projectId = "project-123";
        Project expectedProject = new Project(projectId, "workspace-1", "Test Project",
                "Description", "TEST", "KANBAN");

        // When
        useCase.execute(projectId, mockCallback);

        // Capture callback
        ArgumentCaptor<IProjectRepository.RepositoryCallback<Project>> callbackCaptor =
                ArgumentCaptor.forClass(IProjectRepository.RepositoryCallback.class);
        verify(mockRepository).getProjectById(eq(projectId), callbackCaptor.capture());

        // Simulate success
        callbackCaptor.getValue().onSuccess(expectedProject);

        // Then
        verify(mockCallback).onSuccess(expectedProject);
    }

    @Test
    public void execute_NullId_ShouldReturnError() {
        // When
        useCase.execute(null, mockCallback);

        // Then
        verify(mockCallback).onError("Project ID cannot be empty");
        verify(mockRepository, never()).getProjectById(anyString(), any());
    }

    @Test
    public void execute_ProjectNotFound_ShouldReturnError() {
        // Given
        String projectId = "non-existent";

        // When
        useCase.execute(projectId, mockCallback);

        ArgumentCaptor<IProjectRepository.RepositoryCallback<Project>> callbackCaptor =
                ArgumentCaptor.forClass(IProjectRepository.RepositoryCallback.class);
        verify(mockRepository).getProjectById(eq(projectId), callbackCaptor.capture());

        // Simulate null result
        callbackCaptor.getValue().onSuccess(null);

        // Then
        verify(mockCallback).onError("Project not found");
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_NullCallback_ShouldThrowException() {
        useCase.execute("project-123", null);
    }
}