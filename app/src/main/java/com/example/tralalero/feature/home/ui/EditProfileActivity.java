package com.example.tralalero.feature.home.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tralalero.App.App;
import com.example.tralalero.R;
import com.example.tralalero.auth.remote.AuthApi;
import com.example.tralalero.auth.remote.dto.UpdateProfileRequest;
import com.example.tralalero.auth.remote.dto.UploadUrlRequest;
import com.example.tralalero.auth.remote.dto.UploadUrlResponse;
import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.core.SupabaseConfig;
import com.example.tralalero.data.remote.dto.auth.UserDto;
import com.example.tralalero.network.ApiClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private MaterialToolbar toolbar;
    private FrameLayout avatarContainer;
    private ImageView imgAvatar;
    private TextInputEditText etName;
    private TextInputEditText etUsername;
    private TextInputEditText etEmail;
    private TextInputEditText etBio;
    private TextInputEditText etJobTitle;
    private TextInputEditText etPhoneNumber;
    private MaterialButton btnSave;
    private MaterialButton btnCancel;
    private FrameLayout loadingOverlay;
    private FirebaseUser firebaseUser;
    private TokenManager tokenManager;
    private Uri currentPhotoUri;
    private String newAvatarUrl;
    private boolean avatarChanged = false;
    
    // Store original values for comparison
    private String originalBio = "";
    private String originalJobTitle = "";
    private String originalPhoneNumber = "";
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String> storagePermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        initViews();
        setupActivityResultLaunchers();
        setupToolbar();
        setupClickListeners();
        loadCurrentProfile();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        avatarContainer = findViewById(R.id.avatarContainer);
        imgAvatar = findViewById(R.id.imgAvatar);
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etBio = findViewById(R.id.etBio);
        etJobTitle = findViewById(R.id.etJobTitle);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        tokenManager = new TokenManager(this);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        avatarContainer.setOnClickListener(v -> showImagePickerBottomSheet());
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = s.toString().trim();
                if (!name.isEmpty()) {
                    String username = "@" + name.toLowerCase().replace(" ", "");
                    etUsername.setText(username);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        btnSave.setOnClickListener(v -> saveProfile());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadCurrentProfile() {
        if (firebaseUser == null) return;

        String email = firebaseUser.getEmail();
        String displayName = firebaseUser.getDisplayName();
        if (email != null) {
            etEmail.setText(email);
        }
        if (displayName != null && !displayName.isEmpty()) {
            etName.setText(displayName);
            etUsername.setText("@" + displayName.toLowerCase().replace(" ", ""));
        } else if (email != null) {
            String username = email.split("@")[0];
            etName.setText(username);
            etUsername.setText("@" + username);
        }
        fetchUserProfileFromBackend();
    }

    private void fetchUserProfileFromBackend() {
        AuthApi authApi = ApiClient.get(App.authManager).create(AuthApi.class);

        authApi.getMe().enqueue(new retrofit2.Callback<UserDto>() {
            @Override
            public void onResponse(retrofit2.Call<UserDto> call, retrofit2.Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDto user = response.body();
                    Log.d(TAG, "Fetched user profile from backend");
                    if (user.name != null && !user.name.isEmpty()) {
                        etName.setText(user.name);
                        etUsername.setText("@" + user.name.toLowerCase().replace(" ", ""));
                    }
                    if (user.bio != null && !user.bio.isEmpty()) {
                        originalBio = user.bio;
                        etBio.setText(user.bio);
                    }
                    if (user.jobTitle != null && !user.jobTitle.isEmpty()) {
                        originalJobTitle = user.jobTitle;
                        etJobTitle.setText(user.jobTitle);
                    }
                    if (user.phoneNumber != null && !user.phoneNumber.isEmpty()) {
                        originalPhoneNumber = user.phoneNumber;
                        etPhoneNumber.setText(user.phoneNumber);
                    }
                    if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
                        Log.d(TAG, "Loading avatar from: " + user.avatarUrl);
                        loadAvatarImage(user.avatarUrl);
                    } else {
                        Log.d(TAG, "No avatar URL found");
                    }
                } else {
                    Log.e(TAG, "Failed to fetch profile: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<UserDto> call, Throwable t) {
                Log.e(TAG, "Network error fetching profile", t);
            }
        });
    }

    private void loadAvatarImage(String avatarUrl) {
        Log.d(TAG, "loadAvatarImage called with URL: " + avatarUrl);
        
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            Log.w(TAG, "Avatar URL is null or empty, using default background");
            return;
        }
        
        // Show image view immediately
        imgAvatar.setVisibility(View.VISIBLE);
        
        // Load with cache strategy based on whether it's from backend or newly uploaded
        DiskCacheStrategy cacheStrategy = avatarChanged 
            ? DiskCacheStrategy.NONE  // Don't cache newly uploaded images
            : DiskCacheStrategy.ALL;   // Cache existing images from backend
        
        Glide.with(this)
            .load(avatarUrl)
            .diskCacheStrategy(cacheStrategy)
            .skipMemoryCache(avatarChanged) // Skip memory cache only for newly uploaded
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
                    // Keep default background on error
                    return false; // Let Glide handle error drawable
                }

                @Override
                public boolean onResourceReady(android.graphics.drawable.Drawable resource, 
                                             Object model, 
                                             com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, 
                                             com.bumptech.glide.load.DataSource dataSource, 
                                             boolean isFirstResource) {
                    Log.d(TAG, "✓ Avatar loaded successfully from: " + avatarUrl);
                    Log.d(TAG, "Data source: " + dataSource);
                    // Ensure image is visible
                    imgAvatar.setVisibility(View.VISIBLE);
                    return false; // let Glide handle displaying
                }
            })
            .into(imgAvatar);
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

        showLoading(true);
        
        // Show local preview immediately while uploading
        runOnUiThread(() -> {
            imgAvatar.setVisibility(View.VISIBLE);
            Glide.with(this)
                .load(imageUri)
                .circleCrop()
                .into(imgAvatar);
        });
        
        Log.d(TAG, "Starting 2-step upload process for user: " + firebaseUser.getUid());
        String fileName = getFileNameFromUri(imageUri);
        Log.d(TAG, "File name: " + fileName);
        AuthApi authApi = ApiClient.get(App.authManager).create(AuthApi.class);
        UploadUrlRequest request = new UploadUrlRequest(fileName);

        authApi.requestUploadUrl(request).enqueue(new retrofit2.Callback<UploadUrlResponse>() {
            @Override
            public void onResponse(retrofit2.Call<UploadUrlResponse> call, 
                                 retrofit2.Response<UploadUrlResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UploadUrlResponse uploadData = response.body();
                    Log.d(TAG, "✓ Step 1: Received signed URL");
                    Log.d(TAG, "  Path: " + uploadData.path);
                    Log.d(TAG, "  Signed URL: " + uploadData.signedUrl);
                    uploadFileToSupabase(imageUri, uploadData.signedUrl, uploadData.path);
                } else {
                    Log.e(TAG, "Failed to get upload URL: " + response.code());
                    showLoading(false);
                    Toast.makeText(EditProfileActivity.this, 
                        "Failed to prepare upload", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<UploadUrlResponse> call, Throwable t) {
                Log.e(TAG, "Network error requesting upload URL", t);
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, 
                    "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = "avatar.jpg"; // default

        try {
            android.database.Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file name", e);
        }
        if (!fileName.contains(".")) {
            fileName = fileName + ".jpg";
        }

        return fileName;
    }

    private void uploadFileToSupabase(Uri imageUri, String signedUrl, String storagePath) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                showLoading(false);
                Toast.makeText(this, "Cannot read file", Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] fileBytes = new byte[inputStream.available()];
            inputStream.read(fileBytes);
            inputStream.close();
            String mimeType = getContentResolver().getType(imageUri);
            if (mimeType == null) {
                mimeType = "image/jpeg"; // default
            }
            Log.d(TAG, "MIME type: " + mimeType);
            RequestBody requestBody = RequestBody.create(
                MediaType.parse(mimeType), 
                fileBytes
            );
            AuthApi authApi = ApiClient.get(App.authManager).create(AuthApi.class);
            authApi.uploadToSupabase(signedUrl, requestBody)
                .enqueue(new retrofit2.Callback<ResponseBody>() {
                    @Override
                    public void onResponse(retrofit2.Call<ResponseBody> call, 
                                         retrofit2.Response<ResponseBody> response) {
                        showLoading(false);

                        if (response.isSuccessful()) {
                            Log.d(TAG, "✓ Step 2: File uploaded to Supabase successfully");
                            Log.d(TAG, "  Storage path: " + storagePath);
                            String publicUrl = SupabaseConfig.getPublicUrl(storagePath);
                            Log.d(TAG, "  Public URL: " + publicUrl);
                            newAvatarUrl = publicUrl;
                            avatarChanged = true;
                            runOnUiThread(() -> {
                                // Reload from Supabase URL to confirm upload success
                                imgAvatar.setVisibility(View.VISIBLE);
                                
                                // Force reload from network with cache busting
                                Glide.with(EditProfileActivity.this)
                                    .load(publicUrl)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .circleCrop()
                                    .into(imgAvatar);
                                
                                Toast.makeText(EditProfileActivity.this, 
                                    "Photo uploaded successfully", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            Log.e(TAG, "Failed to upload to Supabase: " + response.code());
                            Toast.makeText(EditProfileActivity.this, 
                                "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                        showLoading(false);
                        Log.e(TAG, "Network error uploading to Supabase", t);
                        Toast.makeText(EditProfileActivity.this, 
                            "Upload error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        } catch (IOException e) {
            showLoading(false);
            Log.e(TAG, "Error reading file", e);
            Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeProfilePhoto() {
        newAvatarUrl = null;
        avatarChanged = true;
        
        // Clear the image and show default background
        imgAvatar.setImageDrawable(null);
        imgAvatar.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Photo will be removed when you save", Toast.LENGTH_SHORT).show();
    }

    private void saveProfile() {
        String newName = etName.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            etName.requestFocus();
            return;
        }

        if (newName.length() > 100) {
            Toast.makeText(this, "Name is too long (max 100 characters)", Toast.LENGTH_SHORT).show();
            etName.requestFocus();
            return;
        }
        
        // Check if any field has changed
        String currentName = firebaseUser != null ? firebaseUser.getDisplayName() : "";
        String newBio = etBio.getText().toString().trim();
        String newJobTitle = etJobTitle.getText().toString().trim();
        String newPhoneNumber = etPhoneNumber.getText().toString().trim();
        
        boolean nameChanged = !newName.equals(currentName);
        boolean bioChanged = !newBio.equals(originalBio);
        boolean jobTitleChanged = !newJobTitle.equals(originalJobTitle);
        boolean phoneNumberChanged = !newPhoneNumber.equals(originalPhoneNumber);
        
        if (!nameChanged && !avatarChanged && !bioChanged && !jobTitleChanged && !phoneNumberChanged) {
            Toast.makeText(this, "No changes to save", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        updateFirebaseProfile(newName);
    }

    private void updateFirebaseProfile(String newName) {
        if (firebaseUser == null) {
            showLoading(false);
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
        builder.setDisplayName(newName);

        UserProfileChangeRequest profileUpdates = builder.build();

        firebaseUser.updateProfile(profileUpdates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "✓ Firebase profile updated (name only)");
                
                // Get bio, job title, phone number
                String bio = etBio.getText().toString().trim();
                String jobTitle = etJobTitle.getText().toString().trim();
                String phoneNumber = etPhoneNumber.getText().toString().trim();
                
                updateBackendProfile(newName, avatarChanged ? newAvatarUrl : null, 
                    bio.isEmpty() ? null : bio,
                    jobTitle.isEmpty() ? null : jobTitle,
                    phoneNumber.isEmpty() ? null : phoneNumber);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to update Firebase profile", e);
                showLoading(false);
                Toast.makeText(this, "Failed to update profile: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
            });
    }

    private void updateBackendProfile(String name, String avatarUrl, String bio, String jobTitle, String phoneNumber) {
        Log.d(TAG, "Updating backend profile:");
        Log.d(TAG, "  Name: " + name);
        Log.d(TAG, "  Avatar URL (public URL): " + avatarUrl);
        Log.d(TAG, "  Bio: " + bio);
        Log.d(TAG, "  Job Title: " + jobTitle);
        Log.d(TAG, "  Phone: " + phoneNumber);

        AuthApi authApi = ApiClient.get(App.authManager).create(AuthApi.class);
        UpdateProfileRequest request = new UpdateProfileRequest(name, avatarUrl, bio, jobTitle, phoneNumber);

        authApi.updateProfile(request).enqueue(new retrofit2.Callback<UserDto>() {
            @Override
            public void onResponse(retrofit2.Call<UserDto> call, retrofit2.Response<UserDto> response) {
                showLoading(false);

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

                    Toast.makeText(EditProfileActivity.this, 
                        "✅ Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Log.e(TAG, "Failed to update backend profile: " + response.code());
                    Toast.makeText(EditProfileActivity.this, 
                        "Failed to update profile on server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<UserDto> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error updating backend profile", t);
                Toast.makeText(EditProfileActivity.this, 
                    "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
        btnCancel.setEnabled(!show);
    }
}
