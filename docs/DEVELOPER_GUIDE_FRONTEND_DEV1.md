# 📱 HƯỚNG DẪN PHÁT TRIỂN - FRONTEND DEVELOPER 1

## 👤 **NHIỆM VỤ: CALENDAR TAB & CALENDAR SYNC UI**

### 🎯 **TỔNG QUAN**

Bạn chịu trách nhiệm phát triển **Tab Calendar** và **Calendar Sync UI** trong CardDetailActivity. Công việc của bạn **hoàn toàn độc lập** với Frontend Dev 2 (người phụ trách Events Tab).

---

## 📋 **DANH SÁCH CÔNG VIỆC**

### **GIAI ĐOẠN 1: CALENDAR SYNC UI (TUẦN 1 - 3 NGÀY)**

#### **Task 1.1: Thêm Calendar Sync Section vào CardDetailActivity**

**Mục tiêu:** Thêm UI cho phép user bật/tắt đồng bộ Google Calendar khi tạo/sửa task.

**Files cần tạo/chỉnh sửa:**

```
📁 app/src/main/res/
├── layout/
│   ├── card_detail.xml (CHỈNH SỬA - thêm Calendar Sync section)
│   └── dialog_calendar_reminder_settings.xml (TẠO MỚI)
├── drawable/
│   ├── ic_google_calendar.xml (TẠO MỚI)
│   ├── ic_sync.xml (TẠO MỚI)
│   ├── ic_open_in_new.xml (TẠO MỚI)
│   └── rounded_background_light_green.xml (TẠO MỚI)
└── values/
    └── strings.xml (CHỈNH SỬA - thêm calendar strings)

📁 app/src/main/java/com/example/tralalero/
└── feature/home/ui/Home/project/
    ├── CardDetailActivity.java (CHỈNH SỬA)
    └── CalendarReminderSettingsDialog.java (TẠO MỚI)
```

**Chi tiết implementation:**

**1. Chỉnh sửa card_detail.xml:**

```xml
<!-- Thêm sau etDueDate -->
<LinearLayout
    android:id="@+id/calendarSyncSection"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp"
    android:background="@drawable/rounded_background_light_green"
    android:layout_marginTop="8dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical">
        
        <ImageView
            android:id="@+id/ivCalendarIcon"
            android:layout_width="24dp"
            android:layout_height="24dp"
            android:src="@drawable/ic_google_calendar"
            android:tint="#4285F4"/>
        
        <TextView
            android:id="@+id/tvCalendarSyncStatus"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginStart="12dp"
            android:text="@string/sync_with_google_calendar"
            android:textSize="14sp"
            android:textStyle="bold"
            android:textColor="#2E7D32"/>
        
        <com.google.android.material.switchmaterial.SwitchMaterial
            android:id="@+id/switchCalendarSync"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:checked="true"/>
    </LinearLayout>
    
    <LinearLayout
        android:id="@+id/layoutCalendarDetails"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:layout_marginTop="12dp"
        android:visibility="visible">
        
        <TextView
            android:id="@+id/tvCalendarEventInfo"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="12sp"
            android:textColor="#666666"
            android:lineSpacingExtra="4dp"/>
        
        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnViewInCalendar"
            android:layout_width="wrap_content"
            android:layout_height="36dp"
            android:layout_marginTop="8dp"
            android:text="@string/view_in_calendar"
            android:textSize="12sp"
            android:backgroundTint="#E8F5E9"
            android:textColor="#2E7D32"
            style="@style/Widget.MaterialComponents.Button.TextButton"
            app:icon="@drawable/ic_open_in_new"
            app:iconTint="#2E7D32"
            app:iconSize="16dp"/>
    </LinearLayout>
</LinearLayout>

<com.google.android.material.button.MaterialButton
    android:id="@+id/btnCalendarSyncSettings"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="8dp"
    android:text="@string/calendar_reminder_settings"
    android:textSize="14sp"
    style="@style/Widget.MaterialComponents.Button.OutlinedButton"/>
```

**2. Thêm vào CardDetailActivity.java:**

