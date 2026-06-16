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
                postImage.setIsMain(i == 0);
                postImage.setSortOrder(i);
            } else if (item.isObject()) {
                if (item.has("url")) {
                    postImage.setUrl(item.get("url").asText());
                }
                if (item.has("width")) {
                    postImage.setWidth(item.get("width").asInt());
                }
                if (item.has("height")) {
                    postImage.setHeight(item.get("height").asInt());
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
            }

            if (postImage.getUrl() != null && !postImage.getUrl().isEmpty()) {
                images.add(postImage);
            }
        }

        return images;
    }
}
