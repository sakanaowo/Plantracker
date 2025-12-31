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
import com.example.tralalero.auth.remote.dto.UploadUrlRequest;
import com.example.tralalero.auth.remote.dto.UploadUrlResponse;
import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.core.DependencyProvider;
import com.example.tralalero.core.SupabaseConfig;
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
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
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
    
    // Notifications
    private com.google.android.material.switchmaterial.SwitchMaterial switchNotifications;
    private TextView tvNotificationStatus;
    private String currentDeviceId;

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
        tvGoogleCalendarStatus = view.findViewById(R.id.tvGoogleCalendarStatus);
        layoutGoogleCalendar = view.findViewById(R.id.layoutGoogleCalendar);
        
        // User card click to view account
        androidx.cardview.widget.CardView cardUserInfo = view.findViewById(R.id.cardUserInfo);
        cardUserInfo.setOnClickListener(v -> openEditProfileActivity());

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
        avatarContainer.setOnClickListener(v -> {
            showImagePickerBottomSheet();
            // Stop propagation to prevent card click
            v.setClickable(true);
        });

        LinearLayout layoutSettings = view.findViewById(R.id.layoutSettings);
        layoutSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });
        
        // Setup Google Calendar integration
        setupGoogleCalendar();
        
        // Setup notification settings
        setupNotificationSettings();
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
    
    private void setupNotificationSettings() {
        switchNotifications = getView().findViewById(R.id.switchNotifications);
        tvNotificationStatus = getView().findViewById(R.id.tvNotificationStatus);
        
        // Load current device status from backend
        checkDeviceRegistrationStatus();
        
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return; // Ignore programmatic changes
            
            if (isChecked) {
                registerDevice();
            } else {
                unregisterDevice();
            }
        });
    }
    
    private void checkDeviceRegistrationStatus() {
        com.example.tralalero.data.remote.api.FcmApiService fcmApi = 
            ApiClient.get(App.authManager).create(com.example.tralalero.data.remote.api.FcmApiService.class);
        
        fcmApi.getDevices().enqueue(new retrofit2.Callback<java.util.List<com.example.tralalero.data.remote.dto.fcm.DeviceResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.example.tralalero.data.remote.dto.fcm.DeviceResponse>> call, 
                                 retrofit2.Response<java.util.List<com.example.tralalero.data.remote.dto.fcm.DeviceResponse>> response) {
                if (!isAdded()) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    java.util.List<com.example.tralalero.data.remote.dto.fcm.DeviceResponse> devices = response.body();
                    
                    // Check if current device is registered
                    String currentToken = com.example.tralalero.util.FCMHelper.getSavedToken(requireContext());
                    boolean isRegistered = false;
                    
                    for (com.example.tralalero.data.remote.dto.fcm.DeviceResponse device : devices) {
                        if (device.getFcmToken().equals(currentToken) && device.isActive()) {
                            currentDeviceId = device.getId();
                            isRegistered = true;
                            break;
                        }
                    }
                    
                    if (switchNotifications != null) {
                        switchNotifications.setChecked(isRegistered);
                        updateNotificationStatusText(isRegistered);
                    }
                } else {
                    Log.e(TAG, "Failed to get devices: " + response.code());
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.example.tralalero.data.remote.dto.fcm.DeviceResponse>> call, Throwable t) {
                Log.e(TAG, "Network error getting devices", t);
            }
        });
    }
    
    private void registerDevice() {
        String fcmToken = com.example.tralalero.util.FCMHelper.getSavedToken(requireContext());
        
        if (fcmToken == null || fcmToken.isEmpty()) {
            Toast.makeText(requireContext(), "FCM token not available", Toast.LENGTH_SHORT).show();
            if (switchNotifications != null) {
                switchNotifications.setChecked(false);
            }
            return;
        }
        
        // Create device registration request
        String deviceModel = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
        String appVersion = com.example.tralalero.BuildConfig.VERSION_NAME;
        String locale = java.util.Locale.getDefault().toString();
        String timezone = java.util.TimeZone.getDefault().getID();
        
        com.example.tralalero.data.remote.dto.fcm.RegisterDeviceRequest request = 
            new com.example.tralalero.data.remote.dto.fcm.RegisterDeviceRequest(
                fcmToken, "ANDROID", deviceModel, appVersion, locale, timezone
            );
        
        com.example.tralalero.data.remote.api.FcmApiService fcmApi = 
            ApiClient.get(App.authManager).create(com.example.tralalero.data.remote.api.FcmApiService.class);
        
        fcmApi.registerDevice(request).enqueue(new retrofit2.Callback<com.example.tralalero.data.remote.dto.fcm.DeviceResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.tralalero.data.remote.dto.fcm.DeviceResponse> call, 
                                 retrofit2.Response<com.example.tralalero.data.remote.dto.fcm.DeviceResponse> response) {
                if (!isAdded()) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    currentDeviceId = response.body().getId();
                    Log.d(TAG, "✓ Device registered successfully: " + currentDeviceId);
                    updateNotificationStatusText(true);
                    Toast.makeText(requireContext(), "✓ Notifications enabled", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to register device: " + response.code());
                    if (switchNotifications != null) {
                        switchNotifications.setChecked(false);
                    }
                    Toast.makeText(requireContext(), "Failed to enable notifications", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.tralalero.data.remote.dto.fcm.DeviceResponse> call, Throwable t) {
                if (!isAdded()) return;
                
                Log.e(TAG, "Network error registering device", t);
                if (switchNotifications != null) {
                    switchNotifications.setChecked(false);
                }
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void unregisterDevice() {
        if (currentDeviceId == null || currentDeviceId.isEmpty()) {
            // No device ID, just update UI
            updateNotificationStatusText(false);
            Toast.makeText(requireContext(), "✓ Notifications disabled", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Call DELETE /api/users/devices/{deviceId}
        AuthApi authApi = ApiClient.get(App.authManager).create(AuthApi.class);
        authApi.unregisterDevice(currentDeviceId).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (!isAdded()) return;
                
                if (response.isSuccessful()) {
                    Log.d(TAG, "✓ Device unregistered successfully");
                    currentDeviceId = null;
                    updateNotificationStatusText(false);
                    Toast.makeText(requireContext(), "✓ Notifications disabled", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to unregister device: " + response.code());
                    // Revert switch on failure
                    if (switchNotifications != null) {
                        switchNotifications.setChecked(true);
                    }
                    Toast.makeText(requireContext(), "Failed to disable notifications", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                if (!isAdded()) return;
                
                Log.e(TAG, "Network error unregistering device", t);
                // Revert switch on failure
                if (switchNotifications != null) {
                    switchNotifications.setChecked(true);
                }
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateNotificationStatusText(boolean enabled) {
        if (tvNotificationStatus != null) {
            tvNotificationStatus.setText(enabled ? "Enabled" : "Disabled");
        }
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
        progressDialog.setMessage("Preparing upload...");
        progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.setProgress(10);
        
        Log.d(TAG, "Starting 2-step upload process for user: " + firebaseUser.getUid());
        String fileName = getFileNameFromUri(imageUri);
        Log.d(TAG, "File name: " + fileName);
        
        // Step 1: Request signed upload URL from backend
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
                    progressDialog.setProgress(30);
                    progressDialog.setMessage("Uploading...");
                    uploadFileToSupabase(imageUri, uploadData.signedUrl, uploadData.path, progressDialog);
                } else {
                    Log.e(TAG, "Failed to get upload URL: " + response.code());
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), 
                        "Failed to prepare upload", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<UploadUrlResponse> call, Throwable t) {
                Log.e(TAG, "Network error requesting upload URL", t);
                progressDialog.dismiss();
                Toast.makeText(requireContext(), 
                    "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private String getFileNameFromUri(Uri uri) {
        String fileName = "avatar.jpg"; // default

        try {
            android.database.Cursor cursor = requireContext().getContentResolver()
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
    
    private void uploadFileToSupabase(Uri imageUri, String signedUrl, String storagePath, 
                                     android.app.ProgressDialog progressDialog) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "Cannot read file", Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] fileBytes = new byte[inputStream.available()];
            inputStream.read(fileBytes);
            inputStream.close();
            
            String mimeType = requireContext().getContentResolver().getType(imageUri);
            if (mimeType == null) {
                mimeType = "image/jpeg"; // default
            }
            Log.d(TAG, "MIME type: " + mimeType);
            
            RequestBody requestBody = RequestBody.create(
                MediaType.parse(mimeType), 
                fileBytes
            );
            
            progressDialog.setProgress(50);
            
            AuthApi authApi = ApiClient.get(App.authManager).create(AuthApi.class);
            authApi.uploadToSupabase(signedUrl, requestBody)
                .enqueue(new retrofit2.Callback<ResponseBody>() {
                    @Override
                    public void onResponse(retrofit2.Call<ResponseBody> call, 
                                         retrofit2.Response<ResponseBody> response) {
                        progressDialog.setProgress(90);
                        
                        if (response.isSuccessful()) {
                            Log.d(TAG, "✓ Step 2: File uploaded to Supabase successfully");
                            Log.d(TAG, "  Storage path: " + storagePath);
                            String publicUrl = SupabaseConfig.getPublicUrl(storagePath);
                            Log.d(TAG, "  Public URL: " + publicUrl);
                            
                            progressDialog.setProgress(100);
                            progressDialog.setMessage("Processing...");
                            
                            // Update backend profile with new avatar URL
                            updateUserProfile(Uri.parse(publicUrl));
                            progressDialog.dismiss();
                            Toast.makeText(requireContext(), 
                                "Upload successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Failed to upload to Supabase: " + response.code());
                            progressDialog.dismiss();
                            Toast.makeText(requireContext(), 
                                "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "Network error uploading to Supabase", t);
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), 
                            "Upload error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        } catch (IOException e) {
            Log.e(TAG, "Error reading file", e);
            progressDialog.dismiss();
            Toast.makeText(requireContext(), "Error reading file", Toast.LENGTH_SHORT).show();
        }
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
