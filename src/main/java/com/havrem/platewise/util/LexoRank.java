package com.havrem.platewise.util;

/**
 * Lexicographic rank generator. Produces a string that sorts strictly between
 * two existing ranks (or at either end when a side is null). The alphabet is
 * lowercase a-z; ranks lengthen as needed when adjacent slots are subdivided.
 */
public final class LexoRank {
    private static final char MIN = 'a';
    private static final char MAX = 'z';

    private LexoRank() {}

    public static String between(String prev, String next) {
        if (prev != null && next != null && prev.compareTo(next) >= 0) {
            throw new IllegalArgumentException("prev must be < next");
        }

        StringBuilder out = new StringBuilder();
        int i = 0;
        while (true) {
            char left = (prev != null && i < prev.length()) ? prev.charAt(i) : MIN;
            char right = (next != null && i < next.length()) ? next.charAt(i) : (char) (MAX + 1);

            if (left == right) {
                out.append(left);
                i++;
                continue;
            }

            char mid = (char) ((left + right) / 2);
            if (mid == left) {
                out.append(left);
                i++;
                next = null;
                continue;
            }

            out.append(mid);
            return out.toString();
        }
    }
}
