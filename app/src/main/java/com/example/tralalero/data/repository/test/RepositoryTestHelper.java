package com.example.tralalero.data.repository.test;

import com.example.tralalero.data.repository.*;
import com.example.tralalero.domain.model.*;
import com.example.tralalero.domain.repository.*;

import java.util.List;

/**
 * Manual Test Helper for Repositories
 * Sử dụng class này để test các Repository trước khi tích hợp vào UI
 */
public class RepositoryTestHelper {

    /**
     * Test WorkspaceRepository
     */
    public static void testWorkspaceRepository(IWorkspaceRepository repository) {
        System.out.println("=== Testing WorkspaceRepository ===");

        // Test getWorkspaces
        repository.getWorkspaces(new IWorkspaceRepository.RepositoryCallback<List<Workspace>>() {
            @Override
            public void onSuccess(List<Workspace> result) {
                System.out.println("✓ getWorkspaces: Success - Found " + result.size() + " workspaces");
            }

            @Override
            public void onError(String error) {
                System.out.println("✗ getWorkspaces: Failed - " + error);
            }
        });
    }

    /**
     * Test ProjectRepository
     */
    public static void testProjectRepository(IProjectRepository repository, String testProjectId) {
        System.out.println("=== Testing ProjectRepository ===");

        // Test getProjectById
        repository.getProjectById(testProjectId, new IProjectRepository.RepositoryCallback<Project>() {
            @Override
            public void onSuccess(Project result) {
                System.out.println("✓ getProjectById: Success - " + result.getName());
            }

            @Override
            public void onError(String error) {
                System.out.println("✗ getProjectById: Failed - " + error);
            }
        });
    }

    /**
     * Test BoardRepository
     */
    public static void testBoardRepository(IBoardRepository repository, String testBoardId) {
        System.out.println("=== Testing BoardRepository ===");

        // Test getBoardById
        repository.getBoardById(testBoardId, new IBoardRepository.RepositoryCallback<Board>() {
            @Override
            public void onSuccess(Board result) {
                System.out.println("✓ getBoardById: Success - " + result.getName());
            }

            @Override
            public void onError(String error) {
                System.out.println("✗ getBoardById: Failed - " + error);
            }
        });
    }

    /**
     * Test TaskRepository
     */
    public static void testTaskRepository(ITaskRepository repository, String testBoardId) {
        System.out.println("=== Testing TaskRepository ===");

        // Test getTasksByBoard
        repository.getTasksByBoard(testBoardId, new ITaskRepository.RepositoryCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> result) {
                System.out.println("✓ getTasksByBoard: Success - Found " + result.size() + " tasks");
            }

            @Override
            public void onError(String error) {
                System.out.println("✗ getTasksByBoard: Failed - " + error);
            }
        });
    }

    /**
     * Test NotificationRepository
     */
    public static void testNotificationRepository(INotificationRepository repository) {
        System.out.println("=== Testing NotificationRepository ===");

        // Test getUnreadCount
        repository.getUnreadCount(new INotificationRepository.RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                System.out.println("✓ getUnreadCount: Success - " + result + " unread notifications");
            }

            @Override
            public void onError(String error) {
                System.out.println("✗ getUnreadCount: Failed - " + error);
            }
        });
    }

    /**
     * Test LabelRepository
     */
    public static void testLabelRepository(ILabelRepository repository, String testWorkspaceId) {
        System.out.println("=== Testing LabelRepository ===");

        // Test getLabelsByWorkspace
        repository.getLabelsByWorkspace(testWorkspaceId, new ILabelRepository.RepositoryCallback<List<Label>>() {
            @Override
            public void onSuccess(List<Label> result) {
                System.out.println("✓ getLabelsByWorkspace: Success - Found " + result.size() + " labels");
            }

            @Override
            public void onError(String error) {
                System.out.println("✗ getLabelsByWorkspace: Failed - " + error);
            }
        });
    }

    /**
     * Test SprintRepository
     */
    public static void testSprintRepository(ISprintRepository repository, String testProjectId) {
        System.out.println("=== Testing SprintRepository ===");

        // Test getSprintsByProject
        repository.getSprintsByProject(testProjectId, new ISprintRepository.RepositoryCallback<List<Sprint>>() {
            @Override
            public void onSuccess(List<Sprint> result) {
                System.out.println("✓ getSprintsByProject: Success - Found " + result.size() + " sprints");
            }

            @Override
            public void onError(String error) {
                System.out.println("✗ getSprintsByProject: Failed - " + error);
            }
        });
    }

    /**
     * Test EventRepository
     */
    public static void testEventRepository(IEventRepository repository, String testProjectId) {
        System.out.println("=== Testing EventRepository ===");

        // Test getEventsByProject
        repository.getEventsByProject(testProjectId, new IEventRepository.RepositoryCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> result) {
                System.out.println("✓ getEventsByProject: Success - Found " + result.size() + " events");
            }

            @Override
            public void onError(String error) {
                System.out.println("✗ getEventsByProject: Failed - " + error);
            }
        });
    }

    /**
     * Test TimeEntryRepository
     */
    public static void testTimeEntryRepository(ITimeEntryRepository repository, String testTaskId) {
        System.out.println("=== Testing TimeEntryRepository ===");

        // Test getTimeEntriesByTask
        repository.getTimeEntriesByTask(testTaskId, new ITimeEntryRepository.RepositoryCallback<List<TimeEntry>>() {
            @Override
            public void onSuccess(List<TimeEntry> result) {
                System.out.println("✓ getTimeEntriesByTask: Success - Found " + result.size() + " entries");
            }

            @Override
            public void onError(String error) {
                System.out.println("✗ getTimeEntriesByTask: Failed - " + error);
            }
        });
    }
}

