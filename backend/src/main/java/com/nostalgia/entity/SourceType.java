package com.nostalgia.entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum SourceType {
    FAMILY_HEIRLOOM("家庭传承", "🏠", "#8B4513", Arrays.asList("家庭传承", "祖传", "家传", "长辈留下", "父母留下", "祖辈", "传家宝", "代代相传")),
    FLEA_MARKET("旧货市场", "🏪", "#D2691E", Arrays.asList("旧货市场", "二手市场", "跳蚤市场", "古玩市场", "地摊", "旧货店", "二手店", "淘来的", "收来的", "废品站")),
    UNIT_RETAINED("单位留存", "🏢", "#4682B4", Arrays.asList("单位留存", "单位发的", "工厂发的", "公家的", "单位福利", "厂里发的", "机关", "学校", "工作单位", "单位遗留")),
    CHILDHOOD_ITEM("童年用品", "🧸", "#EE82EE", Arrays.asList("童年用品", "小时候的", "儿时的", "童年的", "小时候玩的", "小时候用的", "儿时回忆", "童年玩具", "学生时代", "小时候买的"));

    private final String label;
    private final String icon;
    private final String color;
    private final List<String> matchKeywords;

    SourceType(String label, String icon, String color, List<String> matchKeywords) {
        this.label = label;
        this.icon = icon;
        this.color = color;
        this.matchKeywords = matchKeywords;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    public List<String> getMatchKeywords() {
        return matchKeywords;
    }

    public static SourceType fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        for (SourceType type : values()) {
            if (type.label.equals(normalized) || type.name().equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        for (SourceType type : values()) {
            for (String keyword : type.matchKeywords) {
                if (normalized.contains(keyword)) {
                    return type;
                }
            }
        }
        return null;
    }

    public static List<String> getAllLabels() {
        return Arrays.stream(values())
                .map(SourceType::getLabel)
                .collect(Collectors.toList());
    }

    public static List<SourceType> getAllTypes() {
        return Arrays.asList(values());
    }
}
