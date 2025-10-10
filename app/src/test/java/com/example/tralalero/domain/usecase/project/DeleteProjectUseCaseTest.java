package com.example.tralalero.domain.usecase.project;

import com.example.tralalero.domain.repository.IProjectRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class DeleteProjectUseCaseTest {

    @Mock
    private IProjectRepository mockRepository;

    @Mock
    private DeleteProjectUseCase.Callback<Void> mockCallback;

    private DeleteProjectUseCase useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new DeleteProjectUseCase(mockRepository);
    }

    @Test
    public void execute_ValidId_ShouldDeleteSuccessfully() {
        // Given
        String projectId = "project-123";

        // When
        useCase.execute(projectId, mockCallback);

        // Capture
        ArgumentCaptor<IProjectRepository.RepositoryCallback<Void>> callbackCaptor =
                ArgumentCaptor.forClass(IProjectRepository.RepositoryCallback.class);
        verify(mockRepository).deleteProject(eq(projectId), callbackCaptor.capture());

        // Simulate success
        callbackCaptor.getValue().onSuccess(null);

        // Then
        verify(mockCallback).onSuccess(null);
    }

    @Test
    public void execute_NullId_ShouldReturnError() {
        useCase.execute(null, mockCallback);

        verify(mockCallback).onError("Project ID cannot be empty");
        verify(mockRepository, never()).deleteProject(anyString(), any());
    }

    @Test
    public void execute_EmptyId_ShouldReturnError() {
        useCase.execute("  ", mockCallback);

        verify(mockCallback).onError("Project ID cannot be empty");
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_NullCallback_ShouldThrowException() {
        useCase.execute("project-123", null);
    }
}