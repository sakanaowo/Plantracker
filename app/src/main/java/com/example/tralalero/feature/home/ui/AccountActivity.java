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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tralalero.App.App;
import com.example.tralalero.core.DependencyProvider;
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
    private ImageView imgAvatar;
    private ImageButton btnAccountOptions;
    private Uri currentPhotoUri;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String> storagePermissionLauncher;
    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.account);
        View scrollView = findViewById(R.id.scrollViewAccount);
        View bottomNav = findViewById(R.id.bottomNavigation);

        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return WindowInsetsCompat.CONSUMED;
        });

        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        tokenManager = new TokenManager(this);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        setupActivityResultLaunchers();

        TextView tvName = findViewById(R.id.tvName);
        TextView tvEmail = findViewById(R.id.tvEmail);
        tvAvatarLetter = findViewById(R.id.tvAvatarLetter);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnAccountOptions = findViewById(R.id.btnAccountOptions);

        if (firebaseUser != null) {
            String email = firebaseUser.getEmail();
            String displayName = firebaseUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvName.setText(displayName);
                tvAvatarLetter.setText(String.valueOf(displayName.charAt(0)).toUpperCase());
            } else if (email != null) {
                String username = email.split("@")[0];
                tvName.setText(username);
                tvAvatarLetter.setText(String.valueOf(username.charAt(0)).toUpperCase());
            }
            if (email != null) {
                tvEmail.setText(email);
            }
            loadUserProfileFromBackend();
        }

        FrameLayout avatarContainer = findViewById(R.id.avatarContainer);
        avatarContainer.setOnClickListener(v -> showImagePickerBottomSheet());
        if (btnAccountOptions != null) {
            Log.d(TAG, "Setting up click listener for btnAccountOptions");
            btnAccountOptions.setOnClickListener(v -> {
                Log.d(TAG, "btnAccountOptions CLICKED!");
                showAccountOptionsMenu(v);
            });
        } else {
            Log.e(TAG, "btnAccountOptions is NULL!");
        }

        LinearLayout layoutSettings = findViewById(R.id.layoutSettings);
        layoutSettings.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        setupBottomNavigation(3);

        LinearLayout layoutOfflineBoards = findViewById(R.id.layoutOfflineBoards);
        layoutOfflineBoards.setOnClickListener(v -> {
            // TODO: Implement offline boards
        });

        LinearLayout layoutBrowseTemplates = findViewById(R.id.layoutBrowseTemplates);
        layoutBrowseTemplates.setOnClickListener(v -> {
            // TODO: Implement browse templates
        });
    }

    private void setupActivityResultLaunchers() {
        editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    refreshProfileUI();
                }
            }
        );

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
        updateBackendProfile(null, avatarUrl);
        runOnUiThread(() -> {
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                tvAvatarLetter.setVisibility(View.GONE);
                if (imgAvatar != null) {
                    imgAvatar.setVisibility(View.VISIBLE);
                }
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

    private void showAccountOptionsMenu(View anchor) {
        Log.d(TAG, "showAccountOptionsMenu called");
        try {
            PopupMenu popup = new PopupMenu(this, anchor);
            popup.getMenuInflater().inflate(R.menu.account_options_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                Log.d(TAG, "Menu item clicked: " + item.getTitle());
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit_profile) {
                    openEditProfileActivity();
                    return true;
                } else if (itemId == R.id.action_logout) {
                    showLogoutDialog();
                    return true;
                }
                return false;
            });

            popup.show();
            Log.d(TAG, "Popup menu shown successfully");
        } catch (Exception e) {
            Log.e(TAG, "Exception in showAccountOptionsMenu", e);
            Toast.makeText(this, "Menu error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    Log.d(TAG, "User confirmed logout");
                    performLogout();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openEditProfileActivity() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        editProfileLauncher.launch(intent);
    }

    private void refreshProfileUI() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            TextView tvName = findViewById(R.id.tvName);
            TextView tvEmail = findViewById(R.id.tvEmail);

            String displayName = firebaseUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvName.setText(displayName);
                tvAvatarLetter.setText(String.valueOf(displayName.charAt(0)).toUpperCase());
            } else {
                String email = firebaseUser.getEmail();
                if (email != null) {
                    String username = email.split("@")[0];
                    tvName.setText(username);
                    tvAvatarLetter.setText(String.valueOf(username.charAt(0)).toUpperCase());
                }
            }
            loadUserProfileFromBackend();

            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfileFromBackend() {
        AuthApi authApi = ApiClient.get(App.authManager).create(AuthApi.class);

        authApi.getMe().enqueue(new retrofit2.Callback<UserDto>() {
            @Override
            public void onResponse(retrofit2.Call<UserDto> call, retrofit2.Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDto user = response.body();
                    Log.d(TAG, "Loaded user profile from backend");
                    if (user.name != null && !user.name.isEmpty()) {
                        TextView tvName = findViewById(R.id.tvName);
                        tvName.setText(user.name);
                        tvAvatarLetter.setText(String.valueOf(user.name.charAt(0)).toUpperCase());
                    }
                    if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
                        Log.d(TAG, "Loading avatar from: " + user.avatarUrl);
                        loadAvatarImage(user.avatarUrl);
                    } else {
                        Log.d(TAG, "No avatar URL, showing letter");
                        imgAvatar.setVisibility(View.GONE);
                        tvAvatarLetter.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e(TAG, "Failed to load profile: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<UserDto> call, Throwable t) {
                Log.e(TAG, "Network error loading profile", t);
            }
        });
    }

    private void loadAvatarImage(String avatarUrl) {
        Log.d(TAG, "loadAvatarImage called with URL: " + avatarUrl);
        tvAvatarLetter.setVisibility(View.VISIBLE);
        imgAvatar.setVisibility(View.GONE);

        // ✅ Fix: Check if Activity is still alive before loading with Glide
        if (isFinishing() || isDestroyed()) {
            Log.w(TAG, "Activity destroyed, skipping Glide load");
            return;
        }

        Glide.with(this)
            .load(avatarUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .circleCrop()
            .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                @Override
                public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, 
                                          Object model, 
                                          com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, 
                                          boolean isFirstResource) {
                    Log.e(TAG, "Failed to load avatar image from: " + avatarUrl, e);
                    if (e != null) {
                        Log.e(TAG, "Glide error causes: ", e);
                        for (Throwable cause : e.getRootCauses()) {
                            Log.e(TAG, "Root cause: ", cause);
                        }
                    }
                    imgAvatar.setVisibility(View.GONE);
                    tvAvatarLetter.setVisibility(View.VISIBLE);
                    return true; // handled
                }

                @Override
                public boolean onResourceReady(android.graphics.drawable.Drawable resource, 
                                             Object model, 
                                             com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, 
                                             com.bumptech.glide.load.DataSource dataSource, 
                                             boolean isFirstResource) {
                    Log.d(TAG, "✓ Avatar loaded successfully from: " + avatarUrl);
                    Log.d(TAG, "Data source: " + dataSource);
                    imgAvatar.setVisibility(View.VISIBLE);
                    tvAvatarLetter.setVisibility(View.GONE);
                    return false; // let Glide handle displaying
                }
            })
            .into(imgAvatar);
    }

    private void showEditNameDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_name, null);
        com.google.android.material.textfield.TextInputEditText etName = 
            dialogView.findViewById(R.id.etEditName);
        if (firebaseUser != null && firebaseUser.getDisplayName() != null) {
            etName.setText(firebaseUser.getDisplayName());
            etName.setSelection(firebaseUser.getDisplayName().length()); // Move cursor to end
        }

        new AlertDialog.Builder(this)
                .setTitle("Edit Name")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        updateProfileName(newName);
                    } else {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateProfileName(String newName) {
        Log.d(TAG, "Updating profile name to: " + newName);
        Toast.makeText(this, "Updating name...", Toast.LENGTH_SHORT).show();
        if (firebaseUser != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            firebaseUser.updateProfile(profileUpdates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✓ Firebase profile updated");
                        updateBackendProfile(newName, null);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update Firebase profile", e);
                        Toast.makeText(this, "Failed to update name: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void updateBackendProfile(String name, String avatarUrl) {
        AuthApi authApi = ApiClient.get(App.authManager).create(AuthApi.class);
        UpdateProfileRequest request = new UpdateProfileRequest(name, avatarUrl);

        authApi.updateProfile(request).enqueue(new retrofit2.Callback<UserDto>() {
            @Override
            public void onResponse(retrofit2.Call<UserDto> call, retrofit2.Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✓ Backend profile updated successfully");
                    if (name != null) {
                        tokenManager.saveAuthData(
                            tokenManager.getFirebaseIdToken(),
                            tokenManager.getUserId(),
                            tokenManager.getUserEmail(),
                            name
                        );
                    }
                    runOnUiThread(() -> {
                        TextView tvName = findViewById(R.id.tvName);
                        TextView tvAvatarLetter = findViewById(R.id.tvAvatarLetter);

                        if (name != null) {
                            tvName.setText(name);
                            tvAvatarLetter.setText(String.valueOf(name.charAt(0)).toUpperCase());
                        }

                        Toast.makeText(AccountActivity.this, 
                            "✅ Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Log.e(TAG, "Failed to update backend profile: " + response.code());
                    runOnUiThread(() -> 
                        Toast.makeText(AccountActivity.this, 
                            "Failed to update profile on server", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onFailure(retrofit2.Call<UserDto> call, Throwable t) {
                Log.e(TAG, "Network error updating backend profile", t);
                runOnUiThread(() -> 
                    Toast.makeText(AccountActivity.this, 
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void performLogout() {
        Log.d(TAG, "Performing logout...");
        FirebaseAuth.getInstance().signOut();
        App.authManager.signOut();
        Log.d(TAG, "✓ Auth cleared");
        App.dependencyProvider.clearAllCaches();
        Log.d(TAG, "✓ Database cache cleared");
        DependencyProvider.reset();
        Log.d(TAG, "✓ DependencyProvider reset");
        tokenManager.clearAuthData();
        Log.d(TAG, "✓ Token cleared");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Log.d(TAG, "✓ Logout complete");
    }
}
