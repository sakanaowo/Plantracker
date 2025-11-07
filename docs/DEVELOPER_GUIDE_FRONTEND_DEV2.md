# 📱 HƯỚNG DẪN PHÁT TRIỂN - FRONTEND DEVELOPER 2

## 👤 **NHIỆM VỤ: EVENTS TAB & EVENT MANAGEMENT**

### 🎯 **TỔNG QUAN**

Bạn chịu trách nhiệm phát triển **Tab Events** - quản lý meetings và sự kiện trong project. Công việc của bạn **hoàn toàn độc lập** với Frontend Dev 1 (người phụ trách Calendar Tab và Calendar Sync UI).

---

## 📋 **DANH SÁCH CÔNG VIỆC**

### **GIAI ĐOẠN 1: EVENTS TAB LAYOUT (TUẦN 1 - 3 NGÀY)**

#### **Task 1.1: Tạo Events Fragment và Layout**

**Files cần tạo:**

```text
📁 app/src/main/res/
├── layout/
│   ├── fragment_project_events.xml (TẠO MỚI)
│   ├── item_project_event.xml (TẠO MỚI)
│   └── dialog_create_event.xml (TẠO MỚI)
├── drawable/
│   ├── ic_event.xml (TẠO MỚI)
│   ├── ic_videocam.xml (TẠO MỚI)
│   ├── ic_google_meet.xml (TẠO MỚI)
│   ├── ic_people.xml (TẠO MỚI)
│   ├── ic_timer.xml (TẠO MỚI)
│   └── rounded_background_gradient_blue.xml (TẠO MỚI)
├── menu/
│   └── menu_event_actions.xml (TẠO MỚI)
└── values/
    └── strings.xml (CHỈNH SỬA - thêm event strings)

📁 app/src/main/java/com/example/tralalero/feature/home/ui/Home/project/
├── ProjectEventsFragment.java (TẠO MỚI)
├── ProjectEventAdapter.java (TẠO MỚI)
├── CreateEventDialog.java (TẠO MỚI)
├── EventDetailDialog.java (TẠO MỚI)
└── ProjectEventsViewModel.java (TẠO MỚI)
```

**Chi tiết implementation:**

**1. fragment_project_events.xml:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#F6F6F6">
    
    <!-- Toolbar -->
    <com.google.android.material.card.MaterialCardView
        android:id="@+id/cardToolbar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintTop_toTopOf="parent"
        app:cardElevation="2dp">
        
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="12dp"
            android:gravity="center_vertical">
            
            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="@string/events_and_meetings"
                android:textSize="18sp"
                android:textStyle="bold"
                android:textColor="#212121"/>
            
            <ImageButton
                android:id="@+id/btnFilterEvents"
                android:layout_width="40dp"
                android:layout_height="40dp"
                android:src="@drawable/ic_filter"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:contentDescription="@string/filter"/>
            
            <ImageButton
                android:id="@+id/btnSearchEvents"
                android:layout_width="40dp"
                android:layout_height="40dp"
                android:layout_marginStart="8dp"
                android:src="@drawable/ic_search"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:contentDescription="@string/search"/>
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>
    
    <!-- Tabs -->
    <com.google.android.material.tabs.TabLayout
        android:id="@+id/tabLayoutEvents"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        app:layout_constraintTop_toBottomOf="@id/cardToolbar"
        app:tabMode="fixed"
        app:tabGravity="fill"
        app:tabIndicatorColor="#4CAF50">
        
        <com.google.android.material.tabs.TabItem
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/upcoming"/>
        
        <com.google.android.material.tabs.TabItem
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/past"/>
        
        <com.google.android.material.tabs.TabItem
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/recurring"/>
    </com.google.android.material.tabs.TabLayout>
    
    <!-- RecyclerView -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvEvents"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:layout_constraintTop_toBottomOf="@id/tabLayoutEvents"
        app:layout_constraintBottom_toBottomOf="parent"
        android:padding="12dp"
        android:clipToPadding="false"/>
    
    <!-- Empty state -->
    <LinearLayout
        android:id="@+id/layoutEmptyState"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:gravity="center"
        android:visibility="gone"
        app:layout_constraintTop_toTopOf="@id/rvEvents"
        app:layout_constraintBottom_toBottomOf="@id/rvEvents"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">
        
        <ImageView
            android:layout_width="120dp"
            android:layout_height="120dp"
            android:src="@drawable/ic_event_empty"
            android:alpha="0.3"
            android:contentDescription="@string/no_events"/>
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="@string/no_events_yet"
            android:textSize="16sp"
            android:textColor="#757575"/>
        
        <Button
            android:id="@+id/btnCreateFirstEvent"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="@string/create_first_event"
            android:backgroundTint="#4CAF50"/>
    </LinearLayout>
    
    <!-- Loading -->
    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:visibility="gone"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"/>
    
    <!-- FAB -->
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fabCreateEvent"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:src="@drawable/ic_add"
        app:backgroundTint="#4CAF50"
        android:contentDescription="@string/create_event"/>