```java
public class CardDetailActivity extends AppCompatActivity {
    // Thêm các biến mới
    private SwitchMaterial switchCalendarSync;
    private MaterialButton btnCalendarSyncSettings;
    private MaterialButton btnViewInCalendar;
    private TextView tvCalendarEventInfo;
    private LinearLayout layoutCalendarDetails;
    
    private boolean isCalendarSyncEnabled = true;
    private List<Integer> reminderMinutes = Arrays.asList(15, 60, 1440);
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ... existing code ...
        
        setupCalendarSyncUI();
    }
    
    private void setupCalendarSyncUI() {
        switchCalendarSync = findViewById(R.id.switchCalendarSync);
        btnCalendarSyncSettings = findViewById(R.id.btnCalendarSyncSettings);
        btnViewInCalendar = findViewById(R.id.btnViewInCalendar);
        tvCalendarEventInfo = findViewById(R.id.tvCalendarEventInfo);
        layoutCalendarDetails = findViewById(R.id.layoutCalendarDetails);
        
        // Kiểm tra Google Calendar connection
        checkGoogleCalendarConnection();
        
        // Toggle calendar sync
        switchCalendarSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isCalendarSyncEnabled = isChecked;
            layoutCalendarDetails.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            
            if (isChecked && !isGoogleCalendarConnected()) {
                showConnectGoogleCalendarDialog();
            }
        });
        
        // Settings button
        btnCalendarSyncSettings.setOnClickListener(v -> {
            showReminderSettingsDialog();
        });
        
        // View in calendar button
        btnViewInCalendar.setOnClickListener(v -> {
            if (currentTask != null && currentTask.getCalendarEventId() != null) {
                openGoogleCalendarEvent(currentTask.getCalendarEventId());
            }
        });
    }
    
    private void checkGoogleCalendarConnection() {
        // TODO: Call API /api/google-auth/status
        ApiClient.getInstance()
            .create(GoogleAuthApiService.class)
            .getIntegrationStatus()
            .enqueue(new Callback<GoogleCalendarStatusResponse>() {
                @Override
                public void onResponse(Call<GoogleCalendarStatusResponse> call, 
                                     Response<GoogleCalendarStatusResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        updateCalendarSyncUI(response.body().isConnected());
                    }
                }
                
                @Override
                public void onFailure(Call<GoogleCalendarStatusResponse> call, Throwable t) {
                    Log.e(TAG, "Failed to check calendar connection", t);
                }
            });
    }
    
    private void updateCalendarSyncUI(boolean connected) {
        if (!connected) {
            switchCalendarSync.setEnabled(false);
            tvCalendarEventInfo.setText("⚠️ Chưa kết nối Google Calendar. Nhấn để kết nối.");
            tvCalendarEventInfo.setOnClickListener(v -> showConnectGoogleCalendarDialog());
        } else {
            switchCalendarSync.setEnabled(true);
            updateReminderInfoText();
        }
    }
    
    private void showConnectGoogleCalendarDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Kết nối Google Calendar")
            .setMessage("Để sử dụng tính năng đồng bộ lịch, bạn cần kết nối với Google Calendar.")
            .setPositiveButton("Kết nối ngay", (dialog, which) -> {
                startGoogleCalendarAuth();
            })
            .setNegativeButton("Để sau", null)
            .show();
    }
    
    private void startGoogleCalendarAuth() {
        // TODO: Call API /api/google-auth/auth-url
        // Mở WebView hoặc Chrome Custom Tab với URL nhận được
    }
    
    private void showReminderSettingsDialog() {
        CalendarReminderSettingsDialog dialog = new CalendarReminderSettingsDialog();
        dialog.setCurrentReminders(reminderMinutes);
        dialog.setOnSaveListener(newReminders -> {
            reminderMininders = newReminders;
            updateReminderInfoText();
        });
        dialog.show(getSupportFragmentManager(), "reminder_settings");
    }
    
    private void updateReminderInfoText() {
        StringBuilder info = new StringBuilder("📅 Sự kiện: " + etTaskTitle.getText() + " - Hạn chót\n");
        info.append("⏰ Nhắc nhở: ");
        
        List<String> reminderTexts = new ArrayList<>();
        for (int minutes : reminderMinutes) {
            if (minutes < 60) {
                reminderTexts.add(minutes + " phút");
            } else if (minutes < 1440) {
                reminderTexts.add((minutes / 60) + " giờ");
            } else {
                reminderTexts.add((minutes / 1440) + " ngày");
            }
        }
        info.append(String.join(", ", reminderTexts)).append(" trước");
        
        tvCalendarEventInfo.setText(info.toString());
    }
    
    private void openGoogleCalendarEvent(String eventId) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("content://com.android.calendar/events/" + eventId));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Fallback: web browser
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://calendar.google.com/calendar/event?eid=" + eventId));
            startActivity(intent);
        }
    }
    
    // Modify existing saveTask() method
    private void saveTask() {
        // ... existing validation ...
        
        Task task = new Task();
        // ... existing fields ...
        
        // Thêm calendar sync info
        task.setCalendarSyncEnabled(isCalendarSyncEnabled);
        task.setCalendarReminderMinutes(reminderMinutes);
        
        if (isEditMode) {
            updateTask(task);
        } else {
            createTask(task);
        }
    }
}
```

