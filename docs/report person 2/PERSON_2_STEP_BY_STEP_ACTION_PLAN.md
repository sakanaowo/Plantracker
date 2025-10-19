# 👤 NGƯỜI 2 - KẾ HOẠCH HÀNH ĐỘNG CHI TIẾT

**Vai trò:** Repository & UI Integration Specialist  
**Mục tiêu:** Implement cached repositories và migrate UI sang sử dụng cache  
**Timeline:** 5-7 ngày làm việc  
**Điều kiện tiên quyết:** Người 1 đã hoàn thành database infrastructure

---

## 📅 NGÀY 1: REVIEW VÀ SETUP (4-5 giờ)

### ⏰ Buổi Sáng (2-3 giờ)

#### Bước 1.1: Kiểm tra Prerequisites từ Người 1

**Thời gian:** 30 phút

- [ ] Mở Android Studio
- [ ] Sync Gradle project
- [ ] Build project thành công
- [ ] Kiểm tra các file của Người 1:
  - [ ] `AppDatabase.java` tồn tại
  - [ ] `TaskDao.java` tồn tại
  - [ ] `ProjectDao.java` tồn tại
  - [ ] `TaskEntity.java` tồn tại
  - [ ] `TaskEntityMapper.java` tồn tại
  - [ ] `DependencyProvider.java` tồn tại
- [ ] Run app để đảm bảo không crash
- [ ] Check logcat xem DependencyProvider đã khởi tạo chưa

**Kết quả mong đợi:**

- Project build thành công
- App chạy được
- Không có compile errors

---

#### Bước 1.2: Review TaskRepositoryImplWithCache

**Thời gian:** 1-1.5 giờ

- [ ] Mở file `TaskRepositoryImplWithCache.java`
- [ ] Đọc hiểu cấu trúc class:
  - [ ] Constructor nhận gì?
  - [ ] ExecutorService dùng để làm gì?
  - [ ] Handler dùng để làm gì?
- [ ] Hiểu flow của phương thức `getTasksByBoard()`:
  - [ ] Bước 1: Check cache
  - [ ] Bước 2: Return cached data ngay lập tức
  - [ ] Bước 3: Fetch from network ở background
  - [ ] Bước 4: Update cache silently
- [ ] Review các phương thức khác:
  - [ ] `getTaskById()`
  - [ ] `createTask()`
  - [ ] `updateTask()`
  - [ ] `deleteTask()`
- [ ] Hiểu cơ chế shutdown() để tránh memory leak
- [ ] Note lại pattern để áp dụng cho ProjectRepository

**Kết quả mong đợi:**

- Hiểu rõ caching strategy
- Biết cách repository hoạt động
- Sẵn sàng implement ProjectRepository theo pattern tương tự

---

#### Bước 1.3: Verify DependencyProvider hoạt động

**Thời gian:** 30 phút

- [ ] Tìm file `App.java`
- [ ] Kiểm tra DependencyProvider đã được khởi tạo trong `onCreate()`
- [ ] Tạo một Activity test đơn giản hoặc dùng Activity có sẵn
- [ ] Thêm code test trong `onCreate()` của Activity:
  - [ ] Get TaskRepositoryWithCache
  - [ ] Get Database instance
  - [ ] Get TaskDao
  - [ ] Log ra xem có null không
- [ ] Run app và check logcat
- [ ] Verify tất cả objects đều khởi tạo thành công

**Kết quả mong đợi:**

- DependencyProvider.getInstance() trả về instance
- getTaskRepositoryWithCache() không null
- getDatabase() không null
- Không có crashes

---

### ⏰ Buổi Chiều (2 giờ)

#### Bước 1.4: Test Repository với data thật

**Thời gian:** 1.5 giờ

- [ ] Chọn một Board ID có sẵn trong app (hoặc tạo board mới)
- [ ] Tạo test method trong Activity:
  - [ ] Call `getTasksByBoard()` lần đầu
  - [ ] Log thời gian và số lượng tasks
  - [ ] Đợi 2-3 giây
  - [ ] Call `getTasksByBoard()` lần thứ 2
  - [ ] So sánh thời gian 2 lần call
