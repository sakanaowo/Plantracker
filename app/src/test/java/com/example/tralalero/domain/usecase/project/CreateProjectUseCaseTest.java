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

public class CreateProjectUseCaseTest {

    @Mock
    private IProjectRepository mockRepository;

    @Mock
    private CreateProjectUseCase.Callback<Project> mockCallback;

    private CreateProjectUseCase useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new CreateProjectUseCase(mockRepository);
    }

    @Test
    public void execute_ValidProject_ShouldCreateSuccessfully() {
        // Given
        String workspaceId = "workspace-123";
        Project inputProject = new Project(null, workspaceId, "  Test Project  ",
                "Description", "test", null);
        Project expectedProject = new Project("1", workspaceId, "Test Project",
                "Description", "TEST", "KANBAN");

        // When
        useCase.execute(workspaceId, inputProject, mockCallback);

        // Capture project passed to repository
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        ArgumentCaptor<IProjectRepository.RepositoryCallback<Project>> callbackCaptor =
                ArgumentCaptor.forClass(IProjectRepository.RepositoryCallback.class);
        verify(mockRepository).createProject(eq(workspaceId), projectCaptor.capture(),
                callbackCaptor.capture());

        // Verify business rules applied
        Project capturedProject = projectCaptor.getValue();
        assertEquals("Test Project", capturedProject.getName());
        assertEquals("TEST", capturedProject.getKey());
        assertEquals("KANBAN", capturedProject.getBoardType());

        // Simulate success
        callbackCaptor.getValue().onSuccess(expectedProject);

        // Then
        verify(mockCallback).onSuccess(expectedProject);
    }

    @Test
    public void execute_NullWorkspaceId_ShouldReturnError() {
        // Given
        Project project = new Project(null, null, "Test", "Desc", "TEST", "KANBAN");

        // When
        useCase.execute(null, project, mockCallback);

        // Then
        verify(mockCallback).onError("Workspace ID cannot be empty");
        verify(mockRepository, never()).createProject(anyString(), any(), any());
    }

    @Test
    public void execute_NullProject_ShouldReturnError() {
        // When
        useCase.execute("workspace-123", null, mockCallback);

        // Then
        verify(mockCallback).onError("Project cannot be null");
    }

    @Test
    public void execute_EmptyName_ShouldReturnError() {
        // Given
        Project project = new Project(null, "workspace-1", "", "Desc", "TEST", "KANBAN");

        // When
        useCase.execute("workspace-123", project, mockCallback);

        // Then
        verify(mockCallback).onError("Project name cannot be empty");
    }

    @Test
    public void execute_InvalidKeyFormat_ShouldReturnError() {
        // Given - key with lowercase and special chars
        Project project = new Project(null, "workspace-1", "Test", "Desc", "test-123", "KANBAN");

        // When
        useCase.execute("workspace-123", project, mockCallback);

        // Then
        verify(mockCallback).onError("Project key must be 2-10 uppercase letters (A-Z) without special characters");
    }

    @Test
    public void execute_KeyTooShort_ShouldReturnError() {
        // Given
        Project project = new Project(null, "workspace-1", "Test", "Desc", "A", "KANBAN");

        // When
        useCase.execute("workspace-123", project, mockCallback);

        // Then
        verify(mockCallback).onError("Project key must be 2-10 uppercase letters (A-Z) without special characters");
    }

    @Test
    public void execute_KeyTooLong_ShouldReturnError() {
        // Given
        Project project = new Project(null, "workspace-1", "Test", "Desc", "ABCDEFGHIJK", "KANBAN");

        // When
        useCase.execute("workspace-123", project, mockCallback);

        // Then
        verify(mockCallback).onError("Project key must be 2-10 uppercase letters (A-Z) without special characters");
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_NullCallback_ShouldThrowException() {
        Project project = new Project(null, "workspace-1", "Test", "Desc", "TEST", "KANBAN");
        useCase.execute("workspace-123", project, null);
    }
}