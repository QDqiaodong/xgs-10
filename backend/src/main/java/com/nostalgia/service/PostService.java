package com.nostalgia.service;

import com.nostalgia.entity.Category;
import com.nostalgia.entity.Era;
import com.nostalgia.entity.Post;
import com.nostalgia.entity.PostImage;
import com.nostalgia.entity.TimelineEvent;
import com.nostalgia.repository.CategoryRepository;
import com.nostalgia.repository.EraRepository;
import com.nostalgia.repository.PostRepository;
import com.nostalgia.repository.TimelineEventRepository;
import com.nostalgia.util.ImageInfo;
import com.nostalgia.util.StorySummaryExtractor;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final EraRepository eraRepository;
    private final TimelineEventRepository timelineEventRepository;

    private final String uploadPath = "/app/uploads";

    public Page<Post> getPosts(Long categoryId, Long eraId, String preservationStatus, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts;

        String normalizedStatus = normalizePreservationStatus(preservationStatus);

        boolean hasCategory = categoryId != null;
        boolean hasEra = eraId != null;
        boolean hasStatus = normalizedStatus != null;

        if (hasCategory && hasEra && hasStatus) {
            posts = postRepository.findByCategoryIdAndEraIdAndPreservationStatusOrderByCreatedAtDesc(
                    categoryId, eraId, normalizedStatus, pageable);
        } else if (hasCategory && hasEra) {
            posts = postRepository.findByCategoryIdAndEraIdOrderByCreatedAtDesc(categoryId, eraId, pageable);
        } else if (hasCategory && hasStatus) {
            posts = postRepository.findByCategoryIdAndPreservationStatusOrderByCreatedAtDesc(
                    categoryId, normalizedStatus, pageable);
        } else if (hasEra && hasStatus) {
            posts = postRepository.findByEraIdAndPreservationStatusOrderByCreatedAtDesc(
                    eraId, normalizedStatus, pageable);
        } else if (hasCategory) {
            posts = postRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
        } else if (hasEra) {
            posts = postRepository.findByEraIdOrderByCreatedAtDesc(eraId, pageable);
        } else if (hasStatus) {
            posts = postRepository.findByPreservationStatusOrderByCreatedAtDesc(normalizedStatus, pageable);
        } else {
            posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        posts.forEach(this::populateCategoryAndEraNames);
        posts.forEach(this::normalizeImages);
        posts.forEach(this::normalizePreservationStatus);
        return posts;
    }

    private String normalizePreservationStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return com.nostalgia.entity.PreservationStatus.fromString(status).getLabel();
    }

    private void normalizePreservationStatus(Post post) {
        if (post.getPreservationStatus() == null || post.getPreservationStatus().isBlank()) {
            post.setPreservationStatus(com.nostalgia.entity.PreservationStatus.UNKNOWN.getLabel());
        } else {
            post.setPreservationStatus(
                    com.nostalgia.entity.PreservationStatus.fromString(post.getPreservationStatus()).getLabel());
        }
    }

    @Cacheable(value = "hotPosts", key = "'top10'")
    public List<Post> getHotPosts() {
        List<Post> posts = postRepository.findTop10ByOrderByViewCountDesc();
        posts.forEach(this::populateCategoryAndEraNames);
        posts.forEach(this::normalizeImages);
        return posts;
    }

    @CacheEvict(value = {"hotPosts"}, allEntries = true)
    @Transactional
    public Post getPostById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("帖子不存在"));
        postRepository.incrementViewCount(id);
        post.setViewCount((post.getViewCount() == null ? 0 : post.getViewCount()) + 1);
        populateCategoryAndEraNames(post);
        populateTimelineEvents(post);
        normalizeImages(post);
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
            List<PostImage> postImages = new ArrayList<>();
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                Path filePath = uploadDir.resolve(fileName);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                PostImage postImage = new PostImage();
                postImage.setUrl("/api/uploads/" + fileName);
                postImage.setSortOrder(i);
                postImage.setIsMain(i == 0);

                ImageInfo imageInfo = ImageInfo.getImageInfo(image);
                if (imageInfo != null) {
                    postImage.setWidth(imageInfo.getWidth());
                    postImage.setHeight(imageInfo.getHeight());
                }

                postImages.add(postImage);
            }
            post.setImages(postImages);
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

        populateStorySummary(savedPost);
        populateCategoryAndEraNames(savedPost);
        normalizeImages(savedPost);
        return savedPost;
    }

    public void populateCategoryAndEraNamesExternal(Post post) {
        populateCategoryAndEraNames(post);
    }

    @CacheEvict(value = {"hotPosts"}, allEntries = true)
    @Transactional
    public int regenerateAllSummaries() {
        List<Post> allPosts = postRepository.findAll();
        int updated = 0;
        for (Post post : allPosts) {
            StorySummaryExtractor.ExtractionResult result = StorySummaryExtractor.extract(
                    post.getContent(),
                    post.getStory(),
                    post.getMemory(),
                    post.getEraBackground(),
                    post.getCurrentStatus(),
                    post.getUsageScene()
            );

            boolean changed = false;

            if (result.getItemSource() != null && !result.getItemSource().isBlank()) {
                post.setItemSource(result.getItemSource());
                changed = true;
            }
            if (result.getUsageScene() != null && !result.getUsageScene().isBlank()) {
                post.setUsageScene(result.getUsageScene());
                changed = true;
            }
            if (result.getEmotionKeywords() != null && !result.getEmotionKeywords().isEmpty()) {
                post.setEmotionKeywords(result.getEmotionKeywords());
                changed = true;
            }
            if (result.getSummaryText() != null && !result.getSummaryText().isBlank()) {
                post.setStorySummary(result.getSummaryText());
                changed = true;
            }

            if (changed) {
                postRepository.save(post);
                updated++;
            }
        }
        return updated;
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
        populateStorySummary(post);
    }

    private void populateStorySummary(Post post) {
        boolean needsExtraction = post.getItemSource() == null
                || post.getItemSource().isBlank()
                || post.getEmotionKeywords() == null
                || post.getEmotionKeywords().isEmpty()
                || post.getStorySummary() == null
                || post.getStorySummary().isBlank();

        if (!needsExtraction) {
            return;
        }

        StorySummaryExtractor.ExtractionResult result = StorySummaryExtractor.extract(
                post.getContent(),
                post.getStory(),
                post.getMemory(),
                post.getEraBackground(),
                post.getCurrentStatus(),
                post.getUsageScene()
        );

        if (post.getItemSource() == null || post.getItemSource().isBlank()) {
            post.setItemSource(result.getItemSource());
        }

        if (post.getUsageScene() == null || post.getUsageScene().isBlank()) {
            post.setUsageScene(result.getUsageScene());
        }

        if (post.getEmotionKeywords() == null || post.getEmotionKeywords().isEmpty()) {
            post.setEmotionKeywords(result.getEmotionKeywords());
        }

        if (post.getStorySummary() == null || post.getStorySummary().isBlank()) {
            post.setStorySummary(result.getSummaryText());
        }
    }

    private void populateTimelineEvents(Post post) {
        List<TimelineEvent> events = timelineEventRepository.findByPostIdOrderByEventDateAscSortOrderAsc(post.getId());
        post.setTimelineEvents(events);
    }

    private void normalizeImages(Post post) {
        if (post.getImages() == null || post.getImages().isEmpty()) {
            return;
        }

        List<PostImage> images = post.getImages();

        for (int i = 0; i < images.size(); i++) {
            PostImage img = images.get(i);
            if (img.getSortOrder() == null) {
                img.setSortOrder(i);
            }
            if (img.getIsMain() == null) {
                img.setIsMain(i == 0);
            }
        }

        List<PostImage> sortedImages = images.stream()
            .sorted(Comparator.comparingInt(img -> img.getSortOrder() != null ? img.getSortOrder() : 0))
            .collect(Collectors.toList());

        boolean hasMain = sortedImages.stream().anyMatch(img -> Boolean.TRUE.equals(img.getIsMain()));
        if (!hasMain && !sortedImages.isEmpty()) {
            sortedImages.get(0).setIsMain(true);
        }

        post.setImages(sortedImages);
    }
}
