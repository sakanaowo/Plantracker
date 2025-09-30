package com.example.tralalero.feature.home.ui;

import android.content.Intent;
import android.os.Bundle;
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

public class ActivityActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_activity_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager2 = findViewById(R.id.viewPager2);
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


        ImageButton btnBoard = findViewById(R.id.btn1);
        btnBoard.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityActivity.this, MainActivity.class);
            startActivity(intent);
        });

        ImageButton btnInbox = findViewById(R.id.btn2);
        btnInbox.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityActivity.this, InboxActivity.class);
            startActivity(intent);
        });

        ImageButton btnActivity = findViewById(R.id.btn3);
        btnActivity.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityActivity.this, ActivityActivity.class);
            startActivity(intent);
        });

        ImageButton btnAccount = findViewById(R.id.btn4);

        btnAccount.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityActivity.this, AccountActivity.class);
            startActivity(intent);
        });
    }
}
