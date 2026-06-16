package com.nostalgia.controller;

import com.nostalgia.entity.Category;
import com.nostalgia.entity.Era;
import com.nostalgia.entity.Post;
import com.nostalgia.service.CategoryService;
import com.nostalgia.service.EraService;
import com.nostalgia.service.PostService;
import com.nostalgia.util.TextCleaner;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> result = new HashMap<>();
        List<Era> eras = eraService.getAllEras();
        List<Category> categories = categoryService.getAllCategories();
        result.put("eras", eras);
        result.put("categories", categories);

        org.springframework.data.domain.Page<Post> postsPage = postService.getPosts(categoryId, eraId, page, size);
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
            @RequestParam(required = false) Long categoryId) {
        List<Era> eras = eraService.getAllEras();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Era era : eras) {
            Map<String, Object> eraGroup = new LinkedHashMap<>();
            eraGroup.put("eraId", era.getId());
            eraGroup.put("eraName", era.getName());
            eraGroup.put("yearStart", era.getYearStart());
            eraGroup.put("yearEnd", era.getYearEnd());

            org.springframework.data.domain.Page<Post> postsPage = postService.getPosts(categoryId, era.getId(), 0, 100);
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
    public ResponseEntity<Map<String, Object>> getArchiveStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Era> eras = eraService.getAllEras();
        List<Category> categories = categoryService.getAllCategories();
        org.springframework.data.domain.Page<Post> allPosts = postService.getPosts(null, null, 0, Integer.MAX_VALUE);

        stats.put("totalPosts", allPosts.getTotalElements());
        stats.put("totalEras", eras.size());
        stats.put("totalCategories", categories.size());

        Map<String, Long> eraStats = new LinkedHashMap<>();
        for (Era era : eras) {
            org.springframework.data.domain.Page<Post> eraPosts = postService.getPosts(null, era.getId(), 0, 1);
            eraStats.put(era.getName(), eraPosts.getTotalElements());
        }
        stats.put("eraDistribution", eraStats);

        Map<String, Long> categoryStats = new LinkedHashMap<>();
        for (Category cat : categories) {
            org.springframework.data.domain.Page<Post> catPosts = postService.getPosts(cat.getId(), null, 0, 1);
            categoryStats.put(cat.getName(), catPosts.getTotalElements());
        }
        stats.put("categoryDistribution", categoryStats);

        return ResponseEntity.ok(stats);
    }

    private void normalizePost(Post post) {
        if (post.getPreservationStatus() == null || post.getPreservationStatus().isBlank()) {
            post.setPreservationStatus("待评估");
        }
    }
}
