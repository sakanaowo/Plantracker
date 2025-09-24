package com.example.tralalero.feature.home.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tralalero.MainActivity;
import com.example.tralalero.R;

public class NewBoard extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_board);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageButton btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            Intent intent = new Intent(NewBoard.this, MainActivity.class);
            startActivity(intent);
        });

        TextView btnCreate = findViewById(R.id.btnCreate);
        EditText editBoardName = findViewById(R.id.edtBoardName);

        btnCreate.setOnClickListener(v -> {
            String workspaceName = editBoardName.getText().toString();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("workspace_name", workspaceName);

            setResult(RESULT_OK, resultIntent);
            finish(); // đóng BoardActivity để quay về YourWorkspaceActivity
        });

    }
}
