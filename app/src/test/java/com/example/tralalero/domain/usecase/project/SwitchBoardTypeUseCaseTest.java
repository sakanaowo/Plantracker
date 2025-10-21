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

public class SwitchBoardTypeUseCaseTest {

    @Mock
    private IProjectRepository mockRepository;

    @Mock
    private SwitchBoardTypeUseCase.Callback<Project> mockCallback;

    private SwitchBoardTypeUseCase useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new SwitchBoardTypeUseCase(mockRepository);
    }

    @Test
    public void execute_SwitchToKanban_ShouldUpdateSuccessfully() {
        // Given
        String projectId = "project-123";
        String boardType = "  kanban  "; // lowercase with spaces
        Project updatedProject = new Project(projectId, "workspace-1", "Test", "Desc", "TEST", "KANBAN");

        // When
        useCase.execute(projectId, boardType, mockCallback);

        // Capture
        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<IProjectRepository.RepositoryCallback<Project>> callbackCaptor =
                ArgumentCaptor.forClass(IProjectRepository.RepositoryCallback.class);
        verify(mockRepository).updateBoardType(eq(projectId), typeCaptor.capture(),
                callbackCaptor.capture());

        // Verify type was normalized
        assertEquals("KANBAN", typeCaptor.getValue());

        // Simulate success
        callbackCaptor.getValue().onSuccess(updatedProject);

        verify(mockCallback).onSuccess(updatedProject);
    }

    @Test
    public void execute_SwitchToScrum_ShouldUpdateSuccessfully() {
        // Given
        String projectId = "project-123";
        Project updatedProject = new Project(projectId, "workspace-1", "Test", "Desc", "TEST", "SCRUM");

        // When
        useCase.execute(projectId, "SCRUM", mockCallback);

        ArgumentCaptor<IProjectRepository.RepositoryCallback<Project>> callbackCaptor =
                ArgumentCaptor.forClass(IProjectRepository.RepositoryCallback.class);
        verify(mockRepository).updateBoardType(eq(projectId), eq("SCRUM"),
                callbackCaptor.capture());

        callbackCaptor.getValue().onSuccess(updatedProject);

        verify(mockCallback).onSuccess(updatedProject);
    }

    @Test
    public void execute_NullProjectId_ShouldReturnError() {
        useCase.execute(null, "KANBAN", mockCallback);

        verify(mockCallback).onError("Project ID cannot be empty");
    }

    @Test
    public void execute_NullBoardType_ShouldReturnError() {
        useCase.execute("project-123", null, mockCallback);

        verify(mockCallback).onError("Board type cannot be empty");
    }

    @Test
    public void execute_InvalidBoardType_ShouldReturnError() {
        useCase.execute("project-123", "AGILE", mockCallback);

        verify(mockCallback).onError("Board type must be either KANBAN or SCRUM");
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_NullCallback_ShouldThrowException() {
        useCase.execute("project-123", "KANBAN", null);
    }
}