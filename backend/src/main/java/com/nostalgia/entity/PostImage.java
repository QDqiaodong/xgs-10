package com.nostalgia.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostImage {

    private String url;

    private String originalUrl;

    private String compressedUrl;

    private String thumbnailUrl;

    private Integer width;

    private Integer height;

    private Integer originalWidth;

    private Integer originalHeight;

    private Boolean isMain = false;

    private Integer sortOrder = 0;

    private Double displayRatio;

    private String format;

    private Long fileSize;

    private Long compressedFileSize;

    private String processingStatus;

    public PostImage(String url) {
        this.url = url;
        this.isMain = false;
        this.sortOrder = 0;
    }

    public PostImage(String url, int sortOrder) {
        this.url = url;
        this.isMain = sortOrder == 0;
        this.sortOrder = sortOrder;
    }

    public PostImage(String url, Integer width, Integer height, Boolean isMain, Integer sortOrder) {
        this.url = url;
        this.width = width;
        this.height = height;
        this.isMain = isMain;
        this.sortOrder = sortOrder;
        if (width != null && height != null && height != 0) {
            this.displayRatio = (double) width / height;
        }
    }
}
