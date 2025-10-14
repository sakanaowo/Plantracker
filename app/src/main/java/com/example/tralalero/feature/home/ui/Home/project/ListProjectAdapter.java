package com.example.tralalero.feature.home.ui.Home.project;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tralalero.presentation.viewmodel.TaskViewModel;
import com.example.tralalero.presentation.viewmodel.BoardViewModel;
import com.example.tralalero.presentation.viewmodel.BoardViewModelFactory;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.data.repository.BoardRepositoryImpl;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.domain.repository.IBoardRepository;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.remote.api.BoardApiService;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.App.App;
import com.example.tralalero.domain.usecase.task.*;
import com.example.tralalero.domain.usecase.board.*;
import com.example.tralalero.presentation.viewmodel.TaskViewModelFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for ViewPager2 in ProjectActivity
 * Creates 3 fragments: TO DO, IN PROGRESS, DONE
 * 
 * Phase 5 Integration:
 * - Uses TaskViewModel (shared across activity)
 * - Maps status to boardId
 * - Supports both legacy (projectId) and new (boardId) modes
 * 
 * @author Người 3
 * @date 14/10/2025
 */
public class ListProjectAdapter extends FragmentStateAdapter {
    
    private static final String TAG = "ListProjectAdapter";
    
    // Data
    private String projectId;
    private TaskViewModel taskViewModel;
    private BoardViewModel boardViewModel;
    
    // BoardId mapping: position -> boardId
    // Position 0 = TO_DO, 1 = IN_PROGRESS, 2 = DONE
    private Map<Integer, String> boardIdMap = new HashMap<>();
    
    // Status mapping
    private static final String[] STATUSES = {"TO_DO", "IN_PROGRESS", "DONE"};
    private static final String[] DISPLAY_NAMES = {"TO DO", "IN PROGRESS", "DONE"};

    // ===== CONSTRUCTORS =====
    
