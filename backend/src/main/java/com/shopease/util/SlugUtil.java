package com.shopease.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SlugUtil {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern NOT_ALLOWED = Pattern.compile("[^\\w-]");

    private SlugUtil() {
    }

    /** "Wireless Mouse (Black)" -> "wireless-mouse-black" */
    public static String toSlug(String input) {
        String withDashes = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(withDashes, Normalizer.Form.NFD);
        String slug = NOT_ALLOWED.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH)
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }
}