- [ ] Bật airplane mode và test lại:
  - [ ] App vẫn load được tasks từ cache
  - [ ] Không crash
- [ ] Test create/update/delete tasks

**Kết quả mong đợi:**

- Lần 1: ~500-2000ms (from network)
- Lần 2: <50ms (from cache)
- Offline mode: Vẫn load được cached data
- CRUD operations hoạt động bình thường

---

#### Bước 1.5: Document hiểu biết về architecture

**Thời gian:** 30 phút

- [ ] Tạo note cho bản thân về:
  - [ ] Cache-first strategy là gì
  - [ ] Tại sao dùng ExecutorService
  - [ ] Tại sao cần Handler.post() cho callbacks
  - [ ] Flow diagram: User action → Repository → Cache/Network
- [ ] List ra các Activities cần migrate
- [ ] Estimate effort cho từng activity

**Kết quả mong đợi:**

- Có document cá nhân về architecture
- Có danh sách activities cần migrate
- Có plan rõ ràng cho các ngày tiếp theo

---

## 📅 NGÀY 2: IMPLEMENT PROJECT REPOSITORY (6-8 giờ)

### ⏰ Buổi Sáng (3-4 giờ)

#### Bước 2.1: Tạo ProjectRepositoryImplWithCache

**Thời gian:** 2-3 giờ

- [ ] Tạo file mới: `ProjectRepositoryImplWithCache.java`
- [ ] Copy cấu trúc từ `TaskRepositoryImplWithCache.java`
- [ ] Thay đổi:
  - [ ] TaskDao → ProjectDao
  - [ ] TaskEntity → ProjectEntity
  - [ ] TaskDTO → ProjectDTO
  - [ ] TaskMapper → ProjectMapper
  - [ ] TaskEntityMapper → ProjectEntityMapper
- [ ] Implement các methods theo interface `IProjectRepository`:
  - [ ] `getProjectById()`
  - [ ] `getProjectsByWorkspace()` (quan trọng!)
  - [ ] `createProject()`
  - [ ] `updateProject()`
  - [ ] `deleteProject()`
- [ ] Apply pattern giống TaskRepository cho mỗi method:
  - [ ] Input validation
  - [ ] Null checks
  - [ ] Try-catch blocks
  - [ ] ExecutorService cho background work
  - [ ] Handler.post() cho callbacks
- [ ] Thêm method `shutdown()` giống TaskRepository
- [ ] Thêm method `clearCache()`

**Kết quả mong đợi:**

- File mới compile thành công
- Tất cả methods của interface đã implement
- Code format đẹp, có comments

---

#### Bước 2.2: Update DependencyProvider

**Thời gian:** 30 phút

- [ ] Mở file `DependencyProvider.java`
- [ ] Thêm field: `private ProjectRepositoryImplWithCache projectRepositoryWithCache;`
- [ ] Tạo method `getProjectRepositoryWithCache()`:
  - [ ] Check if null
  - [ ] Nếu null: khởi tạo với ApiService và ProjectDao
  - [ ] Return instance
- [ ] Update method `clearAllCaches()`:
  - [ ] Thêm shutdown cho projectRepositoryWithCache
  - [ ] Clear cache của projectRepositoryWithCache
- [ ] Save và sync Gradle

**Kết quả mong đợi:**

- DependencyProvider compile thành công
- Có thể get ProjectRepositoryWithCache
- No errors

---

### ⏰ Buổi Chiều (3-4 giờ)

#### Bước 2.3: Test ProjectRepositoryImplWithCache

**Thời gian:** 1.5-2 giờ

- [ ] Trong một Activity test, get repository:
  - [ ] `App.dependencyProvider.getProjectRepositoryWithCache()`
- [ ] Test `getProjectsByWorkspace()`:
  - [ ] Call với workspace ID có sẵn
  - [ ] Log thời gian lần 1
  - [ ] Call lại lần 2, log thời gian
  - [ ] Verify cached data nhanh hơn
- [ ] Test `createProject()`:
  - [ ] Tạo project mới
  - [ ] Verify project xuất hiện trong list
- [ ] Test `updateProject()`:
  - [ ] Update tên project
  - [ ] Verify thay đổi được lưu