    public ListProjectAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        setupViewModels(fragmentActivity);
    }

    public ListProjectAdapter(@NonNull FragmentActivity fragmentActivity, String projectId) {
        super(fragmentActivity);
        this.projectId = projectId;
        setupViewModels(fragmentActivity);
        
        Log.d(TAG, "ListProjectAdapter created with projectId: " + projectId);
    }
    
    /**
     * Constructor with boardIds (Phase 5 - PREFERRED)
     */
    public ListProjectAdapter(@NonNull FragmentActivity fragmentActivity, 
                              String projectId, 
                              List<String> boardIds) {
        super(fragmentActivity);
        this.projectId = projectId;
        setupViewModels(fragmentActivity);
        setBoardIds(boardIds);
        
        Log.d(TAG, "ListProjectAdapter created with boardIds: " + boardIds);
    }

    // ===== SETUP METHODS =====
    
    /**
     * Setup ViewModels - shared across all fragments
     */
    private void setupViewModels(@NonNull FragmentActivity activity) {
        // Setup TaskViewModel
        setupTaskViewModel(activity);
        
        // Setup BoardViewModel
        setupBoardViewModel(activity);
    }
    
    private void setupTaskViewModel(@NonNull FragmentActivity activity) {
        // Create repository
        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        ITaskRepository repository = new TaskRepositoryImpl(apiService);
        
        // Create UseCases
        GetTaskByIdUseCase getTaskByIdUseCase = new GetTaskByIdUseCase(repository);
        GetTasksByBoardUseCase getTasksByBoardUseCase = new GetTasksByBoardUseCase(repository);
        CreateTaskUseCase createTaskUseCase = new CreateTaskUseCase(repository);
        UpdateTaskUseCase updateTaskUseCase = new UpdateTaskUseCase(repository);
        DeleteTaskUseCase deleteTaskUseCase = new DeleteTaskUseCase(repository);
        AssignTaskUseCase assignTaskUseCase = new AssignTaskUseCase(repository);
        UnassignTaskUseCase unassignTaskUseCase = new UnassignTaskUseCase(repository);
        MoveTaskToBoardUseCase moveTaskToBoardUseCase = new MoveTaskToBoardUseCase(repository);
        UpdateTaskPositionUseCase updateTaskPositionUseCase = new UpdateTaskPositionUseCase(repository);
        AddCommentUseCase addCommentUseCase = new AddCommentUseCase(repository);
        GetTaskCommentsUseCase getTaskCommentsUseCase = new GetTaskCommentsUseCase(repository);
        AddAttachmentUseCase addAttachmentUseCase = new AddAttachmentUseCase(repository);
        GetTaskAttachmentsUseCase getTaskAttachmentsUseCase = new GetTaskAttachmentsUseCase(repository);
        AddChecklistUseCase addChecklistUseCase = new AddChecklistUseCase(repository);
        GetTaskChecklistsUseCase getTaskChecklistsUseCase = new GetTaskChecklistsUseCase(repository);
        
        // Create Factory
        TaskViewModelFactory factory = new TaskViewModelFactory(
            getTaskByIdUseCase,
            getTasksByBoardUseCase,
            createTaskUseCase,
            updateTaskUseCase,
            deleteTaskUseCase,
            assignTaskUseCase,
            unassignTaskUseCase,
            moveTaskToBoardUseCase,
            updateTaskPositionUseCase,
            addCommentUseCase,
            getTaskCommentsUseCase,
            addAttachmentUseCase,
            getTaskAttachmentsUseCase,
            addChecklistUseCase,
            getTaskChecklistsUseCase
        );
        
        // Create ViewModel - SHARED across Activity
        taskViewModel = new ViewModelProvider(activity, factory).get(TaskViewModel.class);
        Log.d(TAG, "TaskViewModel initialized");
    }
    
    private void setupBoardViewModel(@NonNull FragmentActivity activity) {
        // Create board repository
        BoardApiService boardApiService = ApiClient.get(App.authManager).create(BoardApiService.class);
        IBoardRepository boardRepository = new BoardRepositoryImpl(boardApiService);
        
        // Create task repository for GetBoardTasksUseCase
        TaskApiService taskApiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        ITaskRepository taskRepository = new TaskRepositoryImpl(taskApiService);
        
        // Create UseCases
        GetBoardByIdUseCase getBoardByIdUseCase = new GetBoardByIdUseCase(boardRepository);
        CreateBoardUseCase createBoardUseCase = new CreateBoardUseCase(boardRepository);
        UpdateBoardUseCase updateBoardUseCase = new UpdateBoardUseCase(boardRepository);
        DeleteBoardUseCase deleteBoardUseCase = new DeleteBoardUseCase(boardRepository);
        ReorderBoardsUseCase reorderBoardsUseCase = new ReorderBoardsUseCase(boardRepository);
        GetBoardTasksUseCase getBoardTasksUseCase = new GetBoardTasksUseCase(taskRepository);  // ✅ Use taskRepository
        
        // Create Factory
        BoardViewModelFactory factory = new BoardViewModelFactory(
            getBoardByIdUseCase,
            createBoardUseCase,
            updateBoardUseCase,
            deleteBoardUseCase,
            reorderBoardsUseCase,
            getBoardTasksUseCase
        );
        
        // Create ViewModel
        boardViewModel = new ViewModelProvider(activity, factory).get(BoardViewModel.class);
        Log.d(TAG, "BoardViewModel initialized");
    }

    // ===== SETTERS =====
    
    public void setProjectId(String projectId) {
        this.projectId = projectId;
        notifyDataSetChanged();
        Log.d(TAG, "ProjectId updated: " + projectId);
    }
    
    /**
     * Set boardIds for each tab position
     * @param boardIds List of boardIds [TO_DO_boardId, IN_PROGRESS_boardId, DONE_boardId]
     */
    public void setBoardIds(List<String> boardIds) {
        boardIdMap.clear();
        
        if (boardIds != null) {
            for (int i = 0; i < Math.min(boardIds.size(), 3); i++) {
                boardIdMap.put(i, boardIds.get(i));
                Log.d(TAG, "Board mapping: position " + i + " -> boardId " + boardIds.get(i));
            }
            notifyDataSetChanged();
        }
    }
    
    /**
     * Set boardId for specific position
     * @param position Tab position (0=TO_DO, 1=IN_PROGRESS, 2=DONE)
     * @param boardId Board ID
     */
    public void setBoardIdForPosition(int position, String boardId) {
        boardIdMap.put(position, boardId);
        notifyItemChanged(position);
        Log.d(TAG, "Board updated: position " + position + " -> boardId " + boardId);
    }

    // ===== FRAGMENT CREATION =====
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String status = STATUSES[position];
        String displayName = DISPLAY_NAMES[position];
        String boardId = boardIdMap.get(position);
        
        Log.d(TAG, "Creating fragment for position " + position + 
                   ", status: " + status + 
                   ", boardId: " + boardId);
        
        // Phase 5 - PREFERRED: Use boardId
        if (boardId != null && !boardId.isEmpty()) {
            return ListProject.newInstance(displayName, projectId, boardId);
        }
        
        // Legacy mode: Use projectId + status
        if (projectId != null && !projectId.isEmpty()) {
            Log.w(TAG, "Using legacy mode for position " + position + 
                       " - boardId not available");
            return ListProject.newInstance(displayName, projectId);
        }
        
        // Fallback: Create empty fragment
        Log.w(TAG, "Creating fragment without projectId or boardId for position " + position);
        return ListProject.newInstance(displayName);
    }

    @Override
    public int getItemCount() {
        return 3; // TO DO, IN PROGRESS, DONE
    }
    
    // ===== GETTERS =====
    
    /**
     * Get TaskViewModel (shared across fragments)
     */
    public TaskViewModel getTaskViewModel() {
        return taskViewModel;
    }
    
    /**
     * Get BoardViewModel (shared across fragments)
     */
    public BoardViewModel getBoardViewModel() {
        return boardViewModel;
    }
    
    /**
     * Get boardId for specific position
     */
    public String getBoardIdForPosition(int position) {
        return boardIdMap.get(position);
    }
    
    /**
     * Get status for position
     */
    public String getStatusForPosition(int position) {
        if (position >= 0 && position < STATUSES.length) {
            return STATUSES[position];
        }
        return "TO_DO";
    }
    
    /**
     * Get display name for position
     */
    public String getDisplayNameForPosition(int position) {
        if (position >= 0 && position < DISPLAY_NAMES.length) {
            return DISPLAY_NAMES[position];
        }
        return "TO DO";
    }
}
