package com.example.tralalero.feature.home.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tralalero.R;
import com.example.tralalero.feature.home.ui.AccountActivity;
import com.example.tralalero.feature.home.ui.Home.HomeActivity;
import com.example.tralalero.feature.home.ui.ActivityActivity;
import com.example.tralalero.feature.home.ui.InboxActivity;
// Import thêm khi có Activity khác

public abstract class BaseActivity extends AppCompatActivity {

    protected com.example.tralalero.feature.home.ui.BottomNavigationFragment bottomNav;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Gọi method này AFTER setContentView() trong onCreate của Activity con
     * @param selectedPosition vị trí tab được highlight (0-3)
     */
    protected void setupBottomNavigation(int selectedPosition) {
        bottomNav = (com.example.tralalero.feature.home.ui.BottomNavigationFragment)
                getSupportFragmentManager().findFragmentById(R.id.bottomNavigation);

        if (bottomNav != null) {
            bottomNav.setSelectedItem(selectedPosition);
            bottomNav.setOnNavigationItemSelectedListener(position -> {
                // Không navigate nếu đang ở màn hình đó
                if (position == selectedPosition) {
                    return;
                }

                navigateToScreen(position);
            });
        }
    }

    private void navigateToScreen(int position) {
        Intent intent = null;

        switch (position) {
            case 0: // Workspace
                intent = new Intent(this, HomeActivity.class);
                break;
            case 1: // Quick Access
                intent = new Intent(this, InboxActivity.class);
                break;
            case 2: // Activity
                intent = new Intent(this, ActivityActivity.class);
                break;
            case 3: // Account
                intent = new Intent(this, AccountActivity.class);
                break;
        }

        if (intent != null) {
            // Clear back stack để tránh bị chồng Activity
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();

            // Animation mượt mà
        }
    }
}