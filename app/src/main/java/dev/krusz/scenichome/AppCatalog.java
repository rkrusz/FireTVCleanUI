package dev.krusz.scenichome;

import android.content.ActivityNotFoundException;
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

        private Entry(String packageName, String label, Drawable icon) {
            this.packageName = packageName;
            this.label = label;
            this.icon = icon;
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
        return launch(entry == null ? null : entry.packageName);
    }

    /** Useful for favorites restored from persistent package names. */
    public boolean launch(String packageName) {
        if (packageName == null || packageName.equals(context.getPackageName())) return unavailable();
        try {
            // PackageManager returns explicit launcher activities, preferring the TV entry point.
            Intent intent = packages.getLeanbackLaunchIntentForPackage(packageName);
            if (!isAvailableLauncher(intent, packageName)) {
                intent = packages.getLaunchIntentForPackage(packageName);
            }
            if (!isAvailableLauncher(intent, packageName)) return unavailable();
            // Keep the launched app out of Scenic Home's task even when context is an Activity.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException | SecurityException exception) {
            return unavailable();
        }
    }

    private boolean isAvailableLauncher(Intent intent, String packageName) {
        if (intent == null || intent.getComponent() == null
                || !packageName.equals(intent.getComponent().getPackageName())) return false;
        try {
            ActivityInfo activity = packages.getActivityInfo(intent.getComponent(), 0);
            return activity.enabled && activity.exported && activity.applicationInfo != null
                    && activity.applicationInfo.enabled;
        } catch (PackageManager.NameNotFoundException | SecurityException exception) {
            return false;
        }
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
                    resolved.loadIcon(packages)));
        }
    }

    private boolean unavailable() {
        Toast.makeText(context, "App unavailable. Try opening it from Fire TV Home.",
                Toast.LENGTH_SHORT).show();
        return false;
    }
}
