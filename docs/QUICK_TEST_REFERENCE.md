# 🎯 Quick UI Testing Reference Card

**For:** QA Team / Manual Testing  
**Date:** November 9, 2025  
**App:** Plantracker Android

---

## 📱 Test Environment Setup (5 min)

### Quick Checklist

- [ ] App installed on Android device (API 24+)
- [ ] Logged in with test account
- [ ] Google account connected
- [ ] Test project created with 3+ members
- [ ] Sample tasks created (3 To Do, 2 In Progress, 4 Done)
- [ ] Internet connection active

---

## 🎯 Feature 1: Meeting Scheduler (10 min)

### User Flow

```
Calendar Tab → "Schedule Meeting" → Select Members → Next →
Find Times → Pick Time Slot → Create Meeting → Success ✓
```

### Quick Test Steps

**1. Open Member Selection** (30 sec)

- Tap "Schedule Meeting" button
- ✅ Bottom sheet slides up
- ✅ Search box visible
- ✅ "Next" button DISABLED (gray)

**2. Search Members** (30 sec)

- Type in search box
- ✅ List filters instantly
- ✅ Matching members shown

**3. Select 2+ Members** (30 sec)

- Tap checkboxes
- ✅ Count updates "2 members selected"
- ✅ "Next" button ENABLED (blue)

**4. Find Time Slots** (1 min)

- Tap "Next"
- Set duration: 60 min
- Set date range: Next 7 days
- Tap "Find Times"
- ✅ Loading shows
- ✅ 5 time slots appear
- ✅ Color-coded chips:
  - 🟢 Green (≥80%)
  - 🟠 Orange (≥60%)
  - 🔴 Red (<60%)

**5. Create Meeting** (30 sec)

- Tap a time slot
- Enter title
- Tap "Create"
- ✅ Success message
- ✅ Google Meet link shown

**PASS CRITERIA:** All ✅ = PASS

---

## 🎯 Feature 2: Quick Event (5 min)

### User Flow

```
Calendar Tab → FAB (+) → Fill Form → Create → Success ✓
```

### Quick Test Steps

**1. Open Dialog** (10 sec)

- Tap FAB with + icon
- ✅ Dialog appears
- ✅ Keyboard shows (title focused)

**2. Fill All Fields** (2 min)

- **Title:** "Sprint Review"
- **Date:** Tap → Pick Nov 15 → OK
- **Time:** Tap → Pick 3:00 PM → OK
- **Duration:** Select 60 min
- **Type:** Tap "Milestone" chip
- **Google Meet:** Toggle ON
- **Description:** "Review sprint goals"

**3. Create Event** (10 sec)

- Tap "Create" button
- ✅ Loading shows
- ✅ Success: "✓ Event created!"
- ✅ Meet link displayed
- ✅ Dialog closes

**4. Validation Test** (1 min)

- Open dialog again
- Leave title EMPTY
- Tap "Create"
- ✅ Error: "Title is required"

**PASS CRITERIA:** All ✅ = PASS

---

## 🎯 Feature 3: Project Summary (5 min)

### User Flow

```
Project → Summary Tab → View Stats → Pull Refresh → Updated ✓
```

### Quick Test Steps

**1. View Summary** (30 sec)

- Navigate to Summary tab
- ✅ 4 stat cards visible:
  - Done (gray icon)
  - Updated (gray icon)
  - Created (gray icon)
  - Due (gray icon)
- ✅ Numbers displayed

**2. Check Donut Chart** (1 min)

- Scroll to "Status overview" card
- ✅ Chart visible with colored slices:
  - Gray: To Do
  - Blue: In Progress
  - Orange: In Review
  - Green: Done
- ✅ Total count in center
- ✅ Smooth animation (1 second)

**3. Pull to Refresh** (30 sec)

- Swipe DOWN from top
- ✅ Refresh indicator shows
- ✅ Data reloads
- ✅ Chart re-animates

**4. Error Test** (1 min)

- Turn OFF WiFi
- Pull to refresh
- ✅ Error Snackbar: "❌ Failed to load"
- ✅ "Retry" button visible
- Turn ON WiFi
- Tap "Retry"
- ✅ Data loads successfully

**PASS CRITERIA:** All ✅ = PASS

---

## ⚡ Quick Visual Checks

### Color Coding Verification

**Time Slot Chips:**
| Score | Color | Text |
|-------|-------|------|
| 100% | 🟢 Green | Excellent |
| 75% | 🟠 Orange | Good |
| 50% | 🔴 Red | Fair |

