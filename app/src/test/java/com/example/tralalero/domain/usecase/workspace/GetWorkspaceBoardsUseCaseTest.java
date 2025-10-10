package com.example.tralalero.domain.usecase.workspace;

import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.repository.IWorkspaceRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GetWorkspaceBoardsUseCaseTest {

    @Mock
    private IWorkspaceRepository mockRepository;

    @Mock
    private GetWorkspaceBoardsUseCase.Callback<List<Board>> mockCallback;

    private GetWorkspaceBoardsUseCase useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new GetWorkspaceBoardsUseCase(mockRepository);
    }

    @Test
    public void execute_ValidProjectId_ShouldReturnSortedBoards() {
        // Given
        String projectId = "project-123";
        List<Board> unsortedBoards = Arrays.asList(
                new Board("3", projectId, "Board C", 2),
                new Board("1", projectId, "Board A", 0),
                new Board("2", projectId, "Board B", 1)
        );

        // When
        useCase.execute(projectId, mockCallback);

        // Capture the callback
        ArgumentCaptor<IWorkspaceRepository.RepositoryCallback<List<Board>>> callbackCaptor =
                ArgumentCaptor.forClass(IWorkspaceRepository.RepositoryCallback.class);
        verify(mockRepository).getBoards(eq(projectId), callbackCaptor.capture());

        // Simulate success with unsorted list
        callbackCaptor.getValue().onSuccess(unsortedBoards);

        // Capture the result passed to callback
        ArgumentCaptor<List<Board>> resultCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockCallback).onSuccess(resultCaptor.capture());

        // Then - verify boards are sorted by order
        List<Board> sortedBoards = resultCaptor.getValue();
        assertEquals(3, sortedBoards.size());
        assertEquals(0, sortedBoards.get(0).getOrder());
        assertEquals(1, sortedBoards.get(1).getOrder());
        assertEquals(2, sortedBoards.get(2).getOrder());
    }

    @Test
    public void execute_NullProjectId_ShouldReturnError() {
        // When
        useCase.execute(null, mockCallback);

        // Then
        verify(mockCallback).onError("Project ID cannot be empty");
        verify(mockRepository, never()).getBoards(anyString(), any());
    }

    @Test
    public void execute_EmptyProjectId_ShouldReturnError() {
        // When
        useCase.execute("", mockCallback);

        // Then
        verify(mockCallback).onError("Project ID cannot be empty");
        verify(mockRepository, never()).getBoards(anyString(), any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_NullCallback_ShouldThrowException() {
        // When
        useCase.execute("project-123", null);
    }
}