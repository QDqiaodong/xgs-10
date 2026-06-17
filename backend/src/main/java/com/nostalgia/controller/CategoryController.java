package com.nostalgia.controller;

import com.nostalgia.dto.CategoryShowcaseDTO;
import com.nostalgia.entity.Category;
import com.nostalgia.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/showcase")
    public ResponseEntity<List<CategoryShowcaseDTO>> getCategoryShowcase() {
        return ResponseEntity.ok(categoryService.getCategoryShowcase());
    }
}
