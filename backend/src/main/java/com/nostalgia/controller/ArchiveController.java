package com.nostalgia.controller;

import com.nostalgia.dto.DetailedStatsDTO;
import com.nostalgia.entity.Category;
import com.nostalgia.entity.Era;
import com.nostalgia.entity.Post;
import com.nostalgia.service.CategoryService;
import com.nostalgia.service.EraService;
import com.nostalgia.service.PostService;
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
        Map<String, Object> result = new HashMap<>();
        List<Era> eras = eraService.getAllEras();
        List<Category> categories = categoryService.getAllCategories();
        result.put("eras", eras);
        result.put("categories", categories);
        result.put("preservationStatuses", com.nostalgia.entity.PreservationStatus.getAllStatuses());

        org.springframework.data.domain.Page<Post> postsPage = postService.getPosts(categoryId, eraId, preservationStatus, page, size);
        List<Post> posts = postsPage.getContent();
        posts.forEach(this::normalizePost);

        result.put("posts", posts);
        result.put("totalElements", postsPage.getTotalElements());
        result.put("totalPages", postsPage.getTotalPages());
        result.put("currentPage", page);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/grouped")
    public ResponseEntity<List<Map<String, Object>>> getGroupedArchives(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String preservationStatus) {
        List<Era> eras = eraService.getAllEras();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Era era : eras) {
            Map<String, Object> eraGroup = new LinkedHashMap<>();
            eraGroup.put("eraId", era.getId());
            eraGroup.put("eraName", era.getName());
            eraGroup.put("yearStart", era.getYearStart());
            eraGroup.put("yearEnd", era.getYearEnd());

            org.springframework.data.domain.Page<Post> postsPage = postService.getPosts(categoryId, era.getId(), preservationStatus, 0, 100);
            List<Post> posts = postsPage.getContent();
            posts.forEach(this::normalizePost);

            Map<Long, List<Post>> categoryMap = posts.stream()
                    .collect(Collectors.groupingBy(Post::getCategoryId, LinkedHashMap::new, Collectors.toList()));

            List<Map<String, Object>> categoryGroups = new ArrayList<>();
            for (Map.Entry<Long, List<Post>> entry : categoryMap.entrySet()) {
                Map<String, Object> catGroup = new LinkedHashMap<>();
                Long catId = entry.getKey();
                categoryService.getCategoryById(catId).ifPresent(cat -> {
                    catGroup.put("categoryId", cat.getId());
                    catGroup.put("categoryName", cat.getName());
                    catGroup.put("categoryIcon", cat.getIcon());
                });
                catGroup.put("items", entry.getValue());
                catGroup.put("count", entry.getValue().size());
                categoryGroups.add(catGroup);
            }

            eraGroup.put("categories", categoryGroups);
            eraGroup.put("totalCount", posts.size());
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
        org.springframework.data.domain.Page<Post> allPosts = postService.getPosts(null, null, null, 0, Integer.MAX_VALUE);

        stats.setTotalPosts(allPosts.getTotalElements());
        stats.setTotalEras(eras.size());
        stats.setTotalCategories(categories.size());
        stats.setTotalPreservationStatuses(statuses.size());

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

            org.springframework.data.domain.Page<Post> eraPosts = postService.getPosts(null, era.getId(), null, 0, 1);
            long eraTotal = eraPosts.getTotalElements();
            eraStats.setTotalCount(eraTotal);
            eraDistribution.put(era.getName(), eraTotal);

            Map<String, Long> byCategory = new LinkedHashMap<>();
            for (Category cat : categories) {
                org.springframework.data.domain.Page<Post> catEraPosts = postService.getPosts(cat.getId(), era.getId(), null, 0, 1);
                byCategory.put(cat.getName(), catEraPosts.getTotalElements());
            }
            eraStats.setByCategory(byCategory);

            Map<String, Long> byPreservationStatus = new LinkedHashMap<>();
            for (com.nostalgia.entity.PreservationStatus status : statuses) {
                org.springframework.data.domain.Page<Post> statusEraPosts = postService.getPosts(null, era.getId(), status.getLabel(), 0, 1);
                byPreservationStatus.put(status.getLabel(), statusEraPosts.getTotalElements());
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

            org.springframework.data.domain.Page<Post> catPosts = postService.getPosts(cat.getId(), null, null, 0, 1);
            long catTotal = catPosts.getTotalElements();
            catStats.setTotalCount(catTotal);
            categoryDistribution.put(cat.getName(), catTotal);

            Map<String, Long> byEra = new LinkedHashMap<>();
            for (Era era : eras) {
                org.springframework.data.domain.Page<Post> eraCatPosts = postService.getPosts(cat.getId(), era.getId(), null, 0, 1);
                byEra.put(era.getName(), eraCatPosts.getTotalElements());
            }
            catStats.setByEra(byEra);

            Map<String, Long> byPreservationStatus = new LinkedHashMap<>();
            for (com.nostalgia.entity.PreservationStatus status : statuses) {
                org.springframework.data.domain.Page<Post> statusCatPosts = postService.getPosts(cat.getId(), null, status.getLabel(), 0, 1);
                byPreservationStatus.put(status.getLabel(), statusCatPosts.getTotalElements());
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

            org.springframework.data.domain.Page<Post> statusPosts = postService.getPosts(null, null, status.getLabel(), 0, 1);
            long statusTotal = statusPosts.getTotalElements();
            statusStats.setTotalCount(statusTotal);
            preservationStatusDistribution.put(status.getLabel(), statusTotal);

            Map<String, Long> byCategory = new LinkedHashMap<>();
            for (Category cat : categories) {
                org.springframework.data.domain.Page<Post> catStatusPosts = postService.getPosts(cat.getId(), null, status.getLabel(), 0, 1);
                byCategory.put(cat.getName(), catStatusPosts.getTotalElements());
            }
            statusStats.setByCategory(byCategory);

            Map<String, Long> byEra = new LinkedHashMap<>();
            for (Era era : eras) {
                org.springframework.data.domain.Page<Post> eraStatusPosts = postService.getPosts(null, era.getId(), status.getLabel(), 0, 1);
                byEra.put(era.getName(), eraStatusPosts.getTotalElements());
            }
            statusStats.setByEra(byEra);

            preservationStatusStatsList.add(statusStats);
        }
        stats.setPreservationStatusStats(preservationStatusStatsList);
        stats.setPreservationStatusDistribution(preservationStatusDistribution);

        return ResponseEntity.ok(stats);
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
