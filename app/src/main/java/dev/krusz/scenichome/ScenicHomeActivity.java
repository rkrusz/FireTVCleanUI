package dev.krusz.scenichome;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Offline remote-first home app; never intercepts the physical Home key. */
public final class ScenicHomeActivity extends Activity {
    private static final int[] SCENES = {R.drawable.scenic_coast, R.drawable.scenic_desert,
            R.drawable.scenic_falls, R.drawable.scenic_forest, R.drawable.scenic_hills,
            R.drawable.scenic_mountain, R.drawable.scenic_stars, R.drawable.scenic_winter};
    private static final String[] NAMES = {"Coast", "Desert", "Falls", "Forest", "Hills", "Mountain", "Stars", "Winter"};
    private static final int HOME = 0, MANAGE = 1, WALLPAPERS = 2;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Map<String, View> targets = new HashMap<>();
    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            refreshTime();
            handler.postDelayed(this, 60100L - System.currentTimeMillis() % 60000L);
        }
    };
    private AppCatalog catalog;
    private FavoritesStore store;
    private SystemActions actions;
    private SharedPreferences prefs;
    private List<String> pins = new ArrayList<>();
    private List<AppCatalog.Entry> installed = new ArrayList<>();
    private FrameLayout root;
    private ImageView image;
    private TextView clock, date, notice;
    private int currentImage = -1, screen = HOME;
    private String focused = "manage", homeFocus = "manage";

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        catalog = new AppCatalog(this);
        store = new FavoritesStore(this);
        actions = new SystemActions(this);
        prefs = getSharedPreferences(FavoritesStore.PREFS_NAME, MODE_PRIVATE);
        if (state != null) {
            screen = state.getInt("screen", HOME);
            focused = state.getString("focus", "manage");
            homeFocus = state.getString("homeFocus", "manage");
        }
        pins = store.load();
        installed = catalog.listInstalled();
        render();
    }
    @Override protected void onResume() {
        super.onResume();
        pins = store.load();
        installed = catalog.listInstalled();
        render();
        handler.removeCallbacks(ticker);
        ticker.run();
    }
    @Override protected void onPause() {
        handler.removeCallbacks(ticker);
        super.onPause();
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        out.putInt("screen", screen);
        out.putString("focus", focused);
        out.putString("homeFocus", homeFocus);
        super.onSaveInstanceState(out);
    }
    @Override public boolean onKeyDown(int code, KeyEvent event) {
        if (code == KeyEvent.KEYCODE_MENU) {
            if (screen != MANAGE) showScreen(MANAGE, "manage:first");
            return true;
        }
        return super.onKeyDown(code, event);
    }
    @Override public void onBackPressed() {
        if (screen == HOME) super.onBackPressed();
        else { screen = HOME; focused = homeFocus; render(); }
    }
    private void showScreen(int next, String key) {
        if (screen == HOME) homeFocus = focused;
        screen = next;
        focused = key;
        render();
    }
    private int dp(float n) { return (int) (n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable shape(int color, int border) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(10));
        if (border != 0) d.setStroke(dp(2), border);
        return d;
    }
    private TextView label(String value, int size) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(Color.WHITE);
        v.setTextSize(size);
        v.setGravity(Gravity.CENTER_VERTICAL);
        return v;
    }
    private void focusable(View v, String key, Runnable click) {
        v.setFocusable(true);
        v.setClickable(true);
        v.setBackground(shape(0x9a274658, 0x88ffffff));
        v.setOnFocusChangeListener((view, yes) -> {
            view.setBackground(yes ? shape(0xeb245474, Color.WHITE) : shape(0x9a274658, 0x88ffffff));
            view.setScaleX(yes ? 1.04f : 1f);
            view.setScaleY(yes ? 1.04f : 1f);
            if (yes) focused = key;
        });
        v.setOnClickListener(view -> click.run());
        targets.put(key, v);
    }
    private TextView button(String title, String key, Runnable click) {
        TextView v = label(title, 15);
        v.setGravity(Gravity.CENTER);
        v.setSingleLine(true);
        v.setEllipsize(android.text.TextUtils.TruncateAt.END);
        focusable(v, key, click);
        return v;
    }
    private void render() {
        targets.clear();
        clock = null; date = null; notice = null;
        root = new FrameLayout(this);
        root.setBackgroundColor(0xff112634);
        image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(image, new FrameLayout.LayoutParams(-1, -1));
        View scrim = new View(this);
        scrim.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0x77091726, 0x22091726, 0xb5091726}));
        root.addView(scrim, new FrameLayout.LayoutParams(-1, -1));
        if (screen == HOME) home();
        else if (screen == MANAGE) manage();
        else wallpaperPicker();
        // The new ImageView must always receive its scene, even if the index has not changed.
        currentImage = -1;
        refreshTime();
        setContentView(root);
        View target = targets.get(focused);
        if (target == null && screen == HOME) {
            for (String pkg : pins) {
                target = targets.get("app:" + pkg);
                if (target != null) break;
            }
            if (target == null) target = targets.get("pin");
            if (target == null) target = targets.get("manage");
        }
        if (target == null && screen == MANAGE) {
            if (!installed.isEmpty()) target = targets.get("manage:" + installed.get(0).packageName);
            if (target == null) for (String pkg : pins) {
                target = targets.get("manage:missing:" + pkg);
                if (target != null) break;
            }
        }
        if (target == null && screen == WALLPAPERS) target = targets.get("wall:daily");
        if (target != null) { View restore = target; restore.post(restore::requestFocus); }
    }
    private void home() {
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.VERTICAL);
        clock = label("", 52);
        clock.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        date = label("", 21);
        top.addView(clock); top.addView(date);
        FrameLayout.LayoutParams t = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.LEFT);
        t.setMargins(dp(42), dp(30), 0, 0);
        root.addView(top, t);
        notice = label("", 19);
        notice.setPadding(dp(16), dp(8), dp(16), dp(8));
        notice.setBackground(shape(0xeb102d40, Color.WHITE));
        notice.setVisibility(View.GONE);
        FrameLayout.LayoutParams n = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.RIGHT);
        n.setMargins(dp(24), dp(32), dp(24), 0);
        root.addView(notice, n);
        LinearLayout dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.VERTICAL);
        dock.setPadding(dp(14), dp(10), dp(14), dp(12));
        dock.setBackground(shape(0xb2193040, 0x77ffffff));
        FrameLayout.LayoutParams d = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM);
        d.setMargins(dp(32), 0, dp(32), dp(24));
        root.addView(dock, d);
        dock.addView(label("FAVORITES", 14));
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setPadding(0, dp(5), 0, dp(5));
        scroll.addView(row);
        dock.addView(scroll, new LinearLayout.LayoutParams(-1, dp(95)));
        int visible = 0;
        for (String pkg : pins) {
            AppCatalog.Entry entry = entry(pkg);
            if (entry == null) continue;
            visible++;
            LinearLayout tile = new LinearLayout(this);
            tile.setOrientation(LinearLayout.VERTICAL);
            tile.setGravity(Gravity.CENTER);
            ImageView icon = new ImageView(this);
            icon.setImageDrawable(entry.icon);
            tile.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));
            TextView name = label(entry.label, 14);
            name.setGravity(Gravity.CENTER);
            name.setSingleLine(true);
            name.setEllipsize(android.text.TextUtils.TruncateAt.END);
            tile.addView(name, new LinearLayout.LayoutParams(-1, dp(26)));
            focusable(tile, "app:" + pkg, () -> catalog.launch(pkg));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(112), dp(82));
            p.rightMargin = dp(8);
            row.addView(tile, p);
        }
        if (visible == 0) row.addView(button("＋ Pin apps", "pin", () -> showScreen(MANAGE, "manage:first")),
                new LinearLayout.LayoutParams(dp(165), dp(82)));
        LinearLayout shortcuts = new LinearLayout(this);
        dock.addView(shortcuts, new LinearLayout.LayoutParams(-1, dp(51)));
        shortcut(shortcuts, "Live TV", "live", () -> {
            if (!actions.openLiveTv()) message("Live TV unavailable. Use remote Live TV key or Fire TV Home.");
        });
        shortcut(shortcuts, "Inputs", "inputs", () -> {
            if (!actions.openInputs()) message("Inputs unavailable. Use physical Inputs key or Fire TV controls.");
        });
        shortcut(shortcuts, "Settings", "settings", () -> {
            if (!actions.openSettings()) message("Settings unavailable. Open Settings from Fire TV Home.");
        });
        shortcut(shortcuts, "Amazon Home", "amazon", () -> {
            if (!actions.openAmazonHome()) message("Home shortcut unavailable. Press physical Home key.");
        });
        shortcut(shortcuts, "Manage", "manage", () -> showScreen(MANAGE, "manage:first"));
        shortcut(shortcuts, "Wallpapers", "wallpapers", () -> showScreen(WALLPAPERS, "wall:daily"));
        if (visible == 0 && focused.equals("manage")) focused = "pin";
    }
    private void shortcut(LinearLayout row, String title, String key, Runnable click) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(46), 1);
        p.setMargins(dp(3), dp(2), dp(3), 0);
        row.addView(button(title, key, click), p);
    }
    private void message(String value) { notice.setText(value); notice.setVisibility(View.VISIBLE); }
    private LinearLayout page(String title, String instructions) {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(45), dp(28), dp(45), dp(28));
        page.setBackgroundColor(0xb7193040);
        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        TextView heading = label(title, 33);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        page.addView(heading);
        page.addView(label(instructions, 17), new LinearLayout.LayoutParams(-1, dp(48)));
        return page;
    }
    private void manage() {
        LinearLayout page = page("Manage favorites", "↑ ↓ Choose  •  Select Pin/Unpin  •  ← → Move pinned app  •  Back returns home");
        TextView count = label("Pinned " + pins.size() + "/" + Favorites.MAX_PINS + "  •  Missing apps remain saved", 17);
        page.addView(count);
        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        if (installed.isEmpty()) list.addView(label("No launchable apps found. Press Back.", 20));
        for (AppCatalog.Entry app : installed) {
            String pkg = app.packageName;
            int index = pins.indexOf(pkg);
            String key = "manage:" + pkg;
            LinearLayout tile = new LinearLayout(this);
            tile.setGravity(Gravity.CENTER_VERTICAL);
            tile.setPadding(dp(12), dp(4), dp(12), dp(4));
            ImageView icon = new ImageView(this);
            icon.setImageDrawable(app.icon);
            tile.addView(icon, new LinearLayout.LayoutParams(dp(37), dp(37)));
            TextView text = label(app.label + (index < 0 ? "  •  Select to pin"
                    : "  •  Pinned #" + (index + 1) + "  •  Select unpins / ← → reorders"), 18);
            text.setSingleLine(true);
            text.setEllipsize(android.text.TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1);
            lp.leftMargin = dp(13);
            tile.addView(text, lp);
            focusable(tile, key, () -> {
                List<String> updated = pins.contains(pkg) ? Favorites.unpin(pins, pkg) : Favorites.pin(pins, pkg);
                if (updated.equals(pins) && !pins.contains(pkg)) count.setText("Eight pins saved. Unpin one first.");
                else { pins = updated; store.save(pins); render(); }
            });
            tile.setOnKeyListener((v, code, event) -> {
                if (event.getAction() != KeyEvent.ACTION_DOWN ||
                        (code != KeyEvent.KEYCODE_DPAD_LEFT && code != KeyEvent.KEYCODE_DPAD_RIGHT)) return false;
                int from = pins.indexOf(pkg);
                int to = from + (code == KeyEvent.KEYCODE_DPAD_LEFT ? -1 : 1);
                if (from >= 0 && to >= 0 && to < pins.size()) {
                    pins = Favorites.move(pins, from, to);
                    store.save(pins);
                    render();
                }
                return true;
            });
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(55));
            p.bottomMargin = dp(5);
            list.addView(tile, p);
        }
        for (String pkg : pins) {
            if (entry(pkg) != null) continue;
            TextView missing = button("Missing: " + pkg + "  •  Select to unpin", "manage:missing:" + pkg, () -> {
                pins = Favorites.unpin(pins, pkg);
                store.save(pins);
                render();
            });
            list.addView(missing, new LinearLayout.LayoutParams(-1, dp(55)));
        }
        if (!targets.containsKey(focused) && !installed.isEmpty()) focused = "manage:" + installed.get(0).packageName;
    }
    private void wallpaperPicker() {
        LinearLayout page = page("Wallpapers", "Choose Daily rotation or freeze a scene  •  Back returns home");
        ScrollView scroll = new ScrollView(this);
        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(rows);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        int selected = frozen();
        for (int r = 0; r < 3; r++) {
            LinearLayout row = new LinearLayout(this);
            rows.addView(row);
            for (int c = 0; c < 3; c++) {
                int choice = r * 3 + c - 1;
                if (choice >= SCENES.length) break;
                LinearLayout tile = new LinearLayout(this);
                tile.setOrientation(LinearLayout.VERTICAL);
                tile.setGravity(Gravity.CENTER);
                if (choice >= 0) {
                    ImageView preview = new ImageView(this);
                    preview.setImageResource(SCENES[choice]);
                    preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    tile.addView(preview, new LinearLayout.LayoutParams(-1, dp(78)));
                }
                TextView title = label((choice < 0 ? "Daily rotation" : NAMES[choice])
                        + (choice == selected ? "  ✓" : ""), 19);
                title.setGravity(Gravity.CENTER);
                tile.addView(title, new LinearLayout.LayoutParams(-1, dp(45)));
                focusable(tile, choice < 0 ? "wall:daily" : "wall:" + choice, () -> {
                    prefs.edit().putInt("frozen_wallpaper", choice).apply();
                    screen = HOME;
                    focused = homeFocus;
                    render();
                });
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(131), 1);
                p.setMargins(dp(5), dp(5), dp(5), dp(5));
                row.addView(tile, p);
            }
        }
    }
    private AppCatalog.Entry entry(String pkg) {
        for (AppCatalog.Entry app : installed) if (app.packageName.equals(pkg)) return app;
        return null;
    }
    private int frozen() {
        try { return prefs.getInt("frozen_wallpaper", -1); }
        catch (ClassCastException invalid) { return -1; }
    }
    private void refreshTime() {
        LocalDate today = LocalDate.now();
        if (clock != null) {
            Calendar local = Calendar.getInstance();
            int hour = local.get(Calendar.HOUR);
            if (hour == 0) hour = 12;
            clock.setText(String.format(Locale.getDefault(), "%d:%02d %s", hour, local.get(Calendar.MINUTE),
                    local.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM"));
            date.setText(today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())));
        }
        int selected = DailyWallpaper.selectedIndex(today.toEpochDay(), SCENES.length, frozen());
        if (selected >= 0 && selected != currentImage) {
            image.setImageResource(SCENES[selected]);
            currentImage = selected;
        }
    }
}