</androidx.constraintlayout.widget.ConstraintLayout>
```

**2. item_project_event.xml:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginVertical="6dp"
    app:cardElevation="2dp"
    app:cardCornerRadius="12dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">
        
        <!-- Header -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="16dp"
            android:gravity="center_vertical">
            
            <!-- Date box -->
            <LinearLayout
                android:layout_width="60dp"
                android:layout_height="60dp"
                android:orientation="vertical"
                android:gravity="center"
                android:background="@drawable/rounded_background_gradient_blue">
                
                <TextView
                    android:id="@+id/tvEventDay"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="15"
                    android:textSize="24sp"
                    android:textStyle="bold"
                    android:textColor="#FFFFFF"/>
                
                <TextView
                    android:id="@+id/tvEventMonth"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="THG 11"
                    android:textSize="11sp"
                    android:textColor="#FFFFFF"/>
            </LinearLayout>
            
            <!-- Event details -->
            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginStart="16dp"
                android:orientation="vertical">
                
                <TextView
                    android:id="@+id/tvEventTitle"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="📋 Daily Standup"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="#212121"
                    android:maxLines="2"
                    android:ellipsize="end"/>
                
                <LinearLayout
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="6dp"
                    android:orientation="horizontal"
                    android:gravity="center_vertical">
                    
                    <ImageView
                        android:layout_width="14dp"
                        android:layout_height="14dp"
                        android:src="@drawable/ic_clock"
                        app:tint="#757575"
                        android:contentDescription="@string/time"/>
                    
                    <TextView
                        android:id="@+id/tvEventTime"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="6dp"
                        android:text="09:00 - 09:15"
                        android:textSize="13sp"
                        android:textColor="#757575"/>
                </LinearLayout>
                
                <LinearLayout
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp"
                    android:orientation="horizontal"
                    android:gravity="center_vertical">
                    
                    <ImageView
                        android:layout_width="14dp"
                        android:layout_height="14dp"
                        android:src="@drawable/ic_people"
                        app:tint="#757575"
                        android:contentDescription="@string/attendees"/>
                    
                    <TextView
                        android:id="@+id/tvEventAttendees"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="6dp"
                        android:text="5 người"
                        android:textSize="13sp"
                        android:textColor="#757575"/>
                </LinearLayout>
            </LinearLayout>
            
            <!-- Menu button -->
            <ImageButton
                android:id="@+id/btnEventMenu"
                android:layout_width="40dp"
                android:layout_height="40dp"
                android:src="@drawable/ic_more_vert"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:contentDescription="@string/menu"/>
        </LinearLayout>
        
        <!-- Expandable actions section -->
        <LinearLayout
            android:id="@+id/layoutEventActions"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingHorizontal="16dp"
            android:paddingBottom="12dp"
            android:visibility="gone">
            
            <View
                android:layout_width="match_parent"
                android:layout_height="1dp"
                android:background="#E0E0E0"
                android:layout_marginBottom="12dp"/>
            
            <!-- Google Meet link -->
            <LinearLayout
                android:id="@+id/layoutMeetLink"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:padding="8dp"
                android:background="@drawable/rounded_background_light_blue"
                android:gravity="center_vertical"
                android:visibility="gone">
                
                <ImageView
                    android:layout_width="24dp"
                    android:layout_height="24dp"
                    android:src="@drawable/ic_google_meet"
                    android:contentDescription="@string/google_meet"/>
                
                <TextView
                    android:id="@+id/tvMeetLink"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:layout_marginStart="12dp"
                    android:textSize="13sp"
                    android:textColor="#1976D2"/>
                
                <ImageButton
                    android:id="@+id/btnCopyMeetLink"
                    android:layout_width="32dp"
                    android:layout_height="32dp"
                    android:src="@drawable/ic_copy"
                    android:background="?attr/selectableItemBackgroundBorderless"
                    app:tint="#1976D2"
                    android:contentDescription="@string/copy_link"/>
            </LinearLayout>
            
            <!-- Action buttons -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="12dp"
                android:orientation="horizontal">
                
                <Button
                    android:id="@+id/btnJoinMeeting"
                    android:layout_width="0dp"
                    android:layout_height="40dp"
                    android:layout_weight="1"
                    android:text="@string/join"
                    android:textSize="13sp"
                    android:backgroundTint="#4CAF50"
                    app:icon="@drawable/ic_videocam"
                    app:iconSize="18dp"
                    android:visibility="gone"/>
                
                <Button
                    android:id="@+id/btnViewDetails"
                    android:layout_width="0dp"
                    android:layout_height="40dp"
                    android:layout_weight="1"
                    android:layout_marginStart="8dp"
                    android:text="@string/details"
                    android:textSize="13sp"
                    style="@style/Widget.MaterialComponents.Button.OutlinedButton"/>
            </LinearLayout>
        </LinearLayout>
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

**3. ProjectEventsFragment.java:**

```java
package com.example.tralalero.feature.home.ui.Home.project;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProjectEventsFragment extends Fragment {
    private RecyclerView rvEvents;
    private ProjectEventAdapter eventAdapter;
    private TabLayout tabLayoutEvents;
    private View layoutEmptyState;
    private ProgressBar progressBar;
    
    private ProjectEventsViewModel viewModel;
    private String projectId;
    private EventFilter currentFilter = EventFilter.UPCOMING;
    
    public enum EventFilter {
        UPCOMING, PAST, RECURRING
    }
    
    public static ProjectEventsFragment newInstance(String projectId) {
        ProjectEventsFragment fragment = new ProjectEventsFragment();
        Bundle args = new Bundle();
        args.putString("project_id", projectId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString("project_id");
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_events, container, false);
        
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupTabs();
        setupButtons(view);
        loadEvents();
        
        return view;
    }
    
    private void initViews(View view) {
        rvEvents = view.findViewById(R.id.rvEvents);
        tabLayoutEvents = view.findViewById(R.id.tabLayoutEvents);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        progressBar = view.findViewById(R.id.progressBar);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProjectEventsViewModel.class);
    }
    
    private void setupRecyclerView() {
        eventAdapter = new ProjectEventAdapter();
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEvents.setAdapter(eventAdapter);
        
        // Event click
        eventAdapter.setOnEventClickListener(event -> {
            showEventDetails(event);
        });
        
        // Join meeting click
        eventAdapter.setOnJoinMeetingClickListener(event -> {
            if (event.getMeetLink() != null && !event.getMeetLink().isEmpty()) {
                joinGoogleMeet(event.getMeetLink());
            }
        });
        
        // Copy link click
        eventAdapter.setOnCopyLinkClickListener(event -> {
            if (event.getMeetLink() != null) {
                copyMeetLinkToClipboard(event.getMeetLink());
            }
        });
        
        // Menu click
        eventAdapter.setOnMenuClickListener((event, view) -> {
            showEventMenu(event, view);
        });
    }
    
    private void setupTabs() {
        tabLayoutEvents.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        currentFilter = EventFilter.UPCOMING;
                        break;
                    case 1:
                        currentFilter = EventFilter.PAST;
                        break;
                    case 2:
                        currentFilter = EventFilter.RECURRING;
                        break;
                }
                loadEvents();
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void setupButtons(View view) {
        // FAB
        FloatingActionButton fabCreateEvent = view.findViewById(R.id.fabCreateEvent);
        fabCreateEvent.setOnClickListener(v -> showCreateEventDialog());
        
        // Empty state button
        view.findViewById(R.id.btnCreateFirstEvent).setOnClickListener(v -> {
            showCreateEventDialog();
        });
        
        // Filter button
        view.findViewById(R.id.btnFilterEvents).setOnClickListener(v -> {
            showFilterDialog();
        });
        
        // Search button
        view.findViewById(R.id.btnSearchEvents).setOnClickListener(v -> {
            showSearchDialog();
        });
    }
    
    private void loadEvents() {
        progressBar.setVisibility(View.VISIBLE);
        
        // TODO: Call API /api/projects/{projectId}/events
        viewModel.loadProjectEvents(projectId, currentFilter)
            .observe(getViewLifecycleOwner(), result -> {
                progressBar.setVisibility(View.GONE);
                
                if (result.isSuccess()) {
                    List<ProjectEvent> events = result.getData();
                    
                    if (events != null && !events.isEmpty()) {
                        eventAdapter.setEvents(events);
                        rvEvents.setVisibility(View.VISIBLE);
                        layoutEmptyState.setVisibility(View.GONE);
                    } else {
                        rvEvents.setVisibility(View.GONE);
                        layoutEmptyState.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(), 
                        "Lỗi: " + result.getErrorMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void showCreateEventDialog() {
        CreateEventDialog dialog = CreateEventDialog.newInstance(projectId);
        dialog.setOnEventCreatedListener(event -> {
            Toast.makeText(getContext(), 
                "✅ Đã tạo sự kiện: " + event.getTitle(), 
                Toast.LENGTH_SHORT).show();
            loadEvents();
            
            if (event.getMeetLink() != null) {
                showMeetCreatedSnackbar(event);
            }
        });
        dialog.show(getChildFragmentManager(), "create_event");
    }
    
    private void showMeetCreatedSnackbar(ProjectEvent event) {
        Snackbar.make(getView(),
            "📹 Google Meet đã được tạo",
            Snackbar.LENGTH_LONG)
            .setAction("Sao chép link", v -> {
                copyMeetLinkToClipboard(event.getMeetLink());
            })
            .setActionTextColor(Color.parseColor("#4CAF50"))
            .show();
    }
    
    private void joinGoogleMeet(String meetLink) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(meetLink));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), 
                "Không thể mở link họp", 
                Toast.LENGTH_SHORT).show();
        }
    }
    
    private void copyMeetLinkToClipboard(String meetLink) {
        ClipboardManager clipboard = (ClipboardManager) 
            getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Google Meet Link", meetLink);
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(getContext(), 
            "✅ Đã sao chép link", 
            Toast.LENGTH_SHORT).show();
    }
    
    private void showEventDetails(ProjectEvent event) {
        EventDetailDialog dialog = EventDetailDialog.newInstance(event);
        dialog.show(getChildFragmentManager(), "event_detail");
    }
    
    private void showEventMenu(ProjectEvent event, View anchorView) {
        PopupMenu popup = new PopupMenu(getContext(), anchorView);
        popup.inflate(R.menu.menu_event_actions);
        
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    editEvent(event);
                    return true;
                case R.id.action_cancel:
                    cancelEvent(event);
                    return true;
                case R.id.action_send_reminder:
                    sendReminder(event);
                    return true;
                default:
                    return false;
            }
        });
        
        popup.show();
    }
    
    private void editEvent(ProjectEvent event) {
        // TODO: Mở edit dialog
    }
    
    private void cancelEvent(ProjectEvent event) {
        new AlertDialog.Builder(getContext())
            .setTitle("Hủy sự kiện")
            .setMessage("Bạn có chắc muốn hủy sự kiện này?")
            .setPositiveButton("Hủy sự kiện", (dialog, which) -> {
                // TODO: Call API delete event
            })
            .setNegativeButton("Không", null)
            .show();
    }
    
    private void sendReminder(ProjectEvent event) {
        // TODO: Call API send reminder
        Toast.makeText(getContext(), 
            "Đã gửi nhắc nhở tới người tham gia", 
            Toast.LENGTH_SHORT).show();
    }
    
    private void showFilterDialog() {
        // TODO: Implement filter dialog
    }
    
    private void showSearchDialog() {
        // TODO: Implement search
    }
}
```

**Checklist Task 1.1:**
- [ ] Tạo fragment_project_events.xml
- [ ] Tạo item_project_event.xml
- [ ] Tạo các drawable icons
- [ ] Tạo menu_event_actions.xml
- [ ] Thêm strings vào strings.xml
- [ ] Tạo ProjectEventsFragment.java
- [ ] Test UI hiển thị đúng
- [ ] Test tabs switching

---

### **GIAI ĐOẠN 2: CREATE EVENT DIALOG (TUẦN 1 - 2 NGÀY)**

#### **Task 2.1: Tạo Create Event Dialog**

**dialog_create_event.xml:** (Xem file GOOGLE_CALENDAR_FULL_IMPLEMENTATION.md line 820-1000)

**CreateEventDialog.java:**

```java
package com.example.tralalero.feature.home.ui.Home.project;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.*;

