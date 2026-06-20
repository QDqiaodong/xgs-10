package com.nostalgia.util;

public class PaginationUtils {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final int MIN_PAGE = 0;
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 100;

    public static int validatePage(int page) {
        if (page < MIN_PAGE) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    public static int validateSize(int size) {
        if (size < MIN_SIZE) {
            return DEFAULT_SIZE;
        }
        if (size > MAX_SIZE) {
            return MAX_SIZE;
        }
        return size;
    }

    public static int sanitizePage(Integer page) {
        if (page == null) {
            return DEFAULT_PAGE;
        }
        return validatePage(page);
    }

    public static int sanitizeSize(Integer size, int defaultSize) {
        if (size == null) {
            return defaultSize;
        }
        return validateSize(size);
    }

    public static int sanitizeSize(Integer size) {
        return sanitizeSize(size, DEFAULT_SIZE);
    }
}
