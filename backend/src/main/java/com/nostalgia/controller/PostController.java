package com.nostalgia.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nostalgia.entity.Post;
import com.nostalgia.entity.TimelineEvent;
import com.nostalgia.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<Page<Post>> getPosts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long eraId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getPosts(categoryId, eraId, page, size));
    }

    @GetMapping("/hot")
    public ResponseEntity<List<Post>> getHotPosts() {
        return ResponseEntity.ok(postService.getHotPosts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Post> createPost(
            @RequestParam String title,
            @RequestParam String itemName,
            @RequestParam String content,
            @RequestParam(required = false) String story,
            @RequestParam(required = false) String memory,
            @RequestParam Long categoryId,
            @RequestParam Long eraId,
            @RequestParam(required = false) String authorName,
            @RequestParam(required = false) String timelineEvents,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws Exception {
        Post post = new Post();
        post.setTitle(title);
        post.setItemName(itemName);
        post.setContent(content);
        post.setStory(story);
        post.setMemory(memory);
        post.setCategoryId(categoryId);
        post.setEraId(eraId);
        post.setAuthorName(authorName != null && !authorName.isBlank() ? authorName : "匿名用户");

        List<TimelineEvent> events = null;
        if (timelineEvents != null && !timelineEvents.isBlank()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            events = mapper.readValue(timelineEvents, new TypeReference<List<TimelineEvent>>() {});
        }

        return ResponseEntity.ok(postService.createPost(post, images, events));
    }
}
