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

            List<Post> posts = postService.getPostsByEra(categoryId, era.getId(), preservationStatus);

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
                } else {
                    catGroup.put("categoryId", catId);
                    catGroup.put("categoryName", "未分类");
                    catGroup.put("categoryIcon", null);
                }
                List<Post> items = entry.getValue();
                catGroup.put("items", items);
                catGroup.put("count", (long) items.size());
                categoryGroups.add(catGroup);
            }

            eraGroup.put("categories", categoryGroups);
            eraGroup.put("totalCount", (long) posts.size());
            result.add(eraGroup);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<DetailedStatsDTO> getArchiveStats() {
        return ResponseEntity.ok(postService.getArchiveStats());
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
