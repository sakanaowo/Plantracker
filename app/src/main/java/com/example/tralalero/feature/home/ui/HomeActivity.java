package com.example.tralalero.feature.home.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tralalero.R;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String name = getIntent().getStringExtra("user_name");
        String email = getIntent().getStringExtra("user_email");

        TextView tvTitle = findViewById(R.id.tv_home_title);
        if (tvTitle != null) {
            String who = (name != null && !name.isEmpty()) ? name : (email != null ? email : "");
            String text = who.isEmpty() ? "Welcome to Plantracker" : "Welcome, " + who;
            tvTitle.setText(text);
        }
    }
}

