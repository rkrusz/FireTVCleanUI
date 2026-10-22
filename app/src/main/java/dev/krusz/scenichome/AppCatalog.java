package dev.krusz.scenichome;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import java.text.Collator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A snapshot of externally launchable apps; refresh before rendering a picker. */
public final class AppCatalog {
    public static final class Entry {
        public final String packageName;
        public final String label;
        public final Drawable icon;
        private final ComponentName component;

        private Entry(String packageName, String label, Drawable icon, ComponentName component) {
            this.packageName = packageName;
            this.label = label;
            this.icon = icon;
            this.component = component;
        }
    }

    private final Context context;
    private final PackageManager packages;

    public AppCatalog(Context context) {
        this.context = context;
        this.packages = context.getPackageManager();
    }

    /** One entry per package. A TV activity takes precedence over a standard launcher activity. */
    public List<Entry> listInstalled() {
        Map<String, Entry> entries = new LinkedHashMap<>();
        collect(Intent.CATEGORY_LEANBACK_LAUNCHER, entries);
        collect(Intent.CATEGORY_LAUNCHER, entries);
        List<Entry> result = new ArrayList<>(entries.values());
        Collator collator = Collator.getInstance();
        result.sort((a, b) -> {
            int order = collator.compare(a.label, b.label);
            return order != 0 ? order : a.packageName.compareTo(b.packageName);
        });
        return result;
    }

    /** Rechecks availability at launch time; returns false and shows a brief message on failure. */
    public boolean launch(Entry entry) {
        if (entry == null) return unavailable();
        // A saved picker entry may no longer be installed, enabled or exported.
        for (Entry current : listInstalled()) {
            if (current.packageName.equals(entry.packageName)) {
                return start(current.component);
            }
        }
        return unavailable();
    }

    /** Useful for favorites restored from persistent package names. */
    public boolean launch(String packageName) {
        if (packageName != null) {
            for (Entry entry : listInstalled()) {
                if (entry.packageName.equals(packageName)) return start(entry.component);
            }
        }
        return unavailable();
    }

    private void collect(String category, Map<String, Entry> entries) {
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(category);
        for (ResolveInfo resolved : packages.queryIntentActivities(query, 0)) {
            ActivityInfo activity = resolved.activityInfo;
            if (activity == null || activity.applicationInfo == null
                    || activity.packageName == null || activity.name == null
                    || activity.packageName.equals(context.getPackageName())
                    || !activity.enabled || !activity.exported
                    || !activity.applicationInfo.enabled
                    || entries.containsKey(activity.packageName)) continue;
            CharSequence title = resolved.loadLabel(packages);
            String label = title == null || title.length() == 0
                    ? activity.packageName : title.toString();
            entries.put(activity.packageName, new Entry(activity.packageName, label,
                    resolved.loadIcon(packages),
                    new ComponentName(activity.packageName, activity.name)));
        }
    }

    private boolean start(ComponentName component) {
        Intent intent = new Intent(Intent.ACTION_MAIN).setComponent(component);
        if (!(context instanceof Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException | SecurityException exception) {
            return unavailable();
        }
    }

    private boolean unavailable() {
        Toast.makeText(context, "App unavailable. Try opening it from Fire TV Home.",
                Toast.LENGTH_SHORT).show();
        return false;
    }
}
