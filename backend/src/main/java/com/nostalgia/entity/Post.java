package com.nostalgia.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.nostalgia.util.PostImageListDeserializer;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String story;

    @Column(columnDefinition = "TEXT")
    private String memory;

    @Column(name = "era_background", columnDefinition = "TEXT")
    private String eraBackground;

    @Column(name = "current_status", columnDefinition = "TEXT")
    private String currentStatus;

    @Column(name = "preservation_status", length = 50)
    private String preservationStatus;

    @Column(name = "usage_scene", length = 100)
    private String usageScene;

    @Column(name = "story_summary", length = 500)
    private String storySummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    @JsonDeserialize(using = PostImageListDeserializer.class)
    private List<PostImage> images;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "era_id", nullable = false)
    private Long eraId;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "comment_count")
    private Integer commentCount = 0;

    @Column(name = "favorite_count")
    private Integer favoriteCount = 0;

    @Column(name = "author_name", length = 50)
    private String authorName = "匿名用户";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private String categoryName;

    @Transient
    private String eraName;

    @Transient
    private List<TimelineEvent> timelineEvents;
}
