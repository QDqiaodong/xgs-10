package com.nostalgia.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DetailedStatsDTO {
    private Long totalPosts;
    private Integer totalEras;
    private Integer totalCategories;
    private Integer totalPreservationStatuses;

    private Map<String, Long> eraDistribution;
    private Map<String, Long> categoryDistribution;
    private Map<String, Long> preservationStatusDistribution;

    private List<EraStatsDTO> eraStats;
    private List<CategoryStatsDTO> categoryStats;
    private List<PreservationStatusStatsDTO> preservationStatusStats;

    @Data
    public static class EraStatsDTO {
        private Long eraId;
        private String eraName;
        private Integer yearStart;
        private Integer yearEnd;
        private Long totalCount;
        private Map<String, Long> byCategory;
        private Map<String, Long> byPreservationStatus;
    }

    @Data
    public static class CategoryStatsDTO {
        private Long categoryId;
        private String categoryName;
        private String categoryIcon;
        private Long totalCount;
        private Map<String, Long> byEra;
        private Map<String, Long> byPreservationStatus;
    }

    @Data
    public static class PreservationStatusStatsDTO {
        private String status;
        private String label;
        private String icon;
        private String color;
        private Long totalCount;
        private Map<String, Long> byCategory;
        private Map<String, Long> byEra;
    }
}
