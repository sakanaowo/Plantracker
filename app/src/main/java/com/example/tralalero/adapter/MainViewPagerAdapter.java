package com.example.tralalero.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tralalero.feature.home.ui.fragments.HomeFragment;
import com.example.tralalero.feature.home.ui.fragments.InboxFragment;
import com.example.tralalero.feature.home.ui.fragments.ActivityFragment;
import com.example.tralalero.feature.home.ui.fragments.AccountFragment;

/**
 * ViewPager2 Adapter for main app screens
 * Enables swipe navigation between:
 * 0 - Home (Projects)
 * 1 - Inbox (Quick Tasks)
 * 2 - Activity (Notifications)
 * 3 - Account (Profile)
 */
public class MainViewPagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 4;

    public MainViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new InboxFragment();
            case 2:
                return new ActivityFragment();
            case 3:
                return new AccountFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
