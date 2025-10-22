package com.example.tralalero.core;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Custom Glide module for app-specific Glide configuration.
 * This suppresses the "Failed to find GeneratedAppGlideModule" warning.
 * 
 * After creating this file, you need to rebuild the project to generate GlideApp.
 * Then you can optionally use GlideApp.with() instead of Glide.with() for additional features.
 */
@GlideModule
public final class MyAppGlideModule extends AppGlideModule {
    // Empty module - just to suppress warning
    // You can add custom configurations here if needed
}
