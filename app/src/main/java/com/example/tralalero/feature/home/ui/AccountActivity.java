package com.example.tralalero.feature.home.ui;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.example.tralalero.App.App;
import com.example.tralalero.MainActivity;
import com.example.tralalero.R;
import com.example.tralalero.auth.remote.AuthApi;
import com.example.tralalero.auth.remote.dto.UpdateProfileRequest;
import com.example.tralalero.data.remote.dto.auth.UserDto;
import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.feature.auth.ui.login.LoginActivity;
import com.example.tralalero.network.ApiClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.IOException;
public class AccountActivity extends com.example.tralalero.feature.home.ui.BaseActivity {
    private static final String TAG = "AccountActivity";
    private TokenManager tokenManager;
    private FirebaseUser firebaseUser;
    private TextView tvAvatarLetter;
    private View avatarCircle;
    private ImageView imgAvatar;
    private Uri currentPhotoUri;
    private MaterialButton btnLogout;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String> storagePermissionLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account);
        tokenManager = new TokenManager(this);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        setupActivityResultLaunchers();
        TextView tvName = findViewById(R.id.tvName);
        TextView tvUsername = findViewById(R.id.tvUsername);
        TextView tvEmail = findViewById(R.id.tvEmail);
        tvAvatarLetter = findViewById(R.id.tvAvatarLetter);
        avatarCircle = findViewById(R.id.avatarCircle);
        if (firebaseUser != null) {
            String email = firebaseUser.getEmail();
            String displayName = firebaseUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvName.setText(displayName);
                tvUsername.setText("@" + displayName.toLowerCase().replace(" ", ""));
                tvAvatarLetter.setText(String.valueOf(displayName.charAt(0)).toUpperCase());
            } else if (email != null) {
                String username = email.split("@")[0];
                tvName.setText(username);
                tvUsername.setText("@" + username);
                tvAvatarLetter.setText(String.valueOf(username.charAt(0)).toUpperCase());
            }
            if (email != null) {
                tvEmail.setText(email);
            }
        }
        FrameLayout avatarContainer = findViewById(R.id.avatarContainer);
        avatarContainer.setOnClickListener(v -> showImagePickerBottomSheet());
        LinearLayout layoutSettings = findViewById(R.id.layoutSettings);
        layoutSettings.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
        setupBottomNavigation(3);
        LinearLayout layoutOfflineBoards = findViewById(R.id.layoutOfflineBoards);
        layoutOfflineBoards.setOnClickListener(v -> {
            // TODO: Implement offline boards
        });
        LinearLayout layoutBrowseTemplates = findViewById(R.id.layoutBrowseTemplates);
        layoutBrowseTemplates.setOnClickListener(v -> {
            // TODO: Implement browse templates
        });
        LinearLayout layoutShareFeedback = findViewById(R.id.layoutShareFeedback);
        layoutShareFeedback.setOnClickListener(v -> {
            // TODO: Implement share feedback
        });
        LinearLayout layoutHelp = findViewById(R.id.layoutHelp);
        layoutHelp.setOnClickListener(v -> {
            // TODO: Implement help
        });
    }
    private void setupActivityResultLaunchers() {
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadImageToFirebase(imageUri);
                    }
                }
            }
        );
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && currentPhotoUri != null) {
                    uploadImageToFirebase(currentPhotoUri);
                }
            }
        );
        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        );
        storagePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openGallery();
                } else {
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    private void showImagePickerBottomSheet() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_profile_image, null);
        bottomSheet.setContentView(view);
        view.findViewById(R.id.layoutTakePhoto).setOnClickListener(v -> {
            bottomSheet.dismiss();
            checkCameraPermissionAndOpen();
        });
        view.findViewById(R.id.layoutChooseGallery).setOnClickListener(v -> {
            bottomSheet.dismiss();
            checkStoragePermissionAndOpen();
        });
        view.findViewById(R.id.layoutRemovePhoto).setOnClickListener(v -> {
            bottomSheet.dismiss();
            removeProfilePhoto();
        });
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> bottomSheet.dismiss());
        bottomSheet.show();
    }
    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
    private void checkStoragePermissionAndOpen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }
    private void openCamera() {
        try {
            File photoFile = createImageFile();
            currentPhotoUri = FileProvider.getUriForFile(this, 
                getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(currentPhotoUri);
        } catch (IOException e) {
            Log.e(TAG, "Error creating image file", e);
            Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show();
        }
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }
    private File createImageFile() throws IOException {
        String imageFileName = "profile_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
    private void uploadImageToFirebase(Uri imageUri) {
        if (firebaseUser == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Starting upload for user: " + firebaseUser.getUid());
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/" + firebaseUser.getUid() + ".jpg");
        Log.d(TAG, "Upload path: profile_images/" + firebaseUser.getUid() + ".jpg");
        profileImageRef.putFile(imageUri)
            .addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Upload successful, getting download URL...");
                profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d(TAG, "Download URL obtained: " + uri.toString());
                    updateUserProfile(uri);
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get download URL", e);
                    Toast.makeText(this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to upload image. Error: " + e.getMessage(), e);
                String errorMsg = "Upload failed: " + e.getMessage();
                if (e.getMessage() != null && e.getMessage().contains("permission")) {
                    errorMsg = "Permission denied. Please check Firebase Storage rules.";
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            });
    }
    private void updateUserProfile(Uri photoUri) {
        if (firebaseUser == null) return;
        String photoUrl = photoUri.toString();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
            .setPhotoUri(photoUri)
            .build();
        firebaseUser.updateProfile(profileUpdates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Firebase profile updated: " + photoUrl);
                syncProfileImageWithBackend(photoUrl);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to update Firebase profile", e);
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            });
    }
    private void syncProfileImageWithBackend(String avatarUrl) {
        AuthApi authApi = ApiClient.get(App.authManager).create(AuthApi.class);
        UpdateProfileRequest request = UpdateProfileRequest.withAvatar(avatarUrl);
        authApi.updateProfile(request).enqueue(new retrofit2.Callback<UserDto>() {
            @Override
            public void onResponse(retrofit2.Call<UserDto> call, retrofit2.Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDto user = response.body();
                    Log.d(TAG, "Backend profile updated successfully");
                    Toast.makeText(AccountActivity.this, "Profile photo updated!", Toast.LENGTH_SHORT).show();
                    // TODO: Update UI to show new image
                } else {
                    Log.e(TAG, "Backend update failed: " + response.code());
                    Toast.makeText(AccountActivity.this, "Profile updated in Firebase only", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(retrofit2.Call<UserDto> call, Throwable t) {
                Log.e(TAG, "Backend sync failed", t);
                Toast.makeText(AccountActivity.this, "Profile updated in Firebase only", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void removeProfilePhoto() {
        if (firebaseUser == null) return;
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
            .setPhotoUri(null)
            .build();
        firebaseUser.updateProfile(profileUpdates)
            .addOnSuccessListener(aVoid -> {
                syncProfileImageWithBackend(null);
                tvAvatarLetter.setVisibility(View.VISIBLE);
                if (imgAvatar != null) {
                    imgAvatar.setVisibility(View.GONE);
                }
                Toast.makeText(this, "Profile photo removed", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to remove profile photo", e);
                Toast.makeText(this, "Failed to remove photo", Toast.LENGTH_SHORT).show();
            });
    }
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> logout())
            .setNegativeButton("No", null)
            .show();
    }
    private void logout() {
        Log.d(TAG, "Logout button clicked");
        FirebaseAuth.getInstance().signOut();
        Log.d(TAG, "User signed out from Firebase");
        tokenManager.clearAuthData();
        Log.d(TAG, "Auth data cleared");
        Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Log.d(TAG, "Navigated to LoginActivity");
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}
