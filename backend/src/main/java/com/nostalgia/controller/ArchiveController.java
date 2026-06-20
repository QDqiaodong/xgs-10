package com.nostalgia.controller;

import com.nostalgia.dto.DetailedStatsDTO;
import com.nostalgia.entity.Category;
import com.nostalgia.entity.Era;
import com.nostalgia.entity.Post;
import com.nostalgia.service.CategoryService;
import com.nostalgia.service.EraService;
import com.nostalgia.service.PostService;
import com.nostalgia.util.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/archives")
@RequiredArgsConstructor
public class ArchiveController {

    private final PostService postService;
    private final EraService eraService;
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getArchives(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long eraId,
            @RequestParam(required = false) String preservationStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int validPage = PaginationUtils.validatePage(page);
        int validSize = PaginationUtils.validateSize(size);
        
        Map<String, Object> result = new HashMap<>();
        List<Era> eras = eraService.getAllEras();
        List<Category> categories = categoryService.getAllCategories();
        result.put("eras", eras);
        result.put("categories", categories);
        result.put("preservationStatuses", com.nostalgia.entity.PreservationStatus.getAllStatuses());

        org.springframework.data.domain.Page<Post> postsPage = postService.getPosts(categoryId, eraId, preservationStatus, validPage, validSize);
        List<Post> posts = postsPage.getContent();
        posts.forEach(this::normalizePost);

        result.put("posts", posts);
        result.put("totalElements", postsPage.getTotalElements());
        result.put("totalPages", postsPage.getTotalPages());
        result.put("currentPage", validPage);
        result.put("pageSize", validSize);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/grouped")
    public ResponseEntity<List<Map<String, Object>>> getGroupedArchives(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String preservationStatus) {
        List<Era> eras = eraService.getAllEras();
        List<Category> allCategories = categoryService.getAllCategories();
        Map<Long, Category> categoryMapById = allCategories.stream()
                .collect(Collectors.toMap(Category::getId, cat -> cat));

        List<Map<String, Object>> result = new ArrayList<>();

        for (Era era : eras) {
            Map<String, Object> eraGroup = new LinkedHashMap<>();
            eraGroup.put("eraId", era.getId());
            eraGroup.put("eraName", era.getName());
            eraGroup.put("yearStart", era.getYearStart());
            eraGroup.put("yearEnd", era.getYearEnd());

            long eraTotalCount = postService.countPosts(categoryId, era.getId(), preservationStatus);

            int groupPage = PaginationUtils.validatePage(0);
            int groupSize = PaginationUtils.validateSize(100);
            org.springframework.data.domain.Page<Post> postsPage = postService.getPosts(categoryId, era.getId(), preservationStatus, groupPage, groupSize);
            List<Post> posts = postsPage.getContent();
            posts.forEach(this::normalizePost);

            Map<Long, List<Post>> categoryMap = posts.stream()
                    .collect(Collectors.groupingBy(Post::getCategoryId, LinkedHashMap::new, Collectors.toList()));

            List<Map<String, Object>> categoryGroups = new ArrayList<>();
            for (Map.Entry<Long, List<Post>> entry : categoryMap.entrySet()) {
                Map<String, Object> catGroup = new LinkedHashMap<>();
                Long catId = entry.getKey();
                Category cat = categoryMapById.get(catId);
                if (cat != null) {
                    catGroup.put("categoryId", cat.getId());
                    catGroup.put("categoryName", cat.getName());
                    catGroup.put("categoryIcon", cat.getIcon());
                }
                catGroup.put("items", entry.getValue());

                long catCount;
                if (categoryId != null) {
                    catCount = entry.getValue().size();
                } else {
                    catCount = postService.countPosts(catId, era.getId(), preservationStatus);
                }
                catGroup.put("count", catCount);
                categoryGroups.add(catGroup);
            }

            eraGroup.put("categories", categoryGroups);
            eraGroup.put("totalCount", eraTotalCount);
            result.add(eraGroup);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<DetailedStatsDTO> getArchiveStats() {
        DetailedStatsDTO stats = new DetailedStatsDTO();

        List<Era> eras = eraService.getAllEras();
        List<Category> categories = categoryService.getAllCategories();
        List<com.nostalgia.entity.PreservationStatus> statuses = com.nostalgia.entity.PreservationStatus.getAllStatuses();

        long totalPosts = postService.countPosts(null, null, null);
        stats.setTotalPosts(totalPosts);
        stats.setTotalEras(eras.size());
        stats.setTotalCategories(categories.size());
        stats.setTotalPreservationStatuses(statuses.size());

        Map<Long, Long> eraCountMap = postService.getCountByEraGrouped();
        Map<Long, Long> categoryCountMap = postService.getCountByCategoryGrouped();
        Map<String, Long> statusCountMap = postService.getCountByPreservationStatusGrouped();
        Map<String, Long> eraCategoryCountMap = postService.getCountByEraAndCategoryGrouped();
        Map<String, Long> eraStatusCountMap = postService.getCountByEraAndPreservationStatusGrouped();
        Map<String, Long> categoryStatusCountMap = postService.getCountByCategoryAndPreservationStatusGrouped();

        String unknownStatusLabel = com.nostalgia.entity.PreservationStatus.UNKNOWN.getLabel();
        long nullStatusCount = statusCountMap.getOrDefault(null, 0L);
        long blankStatusCount = statusCountMap.getOrDefault("", 0L);
        long unknownStatusCount = statusCountMap.getOrDefault(unknownStatusLabel, 0L) + nullStatusCount + blankStatusCount;

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
            for (com.nostalgia.entity.PreservationStatus status : statuses) {
                String key = era.getId() + "_" + status.getLabel();
                long count = eraStatusCountMap.getOrDefault(key, 0L);
                if (status == com.nostalgia.entity.PreservationStatus.UNKNOWN) {
                    long eraNullStatusCount = getEraNullStatusCount(era.getId(), eraStatusCountMap);
                    count += eraNullStatusCount;
                }
                byPreservationStatus.put(status.getLabel(), count);
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
            for (com.nostalgia.entity.PreservationStatus status : statuses) {
                String key = cat.getId() + "_" + status.getLabel();
                long count = categoryStatusCountMap.getOrDefault(key, 0L);
                if (status == com.nostalgia.entity.PreservationStatus.UNKNOWN) {
                    long catNullStatusCount = getCategoryNullStatusCount(cat.getId(), categoryStatusCountMap);
                    count += catNullStatusCount;
                }
                byPreservationStatus.put(status.getLabel(), count);
            }
            catStats.setByPreservationStatus(byPreservationStatus);

            categoryStatsList.add(catStats);
        }
        stats.setCategoryStats(categoryStatsList);
        stats.setCategoryDistribution(categoryDistribution);

        for (com.nostalgia.entity.PreservationStatus status : statuses) {
            DetailedStatsDTO.PreservationStatusStatsDTO statusStats = new DetailedStatsDTO.PreservationStatusStatsDTO();
            statusStats.setStatus(status.name());
            statusStats.setLabel(status.getLabel());
            statusStats.setIcon(status.getIcon());
            statusStats.setColor(status.getColor());

            long statusTotal;
            if (status == com.nostalgia.entity.PreservationStatus.UNKNOWN) {
                statusTotal = unknownStatusCount;
            } else {
                statusTotal = statusCountMap.getOrDefault(status.getLabel(), 0L);
            }
            statusStats.setTotalCount(statusTotal);
            preservationStatusDistribution.put(status.getLabel(), statusTotal);

            Map<String, Long> byCategory = new LinkedHashMap<>();
            for (Category cat : categories) {
                String key = cat.getId() + "_" + status.getLabel();
                long count = categoryStatusCountMap.getOrDefault(key, 0L);
                if (status == com.nostalgia.entity.PreservationStatus.UNKNOWN) {
                    long catNullStatusCount = getCategoryNullStatusCount(cat.getId(), categoryStatusCountMap);
                    count += catNullStatusCount;
                }
                byCategory.put(cat.getName(), count);
            }
            statusStats.setByCategory(byCategory);

            Map<String, Long> byEra = new LinkedHashMap<>();
            for (Era era : eras) {
                String key = era.getId() + "_" + status.getLabel();
                long count = eraStatusCountMap.getOrDefault(key, 0L);
                if (status == com.nostalgia.entity.PreservationStatus.UNKNOWN) {
                    long eraNullStatusCount = getEraNullStatusCount(era.getId(), eraStatusCountMap);
                    count += eraNullStatusCount;
                }
                byEra.put(era.getName(), count);
            }
            statusStats.setByEra(byEra);

            preservationStatusStatsList.add(statusStats);
        }
        stats.setPreservationStatusStats(preservationStatusStatsList);
        stats.setPreservationStatusDistribution(preservationStatusDistribution);

        return ResponseEntity.ok(stats);
    }

    private long getEraNullStatusCount(Long eraId, Map<String, Long> eraStatusCountMap) {
        long nullCount = eraStatusCountMap.getOrDefault(eraId + "_null", 0L);
        long blankCount = eraStatusCountMap.getOrDefault(eraId + "_", 0L);
        return nullCount + blankCount;
    }

    private long getCategoryNullStatusCount(Long categoryId, Map<String, Long> categoryStatusCountMap) {
        long nullCount = categoryStatusCountMap.getOrDefault(categoryId + "_null", 0L);
        long blankCount = categoryStatusCountMap.getOrDefault(categoryId + "_", 0L);
        return nullCount + blankCount;
    }

    private void normalizePost(Post post) {
        if (post.getPreservationStatus() == null || post.getPreservationStatus().isBlank()) {
            post.setPreservationStatus(com.nostalgia.entity.PreservationStatus.UNKNOWN.getLabel());
        } else {
            post.setPreservationStatus(
                    com.nostalgia.entity.PreservationStatus.fromString(post.getPreservationStatus()).getLabel());
        }
    }
}
