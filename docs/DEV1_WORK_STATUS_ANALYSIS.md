# 📊 PHÂN TÍCH CÔNG VIỆC DEV 1 - CALENDAR TAB & CALENDAR SYNC UI

## 🔍 TÌNH TRẠNG HIỆN TẠI

### ✅ PHẦN ĐÃ CÓ SẴN

#### 1. **CardDetailActivity.java** 
- ✅ File đã tồn tại và hoạt động tốt
- ✅ Có đầy đủ các thành phần cơ bản:
  - EditText cho Task Title
  - TextView cho Board Name  
  - Description, Date pickers
  - Comments section với RecyclerView
  - Attachments section với RecyclerView
  - Checklist section
  - Labels section
  - Quick actions buttons
- ✅ ViewModel đã được setup (TaskViewModel, LabelViewModel)
- ✅ API integration đã có (TaskApiService, CommentApiService, AttachmentApiService)

#### 2. **card_detail.xml**
- ✅ Layout đã tồn tại và đầy đủ chức năng
- ✅ Có các sections:
  - Header với close button
  - Task title và Board name
  - Quick Actions (4 buttons)
  - Description field
  - Date pickers (Start Date, Due Date) với Material Card design
  - Labels section
  - Checklist section
  - Comments section với input field
  - Attachments section
  - Confirm button

#### 3. **ProjectActivity.java**
- ✅ File đã tồn tại
- ✅ Có TabLayout với 3 tabs: Board, Calendar, Event
- ✅ Boards RecyclerView đang hoạt động
- ✅ ViewModel đã setup (ProjectViewModel, BoardViewModel, TaskViewModel)
- ⚠️ **CHƯA IMPLEMENT**: Logic switching giữa tabs
- ⚠️ **CHƯA IMPLEMENT**: Calendar fragment integration

#### 4. **project_main.xml**
- ✅ Layout đã có TabLayout với 3 tabs
- ✅ RecyclerView cho boards đang hoạt động
- ❌ **THIẾU**: FrameLayout container cho fragments

#### 5. **Task.java Model**
- ✅ Model đã tồn tại với đầy đủ fields:
  - id, projectId, boardId, title, description
  - status, priority, position
  - assigneeId, createdBy
  - startAt, dueAt (Date fields)
  - createdAt, updatedAt
- ❌ **THIẾU**: Các fields cho Calendar Sync:
  - calendarSyncEnabled (boolean)
  - calendarReminderMinutes (List<Integer>)
  - calendarEventId (String)
  - calendarSyncedAt (Date)

#### 6. **Dependencies (build.gradle.kts)**
- ✅ Có các dependencies cơ bản:
  - Material Components
  - Lifecycle components (LiveData, ViewModel)
  - RecyclerView
  - Retrofit, OkHttp
  - Firebase
- ❌ **THIẾU**: MaterialCalendarView dependency
  ```gradle
  implementation 'com.prolificinteractive:material-calendarview:1.4.3'
  ```

#### 7. **Drawable Resources**
- ✅ Có sẵn nhiều icons:
  - ic_close.xml
  - ic_label.xml
  - add_ic.xml
  - clock_ic.xml
  - square_ic.xml
- ❌ **THIẾU** các icons cho Calendar:
  - ic_google_calendar.xml
  - ic_sync.xml
  - ic_open_in_new.xml
- ❌ **THIẾU**: rounded_background_light_green.xml

---

## 📋 CÔNG VIỆC CẦN LÀM CỦA DEV 1

### **GIAI ĐOẠN 1: CALENDAR SYNC UI (Ưu tiên cao)**

#### ✅ Checklist Task 1.1: Calendar Sync Section trong CardDetailActivity

##### **A. Tạo XML Resources**
- [ ] 1. Tạo `dialog_calendar_reminder_settings.xml`
  - Layout cho dialog chọn reminder times
  - 4 checkboxes: 15 phút, 1 giờ, 1 ngày, 1 tuần
  
- [ ] 2. Tạo Drawable Icons:
  - [ ] `ic_google_calendar.xml` (Google Calendar icon)
  - [ ] `ic_sync.xml` (Sync icon)
  - [ ] `ic_open_in_new.xml` (Open in new window icon)
  - [ ] `rounded_background_light_green.xml` (Background cho calendar section)

- [ ] 3. Thêm Strings vào `res/values/strings.xml`:
  ```xml
  <string name="sync_with_google_calendar">Đồng bộ với Google Calendar</string>
  <string name="view_in_calendar">Xem trong Calendar</string>
  <string name="calendar_reminder_settings">Cài đặt nhắc nhở</string>
  <string name="connect_google_calendar">Kết nối Google Calendar</string>
  ```

