package com.nostalgia.dto;

import com.nostalgia.entity.Post;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CategoryShowcaseDTO {
    private Long id;
    private String name;
    private String icon;
    private Integer sortOrder;
    private String description;
    private Integer postCount;
    private List<Post> representativePosts;
    private Map<String, Long> byEra;
    private Map<String, Long> byPreservationStatus;
}