public class CreateEventDialog extends DialogFragment {
    private TextInputEditText etEventTitle;
    private TextInputEditText etEventDate;
    private TextInputEditText etEventTime;
    private TextInputEditText etDuration;
    private TextInputEditText etEventDescription;
    private RadioGroup rgEventType;
    private ChipGroup chipGroupAttendees;
    private SwitchMaterial switchCreateMeet;
    private Spinner spinnerRecurrence;
    
    private String projectId;
    private List<String> selectedAttendeeIds = new ArrayList<>();
    private OnEventCreatedListener listener;
    
    public interface OnEventCreatedListener {
        void onEventCreated(ProjectEvent event);
    }
    
    public static CreateEventDialog newInstance(String projectId) {
        CreateEventDialog dialog = new CreateEventDialog();
        Bundle args = new Bundle();
        args.putString("project_id", projectId);
        dialog.setArguments(args);
        return dialog;
    }
    
    public void setOnEventCreatedListener(OnEventCreatedListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
        
        if (getArguments() != null) {
            projectId = getArguments().getString("project_id");
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_event, container, false);
        
        initViews(view);
        setupDateTimePickers();
        setupAttendees();
        setupRecurrence();
        setupButtons(view);
        
        return view;
    }
    
    private void initViews(View view) {
        etEventTitle = view.findViewById(R.id.etEventTitle);
        etEventDate = view.findViewById(R.id.etEventDate);
        etEventTime = view.findViewById(R.id.etEventTime);
        etDuration = view.findViewById(R.id.etDuration);
        etEventDescription = view.findViewById(R.id.etEventDescription);
        rgEventType = view.findViewById(R.id.rgEventType);
        chipGroupAttendees = view.findViewById(R.id.chipGroupAttendees);
        switchCreateMeet = view.findViewById(R.id.switchCreateMeet);
        spinnerRecurrence = view.findViewById(R.id.spinnerRecurrence);
    }
    
