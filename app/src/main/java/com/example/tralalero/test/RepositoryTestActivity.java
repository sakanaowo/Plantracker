package com.example.tralalero.test;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tralalero.R;
import com.example.tralalero.data.remote.api.*;
import com.example.tralalero.data.repository.*;
import com.example.tralalero.domain.model.*;
import com.example.tralalero.domain.repository.*;
import com.example.tralalero.network.ApiClient;

import java.util.List;


public class RepositoryTestActivity extends AppCompatActivity {

    private static final String TAG = "RepositoryTest";

    private TextView tvLog;
    private ScrollView scrollView;

    private IWorkspaceRepository workspaceRepository;
    private IProjectRepository projectRepository;
    private IBoardRepository boardRepository;
    private ITaskRepository taskRepository;
    private INotificationRepository notificationRepository;
    private ILabelRepository labelRepository;
    private ISprintRepository sprintRepository;
    private IEventRepository eventRepository;
    private ITimeEntryRepository timeEntryRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repository_test);

        initViews();
        initRepositories();
        setupTestButtons();
    }

    private void initViews() {
        tvLog = findViewById(R.id.tvLog);
        scrollView = findViewById(R.id.scrollView);

        log("=== Repository Test Activity ===");
        log("Sẵn sàng test các Repository");
    }

    private void initRepositories() {
        WorkspaceApiService workspaceApi = ApiClient.get().create(WorkspaceApiService.class);
        ProjectApiService projectApi = ApiClient.get().create(ProjectApiService.class);
        BoardApiService boardApi = ApiClient.get().create(BoardApiService.class);
        TaskApiService taskApi = ApiClient.get().create(TaskApiService.class);
        NotificationApiService notificationApi = ApiClient.get().create(NotificationApiService.class);
        LabelApiService labelApi = ApiClient.get().create(LabelApiService.class);
        SprintApiService sprintApi = ApiClient.get().create(SprintApiService.class);
        EventApiService eventApi = ApiClient.get().create(EventApiService.class);
        TimerApiService timerApi = ApiClient.get().create(TimerApiService.class);

        workspaceRepository = new WorkspaceRepositoryImpl(workspaceApi);
        projectRepository = new ProjectRepositoryImpl(projectApi);
        boardRepository = new BoardRepositoryImpl(boardApi);
        taskRepository = new TaskRepositoryImpl(taskApi);
        notificationRepository = new NotificationRepositoryImpl(notificationApi);
        labelRepository = new LabelRepositoryImpl(labelApi);
        sprintRepository = new SprintRepositoryImpl(sprintApi);
        eventRepository = new EventRepositoryImpl(eventApi);
        timeEntryRepository = new TimeEntryRepositoryImpl(timerApi);

        log("✓ Đã khởi tạo tất cả Repositories");
    }

    private void setupTestButtons() {
        Button btnTestWorkspace = findViewById(R.id.btnTestWorkspace);
        Button btnTestProject = findViewById(R.id.btnTestProject);
        Button btnTestBoard = findViewById(R.id.btnTestBoard);
        Button btnTestTask = findViewById(R.id.btnTestTask);
        Button btnTestNotification = findViewById(R.id.btnTestNotification);
        Button btnTestAll = findViewById(R.id.btnTestAll);
        Button btnClearLog = findViewById(R.id.btnClearLog);

        btnTestWorkspace.setOnClickListener(v -> testWorkspaceRepository());
        btnTestProject.setOnClickListener(v -> testProjectRepository());
        btnTestBoard.setOnClickListener(v -> testBoardRepository());
        btnTestTask.setOnClickListener(v -> testTaskRepository());
        btnTestNotification.setOnClickListener(v -> testNotificationRepository());
        btnTestAll.setOnClickListener(v -> testAllRepositories());
        btnClearLog.setOnClickListener(v -> clearLog());
    }


    private void testWorkspaceRepository() {
        log("\n=== Testing WorkspaceRepository ===");

        workspaceRepository.getWorkspaces(new IWorkspaceRepository.RepositoryCallback<List<Workspace>>() {
            @Override
            public void onSuccess(List<Workspace> result) {
                log("✓ getWorkspaces: SUCCESS");
                log("  → Tìm thấy " + result.size() + " workspaces");

                if (!result.isEmpty()) {
                    Workspace first = result.get(0);
                    log("  → Workspace đầu tiên: " + first.getName());
                    log("  → ID: " + first.getId());

                    testGetProjects(first.getId());
                }
            }

            @Override
            public void onError(String error) {
                log("✗ getWorkspaces: FAILED");
                log("  → Error: " + error);
                showToast("Lỗi: " + error);
            }
        });
    }

    private void testGetProjects(String workspaceId) {
        log("\n--- Testing getProjects ---");

        workspaceRepository.getProjects(workspaceId, new IWorkspaceRepository.RepositoryCallback<List<Project>>() {
            @Override
            public void onSuccess(List<Project> result) {
                log("✓ getProjects: SUCCESS");
                log("  → Tìm thấy " + result.size() + " projects");

                if (!result.isEmpty()) {
                    Project first = result.get(0);
                    log("  → Project đầu tiên: " + first.getName());
                    log("  → Key: " + first.getKey());

                    testGetBoards(first.getId());
                }
            }

            @Override
            public void onError(String error) {
                log("✗ getProjects: FAILED - " + error);
            }
        });
    }

    private void testGetBoards(String projectId) {
        log("\n--- Testing getBoards ---");

        workspaceRepository.getBoards(projectId, new IWorkspaceRepository.RepositoryCallback<List<Board>>() {
            @Override
            public void onSuccess(List<Board> result) {
                log("✓ getBoards: SUCCESS");
                log("  → Tìm thấy " + result.size() + " boards");

                for (Board board : result) {
                    log("  → Board: " + board.getName() + " (order: " + board.getOrder() + ")");
                }
            }

            @Override
            public void onError(String error) {
                log("✗ getBoards: FAILED - " + error);
            }
        });
    }

    private void testProjectRepository() {
        log("\n=== Testing ProjectRepository ===");

        workspaceRepository.getWorkspaces(new IWorkspaceRepository.RepositoryCallback<List<Workspace>>() {
            @Override
            public void onSuccess(List<Workspace> workspaces) {
                if (!workspaces.isEmpty()) {
                    String workspaceId = workspaces.get(0).getId();

                    workspaceRepository.getProjects(workspaceId, new IWorkspaceRepository.RepositoryCallback<List<Project>>() {
                        @Override
                        public void onSuccess(List<Project> projects) {
                            if (!projects.isEmpty()) {
                                String projectId = projects.get(0).getId();

                                projectRepository.getProjectById(projectId, new IProjectRepository.RepositoryCallback<Project>() {
                                    @Override
                                    public void onSuccess(Project result) {
                                        log("✓ getProjectById: SUCCESS");
                                        log("  → Name: " + result.getName());
                                        log("  → Description: " + result.getDescription());
                                        log("  → Board Type: " + result.getBoardType());
                                    }

                                    @Override
                                    public void onError(String error) {
                                        log("✗ getProjectById: FAILED - " + error);
                                    }
                                });
                            } else {
                                log("! Không có project nào để test");
                            }
                        }

                        @Override
                        public void onError(String error) {
                            log("✗ Không thể lấy projects - " + error);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                log("✗ Không thể lấy workspaces - " + error);
            }
        });
    }

    private void testBoardRepository() {
        log("\n=== Testing BoardRepository ===");

        workspaceRepository.getWorkspaces(new IWorkspaceRepository.RepositoryCallback<List<Workspace>>() {
            @Override
            public void onSuccess(List<Workspace> workspaces) {
                if (!workspaces.isEmpty()) {
                    workspaceRepository.getProjects(workspaces.get(0).getId(),
                        new IWorkspaceRepository.RepositoryCallback<List<Project>>() {
                        @Override
                        public void onSuccess(List<Project> projects) {
                            if (!projects.isEmpty()) {
                                workspaceRepository.getBoards(projects.get(0).getId(),
                                    new IWorkspaceRepository.RepositoryCallback<List<Board>>() {
                                    @Override
                                    public void onSuccess(List<Board> boards) {
                                        if (!boards.isEmpty()) {
                                            String boardId = boards.get(0).getId();

                                            // Test getBoardById
                                            boardRepository.getBoardById(boardId, new IBoardRepository.RepositoryCallback<Board>() {
                                                @Override
                                                public void onSuccess(Board result) {
                                                    log("✓ getBoardById: SUCCESS");
                                                    log("  → Name: " + result.getName());
                                                    log("  → Order: " + result.getOrder());
                                                }

                                                @Override
                                                public void onError(String error) {
                                                    log("✗ getBoardById: FAILED - " + error);
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onError(String error) {
                                        log("✗ Không thể lấy boards - " + error);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            log("✗ Không thể lấy projects - " + error);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                log("✗ Không thể lấy workspaces - " + error);
            }
        });
    }

    private void testTaskRepository() {
        log("\n=== Testing TaskRepository ===");

        workspaceRepository.getWorkspaces(new IWorkspaceRepository.RepositoryCallback<List<Workspace>>() {
            @Override
            public void onSuccess(List<Workspace> workspaces) {
                if (!workspaces.isEmpty()) {
                    workspaceRepository.getProjects(workspaces.get(0).getId(),
                        new IWorkspaceRepository.RepositoryCallback<List<Project>>() {
                        @Override
                        public void onSuccess(List<Project> projects) {
                            if (!projects.isEmpty()) {
                                workspaceRepository.getBoards(projects.get(0).getId(),
                                    new IWorkspaceRepository.RepositoryCallback<List<Board>>() {
                                    @Override
                                    public void onSuccess(List<Board> boards) {
                                        if (!boards.isEmpty()) {
                                            String boardId = boards.get(0).getId();

                                            // Test getTasksByBoard
                                            taskRepository.getTasksByBoard(boardId, new ITaskRepository.RepositoryCallback<List<Task>>() {
                                                @Override
                                                public void onSuccess(List<Task> result) {
                                                    log("✓ getTasksByBoard: SUCCESS");
                                                    log("  → Tìm thấy " + result.size() + " tasks");

                                                    for (int i = 0; i < Math.min(3, result.size()); i++) {
                                                        Task task = result.get(i);
                                                        log("  → Task " + (i+1) + ": " + task.getTitle());
                                                        log("    Status: " + task.getStatus());
                                                        log("    Priority: " + task.getPriority());
                                                    }
                                                }

                                                @Override
                                                public void onError(String error) {
                                                    log("✗ getTasksByBoard: FAILED - " + error);
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onError(String error) {
                                        log("✗ Không thể lấy boards - " + error);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            log("✗ Không thể lấy projects - " + error);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                log("✗ Không thể lấy workspaces - " + error);
            }
        });
    }

    private void testNotificationRepository() {
        log("\n=== Testing NotificationRepository ===");

        notificationRepository.getUnreadCount(new INotificationRepository.RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                log("✓ getUnreadCount: SUCCESS");
                log("  → Số notification chưa đọc: " + result);

                testGetNotifications();
            }

            @Override
            public void onError(String error) {
                log("✗ getUnreadCount: FAILED - " + error);
            }
        });
    }

    private void testGetNotifications() {
        notificationRepository.getNotifications(new INotificationRepository.RepositoryCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> result) {
                log("✓ getNotifications: SUCCESS");
                log("  → Tổng số notifications: " + result.size());

                for (int i = 0; i < Math.min(3, result.size()); i++) {
                    Notification notif = result.get(i);
                    log("  → " + (i+1) + ". " + notif.getTitle());
                    log("     Type: " + notif.getType());
                }
            }

            @Override
            public void onError(String error) {
                log("✗ getNotifications: FAILED - " + error);
            }
        });
    }

    private void testAllRepositories() {
        log("\n\n========== TESTING ALL REPOSITORIES ==========");
        testWorkspaceRepository();
        testProjectRepository();
        testBoardRepository();
        testTaskRepository();
        testNotificationRepository();
        log("\n==========   END OF ALL TESTS   ==========");
    }



    private void log(String message) {
        Log.d(TAG, message);
        tvLog.append(message + "\n");
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void clearLog() {
        tvLog.setText("");
        log("=== Repository Test Activity ===");
        log("Sẵn sàng test các Repository");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