- [ ] Test `deleteProject()`:
  - [ ] Xóa project
  - [ ] Verify project không còn trong list

**Kết quả mong đợi:**

- Tất cả operations hoạt động
- Caching hoạt động (lần 2 nhanh hơn)
- No crashes
- Data consistency

---

#### Bước 2.4: Code Review tự thân

**Thời gian:** 1 giờ

- [ ] Review lại code ProjectRepository:
  - [ ] Có null checks đầy đủ?
  - [ ] Có try-catch blocks?
  - [ ] Callbacks đều dùng mainHandler.post()?
  - [ ] ExecutorService được dùng đúng cách?
- [ ] Check memory leaks:
  - [ ] ExecutorService có shutdown()?
  - [ ] Cache có được clear khi cần?
- [ ] Check edge cases:
  - [ ] Null workspaceId?
  - [ ] Empty list?
  - [ ] Network error handling?
- [ ] Fix các issues tìm thấy
- [ ] Commit code với message rõ ràng

**Kết quả mong đợi:**

- Code quality tốt
- Không có obvious bugs
- Ready để integrate vào UI

---

#### Bước 2.5: Document Progress

**Thời gian:** 30 phút

- [ ] Update checklist tiến độ
- [ ] Note lại issues gặp phải và cách giải quyết
- [ ] Chuẩn bị plan chi tiết cho ngày 3 (UI Migration)

---

## 📅 NGÀY 3: UI MIGRATION - PHASE 1 (6-8 giờ)

### ⏰ Buổi Sáng (3-4 giờ)

#### Bước 3.1: Identify Activities cần migrate

**Thời gian:** 30 phút

- [ ] Tìm tất cả Activities đang dùng:
  - [ ] `new TaskRepositoryImpl()`
  - [ ] `new ProjectRepositoryImpl()`
- [ ] Sử dụng Find in Files (Ctrl+Shift+F):
  - [ ] Search: `TaskRepositoryImpl`
  - [ ] Search: `ProjectRepositoryImpl`
- [ ] List ra các Activities tìm thấy:
  - [ ] InboxActivity
  - [ ] ProjectActivity / NewBoard
  - [ ] TaskDetailActivity
  - [ ] Các activities khác...
- [ ] Ưu tiên migrate theo thứ tự:
  - [ ] Priority 1: Activities dùng nhiều nhất
  - [ ] Priority 2: Activities có nhiều API calls
  - [ ] Priority 3: Activities khác

**Kết quả mong đợi:**

- Có danh sách đầy đủ Activities cần migrate
- Có thứ tự ưu tiên rõ ràng
- Estimate time cho từng activity

---

#### Bước 3.2: Migrate Activity #1 (VD: InboxActivity)

**Thời gian:** 2-3 giờ

- [ ] Mở file Activity đầu tiên (ví dụ: `InboxActivity.java`)
- [ ] Tìm method khởi tạo repository (thường là `setupViewModel()` hoặc `onCreate()`)
- [ ] **BACKUP**: Comment out code cũ thay vì xóa luôn
- [ ] Thay thế:
  ```
  OLD: ITaskRepository repository = new TaskRepositoryImpl(apiService);
  NEW: TaskRepositoryImplWithCache repository = App.dependencyProvider.getTaskRepositoryWithCache();
  ```
- [ ] Remove dòng khởi tạo ApiService nếu không dùng nữa
- [ ] Sync Gradle
- [ ] Fix compile errors nếu có
- [ ] Run app và test Activity này:
  - [ ] Open activity
  - [ ] Load data lần đầu
  - [ ] Close và reopen
  - [ ] Verify load nhanh hơn lần 2
  - [ ] Test các actions (create, update, delete)
- [ ] Check logcat:
  - [ ] Có log từ PerformanceLogger?
  - [ ] Có errors?
  - [ ] Cache hoạt động?
- [ ] Nếu mọi thứ OK, xóa code cũ đã comment

**Kết quả mong đợi:**

- Activity compile thành công
- Chạy không crash
- Performance cải thiện rõ rệt
- Tất cả features hoạt động bình thường

---

### ⏰ Buổi Chiều (3-4 giờ)

#### Bước 3.3: Migrate Activity #2 & #3

**Thời gian:** 2-2.5 giờ

