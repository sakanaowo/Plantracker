package com.example.tralalero.feature.home.ui.Home.project;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tralalero.presentation.viewmodel.BoardViewModel;

/**
 * Adapter for ViewPager2 to display project boards (TO DO, IN PROGRESS, DONE)
 * Phase 5: Updated to pass BoardViewModel to fragments
 * 
 * @author Người 2 - Phase 5
 * @date 14/10/2025
 */
public class ListProjectAdapter extends FragmentStateAdapter {
    private String projectId;
    private BoardViewModel boardViewModel;

    /**
     * Constructor without projectId (legacy support)
     * @deprecated Use constructor with boardViewModel
     */
    @Deprecated
    public ListProjectAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Constructor with projectId only (legacy support)
     * @deprecated Use constructor with boardViewModel
     */
    @Deprecated
    public ListProjectAdapter(@NonNull FragmentActivity fragmentActivity, String projectId) {
        super(fragmentActivity);
        this.projectId = projectId;
    }
    
    /**
     * Constructor with BoardViewModel injection
     * Phase 5: NEW - Inject BoardViewModel to pass to fragments
     * 
     * @param fragmentActivity Parent activity
     * @param projectId Project ID for loading boards
     * @param boardViewModel Shared BoardViewModel instance
     */
    public ListProjectAdapter(@NonNull FragmentActivity fragmentActivity, 
                             String projectId, 
                             BoardViewModel boardViewModel) {
        super(fragmentActivity);
        this.projectId = projectId;
        this.boardViewModel = boardViewModel;
    }

    /**
     * Update project ID and refresh
     */
    public void setProjectId(String projectId) {
        this.projectId = projectId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Map position to board status
        String boardStatus;
        String boardName;
        
        switch (position) {
            case 0:
                boardStatus = "TO_DO";
                boardName = "TO DO";
                break;
            case 1:
                boardStatus = "IN_PROGRESS";
                boardName = "IN PROGRESS";
                break;
            case 2:
                boardStatus = "DONE";
                boardName = "DONE";
                break;
            default:
                boardStatus = "TO_DO";
                boardName = "TO DO";
                break;
        }
        
        // Phase 5: Pass BoardViewModel to fragment
        if (boardViewModel != null && projectId != null) {
            return ListProject.newInstance(boardName, projectId, boardViewModel);
        } else if (projectId != null) {
            // Legacy: Without ViewModel (will use old approach)
            return ListProject.newInstance(boardName, projectId);
        } else {
            // Fallback: No projectId
            return ListProject.newInstance(boardName);
        }
    }

    @Override
    public int getItemCount() {
        return 3; // TO DO, IN PROGRESS, DONE
    }
}