    private void setupDateTimePickers() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        // Date picker
        etEventDate.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    etEventDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });
        
        // Time picker
        etEventTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    etEventTime.setText(timeFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            );
            timePicker.show();
        });
    }
    
    private void setupAttendees() {
        view.findViewById(R.id.btnAddAttendees).setOnClickListener(v -> {
            showSelectAttendeesDialog();
        });
    }
    
    private void showSelectAttendeesDialog() {
        // TODO: Load project members và show selection dialog
        // Khi chọn xong, thêm chips vào chipGroupAttendees
    }
    
    private void addAttendeeChip(String userId, String userName) {
        Chip chip = new Chip(getContext());
        chip.setText(userName);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroupAttendees.removeView(chip);
            selectedAttendeeIds.remove(userId);
        });
        chipGroupAttendees.addView(chip);
        selectedAttendeeIds.add(userId);
    }
    
    private void setupRecurrence() {
        String[] recurrenceOptions = {
            "Không lặp lại",
            "Hàng ngày",
            "Hàng tuần",
            "Hai tuần một lần",
            "Hàng tháng"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            getContext(),
            android.R.layout.simple_spinner_item,
            recurrenceOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecurrence.setAdapter(adapter);
    }
    
    private void setupButtons(View view) {
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
        
        view.findViewById(R.id.btnCreateEvent).setOnClickListener(v -> {
            if (validateInput()) {
                createEvent();
            }
        });
    }
    
    private boolean validateInput() {
        if (etEventTitle.getText().toString().trim().isEmpty()) {
            etEventTitle.setError("Vui lòng nhập tiêu đề");
            return false;
        }
        
        if (etEventDate.getText().toString().trim().isEmpty()) {
            etEventDate.setError("Vui lòng chọn ngày");
            return false;
        }
        
        if (etEventTime.getText().toString().trim().isEmpty()) {
            etEventTime.setError("Vui lòng chọn giờ");
            return false;
        }
        
        return true;
    }
    
    private void createEvent() {
        // TODO: Build event object và call API
        ProjectEvent event = new ProjectEvent();
        event.setTitle(etEventTitle.getText().toString());
        event.setDate(parseDate(etEventDate.getText().toString()));
        event.setTime(etEventTime.getText().toString());
        event.setDuration(Integer.parseInt(etDuration.getText().toString()));
        event.setDescription(etEventDescription.getText().toString());
        event.setType(getSelectedEventType());
        event.setAttendeeIds(selectedAttendeeIds);
        event.setCreateGoogleMeet(switchCreateMeet.isChecked());
        event.setRecurrence(getSelectedRecurrence());
        
        // Call API
        // ...
        
        if (listener != null) {
            listener.onEventCreated(event);
        }
        
        dismiss();
    }
    
    private String getSelectedEventType() {
        int selectedId = rgEventType.getCheckedRadioButtonId();
        if (selectedId == R.id.rbMeeting) return "MEETING";
        if (selectedId == R.id.rbMilestone) return "MILESTONE";
        return "OTHER";
    }
    
    private String getSelectedRecurrence() {
        int position = spinnerRecurrence.getSelectedItemPosition();
        switch (position) {
            case 1: return "DAILY";
            case 2: return "WEEKLY";
            case 3: return "BIWEEKLY";
            case 4: return "MONTHLY";
            default: return "NONE";
        }
    }
}
```

**Checklist Task 2.1:**
- [ ] Tạo dialog_create_event.xml
- [ ] Tạo CreateEventDialog.java
- [ ] Implement date/time pickers
- [ ] Implement attendee selection
- [ ] Implement recurrence options
- [ ] Test form validation
- [ ] Test dialog hiển thị và dismiss

---

### **GIAI ĐOẠN 3: ADAPTER & VIEWMODEL (TUẦN 2 - 2 NGÀY)**

#### **Task 3.1: Tạo ProjectEventAdapter**

```java
package com.example.tralalero.feature.home.ui.Home.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.*;

