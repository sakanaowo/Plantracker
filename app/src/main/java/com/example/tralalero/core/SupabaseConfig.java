package com.example.tralalero.core;

public class SupabaseConfig {

    public static final String SUPABASE_URL = "https://acswtonhplvmdryauyhp.supabase.co";

    public static final String STORAGE_BUCKET = "images";

    public static final String STORAGE_PUBLIC_URL = SUPABASE_URL + "/storage/v1/object/public/" + STORAGE_BUCKET;

    public static String getPublicUrl(String storagePath) {
        if (storagePath == null || storagePath.isEmpty()) {
            return null;
        }
        
        // Remove leading slash if present
        if (storagePath.startsWith("/")) {
            storagePath = storagePath.substring(1);
        }
        
        return STORAGE_PUBLIC_URL + "/" + storagePath;
    }
}
