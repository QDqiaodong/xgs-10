package com.nostalgia.dto;

import com.nostalgia.entity.Post;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class EraTimelineDTO {
    private Long id;
    private String name;
    private Integer yearStart;
    private Integer yearEnd;
    private String description;
    private String icon;
    private String colorScheme;
    private String representativeCategories;
    private Integer postCount;
    private List<Post> representativePosts;
    private Map<String, Long> byCategory;
    private Map<String, Long> byPreservationStatus;
}
