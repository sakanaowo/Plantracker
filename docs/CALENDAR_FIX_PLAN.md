# 🔥 PHÂN TÍCH VẤN ĐỀ CALENDAR & PHÂN CÔNG KHẮC PHỤC

**Ngày:** 10/11/2025  
**Trạng thái:** Backend đã deploy, Frontend cần fix urgent  
**Team:** 1 BE Dev + 2 FE Dev (độc lập)

---

## 📋 TÓM TẮT CÁC VẤN ĐỀ

### ❌ **CRITICAL - Blocking Features**

1. **Tab Summary trả về 401 Unauthorized** - Endpoint thiếu guard
2. **POST /api/events trả về 400** - Frontend gửi sai format ISO 8601
3. **GET /api/calendar/sync trả về 500** - Backend lỗi khi sync Google Calendar
4. **Member list parse error** - Frontend expect Object, BE trả Array

### ⚠️ **HIGH - UX Issues**

5. **Attendee picker không hiện UI** - Frontend chưa handle response
6. **Event type (Meeting vs Milestone) không có UI khác biệt**
7. **Board scroll không work** - Đã fix trong session trước nhưng chưa deploy
8. **Activity log message không tự nhiên** - Logic generate message sai

---

## 🔍 PHÂN TÍCH SÂU TỪNG VẤN ĐỀ

### **Vấn đề 1: Tab Summary 401 Unauthorized**

**Log:**

```
GET https://plantracker-backend.onrender.com/api/projects/.../summary
<-- 401 {"message":"No token provided","error":"Unauthorized","statusCode":401}
```

**Root Cause (Backend):**

```typescript
// File: projects.controller.ts line 48-50
@Get(':id/summary')
getSummary(@Param('id') projectId: string) {  // ❌ Thiếu @UseGuards
  return this.svc.getProjectSummary(projectId);
}
```

**Impact:** Tab Summary không load được data → UX broken

**Giải pháp:**

- Backend cần thêm `@UseGuards(FirebaseAuthGuard)` vào endpoint
- Frontend log cho thấy token có sẵn nhưng endpoint không yêu cầu auth

---

### **Vấn đề 2: POST /api/events 400 Bad Request**

**Log:**

```
POST /api/events
Body: {
  "start_at":"2025-11-10T18:30:00",  // ❌ Thiếu timezone Z
  "end_at":"2025-11-10T19:00:00"     // ❌ Thiếu timezone Z
}
Response: {
  "message":["startAt must be a valid ISO 8601 date string",
             "endAt must be a valid ISO 8601 date string"]
}
```

**Root Cause (Frontend):**

```java
// CreateEventDialog.java - Gửi datetime không có timezone
CalendarEvent event = new CalendarEvent();
event.setStartAt("2025-11-10T18:30:00");  // ❌ Sai format
```

**Backend expects (DTO):**

```typescript
@IsDateString()  // Requires ISO 8601 with timezone
startAt: Date;   // Example: "2025-11-10T18:30:00Z" or "2025-11-10T18:30:00+07:00"
```

**Impact:** Không thể tạo event → Feature hoàn toàn broken

---

### **Vấn đề 3: Calendar Sync 500 Internal Server Error**

**Log:**

```
GET /api/calendar/sync/from-google?projectId=...&timeMin=2025-11-01T00:00:00&timeMax=2025-11-30T23:59:59
<-- 500 {"statusCode":500,"message":"Internal server error"}
```

**Root Cause (Backend):**

```typescript
// google-calendar.service.ts line 728
include: {
  event: true,  // ❌ Sai relation name, đã fix thành 'events'
}

const syncedEvents = [];  // ❌ Type inference error, đã fix thành any[]
```

**Status:** ✅ **ĐÃ FIX trong code nhưng CHƯA DEPLOY**

**Impact:** Calendar sync không hoạt động → Events từ Google Calendar không hiện

---

### **Vấn đề 4: Member List Parse Error**

**Log:**

