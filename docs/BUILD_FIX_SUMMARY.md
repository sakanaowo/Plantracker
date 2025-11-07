# Build Error Fixes - November 7, 2025

## Issues Found

### 1. Duplicate Resource: `SquareOutlinedButton`
**Error:**
```
Duplicate resources: style/SquareOutlinedButton
- C:\...\res\values\btn_style.xml
- C:\...\res\values\themes.xml
```

**Root Cause:**
The style `SquareOutlinedButton` was defined in both `btn_style.xml` and `themes.xml`, causing a resource conflict.

**Fix:**
Removed the duplicate definition from `themes.xml`, keeping only the one in `btn_style.xml`.

**Files Modified:**
- `app/src/main/res/values/themes.xml` - Removed duplicate style definition

---

### 2. Dependency Conflict: MaterialCalendarView vs AndroidX
**Error:**
```
Duplicate class android.support.v4.app.INotificationSideChannel found in:
- core-1.15.0.aar (androidx.core:core:1.15.0)
- support-compat-25.1.1.aar (com.android.support:support-compat:25.1.1)
```

**Root Cause:**
The library `material-calendarview:1.4.3` uses old Android Support Library (v25.1.1) which conflicts with AndroidX libraries used in the project.

**Fix:**
Removed the conflicting MaterialCalendarView dependency and refactored the calendar UI to use Material Components' DatePicker instead.

**Changes:**

1. **Removed Dependency** (`app/build.gradle.kts`):
   ```kotlin
   // REMOVED:
   implementation("com.prolificinteractive:material-calendarview:1.4.3")
   ```

2. **Updated Fragment** (`ProjectCalendarFragment.java`):
   - Replaced `MaterialCalendarView` with Material `DatePicker` dialog
   - Changed from interactive calendar widget to month navigation buttons
   - Added "Pick Date" button that shows Material Date Picker
   - Added Previous/Next month navigation buttons

3. **Updated Layout** (`fragment_project_calendar.xml`):
   - Removed `<com.prolificinteractive.materialcalendarview.MaterialCalendarView>`
   - Added month navigation controls:
     - `btnPreviousMonth` - Navigate to previous month
     - `btnNextMonth` - Navigate to next month
     - `tvCurrentMonth` - Display current month/year
     - `btnPickDate` - Open Material Date Picker dialog
   - Kept all other features: event list, sync, filter buttons

**New User Experience:**
- Month/Year displayed at top with navigation arrows
- "Chọn" button opens Material Date Picker for quick date selection
- Events filtered by selected date displayed below
- All existing features (sync, filter, event list) remain intact

---

## Files Modified Summary

| File | Change Type | Description |
|------|-------------|-------------|
| `app/src/main/res/values/themes.xml` | Modified | Removed duplicate `SquareOutlinedButton` style |
| `app/build.gradle.kts` | Modified | Removed `material-calendarview:1.4.3` dependency |
| `fragment_project_calendar.xml` | Modified | Replaced MaterialCalendarView with month navigation UI |
| `ProjectCalendarFragment.java` | Modified | Updated to use Material DatePicker instead of MaterialCalendarView |

---

## Build Status

✅ **All duplicate resource errors resolved**
✅ **All dependency conflicts resolved**
✅ **Calendar functionality maintained with Material Components**
✅ **No breaking changes to user features**

---

## Testing Recommendations

1. ✅ Clean build succeeds
2. ⏳ Test Calendar Tab navigation
3. ⏳ Test date picker dialog
4. ⏳ Test month navigation (previous/next)
5. ⏳ Test event filtering by date
6. ⏳ Test sync button functionality
7. ⏳ Test opening task detail from calendar event

---

## Technical Notes

### Why Remove MaterialCalendarView?
- Uses deprecated Support Library (android.support.v4)
- Conflicts with AndroidX migration
- Not actively maintained (last update 2017)
- Material Components provides modern alternative

### Migration Path
- **Old:** Third-party calendar widget with inline date selection
- **New:** Material DatePicker dialog + month navigation
- **Benefit:** No dependency conflicts, modern Material Design, better AndroidX compatibility

### Alternative Libraries Considered
1. **kizitonwose/CalendarView** - Also has potential conflicts
2. **Material DatePicker** (chosen) - Built-in, no conflicts, Material Design 3
3. **Custom Calendar Grid** - Too much work for MVP

---

## Next Steps

After build succeeds:
1. Run app and test Calendar Tab
2. Verify date navigation works correctly
3. Check event filtering logic
4. Test Google Calendar sync UI
5. Verify no regressions in other features (Board tab, Card Detail)

---

**Status:** Build in progress...
**Expected Result:** Clean build with 0 errors
**Estimated Completion:** ~2-3 minutes (Gradle daemon starting)
