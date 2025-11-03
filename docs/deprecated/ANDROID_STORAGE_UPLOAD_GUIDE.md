# 📤 ANDROID STORAGE UPLOAD IMPLEMENTATION

## 🎯 Overview

EditProfileActivity sử dụng **2-step Supabase Storage upload** thay vì Firebase Storage:

### ✅ Advantages:

- 🔒 **Secure**: Backend controls signed URLs with expiration
- 🚀 **Fast**: Direct upload to Supabase CDN
- 📊 **Scalable**: No backend bandwidth usage
- 💰 **Cost-effective**: Supabase storage pricing better than Firebase

---

## 🏗️ Architecture

```
┌─────────────────┐         ┌──────────────┐         ┌──────────────┐
│   Android App   │  Step 1 │   Backend    │         │   Supabase   │
│ EditProfile     │────────>│   Server     │         │   Storage    │
│   Activity      │         │              │         │              │
└─────────────────┘         └──────────────┘         └──────────────┘
        │                          │                         │
        │  POST /storage/upload-url                         │
        │  { fileName: "avatar.jpg" }                       │
        │                          │                         │
        │                          │  Generate Signed URL    │
        │                          │────────────────────────>│
        │                          │                         │
        │                          │<────────────────────────│
        │  Response:               │                         │
        │  {                       │                         │
        │    path: "userId/...",   │                         │
        │    signedUrl: "...",     │                         │
        │    token: "..."          │                         │
        │  }                       │                         │
        │<─────────────────────────│                         │
        │                                                     │
        │  Step 2: PUT to signedUrl                          │
        │  Body: File binary (image)                         │
        │────────────────────────────────────────────────────>│
        │                                                     │
        │<────────────────────────────────────────────────────│
        │  200 OK - File uploaded                            │
        │                                                     │
        │  Step 3: PUT /users/me                             │
        │  { avatar_url: "userId/..." }                      │
        │────────>                                            │
```

---

## 📝 API Interfaces

### AuthApi.java

```java
public interface AuthApi {
    // Step 1: Request signed upload URL
    @POST("storage/upload-url")
    Call<UploadUrlResponse> requestUploadUrl(@Body UploadUrlRequest request);

    // Step 2: Upload file to Supabase
    @PUT
    Call<ResponseBody> uploadToSupabase(@Url String signedUrl, @Body RequestBody file);

    // Get view URL (optional)
    @GET("storage/view-url")
    Call<ViewUrlResponse> getViewUrl(@Query("path") String path);
}
```

### DTOs

**UploadUrlRequest.java**

```java
public class UploadUrlRequest {
    public String fileName;
}
```

**UploadUrlResponse.java**

```java
public class UploadUrlResponse {
    public String path;        // Storage path: "userId/uploads/timestamp-filename.ext"
    public String signedUrl;   // Temporary URL to upload
    public String token;       // Token embedded in signedUrl
}
```

**ViewUrlResponse.java**

```java
public class ViewUrlResponse {
    public String signedUrl;   // Temporary URL to view file (10 min expiry)
}
```

---

## 🔄 Upload Flow in EditProfileActivity

### Step 1: Request Signed Upload URL

```java
private void uploadImageToFirebase(Uri imageUri) {
    // Extract file name
    String fileName = getFileNameFromUri(imageUri);

    // Request signed URL from backend
    AuthApi authApi = ApiClient.get(App.authManager).create(AuthApi.class);
    UploadUrlRequest request = new UploadUrlRequest(fileName);

    authApi.requestUploadUrl(request).enqueue(new Callback<UploadUrlResponse>() {
        @Override
        public void onResponse(Call<UploadUrlResponse> call, Response<UploadUrlResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                UploadUrlResponse uploadData = response.body();

                // Step 2: Upload file
                uploadFileToSupabase(imageUri, uploadData.signedUrl, uploadData.path);
            }
        }
    });
}
```

### Step 2: Upload File to Supabase

