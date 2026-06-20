package com.nostalgia.entity;

import com.nostalgia.util.EventTypeConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "timeline_events")
public class TimelineEvent {

    public enum EventType {
        ACQUISITION,
        USAGE,
        IDLE,
        DISPOSAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Convert(converter = EventTypeConverter.class)
    @Column(name = "event_type", length = 20)
    private EventType eventType;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String location;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
