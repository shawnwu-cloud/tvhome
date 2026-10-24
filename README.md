# TV Home (Apple TV style launcher)
1. Push this folder to a GitHub repo. Actions > Build APK > download artifact `tvhome-apk` (app-debug.apk).
2. Put app-debug.apk next to scripts\install.bat, edit SERIAL if the port changed, run it.
3. Undo: scripts\restore.bat. The stock launcher is never disabled.
Change dock: edit DOCK in MainActivity.java. Change columns: COLS.
If `adb install` hangs: `adb push app-debug.apk /data/local/tmp/` then `adb shell pm install -r /data/local/tmp/app-debug.apk`.
