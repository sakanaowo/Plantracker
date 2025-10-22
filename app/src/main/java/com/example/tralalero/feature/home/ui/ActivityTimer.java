package com.example.tralalero.feature.home.ui;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tralalero.R;
import java.util.Date;
import java.util.Locale;
public class ActivityTimer extends AppCompatActivity {
    private TextView tvTimer;
    private ImageButton btnPlay;
    private ImageView ivClose;
    private TextView etDateStart, etDueDate;
    private Runnable runnable;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;
    private long elapsedTime = 0;
    private boolean check_start_Date = true, check_time = true, check_date_input = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_detail);
        etDateStart = findViewById(R.id.etDateStart);
        etDueDate = findViewById(R.id.etDueDate);
        ivClose = findViewById(R.id.ivClose);
        setDateFieldsEnabled(true);
        updateTimerText();
        etDateStart.setOnFocusChangeListener((v, hasFocus) -> {
            btnPlay.setEnabled(false);
        });
        etDueDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                btnPlay.setEnabled(false);
                btnPlay.setAlpha(0.5f);
            } else {
                btnPlay.setEnabled(true);
                btnPlay.setAlpha(1f);
            }
        });
        btnPlay.setOnClickListener(v -> {
            if (!isRunning) {
                startTimer();
            } else {
                pauseTimer();
            }
        });
        ivClose.setOnClickListener(v -> finish());
    }
    private void setDateFieldsEnabled(boolean enabled) {
        etDateStart.setEnabled(enabled);
        etDueDate.setEnabled(enabled);
    }
    private void startTimer() {
        if (isRunning) return;
        btnPlay.setImageResource(R.drawable.ic_pause);
        setDateFieldsEnabled(false);
        if (check_start_Date) {
            long startMillis = System.currentTimeMillis();
            Date now = new Date(startMillis);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String formattedDateTime = sdf.format(now);
            if (etDateStart != null) {
                etDateStart.setText(formattedDateTime);
            }
            check_start_Date = false;
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                elapsedTime += 1000;
                updateTimerText();
                handler.postDelayed(this, 1000); // gọi lại sau 1 giây
            }
        };
        handler.post(runnable);
        isRunning = true;
        Toast.makeText(this, "Bắt đầu đếm giờ!", Toast.LENGTH_SHORT).show();
    }
    private void pauseTimer() {
        if (!isRunning) return;
        handler.removeCallbacks(runnable);
        isRunning = false;
        btnPlay.setImageResource(R.drawable.ic_play);
        long startMillis = System.currentTimeMillis();
        Date now = new Date(startMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String formattedDateTime = sdf.format(now);
        if (etDueDate != null) {
            etDueDate.setText(formattedDateTime);
        }
        Toast.makeText(this, "Tạm dừng!", Toast.LENGTH_SHORT).show();
    }
    private void updateTimerText() {
        int hours = (int) (elapsedTime / 1000) / 3600;
        int minutes = (int) (elapsedTime / 1000) / 60;
        int seconds = (int) (elapsedTime / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        tvTimer.setText(timeFormatted);
    }
    private void sendNotification() {
    }
    private void createNotificationChannel() {
    }
}
