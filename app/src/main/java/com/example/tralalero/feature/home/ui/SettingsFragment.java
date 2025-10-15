package com.example.tralalero.feature.home.ui;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import com.example.tralalero.App.App;
import com.example.tralalero.R;
import com.example.tralalero.auth.remote.AuthApi;
import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.feature.auth.ui.login.LoginActivity;
import com.example.tralalero.network.ApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = "SettingsFragment";
    private TokenManager tokenManager;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        tokenManager = new TokenManager(requireContext());
        androidx.preference.ListPreference languagePref = findPreference("language");
        if (languagePref != null) {
            languagePref.setOnPreferenceChangeListener((preference, newValue) -> {
                String languageCode = (String) newValue;
                changeLanguage(languageCode);
                return true;
            });
        }
        SwitchPreferenceCompat themePref = findPreference("theme");
        if (themePref != null) {
            themePref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean isDark = (boolean) newValue;
                if (isDark) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                getActivity().recreate();
                return true;
            });
        }
        Preference deleteAccountPref = findPreference("delete_account");
        if (deleteAccountPref != null) {
            deleteAccountPref.setOnPreferenceClickListener(preference -> {
                showDeleteAccountDialog();
                return true;
            });
        }
    }
    private void changeLanguage(String languageCode) {
        android.content.res.Configuration configuration = new android.content.res.Configuration();
        java.util.Locale locale = new java.util.Locale(languageCode);
        java.util.Locale.setDefault(locale);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        requireContext().getResources().updateConfiguration(configuration, 
                requireContext().getResources().getDisplayMetrics());
        requireActivity().recreate();
    }
    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void deleteAccount() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(requireContext(), "Deleting account...", Toast.LENGTH_SHORT).show();
        AuthApi authApi = ApiClient.get(App.authManager)
                .create(AuthApi.class);
        authApi.deleteAccount().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    deleteFirebaseUser(firebaseUser);
                } else {
                    Log.e(TAG, "Backend delete failed: " + response.code());
                    Toast.makeText(requireContext(), "Failed to delete account from server", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error deleting account", t);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deleteFirebaseUser(FirebaseUser firebaseUser) {
        firebaseUser.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firebase user deleted successfully");
                    clearDataAndLogout();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete Firebase user", e);
                    Toast.makeText(requireContext(), "Failed to delete Firebase account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void clearDataAndLogout() {
        tokenManager.clearAll();
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
