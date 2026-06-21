package com.nostalgia.entity;

import com.nostalgia.util.RestorationTypeConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "restoration_records")
public class RestorationRecord {

    public enum RestorationType {
        CLEANING("清洁"),
        REPLACEMENT("补件"),
        RUST_REMOVAL("除锈"),
        RENOVATION("翻新"),
        PAINTING("上漆"),
        REPAIR("维修"),
        MAINTENANCE("保养"),
        POLISHING("抛光"),
        WOOD_TREATMENT("木艺处理"),
        ELECTRONIC_REPAIR("电子维修"),
        CUSTOM("自定义");

        private final String label;

        RestorationType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Convert(converter = RestorationTypeConverter.class)
    @Column(name = "restoration_type", length = 30, nullable = false)
    private RestorationType restorationType;

    @Column(name = "custom_type", length = 50)
    private String customType;

    @Column(name = "restoration_date", nullable = false)
    private LocalDate restorationDate;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "before_image", length = 500)
    private String beforeImage;

    @Column(name = "after_image", length = 500)
    private String afterImage;

    @Column(length = 500)
    private String materials;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "restorer", length = 100)
    private String restorer;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "preservation_status_before", length = 50)
    private String preservationStatusBefore;

    @Column(name = "preservation_status_after", length = 50)
    private String preservationStatusAfter;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getTypeLabel() {
        if (restorationType == RestorationType.CUSTOM && customType != null) {
            return customType;
        }
        return restorationType != null ? restorationType.getLabel() : "";
    }
}