```
GET /api/projects/.../members
Response: [{"id":"...","users":{...}}, ...]  // Array

Error: Expected BEGIN_OBJECT but was BEGIN_ARRAY
```

**Root Cause (Frontend):**

```java
// MemberApiService.java line 34-37
class MemberListResponse {
    public List<MemberDTO> data;  // ❌ Expect: {"data": [...]}
    public int count;
}
```

**Backend thực tế trả về:**

```json
// Array trực tiếp, không wrap trong object
[
  {"id":"...","projectId":"...","users":{...}},
  {"id":"...","projectId":"...","users":{...}}
]
```

**Impact:** Member list không hiển thị trong Project Menu

---

### **Vấn đề 5: Attendee Picker No UI**

**Log:**

```
18:28:47 Response: [{"id":"...","users":{"name":"...","email":"..."}}]
// Nhưng UI không hiện gì
```

**Root Cause (Frontend):**

```java
// CreateEventDialog.java line 160-170
if (response.isSuccessful() && response.body() != null && response.body().data != null) {
    // ❌ response.body() là Array, không có .data property
    List<User> users = new ArrayList<>();
    for (MemberDTO dto : response.body().data) {  // ❌ Null pointer
```

**Impact:** Không thể chọn attendees khi tạo event

---

### **Vấn đề 6: Event Type UI Không Khác Biệt**

**Phân tích:**

- Schema có field `type` (MEETING | MILESTONE) nhưng UI không hiển thị khác biệt
- Meeting cần `meet_link` (Zoom/Google Meet)
- Milestone không cần `meet_link`, chỉ cần `location`

**Current UI:** Chỉ có 1 form duy nhất cho cả 2 loại

**Expected UI:**

- Type selector: Radio button hoặc Chip (Meeting | Milestone)
- Nếu Meeting → Show "Meeting Link" field (required)
- Nếu Milestone → Hide "Meeting Link", show "Location" (optional)

---

### **Vấn đề 7: Board Scroll Broken**

**Status:** ✅ **ĐÃ FIX** trong session trước (TASKS_DIVISON.md Task 2)

**Fix applied:**

```xml
<!-- board_list_item.xml -->
<RecyclerView
    android:paddingHorizontal="8dp"
    android:paddingVertical="4dp"
    android:clipToPadding="false"
    android:minHeight="120dp" />
```

**Next:** Cần build APK mới và install

---

### **Vấn đề 8: Activity Log Message Không Tự Nhiên**

**Example log:**

```
Dan Bay invited sakana to team project
// ❌ Sai: Dan Bay là chính user đang đăng nhập, nhưng log nói "invited sakana"
//         → Nên là "You invited sakana to team project"
```

**Root Cause:** Logic generate message không check `userId === currentUserId`

**Impact:** UX confusing, user nghĩ người khác invite

---

## 👥 PHÂN CÔNG CHI TIẾT

### **BACKEND DEV** (2-3 giờ)

#### Task 1: Fix Summary Endpoint Auth (15 phút)

**File:** `src/modules/projects/projects.controller.ts`

```typescript
@Get(':id/summary')
@UseGuards(FirebaseAuthGuard)  // ✅ Thêm dòng này
@ApiOperation({ summary: 'Get project summary statistics' })
getSummary(
  @Param('id') projectId: string,
  @CurrentUser('id') userId: string  // ✅ Thêm param này
) {
  return this.svc.getProjectSummary(projectId, userId);  // ✅ Pass userId
}
```

**Service update:**

```typescript
async getProjectSummary(projectId: string, userId: string) {
  // ✅ Validate user có quyền access project
  await this.validateUserAccess(projectId, userId);
  // ... existing logic
}
```

---

#### Task 2: Fix Calendar Sync & Deploy (1 giờ)

**Bước 1: Verify fixes (5 phút)**

```bash
cd plantracker-backend
git status
# Should see: google-calendar.service.ts modified
```

**Bước 2: Build & Test local (10 phút)**

```bash
npm run build
# Should pass without errors
```

**Bước 3: Commit & Deploy (5 phút)**