**3. Tạo CalendarReminderSettingsDialog.java:**

```java
package com.example.tralalero.feature.home.ui.Home.project;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public class CalendarReminderSettingsDialog extends DialogFragment {
    private CheckBox cbReminder15Min;
    private CheckBox cbReminder1Hour;
    private CheckBox cbReminder1Day;
    private CheckBox cbReminder1Week;
    
    private List<Integer> currentReminders = new ArrayList<>();
    private OnSaveListener onSaveListener;
    
    public interface OnSaveListener {
        void onSave(List<Integer> reminderMinutes);
    }
    
    public void setCurrentReminders(List<Integer> reminders) {
        this.currentReminders = reminders;
    }
    
    public void setOnSaveListener(OnSaveListener listener) {
        this.onSaveListener = listener;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
            .inflate(R.layout.dialog_calendar_reminder_settings, null);
        
        cbReminder15Min = view.findViewById(R.id.cbReminder15Min);
        cbReminder1Hour = view.findViewById(R.id.cbReminder1Hour);
        cbReminder1Day = view.findViewById(R.id.cbReminder1Day);
        cbReminder1Week = view.findViewById(R.id.cbReminder1Week);
        
        // Set current values
        cbReminder15Min.setChecked(currentReminders.contains(15));
        cbReminder1Hour.setChecked(currentReminders.contains(60));
        cbReminder1Day.setChecked(currentReminders.contains(1440));
        cbReminder1Week.setChecked(currentReminders.contains(10080));
        
        return new AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton("Lưu", (dialog, which) -> {
                List<Integer> newReminders = new ArrayList<>();
                if (cbReminder15Min.isChecked()) newReminders.add(15);
                if (cbReminder1Hour.isChecked()) newReminders.add(60);
                if (cbReminder1Day.isChecked()) newReminders.add(1440);
                if (cbReminder1Week.isChecked()) newReminders.add(10080);
                
                if (onSaveListener != null) {
                    onSaveListener.onSave(newReminders);
                }
            })
            .setNegativeButton("Hủy", null)
            .create();
    }
}
```

**Checklist Task 1.1:**
- [ ] Tạo layout dialog_calendar_reminder_settings.xml
- [ ] Tạo các drawable icons (ic_google_calendar, ic_sync, ic_open_in_new)
- [ ] Tạo rounded_background_light_green.xml
- [ ] Thêm strings vào strings.xml
- [ ] Chỉnh sửa card_detail.xml
- [ ] Chỉnh sửa CardDetailActivity.java
- [ ] Tạo CalendarReminderSettingsDialog.java
- [ ] Test UI hiển thị đúng
- [ ] Test switch toggle hoạt động

---

### **GIAI ĐOẠN 2: TAB CALENDAR (TUẦN 1-2 - 4 NGÀY)**

#### **Task 2.1: Tạo Calendar Fragment và Layout**

**Files cần tạo:**

```
📁 app/src/main/res/layout/
├── fragment_project_calendar.xml (TẠO MỚI)
└── item_calendar_event.xml (TẠO MỚI)

📁 app/src/main/java/com/example/tralalero/feature/home/ui/Home/project/
├── ProjectCalendarFragment.java (TẠO MỚI)
├── CalendarEventAdapter.java (TẠO MỚI)
├── ProjectCalendarViewModel.java (TẠO MỚI)
└── EventDecorator.java (TẠO MỚI)
```

**Thêm dependency vào build.gradle:**

```gradle
dependencies {
    // Material Calendar View
    implementation 'com.prolificinteractive:material-calendarview:1.4.3'
    
    // Existing dependencies...
}
```