public class ProjectEventAdapter extends RecyclerView.Adapter<ProjectEventAdapter.ViewHolder> {
    private List<ProjectEvent> events = new ArrayList<>();
    private OnEventClickListener onEventClickListener;
    private OnJoinMeetingClickListener onJoinMeetingClickListener;
    private OnCopyLinkClickListener onCopyLinkClickListener;
    private OnMenuClickListener onMenuClickListener;
    
    public interface OnEventClickListener {
        void onEventClick(ProjectEvent event);
    }
    
    public interface OnJoinMeetingClickListener {
        void onJoinMeeting(ProjectEvent event);
    }
    
    public interface OnCopyLinkClickListener {
        void onCopyLink(ProjectEvent event);
    }
    
    public interface OnMenuClickListener {
        void onMenuClick(ProjectEvent event, View anchorView);
    }
    
    public void setEvents(List<ProjectEvent> events) {
        this.events = events;
        notifyDataSetChanged();
    }
    
    public void setOnEventClickListener(OnEventClickListener listener) {
        this.onEventClickListener = listener;
    }
    
    public void setOnJoinMeetingClickListener(OnJoinMeetingClickListener listener) {
        this.onJoinMeetingClickListener = listener;
    }
    
    public void setOnCopyLinkClickListener(OnCopyLinkClickListener listener) {
        this.onCopyLinkClickListener = listener;
    }
    
