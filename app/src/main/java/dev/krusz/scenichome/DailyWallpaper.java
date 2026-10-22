package dev.krusz.scenichome;

/** Pure selection policy for bundled scenic wallpapers. */
public final class DailyWallpaper {
    private DailyWallpaper() {
    }

    /** Returns the daily index, or -1 if no wallpapers are available. */
    public static int indexForDay(long epochDay, int count) {
        return count > 0 ? Math.floorMod(epochDay, count) : -1;
    }

    /** Returns a valid frozen index, otherwise the daily index; -1 means none available. */
    public static int selectedIndex(long epochDay, int count, int frozenIndex) {
        if (count <= 0) {
            return -1;
        }
        return frozenIndex >= 0 && frozenIndex < count
                ? frozenIndex : indexForDay(epochDay, count);
    }
}