```bash
git add .
git commit -m "fix: calendar sync endpoint - relation name and type inference"
git push origin master
# Render auto-deploy: ~3-5 minutes
```

**Bước 4: Monitor deploy (5 phút)**

- Check Render dashboard: https://dashboard.render.com
- Wait for "Live" status
- Test endpoint:

```bash
curl "https://plantracker-backend.onrender.com/api/calendar/sync/from-google?projectId=xxx&timeMin=2025-11-01T00:00:00Z&timeMax=2025-11-30T23:59:59Z" \
  -H "Authorization: Bearer <token>"
```

**Bước 5: Add error logging (30 phút)**

```typescript
// google-calendar.service.ts
async syncEventsFromGoogle(...) {
  try {
    this.logger.log(`Syncing events for project ${projectId} from ${timeMin} to ${timeMax}`);

    const integration = await this.prisma.integration_tokens.findFirst(...);
    if (!integration) {
      this.logger.error(`No Google Calendar integration found for user ${userId}`);
      throw new Error('Google Calendar not connected');
    }

    this.logger.log(`Found integration, access_token: ${integration.access_token?.substring(0, 10)}...`);

    // ... existing code

    this.logger.log(`Successfully synced ${syncedEvents.length} events`);
    return syncedEvents;
  } catch (error) {
    this.logger.error(`Failed to sync events: ${error.message}`, error.stack);
    throw error;
  }
}
```

---

#### Task 3: Add Activity Log for Event Creation (45 phút)

**File:** `src/modules/events/events.service.ts`

```typescript
async create(createEventDto: any, userId: string) {
  const event = await this.prisma.events.create({...});

  // ✅ Add activity log
  await this.activityLogService.create({
    userId,
    action: 'CREATED',
    entityType: 'EVENT',
    entityId: event.id,
    entityName: event.title,
    projectId: event.project_id,
    metadata: {
      startAt: event.start_at,
      endAt: event.end_at,
      type: 'MEETING'  // hoặc 'MILESTONE'
    }
  });

  return event;
}
```

**Test:**

```bash
# Tạo event → Check activity log API
curl "https://plantracker-backend.onrender.com/api/activity-logs/project/<projectId>?limit=20"
# Should see "CREATED EVENT" entry
```

---

### **FRONTEND DEV 1** (2-3 giờ) - Calendar & Events

#### Task 1: Fix Event Creation DateTime Format (30 phút)

**File:** `CreateEventDialog.java` (line ~280)

```java
// ❌ Before
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
event.setStartAt(sdf.format(startDate));

// ✅ After - Add timezone
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
event.setStartAt(sdf.format(startDate));
event.setEndAt(sdf.format(endDate));
```

**Test:**

- Tạo event → Check log: `"start_at":"2025-11-10T18:30:00Z"` ✅
- Response 200 → Event created successfully

---

#### Task 2: Fix Attendee Picker Response Handling (45 phút)

**File:** `CreateEventDialog.java` (line 156-170)

```java
// ❌ Before
api.getMembers(projectId).enqueue(new Callback<MemberApiService.MemberListResponse>() {
    @Override
    public void onResponse(Call<MemberApiService.MemberListResponse> call,
                         Response<MemberApiService.MemberListResponse> response) {
        if (response.isSuccessful() && response.body() != null && response.body().data != null) {
            for (MemberDTO dto : response.body().data) {  // ❌ Null

// ✅ After - Parse Array directly
api.getMembers(projectId).enqueue(new Callback<List<MemberDTO>>() {
    @Override
    public void onResponse(Call<List<MemberDTO>> call,
                         Response<List<MemberDTO>> response) {
        if (response.isSuccessful() && response.body() != null) {
            List<User> users = new ArrayList<>();
            for (MemberDTO dto : response.body()) {  // ✅ Direct array access
                if (dto.getUser() != null) {
                    User user = new User(
                        dto.getUserId(),
                        dto.getUser().getName(),
                        dto.getUser().getEmail(),
                        dto.getUser().getAvatarUrl(),
                        null
                    );
                    users.add(user);
                }
            }

            // Show bottom sheet
            MemberSelectionBottomSheet sheet = MemberSelectionBottomSheet.newInstance(users);
            sheet.setListener(selected -> {
                chipGroupAttendees.removeAllViews();
                selectedAttendeeIds.clear();
                for (User u : selected) {
                    addAttendeeChip(u.getId(), u.getName());
                }
            });
            sheet.show(getParentFragmentManager(), "select_members");
        }
    }
});
```