    public void setOnMenuClickListener(OnMenuClickListener listener) {
        this.onMenuClickListener = listener;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_project_event, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ProjectEvent event = events.get(position);
        holder.bind(event);
    }
    
    @Override
    public int getItemCount() {
        return events.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventDay;
        TextView tvEventMonth;
        TextView tvEventTitle;
        TextView tvEventTime;
        TextView tvEventAttendees;
        TextView tvMeetLink;
        LinearLayout layoutEventActions;
        LinearLayout layoutMeetLink;
        Button btnJoinMeeting;
        Button btnViewDetails;
        ImageButton btnCopyMeetLink;
        ImageButton btnEventMenu;
        
        private boolean isExpanded = false;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvEventDay = itemView.findViewById(R.id.tvEventDay);
            tvEventMonth = itemView.findViewById(R.id.tvEventMonth);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventTime = itemView.findViewById(R.id.tvEventTime);
            tvEventAttendees = itemView.findViewById(R.id.tvEventAttendees);
            tvMeetLink = itemView.findViewById(R.id.tvMeetLink);
            layoutEventActions = itemView.findViewById(R.id.layoutEventActions);
            layoutMeetLink = itemView.findViewById(R.id.layoutMeetLink);
            btnJoinMeeting = itemView.findViewById(R.id.btnJoinMeeting);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnCopyMeetLink = itemView.findViewById(R.id.btnCopyMeetLink);
            btnEventMenu = itemView.findViewById(R.id.btnEventMenu);
        }
        
