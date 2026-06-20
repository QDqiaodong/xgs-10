package com.nostalgia.service;

import com.nostalgia.dto.CategoryShowcaseDTO;
import com.nostalgia.entity.Category;
import com.nostalgia.entity.Era;
import com.nostalgia.entity.Post;
import com.nostalgia.repository.CategoryRepository;
import com.nostalgia.repository.EraRepository;
import com.nostalgia.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final EraRepository eraRepository;
    private final PostService postService;

    @Cacheable(value = "categories", key = "'all'")
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Cacheable(value = "categories", key = "'showcase'")
    public List<CategoryShowcaseDTO> getCategoryShowcase() {
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAsc();
        List<com.nostalgia.entity.Era> eras = eraRepository.findAllByOrderBySortOrderAsc();
        List<com.nostalgia.entity.PreservationStatus> statuses = com.nostalgia.entity.PreservationStatus.getAllStatuses();
        List<CategoryShowcaseDTO> showcase = new ArrayList<>();

        for (Category category : categories) {
            CategoryShowcaseDTO dto = new CategoryShowcaseDTO();
            dto.setId(category.getId());
            dto.setName(category.getName());
            dto.setIcon(category.getIcon());
            dto.setSortOrder(category.getSortOrder());

            long count = postRepository.countByCategoryId(category.getId());
            dto.setPostCount((int) count);

            List<Post> topPosts = postRepository.findTopByCategoryIdOrderByViewCountDesc(
                category.getId(), PageRequest.of(0, 3));
            topPosts.forEach(postService::populateCategoryAndEraNamesExternal);
            dto.setRepresentativePosts(topPosts);

            Map<String, Long> byEra = new LinkedHashMap<>();
            for (com.nostalgia.entity.Era era : eras) {
                byEra.put(era.getName(), postRepository.countByCategoryIdAndEraId(category.getId(), era.getId()));
            }
            dto.setByEra(byEra);

            Map<String, Long> byPreservationStatus = new LinkedHashMap<>();
            for (com.nostalgia.entity.PreservationStatus status : statuses) {
                byPreservationStatus.put(status.getLabel(),
                        postService.countBySpecification(category.getId(), null, status.getLabel()));
            }
            dto.setByPreservationStatus(byPreservationStatus);

            showcase.add(dto);
        }

        return showcase;
    }
}
