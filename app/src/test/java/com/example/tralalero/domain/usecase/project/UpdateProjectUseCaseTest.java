package com.example.tralalero.domain.usecase.project;

import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IProjectRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class UpdateProjectUseCaseTest {

    @Mock
    private IProjectRepository mockRepository;

    @Mock
    private UpdateProjectUseCase.Callback<Project> mockCallback;

    private UpdateProjectUseCase useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new UpdateProjectUseCase(mockRepository);
    }

    @Test
    public void execute_ValidProject_ShouldUpdateSuccessfully() {
        // Given
        String projectId = "project-123";
        Project inputProject = new Project(projectId, "workspace-1", "  Updated Name  ",
                "New Desc", "NEW", "SCRUM");
        Project expectedProject = new Project(projectId, "workspace-1", "Updated Name",
                "New Desc", "NEW", "SCRUM");

        // When
        useCase.execute(projectId, inputProject, mockCallback);

        // Capture
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        ArgumentCaptor<IProjectRepository.RepositoryCallback<Project>> callbackCaptor =
                ArgumentCaptor.forClass(IProjectRepository.RepositoryCallback.class);
        verify(mockRepository).updateProject(eq(projectId), projectCaptor.capture(),
                callbackCaptor.capture());

        // Verify trimming
        Project capturedProject = projectCaptor.getValue();
        assertEquals("Updated Name", capturedProject.getName());
        assertEquals("NEW", capturedProject.getKey());

        // Simulate success
        callbackCaptor.getValue().onSuccess(expectedProject);

        // Then
        verify(mockCallback).onSuccess(expectedProject);
    }

    @Test
    public void execute_NullProjectId_ShouldReturnError() {
        Project project = new Project("1", "workspace-1", "Test", "Desc", "TEST", "KANBAN");

        useCase.execute(null, project, mockCallback);

        verify(mockCallback).onError("Project ID cannot be empty");
        verify(mockRepository, never()).updateProject(anyString(), any(), any());
    }

    @Test
    public void execute_EmptyName_ShouldReturnError() {
        Project project = new Project("1", "workspace-1", "", "Desc", "TEST", "KANBAN");

        useCase.execute("project-123", project, mockCallback);

        verify(mockCallback).onError("Project name cannot be empty");
        verify(mockRepository, never()).updateProject(anyString(), any(), any());
    }

    @Test
    public void execute_InvalidKey_ShouldReturnError() {
        // Given - key PHẢI CÓ GIÁ TRỊ và invalid format
        String projectId = "project-123";
        Project project = new Project("1", "workspace-1", "Test", "Desc", "invalid-123", "KANBAN");

        // When
        useCase.execute(projectId, project, mockCallback);

        // Then
        verify(mockCallback).onError("Project key must be 2-10 uppercase letters (A-Z) without special characters");
        verify(mockRepository, never()).updateProject(anyString(), any(), any());
    }

    @Test
    public void execute_NullKey_ShouldAllowUpdate() {
        // Given - null key is OK for update (optional field)
        String projectId = "project-123";
        Project project = new Project("1", "workspace-1", "Test", "Desc", null, "KANBAN");
        Project updatedProject = new Project("1", "workspace-1", "Test", "Desc", null, "KANBAN");

        // When
        useCase.execute(projectId, project, mockCallback);

        // Capture callback
        ArgumentCaptor<IProjectRepository.RepositoryCallback<Project>> callbackCaptor =
                ArgumentCaptor.forClass(IProjectRepository.RepositoryCallback.class);
        verify(mockRepository).updateProject(eq(projectId), any(Project.class), callbackCaptor.capture());

        // Simulate success
        callbackCaptor.getValue().onSuccess(updatedProject);

        // Then - should succeed
        verify(mockCallback).onSuccess(updatedProject);
        verify(mockCallback, never()).onError(anyString());
    }

    @Test
    public void execute_EmptyKey_ShouldAllowUpdate() {
        // Given - empty string key is treated as "no update"
        String projectId = "project-123";
        Project project = new Project("1", "workspace-1", "Test", "Desc", "", "KANBAN");
        Project updatedProject = new Project("1", "workspace-1", "Test", "Desc", "", "KANBAN");

        // When
        useCase.execute(projectId, project, mockCallback);

        // Capture callback
        ArgumentCaptor<IProjectRepository.RepositoryCallback<Project>> callbackCaptor =
                ArgumentCaptor.forClass(IProjectRepository.RepositoryCallback.class);
        verify(mockRepository).updateProject(eq(projectId), any(Project.class), callbackCaptor.capture());

        // Simulate success
        callbackCaptor.getValue().onSuccess(updatedProject);

        // Then - should succeed
        verify(mockCallback).onSuccess(updatedProject);
        verify(mockCallback, never()).onError(anyString());
    }

    @Test
    public void execute_ValidKeyChange_ShouldUpdate() {
        // Given
        String projectId = "project-123";
        Project project = new Project("1", "workspace-1", "Test", "Desc", "NEWKEY", "KANBAN");
        Project updatedProject = new Project("1", "workspace-1", "Test", "Desc", "NEWKEY", "KANBAN");

        // When
        useCase.execute(projectId, project, mockCallback);

        // Capture
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        ArgumentCaptor<IProjectRepository.RepositoryCallback<Project>> callbackCaptor =
                ArgumentCaptor.forClass(IProjectRepository.RepositoryCallback.class);
        verify(mockRepository).updateProject(eq(projectId), projectCaptor.capture(), callbackCaptor.capture());

        // Verify key was uppercased
        assertEquals("NEWKEY", projectCaptor.getValue().getKey());

        // Simulate success
        callbackCaptor.getValue().onSuccess(updatedProject);

        // Then
        verify(mockCallback).onSuccess(updatedProject);
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_NullCallback_ShouldThrowException() {
        Project project = new Project("1", "workspace-1", "Test", "Desc", "TEST", "KANBAN");
        useCase.execute("project-123", project, null);
    }
}