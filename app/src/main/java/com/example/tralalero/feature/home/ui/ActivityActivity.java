package com.example.tralalero.feature.home.ui;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.example.tralalero.MainActivity;
import com.example.tralalero.R;
import com.example.tralalero.feature.home.ui.Activity.ListFrmAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
public class ActivityActivity extends com.example.tralalero.feature.home.ui.BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_activity_notification);

        // Apply window insets properly - separate for content and navigation
        View titleView = findViewById(R.id.tvTitle);
        View bottomNav = findViewById(R.id.bottomNavigation);

        ViewCompat.setOnApplyWindowInsetsListener(titleView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return WindowInsetsCompat.CONSUMED;
        });

        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager2 = findViewById(R.id.PrjViewPager2);
        ListFrmAdapter adapter = new ListFrmAdapter(this);
        viewPager2.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("All");
                    break;
                case 1:
                    tab.setText("Mentions");
                    break;
                case 2:
                    tab.setText("Unread");
                    break;
            }
        }).attach();
        setupBottomNavigation(2); 
    }
}
