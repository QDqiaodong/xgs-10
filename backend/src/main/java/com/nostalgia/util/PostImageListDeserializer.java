package com.nostalgia.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.nostalgia.entity.PostImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PostImageListDeserializer extends JsonDeserializer<List<PostImage>> {

    @Override
    public List<PostImage> deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        List<PostImage> images = new ArrayList<>();

        if (!node.isArray()) {
            return images;
        }

        for (int i = 0; i < node.size(); i++) {
            JsonNode item = node.get(i);
            PostImage postImage = new PostImage();

            if (item.isTextual()) {
                postImage.setUrl(item.asText());
                postImage.setOriginalUrl(item.asText());
                postImage.setCompressedUrl(item.asText());
                postImage.setThumbnailUrl(item.asText());
                postImage.setIsMain(i == 0);
                postImage.setSortOrder(i);
                postImage.setProcessingStatus("COMPLETED");
            } else if (item.isObject()) {
                if (item.has("url")) {
                    String url = item.get("url").asText();
                    postImage.setUrl(url);
                    if (!item.has("originalUrl")) {
                        postImage.setOriginalUrl(url);
                    }
                    if (!item.has("compressedUrl")) {
                        postImage.setCompressedUrl(url);
                    }
                    if (!item.has("thumbnailUrl")) {
                        postImage.setThumbnailUrl(url);
                    }
                }
                if (item.has("originalUrl")) {
                    postImage.setOriginalUrl(item.get("originalUrl").asText());
                }
                if (item.has("compressedUrl")) {
                    postImage.setCompressedUrl(item.get("compressedUrl").asText());
                }
                if (item.has("thumbnailUrl")) {
                    postImage.setThumbnailUrl(item.get("thumbnailUrl").asText());
                }
                if (item.has("width")) {
                    postImage.setWidth(item.get("width").asInt());
                }
                if (item.has("height")) {
                    postImage.setHeight(item.get("height").asInt());
                }
                if (item.has("originalWidth")) {
                    postImage.setOriginalWidth(item.get("originalWidth").asInt());
                }
                if (item.has("originalHeight")) {
                    postImage.setOriginalHeight(item.get("originalHeight").asInt());
                }
                if (item.has("isMain")) {
                    postImage.setIsMain(item.get("isMain").asBoolean());
                } else {
                    postImage.setIsMain(i == 0);
                }
                if (item.has("sortOrder")) {
                    postImage.setSortOrder(item.get("sortOrder").asInt());
                } else {
                    postImage.setSortOrder(i);
                }
                if (item.has("displayRatio")) {
                    postImage.setDisplayRatio(item.get("displayRatio").asDouble());
                }
                if (item.has("format")) {
                    postImage.setFormat(item.get("format").asText());
                }
                if (item.has("fileSize")) {
                    postImage.setFileSize(item.get("fileSize").asLong());
                }
                if (item.has("compressedFileSize")) {
                    postImage.setCompressedFileSize(item.get("compressedFileSize").asLong());
                }
                if (item.has("processingStatus")) {
                    postImage.setProcessingStatus(item.get("processingStatus").asText());
                } else {
                    postImage.setProcessingStatus("COMPLETED");
                }
            }

            if (postImage.getUrl() != null && !postImage.getUrl().isEmpty()) {
                images.add(postImage);
            }
        }

        return images;
    }
}
