package com.nostalgia.service;

import com.nostalgia.dto.DetailedStatsDTO;
import com.nostalgia.entity.Category;
import com.nostalgia.entity.Era;
import com.nostalgia.entity.Post;
import com.nostalgia.entity.PostImage;
import com.nostalgia.entity.PreservationStatus;
import com.nostalgia.entity.SourceType;
import com.nostalgia.entity.TimelineEvent;
import com.nostalgia.repository.CategoryRepository;
import com.nostalgia.repository.EraRepository;
import com.nostalgia.repository.PostRepository;
import com.nostalgia.repository.TimelineEventRepository;
import com.nostalgia.util.ImageInfo;
import com.nostalgia.util.StorySummaryExtractor;
import com.nostalgia.util.TextCleaner;
import com.nostalgia.util.EraCategoryValidator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nostalgia.config.GlobalExceptionHandler;
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
    private final ImageProcessingService imageProcessingService;

    private final String uploadPath = "/app/uploads";

    public Page<Post> getPosts(Long categoryId, Long eraId, String preservationStatus, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Post> spec = buildSpecification(categoryId, eraId, preservationStatus);

        Page<Post> posts = postRepository.findAll(spec, pageable);

        posts.forEach(this::populateCategoryAndEraNames);
        posts.forEach(this::normalizeImages);
        posts.forEach(this::normalizePreservationStatus);
        return posts;
    }

    public List<Post> getPostsByEra(Long categoryId, Long eraId, String preservationStatus) {
        Specification<Post> spec = buildSpecification(categoryId, eraId, preservationStatus);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<Post> posts = postRepository.findAll(spec, sort);
        posts.forEach(this::populateCategoryAndEraNames);
        posts.forEach(this::normalizeImages);
        posts.forEach(this::normalizePreservationStatus);
        return posts;
    }

    private Specification<Post> buildSpecification(Long categoryId, Long eraId, String preservationStatus) {
        return (Root<Post> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }

            if (eraId != null) {
                predicates.add(cb.equal(root.get("eraId"), eraId));
            }

            if (preservationStatus != null && !preservationStatus.isBlank()) {
                PreservationStatus targetStatus = PreservationStatus.fromString(preservationStatus);
                List<String> keywords = targetStatus.getMatchKeywords();

                if (targetStatus == PreservationStatus.UNKNOWN) {
                    List<Predicate> unknownPredicates = new ArrayList<>();
                    unknownPredicates.add(cb.isNull(root.get("preservationStatus")));
                    unknownPredicates.add(cb.equal(root.get("preservationStatus"), ""));

                    List<Predicate> keywordPredicates = new ArrayList<>();
                    for (String kw : keywords) {
                        keywordPredicates.add(cb.like(root.get("preservationStatus"), "%" + kw + "%"));
                    }
                    Predicate keywordOr = cb.or(keywordPredicates.toArray(new Predicate[0]));

                    Predicate notMatched = cb.not(buildMatchAnyStatusPredicate(root, cb));
                    unknownPredicates.add(cb.and(notMatched, cb.isNotNull(root.get("preservationStatus")), cb.notEqual(root.get("preservationStatus"), "")));

                    predicates.add(cb.or(unknownPredicates.toArray(new Predicate[0])));
                } else {
                    List<Predicate> statusPredicates = new ArrayList<>();
                    for (String kw : keywords) {
                        statusPredicates.add(cb.like(root.get("preservationStatus"), "%" + kw + "%"));
                    }
                    predicates.add(cb.or(statusPredicates.toArray(new Predicate[0])));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Predicate buildMatchAnyStatusPredicate(Root<Post> root, CriteriaBuilder cb) {
        List<Predicate> allMatchPredicates = new ArrayList<>();
        for (PreservationStatus status : PreservationStatus.values()) {
            if (status == PreservationStatus.UNKNOWN) continue;
            for (String kw : status.getMatchKeywords()) {
                allMatchPredicates.add(cb.like(root.get("preservationStatus"), "%" + kw + "%"));
            }
        }
        return cb.or(allMatchPredicates.toArray(new Predicate[0]));
    }

    public long countBySpecification(Long categoryId, Long eraId, String preservationStatus) {
        Specification<Post> spec = buildSpecification(categoryId, eraId, preservationStatus);
        return postRepository.count(spec);
    }

    private void normalizePreservationStatus(Post post) {
        if (post.getPreservationStatus() == null || post.getPreservationStatus().isBlank()) {
            post.setPreservationStatus(PreservationStatus.UNKNOWN.getLabel());
        } else {
            post.setPreservationStatus(
                    PreservationStatus.fromString(post.getPreservationStatus()).getLabel());
        }
    }

    @Cacheable(value = "hotPosts", key = "'top10'")
    public List<Post> getHotPosts() {
        List<Post> posts = postRepository.findTop10ByOrderByViewCountDesc();
        posts.forEach(this::populateCategoryAndEraNames);
        posts.forEach(this::normalizeImages);
        posts.forEach(this::normalizePreservationStatus);
        return posts;
    }

    @CacheEvict(value = {"hotPosts", "categories", "eras"}, allEntries = true)
    @Transactional
    public Post getPostById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> 
            new GlobalExceptionHandler.ResourceNotFoundException("档案编号 " + id + " 不存在，该回忆可能已被删除或从未存在"));
        postRepository.incrementViewCount(id);
        post.setViewCount((post.getViewCount() == null ? 0 : post.getViewCount()) + 1);
        populateCategoryAndEraNames(post);
        populateTimelineEvents(post);
        normalizeImages(post);
        normalizePreservationStatus(post);
        return post;
    }

    @CacheEvict(value = {"hotPosts", "archiveStats", "categories", "eras"}, allEntries = true)
    @Transactional
    public Post createPost(Post post, List<MultipartFile> images) throws IOException {
        return createPost(post, images, null);
    }

    @CacheEvict(value = {"hotPosts", "archiveStats", "categories", "eras"}, allEntries = true)
    @Transactional
    public Post createPost(Post post, List<MultipartFile> images, List<TimelineEvent> timelineEvents) throws IOException {
        validateEraAndCategory(post.getEraId(), post.getCategoryId());

        if (images != null && !images.isEmpty()) {
            List<PostImage> postImages = new ArrayList<>();

            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);
                PostImage postImage = imageProcessingService.processImage(image, i);
                postImages.add(postImage);
            }
            post.setImages(postImages);
        }

        if (post.getPreservationStatus() != null && !post.getPreservationStatus().isBlank()) {
            post.setPreservationStatus(PreservationStatus.fromString(post.getPreservationStatus()).getLabel());
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

    private void validateEraAndCategory(Long eraId, Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("所属品类不能为空");
        }
        if (eraId == null) {
            throw new IllegalArgumentException("所属年代不能为空");
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException(
                        "类别编号 " + categoryId + " 不存在，请选择有效的品类"));
        Era era = eraRepository.findById(eraId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException(
                        "年代编号 " + eraId + " 不存在，请选择有效的年代"));
        EraCategoryValidator.validate(era, category);
    }

    @CacheEvict(value = {"hotPosts", "categories", "eras"}, allEntries = true)
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
        if (post.getSourceType() != null) {
            post.setSourceTypeName(post.getSourceType().getLabel());
            post.setSourceTypeIcon(post.getSourceType().getIcon());
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
            if (post.getSourceType() == null && post.getItemSource() != null && !post.getItemSource().isBlank()) {
                SourceType inferredType = SourceType.fromString(post.getItemSource());
                if (inferredType != null) {
                    post.setSourceType(inferredType);
                }
            }
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

        if (post.getSourceType() == null && post.getItemSource() != null && !post.getItemSource().isBlank()) {
            SourceType inferredType = SourceType.fromString(post.getItemSource());
            if (inferredType != null) {
                post.setSourceType(inferredType);
            }
        }
    }

    private void populateTimelineEvents(Post post) {
        try {
            List<TimelineEvent> events = timelineEventRepository.findByPostIdOrderByEventDateAscSortOrderAsc(post.getId());
            if (events != null) {
                events.forEach(event -> {
                    if (event.getEventType() == null) {
                        event.setEventType(TimelineEvent.EventType.USAGE);
                    }
                });
            }
            post.setTimelineEvents(events);
        } catch (Exception e) {
            post.setTimelineEvents(java.util.Collections.emptyList());
        }
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
            if (img.getUrl() != null) {
                if (img.getOriginalUrl() == null) {
                    img.setOriginalUrl(img.getUrl());
                }
                if (img.getCompressedUrl() == null) {
                    img.setCompressedUrl(img.getUrl());
                }
                if (img.getThumbnailUrl() == null) {
                    img.setThumbnailUrl(img.getUrl());
                }
            }
            if (img.getProcessingStatus() == null) {
                img.setProcessingStatus("COMPLETED");
            }
            if (img.getWidth() != null && img.getHeight() != null && img.getHeight() != 0) {
                if (img.getDisplayRatio() == null) {
                    img.setDisplayRatio((double) img.getWidth() / img.getHeight());
                }
            }
            if (img.getOriginalWidth() == null && img.getWidth() != null) {
                img.setOriginalWidth(img.getWidth());
            }
            if (img.getOriginalHeight() == null && img.getHeight() != null) {
                img.setOriginalHeight(img.getHeight());
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

    public long countPosts(Long categoryId, Long eraId, String preservationStatus) {
        return countBySpecification(categoryId, eraId, preservationStatus);
    }

    public Map<Long, Long> getCountByEraGrouped() {
        List<Object[]> results = postRepository.countByEraGrouped();
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : results) {
            Long eraId = (Long) row[0];
            Long count = (Long) row[1];
            map.put(eraId, count);
        }
        return map;
    }

    public Map<Long, Long> getCountByCategoryGrouped() {
        List<Object[]> results = postRepository.countByCategoryGrouped();
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : results) {
            Long categoryId = (Long) row[0];
            Long count = (Long) row[1];
            map.put(categoryId, count);
        }
        return map;
    }

    public Map<String, Long> getCountByPreservationStatusGrouped() {
        List<Object[]> results = postRepository.countByPreservationStatusGrouped();
        Map<String, Long> normalizedMap = new LinkedHashMap<>();
        for (Object[] row : results) {
            String rawStatus = (String) row[0];
            Long count = (Long) row[1];
            String normalizedLabel = PreservationStatus.fromString(rawStatus).getLabel();
            normalizedMap.merge(normalizedLabel, count, Long::sum);
        }
        return normalizedMap;
    }

    public Map<String, Long> getCountByEraAndCategoryGrouped() {
        List<Object[]> results = postRepository.countByEraAndCategoryGrouped();
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : results) {
            Long eraId = (Long) row[0];
            Long categoryId = (Long) row[1];
            Long count = (Long) row[2];
            map.put(eraId + "_" + categoryId, count);
        }
        return map;
    }

    public Map<String, Long> getCountByEraAndPreservationStatusGrouped() {
        List<Object[]> results = postRepository.countByEraAndPreservationStatusGrouped();
        Map<String, Long> normalizedMap = new LinkedHashMap<>();
        for (Object[] row : results) {
            Long eraId = (Long) row[0];
            String rawStatus = (String) row[1];
            Long count = (Long) row[2];
            String normalizedLabel = PreservationStatus.fromString(rawStatus).getLabel();
            String key = eraId + "_" + normalizedLabel;
            normalizedMap.merge(key, count, Long::sum);
        }
        return normalizedMap;
    }

    public Map<String, Long> getCountByCategoryAndPreservationStatusGrouped() {
        List<Object[]> results = postRepository.countByCategoryAndPreservationStatusGrouped();
        Map<String, Long> normalizedMap = new LinkedHashMap<>();
        for (Object[] row : results) {
            Long categoryId = (Long) row[0];
            String rawStatus = (String) row[1];
            Long count = (Long) row[2];
            String normalizedLabel = PreservationStatus.fromString(rawStatus).getLabel();
            String key = categoryId + "_" + normalizedLabel;
            normalizedMap.merge(key, count, Long::sum);
        }
        return normalizedMap;
    }

    @Cacheable(value = "archiveStats", key = "'all'")
    public DetailedStatsDTO getArchiveStats() {
        DetailedStatsDTO stats = new DetailedStatsDTO();

        List<Era> eras = eraRepository.findAllByOrderBySortOrderAsc();
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAsc();
        List<PreservationStatus> statuses = PreservationStatus.getAllStatuses();

        long totalPosts = countPosts(null, null, null);
        stats.setTotalPosts(totalPosts);
        stats.setTotalEras(eras.size());
        stats.setTotalCategories(categories.size());
        stats.setTotalPreservationStatuses(statuses.size());

        Map<Long, Long> eraCountMap = getCountByEraGrouped();
        Map<Long, Long> categoryCountMap = getCountByCategoryGrouped();
        Map<String, Long> statusCountMap = getCountByPreservationStatusGrouped();
        Map<String, Long> eraCategoryCountMap = getCountByEraAndCategoryGrouped();
        Map<String, Long> eraStatusCountMap = getCountByEraAndPreservationStatusGrouped();
        Map<String, Long> categoryStatusCountMap = getCountByCategoryAndPreservationStatusGrouped();

        Map<String, Long> eraDistribution = new LinkedHashMap<>();
        Map<String, Long> categoryDistribution = new LinkedHashMap<>();
        Map<String, Long> preservationStatusDistribution = new LinkedHashMap<>();

        List<DetailedStatsDTO.EraStatsDTO> eraStatsList = new ArrayList<>();
        List<DetailedStatsDTO.CategoryStatsDTO> categoryStatsList = new ArrayList<>();
        List<DetailedStatsDTO.PreservationStatusStatsDTO> preservationStatusStatsList = new ArrayList<>();

        for (Era era : eras) {
            DetailedStatsDTO.EraStatsDTO eraStats = new DetailedStatsDTO.EraStatsDTO();
            eraStats.setEraId(era.getId());
            eraStats.setEraName(era.getName());
            eraStats.setYearStart(era.getYearStart());
            eraStats.setYearEnd(era.getYearEnd());

            long eraTotal = eraCountMap.getOrDefault(era.getId(), 0L);
            eraStats.setTotalCount(eraTotal);
            eraDistribution.put(era.getName(), eraTotal);

            Map<String, Long> byCategory = new LinkedHashMap<>();
            for (Category cat : categories) {
                String key = era.getId() + "_" + cat.getId();
                byCategory.put(cat.getName(), eraCategoryCountMap.getOrDefault(key, 0L));
            }
            eraStats.setByCategory(byCategory);

            Map<String, Long> byPreservationStatus = new LinkedHashMap<>();
            for (PreservationStatus status : statuses) {
                String key = era.getId() + "_" + status.getLabel();
                byPreservationStatus.put(status.getLabel(), eraStatusCountMap.getOrDefault(key, 0L));
            }
            eraStats.setByPreservationStatus(byPreservationStatus);

            eraStatsList.add(eraStats);
        }
        stats.setEraStats(eraStatsList);
        stats.setEraDistribution(eraDistribution);

        for (Category cat : categories) {
            DetailedStatsDTO.CategoryStatsDTO catStats = new DetailedStatsDTO.CategoryStatsDTO();
            catStats.setCategoryId(cat.getId());
            catStats.setCategoryName(cat.getName());
            catStats.setCategoryIcon(cat.getIcon());

            long catTotal = categoryCountMap.getOrDefault(cat.getId(), 0L);
            catStats.setTotalCount(catTotal);
            categoryDistribution.put(cat.getName(), catTotal);

            Map<String, Long> byEra = new LinkedHashMap<>();
            for (Era era : eras) {
                String key = era.getId() + "_" + cat.getId();
                byEra.put(era.getName(), eraCategoryCountMap.getOrDefault(key, 0L));
            }
            catStats.setByEra(byEra);

            Map<String, Long> byPreservationStatus = new LinkedHashMap<>();
            for (PreservationStatus status : statuses) {
                String key = cat.getId() + "_" + status.getLabel();
                byPreservationStatus.put(status.getLabel(), categoryStatusCountMap.getOrDefault(key, 0L));
            }
            catStats.setByPreservationStatus(byPreservationStatus);

            categoryStatsList.add(catStats);
        }
        stats.setCategoryStats(categoryStatsList);
        stats.setCategoryDistribution(categoryDistribution);

        for (PreservationStatus status : statuses) {
            DetailedStatsDTO.PreservationStatusStatsDTO statusStats = new DetailedStatsDTO.PreservationStatusStatsDTO();
            statusStats.setStatus(status.name());
            statusStats.setLabel(status.getLabel());
            statusStats.setIcon(status.getIcon());
            statusStats.setColor(status.getColor());

            long statusTotal = statusCountMap.getOrDefault(status.getLabel(), 0L);
            statusStats.setTotalCount(statusTotal);
            preservationStatusDistribution.put(status.getLabel(), statusTotal);

            Map<String, Long> byCategory = new LinkedHashMap<>();
            for (Category cat : categories) {
                String key = cat.getId() + "_" + status.getLabel();
                byCategory.put(cat.getName(), categoryStatusCountMap.getOrDefault(key, 0L));
            }
            statusStats.setByCategory(byCategory);

            Map<String, Long> byEra = new LinkedHashMap<>();
            for (Era era : eras) {
                String key = era.getId() + "_" + status.getLabel();
                byEra.put(era.getName(), eraStatusCountMap.getOrDefault(key, 0L));
            }
            statusStats.setByEra(byEra);

            preservationStatusStatsList.add(statusStats);
        }
        stats.setPreservationStatusStats(preservationStatusStatsList);
        stats.setPreservationStatusDistribution(preservationStatusDistribution);

        return stats;
    }
}
