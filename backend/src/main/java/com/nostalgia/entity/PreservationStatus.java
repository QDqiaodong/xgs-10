package com.nostalgia.entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum PreservationStatus {
    PERFECT("完好", "✓", "#2e7d32", Arrays.asList("完好", "完美", "正常", "全新", "如新", "保存", "功能完好", "运行良好", "可正常使用")),
    NEEDS_REPAIR("待修复", "🔧", "#f57c00", Arrays.asList("待修复", "需要修", "要修", "修理", "待修", "需维修", "需修理", "破损", "坏", "故障", "损坏", "不能用", "无法使用")),
    MISSING_PARTS("缺件", "⚠️", "#c62828", Arrays.asList("缺", "少", "丢失", "缺件", "缺少", "零件丢失", "配件缺失", "不完整")),
    UNDER_REPAIR("修复中", "⚙️", "#1565c0", Arrays.asList("修复中", "维修中", "正在修", "修理中", "维护中")),
    WORN("磨损", "📜", "#6d4c41", Arrays.asList("磨损", "旧", "锈", "划痕", "磨", "老化", "变旧", "使用痕迹", "陈旧", "略有锈迹", "有锈迹")),
    UNKNOWN("待评估", "❓", "#757575", Arrays.asList("待评估", "未知", "不清楚", "不确定", "待鉴定"));

    private final String label;
    private final String icon;
    private final String color;
    private final List<String> matchKeywords;

    PreservationStatus(String label, String icon, String color, List<String> matchKeywords) {
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

    public static PreservationStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        String normalized = value.trim();
        for (PreservationStatus status : values()) {
            if (status.label.equals(normalized) || status.name().equalsIgnoreCase(normalized)) {
                return status;
            }
        }
        for (PreservationStatus status : values()) {
            for (String keyword : status.matchKeywords) {
                if (normalized.contains(keyword)) {
                    return status;
                }
            }
        }
        return UNKNOWN;
    }

    public static List<String> getAllLabels() {
        return Arrays.stream(values())
                .map(PreservationStatus::getLabel)
                .collect(Collectors.toList());
    }

    public static List<PreservationStatus> getAllStatuses() {
        return Arrays.asList(values());
    }
}