        void bind(ProjectEvent event) {
            // Format date
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(event.getDate());
            tvEventDay.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
            tvEventMonth.setText(new SimpleDateFormat("MMM", Locale.getDefault())
                .format(event.getDate()).toUpperCase());
            
            // Title with icon
            String icon = getEventIcon(event.getType());
            tvEventTitle.setText(icon + " " + event.getTitle());
            
            // Time
            tvEventTime.setText(event.getTime() + " (" + event.getDuration() + " phút)");
            
            // Attendees
            tvEventAttendees.setText(event.getAttendeeCount() + " người tham gia");
            
            // Google Meet link
            if (event.getMeetLink() != null && !event.getMeetLink().isEmpty()) {
                layoutMeetLink.setVisibility(View.VISIBLE);
                btnJoinMeeting.setVisibility(View.VISIBLE);
                tvMeetLink.setText(event.getMeetLink());
            } else {
                layoutMeetLink.setVisibility(View.GONE);
                btnJoinMeeting.setVisibility(View.GONE);
            }
            
            // Click to expand/collapse
            itemView.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                layoutEventActions.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            });
            
            // Join meeting
            btnJoinMeeting.setOnClickListener(v -> {
                if (onJoinMeetingClickListener != null) {
                    onJoinMeetingClickListener.onJoinMeeting(event);
                }
            });
            
            // View details
            btnViewDetails.setOnClickListener(v -> {
                if (onEventClickListener != null) {
                    onEventClickListener.onEventClick(event);
                }
            });
            
            // Copy link
            btnCopyMeetLink.setOnClickListener(v -> {
                if (onCopyLinkClickListener != null) {
                    onCopyLinkClickListener.onCopyLink(event);
                }
            });
            
            // Menu
            btnEventMenu.setOnClickListener(v -> {
                if (onMenuClickListener != null) {
                    onMenuClickListener.onMenuClick(event, v);
                }
            });
        }
        
        private String getEventIcon(String type) {
            switch (type) {
                case "MEETING": return "📋";
                case "MILESTONE": return "🎯";
                default: return "📌";
            }
        }
    }
}
```

**Checklist Task 3:**
- [ ] Tạo ProjectEventAdapter.java
- [ ] Tạo ProjectEventsViewModel.java
- [ ] Implement click listeners
- [ ] Test expand/collapse animations
- [ ] Test adapter data updates

---

### **GIAI ĐOẠN 4: API INTEGRATION & MODELS (TUẦN 2 - 2 NGÀY)**

#### **Task 4.1: Tạo EventApiService**

```java
package com.example.tralalero.data.remote.api;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface EventApiService {
    
    @GET("projects/{projectId}/events")
    Call<List<ProjectEvent>> getProjectEvents(
        @Path("projectId") String projectId,
        @Query("filter") String filter  // UPCOMING, PAST, RECURRING
    );
    
    @GET("events/{id}")
    Call<ProjectEvent> getEventById(@Path("id") String eventId);
    
    @POST("events")
    Call<ProjectEvent> createEvent(@Body CreateEventRequest request);
    
    @PUT("events/{id}")
    Call<ProjectEvent> updateEvent(
        @Path("id") String eventId,
        @Body UpdateEventRequest request
    );
    
    @DELETE("events/{id}")
    Call<Void> deleteEvent(@Path("id") String eventId);
    
    @POST("events/{id}/send-reminder")
    Call<Void> sendReminder(@Path("id") String eventId);
}
```

#### **Task 4.2: Tạo Models**

```java
package com.example.tralalero.domain.model;

