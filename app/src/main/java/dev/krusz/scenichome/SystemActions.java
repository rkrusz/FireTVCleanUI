package dev.krusz.scenichome;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.provider.Settings;

/** Best-effort system shortcuts. A false result requires an explanatory UI fallback. */
public final class SystemActions {
    private final Context context;

    public SystemActions(Context context) {
        this.context = context;
    }

    /** Opens Android/Fire TV settings if the device exposes the standard Settings action. */
    public boolean openSettings() {
        return startResolved(new Intent(Settings.ACTION_SETTINGS));
    }

    /** Tries the device's default HOME handler; use the physical Home key if blocked. */
    public boolean openAmazonHome() {
        // No Amazon-specific package is assumed. On Fire TV the stock HOME handler is
        // expected to be Amazon Home, but this must be checked on the target device.
        return startResolved(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
    }

    /** No portable Live TV launch intent is known for the target Fire TV firmware. */
    public boolean openLiveTv() {
        // The UI should point to the remote's Live TV button or Fire TV Home instead.
        return false;
    }

    /** Never substitute input-method settings for a real HDMI input selector. */
    public boolean openInputs() {
        // The UI should point to the physical Inputs key or Fire TV settings instead.
        return false;
    }

    private boolean startResolved(Intent intent) {
        try {
            ResolveInfo resolved = context.getPackageManager().resolveActivity(intent, 0);
            ActivityInfo activity = resolved == null ? null : resolved.activityInfo;
            if (activity == null || activity.packageName == null || activity.name == null
                    || !activity.enabled || !activity.exported
                    || activity.packageName.equals(context.getPackageName())) return false;
            // Use the resolved component, not a guess at a vendor package name.
            intent.setClassName(activity.packageName, activity.name);
            if (!(context instanceof Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException | SecurityException exception) {
            return false;
        }
    }
}
