package com.example.tralalero.feature.home.ui.Activity;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
public class ListFrmAdapter extends FragmentStateAdapter {
    public ListFrmAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return ListFragment.newInstance("All");
            case 1:
                return ListFragment.newInstance("Mentions");
            case 2:
                return ListFragment.newInstance("Unread");
            default:
                return ListFragment.newInstance("All");
        }
    }
    @Override
    public int getItemCount() {
        return 3;
    }
}