package com.nostalgia.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostImage {

    private String url;

    private Integer width;

    private Integer height;

    private Boolean isMain = false;

    private Integer sortOrder = 0;

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
}
