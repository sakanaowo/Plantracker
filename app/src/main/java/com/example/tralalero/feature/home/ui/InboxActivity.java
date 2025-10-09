package com.example.tralalero.feature.home.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tralalero.MainActivity;
import com.example.tralalero.R;
import com.google.android.material.textfield.TextInputEditText;

public class InboxActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.inbox_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RelativeLayout notiLayout = findViewById(R.id.notificationLayout);
        ImageButton btnCloseNotification = findViewById(R.id.btnClosePjrDetail);
        btnCloseNotification.setOnClickListener(v -> notiLayout.setVisibility(View.GONE));

        LinearLayout inboxQuickAccess = findViewById(R.id.inboxQuickAccess);
        TextInputEditText inboxAddCard = findViewById(R.id.inboxAddCard);
        inboxAddCard.setOnClickListener(v -> inboxQuickAccess.setVisibility(View.VISIBLE));

        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
                    inboxQuickAccess.setVisibility(View.GONE);
                    inboxAddCard.setText("");
                }
        );
        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            String text = inboxAddCard.getText().toString().trim();

            if (!text.isEmpty()) {
                // 👉 gọi hàm lưu vào database
//                TODO: lưu vào database
//                saveToDatabase(text);

                Toast.makeText(this, "Đã thêm: " + text, Toast.LENGTH_SHORT).show();

                inboxQuickAccess.setVisibility(View.GONE);
                inboxAddCard.setText(""); // clear sau khi lưu
            } else {
                Toast.makeText(this, "Vui lòng nhập dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton btnBoard = findViewById(R.id.btn1);
        btnBoard.setOnClickListener(v -> {
            Intent intent = new Intent(InboxActivity.this, MainActivity.class);
            startActivity(intent);
        });

        ImageButton btnInbox = findViewById(R.id.btn2);
        btnInbox.setOnClickListener(v -> {
            Intent intent = new Intent(InboxActivity.this, InboxActivity.class);
            startActivity(intent);
        });

        ImageButton btnActivity = findViewById(R.id.btn3);
        btnActivity.setOnClickListener(v -> {
            Intent intent = new Intent(InboxActivity.this, ActivityActivity.class);
            startActivity(intent);
        });

        ImageButton btnAccount = findViewById(R.id.btn4);

        btnAccount.setOnClickListener(v -> {
            Intent intent = new Intent(InboxActivity.this, AccountActivity.class);
            startActivity(intent);
        });
    }
}
