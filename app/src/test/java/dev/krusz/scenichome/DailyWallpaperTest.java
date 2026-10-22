package dev.krusz.scenichome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public final class DailyWallpaperTest {
    @Test
    public void epochDayMapsToExpectedIndex() {
        assertEquals(4, DailyWallpaper.indexForDay(100, 8));
        assertEquals(5, DailyWallpaper.indexForDay(101, 8));
    }

    @Test
    public void nextDayRotatesAndWraps() {
        assertNotEquals(DailyWallpaper.indexForDay(100, 8), DailyWallpaper.indexForDay(101, 8));
        assertEquals(0, DailyWallpaper.indexForDay(8, 8));
        assertEquals(0, DailyWallpaper.indexForDay(-8, 8));
        assertEquals(7, DailyWallpaper.indexForDay(-1, 8));
    }

    @Test
    public void frozenSelectionOverridesDay() {
        assertEquals(3, DailyWallpaper.selectedIndex(100, 8, 3));
        assertEquals(3, DailyWallpaper.selectedIndex(101, 8, 3));
        assertEquals(0, DailyWallpaper.selectedIndex(101, 8, 0));
    }

    @Test
    public void invalidFrozenSelectionFallsBackToDaily() {
        assertEquals(4, DailyWallpaper.selectedIndex(100, 8, -1));
        assertEquals(4, DailyWallpaper.selectedIndex(100, 8, -2));
        assertEquals(4, DailyWallpaper.selectedIndex(100, 8, 8));
        assertEquals(5, DailyWallpaper.selectedIndex(101, 8, Integer.MAX_VALUE));
    }

    @Test
    public void nonPositiveImageCountReturnsNoSelection() {
        assertEquals(-1, DailyWallpaper.indexForDay(100, 0));
        assertEquals(-1, DailyWallpaper.indexForDay(100, -8));
        assertEquals(-1, DailyWallpaper.selectedIndex(100, 0, 0));
        assertEquals(-1, DailyWallpaper.selectedIndex(100, -8, 3));
    }

    @Test
    public void extremeEpochDaysAndSingleImageAreSafe() {
        assertEquals(0, DailyWallpaper.indexForDay(Long.MIN_VALUE, 8));
        assertEquals(7, DailyWallpaper.indexForDay(Long.MAX_VALUE, 8));
        assertEquals(0, DailyWallpaper.selectedIndex(Long.MAX_VALUE, 1, -1));
    }
}