- [ ] Áp dụng pattern tương tự cho 2 activities tiếp theo
- [ ] Mỗi activity:
  - [ ] Backup code cũ
  - [ ] Thay thế repository
  - [ ] Test kỹ lưỡng
  - [ ] Verify performance
  - [ ] Commit code

**Kết quả mong đợi:**

- Ít nhất 3 major activities đã migrate
- Tất cả đều hoạt động tốt
- Performance cải thiện đáng kể

---

#### Bước 3.4: Add Pull-to-Refresh (Optional)

**Thời gian:** 1-1.5 giờ

- [ ] Chọn 1-2 activities quan trọng
- [ ] Thêm SwipeRefreshLayout vào layout XML
- [ ] Implement refresh logic:
  - [ ] Setup SwipeRefreshLayout
  - [ ] OnRefreshListener
  - [ ] Force refresh from network
  - [ ] Update adapter with new data
  - [ ] Stop refreshing animation
- [ ] Test pull-to-refresh hoạt động
- [ ] Add toast hoặc snackbar feedback cho user

**Kết quả mong đợi:**

- Pull-to-refresh hoạt động mượt
- User có feedback rõ ràng
- Data được update từ server

---

#### Bước 3.5: Commit và Document

**Thời gian:** 30 phút

- [ ] Commit tất cả changes với messages rõ ràng:
  - [ ] "Migrate InboxActivity to cached repository"
  - [ ] "Migrate ProjectActivity to cached repository"
  - [ ] "Add pull-to-refresh to InboxActivity"
- [ ] Update progress checklist
- [ ] Note lại issues và solutions

---

## 📅 NGÀY 4: UI MIGRATION - PHASE 2 + TESTING (6-8 giờ)

### ⏰ Buổi Sáng (3-4 giờ)

#### Bước 4.1: Migrate các Activities còn lại

**Thời gian:** 2-3 giờ

- [ ] Continue migrate các activities còn lại trong list
- [ ] Áp dụng pattern đã quen
- [ ] Test từng activity sau khi migrate
- [ ] Fix bugs nếu có

**Kết quả mong đợi:**

- Tất cả activities đã migrate
- Không còn dùng old repository
- App hoạt động ổn định

---

#### Bước 4.2: Performance Testing

**Thời gian:** 1-1.5 giờ

- [ ] Chuẩn bị spreadsheet hoặc document để ghi kết quả
- [ ] Test từng activity đã migrate:
  - [ ] Measure thời gian load lần đầu (no cache)
  - [ ] Measure thời gian load lần 2 (with cache)
  - [ ] Calculate improvement percentage
- [ ] Test scenarios:
  - [ ] **Cold start**: Clear app data, mở app lần đầu
  - [ ] **Warm start**: Close và reopen app
  - [ ] **Hot reload**: Navigate giữa các activities
- [ ] Record kết quả vào table:
  ```
  Activity       | Before | After | Improvement
  InboxActivity  | 1200ms | 35ms  | 97%
  ProjectActivity| 800ms  | 30ms  | 96%
  ```

**Kết quả mong đợi:**

- Performance improvement > 90% cho cached loads
- Có data cụ thể để report
- Screenshots của logcat với timing

---

### ⏰ Buổi Chiều (3-4 giờ)

#### Bước 4.3: Edge Cases Testing

**Thời gian:** 2 giờ

- [ ] **Test 1: No Network + Empty Cache**
  - [ ] Clear app data
  - [ ] Bật airplane mode
  - [ ] Mở app
  - [ ] Verify: Show error message, không crash
- [ ] **Test 2: No Network + Has Cache**
  - [ ] Load data bình thường
  - [ ] Bật airplane mode
  - [ ] Close và reopen app
  - [ ] Verify: Load cached data, không crash
- [ ] **Test 3: Rapid Successive Calls**
  - [ ] Nhanh chóng navigate giữa các screens
  - [ ] Spam refresh button
  - [ ] Verify: Không crash, handle gracefully
- [ ] **Test 4: Large Dataset**
  - [ ] Tạo board với 50-100 tasks
  - [ ] Load và test performance
  - [ ] Verify: Vẫn nhanh
