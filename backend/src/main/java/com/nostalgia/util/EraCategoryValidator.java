package com.nostalgia.util;

import com.nostalgia.entity.Category;
import com.nostalgia.entity.Era;

import java.util.Map;

public class EraCategoryValidator {

    private static final int DEFAULT_ERA_SORT_ORDER = 1;

    private static final Map<Integer, String> SORT_ORDER_TO_ERA_NAME = Map.of(
            1, "60年代",
            2, "70年代",
            3, "80年代",
            4, "90年代",
            5, "00年代"
    );

    private static final Map<String, Integer> MIN_ERA_SORT_ORDER_BY_CATEGORY = Map.of(
            "通讯工具", 4,
            "家用电器", 3,
            "影音设备", 2
    );

    private EraCategoryValidator() {
    }

    public static void validate(Era era, Category category) {
        if (era == null || category == null) {
            return;
        }
        String categoryName = category.getName();
        if (categoryName == null || categoryName.isBlank()) {
            return;
        }
        Integer minSortOrder = MIN_ERA_SORT_ORDER_BY_CATEGORY.get(categoryName);
        if (minSortOrder == null) {
            return;
        }
        int eraSortOrder = era.getSortOrder() == null ? DEFAULT_ERA_SORT_ORDER : era.getSortOrder();
        if (eraSortOrder < minSortOrder) {
            String earliestEraName = SORT_ORDER_TO_ERA_NAME.getOrDefault(minSortOrder, "更早年代");
            throw new IllegalArgumentException(
                    "年代与类别组合不合理：「" + categoryName + "」类物件最早见于"
                            + earliestEraName + "，请勿归入「" + era.getName() + "」，以免影响资料可信度。"
            );
        }
    }

    public static String getEarliestEraName(String categoryName) {
        if (categoryName == null) {
            return null;
        }
        Integer minSortOrder = MIN_ERA_SORT_ORDER_BY_CATEGORY.get(categoryName);
        if (minSortOrder == null) {
            return null;
        }
        return SORT_ORDER_TO_ERA_NAME.get(minSortOrder);
    }
}