**Chi tiết fragment_project_calendar.xml:** (Xem file GOOGLE_CALENDAR_FULL_IMPLEMENTATION.md)

**ProjectCalendarFragment.java:**

```java
package com.example.tralalero.feature.home.ui.Home.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.CalendarDay;

public class ProjectCalendarFragment extends Fragment {
    private MaterialCalendarView calendarView;
    private RecyclerView rvCalendarEvents;
    private CalendarEventAdapter eventAdapter;
    private ProgressBar progressBar;
    
    private ProjectCalendarViewModel viewModel;
    private String projectId;
    
    public static ProjectCalendarFragment newInstance(String projectId) {
        ProjectCalendarFragment fragment = new ProjectCalendarFragment();
        Bundle args = new Bundle();
        args.putString("project_id", projectId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_calendar, container, false);
        
        if (getArguments() != null) {
            projectId = getArguments().getString("project_id");
        }
        
        initViews(view);
        setupViewModel();
        setupCalendar();
        setupRecyclerView();
        setupButtons(view);
        loadCalendarData();
        
        return view;
    }
    
    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendarView);
        rvCalendarEvents = view.findViewById(R.id.rvCalendarEvents);
        progressBar = view.findViewById(R.id.progressBar);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProjectCalendarViewModel.class);
    }
    
    private void setupCalendar() {
        calendarView.setSelectedDate(CalendarDay.today());
        
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            loadEventsForDate(date);
        });
    }
    
    private void setupRecyclerView() {
        eventAdapter = new CalendarEventAdapter();
        rvCalendarEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCalendarEvents.setAdapter(eventAdapter);
        
        eventAdapter.setOnEventClickListener(event -> {
            if (event.getType() == CalendarEventType.TASK_DEADLINE) {
                openTaskDetail(event.getTaskId());
            }
        });
    }
    
    private void setupButtons(View view) {
        // View mode toggle
        MaterialButtonToggleGroup toggleViewMode = view.findViewById(R.id.toggleViewMode);
        toggleViewMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnWeekView) {
                    calendarView.state().edit()
                        .setCalendarDisplayMode(CalendarMode.WEEKS)
                        .commit();
                } else {
                    calendarView.state().edit()
                        .setCalendarDisplayMode(CalendarMode.MONTHS)
                        .commit();
                }
            }
        });
        
        // Sync button
        view.findViewById(R.id.btnSyncCalendar).setOnClickListener(v -> {
            syncWithGoogleCalendar();
        });
        
        // Filter button
        view.findViewById(R.id.btnFilter).setOnClickListener(v -> {
            showFilterDialog();
        });
        
        // Export button
        view.findViewById(R.id.btnExport).setOnClickListener(v -> {
            showExportDialog();
        });
    }
    
    private void loadCalendarData() {
        progressBar.setVisibility(View.VISIBLE);
        
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        
        // TODO: Call API /api/projects/{projectId}/calendar
        viewModel.loadProjectCalendarEvents(projectId, year, month)
            .observe(getViewLifecycleOwner(), result -> {
                progressBar.setVisibility(View.GONE);
                
                if (result.isSuccess()) {
                    updateCalendarDecorators(result.getData());
                    loadEventsForDate(CalendarDay.today());
                } else {
                    Toast.makeText(getContext(), 
                        "Lỗi: " + result.getErrorMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void loadEventsForDate(CalendarDay date) {
        viewModel.getEventsForDate(projectId, date)
            .observe(getViewLifecycleOwner(), events -> {
                eventAdapter.setEvents(events);
            });
    }
    
    private void syncWithGoogleCalendar() {
        progressBar.setVisibility(View.VISIBLE);
        
        // TODO: Call API /api/calendar/sync
        viewModel.syncWithGoogleCalendar(projectId)
            .observe(getViewLifecycleOwner(), result -> {
                progressBar.setVisibility(View.GONE);
                
                if (result.isSuccess()) {
                    Toast.makeText(getContext(), 
                        "✅ Đã đồng bộ thành công", 
                        Toast.LENGTH_SHORT).show();
                    loadCalendarData();
                }
            });
    }
    
    private void openTaskDetail(String taskId) {
        Intent intent = new Intent(getContext(), CardDetailActivity.class);
        intent.putExtra(CardDetailActivity.EXTRA_TASK_ID, taskId);
        startActivity(intent);
    }
}
```

