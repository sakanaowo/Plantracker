package com.example.tralalero.feature.home.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tralalero.App.App;
import com.example.tralalero.R;
import com.example.tralalero.auth.remote.AuthApi;
import com.example.tralalero.auth.remote.dto.UpdateProfileRequest;
import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.core.DependencyProvider;
import com.example.tralalero.data.remote.api.GoogleAuthApiService;
import com.example.tralalero.data.remote.dto.auth.UserDto;
import com.example.tralalero.domain.model.AuthUrlResponse;
import com.example.tralalero.domain.model.GoogleCalendarStatusResponse;
import com.example.tralalero.feature.auth.ui.login.LoginActivity;
import com.example.tralalero.feature.home.ui.EditProfileActivity;
import com.example.tralalero.feature.home.ui.SettingsActivity;
import com.example.tralalero.feature.home.ui.WorkspaceListActivity;
import com.example.tralalero.network.ApiClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {
    private static final String TAG = "AccountFragment";
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
    
    // Google Calendar
    private GoogleAuthApiService googleAuthApiService;
    private TextView tvGoogleCalendarStatus;
    private LinearLayout layoutGoogleCalendar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivityResultLaunchers();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(requireContext());
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        googleAuthApiService = ApiClient.get(App.authManager).create(GoogleAuthApiService.class);

        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvEmail = view.findViewById(R.id.tvEmail);
        tvAvatarLetter = view.findViewById(R.id.tvAvatarLetter);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        btnAccountOptions = view.findViewById(R.id.btnAccountOptions);
        tvGoogleCalendarStatus = view.findViewById(R.id.tvGoogleCalendarStatus);
        layoutGoogleCalendar = view.findViewById(R.id.layoutGoogleCalendar);

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

        FrameLayout avatarContainer = view.findViewById(R.id.avatarContainer);
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

        LinearLayout layoutSettings = view.findViewById(R.id.layoutSettings);
        layoutSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });
        
        // Workspace button
        LinearLayout layoutWorkspaces = view.findViewById(R.id.layoutWorkspaces);
        layoutWorkspaces.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), WorkspaceListActivity.class);
            startActivity(intent);
        });

        LinearLayout layoutOfflineBoards = view.findViewById(R.id.layoutOfflineBoards);
        layoutOfflineBoards.setOnClickListener(v -> {
            // TODO: Implement offline boards
        });
        
        // Setup Google Calendar integration
        setupGoogleCalendar();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Re-check status when returning to fragment (after OAuth callback)
        checkGoogleCalendarStatus();
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
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        storagePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openGallery();
                } else {
                    Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void setupGoogleCalendar() {
        checkGoogleCalendarStatus();
        layoutGoogleCalendar.setOnClickListener(v -> showGoogleCalendarDialog());
    }
    
    private void checkGoogleCalendarStatus() {
        googleAuthApiService.getIntegrationStatus().enqueue(new Callback<GoogleCalendarStatusResponse>() {
            @Override
            public void onResponse(Call<GoogleCalendarStatusResponse> call, Response<GoogleCalendarStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GoogleCalendarStatusResponse status = response.body();
                    updateGoogleCalendarUI(status);
                } else {
                    Log.e(TAG, "Failed to check Google Calendar status: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<GoogleCalendarStatusResponse> call, Throwable t) {
                Log.e(TAG, "Network error checking Google Calendar status", t);
            }
        });
    }
    
    private void updateGoogleCalendarUI(GoogleCalendarStatusResponse status) {
        if (!isAdded()) return;
        
        if (tvGoogleCalendarStatus != null) {
            if (status.isConnected()) {
                tvGoogleCalendarStatus.setText("✓ Connected: " + status.getAccountEmail());
                tvGoogleCalendarStatus.setTextColor(getResources().getColor(R.color.primary));
            } else {
                tvGoogleCalendarStatus.setText("Not connected - Tap to connect");
                tvGoogleCalendarStatus.setTextColor(getResources().getColor(R.color.on_surface_variant));
            }
        }
    }
    
    private void showGoogleCalendarDialog() {
        googleAuthApiService.getIntegrationStatus().enqueue(new Callback<GoogleCalendarStatusResponse>() {
            @Override
            public void onResponse(Call<GoogleCalendarStatusResponse> call, Response<GoogleCalendarStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GoogleCalendarStatusResponse status = response.body();
                    
                    if (status.isConnected()) {
                        showDisconnectDialog(status.getAccountEmail());
                    } else {
                        showConnectDialog();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<GoogleCalendarStatusResponse> call, Throwable t) {
                Log.e(TAG, "Failed to check status", t);
                Toast.makeText(requireContext(), "Failed to check connection status", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showConnectDialog() {
        if (!isAdded()) return;
        
        new AlertDialog.Builder(requireContext())
            .setTitle("Connect Google Calendar")
            .setMessage("Connect your Google Calendar to sync tasks and events across devices.\n\nFeatures:\n• Automatic task reminders\n• Google Meet integration\n• Cross-device sync")
            .setPositiveButton("Connect", (dialog, which) -> startGoogleOAuth())
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showDisconnectDialog(String email) {
        if (!isAdded()) return;
        
        new AlertDialog.Builder(requireContext())
            .setTitle("Disconnect Google Calendar")
            .setMessage("Connected as: " + email + "\n\nDisconnecting will:\n• Stop syncing tasks to Google Calendar\n• Remove Google Meet links from events\n• Keep existing calendar events")
            .setPositiveButton("Disconnect", (dialog, which) -> disconnectGoogleCalendar())
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Re-connect", (dialog, which) -> startGoogleOAuth())
            .show();
    }
    
    private void startGoogleOAuth() {
        Toast.makeText(requireContext(), "Connecting to Google Calendar...", Toast.LENGTH_SHORT).show();
        
        googleAuthApiService.getAuthUrl().enqueue(new Callback<AuthUrlResponse>() {
            @Override
            public void onResponse(Call<AuthUrlResponse> call, Response<AuthUrlResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String authUrl = response.body().getAuthUrl();
                    openChromeCustomTab(authUrl);
                } else {
                    Log.e(TAG, "Failed to get auth URL: " + response.code());
                    Toast.makeText(requireContext(), "Failed to start Google sign-in", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<AuthUrlResponse> call, Throwable t) {
                Log.e(TAG, "Network error getting auth URL", t);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void openChromeCustomTab(String url) {
        try {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setShowTitle(true);
            builder.setToolbarColor(getResources().getColor(R.color.primary));
            
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(requireContext(), Uri.parse(url));
        } catch (Exception e) {
            Log.e(TAG, "Failed to open Chrome Custom Tab", e);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }
    }
    
    private void disconnectGoogleCalendar() {
        Toast.makeText(requireContext(), "Disconnecting...", Toast.LENGTH_SHORT).show();
        
        googleAuthApiService.disconnect().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded()) return;
                
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "✓ Disconnected from Google Calendar", Toast.LENGTH_SHORT).show();
                    if (tvGoogleCalendarStatus != null) {
                        tvGoogleCalendarStatus.setText("Not connected - Tap to connect");
                        tvGoogleCalendarStatus.setTextColor(getResources().getColor(R.color.on_surface_variant));
                    }
                } else {
                    Log.e(TAG, "Failed to disconnect: " + response.code());
                    Toast.makeText(requireContext(), "Failed to disconnect", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Network error disconnecting", t);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showImagePickerBottomSheet() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext());
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void checkStoragePermissionAndOpen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
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
            currentPhotoUri = FileProvider.getUriForFile(requireContext(),
                requireContext().getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(currentPhotoUri);
        } catch (IOException e) {
            Log.e(TAG, "Error creating image file", e);
            Toast.makeText(requireContext(), "Error opening camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        String imageFileName = "profile_" + System.currentTimeMillis();
        File storageDir = requireContext().getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (firebaseUser == null) {
            Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create progress dialog
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(requireContext());
        progressDialog.setTitle("Uploading Profile Picture");
        progressDialog.setMessage("Please wait...");
        progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        Log.d(TAG, "Starting upload for user: " + firebaseUser.getUid());
        
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/" + firebaseUser.getUid() + ".jpg");
        Log.d(TAG, "Upload path: profile_images/" + firebaseUser.getUid() + ".jpg");
        
        profileImageRef.putFile(imageUri)
            .addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressDialog.setProgress((int) progress);
                Log.d(TAG, "Upload progress: " + (int) progress + "%");
            })
            .addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Upload successful, getting download URL...");
                progressDialog.setMessage("Processing...");
                profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d(TAG, "Download URL obtained: " + uri.toString());
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Upload successful!", Toast.LENGTH_SHORT).show();
                    updateUserProfile(uri);
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get download URL", e);
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to upload image. Error: " + e.getMessage(), e);
                progressDialog.dismiss();
                String errorMsg = "Upload failed: " + e.getMessage();
                if (e.getMessage() != null && e.getMessage().contains("permission")) {
                    errorMsg = "Permission denied. Please check Firebase Storage rules.";
                }
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
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
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
            });
    }

    private void syncProfileImageWithBackend(String avatarUrl) {
        updateBackendProfile(null, avatarUrl);
        
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
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
                Toast.makeText(requireContext(), "Profile photo removed", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to remove profile photo", e);
                Toast.makeText(requireContext(), "Failed to remove photo", Toast.LENGTH_SHORT).show();
            });
    }

    private void showAccountOptionsMenu(View anchor) {
        Log.d(TAG, "showAccountOptionsMenu called");
        try {
            PopupMenu popup = new PopupMenu(requireContext(), anchor);
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
            Toast.makeText(requireContext(), "Menu error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showLogoutDialog() {
        if (!isAdded()) return;
        
        new AlertDialog.Builder(requireContext())
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
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        editProfileLauncher.launch(intent);
    }

    private void refreshProfileUI() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null && getView() != null) {
            TextView tvName = getView().findViewById(R.id.tvName);
            TextView tvEmail = getView().findViewById(R.id.tvEmail);

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

            Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfileFromBackend() {
        AuthApi authApi = ApiClient.get(App.authManager).create(AuthApi.class);

        authApi.getMe().enqueue(new retrofit2.Callback<UserDto>() {
            @Override
            public void onResponse(retrofit2.Call<UserDto> call, retrofit2.Response<UserDto> response) {
                if (!isAdded() || getView() == null) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    UserDto user = response.body();
                    Log.d(TAG, "Loaded user profile from backend");
                    
                    if (user.name != null && !user.name.isEmpty()) {
                        TextView tvName = getView().findViewById(R.id.tvName);
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
        if (!isAdded() || getActivity() == null) {
            Log.w(TAG, "Fragment not added, skipping Glide load");
            return;
        }
        
        Log.d(TAG, "loadAvatarImage called with URL: " + avatarUrl);
        tvAvatarLetter.setVisibility(View.VISIBLE);
        imgAvatar.setVisibility(View.GONE);

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
                    if (imgAvatar != null) {
                        imgAvatar.setVisibility(View.GONE);
                    }
                    if (tvAvatarLetter != null) {
                        tvAvatarLetter.setVisibility(View.VISIBLE);
                    }
                    return true;
                }

                @Override
                public boolean onResourceReady(android.graphics.drawable.Drawable resource, 
                                             Object model, 
                                             com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, 
                                             com.bumptech.glide.load.DataSource dataSource, 
                                             boolean isFirstResource) {
                    Log.d(TAG, "✓ Avatar loaded successfully from: " + avatarUrl);
                    Log.d(TAG, "Data source: " + dataSource);
                    if (imgAvatar != null) {
                        imgAvatar.setVisibility(View.VISIBLE);
                    }
                    if (tvAvatarLetter != null) {
                        tvAvatarLetter.setVisibility(View.GONE);
                    }
                    return false;
                }
            })
            .into(imgAvatar);
    }

    private void updateBackendProfile(String name, String avatarUrl) {
        AuthApi authApi = ApiClient.get(App.authManager).create(AuthApi.class);
        UpdateProfileRequest request = new UpdateProfileRequest(name, avatarUrl);

        authApi.updateProfile(request).enqueue(new retrofit2.Callback<UserDto>() {
            @Override
            public void onResponse(retrofit2.Call<UserDto> call, retrofit2.Response<UserDto> response) {
                if (!isAdded()) return;
                
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
                    
                    requireActivity().runOnUiThread(() -> {
                        if (getView() == null) return;
                        
                        TextView tvName = getView().findViewById(R.id.tvName);
                        TextView tvAvatarLetter = getView().findViewById(R.id.tvAvatarLetter);

                        if (name != null) {
                            tvName.setText(name);
                            tvAvatarLetter.setText(String.valueOf(name.charAt(0)).toUpperCase());
                        }

                        Toast.makeText(requireContext(), 
                            "✅ Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Log.e(TAG, "Failed to update backend profile: " + response.code());
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(requireContext(), 
                            "Failed to update profile on server", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onFailure(retrofit2.Call<UserDto> call, Throwable t) {
                if (!isAdded()) return;
                
                Log.e(TAG, "Network error updating backend profile", t);
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(requireContext(), 
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void performLogout() {
        Log.d(TAG, "=== PERFORMING LOGOUT ===");
        
        try {
            String clientId = getString(R.string.client_id);
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(clientId)
                    .requestEmail()
                    .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
            
            Log.d(TAG, "Signing out from Google...");
            googleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "✅ Google sign-out SUCCESSFUL");
                } else {
                    Log.e(TAG, "❌ Google sign-out FAILED", task.getException());
                }
                continueLogout();
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception during Google sign-out", e);
            continueLogout();
        }
    }
    
    private void continueLogout() {
        Log.d(TAG, "Continuing logout: Firebase and cache cleanup");
        
        // Get user ID before signing out
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;
        
        FirebaseAuth.getInstance().signOut();
        App.authManager.signOut();
        Log.d(TAG, "✓ Auth cleared");
        
        // Clear calendar sync cache for this user using new CalendarSyncManager
        if (userId != null) {
            com.example.tralalero.feature.calendar.CalendarSyncManager
                .getInstance(requireContext())
                .clearUserCache(userId);
            Log.d(TAG, "✓ Calendar sync cache cleared for user: " + userId);
        }
        
        App.dependencyProvider.clearAllCaches();
        Log.d(TAG, "✓ Database cache cleared");
        DependencyProvider.reset();
        Log.d(TAG, "✓ DependencyProvider reset");
        tokenManager.clearAuthData();
        Log.d(TAG, "✓ Token cleared");
        
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
        Log.d(TAG, "✓ Logout complete, navigating to Login");
    }
}