**File:** `MemberApiService.java` (line 16)

```java
// ❌ Before
@GET("projects/{projectId}/members")
Call<MemberListResponse> getMembers(@Path("projectId") String projectId);

// ✅ After
@GET("projects/{projectId}/members")
Call<List<MemberDTO>> getMembers(@Path("projectId") String projectId);
```

**Delete class MemberListResponse** (not needed)

---

#### Task 3: Add Event Type UI (1 giờ)

**File:** `fragment_create_event.xml`

```xml
<!-- Add after title field -->
<com.google.android.material.chip.ChipGroup
    android:id="@+id/chipGroupEventType"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="8dp"
    app:singleSelection="true"
    app:selectionRequired="true">

    <com.google.android.material.chip.Chip
        android:id="@+id/chipTypeMeeting"
        style="@style/Widget.Material3.Chip.Filter"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Meeting"
        android:checked="true"/>

    <com.google.android.material.chip.Chip
        android:id="@+id/chipTypeMilestone"
        style="@style/Widget.Material3.Chip.Filter"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Milestone"/>
</com.google.android.material.chip.ChipGroup>

<!-- Meeting Link field - show only for Meeting type -->
<com.google.android.material.textfield.TextInputLayout
    android:id="@+id/tilMeetingLink"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="16dp"
    android:hint="Meeting Link"
    android:visibility="visible"
    app:startIconDrawable="@drawable/ic_link">

    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/etMeetingLink"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:inputType="textUri"/>
</com.google.android.material.textfield.TextInputLayout>
```

**File:** `CreateEventDialog.java`

```java
private ChipGroup chipGroupEventType;
private Chip chipTypeMeeting, chipTypeMilestone;
private TextInputLayout tilMeetingLink;
private TextInputEditText etMeetingLink;

@Override
public View onCreateView(...) {
    // ... bind views
    chipGroupEventType = view.findViewById(R.id.chipGroupEventType);
    chipTypeMeeting = view.findViewById(R.id.chipTypeMeeting);
    chipTypeMilestone = view.findViewById(R.id.chipTypeMilestone);
    tilMeetingLink = view.findViewById(R.id.tilMeetingLink);
    etMeetingLink = view.findViewById(R.id.etMeetingLink);

    setupEventTypeToggle();
}

private void setupEventTypeToggle() {
    chipGroupEventType.setOnCheckedStateChangeListener((group, checkedIds) -> {
        if (checkedIds.contains(R.id.chipTypeMeeting)) {
            // Meeting selected → Show meeting link
            tilMeetingLink.setVisibility(View.VISIBLE);
            tilLocation.setVisibility(View.GONE);
        } else {
            // Milestone selected → Hide meeting link, show location
            tilMeetingLink.setVisibility(View.GONE);
            tilLocation.setVisibility(View.VISIBLE);
        }
    });
}

private void createEvent() {
    String type = chipTypeMeeting.isChecked() ? "MEETING" : "MILESTONE";

    CalendarEvent event = new CalendarEvent();
    event.setType(type);

    if (type.equals("MEETING")) {
        String meetLink = etMeetingLink.getText().toString().trim();
        if (meetLink.isEmpty()) {
            tilMeetingLink.setError("Meeting link is required");
            return;
        }
        event.setMeetLink(meetLink);
    } else {
        event.setLocation(etLocation.getText().toString().trim());
    }

    // ... rest of create logic
}
```

---

### **FRONTEND DEV 2** (2-3 giờ) - Project UI & Activity