```java
private void uploadFileToSupabase(Uri imageUri, String signedUrl, String storagePath) {
    try {
        // Read file
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        byte[] fileBytes = new byte[inputStream.available()];
        inputStream.read(fileBytes);
        inputStream.close();

        // Get MIME type
        String mimeType = getContentResolver().getType(imageUri);
        if (mimeType == null) mimeType = "image/jpeg";

        // Create RequestBody
        RequestBody requestBody = RequestBody.create(
            MediaType.parse(mimeType),
            fileBytes
        );

        // Upload to Supabase
        authApi.uploadToSupabase(signedUrl, requestBody)
            .enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        // Save storage path (NOT signed URL!)
                        newAvatarUrl = storagePath;
                        avatarChanged = true;
                    }
                }
            });
    } catch (IOException e) {
        Log.e(TAG, "Error reading file", e);
    }
}
```

### Step 3: Update Backend Profile

```java
private void updateBackendProfile(String name, String avatarUrl) {
    // avatarUrl is the storage path: "userId/uploads/timestamp-filename.ext"
    UpdateProfileRequest request = new UpdateProfileRequest(name, avatarUrl);

    authApi.updateProfile(request).enqueue(new Callback<UserDto>() {
        @Override
        public void onResponse(Call<UserDto> call, Response<UserDto> response) {
            if (response.isSuccessful()) {
                // Profile updated with Supabase storage path
                Log.d(TAG, "✓ Profile updated with avatar path: " + avatarUrl);
            }
        }
    });
}
```

---

## 🎨 EditProfileActivity Flow

```
User clicks Avatar
    ↓
Bottom Sheet: Camera/Gallery/Remove
    ↓
[Select Photo]
    ↓
uploadImageToFirebase(uri)
    ↓
Step 1: POST /storage/upload-url { fileName }
    ↓
Backend returns: { path, signedUrl, token }
    ↓
Step 2: PUT signedUrl with file bytes
    ↓
Supabase returns: 200 OK
    ↓
Save storagePath to newAvatarUrl
    ↓
User clicks "Save Changes"
    ↓
updateFirebaseProfile(name) - updates Firebase displayName
    ↓
updateBackendProfile(name, avatarPath) - saves to database
    ↓
Backend: PUT /users/me { name, avatar_url: "userId/..." }
    ↓
✅ Success → Return to AccountActivity
```

---

## 📊 Storage Path Format

Backend automatically formats paths:

```
Input fileName:  "My Avatar Photo.jpg"
Backend creates: "userId/uploads/1729536000000-my-avatar-photo.jpg"

Rules:
✅ Lowercase
✅ Spaces → dashes (-)
✅ Remove special chars
✅ Add timestamp prefix
✅ Preserve extension
✅ Add userId prefix
```

**Examples:**

```
"avatar.jpg"           → "d4e5f6a7.../uploads/1729536000000-avatar.jpg"
"Profile Photo.png"    → "d4e5f6a7.../uploads/1729536000000-profile-photo.png"
"Screenshot (2).jpg"   → "d4e5f6a7.../uploads/1729536000000-screenshot-2.jpg"
```

---

## 🔍 Key Differences from Firebase Storage

### ❌ OLD (Firebase Storage)

```java
StorageReference storageRef = FirebaseStorage.getInstance().getReference();
StorageReference profileImageRef = storageRef.child("profile_images/" + userId + ".jpg");

profileImageRef.putFile(imageUri)
    .addOnSuccessListener(taskSnapshot -> {
        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String downloadUrl = uri.toString(); // Full HTTPS URL
            updateBackend(downloadUrl);
        });
    });
```

**Issues:**

- ❌ Direct Firebase SDK dependency
- ❌ Download URL is permanent (hard to revoke)
- ❌ Fixed file naming (overwrites previous)
- ❌ No backend control over uploads

### ✅ NEW (Supabase via Backend)

```java
// Step 1: Request signed URL from backend
authApi.requestUploadUrl(new UploadUrlRequest(fileName))
    .enqueue(response -> {
        String signedUrl = response.signedUrl;
        String storagePath = response.path; // "userId/uploads/timestamp-name.jpg"

        // Step 2: Upload directly to Supabase
        authApi.uploadToSupabase(signedUrl, fileBytes)
            .enqueue(result -> {
                // Step 3: Save path to backend
                updateBackend(storagePath); // Path, not full URL
            });
    });
```

**Benefits:**