**Donut Chart Slices:**
| Status | Color |
|--------|-------|
| To Do | Gray (#9E9E9E) |
| In Progress | Blue (#2196F3) |
| In Review | Orange (#FF9800) |
| Done | Green (#4CAF50) |

---

## 🐛 Common Issues Checklist

### If Member Selection Doesn't Open:

- [ ] Check if "Schedule Meeting" button exists
- [ ] Check if user is in Calendar tab
- [ ] Restart app

### If Time Slots Don't Load:

- [ ] Check internet connection
- [ ] Verify Google account connected
- [ ] Check date range is future dates
- [ ] Look for error message in Snackbar

### If Quick Event Fails:

- [ ] Verify title is filled
- [ ] Check date/time not in past
- [ ] Confirm internet connection
- [ ] Look for error Snackbar

### If Donut Chart Missing:

- [ ] Clear app cache
- [ ] Restart app
- [ ] Check if project has tasks

---

## 📊 Performance Benchmarks

| Action                | Target | Max | Fail |
| --------------------- | ------ | --- | ---- |
| Open Member Selection | < 0.5s | 1s  | > 2s |
| Load Time Slots       | < 2s   | 3s  | > 5s |
| Open Quick Event      | < 0.5s | 1s  | > 2s |
| Load Summary          | < 1s   | 2s  | > 3s |
| Pull Refresh          | < 2s   | 3s  | > 5s |

---

## 📸 Screenshot Checklist

**Required Screenshots (10 total):**

**Meeting Scheduler (4):**

- [ ] Member selection with search
- [ ] Time slot dialog
- [ ] Time slots with color chips
- [ ] Success with Meet link

**Quick Event (2):**

- [ ] Dialog with all fields filled
- [ ] Success message

**Project Summary (4):**

- [ ] Overview with 4 cards
- [ ] Donut chart close-up
- [ ] Pull-to-refresh indicator
- [ ] Error Snackbar

---

## ✅ Final Checklist

### Before Reporting Complete:

**Functionality:**

- [ ] Meeting Scheduler: All 5 steps work
- [ ] Quick Event: Create successful
- [ ] Project Summary: Stats accurate
- [ ] Pull-to-refresh works
- [ ] Donut chart displays correctly

**UI/UX:**

- [ ] All colors correct
- [ ] Animations smooth (60 FPS)
- [ ] Loading indicators show
- [ ] Error messages clear

**Integration:**

- [ ] Google Meet links generated
- [ ] Data syncs across tabs
- [ ] Notifications received (if applicable)

**Edge Cases:**

- [ ] Empty states handled
- [ ] Offline errors shown
- [ ] Large data sets work

**Screenshots:**

- [ ] 10/10 screenshots captured
- [ ] Saved in `screenshots/` folder
- [ ] Named correctly

---

## 🎯 Quick Pass/Fail Decision

### PASS if:

✅ All 3 features work end-to-end  
✅ No crashes or freezes  
✅ Data displays correctly  
✅ Colors match spec  
✅ Errors handled gracefully

### FAIL if:

❌ Any feature crashes  
❌ Data doesn't load  
❌ UI broken/misaligned  
❌ No error messages shown  
❌ Performance < benchmarks

---

## 📞 Bug Reporting (Quick Template)

```markdown
**Bug:** [One-line description]

**Steps:**

1.
2.
3.

**Expected:** [What should happen]
**Actual:** [What happened]

**Screenshot:** [Attach]
**Device:** [e.g., Samsung S21, Android 12]
**Severity:** Critical / High / Medium / Low
```

---

## ⏱️ Time Estimates

| Feature           | Test Time | Total      |
| ----------------- | --------- | ---------- |
| Meeting Scheduler | 10 min    |            |
| Quick Event       | 5 min     |            |
| Project Summary   | 5 min     |            |
| Screenshots       | 10 min    |            |
| **TOTAL**         |           | **30 min** |

---

## 🎓 Pro Tips

1. **Test Happy Path First**

   - Follow exact steps in User Flow
   - Ensure basic functionality works

2. **Then Test Edge Cases**

   - Empty inputs
   - Offline mode
   - Large data

3. **Check Visual Polish**

   - Smooth animations
   - Proper colors
   - Readable text

4. **Performance Matters**
   - Should feel instant
   - No lag during scroll
   - Quick API responses

---

**For detailed testing:** See `UI_TESTING_GUIDE.md` (33 test cases)

**Questions?** Contact Dev Team

---

**Last Updated:** November 9, 2025  
**Version:** 1.0
