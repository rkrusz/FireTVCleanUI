package dev.krusz.scenichome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.content.SharedPreferences;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public final class FavoritesStoreTest {
    @Test
    public void saveAndLoadRoundTripPreservesOrderAcrossStoreInstances() {
        FakePreferences preferences = new FakePreferences();
        List<String> pins = new ArrayList<>(Arrays.asList("com.third", "com.first", "com.second"));
        new FavoritesStore(preferences.instance()).save(pins);
        assertEquals("[\"com.third\",\"com.first\",\"com.second\"]", preferences.rawPins());
        assertEquals(pins, new FavoritesStore(preferences.instance()).load());
        assertEquals(pins, new FavoritesStore(preferences.instance()).load());
        assertImmutable(new FavoritesStore(preferences.instance()).load());
    }

    @Test
    public void saveDropsDuplicatesAndInvalidEntriesAndCapsAtEight() {
        FakePreferences preferences = new FakePreferences();
        List<String> input = new ArrayList<>(Arrays.asList(null, "bad..pkg", "a", "a", "b", "bad/pkg", "c", "d", "e", "f", "g", "h", "i"));
        new FavoritesStore(preferences.instance()).save(input);
        List<String> expected = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
        assertEquals(expected, new FavoritesStore(preferences.instance()).load());
        assertEquals("[\"a\",\"b\",\"c\",\"d\",\"e\",\"f\",\"g\",\"h\"]", preferences.rawPins());
        assertEquals(13, input.size());
    }

    @Test
    public void missingPreferenceLoadsEmptyAndSavingNullStoresEmptyArray() {
        FakePreferences preferences = new FakePreferences();
        FavoritesStore store = new FavoritesStore(preferences.instance());
        assertEquals(Collections.emptyList(), store.load());
        assertImmutable(store.load());
        store.save(null);
        assertEquals("[]", preferences.rawPins());
        assertEquals(Collections.emptyList(), store.load());
    }

    @Test
    public void malformedJsonLoadsEmpty() {
        FakePreferences preferences = new FakePreferences();
        preferences.setRawPins("[\"a\",");
        assertEquals(Collections.emptyList(), new FavoritesStore(preferences.instance()).load());
        preferences.setRawPins("{\"not\":\"an array\"}");
        assertEquals(Collections.emptyList(), new FavoritesStore(preferences.instance()).load());
    }

    @Test
    public void wrongPreferenceTypeLoadsEmpty() {
        FakePreferences preferences = new FakePreferences();
        preferences.setRawPins(Integer.valueOf(42));
        assertEquals(Collections.emptyList(), new FavoritesStore(preferences.instance()).load());
    }

    @Test
    public void partiallyValidArrayKeepsValidStringsInOrderAndCapsAtEight() {
        FakePreferences preferences = new FakePreferences();
        preferences.setRawPins("[null,5,true,{},\"bad..pkg\",\"a\",\"a\",\"b\",\"c\",\"d\",\"e\",\"f\",\"g\",\"h\",\"i\"]");
        List<String> loaded = new FavoritesStore(preferences.instance()).load();
        assertEquals(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h"), loaded);
        assertImmutable(loaded);
    }

    private static void assertImmutable(List<String> pins) {
        try {
            pins.add("other");
            throw new AssertionError("loaded pins were mutable");
        } catch (UnsupportedOperationException expected) {
            assertFalse(pins.contains("other"));
        }
    }

    private static final class FakePreferences implements InvocationHandler {
        private final Map<String, Object> values = new HashMap<>();
        private final SharedPreferences preferences = (SharedPreferences) Proxy.newProxyInstance(
                SharedPreferences.class.getClassLoader(), new Class<?>[] { SharedPreferences.class }, this);

        SharedPreferences instance() {
            return preferences;
        }

        void setRawPins(Object value) {
            values.put(FavoritesStore.KEY_PINS, value);
        }

        String rawPins() {
            return (String) values.get(FavoritesStore.KEY_PINS);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getName().equals("getString")) {
                Object value = values.get(args[0]);
                return value == null ? args[1] : (String) value;
            }
            if (method.getName().equals("edit")) {
                return editor();
            }
            throw new UnsupportedOperationException("Unexpected SharedPreferences call: " + method.getName());
        }

        private SharedPreferences.Editor editor() {
            final Map<String, Object> pending = new HashMap<>();
            return (SharedPreferences.Editor) Proxy.newProxyInstance(
                    SharedPreferences.Editor.class.getClassLoader(),
                    new Class<?>[] { SharedPreferences.Editor.class },
                    (proxy, method, args) -> {
                        if (method.getName().equals("putString")) {
                            pending.put((String) args[0], args[1]);
                            return proxy;
                        }
                        if (method.getName().equals("apply")) {
                            values.putAll(pending);
                            return null;
                        }
                        throw new UnsupportedOperationException("Unexpected Editor call: " + method.getName());
                    });
        }
    }
}
