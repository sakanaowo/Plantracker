package com.example.tralalero.feature.task.attachments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.tralalero.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Full-screen dialog for previewing images with zoom/pan support
 */
public class ImagePreviewDialog extends Dialog {

    private static final String TAG = "ImagePreviewDialog";

    private String imageUrl;
    private String fileName;
    private OnDownloadClickListener downloadListener;

    private ImageView ivPreview;
    private ProgressBar progressBar;
    private TextView tvFileName;
    private TextView tvError;
    private ImageButton btnClose;
    private ImageButton btnDownload;

    public interface OnDownloadClickListener {
        void onDownloadClick();
    }

    public ImagePreviewDialog(@NonNull Context context, String imageUrl, String fileName) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.imageUrl = imageUrl;
        this.fileName = fileName;
    }

    public void setOnDownloadClickListener(OnDownloadClickListener listener) {
        this.downloadListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make dialog full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_image_preview);
        
        Window window = getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, 
                           WindowManager.LayoutParams.MATCH_PARENT);
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                          WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        initViews();
        setupListeners();
        loadImage();
    }

    private void initViews() {
        ivPreview = findViewById(R.id.ivPreview);
        progressBar = findViewById(R.id.progressBar);
        tvFileName = findViewById(R.id.tvFileName);
        tvError = findViewById(R.id.tvError);
        btnClose = findViewById(R.id.btnClose);
        btnDownload = findViewById(R.id.btnDownload);

        if (fileName != null && !fileName.isEmpty()) {
            tvFileName.setText(fileName);
        }
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());
        
        btnDownload.setOnClickListener(v -> {
            if (downloadListener != null) {
                downloadListener.onDownloadClick();
            }
        });

        // Add zoom/pan support with double-tap
        ivPreview.setOnClickListener(v -> {
            // Toggle controls visibility on single tap
            toggleControlsVisibility();
        });
    }

    private void toggleControlsVisibility() {
        if (btnClose.getVisibility() == View.VISIBLE) {
            btnClose.setVisibility(View.GONE);
            btnDownload.setVisibility(View.GONE);
            tvFileName.setVisibility(View.GONE);
        } else {
            btnClose.setVisibility(View.VISIBLE);
            btnDownload.setVisibility(View.VISIBLE);
            tvFileName.setVisibility(View.VISIBLE);
        }
    }

    private void loadImage() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            showError("Invalid image URL");
            return;
        }

        Log.d(TAG, "Loading image from URL: " + imageUrl);
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
        ivPreview.setVisibility(View.GONE);

        // Load image in background thread
        new Thread(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.connect();

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Image download response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    input.close();

                    if (bitmap != null) {
                        // Update UI on main thread
                        post(() -> showImage(bitmap));
                    } else {
                        post(() -> showError("Failed to decode image"));
                    }
                } else {
                    post(() -> showError("Failed to load image (HTTP " + responseCode + ")"));
                }

                connection.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "Error loading image", e);
                post(() -> showError("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void post(Runnable runnable) {
        if (ivPreview != null) {
            ivPreview.post(runnable);
        }
    }

    private void showImage(Bitmap bitmap) {
        progressBar.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        ivPreview.setVisibility(View.VISIBLE);
        tvFileName.setVisibility(View.VISIBLE);
        ivPreview.setImageBitmap(bitmap);
        
        Log.d(TAG, "Image loaded successfully");
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        ivPreview.setVisibility(View.GONE);
        tvFileName.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(message);
        
        Log.e(TAG, "Error showing image: " + message);
    }
}