**Checklist Task 2.1:**
- [ ] Thêm MaterialCalendarView dependency
- [ ] Tạo fragment_project_calendar.xml
- [ ] Tạo item_calendar_event.xml
- [ ] Tạo ProjectCalendarFragment.java
- [ ] Tạo CalendarEventAdapter.java
- [ ] Tạo EventDecorator.java
- [ ] Integrate vào ProjectActivity TabLayout
- [ ] Test calendar hiển thị đúng
- [ ] Test date selection

#### **Task 2.2: Tích hợp Calendar Tab vào ProjectActivity**

**Chỉnh sửa ProjectActivity.java:**

```java
public class ProjectActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager; // Hoặc sử dụng Fragment transaction
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ... existing code ...
        
        setupTabs();
    }
    
    private void setupTabs() {
        tabLayout = findViewById(R.id.tabLayout);
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0: // Board tab
                        showBoardView();
                        break;
                    case 1: // Calendar tab
                        showCalendarFragment();
                        break;
                    case 2: // Event tab
                        // Dev 2 sẽ handle
                        break;
                }
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void showBoardView() {
        boardsRecyclerView.setVisibility(View.VISIBLE);
        // Hide calendar fragment if showing
        Fragment calendarFragment = getSupportFragmentManager()
            .findFragmentByTag("calendar_fragment");
        if (calendarFragment != null) {
            getSupportFragmentManager().beginTransaction()
                .hide(calendarFragment)
                .commit();
        }
    }
    
    private void showCalendarFragment() {
        boardsRecyclerView.setVisibility(View.GONE);
        
        Fragment calendarFragment = getSupportFragmentManager()
            .findFragmentByTag("calendar_fragment");
        
        if (calendarFragment == null) {
            calendarFragment = ProjectCalendarFragment.newInstance(projectId);
            getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, calendarFragment, "calendar_fragment")
                .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                .show(calendarFragment)
                .commit();
        }
    }
}
```

**Chỉnh sửa project_main.xml:**

```xml
<!-- Thay thế boardsRecyclerView bằng FrameLayout -->
<FrameLayout
    android:id="@+id/fragmentContainer"
    android:layout_width="0dp"
    android:layout_height="0dp"
    app:layout_constraintTop_toBottomOf="@id/tabLayout"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent">
    
    <!-- Board RecyclerView -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/boardsRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:visibility="visible"/>
</FrameLayout>
```

**Checklist Task 2.2:**
- [ ] Chỉnh sửa project_main.xml
- [ ] Chỉnh sửa ProjectActivity.java
- [ ] Test tab switching
- [ ] Test calendar fragment lifecycle

---

### **GIAI ĐOẠN 3: API INTEGRATION (TUẦN 2 - 2 NGÀY)**

#### **Task 3.1: Tạo GoogleAuthApiService**

```java
package com.example.tralalero.data.remote.api;

import retrofit2.Call;
import retrofit2.http.*;

public interface GoogleAuthApiService {
    
    @GET("google-auth/status")
    Call<GoogleCalendarStatusResponse> getIntegrationStatus();
    
    @GET("google-auth/auth-url")
    Call<AuthUrlResponse> getAuthUrl();
    
    @POST("google-auth/disconnect")
    Call<Void> disconnect();
}
```

#### **Task 3.2: Tạo Response Models**

```java
package com.example.tralalero.domain.model;

public class GoogleCalendarStatusResponse {
    private boolean connected;
    private String userEmail;
    
    // Getters and setters
}

public class AuthUrlResponse {
    private String url;
    
    // Getters and setters
}

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
    
    // Getters and setters
}

public enum CalendarEventType {
    TASK_DEADLINE,
    MEETING,
    MILESTONE,
    OTHER
}
```

#### **Task 3.3: Update Task Model**

```java
public class Task {
    // Existing fields...
    
    // New fields for calendar sync
    private boolean calendarSyncEnabled;
    private List<Integer> calendarReminderMinutes;
    private String calendarEventId;
    private Date calendarSyncedAt;
    
    // Getters and setters
}
```

**Checklist Task 3:**
- [ ] Tạo GoogleAuthApiService.java
- [ ] Tạo response models
- [ ] Update Task model
- [ ] Tạo ViewModel cho calendar operations
- [ ] Test API calls

---

