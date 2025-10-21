package com.example.tralalero.util;

import android.util.Log;

public class PerformanceLogger {
    private static final String TAG = "Performance";
    private long startTime;
    private String operation;
    
    public PerformanceLogger(String operation) {
        this.operation = operation;
        this.startTime = System.currentTimeMillis();
        Log.d(TAG, "▶ " + operation + " - START");
    }
    
    public void end() {
        long duration = System.currentTimeMillis() - startTime;
        Log.d(TAG, "■ " + operation + " - END: " + duration + "ms");
        
        if (duration < 100) {
            Log.d(TAG, "  ✓ EXCELLENT performance (< 100ms)");
        } else if (duration < 500) {
            Log.d(TAG, "  ✓ GOOD performance (< 500ms)");
        } else if (duration < 2000) {
            Log.w(TAG, "  ⚠ ACCEPTABLE performance (< 2s)");
        } else {
            Log.e(TAG, "  ✗ SLOW performance (> 2s)");
        }
    }
    
    public void endWithCount(int count) {
        long duration = System.currentTimeMillis() - startTime;
        Log.d(TAG, "■ " + operation + " - END: " + duration + "ms (" + count + " items)");
        
        if (duration < 100) {
            Log.d(TAG, "  ✓ EXCELLENT");
        } else if (duration < 500) {
            Log.d(TAG, "  ✓ GOOD");
        } else {
            Log.w(TAG, "  ⚠ SLOW");
        }
    }
}
