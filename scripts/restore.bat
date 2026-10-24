@echo off
set SERIAL=192.168.1.112:36469
adb -s %SERIAL% shell pm enable --user 0 com.google.android.apps.tv.launcherx
adb -s %SERIAL% shell cmd package set-home-activity com.google.android.apps.tv.launcherx/.home.HomeActivity
adb -s %SERIAL% shell am start -n com.google.android.apps.tv.launcherx/.home.HomeActivity
echo To remove the custom launcher completely, run: adb -s %SERIAL% uninstall com.example.tvhome
pause