##### **B. Chỉnh sửa card_detail.xml**
- [ ] 4. Thêm Calendar Sync Section sau `etDueDate`:
  ```xml
  <LinearLayout android:id="@+id/calendarSyncSection">
    - SwitchMaterial để bật/tắt sync
    - TextView hiển thị thông tin event
    - Button "View in Calendar"
  ```

##### **C. Tạo Java Classes**
- [ ] 5. Tạo `CalendarReminderSettingsDialog.java`:
  - Extends DialogFragment
  - 4 CheckBoxes cho reminder times
  - Interface OnSaveListener
  - Logic lưu preferences

- [ ] 6. Chỉnh sửa `CardDetailActivity.java`:
  - [ ] Thêm biến members:
    ```java
    private SwitchMaterial switchCalendarSync;
    private MaterialButton btnCalendarSyncSettings;
    private MaterialButton btnViewInCalendar;
    private TextView tvCalendarEventInfo;
    private LinearLayout layoutCalendarDetails;
    private boolean isCalendarSyncEnabled = true;
    private List<Integer> reminderMinutes = Arrays.asList(15, 60, 1440);
    ```
  
  - [ ] Thêm method `setupCalendarSyncUI()`:
    - Initialize views
    - Check Google Calendar connection status
    - Setup listeners cho switch, buttons
  
  - [ ] Thêm method `checkGoogleCalendarConnection()`:
    - Call API `/api/google-auth/status`
    - Update UI dựa trên kết quả
  
  - [ ] Thêm method `showReminderSettingsDialog()`:
    - Show CalendarReminderSettingsDialog
    - Handle save reminder preferences
  
  - [ ] Thêm method `openGoogleCalendarEvent()`:
    - Intent to open Calendar app
    - Fallback to browser
  
  - [ ] Modify `saveTask()` method:
    - Thêm calendar sync info vào Task object
    - Call API với calendar data

##### **D. API Integration**
- [ ] 7. Tạo `GoogleAuthApiService.java` trong `data/remote/api/`:
  ```java
  @GET("google-auth/status")
  Call<GoogleCalendarStatusResponse> getIntegrationStatus();
  
  @GET("google-auth/auth-url")
  Call<AuthUrlResponse> getAuthUrl();
  
  @POST("google-auth/disconnect")
  Call<Void> disconnect();
  ```

- [ ] 8. Tạo Response Models trong `domain/model/`:
  - [ ] `GoogleCalendarStatusResponse.java`:
    ```java
    public class GoogleCalendarStatusResponse {
        private boolean connected;
        private String userEmail;
        // Getters/setters
    }
    ```
  
  - [ ] `AuthUrlResponse.java`:
    ```java
    public class AuthUrlResponse {
        private String url;
        // Getters/setters
    }
    ```

##### **E. Update Task Model**
- [ ] 9. Chỉnh sửa `Task.java`:
  - [ ] Thêm fields:
    ```java
    private boolean calendarSyncEnabled;
    private List<Integer> calendarReminderMinutes;
    private String calendarEventId;
    private Date calendarSyncedAt;
    ```
  - [ ] Thêm getters/setters
  - [ ] Update constructor

---

### **GIAI ĐOẠN 2: CALENDAR TAB (Ưu tiên trung bình)**

#### ✅ Checklist Task 2.1: Tạo Calendar Fragment

##### **A. Thêm Dependencies**
- [ ] 1. Update `app/build.gradle.kts`:
  ```gradle
  implementation 'com.prolificinteractive:material-calendarview:1.4.3'
  ```

##### **B. Tạo Layouts**
- [ ] 2. Tạo `fragment_project_calendar.xml`:
  - MaterialCalendarView
  - Toggle buttons (Week/Month view)
  - Buttons (Sync, Filter, Export)
  - RecyclerView cho events list
  - ProgressBar

- [ ] 3. Tạo `item_calendar_event.xml`:
  - Layout cho mỗi event trong list
  - Icon, title, time, status

##### **C. Tạo Java Classes**
- [ ] 4. Tạo `ProjectCalendarFragment.java`:
  - [ ] Setup MaterialCalendarView
  - [ ] Setup RecyclerView cho events
  - [ ] Date selection listener
  - [ ] View mode toggle (Week/Month)
  - [ ] Sync button logic
  - [ ] Filter và Export buttons
  - [ ] Load calendar data từ API

- [ ] 5. Tạo `CalendarEventAdapter.java`:
  - RecyclerView.Adapter cho calendar events
  - ViewHolder pattern
  - Click listeners

- [ ] 6. Tạo `ProjectCalendarViewModel.java`:
  - LiveData cho calendar events
  - Method loadProjectCalendarEvents()
  - Method getEventsForDate()
  - Method syncWithGoogleCalendar()

