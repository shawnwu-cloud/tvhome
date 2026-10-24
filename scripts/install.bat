@echo off
set SERIAL=192.168.1.112:36469
adb -s %SERIAL% install -r app-debug.apk
adb -s %SERIAL% shell cmd package set-home-activity com.example.tvhome/.MainActivity
adb -s %SERIAL% shell am start -n com.example.tvhome/.MainActivity
adb -s %SERIAL% shell dumpsys activity activities | findstr mFocusedApp
echo Check the TV screen. mFocusedApp above should name com.example.tvhome.
pause
