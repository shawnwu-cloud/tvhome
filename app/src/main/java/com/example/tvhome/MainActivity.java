package com.example.tvhome;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    // Dock apps, in order. Edit this list to change the dock.
    static final String[] DOCK = {"com.google.android.youtube.tv", "com.netflix.ninja",
            "com.disney.disneyplus", "hami.androidtv"};
    static final int COLS = 4;

    PackageManager pm;
    float d;
    TextView dockName, date, greet;
    LinearLayout dockRow, grid;
    View wifi, settings;

    int dp(int v) { return (int) (v * d + 0.5f); }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        pm = getPackageManager();
        d = getResources().getDisplayMetrics().density;
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        // top-left: greeting, clock, date
        LinearLayout tl = new LinearLayout(this);
        tl.setOrientation(LinearLayout.VERTICAL);
        greet = text(18, Color.argb(160, 255, 255, 255));
        TextClock clock = new TextClock(this);
        clock.setFormat24Hour("HH:mm"); clock.setFormat12Hour("HH:mm");
        clock.setTextSize(64); clock.setTextColor(Color.WHITE);
        clock.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
        date = text(18, Color.argb(160, 255, 255, 255));
        tl.addView(greet); tl.addView(clock); tl.addView(date);
        FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.START);
        tlp.setMargins(dp(48), dp(24), 0, 0);
        root.addView(tl, tlp);

        // top-right pills
        LinearLayout pills = new LinearLayout(this);
        wifi = pill("Wi-Fi", Settings.ACTION_WIFI_SETTINGS);
        settings = pill("Settings", Settings.ACTION_SETTINGS);
        pills.addView(wifi); pills.addView(settings);
        FrameLayout.LayoutParams plp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.END);
        plp.setMargins(0, dp(32), dp(48), 0);
        root.addView(pills, plp);

        // scrolling body: focused name, dock band, grid
        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(48), dp(190), dp(48), dp(48));
        dockName = text(34, Color.WHITE);
        dockName.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        dockName.setPadding(dp(8), 0, 0, dp(10));
        body.addView(dockName);
        dockRow = new LinearLayout(this);
        dockRow.setPadding(dp(16), dp(14), dp(16), dp(14));
        GradientDrawable band = new GradientDrawable();
        band.setColor(Color.argb(40, 255, 255, 255)); band.setCornerRadius(dp(28));
        dockRow.setBackground(band);
        body.addView(dockRow);
        grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setPadding(dp(16), dp(28), dp(16), 0);
        body.addView(grid);
        sv.addView(body);
        root.addView(sv, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    @Override protected void onResume() {
        super.onResume();
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        greet.setText(h < 12 ? "早安" : h < 18 ? "午安" : "晚安");
        date.setText(new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(new Date()));
        populate();
    }

    void populate() {
        dockRow.removeAllViews(); grid.removeAllViews();
        Intent q = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> all = pm.queryIntentActivities(q, 0);
        Map<String, ResolveInfo> byPkg = new LinkedHashMap<>();
        for (ResolveInfo ri : all) byPkg.put(ri.activityInfo.packageName, ri);
        List<String> dockPkgs = new ArrayList<>();
        for (String p : DOCK) if (byPkg.containsKey(p)) dockPkgs.add(p);
        for (String p : dockPkgs) dockRow.addView(tile(byPkg.get(p), true));
        List<ResolveInfo> rest = new ArrayList<>();
        for (ResolveInfo ri : all) {
            String p = ri.activityInfo.packageName;
            if (!p.equals(getPackageName()) && !dockPkgs.contains(p)) rest.add(ri);
        }
        Collections.sort(rest, (a, c) -> a.loadLabel(pm).toString().compareToIgnoreCase(c.loadLabel(pm).toString()));
        LinearLayout row = null;
        for (int i = 0; i < rest.size(); i++) {
            if (i % COLS == 0) { row = new LinearLayout(this); grid.addView(row); }
            row.addView(tile(rest.get(i), false));
        }
        if (dockRow.getChildCount() > 0) {
            View first = dockRow.getChildAt(0);
            for (int i = 0; i < dockRow.getChildCount(); i++) dockRow.getChildAt(i).setNextFocusUpId(wifi.getId());
            if (getCurrentFocus() == null) first.requestFocus();
        }
    }

    View tile(ResolveInfo ri, boolean dock) {
        final String label = ri.loadLabel(pm).toString();
        final String pkg = ri.activityInfo.packageName;
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.VERTICAL);
        t.setGravity(Gravity.CENTER_HORIZONTAL);
        t.setPadding(dp(8), dp(8), dp(8), dp(6));
        t.setFocusable(true); t.setClickable(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(208), -2);
        lp.setMargins(dp(8), dp(8), dp(8), dp(8));
        t.setLayoutParams(lp);
        ImageView iv = new ImageView(this);
        Drawable img = ri.activityInfo.loadBanner(pm);
        boolean banner = img != null;
        if (!banner) img = ri.loadIcon(pm);
        iv.setImageDrawable(img);
        iv.setScaleType(banner ? ImageView.ScaleType.CENTER_CROP : ImageView.ScaleType.FIT_CENTER);
        iv.setBackgroundColor(Color.argb(60, 255, 255, 255));
        iv.setClipToOutline(true);
        iv.setOutlineProvider(new ViewOutlineProvider() {
            @Override public void getOutline(View v, Outline o) { o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), dp(12)); }
        });
        t.addView(iv, new LinearLayout.LayoutParams(dp(192), dp(108)));
        TextView n = text(14, Color.WHITE);
        n.setGravity(Gravity.CENTER); n.setSingleLine(true); n.setText(label); n.setAlpha(0f);
        n.setPadding(0, dp(6), 0, 0);
        t.addView(n, new LinearLayout.LayoutParams(-1, -2));
        final GradientDrawable glass = new GradientDrawable();
        glass.setColor(Color.argb(70, 255, 255, 255)); glass.setCornerRadius(dp(18));
        t.setOnFocusChangeListener((v, f) -> {
            v.animate().scaleX(f ? 1.08f : 1f).scaleY(f ? 1.08f : 1f).setDuration(150).start();
            v.setBackground(f ? glass : null);
            n.setAlpha(f ? 1f : 0f);
            if (f && dock) dockName.setText(label);
        });
        t.setOnClickListener(v -> launch(pkg));
        return t;
    }

    View pill(String label, final String action) {
        TextView p = text(16, Color.WHITE);
        p.setText(label); p.setId(View.generateViewId());
        p.setPadding(dp(22), dp(10), dp(22), dp(10));
        p.setFocusable(true); p.setClickable(true);
        final GradientDrawable off = new GradientDrawable(), on = new GradientDrawable();
        off.setColor(Color.argb(50, 255, 255, 255)); off.setCornerRadius(dp(30));
        on.setColor(Color.argb(140, 255, 255, 255)); on.setCornerRadius(dp(30));
        p.setBackground(off);
        p.setOnFocusChangeListener((v, f) -> v.setBackground(f ? on : off));
        p.setOnClickListener(v -> {
            try { startActivity(new Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); } catch (Exception ignored) {}
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(dp(8), 0, dp(8), 0);
        p.setLayoutParams(lp);
        return p;
    }

    void launch(String pkg) {
        Intent i = pm.getLeanbackLaunchIntentForPackage(pkg);
        if (i == null) i = pm.getLaunchIntentForPackage(pkg);
        if (i != null) { i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); }
    }

    TextView text(int sp, int color) {
        TextView t = new TextView(this);
        t.setTextSize(sp); t.setTextColor(color);
        return t;
    }

    @Override public void onBackPressed() { /* stay on Home */ }
}
