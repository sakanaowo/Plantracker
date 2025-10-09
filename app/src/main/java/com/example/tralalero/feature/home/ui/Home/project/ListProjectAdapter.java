package com.example.tralalero.feature.home.ui.Home.project;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ListProjectAdapter extends FragmentStateAdapter {
    private String projectId;

    public ListProjectAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public ListProjectAdapter(@NonNull FragmentActivity fragmentActivity, String projectId) {
        super(fragmentActivity);
        this.projectId = projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return projectId != null 
                    ? ListProject.newInstance("TO DO", projectId)
                    : ListProject.newInstance("TO DO");
            case 1:
                return projectId != null 
                    ? ListProject.newInstance("IN PROGRESS", projectId)
                    : ListProject.newInstance("IN PROGRESS");
            case 2:
                return projectId != null 
                    ? ListProject.newInstance("DONE", projectId)
                    : ListProject.newInstance("DONE");
            default:
                return projectId != null 
                    ? ListProject.newInstance("TO DO", projectId)
                    : ListProject.newInstance("TO DO");
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