- [ ] **Test 5: Logout và Login lại**
  - [ ] Login, load data
  - [ ] Logout
  - [ ] Login với account khác
  - [ ] Verify: Không còn data của user cũ
- [ ] **Test 6: Memory Leak Check**
  - [ ] Open/close activity 10 lần
  - [ ] Check memory usage trong Android Profiler
  - [ ] Verify: Memory không tăng liên tục

**Kết quả mong đợi:**

- Tất cả edge cases được handle
- Không có crashes
- Memory stable
- Document lại issues tìm thấy

---

#### Bước 4.4: Fix Issues Found

**Thời gian:** 1-2 giờ

- [ ] Review tất cả issues tìm thấy trong testing
- [ ] Ưu tiên fix theo severity:
  - [ ] Critical: Crashes, data loss
  - [ ] High: Performance issues
  - [ ] Medium: UX issues
  - [ ] Low: Nice-to-have improvements
- [ ] Fix từng issue
- [ ] Retest sau mỗi fix
- [ ] Commit fixes

**Kết quả mong đợi:**

- Tất cả critical và high issues đã fix
- App stable
- Ready cho polish phase

---

## 📅 NGÀY 5: POLISH & DOCUMENTATION (4-6 giờ)

### ⏰ Buổi Sáng (2-3 giờ)

#### Bước 5.1: UX Improvements (Optional)

**Thời gian:** 1.5-2 giờ

- [ ] **Add Offline Indicator**
  - [ ] Tạo banner hoặc snackbar
  - [ ] Monitor network connectivity
  - [ ] Show/hide indicator based on network status
- [ ] **Improve Loading States**
  - [ ] Skeleton screens cho first load
  - [ ] Subtle refresh indicator cho background updates
  - [ ] Progress feedback rõ ràng
- [ ] **Add Success/Error Messages**
  - [ ] Toast hoặc Snackbar cho CRUD operations
  - [ ] Clear error messages
  - [ ] Retry options

**Kết quả mong đợi:**

- User experience mượt mà hơn
- Feedback rõ ràng cho user
- Professional polish

---

#### Bước 5.2: Code Cleanup

**Thời gian:** 30-45 phút

- [ ] Remove tất cả commented code cũ
- [ ] Remove unused imports
- [ ] Format code properly
- [ ] Add/update comments where needed
- [ ] Check naming conventions
- [ ] Run lint và fix warnings

**Kết quả mong đợi:**

- Clean, readable code
- No warnings
- Professional code quality

---

### ⏰ Buổi Chiều (2-3 giờ)

#### Bước 5.3: Final Testing Round

**Thời gian:** 1-1.5 giờ

- [ ] Regression testing:
  - [ ] Test tất cả migrated activities lại lần nữa
  - [ ] Verify không có bugs mới
  - [ ] Check performance vẫn tốt
- [ ] User flow testing:
  - [ ] Login → Create workspace → Create project → Create tasks
  - [ ] Update tasks → Move tasks → Delete tasks
  - [ ] Logout → Login lại
- [ ] Device testing (nếu có nhiều devices):
  - [ ] Test trên different screen sizes
  - [ ] Test trên different Android versions

**Kết quả mong đợi:**

- Tất cả features hoạt động
- No regressions
- Smooth user experience

---

#### Bước 5.4: Create Implementation Report

**Thời gian:** 1-1.5 giờ

- [ ] Tạo file: `IMPLEMENTATION_REPORT_PERSON_2.md`
- [ ] Include sections:
  - [ ] **Summary**: Tổng quan những gì đã làm
  - [ ] **Repositories Implemented**: List với status
  - [ ] **Activities Migrated**: List với before/after metrics
  - [ ] **Performance Improvements**: Table với số liệu cụ thể
  - [ ] **Test Results**: Summary của tất cả tests
  - [ ] **Known Issues**: Bugs còn tồn tại (nếu có)
  - [ ] **Recommendations**: Suggestions cho tương lai
  - [ ] **Screenshots**: Logcat performance, UI improvements
- [ ] Make it professional và dễ đọc

**Kết quả mong đợi:**

- Comprehensive report
- Clear metrics và results
- Ready để present cho team lead

---

#### Bước 5.5: Final Commit và Prepare Demo

