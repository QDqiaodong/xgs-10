package com.nostalgia.entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum PreservationStatus {
    PERFECT("完好", "✓", "#2e7d32"),
    NEEDS_REPAIR("待修复", "🔧", "#f57c00"),
    MISSING_PARTS("缺件", "⚠️", "#c62828"),
    UNDER_REPAIR("修复中", "⚙️", "#1565c0"),
    WORN("磨损", "📜", "#6d4c41"),
    UNKNOWN("待评估", "❓", "#757575");

    private final String label;
    private final String icon;
    private final String color;

    PreservationStatus(String label, String icon, String color) {
        this.label = label;
        this.icon = icon;
        this.color = color;
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
        if (normalized.contains("完好") || normalized.contains("完美") || normalized.contains("正常")) {
            return PERFECT;
        }
        if (normalized.contains("修复") && normalized.contains("中")) {
            return UNDER_REPAIR;
        }
        if (normalized.contains("修复") || normalized.contains("修理")) {
            return NEEDS_REPAIR;
        }
        if (normalized.contains("缺") || normalized.contains("少") || normalized.contains("丢失")) {
            return MISSING_PARTS;
        }
        if (normalized.contains("磨损") || normalized.contains("旧") || normalized.contains("锈")) {
            return WORN;
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
