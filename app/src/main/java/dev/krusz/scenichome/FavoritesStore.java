package dev.krusz.scenichome;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;

/** Ordered pin persistence; never filters pins by installed-app availability. */
public final class FavoritesStore {
    public static final String PREFS_NAME = "scenic_home";
    public static final String KEY_PINS = "favorite_packages";

    private final SharedPreferences preferences;

    public FavoritesStore(Context context) {
        this(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE));
    }

    public FavoritesStore(SharedPreferences preferences) {
        if (preferences == null) {
            throw new IllegalArgumentException("preferences must not be null");
        }
        this.preferences = preferences;
    }

    /** Returns an immutable sanitized ordered list, empty for missing or corrupt data. */
    public List<String> load() {
        final String stored;
        try {
            stored = preferences.getString(KEY_PINS, null);
        } catch (ClassCastException malformedPreferenceType) {
            return Favorites.sanitize(null);
        }
        if (stored == null) {
            return Favorites.sanitize(null);
        }
        try {
            JSONArray array = new JSONArray(stored);
            List<String> packages = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                Object item = array.opt(i);
                if (item instanceof String) {
                    packages.add((String) item);
                }
            }
            return Favorites.sanitize(packages);
        } catch (JSONException malformedJson) {
            return Favorites.sanitize(null);
        }
    }

    /** Stores up to eight unique valid packages in order, including currently unavailable apps. */
    public void save(List<String> packages) {
        JSONArray array = new JSONArray();
        for (String packageName : Favorites.sanitize(packages)) {
            array.put(packageName);
        }
        preferences.edit().putString(KEY_PINS, array.toString()).apply();
    }
}