#### Task 1: Fix Member List Display in Project Menu (45 phút)

**File:** `ProjectMenuBottomSheet.java` (similar to CreateEventDialog fix)

```java
// Change response type
api.getMembers(projectId).enqueue(new Callback<List<MemberDTO>>() {
    @Override
    public void onResponse(Call<List<MemberDTO>> call,
                         Response<List<MemberDTO>> response) {
        if (response.isSuccessful() && response.body() != null) {
            List<Member> members = new ArrayList<>();
            for (MemberDTO dto : response.body()) {
                Member member = new Member();
                member.setId(dto.getId());
                member.setUserId(dto.getUserId());
                member.setRole(dto.getRole());
                member.setUser(dto.getUsers());  // UserInfo from API
                members.add(member);
            }

            memberAdapter.setMembers(members);
            Log.d(TAG, "Loaded " + members.size() + " members");
        } else {
            Log.e(TAG, "Failed to load members: " + response.code());
        }
    }
});
```

**Test:** Mở Project Menu → Members list hiện đúng với avatar

---

#### Task 2: Improve Activity Log Messages (1 giờ)

**File:** `ActivityLogAdapter.java`

```java
private String formatActivityMessage(ActivityLogDTO log, String currentUserId) {
    boolean isSelf = log.getUserId().equals(currentUserId);
    String userName = isSelf ? "You" : log.getUsers().getName();

    switch (log.getAction()) {
        case "CREATED":
            if (log.getEntityType().equals("TASK")) {
                return userName + " created task \"" + log.getEntityName() + "\"";
            } else if (log.getEntityType().equals("PROJECT")) {
                return userName + " created project \"" + log.getEntityName() + "\"";
            } else if (log.getEntityType().equals("EVENT")) {
                return userName + " created event \"" + log.getEntityName() + "\"";
            }
            break;

        case "ADDED":
            if (log.getEntityType().equals("MEMBERSHIP")) {
                // Check metadata để xem đây là invitation hay acceptance
                if (log.getMetadata() != null) {
                    String type = log.getMetadata().getType();
                    if ("INVITATION_SENT".equals(type)) {
                        return userName + " invited " + log.getEntityName() + " to the project";
                    } else if ("INVITATION_ACCEPTED".equals(type)) {
                        return log.getEntityName() + " joined the project";
                    }
                }
            }
            break;

        case "ASSIGNED":
            if (log.getEntityType().equals("TASK")) {
                // Parse newValue to get assigneeId
                String assigneeId = log.getNewValue().getAssigneeId();
                if (assigneeId.equals(currentUserId)) {
                    return userName + " assigned you to \"" + log.getEntityName() + "\"";
                } else {
                    return userName + " assigned task \"" + log.getEntityName() + "\"";
                }
            }
            break;

        case "UPDATED":
            return userName + " updated " + log.getEntityType().toLowerCase() +
                   " \"" + log.getEntityName() + "\"";

        default:
            return userName + " " + log.getAction().toLowerCase() + " " +
                   log.getEntityType().toLowerCase();
    }
}
```

**Test:** Check Activity tab → Messages sound natural

---

#### Task 3: Add Empty State for Calendar Events (30 phút)

**File:** `fragment_project_calendar.xml`

```xml
<!-- Add empty state layout -->
<LinearLayout
    android:id="@+id/layoutEmptyCalendar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_gravity="center"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="32dp"
    android:visibility="gone">

    <ImageView
        android:layout_width="120dp"
        android:layout_height="120dp"
        android:src="@drawable/ic_calendar_empty"
        android:alpha="0.3"/>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="No events this month"
        android:textSize="16sp"
        android:textColor="?attr/colorOnSurfaceVariant"/>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="Tap + to create an event or sync from Google Calendar"
        android:textSize="14sp"
        android:gravity="center"
        android:textColor="?attr/colorOnSurfaceVariant"
        android:alpha="0.7"/>
</LinearLayout>
```

**File:** `ProjectCalendarFragment.java`

