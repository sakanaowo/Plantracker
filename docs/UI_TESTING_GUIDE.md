# 📱 UI Testing Guide - Calendar Integration Features

**Version:** 1.0  
**Date:** November 9, 2025  
**Target:** Frontend QA & Testing  
**Platform:** Android (Plantracker App)

---

## 📋 Table of Contents

1. [Test Environment Setup](#test-environment-setup)
2. [Feature 1: Meeting Scheduler](#feature-1-meeting-scheduler)
3. [Feature 2: Quick Event Creation](#feature-2-quick-event-creation)
4. [Feature 3: Project Summary](#feature-3-project-summary)
5. [Integration Testing](#integration-testing)
6. [Performance Testing](#performance-testing)
7. [Bug Reporting Template](#bug-reporting-template)

---

## 🔧 Test Environment Setup

### Prerequisites

✅ **Before Testing:**

- [ ] App installed on Android device/emulator (Android 7.0+, API 24+)
- [ ] Test account logged in
- [ ] Google account connected (for Calendar integration)
- [ ] At least 1 project created
- [ ] At least 2-3 team members in the project
- [ ] Stable internet connection
- [ ] Backend server running

### Test Data Preparation

**Create Test Project:**

```
Project Name: QA Test Project
Members:
  - User A (Tester 1)
  - User B (Tester 2)
  - User C (Tester 3)
```

**Create Sample Tasks:**

```
- 3 tasks in "To Do" status
- 2 tasks in "In Progress" status
- 1 task in "In Review" status
- 4 tasks in "Done" status
Total: 10 tasks
```

---

## 🎯 Feature 1: Meeting Scheduler

### Test Case 1.1: Open Member Selection

**Steps:**

1. Open app → Navigate to **Calendar** tab
2. Tap **"Schedule Meeting"** button (or FAB with calendar icon)
3. Member selection bottom sheet appears from bottom

**Expected Results:**

- ✅ Bottom sheet slides up smoothly (Material motion)
- ✅ Shows all project members with avatars
- ✅ Search box visible at top
- ✅ "0 members selected" text visible
- ✅ "Next" button is **DISABLED** (grayed out)

**Screenshot Location:** `screenshots/meeting_scheduler_01_member_selection_initial.png`

---

### Test Case 1.2: Search Members

**Steps:**

1. In member selection bottom sheet
2. Tap search box
3. Type "test" (or part of member name)
4. Observe filtered results

**Expected Results:**

- ✅ Keyboard appears
- ✅ List filters in real-time (instant response)
- ✅ Only matching members shown (by name OR email)
- ✅ Clear button (X) appears in search box
- ✅ Tap X → all members shown again

**Edge Cases:**

- Search with no matches → Shows "No members found" message
- Search with special characters → No crash

---

### Test Case 1.3: Select Multiple Members

**Steps:**

1. Tap checkbox next to **User A** → Check appears
2. Tap checkbox next to **User B** → Check appears
3. Observe selected count text

**Expected Results:**

- ✅ Checkboxes toggle on/off smoothly
- ✅ Selected count updates: "2 members selected"
- ✅ "Next" button becomes **ENABLED** (blue/primary color)
- ✅ Tap same checkbox again → Deselects (toggle works)

**Business Rule:** Minimum 1 member required to enable "Next"

---

### Test Case 1.4: Open Time Slot Selection Dialog

**Steps:**

1. With 2+ members selected
2. Tap **"Next"** button
3. Bottom sheet dismisses
4. Time Slot Selection Dialog appears

**Expected Results:**

- ✅ Bottom sheet slides down smoothly
- ✅ Dialog appears (full-screen or large modal)
- ✅ Dialog title: "Find Meeting Times"
- ✅ Duration input shows default: **60 minutes**
- ✅ Start date shows: **Today's date**
- ✅ End date shows: **7 days from today**
- ✅ "Find Times" button visible
- ✅ RecyclerView area empty (shows "Tap 'Find Times' to search")

**Screenshot Location:** `screenshots/meeting_scheduler_02_time_slot_dialog_initial.png`

---

### Test Case 1.5: Change Meeting Duration

**Steps:**

1. In Time Slot Selection Dialog
2. Tap **Duration** input field
3. Change value to **30** minutes
4. Tap outside or press Enter

**Expected Results:**

- ✅ Duration updates to "30 minutes"
- ✅ Input accepts only numbers
- ✅ Minimum value: 15 minutes
- ✅ Maximum value: 480 minutes (8 hours)

**Test Values:**

- ✅ 30 min → Valid
- ✅ 60 min → Valid
- ✅ 120 min → Valid
- ❌ 10 min → Shows error "Minimum 15 minutes"
- ❌ 500 min → Shows error "Maximum 480 minutes"

---

### Test Case 1.6: Select Date Range

**Steps:**

1. Tap **Start Date** field
2. Material DatePicker appears
3. Select **Today**
4. Tap OK
5. Tap **End Date** field
6. Select **7 days from today**
7. Tap OK

**Expected Results:**

- ✅ DatePicker shows calendar grid
- ✅ Selected date highlighted
- ✅ Start date updates in field (format: "Nov 9, 2025")
- ✅ End date updates in field
- ✅ End date must be >= Start date (validation)

**Edge Cases:**

- End date < Start date → Shows error "End date must be after start date"

---

### Test Case 1.7: Find Available Time Slots

**Steps:**

1. Duration: **60 minutes**
2. Date range: **Nov 9 - Nov 16**
3. Tap **"Find Times"** button
4. Wait for response

**Expected Results:**

- ✅ Loading indicator appears (spinning progress bar)
- ✅ "Find Times" button shows "Loading..." or spinner inside
- ✅ After 1-3 seconds → Time slots appear in RecyclerView
- ✅ Shows **top 5 suggested times** (sorted by availability score)
- ✅ Each slot shows:
  - 📅 Date (e.g., "Saturday, Nov 9")
  - ⏰ Time range (e.g., "2:00 PM - 3:00 PM")
  - 👥 Availability (e.g., "3 available")
  - 🏷️ Score chip (color-coded)

**Screenshot Location:** `screenshots/meeting_scheduler_03_time_slots_results.png`

---

### Test Case 1.8: Verify Color-Coded Score Chips

**Visual Verification:**

Check each time slot's score chip color:

| Score Range | Color                 | Chip Text             | Meaning                             |
| ----------- | --------------------- | --------------------- | ----------------------------------- |
| 80% - 100%  | 🟢 Green (`#4CAF50`)  | "Excellent" or "100%" | All or almost all members available |
| 60% - 79%   | 🟠 Orange (`#FF9800`) | "Good" or "75%"       | Most members available              |
| 0% - 59%    | 🔴 Red (`#F44336`)    | "Fair" or "50%"       | Some conflicts                      |

**Expected Results:**

- ✅ Colors match availability percentage
- ✅ Chip text readable (white text on colored background)
- ✅ Highest score slots appear first (top of list)

---

### Test Case 1.9: Select Time Slot

**Steps:**

1. Scroll through time slot list
2. Tap on a time slot card (e.g., "Saturday 2:00 PM")
3. Observe card selection state

**Expected Results:**

- ✅ Card highlights with primary color border
- ✅ Previous selection de-highlights (single selection)
- ✅ Ripple effect on tap (Material design)
- ✅ "Create Meeting" button appears at bottom OR confirmation dialog opens

---

### Test Case 1.10: Create Meeting

**Steps:**

1. Time slot selected: **Nov 9, 2:00 PM**
2. Dialog/prompt appears: "Create Meeting?"
3. Enter meeting title: **"Sprint Planning"**
4. Tap **"Create"** button
5. Wait for response

**Expected Results:**

- ✅ Loading indicator during API call
- ✅ Success message: "✓ Meeting created!"
- ✅ Shows Google Meet link (clickable)
- ✅ Dialog dismisses automatically
- ✅ Calendar tab refreshes (meeting appears in calendar view)
- ✅ Push notification sent to all attendees

**Screenshot Location:** `screenshots/meeting_scheduler_04_success.png`

---

### Test Case 1.11: Empty State Handling

**Steps:**

1. Select date range in past: **Nov 1 - Nov 3**
2. Tap "Find Times"
3. Backend returns empty array

**Expected Results:**

- ✅ Shows message: "No available times found"
- ✅ Suggestion text: "Try expanding your date range"
- ✅ No crash, no blank screen

---

### Test Case 1.12: Error Handling

**Steps:**

1. Turn OFF internet/WiFi
2. Tap "Find Times"
3. Network request fails

**Expected Results:**

- ✅ Error Snackbar appears: "❌ Network error. Please check your connection."
- ✅ "Retry" button in Snackbar
- ✅ Tap Retry → Tries API call again
- ✅ No crash

**Other Error Cases:**

- 401 Unauthorized → "Please log in again"
- 500 Server Error → "Server error. Try again later."

---

## 🎯 Feature 2: Quick Event Creation

### Test Case 2.1: Open Quick Event Dialog

**Steps:**

1. Navigate to **Calendar** tab
2. Tap **FAB** (Floating Action Button) with **+** icon
3. Quick Event Dialog appears

**Expected Results:**

- ✅ Dialog slides up from bottom (Material motion)
- ✅ Dialog title: "Create Quick Event"
- ✅ Title input field focused (keyboard appears)
- ✅ Date field shows: **Today's date**
- ✅ Time field shows: **Current time + 1 hour**
- ✅ Duration dropdown shows: **60 minutes** (default)
- ✅ Event type chips visible: Meeting | Milestone | Other
- ✅ "Meeting" chip selected by default
- ✅ Google Meet switch: **OFF** by default
- ✅ Description field: Empty (optional)
- ✅ "Create" button visible

**Screenshot Location:** `screenshots/quick_event_01_initial.png`

---

### Test Case 2.2: Fill Event Title

**Steps:**

1. In Quick Event Dialog
2. Tap **Title** field (already focused)
3. Type: **"Client Demo"**

**Expected Results:**

- ✅ Text appears as typed
- ✅ No character limit warning (or shows "50/100 characters" if limited)
- ✅ Required field indicator (red asterisk)

**Validation:**

- Leave blank → Shows error "Title is required"

---

### Test Case 2.3: Select Event Date

**Steps:**

1. Tap **Date** field (shows "Nov 9, 2025")
2. Material DatePicker opens
3. Select **Nov 15, 2025**
4. Tap **OK**

**Expected Results:**

- ✅ DatePicker calendar grid appears
- ✅ Today highlighted in different color
- ✅ Selected date: **Nov 15** highlighted
- ✅ Date field updates: "Nov 15, 2025"
- ✅ Can select future dates only (past dates disabled)

---

### Test Case 2.4: Select Event Time

**Steps:**

1. Tap **Time** field (shows "2:00 PM")
2. Material TimePicker opens (clock face or input mode)
3. Select **3:30 PM**
4. Tap **OK**

**Expected Results:**

- ✅ TimePicker appears (clock UI or number input)
- ✅ Shows 12-hour format (AM/PM) or 24-hour based on device settings
- ✅ Time field updates: "3:30 PM"
- ✅ Picker validates time (e.g., can't select invalid values)

---

### Test Case 2.5: Select Duration

**Steps:**

1. Tap **Duration** dropdown (shows "60 minutes")
2. Dropdown menu appears
3. Select **30 minutes**

**Expected Results:**

- ✅ Dropdown shows options:
  - 15 minutes
  - 30 minutes
  - 60 minutes
  - 120 minutes
  - Custom... (shows number input)
- ✅ Selected value updates: "30 minutes"

---

### Test Case 2.6: Select Event Type

**Steps:**

1. Observe 3 chips: **Meeting** | Milestone | Other
2. **Meeting** selected by default (filled color)
3. Tap **Milestone** chip

**Expected Results:**

- ✅ Milestone chip becomes selected (filled)
- ✅ Meeting chip de-selects (outline only)
- ✅ Only one chip selected at a time (single select)

**Chip Colors:**

- Meeting: Blue
- Milestone: Orange
- Other: Gray

---

### Test Case 2.7: Toggle Google Meet

**Steps:**

1. Observe **Google Meet** switch (OFF by default)
2. Tap switch to turn **ON**
3. Switch animates to ON state

**Expected Results:**

- ✅ Switch slides to ON (blue/primary color)
- ✅ Label shows: "Include Google Meet link"
- ✅ Tap again → Switch turns OFF

---

### Test Case 2.8: Add Description (Optional)

**Steps:**

1. Tap **Description** field (multiline input)
2. Type: **"Demo the new dashboard features"**

**Expected Results:**

- ✅ Text area expands (multiline)
- ✅ Placeholder disappears
- ✅ Optional field (can be left blank)

---

### Test Case 2.9: Create Event Successfully

**Steps:**

1. Title: **"Client Demo"**
2. Date: **Nov 15, 2025**
3. Time: **3:30 PM**
4. Duration: **30 minutes**
5. Type: **Meeting**
6. Google Meet: **ON**
7. Description: **"Demo dashboard"**
8. Tap **"Create"** button
9. Wait for API response

**Expected Results:**

- ✅ Loading indicator (button shows spinner)
- ✅ Success message: "✓ Event created!"
- ✅ Toast notification shows event name
- ✅ If Google Meet ON → Shows Meet link (clickable)
- ✅ Dialog dismisses
- ✅ Calendar refreshes (event appears)
- ✅ Backend syncs to Google Calendar
- ✅ Notification sent to attendees (if any)

**Screenshot Location:** `screenshots/quick_event_02_success.png`

---

### Test Case 2.10: Validation Errors

**Test Missing Title:**

1. Leave title blank
2. Fill other fields
3. Tap "Create"

**Expected:**

- ❌ Error message: "Title is required"
- ❌ Title field highlighted in red
- ❌ Event NOT created

**Test Invalid Time:**

1. Select date: **Today**
2. Select time: **1:00 PM** (in the past)
3. Tap "Create"

**Expected:**

- ⚠️ Warning: "Event time is in the past. Continue?"
- OR auto-adjust to current time + 1 hour

---

### Test Case 2.11: Cancel Dialog

**Steps:**

1. Open Quick Event Dialog
2. Fill some fields
3. Tap **Cancel** or **Back** button

**Expected Results:**

- ✅ Confirmation prompt: "Discard changes?"
- ✅ Tap "Discard" → Dialog closes, no event created
- ✅ Tap "Cancel" → Stays in dialog, keeps data

---

## 🎯 Feature 3: Project Summary

### Test Case 3.1: Navigate to Summary Tab

**Steps:**

1. Open app
2. Select a project
3. Navigate to **Summary** tab (bottom nav or tabs)
4. Observe summary screen

**Expected Results:**

- ✅ Screen loads within 1-2 seconds
- ✅ Shows 4 stat cards (2x2 grid):
  - ✅ **Done** card (gray icon)
  - ✅ **Updated** card (gray icon)
  - ✅ **Created** card (gray icon)
  - ✅ **Due** card (gray icon)
- ✅ Shows **Status Overview** card below
- ✅ Donut chart visible in center
- ✅ Status list below chart (To Do, In Progress, Done)

**Screenshot Location:** `screenshots/project_summary_01_initial.png`

---

### Test Case 3.2: Verify Stat Cards

**Visual Check:**

Each card should show:

**Done Card:**

```
[Gray circle icon with checkmark]
5 done
in the last 7 days
```

**Updated Card:**

```
[Gray circle icon with edit]
8 updated
in the last 7 days
```

**Created Card:**

```
[Gray circle icon with plus]
3 created
in the last 7 days
```

**Due Card:**

```
[Gray circle icon with calendar]
2 due
in the next 7 days
```

**Expected Results:**

- ✅ Numbers match backend data
- ✅ Icons centered, proper size (24dp)
- ✅ Text readable, proper contrast
- ✅ Cards have elevation/shadow (Material)

---

### Test Case 3.3: Verify Donut Chart

**Visual Check:**

Chart should show:

```
      🎯
     100      <- Total work items (center)
  Work items

Surrounding donut slices:
- Gray slice: To Do (3 items)
- Blue slice: In Progress (2 items)
- Orange slice: In Review (1 item)
- Green slice: Done (4 items)
```

**Expected Results:**

- ✅ Chart is circular (220dp diameter)
- ✅ Hole in center (donut style, not full pie)
- ✅ Total count in center: **"10"** (bold, large)
- ✅ Label below: **"Work items"** (smaller, gray)
- ✅ Slices sized proportionally:
  - Gray (To Do): 30%
  - Blue (In Progress): 20%
  - Orange (In Review): 10%
  - Green (Done): 40%
- ✅ Colors match status indicators below
- ✅ Smooth animation on load (1 second)

**Screenshot Location:** `screenshots/project_summary_02_donut_chart.png`

---

### Test Case 3.4: Verify Status List

**Visual Check:**

Below chart, 3 rows:

```
[Gray dot]  To Do           3  [>]
[Blue dot]  In Progress     2  [>]
[Green dot] Done            4  [>]
```

**Expected Results:**

- ✅ Each row has colored dot (12dp circle)
- ✅ Status name aligned left
- ✅ Count aligned right
- ✅ Chevron (>) at far right
- ✅ Divider line between rows (light gray)
- ✅ Colors match donut chart slices

---

### Test Case 3.5: Pull-to-Refresh

**Steps:**

1. On Summary screen
2. Swipe DOWN from top of screen
3. Pull down ~100dp
4. Release finger

**Expected Results:**

- ✅ Refresh indicator appears (spinning circle)
- ✅ Screen pulls down smoothly (elastic effect)
- ✅ On release → API call to reload summary
- ✅ Data refreshes:
  - Stat cards update
  - Donut chart re-animates
  - Status list updates
- ✅ Refresh indicator disappears
- ✅ Success message (optional): "✓ Refreshed" (Snackbar)

**Screenshot Location:** `screenshots/project_summary_03_pull_to_refresh.png`

**Timing:**

- Total refresh time: 1-3 seconds

---

### Test Case 3.6: Loading State

**Steps:**

1. Clear app cache/data
2. Open Summary tab (first time load)
3. Observe loading state

**Expected Results:**

- ✅ Centered progress bar (spinning circle)
- ✅ Stat cards show skeleton/shimmer effect OR "Loading..."
- ✅ Chart area shows placeholder
- ✅ After data loads → Smooth fade-in transition

---

### Test Case 3.7: Error Handling

**Steps:**

1. Turn OFF internet
2. Navigate to Summary tab
3. API call fails

**Expected Results:**

- ✅ Error Snackbar appears at bottom:
  - Message: "❌ Failed to load summary"
  - Action button: **"Retry"**
- ✅ Tap Retry → Re-fetches data
- ✅ If still offline → Shows same error again
- ✅ No crash, no blank white screen

**Screenshot Location:** `screenshots/project_summary_04_error_state.png`

---

### Test Case 3.8: Empty State

**Steps:**

1. Use brand new project (no tasks created yet)
2. Navigate to Summary tab

**Expected Results:**

- ✅ Stat cards all show **"0"**
- ✅ Donut chart shows:
  - Center text: **"0 Work items"**
  - Single gray slice: "No data"
- ✅ Status list shows all zeros
- ✅ Optional: Empty state illustration + message "No tasks yet. Create your first task!"

---

### Test Case 3.9: Large Numbers (Stress Test)

**Setup:**

- Create project with 100+ tasks

**Expected Results:**

- ✅ Stat cards show correct counts (e.g., "127 done")
- ✅ Large numbers don't overflow/break layout
- ✅ Chart still readable with many slices
- ✅ No performance lag

---

### Test Case 3.10: Status List Click (Future Feature)

**Steps:**

1. Tap **"To Do"** row in status list
2. Should navigate to filtered task list

**Expected Results:**

- ✅ Opens Tasks screen
- ✅ Filter applied: Only shows "To Do" tasks
- ✅ Back button returns to Summary

_(If not implemented yet, document as TODO)_

---

## 🔗 Integration Testing

### Test Case INT-1: End-to-End User Flow

**Scenario:** User schedules a meeting and sees it in summary

**Steps:**

1. **Calendar Tab** → Schedule meeting
   - Select 2 members
   - Find times
   - Select time slot: **Nov 10, 10:00 AM**
   - Create meeting: "Team Standup"
2. **Summary Tab** → Verify update
   - Pull to refresh
   - Check "Created" count increased by 1
   - Donut chart updates

**Expected Results:**

- ✅ Meeting created successfully
- ✅ Summary reflects new event
- ✅ Counts accurate across tabs

---

### Test Case INT-2: Offline-to-Online Transition

**Steps:**

1. Turn OFF internet
2. Try to schedule meeting → Error
3. Turn ON internet
4. Tap "Retry"
5. Meeting created successfully

**Expected Results:**

- ✅ Offline error shown gracefully
- ✅ Retry works when back online
- ✅ No duplicate events
- ✅ No data loss

---

### Test Case INT-3: Multi-User Collaboration

**Setup:**

- 2 devices logged in as User A and User B

**Steps:**

1. **Device A (User A):** Create quick event "Sprint Review" on Nov 12
2. **Device B (User B):** Open Calendar tab
3. Pull to refresh or wait for auto-sync

**Expected Results:**

- ✅ User B sees "Sprint Review" in calendar
- ✅ Push notification sent to User B
- ✅ Real-time sync works (via WebSocket or polling)

---

## ⚡ Performance Testing

### Test Case PERF-1: Load Time

**Measure:** Time from tap to fully loaded screen

| Screen             | Target | Acceptable | Slow |
| ------------------ | ------ | ---------- | ---- |
| Summary Tab        | < 1s   | < 2s       | > 3s |
| Member Selection   | < 0.5s | < 1s       | > 2s |
| Time Slot Results  | < 2s   | < 3s       | > 5s |
| Quick Event Dialog | < 0.5s | < 1s       | > 2s |

---

### Test Case PERF-2: Scroll Performance

**Steps:**

1. Time slot list with 20+ results
2. Scroll rapidly up and down

**Expected Results:**

- ✅ Smooth 60 FPS scrolling (no lag)
- ✅ DiffUtil prevents full list re-render
- ✅ No janky animations

---

### Test Case PERF-3: Memory Usage

**Tool:** Android Studio Profiler

**Expected Results:**

- ✅ No memory leaks when closing dialogs
- ✅ Bitmaps (avatars) recycled properly
- ✅ Chart library doesn't leak views

---

## 🐛 Bug Reporting Template

**When you find a bug, report using this format:**

```markdown
### Bug Title: [Brief description]

**Priority:** Critical / High / Medium / Low

**Steps to Reproduce:**

1.
2.
3.

**Expected Result:**

**Actual Result:**

**Screenshot/Video:**
[Attach here]

**Device Info:**

- Device: Samsung Galaxy S21
- Android Version: 12
- App Version: 1.0.0
- Network: WiFi

**Additional Notes:**
```

---

## ✅ Testing Checklist

### Before Reporting Complete:

- [ ] All Meeting Scheduler tests passed (12/12)
- [ ] All Quick Event tests passed (11/11)
- [ ] All Project Summary tests passed (10/10)
- [ ] Integration tests passed (3/3)
- [ ] Performance tests passed (3/3)
- [ ] Edge cases tested (empty states, errors, offline)
- [ ] Screenshots captured (at least 10)
- [ ] Bug reports filed (if any)
- [ ] Tested on 2+ devices (phone + tablet)
- [ ] Tested on 2+ Android versions (e.g., Android 10 & 12)

---

## 📸 Required Screenshots

Save screenshots in: `Plantracker/screenshots/`

**Minimum Required:**

1. `meeting_scheduler_01_member_selection_initial.png`
2. `meeting_scheduler_02_time_slot_dialog_initial.png`
3. `meeting_scheduler_03_time_slots_results.png`
4. `meeting_scheduler_04_success.png`
5. `quick_event_01_initial.png`
6. `quick_event_02_success.png`
7. `project_summary_01_initial.png`
8. `project_summary_02_donut_chart.png`
9. `project_summary_03_pull_to_refresh.png`
10. `project_summary_04_error_state.png`

---

## 🎓 Testing Tips

**For Testers:**

1. **Test Happy Path First**

   - Follow ideal user flow
   - Verify core functionality works

2. **Then Test Edge Cases**

   - Empty states
   - Large data sets
   - Slow network
   - Offline mode

3. **Use Realistic Data**

   - Don't test with "Test User 1, Test User 2"
   - Use real-looking names, emails

4. **Check Accessibility**

   - TalkBack screen reader
   - Large font sizes
   - Color contrast

5. **Report Clearly**
   - Steps must be reproducible
   - Include screenshots
   - Note device details

---

## 📞 Support Contacts

**Questions?**

- Frontend Lead: [Contact Info]
- Backend API: [Contact Info]
- QA Manager: [Contact Info]

**Reporting Issues:**

- GitHub Issues: [Repo URL]
- Slack Channel: #plantracker-bugs

---

**Happy Testing! 🚀**

Last Updated: November 9, 2025