- [ ] 7. Tạo `EventDecorator.java`:
  - Custom decorator cho MaterialCalendarView
  - Màu sắc khác nhau cho:
    - Task deadlines (red)
    - Meetings (blue)
    - Milestones (green)

##### **D. Domain Models**
- [ ] 8. Tạo `CalendarEvent.java` trong `domain/model/`:
  ```java
  public class CalendarEvent {
      private String id;
      private String title;
      private String description;
      private Date date;
      private String time;
      private CalendarEventType type;
      private String taskId;
      private String calendarEventId;
      private boolean synced;
      // Getters/setters
  }
  ```

- [ ] 9. Tạo `CalendarEventType.java` enum:
  ```java
  public enum CalendarEventType {
      TASK_DEADLINE,
      MEETING,
      MILESTONE,
      OTHER
  }
  ```

#### ✅ Checklist Task 2.2: Tích hợp vào ProjectActivity

##### **A. Chỉnh sửa project_main.xml**
- [ ] 1. Thay RecyclerView bằng FrameLayout:
  ```xml
  <FrameLayout
      android:id="@+id/fragmentContainer"
      android:layout_width="match_parent"
      android:layout_height="0dp">
      
      <RecyclerView
          android:id="@+id/boardsRecyclerView"
          android:visibility="visible"/>
  </FrameLayout>
  ```

##### **B. Chỉnh sửa ProjectActivity.java**
- [ ] 2. Thêm TabLayout listener:
  ```java
  tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override
      public void onTabSelected(TabLayout.Tab tab) {
          switch (tab.getPosition()) {
              case 0: showBoardView(); break;
              case 1: showCalendarFragment(); break;
              case 2: // Events (Dev 2)
          }
      }
  });
  ```

- [ ] 3. Thêm methods:
  - [ ] `showBoardView()` - hiển thị RecyclerView
  - [ ] `showCalendarFragment()` - show/hide fragment
  - [ ] Handle fragment lifecycle

- [ ] 4. Test tab switching:
  - Board tab → Calendar tab → Board tab
  - Data persistence khi switch
  - Fragment lifecycle

---

### **GIAI ĐOẠN 3: API INTEGRATION**

#### ✅ Checklist API Endpoints

- [ ] 1. Verify backend endpoints:
  - [ ] `GET /api/google-auth/status`
  - [ ] `GET /api/google-auth/auth-url`
  - [ ] `POST /api/google-auth/disconnect`
  - [ ] `GET /api/projects/{id}/calendar`
  - [ ] `POST /api/calendar/sync`

- [ ] 2. Tạo Repository cho Calendar:
  - [ ] `ICalendarRepository.java` (interface)
  - [ ] `CalendarRepositoryImpl.java`

- [ ] 3. Tạo Use Cases:
  - [ ] `GetCalendarEventsUseCase.java`
  - [ ] `SyncCalendarUseCase.java`
  - [ ] `GetGoogleAuthStatusUseCase.java`

- [ ] 4. Update ViewModel Factory:
  - [ ] Add calendar use cases
  - [ ] Inject repository

---

### **GIAI ĐOẠN 4: TESTING & POLISH**

#### ✅ Checklist Testing

##### **A. Calendar Sync UI Tests**
- [ ] 1. Switch bật/tắt:
  - [ ] Toggle hoạt động
  - [ ] Layout details show/hide đúng
  - [ ] State được lưu

- [ ] 2. Reminder Settings Dialog:
  - [ ] Dialog mở đúng
  - [ ] Checkboxes hoạt động
  - [ ] Save preferences
  - [ ] Display reminder info đúng

- [ ] 3. View in Calendar Button:
  - [ ] Mở Calendar app khi có eventId
  - [ ] Fallback to browser nếu no app
  - [ ] Error handling

- [ ] 4. Google Calendar Connection:
  - [ ] Status check hoạt động
  - [ ] Connected state UI
  - [ ] Not connected state UI
  - [ ] Connect dialog

##### **B. Calendar Tab Tests**
- [ ] 1. Calendar Display:
  - [ ] Hiển thị tháng hiện tại
  - [ ] Date selection
  - [ ] Color decorators
  - [ ] Week/Month toggle

- [ ] 2. Events List:
  - [ ] Load events cho ngày đã chọn
  - [ ] Empty state
  - [ ] Click event → open task detail
  - [ ] Scroll behavior

- [ ] 3. Sync Button:
  - [ ] Loading state
  - [ ] Success message
  - [ ] Error handling
  - [ ] Refresh data

##### **C. Integration Tests**
- [ ] 1. Tab Switching:
  - [ ] Board ↔ Calendar smooth
  - [ ] Data persistence
  - [ ] Fragment lifecycle
  - [ ] Memory leaks check