```java
private void updateEventsList(List<CalendarEvent> events) {
    if (events == null || events.isEmpty()) {
        rvCalendarEvents.setVisibility(View.GONE);
        layoutEmptyCalendar.setVisibility(View.VISIBLE);
        tvEventCount.setText("0 events");
    } else {
        rvCalendarEvents.setVisibility(View.VISIBLE);
        layoutEmptyCalendar.setVisibility(View.GONE);
        tvEventCount.setText(events.size() + " events");
        eventAdapter.setEvents(events);
    }
}
```

---

## ⏱️ TIMELINE ƯỚC TÍNH

| Dev     | Task                 | Thời gian | Độc lập?               |
| ------- | -------------------- | --------- | ---------------------- |
| **BE**  | Fix Summary Auth     | 15 phút   | ✅                     |
| **BE**  | Deploy Calendar Sync | 1 giờ     | ✅                     |
| **BE**  | Add Activity Log     | 45 phút   | ✅                     |
| **FE1** | Fix DateTime Format  | 30 phút   | ✅ (sau khi BE deploy) |
| **FE1** | Fix Attendee Picker  | 45 phút   | ✅                     |
| **FE1** | Add Event Type UI    | 1 giờ     | ✅                     |
| **FE2** | Fix Member List      | 45 phút   | ✅                     |
| **FE2** | Improve Activity Log | 1 giờ     | ✅                     |
| **FE2** | Add Empty State      | 30 phút   | ✅                     |

**Tổng:** ~6 giờ (song song) → **~3 giờ real time**

---

## ✅ TESTING CHECKLIST

### Backend (BE Dev)

- [ ] `GET /api/projects/:id/summary` trả về 200 với token
- [ ] `GET /api/calendar/sync/from-google` trả về 200 và sync events
- [ ] Activity log có entry "CREATED EVENT" sau khi tạo event
- [ ] Render logs không có error 500

### Frontend Dev 1 (Calendar)

- [ ] Tạo event → Request body có `start_at: "...Z"` format
- [ ] Tạo event → Response 200 → Event hiện trong calendar
- [ ] Click "Add Attendee" → Bottom sheet hiện danh sách members
- [ ] Chọn attendees → Chips hiện đúng tên
- [ ] Toggle Meeting/Milestone → UI fields thay đổi đúng
- [ ] Meeting type bắt buộc nhập Meeting Link
- [ ] Sync Calendar → Events từ Google hiện trong list

### Frontend Dev 2 (Project UI)

- [ ] Project Menu → Members list hiện đúng với avatar
- [ ] Activity tab → Messages tự nhiên ("You created...", "joined...")
- [ ] Calendar empty → Empty state hiện với icon và message
- [ ] Board scroll → Chạm vùng trống vẫn scroll được

---

## 🚀 DEPLOYMENT PLAN

### Phase 1: Backend Deploy (BE Dev - 1 giờ)

1. Fix Summary endpoint
2. Verify Calendar Sync fixes
3. Add Activity Log
4. Commit → Push → Wait Render deploy
5. Test endpoints with Postman/curl

### Phase 2: Frontend Build (FE1 + FE2 - song song 2 giờ)

1. FE1: Fix CreateEventDialog
2. FE2: Fix ProjectMenuBottomSheet
3. Both: Test locally

### Phase 3: Integration Test (All - 30 phút)

1. Build APK
2. Install trên thiết bị test
3. Test E2E theo checklist
4. Fix bugs nhỏ nếu có

### Phase 4: Final Commit (All - 15 phút)

1. Commit tất cả changes
2. Push to develop branch
3. Create summary report

---

## 📝 NOTES

- **FE1 và FE2 hoàn toàn độc lập**, không block nhau
- Backend cần deploy trước thì FE mới test được Calendar Sync
- Nhưng FE có thể code trước, test sau khi BE deploy
- Dùng Logcat để debug, enable verbose logging
- Test trên emulator Android 12+ để tránh permission issues

---

**END OF DOCUMENT**
