package com.example.tralalero.feature.home.ui.Home.project;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ListProjectAdapter extends FragmentStateAdapter {
    public ListProjectAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
         switch (position) {
            case 0:
                return ListProject.newInstance("TO DO");
            case 1:
                return ListProject.newInstance("IN PROGRESS");
            case 2:
                return ListProject.newInstance("DONE");
            default:
                return ListProject.newInstance("TO DO");
        }
    }
    @Override
    public int getItemCount() {
        return 3;
    }
}
