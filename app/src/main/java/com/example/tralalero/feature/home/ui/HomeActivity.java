package com.example.tralalero.feature.home.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.App.App;
import com.example.tralalero.MainActivity;
import com.example.tralalero.feature.home.adapter.WorkspaceAdapter;
import com.example.tralalero.feature.home.api.WorkspaceApiService;
import com.example.tralalero.feature.home.model.Workspace;
import com.example.tralalero.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        EditText cardNew = findViewById(R.id.cardNew);
        LinearLayout inboxForm = findViewById(R.id.inboxForm);

        cardNew.setOnClickListener(v -> inboxForm.setVisibility(View.VISIBLE));

        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
                    inboxForm.setVisibility(View.GONE);
                    cardNew.setText("");
                }
        );

        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            String text = cardNew.getText().toString().trim();

            if (!text.isEmpty()) {
                // 👉 gọi hàm lưu vào database
//                TODO: lưu vào database
//                saveToDatabase(text);

                Toast.makeText(this, "Đã thêm: " + text, Toast.LENGTH_SHORT).show();

                inboxForm.setVisibility(View.GONE);
                cardNew.setText(""); // clear sau khi lưu
            } else {
                Toast.makeText(this, "Vui lòng nhập dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout btnDefault = findViewById(R.id.default_button);
        btnDefault.setOnClickListener(v -> {
            Intent intent = new Intent(this, WorkspaceActivity.class);
            startActivity(intent);
        });


//        ImageButton btnBoard = findViewById(R.id.btn1);
//        btnBoard.setOnClickListener(v -> {
//            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
//            startActivity(intent);
//        });

        ImageButton btnInbox = findViewById(R.id.btn2);
        btnInbox.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, InboxActivity.class);
            startActivity(intent);
        });

        ImageButton btnActivity = findViewById(R.id.btn3);
        btnActivity.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ActivityActivity.class);
            startActivity(intent);
        });

        ImageButton btnAccount = findViewById(R.id.btn4);

        btnAccount.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AccountActivity.class);
            startActivity(intent);
        });
    }


//    private void handleAccount() {
//        Intent intent = new Intent(this, AccountActivity.class);
//        startActivity(intent);
//        finish();
//    }

}