**Thời gian:** 30 phút

- [ ] Final commit với message: "Complete Person 2 implementation - Cached repositories and UI migration"
- [ ] Push code lên repository
- [ ] Chuẩn bị demo:
  - [ ] List ra key features để demo
  - [ ] Chuẩn bị test data
  - [ ] Practice demo flow
- [ ] Update team lead về completion

**Kết quả mong đợi:**

- Code pushed thành công
- Ready để demo
- Documentation complete

---

## 📊 SUMMARY CHECKLIST - PERSON 2

### Phase 1: Review & Setup ✅

- [ ] Prerequisites verified (Người 1's work complete)
- [ ] TaskRepositoryImplWithCache reviewed và understood
- [ ] DependencyProvider tested và working
- [ ] Initial tests với real data successful

### Phase 2: Repository Implementation ✅

- [ ] ProjectRepositoryImplWithCache implemented
- [ ] All interface methods completed
- [ ] DependencyProvider updated
- [ ] Testing passed
- [ ] Code reviewed và cleaned

### Phase 3: UI Migration ✅

- [ ] All target activities identified
- [ ] Migration completed (minimum 3 major activities)
- [ ] Pull-to-refresh added (optional)
- [ ] All migrated activities tested
- [ ] No regression bugs

### Phase 4: Testing & Quality Assurance ✅

- [ ] Performance testing completed
- [ ] Metrics collected và documented
- [ ] Edge cases tested
- [ ] Memory leak check passed
- [ ] All critical issues fixed

### Phase 5: Polish & Documentation ✅

- [ ] UX improvements added (optional)
- [ ] Code cleanup completed
- [ ] Final testing round passed
- [ ] Implementation report created
- [ ] Demo prepared

---

## 🚨 TROUBLESHOOTING GUIDE

### Issue: DependencyProvider returns null

**Solutions:**

- Check DependencyProvider được khởi tạo trong App.onCreate()
- Verify singleton pattern implementation
- Check thread safety

### Issue: Cache không hoạt động

**Solutions:**

- Verify TaskDao methods được gọi đúng
- Check database permissions
- Verify entities được save vào database
- Check Room database inspector trong Android Studio

### Issue: App crash khi offline

**Solutions:**

- Add proper null checks
- Handle network errors gracefully
- Check callback null before calling

### Issue: Memory leak

**Solutions:**

- Ensure executorService.shutdown() được gọi
- Check static references
- Use Android Profiler để identify leaks

### Issue: Performance không cải thiện

**Solutions:**

- Verify cache-first strategy implemented correctly
- Check database queries optimized
- Ensure UI thread không bị block
- Review ExecutorService configuration

---

## 📞 ESCALATION POINTS

**Escalate ngay lập tức nếu:**

- Stuck > 1 giờ trên cùng một issue
- Critical bugs không thể fix được
- Data corruption hoặc loss
- App crashes không thể reproduce
- Conflicts với code của Người 1

**Escalate sau khi try:**

1. Debug carefully
2. Review documentation
3. Check existing code examples
4. Google error messages
5. Ask Người 1 (nếu database-related)

---

## ✅ DEFINITION OF DONE

**Person 2 hoàn thành khi:**

- [ ] Tất cả repositories implemented và tested
- [ ] Minimum 3 major activities migrated successfully
- [ ] Performance improvement > 90% cho cached loads
- [ ] All tests passed (unit, integration, edge cases)
- [ ] No critical or high severity bugs
- [ ] Code clean và well-documented
- [ ] Implementation report completed
- [ ] Demo successful
- [ ] Team lead approval

---

## 💡 TIPS FOR SUCCESS

1. **Đừng rush**: Take time để hiểu code trước khi modify
2. **Test frequently**: Test sau mỗi change, đừng đợi đến cuối
3. **Commit often**: Small, frequent commits với clear messages
4. **Document as you go**: Đừng để documentation đến phút chót
5. **Communicate**: Update team lead daily về progress
6. **Backup**: Luôn comment out code cũ trước khi replace
7. **Performance metrics**: Measure everything, data is important
8. **User perspective**: Think như end-user, UX matters

---

**Good luck! 🚀 Focus on quality over speed. Take breaks when needed.**
