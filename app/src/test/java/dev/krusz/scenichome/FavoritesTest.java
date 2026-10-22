package dev.krusz.scenichome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public final class FavoritesTest {
    @Test
    public void emptyAndNullListsAreImmutable() {
        assertEquals(Collections.emptyList(), Favorites.sanitize(null));
        List<String> empty = Favorites.sanitize(Collections.<String>emptyList());
        assertEquals(Collections.emptyList(), empty);
        assertImmutable(empty);
    }

    @Test
    public void pinAndUnpinPreserveOrderWithoutMutatingInput() {
        List<String> original = new ArrayList<>(Arrays.asList("a", "b"));
        List<String> pinned = Favorites.pin(original, "c");
        assertEquals(Arrays.asList("a", "b", "c"), pinned);
        assertEquals(Arrays.asList("a", "b"), original);
        assertEquals(Arrays.asList("a", "c"), Favorites.unpin(pinned, "b"));
        assertEquals(Arrays.asList("a", "b", "c"), pinned);
        assertImmutable(pinned);
        assertNotSame(original, Favorites.pin(original, "a"));
    }

    @Test
    public void duplicatesAndInvalidPinsAreIgnored() {
        List<String> first = Arrays.asList("a", "b");
        assertEquals(first, Favorites.pin(first, "a"));
        assertEquals(first, Favorites.pin(first, null));
        assertEquals(first, Favorites.pin(first, "  "));
        assertEquals(first, Favorites.pin(first, "com. bad"));
        assertEquals(first, Favorites.pin(first, "bad..pkg"));
        assertEquals(first, Favorites.pin(first, ".bad"));
        assertEquals(first, Favorites.pin(first, "bad."));
        assertEquals(first, Favorites.pin(first, "bad/pkg"));
        assertEquals(first, Favorites.unpin(first, "missing"));
        assertEquals(first, Favorites.unpin(first, null));
    }

    @Test
    public void maxEightPins() {
        List<String> pins = Favorites.sanitize(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i"));
        assertEquals(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h"), pins);
        assertEquals(pins, Favorites.pin(pins, "i"));
        assertEquals(8, Favorites.MAX_PINS);
        assertEquals(8, pins.size());
    }

    @Test
    public void moveBothDirectionsAndIgnoreInvalidIndices() {
        List<String> original = Arrays.asList("a", "b", "c", "d");
        assertEquals(Arrays.asList("b", "c", "a", "d"), Favorites.move(original, 0, 2));
        assertEquals(Arrays.asList("a", "d", "b", "c"), Favorites.move(original, 3, 1));
        assertEquals(original, Favorites.move(original, 0, 0));
        assertEquals(original, Favorites.move(original, -1, 2));
        assertEquals(original, Favorites.move(original, 1, 4));
        assertEquals(original, Favorites.move(original, 4, 0));
        assertImmutable(Favorites.move(original, 0, 0));
    }

    @Test
    public void sanitizeDropsInvalidAndDuplicatePackagesWithoutShiftingGoodOrder() {
        List<String> raw = new ArrayList<>(Arrays.asList(null, "", "  ", "a", "a", "com.example_1.app", "bad-pkg", "b", "c"));
        List<String> cleaned = Favorites.sanitize(raw);
        assertEquals(Arrays.asList("a", "com.example_1.app", "b", "c"), cleaned);
        assertEquals(9, raw.size());
        assertImmutable(cleaned);
        assertEquals(Collections.emptyList(), Favorites.unpin(null, "a"));
        assertEquals(Collections.singletonList("a"), Favorites.pin(null, "a"));
        assertEquals(Collections.emptyList(), Favorites.move(null, 0, 1));
    }

    private static void assertImmutable(List<String> list) {
        boolean rejected = false;
        try {
            list.add("other");
        } catch (UnsupportedOperationException expected) {
            rejected = true;
        }
        assertTrue(rejected);
        assertFalse(list.contains("other"));
    }
}
