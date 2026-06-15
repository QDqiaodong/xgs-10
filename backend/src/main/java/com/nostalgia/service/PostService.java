package com.nostalgia.service;

import com.nostalgia.entity.Category;
import com.nostalgia.entity.Era;
import com.nostalgia.entity.Post;
import com.nostalgia.entity.TimelineEvent;
import com.nostalgia.repository.CategoryRepository;
import com.nostalgia.repository.EraRepository;
import com.nostalgia.repository.PostRepository;
import com.nostalgia.repository.TimelineEventRepository;
import com.nostalgia.util.TextCleaner;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final EraRepository eraRepository;
    private final TimelineEventRepository timelineEventRepository;

    private final String uploadPath = "/app/uploads";

    public Page<Post> getPosts(Long categoryId, Long eraId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts;

        if (categoryId != null && eraId != null) {
            posts = postRepository.findByCategoryIdAndEraIdOrderByCreatedAtDesc(categoryId, eraId, pageable);
        } else if (categoryId != null) {
            posts = postRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
        } else if (eraId != null) {
            posts = postRepository.findByEraIdOrderByCreatedAtDesc(eraId, pageable);
        } else {
            posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        posts.forEach(this::populateCategoryAndEraNames);
        return posts;
    }

    @Cacheable(value = "hotPosts", key = "'top10'")
    public List<Post> getHotPosts() {
        List<Post> posts = postRepository.findTop10ByOrderByViewCountDesc();
        posts.forEach(this::populateCategoryAndEraNames);
        return posts;
    }

    @Transactional
    public Post getPostById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("帖子不存在"));
        postRepository.incrementViewCount(id);
        populateCategoryAndEraNames(post);
        populateTimelineEvents(post);
        return post;
    }

    @CacheEvict(value = {"hotPosts"}, allEntries = true)
    @Transactional
    public Post createPost(Post post, List<MultipartFile> images) throws IOException {
        return createPost(post, images, null);
    }

    @CacheEvict(value = {"hotPosts"}, allEntries = true)
    @Transactional
    public Post createPost(Post post, List<MultipartFile> images, List<TimelineEvent> timelineEvents) throws IOException {
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            for (MultipartFile image : images) {
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                Path filePath = uploadDir.resolve(fileName);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                imageUrls.add("/uploads/" + fileName);
            }
            post.setImages(imageUrls);
        }

        Post savedPost = postRepository.save(post);

        if (timelineEvents != null && !timelineEvents.isEmpty()) {
            for (int i = 0; i < timelineEvents.size(); i++) {
                TimelineEvent event = timelineEvents.get(i);
                event.setPostId(savedPost.getId());
                event.setSortOrder(i);
            }
            timelineEventRepository.saveAll(timelineEvents);
        }

        return savedPost;
    }

    private void populateCategoryAndEraNames(Post post) {
        if (post.getItemName() != null) {
            post.setItemName(TextCleaner.safeItemName(post.getItemName()));
        }
        if (post.getTitle() != null) {
            String cleanedTitle = TextCleaner.cleanTitle(post.getTitle());
            if (!cleanedTitle.isEmpty()) {
                post.setTitle(cleanedTitle);
            }
        }
        if (post.getCategoryId() != null) {
            categoryRepository.findById(post.getCategoryId())
                .map(Category::getName)
                .ifPresent(post::setCategoryName);
        }
        if (post.getEraId() != null) {
            eraRepository.findById(post.getEraId())
                .map(Era::getName)
                .ifPresent(post::setEraName);
        }
    }

    private void populateTimelineEvents(Post post) {
        List<TimelineEvent> events = timelineEventRepository.findByPostIdOrderByEventDateAscSortOrderAsc(post.getId());
        post.setTimelineEvents(events);
    }
}