- [ ] 2. API Tests:
  - [ ] Network errors
  - [ ] Timeout handling
  - [ ] Loading states
  - [ ] Error messages

##### **D. UI Polish**
- [ ] 1. Responsive Design:
  - [ ] Test trên các screen sizes
  - [ ] Portrait/Landscape
  - [ ] Tablet layout

- [ ] 2. Animations:
  - [ ] Fragment transitions
  - [ ] Calendar animations
  - [ ] Loading indicators

- [ ] 3. Accessibility:
  - [ ] Content descriptions
  - [ ] Touch targets (48dp)
  - [ ] Color contrast

- [ ] 4. States:
  - [ ] Loading states
  - [ ] Empty states
  - [ ] Error states
  - [ ] Success states

---

## 🚨 VẤN ĐỀ CẦN LƯU Ý

### **1. Task Model - Immutable Issue**
⚠️ Task model hiện tại là **immutable** (final fields, no setters)
- **Vấn đề**: Không thể thêm calendar sync fields đơn giản
- **Giải pháp**:
  - **Option A**: Tạo constructor mới với thêm calendar fields
  - **Option B**: Tạo `TaskBuilder` pattern
  - **Option C**: Tạo separate `CalendarSyncInfo` class

### **2. ProjectActivity Layout**
⚠️ `project_main.xml` chưa có FrameLayout container
- **Hiện tại**: RecyclerView trực tiếp trong layout
- **Cần**: Wrap trong FrameLayout để add/hide fragments

### **3. Dependencies**
⚠️ MaterialCalendarView chưa được thêm vào build.gradle
- Cần thêm dependency trước khi code Calendar Tab

### **4. API Endpoints**
⚠️ Backend endpoints chưa được verify
- Cần confirm với Backend Dev về:
  - Endpoint URLs
  - Request/Response format
  - Authentication

### **5. Fragment Management**
⚠️ ProjectActivity chưa có fragment transaction logic
- Cần implement proper fragment lifecycle
- Handle back stack
- Avoid memory leaks

---

## 📊 THỜI GIAN ƯỚC TÍNH

### Breakdown theo giai đoạn:
1. **Calendar Sync UI**: 3 ngày
   - Day 1: XML layouts + icons (4h) + Java classes (4h)
   - Day 2: API integration (6h) + Task model update (2h)
   - Day 3: Testing + bug fixes (8h)

2. **Calendar Tab**: 4 ngày
   - Day 1: Dependencies + layouts (6h) + Fragment setup (2h)
   - Day 2: CalendarView integration (6h) + Event adapter (2h)
   - Day 3: ViewModel + API (6h) + Decorators (2h)
   - Day 4: ProjectActivity integration (8h)

3. **Testing & Polish**: 1 ngày
   - Morning: Unit tests + Integration tests (4h)
   - Afternoon: UI polish + Bug fixes (4h)

**TỔNG**: 8 ngày làm việc

---

## 🎯 ĐỀ XUẤT THỨ TỰ THỰC HIỆN

### **Week 1 (5 ngày)**
1. **Day 1-3**: Calendar Sync UI (Task 1.1)
   - Hoàn thành Calendar Sync Section
   - Test thoroughly
   - Working demo

2. **Day 4-5**: Calendar Tab - Phase 1
   - Add dependency
   - Create layouts
   - Basic fragment setup

### **Week 2 (3 ngày)**
1. **Day 1-2**: Calendar Tab - Phase 2
   - Complete fragment logic
   - API integration
   - ProjectActivity integration

2. **Day 3**: Testing & Polish
   - All test cases
   - UI improvements
   - Documentation

---

## ✅ DEFINITION OF DONE

Công việc hoàn thành khi:
- [x] Calendar Sync UI hoạt động đầy đủ
- [x] Calendar Tab hiển thị và navigate được
- [x] Tab switching smooth không crash
- [x] API integration hoạt động
- [x] Tất cả test cases pass
- [x] UI polish và responsive
- [x] No critical bugs
- [x] Code review approved
- [x] Documentation updated

---

## 📞 HỖ TRỢ CẦN THIẾT

### Cần hỏi Backend Dev:
1. API endpoints có sẵn chưa?
2. Request/Response format?
3. Google Calendar OAuth flow?
4. Test credentials?

### Cần hỏi Team Lead:
1. Task model update strategy?
2. Priority features?
3. Design mockups available?

### Cần hỏi Dev 2:
1. Shared models nào?
2. Interface nào cần coordination?

---

**Cập nhật**: 7 tháng 11, 2025
**Status**: Planning Phase
**Next Action**: Start with Calendar Sync UI (Task 1.1)
