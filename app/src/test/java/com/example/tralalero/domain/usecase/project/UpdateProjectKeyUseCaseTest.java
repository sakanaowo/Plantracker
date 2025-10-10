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

public class UpdateProjectKeyUseCaseTest {

    @Mock
    private IProjectRepository mockRepository;

    @Mock
    private UpdateProjectKeyUseCase.Callback<Project> mockCallback;

    private UpdateProjectKeyUseCase useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new UpdateProjectKeyUseCase(mockRepository);
    }

    @Test
    public void execute_ValidKey_ShouldUpdateSuccessfully() {
        // Given
        String projectId = "project-123";
        String newKey = "  newkey  "; // lowercase with spaces
        Project updatedProject = new Project(projectId, "workspace-1", "Test", "Desc", "NEWKEY", "KANBAN");

        // When
        useCase.execute(projectId, newKey, mockCallback);

        // Capture
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<IProjectRepository.RepositoryCallback<Project>> callbackCaptor =
                ArgumentCaptor.forClass(IProjectRepository.RepositoryCallback.class);
        verify(mockRepository).updateProjectKey(eq(projectId), keyCaptor.capture(),
                callbackCaptor.capture());

        // Verify key was trimmed and uppercased
        assertEquals("NEWKEY", keyCaptor.getValue());

        // Simulate success
        callbackCaptor.getValue().onSuccess(updatedProject);

        verify(mockCallback).onSuccess(updatedProject);
    }

    @Test
    public void execute_NullProjectId_ShouldReturnError() {
        useCase.execute(null, "NEWKEY", mockCallback);

        verify(mockCallback).onError("Project ID cannot be empty");
    }

    @Test
    public void execute_NullKey_ShouldReturnError() {
        useCase.execute("project-123", null, mockCallback);

        verify(mockCallback).onError("Project key cannot be empty");
    }

    @Test
    public void execute_KeyWithNumbers_ShouldReturnError() {
        useCase.execute("project-123", "KEY123", mockCallback);

        verify(mockCallback).onError("Project key must be 2-10 uppercase letters (A-Z) without special characters");
    }

    @Test
    public void execute_KeyWithSpecialChars_ShouldReturnError() {
        useCase.execute("project-123", "KEY-123", mockCallback);

        verify(mockCallback).onError("Project key must be 2-10 uppercase letters (A-Z) without special characters");
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_NullCallback_ShouldThrowException() {
        useCase.execute("project-123", "NEWKEY", null);
    }
}