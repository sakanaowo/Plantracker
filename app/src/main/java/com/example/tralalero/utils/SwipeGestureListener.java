package com.example.tralalero.utils;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Gesture listener to detect horizontal swipe gestures
 * Usage: Attach to any View to detect swipe left/right
 */
public abstract class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
    
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1 == null || e2 == null) return false;
        
        float diffX = e2.getX() - e1.getX();
        float diffY = e2.getY() - e1.getY();
        
        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    onSwipeRight();
                } else {
                    onSwipeLeft();
                }
                return true;
            }
        }
        return false;
    }

    public abstract void onSwipeLeft();
    public abstract void onSwipeRight();
}