import java.util.Date;
import java.util.List;

public class ProjectEvent {
    private String id;
    private String projectId;
    private String title;
    private String description;
    private Date date;
    private String time;
    private int duration;
    private String type; // MEETING, MILESTONE, OTHER
    private String recurrence; // NONE, DAILY, WEEKLY, BIWEEKLY, MONTHLY
    private List<String> attendeeIds;
    private int attendeeCount;
    private String meetLink;
    private boolean createGoogleMeet;
    private String calendarEventId;
    private Date createdAt;
    private String createdBy;
    
    // Getters and setters
}

public class CreateEventRequest {
    private String projectId;
    private String title;
    private String description;
    private String date;
    private String time;
    private int duration;
    private String type;
    private String recurrence;
    private List<String> attendeeIds;
    private boolean createGoogleMeet;
    
    // Getters and setters
}
```

**Checklist Task 4:**
- [ ] Tạo EventApiService.java
- [ ] Tạo ProjectEvent model
- [ ] Tạo CreateEventRequest/Response models
- [ ] Tạo Repository pattern
- [ ] Test API calls

---

### **GIAI ĐOẠN 5: TÍCH HỢP VÀO PROJECT ACTIVITY (TUẦN 2 - 1 NGÀY)**

**Chỉnh sửa ProjectActivity.java:**

```java
private void setupTabs() {
    tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            int position = tab.getPosition();
            switch (position) {
                case 0: // Board
                    showBoardView();
                    break;
                case 1: // Calendar - Dev 1
                    // Dev 1 handles this
                    break;
                case 2: // Event - Dev 2
                    showEventsFragment();
                    break;
            }
        }
        
        @Override
        public void onTabUnselected(TabLayout.Tab tab) {}
        
        @Override
        public void onTabReselected(TabLayout.Tab tab) {}
    });
}

private void showEventsFragment() {
    boardsRecyclerView.setVisibility(View.GONE);
    
    Fragment eventsFragment = getSupportFragmentManager()
        .findFragmentByTag("events_fragment");
    
    if (eventsFragment == null) {
        eventsFragment = ProjectEventsFragment.newInstance(projectId);
        getSupportFragmentManager().beginTransaction()
            .add(R.id.fragmentContainer, eventsFragment, "events_fragment")
            .commit();
    } else {
        getSupportFragmentManager().beginTransaction()
            .show(eventsFragment)
            .commit();
    }
}
```

**Checklist:**
- [ ] Integrate vào ProjectActivity
- [ ] Test tab switching
- [ ] Test fragment lifecycle
- [ ] Test data persistence

---

## 📦 **DEPENDENCIES**

```gradle
dependencies {
    // Material Design
    implementation 'com.google.android.material:material:1.9.0'
    
    // Lifecycle
    implementation 'androidx.lifecycle:lifecycle-viewmodel:2.6.1'
    implementation 'androidx.lifecycle:lifecycle-livedata:2.6.1'
}
```

---

## 🔗 **API ENDPOINTS**

```text
GET  /api/projects/{id}/events          -> Lấy events của project
GET  /api/events/{id}                   -> Chi tiết event
POST /api/events                        -> Tạo event mới
PUT  /api/events/{id}                   -> Cập nhật event
DELETE /api/events/{id}                 -> Xóa event
POST /api/events/{id}/send-reminder     -> Gửi reminder
```

---

## ✅ **DEFINITION OF DONE**

1. ✅ Events Tab hiển thị danh sách events
2. ✅ Tabs (Upcoming/Past/Recurring) hoạt động
3. ✅ Create Event Dialog hoàn chỉnh
4. ✅ Google Meet integration hiển thị
5. ✅ Event menu actions hoạt động
6. ✅ Expand/collapse event details
7. ✅ API integration hoàn chỉnh
8. ✅ Error handling đầy đủ
9. ✅ Code review và approve
10. ✅ Không có critical bugs

---

## 📞 **SUPPORT**

- **Hỏi Backend Dev:** Về API endpoints
- **Hỏi Frontend Dev 1:** Về shared models (nếu cần)
- **Daily standup:** Báo cáo tiến độ

---

**Chúc bạn code vui vẻ! 🚀**