### **GIAI ĐOẠN 4: TESTING & POLISH (TUẦN 2 - 1 NGÀY)**

#### **Task 4.1: Testing**

**Test cases cần cover:**

1. **Calendar Sync UI:**
   - [ ] Switch bật/tắt hoạt động đúng
   - [ ] Settings dialog hiển thị đúng
   - [ ] Reminder preferences được lưu
   - [ ] View in Calendar button mở đúng app

2. **Calendar Tab:**
   - [ ] Calendar hiển thị đúng tháng hiện tại
   - [ ] Date selection hoạt động
   - [ ] Events hiển thị đúng cho ngày được chọn
   - [ ] Color decorators hiển thị đúng
   - [ ] View mode toggle (Tuần/Tháng)

3. **Integration:**
   - [ ] Tab switching mượt mà
   - [ ] Data persistence khi switch tabs
   - [ ] API calls hoạt động đúng
   - [ ] Error handling hiển thị đúng

#### **Task 4.2: UI Polish**

- [ ] Kiểm tra responsive trên các screen sizes
- [ ] Kiểm tra animations mượt mà
- [ ] Kiểm tra colors theo design system
- [ ] Kiểm tra accessibility (content descriptions)
- [ ] Kiểm tra loading states
- [ ] Kiểm tra empty states

---

## 📦 **DEPENDENCIES CẦN THIẾT**

Thêm vào `app/build.gradle`:

```gradle
dependencies {
    // Material Calendar View
    implementation 'com.prolificinteractive:material-calendarview:1.4.3'
    
    // Material Design (nếu chưa có)
    implementation 'com.google.android.material:material:1.9.0'
    
    // Lifecycle components (nếu chưa có)
    implementation 'androidx.lifecycle:lifecycle-viewmodel:2.6.1'
    implementation 'androidx.lifecycle:lifecycle-livedata:2.6.1'
}
```

---

## 🔗 **API ENDPOINTS CẦN DÙNG**

Backend sẽ cung cấp các endpoints sau:

```
GET  /api/google-auth/status           -> Kiểm tra connection
GET  /api/google-auth/auth-url         -> Lấy OAuth URL
POST /api/google-auth/disconnect       -> Ngắt kết nối

GET  /api/projects/{id}/calendar       -> Lấy calendar events
POST /api/calendar/sync                -> Đồng bộ thủ công

POST /api/tasks                        -> Tạo task (với calendar sync)
PUT  /api/tasks/{id}                   -> Cập nhật task
```

---

## 📝 **NOTES & TIPS**

### **Best Practices:**

1. **Separation of Concerns:**
   - Fragment chỉ handle UI logic
   - ViewModel handle business logic và API calls
   - Repository pattern cho data layer

2. **Error Handling:**
   - Luôn handle network errors
   - Show user-friendly messages
   - Log errors để debug

3. **Loading States:**
   - Show ProgressBar khi loading
   - Disable buttons khi đang process
   - Show Snackbar cho thành công/thất bại

4. **Memory Leaks:**
   - Unregister listeners trong onDestroy
   - Cancel API calls khi Fragment destroyed
   - Use ViewLifecycleOwner cho LiveData

### **Testing Tips:**

- Test với internet bật/tắt
- Test với Google Calendar đã kết nối/chưa kết nối
- Test với tasks có/không có due_date
- Test với nhiều events trong cùng ngày

---

## ✅ **DEFINITION OF DONE**

Công việc được coi là hoàn thành khi:

1. ✅ Calendar Sync UI hiển thị đúng trong CardDetailActivity
2. ✅ Settings dialog cho reminders hoạt động
3. ✅ Calendar Tab hiển thị events với decorators
4. ✅ Date selection và event list hoạt động
5. ✅ Tab switching mượt mà
6. ✅ Tất cả API calls hoạt động
7. ✅ Error handling đầy đủ
8. ✅ UI polish và responsive
9. ✅ Code được review và approve
10. ✅ Không có critical bugs

---

## 📞 **SUPPORT & COMMUNICATION**

- **Hỏi Backend Dev:** Về API endpoints, response format
- **Hỏi Frontend Dev 2:** Về shared models, interfaces (nếu có)
- **Daily standup:** Báo cáo tiến độ hàng ngày
- **Blocker:** Báo ngay khi gặp vấn đề không giải quyết được

---

**Chúc bạn code vui vẻ! 🚀**
