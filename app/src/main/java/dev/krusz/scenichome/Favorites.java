package dev.krusz.scenichome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Pure, immutable ordered package-name pin operations. */
public final class Favorites {
    public static final int MAX_PINS = 8;

    private Favorites() {
    }

    /** Keeps the first eight unique, valid packages in their original order. */
    public static List<String> sanitize(List<String> packages) {
        if (packages == null || packages.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(MAX_PINS);
        Set<String> seen = new HashSet<>();
        for (String packageName : packages) {
            if (isValidPackage(packageName) && seen.add(packageName)) {
                result.add(packageName);
                if (result.size() == MAX_PINS) {
                    break;
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    /** Appends a package if valid, not already pinned, and room remains. */
    public static List<String> pin(List<String> packages, String packageName) {
        List<String> result = sanitize(packages);
        if (!isValidPackage(packageName) || result.contains(packageName) || result.size() == MAX_PINS) {
            return result;
        }
        List<String> updated = new ArrayList<>(result);
        updated.add(packageName);
        return Collections.unmodifiableList(updated);
    }

    /** Removes a package, if present, while preserving all other pins' order. */
    public static List<String> unpin(List<String> packages, String packageName) {
        List<String> result = sanitize(packages);
        if (!result.contains(packageName)) {
            return result;
        }
        List<String> updated = new ArrayList<>(result);
        updated.remove(packageName);
        return Collections.unmodifiableList(updated);
    }

    /** Moves the pin at fromIndex to toIndex; out-of-range positions are ignored. */
    public static List<String> move(List<String> packages, int fromIndex, int toIndex) {
        List<String> result = sanitize(packages);
        if (fromIndex < 0 || toIndex < 0 || fromIndex >= result.size()
                || toIndex >= result.size() || fromIndex == toIndex) {
            return result;
        }
        List<String> updated = new ArrayList<>(result);
        updated.add(toIndex, updated.remove(fromIndex));
        return Collections.unmodifiableList(updated);
    }

    private static boolean isValidPackage(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        boolean segmentStart = true;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '.') {
                if (segmentStart) {
                    return false;
                }
                segmentStart = true;
            } else if (c == '_' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
                segmentStart = false;
            } else if (c >= '0' && c <= '9' && !segmentStart) {
                // Digits may follow a segment's initial letter or underscore.
            } else {
                return false;
            }
        }
        return !segmentStart;
    }
}
