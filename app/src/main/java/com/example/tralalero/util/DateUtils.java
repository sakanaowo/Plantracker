package com.example.tralalero.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility class for date and time operations
 */
public class DateUtils {
    
    private static final String TAG = "DateUtils";
    
    // ISO 8601 formats
    private static final String ISO_8601_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String ISO_8601_NO_MILLIS = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    /**
     * Format Calendar to ISO 8601 string
     * @param calendar Calendar instance
     * @return ISO 8601 formatted string
     */
    public static String formatToISO8601(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_WITH_MILLIS, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(calendar.getTime());
    }
    
    /**
     * Format Date to ISO 8601 string
     * @param date Date instance
     * @return ISO 8601 formatted string
     */
    public static String formatToISO8601(Date date) {
        if (date == null) {
            return null;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_WITH_MILLIS, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }
    
    /**
     * Parse ISO 8601 string to Date
     * Supports both formats with and without milliseconds
     * @param iso8601String ISO 8601 formatted string
     * @return Date instance or null if parsing fails
     */
    public static Date parseISO8601(String iso8601String) {
        if (iso8601String == null || iso8601String.isEmpty()) {
            return null;
        }
        
        // Try with milliseconds first
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_WITH_MILLIS, Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.parse(iso8601String);
        } catch (ParseException e) {
            Log.d(TAG, "Failed to parse with milliseconds, trying without", e);
        }
        
        // Try without milliseconds
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_NO_MILLIS, Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.parse(iso8601String);
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse ISO 8601 date: " + iso8601String, e);
            return null;
        }
    }
    
    /**
     * Format date to display format (e.g., "Nov 9, 2025")
     * @param date Date instance
     * @return Formatted date string
     */
    public static String formatDisplayDate(Date date) {
        if (date == null) {
            return "N/A";
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        return sdf.format(date);
    }
    
    /**
     * Format time to display format (e.g., "2:00 PM")
     * @param date Date instance
     * @return Formatted time string
     */
    public static String formatDisplayTime(Date date) {
        if (date == null) {
            return "N/A";
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(date);
    }
    
    /**
     * Format date range to display format
     * @param startDate Start date
     * @param endDate End date
     * @return Formatted range string (e.g., "2:00 PM - 3:00 PM")
     */
    public static String formatDisplayTimeRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return "N/A";
        }
        
        return formatDisplayTime(startDate) + " - " + formatDisplayTime(endDate);
    }
    
    /**
     * Validate date range
     * @param startDate Start date
     * @param endDate End date
     * @return true if valid (start before end)
     */
    public static boolean isValidDateRange(Calendar startDate, Calendar endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }
        
        return startDate.before(endDate);
    }
    
    /**
     * Check if date range exceeds maximum days
     * @param startDate Start date
     * @param endDate End date
     * @param maxDays Maximum allowed days
     * @return true if range is within limit
     */
    public static boolean isWithinMaxDays(Calendar startDate, Calendar endDate, int maxDays) {
        if (startDate == null || endDate == null) {
            return false;
        }
        
        long diffMillis = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        long diffDays = diffMillis / (24 * 60 * 60 * 1000);
        
        return diffDays <= maxDays;
    }
    
    /**
     * Add days to calendar
     * @param calendar Calendar instance
     * @param days Number of days to add
     * @return New Calendar with days added
     */
    public static Calendar addDays(Calendar calendar, int days) {
        Calendar newCalendar = (Calendar) calendar.clone();
        newCalendar.add(Calendar.DAY_OF_MONTH, days);
        return newCalendar;
    }
}
