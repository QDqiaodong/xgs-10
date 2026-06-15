package com.nostalgia.util;

import java.util.regex.Pattern;

public class TextCleaner {

    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern REPEATED_PUNCTUATION = Pattern.compile("([，。！？、；：\"\"''（）【】《》,.!?;:'\"()\\[\\]<>])\\1+");
    private static final Pattern REPEATED_SEPARATORS = Pattern.compile("[\\s\\-_·.。,，、]{2,}");
    private static final Pattern LEADING_TRASH = Pattern.compile("^[\\s\\-_·.。,，、!！?？;；:：\"\"''（）【】《》()\\[\\]<>]+");
    private static final Pattern TRAILING_TRASH = Pattern.compile("[\\s\\-_·.。,，、!！?？;；:：\"\"''（）【】《》()\\[\\]<>]+$");

    public static String cleanItemName(String name) {
        if (name == null) {
            return "";
        }
        String cleaned = name.trim();
        cleaned = MULTIPLE_SPACES.matcher(cleaned).replaceAll(" ");
        cleaned = REPEATED_PUNCTUATION.matcher(cleaned).replaceAll("$1");
        cleaned = REPEATED_SEPARATORS.matcher(cleaned).replaceAll(mr -> {
            String m = mr.group();
            return m.contains(" ") ? " " : String.valueOf(m.charAt(0));
        });
        cleaned = LEADING_TRASH.matcher(cleaned).replaceAll("");
        cleaned = TRAILING_TRASH.matcher(cleaned).replaceAll("");
        cleaned = cleaned.trim();
        if (cleaned.length() < 2) {
            return "";
        }
        if (cleaned.length() > 50) {
            cleaned = cleaned.substring(0, 50);
        }
        return cleaned;
    }

    public static String cleanTitle(String title) {
        if (title == null) {
            return "";
        }
        String cleaned = title.trim();
        cleaned = MULTIPLE_SPACES.matcher(cleaned).replaceAll(" ");
        cleaned = REPEATED_PUNCTUATION.matcher(cleaned).replaceAll("$1");
        cleaned = cleaned.trim();
        if (cleaned.length() < 2) {
            return "";
        }
        if (cleaned.length() > 200) {
            cleaned = cleaned.substring(0, 200);
        }
        return cleaned;
    }

    public static String safeItemName(String name) {
        String cleaned = cleanItemName(name);
        return cleaned.isEmpty() ? "未知物件" : cleaned;
    }
}