- ✅ Backend controls signed URL expiration
- ✅ Storage path (not full URL) saved to DB
- ✅ Unique file names with timestamps
- ✅ Backend can validate/authorize uploads
- ✅ Can get temporary view URLs anytime

---

## 🔐 Security

### Signed URLs

- ✅ Temporary (expire after ~60 seconds for upload)
- ✅ One-time use
- ✅ Token embedded in URL
- ✅ Backend validates user authentication before issuing

### View URLs

```java
// Get temporary view URL (expires after 10 minutes)
authApi.getViewUrl(storagePath).enqueue(response -> {
    String viewUrl = response.signedUrl;
    // Use this URL to display image (Glide, Picasso, etc.)
    Glide.with(context).load(viewUrl).into(imageView);
});
```

---

## 🧪 Testing Checklist

- [ ] Upload JPG image
- [ ] Upload PNG image
- [ ] Upload with special characters in filename
- [ ] Upload with Vietnamese characters
- [ ] Upload large file (>5MB)
- [ ] Upload without internet → Retry
- [ ] Remove photo
- [ ] Change avatar multiple times
- [ ] Verify old files remain in storage (timestamped)
- [ ] Verify backend receives correct path format

---

## 📝 Important Notes

### ⚠️ Storage Path vs Full URL

**WRONG ❌**

```java
// Don't save full signed URL to database!
updateProfile(name, "https://xxx.supabase.co/storage/v1/object/sign/bucket/...?token=...")
```

**CORRECT ✅**

```java
// Save relative path only
updateProfile(name, "userId/uploads/1729536000000-avatar.jpg")
```

### ⚠️ Viewing Uploaded Images

To display uploaded images:

1. **Option A: Get temporary view URL** (recommended for security)

```java
authApi.getViewUrl(avatarPath).enqueue(response -> {
    Glide.with(this).load(response.signedUrl).into(imgAvatar);
});
```

2. **Option B: Construct public URL** (if bucket is public)

```java
String publicUrl = "https://xxx.supabase.co/storage/v1/object/public/bucket/" + avatarPath;
Glide.with(this).load(publicUrl).into(imgAvatar);
```

### ⚠️ AccountActivity Legacy Code

AccountActivity still has Firebase Storage upload code for quick avatar changes:

```java
// This is LEGACY - consider migrating to EditProfileActivity flow
private void uploadImageToFirebase(Uri imageUri) {
    // Old Firebase Storage upload
    // TODO: Migrate to 2-step Supabase upload
}
```

**Recommendation:** Disable quick upload in AccountActivity and always route to EditProfileActivity for consistency.

---

## 🚀 Future Improvements

1. **Image Compression**

```java
// Before upload, compress image
Bitmap compressed = compressImage(originalBitmap, 1024, 1024, 80);
```

2. **Progress Tracking**

```java
// Show upload progress
RequestBody requestBody = new ProgressRequestBody(fileBytes, mimeType,
    (bytesWritten, totalBytes) -> {
        int progress = (int) ((bytesWritten * 100) / totalBytes);
        updateProgressBar(progress);
    });
```

3. **Caching View URLs**

```java
// Cache temporary URLs to avoid repeated requests
Map<String, CachedUrl> viewUrlCache = new HashMap<>();
// Check cache before requesting new URL
```

4. **Glide Integration**

```java
// Add Glide for image loading
implementation 'com.github.bumptech.glide:glide:4.16.0'

Glide.with(this)
    .load(viewUrl)
    .placeholder(R.drawable.ic_person)
    .circleCrop()
    .into(imgAvatar);
```

---

## 📚 Related Files

### Android

- `EditProfileActivity.java` - Main implementation
- `AuthApi.java` - API interface
- `UploadUrlRequest.java` - Step 1 request DTO
- `UploadUrlResponse.java` - Step 1 response DTO
- `ViewUrlResponse.java` - View URL response
- `UpdateProfileRequest.java` - Profile update DTO

### Backend

- `storage.controller.ts` - Upload URL endpoints
- `storage.service.ts` - Supabase integration
- `request-upload.dto.ts` - Validation
- `STORAGE_UPLOAD_POSTMAN_GUIDE.md` - Testing guide

---

**Document Version:** 1.0  
**Last Updated:** October 22, 2025  
**Implementation:** EditProfileActivity + AuthApi
