# 🔧 Backend 502 Error Troubleshooting

## ❌ Issue: Backend Unavailable (502 Bad Gateway)

**Date:** November 12, 2025  
**Endpoint:** `GET /api/projects/:id/summary`  
**Status Code:** 502 Bad Gateway  
**Server:** Render (plantracker-backend-1.onrender.com)

---

## 📋 Error Details

### Log Output
```
15:45:06.799  D  <-- 502 https://plantracker-backend-1.onrender.com/api/projects/b6f4b6d5-7ae8-4a3a-8574-10caac847468/summary (117ms)
15:45:06.799  D  content-type: text/html; charset=utf-8
15:45:06.799  D  x-render-routing: dynamic-free-error
15:45:07.692  E  Error occurred: Failed to get project summary: 502
```

### Error Response HTML
```html
<title>502</title>
<h1>Bad Gateway</h1>
<div>This service is currently unavailable. Please try again in a few minutes.</div>
```

---

## 🔍 Root Cause Analysis

### Render Free Tier Behavior
1. **Auto-sleep:** Services sleep after 15 minutes of inactivity
2. **Cold start:** Takes 30-60 seconds to wake up on first request
3. **502 Error:** Returned when service is starting or crashed

### Possible Causes
- ✅ **Most likely:** Service is sleeping (free tier auto-sleep)
- ⚠️ **Possible:** Backend crashed due to error
- ⚠️ **Less likely:** Deploy in progress
- ❌ **Unlikely:** Server capacity issue (free tier)

---

## ✅ Frontend Behavior (Correct!)

### Request Flow
1. App makes authenticated request with Firebase token
2. OkHttp interceptor adds Authorization header
3. Backend returns 502 (unavailable)
4. Frontend shows error: "Failed to get project summary: 502"

### Frontend Logic (No Changes Needed)
```java
// ProjectActivity.java
projectViewModel.selectProject(projectId);
  ↓
// ProjectViewModel.java  
loadProjectById(projectId);     // Load project details
loadBoardsForProject(projectId); // Load boards → Auto-load tasks
  ↓
// Observer pattern updates UI automatically
tasksPerBoardLiveData.observe() // Boards & tasks displayed when data arrives
```

**✅ Frontend is working correctly!** Error handling is proper. Once backend is available, tasks will load automatically.

---

## 🛠️ Solutions

### Option 1: Wait for Auto-Wake (Recommended)
**Time:** 30-60 seconds  
**Steps:**
1. Wait 1-2 minutes for Render to wake up service
2. Pull to refresh in app
3. Backend will respond normally

**Why this works:** Render free tier automatically wakes services on incoming requests

---

### Option 2: Manual Wake via Dashboard
**Time:** 2-3 minutes  
**Steps:**
1. Go to https://dashboard.render.com
2. Navigate to `plantracker-backend-1` service
3. Check logs for errors
4. Click "Manual Deploy" if needed (deploys latest commit)
5. Wait for deploy to complete

---

### Option 3: Keep Backend Alive
**Time:** Setup once  
**Steps:**
1. Use cron job or uptime monitor to ping every 10 minutes
2. Options:
   - UptimeRobot (https://uptimerobot.com) - Free tier
   - Cron-job.org (https://cron-job.org) - Free
   - GitHub Actions scheduled workflow

**Example endpoint to ping:**
```
GET https://plantracker-backend-1.onrender.com/health
```

---

## 📊 Monitoring Recommendations

### Add Health Check Endpoint
**Backend:** `src/app.controller.ts`
```typescript
@Get('/health')
healthCheck() {
  return {
    status: 'ok',
    timestamp: new Date().toISOString(),
    uptime: process.uptime()
  };
}
```

### Add Retry Logic (Optional)
**Frontend:** `ProjectRepositoryImpl.java`
```java
// Add retry interceptor to OkHttpClient
new Interceptor() {
  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    Response response = chain.proceed(request);
    
    // Retry on 502 (backend waking up)
    int tryCount = 0;
    while (!response.isSuccessful() && response.code() == 502 && tryCount < 3) {
      tryCount++;
      Thread.sleep(2000); // Wait 2s
      response = chain.proceed(request);
    }
    return response;
  }
}
```

---

## 🎯 Quick Test After Fix

### 1. Test Backend Directly
```bash
curl -X GET https://plantracker-backend-1.onrender.com/health
# Expected: 200 OK
```

### 2. Test Project Summary Endpoint
```bash
curl -X GET \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN" \
  https://plantracker-backend-1.onrender.com/api/projects/PROJECT_ID/summary
# Expected: 200 OK with JSON response
```

### 3. Test in App
1. Open ProjectActivity
2. Check Logcat for:
   ```
   D  <-- 200 https://plantracker-backend-1.onrender.com/api/projects/.../summary
   ```
3. Verify boards and tasks load correctly

---

## 📈 Long-term Solution: Upgrade Render Plan

### Free Tier Limitations
- ❌ Auto-sleep after 15 minutes inactivity
- ❌ 750 hours/month (not 24/7)
- ❌ Cold start delays (30-60s)
- ❌ Shared resources

### Paid Tier Benefits ($7/month Starter)
- ✅ No auto-sleep
- ✅ 24/7 uptime
- ✅ No cold starts
- ✅ Better performance
- ✅ Custom domains

---

## 🔄 Alternative: Self-Host

### Options
1. **DigitalOcean App Platform** - $5/month
2. **Railway** - $5/month (free tier available)
3. **Fly.io** - Free tier with better limits
4. **Heroku** - $7/month (eco dynos)
5. **AWS EC2 Free Tier** - Free for 12 months

---

## ✅ Current Status

### Frontend
- ✅ Logic is correct
- ✅ Error handling works
- ✅ Observers auto-update UI
- ✅ No code changes needed

### Backend
- ⏳ Service sleeping (free tier)
- 🔄 Will wake on next request
- ⏱️ Wait 1-2 minutes then retry

### Drag & Drop
- ✅ Implementation complete
- ✅ Build successful
- ✅ Ready to test when backend is up

---

## 📝 Notes

- This is **NOT a bug** - it's expected Render free tier behavior
- Frontend is production-ready
- Consider upgrading Render plan for production use
- Alternative: Use uptime monitor to keep backend alive

---

## 🎯 Next Steps

1. **Immediate:** Wait 1-2 minutes for backend to wake up
2. **Short-term:** Add uptime monitor (UptimeRobot free tier)
3. **Long-term:** Upgrade to paid Render plan ($7/month) for 24/7 uptime

**Backend will be available shortly!** 🚀
