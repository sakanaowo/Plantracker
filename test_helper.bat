@echo off
REM Test Helper Script for Phase 5 - Person 1
REM Windows batch file

echo ================================================
echo  PHASE 5 TESTING - PERSON 1
echo ================================================
echo.

:menu
echo Choose an option:
echo 1. Check Backend Server Status
echo 2. Show Logcat (LoginActivity)
echo 3. Show Logcat (SignupActivity)
echo 4. Show Logcat (HomeActivity)
echo 5. Show All Auth Related Logs
echo 6. Clear Logcat
echo 7. Uninstall App
echo 8. Install Debug APK
echo 9. Run Full Build and Install
echo 0. Exit
echo.
set /p choice="Enter your choice (0-9): "

if "%choice%"=="1" goto check_backend
if "%choice%"=="2" goto logcat_login
if "%choice%"=="3" goto logcat_signup
if "%choice%"=="4" goto logcat_home
if "%choice%"=="5" goto logcat_all
if "%choice%"=="6" goto clear_logcat
if "%choice%"=="7" goto uninstall
if "%choice%"=="8" goto install
if "%choice%"=="9" goto build_install
if "%choice%"=="0" goto end

:check_backend
echo.
echo Checking backend server...
curl -s http://localhost:3000/health
if errorlevel 1 (
    echo [ERROR] Backend server not responding!
    echo Please start the backend server first.
) else (
    echo [OK] Backend server is running
)
echo.
pause
goto menu

:logcat_login
echo.
echo Showing LoginActivity logs (Ctrl+C to stop)...
echo.
adb logcat -c
adb logcat | findstr /i "LoginActivity AuthViewModel AuthRepository"
pause
goto menu

:logcat_signup
echo.
echo Showing SignupActivity logs (Ctrl+C to stop)...
echo.
adb logcat -c
adb logcat | findstr /i "SignupActivity AuthViewModel AuthRepository"
pause
goto menu

:logcat_home
echo.
echo Showing HomeActivity logs (Ctrl+C to stop)...
echo.
adb logcat -c
adb logcat | findstr /i "HomeActivity WorkspaceViewModel WorkspaceRepository"
pause
goto menu

:logcat_all
echo.
echo Showing all auth-related logs (Ctrl+C to stop)...
echo.
adb logcat -c
adb logcat | findstr /i "LoginActivity SignupActivity HomeActivity AuthViewModel WorkspaceViewModel Repository"
pause
goto menu

:clear_logcat
echo.
echo Clearing logcat buffer...
adb logcat -c
echo [OK] Logcat cleared
echo.
pause
goto menu

:uninstall
echo.
echo Uninstalling app...
adb uninstall com.example.tralalero
echo [OK] App uninstalled
echo.
pause
goto menu

:install
echo.
echo Installing debug APK...
adb install -r app\build\outputs\apk\debug\app-debug.apk
if errorlevel 1 (
    echo [ERROR] Installation failed
    echo Make sure the APK is built first (gradlew assembleDebug)
) else (
    echo [OK] App installed
)
echo.
pause
goto menu

:build_install
echo.
echo Building and installing app...
echo Step 1: Clean build
call gradlew clean
echo.
echo Step 2: Build debug APK
call gradlew assembleDebug
echo.
echo Step 3: Install to device
call gradlew installDebug
echo.
echo [OK] Build and install complete
echo.
pause
goto menu

:end
echo.
echo Exiting test helper. Good luck with testing!
exit /b

